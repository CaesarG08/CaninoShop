
package com.caninoshop.controller;

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller //Indica que es un controlador.
@RequiredArgsConstructor //Genera automáticamente un constructor con los atributos que son obligatorios
public class HomeController {

    @GetMapping("/") //Indica que el método responderá a una petición HTTP GET en la ruta principal.
    public String muestraHome(Model model) {
        return "index";
    }
}
