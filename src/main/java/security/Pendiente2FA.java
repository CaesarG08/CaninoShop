package com.caninoshop.security;

import java.io.Serializable;

/** Autenticacion de contrasena pendiente de segundo factor; NO concede acceso. */
public class Pendiente2FA implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String ATRIBUTO = "CANINOSHOP_2FA_PENDIENTE";
    private static final long VIGENCIA_MILISEGUNDOS = 5 * 60 * 1000;
    private static final int INTENTOS_MAXIMOS = 5;

    private final String username;
    private final long creadoEn;
    private int intentos = 0;
    private String secretoTemporal;

    public Pendiente2FA(String username) {
        this.username = username;
        this.creadoEn = System.currentTimeMillis();
    }

    public String getUsername() { return username; }
    public String getSecretoTemporal() { return secretoTemporal; }
    public void setSecretoTemporal(String secretoTemporal) { this.secretoTemporal = secretoTemporal; }
    public boolean expirado() {
        return System.currentTimeMillis() - creadoEn > VIGENCIA_MILISEGUNDOS;
    }
    public boolean agotado() { return intentos >= INTENTOS_MAXIMOS; }
    public void registrarIntentoFallido() { intentos++; }
    public int getIntentosRestantes() { return Math.max(0, INTENTOS_MAXIMOS - intentos); }
}
