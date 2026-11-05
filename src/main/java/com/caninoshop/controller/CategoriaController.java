package com.caninoshop.controller;

import com.caninoshop.domain.Categoria;
import com.caninoshop.service.CategoriaService;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/categoria")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final MessageSource messageSource;

    @GetMapping("/listado")
    public String listado(@RequestParam(required = false) Integer editar, Model model) {
        Categoria categoria = editar == null
                ? new Categoria()
                : categoriaService.getCategoria(editar).orElseGet(Categoria::new);
        model.addAttribute("categoria", categoria);
        model.addAttribute("categorias", categoriaService.getCategorias(true));
        return "categoria/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoria") Categoria categoria,
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(
                            "categoria.error.validacion", null, Locale.getDefault()));
            return "redirect:/categoria/listado";
        }

        try {
            categoriaService.guardar(categoria);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage(
                            "categoria.guardada", null, Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categoria/listado";
    }

    @PostMapping("/desactivar")
    public String desactivar(@RequestParam Integer idCategoria,
            RedirectAttributes redirectAttributes) {
        try {
            categoriaService.desactivar(idCategoria);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage(
                            "categoria.desactivada", null, Locale.getDefault()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categoria/listado";
    }
}
