package com.caninoshop.controller;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import com.caninoshop.domain.DetallePedido;
import com.caninoshop.domain.Pedido;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.caninoshop.repository.EstadoPedidoRepository;
import com.caninoshop.service.PedidoService;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // Indica que esta clase atiende las peticiones de la pantalla de pedidos.
@RequiredArgsConstructor // Genera el constructor para las dependencias obligatorias.
@RequestMapping("/pedido") // Define la ruta base para las operaciones de pedidos.
public class PedidoController {

    // Servicios y repositorios usados por las acciones del controlador.
    private final PedidoService pedidoService;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final MessageSource messageSource;

    @GetMapping("/listado") // Muestra los pedidos y aplica los filtros seleccionados.
    public String listado(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
            @RequestParam(required = false) Long idEstado,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int pageSize = 50; // Cantidad de pedidos que se muestran por página.
        page = Math.max(0, page);
        Page<Pedido> paginaPedidos = pedidoService.getPedidosFiltrados(
                fechaInicio, fechaFin, idEstado, PageRequest.of(page, pageSize));
        List<Pedido> pedidos = paginaPedidos.getContent();
        boolean hasNext = paginaPedidos.hasNext();

        // Envía a la vista los pedidos, estados y filtros para conservar la consulta.
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("estados", estadoPedidoRepository.findAll());

        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("idEstadoSeleccionado", idEstado);

        return "pedido/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Pedido pedido,
            RedirectAttributes redirectAttributes) {
        try {
            Pedido pedidoActual = pedidoService.getPedido(pedido);

            if (pedidoActual == null) {
                redirectAttributes.addFlashAttribute("error",
                        messageSource.getMessage(
                                "pedido.error.noExiste", null, Locale.getDefault()));
                return "redirect:/pedido/listado";
            }

            pedidoActual.setEstadoPedido(pedido.getEstadoPedido());
            pedidoService.guardar(pedidoActual);

            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("pedido.actualizado",
                            new Object[]{pedido.getIdPedido()}, Locale.getDefault()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("pedido.error.actualizar",
                            null, Locale.getDefault()));
        }

        return "redirect:/pedido/listado";
    }

    @GetMapping("/detalle/{idPedido}") // Muestra los datos y las líneas de un pedido.
    public String detalle(@PathVariable("idPedido") Long idPedido, Model model) {
        Pedido pedidoBuscado = new Pedido();
        pedidoBuscado.setIdPedido(idPedido);
        Pedido pedido = pedidoService.getPedido(pedidoBuscado);
        if (pedido == null) {
            // Regresa al listado si el pedido solicitado no existe.
            return "redirect:/pedido/listado";
        }
        List<DetallePedido> detalles = pedidoService.getDetallesPedido(idPedido);
        BigDecimal totalDetalles = detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("pedido", pedido);
        model.addAttribute("detalles", detalles);
        model.addAttribute("totalDetalles", totalDetalles);
        return "pedido/detalle";
    }
}
