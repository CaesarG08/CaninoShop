package com.caninoshop.controller;

import com.caninoshop.domain.Producto;
import com.caninoshop.service.CategoriaService;
import com.caninoshop.service.ProductoService;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/producto")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final MessageSource messageSource;

    @GetMapping("/administrar")
    public String administrar(@RequestParam(required = false) Integer editar, Model model) {
        Producto producto = editar == null
                ? new Producto()
                : productoService.getProducto(editar).orElseGet(Producto::new);
        model.addAttribute("producto", producto);
        model.addAttribute("productos", productoService.listar(true, null, null));
        model.addAttribute("categorias", categoriaService.getCategorias(false));
        return "producto/administrar";
    }

    @GetMapping({"/listado", "/buscar"})
    public String listado(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) Integer editar,
            @RequestParam(defaultValue = "false") boolean destacados,
            @RequestParam(defaultValue = "false") boolean agotados,
            Authentication authentication,
            Model model) {
        boolean administrador = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (agotados && administrador) {
            model.addAttribute("productos", productoService.getAgotados());
        } else if (destacados) {
            model.addAttribute("productos", productoService.getDestacados());
        } else {
            model.addAttribute("productos",
                    productoService.listar(administrador, nombre, idCategoria));
        }

        Producto producto = editar == null || !administrador
                ? new Producto()
                : productoService.getProducto(editar).orElseGet(Producto::new);
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaService.getCategorias(false));
        model.addAttribute("filtroNombre", nombre);
        model.addAttribute("idCategoriaSeleccionada", idCategoria);
        model.addAttribute("esAdministrador", administrador);
        return "producto/listado";
    }

    @GetMapping("/destacados")
    public String destacados() {
        return "redirect:/producto/listado?destacados=true";
    }

    @GetMapping("/agotados")
    public String agotados() {
        return "redirect:/producto/listado?agotados=true";
    }

    @GetMapping("/detalle/{idProducto}")
    public String detalle(@PathVariable Integer idProducto, Model model,
            RedirectAttributes redirectAttributes) {
        return productoService.getProducto(idProducto)
                .filter(Producto::isEstado)
                .map(producto -> {
                    model.addAttribute("producto", producto);
                    return "producto/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error",
                            messageSource.getMessage(
                                    "producto.error.noExiste", null, Locale.getDefault()));
                    return "redirect:/";
                });
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("producto") Producto producto,
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(
                            "producto.error.validacion", null, Locale.getDefault()));
            return "redirect:/producto/administrar";
        }

        try {
            productoService.guardar(producto);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage(
                            "producto.guardado", null, Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/producto/administrar";
    }

    @PostMapping("/eliminar")
    public String desactivar(@RequestParam Integer idProducto,
            RedirectAttributes redirectAttributes) {
        try {
            productoService.desactivar(idProducto);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage(
                            "producto.desactivado", null, Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/producto/administrar";
    }
}
