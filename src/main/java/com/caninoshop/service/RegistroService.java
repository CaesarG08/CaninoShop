package com.caninoshop.service;

import com.caninoshop.domain.Rol;
import com.caninoshop.domain.Usuario;
import com.caninoshop.repository.RolRepository;
import com.caninoshop.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void crearUsuario(Usuario datos) {
        String correo = datos.getCorreo() == null ? "" : datos.getCorreo().trim();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("El correo ya esta registrado.");
        }

        String cedula = datos.getCedula() == null ? null : datos.getCedula().trim();
        if (cedula != null && cedula.isEmpty()) {
            cedula = null;
        }
        if (cedula != null && usuarioRepository.existsByCedula(cedula)) {
            throw new IllegalArgumentException("La cedula ya esta registrada.");
        }

        Rol rolCliente = rolRepository.findByRol("CLIENTE")
                .orElseThrow(() -> new IllegalStateException(
                        "El rol CLIENTE no existe en la tabla rol."));

        Usuario usuario = new Usuario();
        usuario.setCedula(cedula);
        usuario.setNombre(datos.getNombre());
        usuario.setApellidos(datos.getApellidos());
        usuario.setCorreo(correo);
        usuario.setTelefono(datos.getTelefono());
        usuario.setDireccion(datos.getDireccion());
        usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        usuario.setActivo(true);
        usuario.setRol(rolCliente);
        usuarioRepository.save(usuario);
    }
}
