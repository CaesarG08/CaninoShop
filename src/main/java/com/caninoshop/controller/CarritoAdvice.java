package com.caninoshop.controller;

import com.caninoshop.service.CarritoService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice // Agrega a todas las vistas la cantidad de unidades del carrito para mostrarla en el menú.
@RequiredArgsConstructor
public class CarritoAdvice {

    private final CarritoService carritoService;

    @ModelAttribute("cantidadCarrito")
    public long cantidadCarrito(Principal principal) {
        if (principal == null) {
            return 0;
        }
        return carritoService.contarUnidades(principal.getName());
    }
}
