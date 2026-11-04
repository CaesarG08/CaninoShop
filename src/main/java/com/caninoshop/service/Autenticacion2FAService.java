package com.caninoshop.service;

import com.caninoshop.domain.CodigoRecuperacion;
import com.caninoshop.domain.Usuario;
import com.caninoshop.repository.CodigoRecuperacionRepository;
import com.caninoshop.repository.UsuarioRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** TOTP RFC 6238 con secreto cifrado (AES-256-GCM) y codigos de recuperacion BCrypt. */
@Service
public class Autenticacion2FAService {
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final long PASO_SEGUNDOS = 30;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final CodigoRecuperacionRepository recuperaciones;
    private final PasswordEncoder passwordEncoder;
    private final SecretKeySpec claveAes;

    public Autenticacion2FAService(UsuarioRepository usuarios,
            CodigoRecuperacionRepository recuperaciones, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.recuperaciones = recuperaciones;
        this.passwordEncoder = passwordEncoder;
        String configuracion = System.getenv("CANINOSHOP_2FA_KEY");
        if (configuracion == null || configuracion.isBlank()) {
            throw new IllegalStateException("Falta CANINOSHOP_2FA_KEY: configure la variable de entorno antes de iniciar CaninoShop.");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(configuracion.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("CANINOSHOP_2FA_KEY debe estar en formato Base64.", ex);
        }
        if (bytes.length != 32) {
            throw new IllegalStateException("CANINOSHOP_2FA_KEY debe contener una clave aleatoria de 32 bytes en Base64.");
        }
        this.claveAes = new SecretKeySpec(bytes, "AES");
    }

    @Transactional(readOnly = true)
    public boolean yaHabilitado(String username) {
        Usuario usuario = usuarios.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new IllegalStateException("Cuenta no disponible."));
        return usuario.isTotpHabilitado();
    }

    public String crearSecreto() {
        byte[] bytes = new byte[20];
        ALEATORIO.nextBytes(bytes);
        return aBase32(bytes);
    }

    public String crearQrBase64(String username, String secreto) {
        String etiqueta = URLEncoder.encode("CaninoShop:" + username, StandardCharsets.UTF_8);
        String emisor = URLEncoder.encode("CaninoShop", StandardCharsets.UTF_8);
        String uri = "otpauth://totp/" + etiqueta + "?secret=" + secreto
                + "&issuer=" + emisor + "&algorithm=SHA1&digits=6&period=30";
        try {
            var matriz = new QRCodeWriter().encode(uri, BarcodeFormat.QR_CODE, 240, 240);
            var imagen = MatrixToImageWriter.toBufferedImage(matriz);
            var salida = new ByteArrayOutputStream();
            ImageIO.write(imagen, "png", salida);
            return Base64.getEncoder().encodeToString(salida.toByteArray());
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("No se pudo crear el QR para el autenticador.", ex);
        }
    }

