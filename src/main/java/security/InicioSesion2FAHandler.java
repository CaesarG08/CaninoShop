package com.caninoshop.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

/** Al validar la contrasena, NO deja iniciar sesion hasta aprobar 2FA. */
@Component
public class InicioSesion2FAHandler implements AuthenticationSuccessHandler {
    private final HttpSessionSecurityContextRepository contextRepository =
            new HttpSessionSecurityContextRepository();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        HttpSession session = request.getSession(true);
        session.removeAttribute("CANINOSHOP_CODIGOS_RECUPERACION");
        session.setAttribute(Pendiente2FA.ATRIBUTO, new Pendiente2FA(authentication.getName()));

        // Spring Security ya valido la contrasena, pero la sesion NO debe ser autenticada aun.
        SecurityContextHolder.clearContext();
        session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        contextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response);
        response.sendRedirect(request.getContextPath() + "/seguridad/2fa");
    }
}
