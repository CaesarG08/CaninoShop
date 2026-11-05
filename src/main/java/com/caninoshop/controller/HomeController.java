
package com.caninoshop.controller;

import com.caninoshop.service.CategoriaService;
import com.caninoshop.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    @GetMapping("/")
    public String muestraHome(@RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer idCategoria,
            Authentication authentication, Model model) {
        boolean esAdministrador = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        boolean esEncargado = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ENCARGADO"));
        model.addAttribute("productos", productoService.listar(false, nombre, idCategoria));
        model.addAttribute("categorias", categoriaService.getCategorias(false));
        model.addAttribute("idCategoriaSeleccionada", idCategoria);
        model.addAttribute("filtroNombre", nombre);
        model.addAttribute("esAdministrador", esAdministrador);
        model.addAttribute("esEncargado", esEncargado);
        return "index";
    }
}