    /** Guarda el secreto solo despues de comprobar un codigo emitido por el dispositivo. */
    @Transactional
    public List<String> habilitar(String username, String secretoTemporal, String codigo) {
        Usuario usuario = usuarios.bloquearUsuarioActivo(username)
                .orElseThrow(() -> new IllegalStateException("Cuenta no disponible."));
        if (usuario.isTotpHabilitado()) {
            throw new IllegalStateException("La doble autenticacion ya estaba activada.");
        }
        long paso = validarPaso(secretoTemporal, codigo, -1);
        if (paso < 0) { return null; }
        usuario.setTotpSecreto(cifrar(secretoTemporal, usuario.getIdUsuario()));
        usuario.setTotpHabilitado(true);
        usuario.setTotpUltimoPaso(paso);
        usuarios.save(usuario);

        // Se entregan una unica vez; en la BD se almacenan solamente hashes BCrypt.
        List<String> originales = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            String original = crearCodigoRecuperacion();
            CodigoRecuperacion nuevo = new CodigoRecuperacion();
            nuevo.setUsuario(usuario);
            nuevo.setCodigoHash(passwordEncoder.encode(original));
            recuperaciones.save(nuevo);
            originales.add(original);
        }
        return originales;
    }

    /** Acepta un TOTP aun no usado, o un codigo de recuperacion de un solo uso. */
    @Transactional
    public boolean verificar(String username, String codigo) {
        Usuario usuario = usuarios.bloquearUsuarioActivo(username)
                .orElseThrow(() -> new IllegalStateException("Cuenta no disponible."));
        if (!usuario.isTotpHabilitado() || usuario.getTotpSecreto() == null) { return false; }
        if (codigo != null && codigo.matches("[0-9]{6}")) {
            String secreto = descifrar(usuario.getTotpSecreto(), usuario.getIdUsuario());
            long paso = validarPaso(secreto, codigo, usuario.getTotpUltimoPaso());
            if (paso < 0) { return false; }
            usuario.setTotpUltimoPaso(paso);
            usuarios.save(usuario);
            return true;
        }
        if (codigo != null && codigo.matches("[A-Za-z2-7]{6}-[A-Za-z2-7]{6}")) {
            String normalizado = codigo.toUpperCase(Locale.ROOT);
            for (CodigoRecuperacion almacenado : recuperaciones.buscarPorUsuario(usuario.getIdUsuario())) {
                if (passwordEncoder.matches(normalizado, almacenado.getCodigoHash())) {
                    recuperaciones.delete(almacenado);
                    return true;
                }
            }
        }
        return false;
    }

    private long validarPaso(String secreto, String codigo, long ultimoPaso) {
        if (codigo == null || !codigo.matches("[0-9]{6}")) { return -1; }
        long actual = (System.currentTimeMillis() / 1000) / PASO_SEGUNDOS;
        // Se toleran +/- 30 segundos de diferencia del reloj del telefono.
        for (long paso = actual - 1; paso <= actual + 1; paso++) {
            if (paso <= ultimoPaso) { continue; }
            String esperado = codigoTotp(secreto, paso);
            if (MessageDigest.isEqual(esperado.getBytes(StandardCharsets.US_ASCII),
                    codigo.getBytes(StandardCharsets.US_ASCII))) {
                return paso;
            }
        }
        return -1;
    }

    private static String codigoTotp(String secreto, long paso) {
        try {
            byte[] clave = deBase32(secreto);
            byte[] contador = new byte[8];
            long copia = paso;
            for (int i = 7; i >= 0; i--) { contador[i] = (byte) (copia & 0xff); copia >>>= 8; }
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(clave, "HmacSHA1"));
            byte[] hash = mac.doFinal(contador);
            int desplazamiento = hash[hash.length - 1] & 0x0f;
            int numero = ((hash[desplazamiento] & 0x7f) << 24)
                    | ((hash[desplazamiento + 1] & 0xff) << 16)
                    | ((hash[desplazamiento + 2] & 0xff) << 8)
                    | (hash[desplazamiento + 3] & 0xff);
            return String.format(Locale.ROOT, "%06d", numero % 1000000);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo verificar el codigo TOTP.", ex);
        }
    }

    private String cifrar(String secreto, Integer idUsuario) {
        try {
            byte[] iv = new byte[12];
            ALEATORIO.nextBytes(iv);
            Cipher cifrador = Cipher.getInstance("AES/GCM/NoPadding");
            cifrador.init(Cipher.ENCRYPT_MODE, claveAes, new GCMParameterSpec(128, iv));
            cifrador.updateAAD(idUsuario.toString().getBytes(StandardCharsets.UTF_8));
            byte[] cifrado = cifrador.doFinal(secreto.getBytes(StandardCharsets.US_ASCII));
            byte[] paquete = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, paquete, 0, iv.length);
            System.arraycopy(cifrado, 0, paquete, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(paquete);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo cifrar el secreto TOTP.", ex);
        }
    }

    private String descifrar(String paqueteBase64, Integer idUsuario) {
        try {
            byte[] paquete = Base64.getDecoder().decode(paqueteBase64);
            if (paquete.length < 29) { throw new IllegalStateException("Secreto TOTP incompleto."); }
            byte[] iv = java.util.Arrays.copyOfRange(paquete, 0, 12);
            byte[] contenido = java.util.Arrays.copyOfRange(paquete, 12, paquete.length);
            Cipher cifrador = Cipher.getInstance("AES/GCM/NoPadding");
            cifrador.init(Cipher.DECRYPT_MODE, claveAes, new GCMParameterSpec(128, iv));
            cifrador.updateAAD(idUsuario.toString().getBytes(StandardCharsets.UTF_8));
            return new String(cifrador.doFinal(contenido), StandardCharsets.US_ASCII);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("No se pudo leer el secreto TOTP. Revise la clave de cifrado.", ex);
        }
    }

    private static String crearCodigoRecuperacion() {
        byte[] bytes = new byte[8];
        ALEATORIO.nextBytes(bytes);
        String clave = aBase32(bytes);
        return clave.substring(0, 6) + "-" + clave.substring(6, 12);
    }

    private static String aBase32(byte[] bytes) {
        StringBuilder salida = new StringBuilder();
        int acumulador = 0, bits = 0;
        for (byte b : bytes) {
            acumulador = (acumulador << 8) | (b & 0xff);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                salida.append(BASE32.charAt((acumulador >> bits) & 31));
            }
        }
        if (bits > 0) { salida.append(BASE32.charAt((acumulador << (5 - bits)) & 31)); }
        return salida.toString();
    }

    private static byte[] deBase32(String texto) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        int acumulador = 0, bits = 0;
        for (char letra : texto.toUpperCase(Locale.ROOT).toCharArray()) {
            if (letra == '=') { break; }
            int valor = BASE32.indexOf(letra);
            if (valor < 0) { throw new IllegalArgumentException("Secreto TOTP invalido."); }
            acumulador = (acumulador << 5) | valor;
            bits += 5;
            if (bits >= 8) {
                bits -= 8;
                salida.write((acumulador >> bits) & 0xff);
            }
        }
        return salida.toByteArray();
    }
}
