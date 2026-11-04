package com.caninoshop.controller;

import com.caninoshop.domain.DetalleCarrito;
import com.caninoshop.domain.DetallePedido;
import com.caninoshop.domain.Pedido;
import com.caninoshop.repository.UsuarioRepository;
import com.caninoshop.service.CarritoException;
import com.caninoshop.service.CarritoService;
import com.caninoshop.service.CompraService;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // Atiende las pantallas del carrito, el pago y la confirmación del pedido.
@RequiredArgsConstructor
@RequestMapping("/carrito")
public class CarritoController {

    // Se comunica con la capa de servicio.
    private final CarritoService carritoService;
    private final CompraService compraService;
    private final UsuarioRepository usuarioRepository;
    private final MessageSource messageSource;

    // HU12: muestra el carrito con sus productos y el total.
    @GetMapping("/listado")
    public String listado(Principal principal, Model model) {
        List<DetalleCarrito> detalles = carritoService.getDetalles(principal.getName());
        model.addAttribute("detalles", detalles);
        model.addAttribute("total", carritoService.calcularTotal(detalles));
        return "carrito/listado";
    }

    // HU11: agrega un producto al carrito (se invoca desde el catálogo).
    @PostMapping("/agregar")
    public String agregar(@RequestParam Integer idProducto,
            @RequestParam(defaultValue = "1") Integer cantidad,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            carritoService.agregar(principal.getName(), idProducto, cantidad);
            redirectAttributes.addFlashAttribute("todoOk", mensaje("carrito.agregado"));
        } catch (CarritoException e) {
            redirectAttributes.addFlashAttribute("error", mensaje(e));
        }
        return "redirect:/carrito/listado";
    }

    // HU12: cambia la cantidad de un producto.
    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Integer idDetalle,
            @RequestParam Integer cantidad,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            carritoService.actualizarCantidad(principal.getName(), idDetalle, cantidad);
            redirectAttributes.addFlashAttribute("todoOk", mensaje("carrito.actualizado"));
        } catch (CarritoException e) {
            redirectAttributes.addFlashAttribute("error", mensaje(e));
        }
        return "redirect:/carrito/listado";
    }

    // HU12: quita un producto del carrito.
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idDetalle,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            carritoService.eliminar(principal.getName(), idDetalle);
            redirectAttributes.addFlashAttribute("todoOk", mensaje("carrito.eliminado"));
        } catch (CarritoException e) {
            redirectAttributes.addFlashAttribute("error", mensaje(e));
        }
        return "redirect:/carrito/listado";
    }

    // HU12: vacía el carrito completo.
    @PostMapping("/vaciar")
    public String vaciar(Principal principal, RedirectAttributes redirectAttributes) {
        carritoService.vaciar(principal.getName());
        redirectAttributes.addFlashAttribute("todoOk", mensaje("carrito.vaciado"));
        return "redirect:/carrito/listado";
    }

    // HU13: resumen de la compra antes de pagar (requiere sesión activa, lo valida SecurityConfig).
    @GetMapping("/pagar")
    public String pagar(Principal principal, Model model, RedirectAttributes redirectAttributes) {
        List<DetalleCarrito> detalles = carritoService.getDetalles(principal.getName());
        if (detalles.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", mensaje("carrito.error.vacio"));
            return "redirect:/carrito/listado";
        }
        model.addAttribute("detalles", detalles);
        model.addAttribute("total", carritoService.calcularTotal(detalles));
        model.addAttribute("cliente", usuarioRepository.findByCorreo(principal.getName()).orElse(null));
        return "carrito/pagar";
    }

    // HU14: confirma la compra, registra el pedido y descuenta el stock.
    @PostMapping("/confirmar")
    public String confirmar(Principal principal, RedirectAttributes redirectAttributes) {
        try {
            Pedido pedido = compraService.confirmarCompra(principal.getName());
            return "redirect:/carrito/confirmacion/" + pedido.getIdPedido();
        } catch (CarritoException e) {
            redirectAttributes.addFlashAttribute("error", mensaje(e));
            return "redirect:/carrito/listado";
        }
    }

    // HU14: pantalla con el resultado del pedido recién creado.
    @GetMapping("/confirmacion/{idPedido}")
    public String confirmacion(@PathVariable Long idPedido,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Pedido pedido = compraService.getPedidoCliente(principal.getName(), idPedido);
            List<DetallePedido> detalles = compraService.getDetallesPedido(idPedido);
            model.addAttribute("pedido", pedido);
            model.addAttribute("detalles", detalles);
            model.addAttribute("nombres", compraService.getNombresProductos(detalles));
            model.addAttribute("totalDetalles", sumar(detalles));
            return "carrito/confirmacion";
        } catch (CarritoException e) {
            redirectAttributes.addFlashAttribute("error", mensaje(e));
            return "redirect:/carrito/listado";
        }
    }

    // Suma los subtotales de las líneas del pedido.
    private BigDecimal sumar(List<DetallePedido> detalles) {
        return detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String mensaje(String clave) {
        return messageSource.getMessage(clave, null, Locale.getDefault());
    }

    private String mensaje(CarritoException e) {
        return messageSource.getMessage(e.getClave(), e.getArgumentos(), Locale.getDefault());
    }
}
