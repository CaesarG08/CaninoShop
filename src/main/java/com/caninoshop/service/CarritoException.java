package com.caninoshop.service;

// Error de negocio del carrito o de la compra. Lleva la clave del mensaje (messages.properties)
// y los valores que ese mensaje necesite, para que el controlador lo muestre al usuario.
public class CarritoException extends RuntimeException {

    private final String clave;
    private final Object[] argumentos;

    public CarritoException(String clave, Object... argumentos) {
        super(clave);
        this.clave = clave;
        this.argumentos = argumentos;
    }

    public String getClave() {
        return clave;
    }

    public Object[] getArgumentos() {
        return argumentos;
    }
}
