package com.caninoshop.controller;

import com.caninoshop.domain.DetallePedido;
import com.caninoshop.domain.Pedido;
import com.caninoshop.service.CarritoException;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // HU15: historial de pedidos del cliente que inició sesión.
@RequiredArgsConstructor
@RequestMapping("/mispedidos")
public class MisPedidosController {

    private final CompraService compraService;
    private final MessageSource messageSource;

    // Lista solo los pedidos del cliente autenticado.
    @GetMapping("/listado")
    public String listado(Principal principal, Model model) {
        List<Pedido> pedidos = compraService.getPedidosCliente(principal.getName());
        model.addAttribute("pedidos", pedidos);
        return "mispedidos/listado";
    }

    // Detalle de uno de sus pedidos; si el pedido es de otro cliente no se muestra.
    @GetMapping("/detalle/{idPedido}")
    public String detalle(@PathVariable Long idPedido,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Pedido pedido = compraService.getPedidoCliente(principal.getName(), idPedido);
            List<DetallePedido> detalles = compraService.getDetallesPedido(idPedido);
            BigDecimal total = detalles.stream()
                    .map(DetallePedido::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("pedido", pedido);
            model.addAttribute("detalles", detalles);
            model.addAttribute("nombres", compraService.getNombresProductos(detalles));
            model.addAttribute("totalDetalles", total);
            return "mispedidos/detalle";
        } catch (CarritoException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(e.getClave(), e.getArgumentos(), Locale.getDefault()));
            return "redirect:/mispedidos/listado";
        }
    }
}
