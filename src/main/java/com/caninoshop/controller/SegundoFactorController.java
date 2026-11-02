package com.caninoshop.controller;

import com.caninoshop.security.Pendiente2FA;
import com.caninoshop.service.Autenticacion2FAService;
import com.caninoshop.service.UsuarioDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Rutas accesibles solo con una contrasena ya validada en la sesion pendiente. */
@Controller
@RequestMapping("/seguridad/2fa")
public class SegundoFactorController {
    private static final String CODIGOS = "CANINOSHOP_CODIGOS_RECUPERACION";
    private final Autenticacion2FAService dosFactores;
    private final UsuarioDetailsService usuarios;
    private final HttpSessionSecurityContextRepository repositorioContexto =
            new HttpSessionSecurityContextRepository();

    public SegundoFactorController(Autenticacion2FAService dosFactores,
            UsuarioDetailsService usuarios) {
        this.dosFactores = dosFactores;
        this.usuarios = usuarios;
    }

    @GetMapping
    public String pantalla(HttpServletRequest request, HttpServletResponse response, Model model) {
        noCache(response);
        Pendiente2FA pendiente = pendienteValido(request);
        if (pendiente == null) { return "redirect:/login"; }
        if (dosFactores.yaHabilitado(pendiente.getUsername())) {
            model.addAttribute("intentosRestantes", pendiente.getIntentosRestantes());
            return "seguridad/verificar";
        }
        if (pendiente.getSecretoTemporal() == null) {
            pendiente.setSecretoTemporal(dosFactores.crearSecreto());
        }
        model.addAttribute("qrBase64", dosFactores.crearQrBase64(pendiente.getUsername(), pendiente.getSecretoTemporal()));
        model.addAttribute("secreto", pendiente.getSecretoTemporal());
        model.addAttribute("intentosRestantes", pendiente.getIntentosRestantes());
        return "seguridad/activar";
    }

    @PostMapping("/activar")
    public String activar(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(name = "codigo", defaultValue = "") String codigo, Model model) {
        noCache(response);
        Pendiente2FA pendiente = pendienteValido(request);
        if (pendiente == null) { return "redirect:/login"; }
        if (dosFactores.yaHabilitado(pendiente.getUsername()) || pendiente.getSecretoTemporal() == null) {
            return "redirect:/seguridad/2fa";
        }
        List<String> codigos = dosFactores.habilitar(
                pendiente.getUsername(), pendiente.getSecretoTemporal(), codigo.trim());
        if (codigos == null) {
            return errorIntento(request, pendiente, model, "El codigo no es valido o ya expiro.", response);
        }
        request.getSession().setAttribute(CODIGOS, codigos);
        autenticarCompletamente(request, response, pendiente);
        return "redirect:/seguridad/2fa/codigos";
    }

    @PostMapping("/verificar")
    public String verificar(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(name = "codigo", defaultValue = "") String codigo, Model model) {
        noCache(response);
        Pendiente2FA pendiente = pendienteValido(request);
        if (pendiente == null) { return "redirect:/login"; }
        if (!dosFactores.yaHabilitado(pendiente.getUsername())) {
            return "redirect:/seguridad/2fa";
        }
        if (!dosFactores.verificar(pendiente.getUsername(), codigo.trim())) {
            return errorIntento(request, pendiente, model, "Codigo incorrecto, utilizado o caducado.", response);
        }
        autenticarCompletamente(request, response, pendiente);
        return "redirect:/";
    }

    @GetMapping("/codigos")
    @SuppressWarnings("unchecked")
    public String mostrarCodigos(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication, Model model) {
        noCache(response);
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        HttpSession session = request.getSession(false);
        if (session == null) { return "redirect:/"; }
        List<String> codigos = (List<String>) session.getAttribute(CODIGOS);
        if (codigos == null) { return "redirect:/"; }
        session.removeAttribute(CODIGOS);
        model.addAttribute("codigos", codigos);
        return "seguridad/codigos";
    }

    @PostMapping("/cancelar")
    public String cancelar(HttpServletRequest request, HttpServletResponse response) {
        noCache(response);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(Pendiente2FA.ATRIBUTO);
            session.removeAttribute(CODIGOS);
        }
        return "redirect:/login";
    }

    private String errorIntento(HttpServletRequest request, Pendiente2FA pendiente,
            Model model, String error, HttpServletResponse response) {
        pendiente.registrarIntentoFallido();
        if (pendiente.agotado()) {
            request.getSession().removeAttribute(Pendiente2FA.ATRIBUTO);
            return "redirect:/login?mfa=bloqueado";
        }
        model.addAttribute("error", error);
        model.addAttribute("intentosRestantes", pendiente.getIntentosRestantes());
        if (dosFactores.yaHabilitado(pendiente.getUsername())) {
            return "seguridad/verificar";
        }
        model.addAttribute("secreto", pendiente.getSecretoTemporal());
        model.addAttribute("qrBase64", dosFactores.crearQrBase64(
                pendiente.getUsername(), pendiente.getSecretoTemporal()));
        return "seguridad/activar";
    }

    private static Pendiente2FA pendienteValido(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) { return null; }
        Object valor = session.getAttribute(Pendiente2FA.ATRIBUTO);
        if (!(valor instanceof Pendiente2FA pendiente)) { return null; }
        if (pendiente.expirado() || pendiente.agotado()) {
            session.removeAttribute(Pendiente2FA.ATRIBUTO);
            return null;
        }
        return pendiente;
    }

    private void autenticarCompletamente(HttpServletRequest request,
            HttpServletResponse response, Pendiente2FA pendiente) {
        var detalle = usuarios.loadUserByUsername(pendiente.getUsername());
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(new UsernamePasswordAuthenticationToken(
                detalle, null, detalle.getAuthorities()));
        SecurityContextHolder.setContext(contexto);
        repositorioContexto.saveContext(contexto, request, response);
        session.removeAttribute(Pendiente2FA.ATRIBUTO);
    }

    private static void noCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
    }
}
