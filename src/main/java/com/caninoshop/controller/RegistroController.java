package com.caninoshop.controller;

import com.caninoshop.domain.Usuario;
import com.caninoshop.service.RegistroService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registro")
public class RegistroController {
    private final RegistroService registroService;

    public RegistroController(RegistroService registroService) {
        this.registroService = registroService;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro/nuevo";
    }

    @PostMapping("/crearUsuario")
    public String crearUsuario(@Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Revise los datos obligatorios.");
            return "registro/nuevo";
        }
        try {
            registroService.crearUsuario(usuario);
            redirectAttributes.addFlashAttribute("todoOk", "Cuenta creada correctamente. Inicie sesion.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "registro/nuevo";
        }
    }
}
