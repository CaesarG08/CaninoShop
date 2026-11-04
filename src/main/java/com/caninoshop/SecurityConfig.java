package com.caninoshop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final com.caninoshop.security.InicioSesion2FAHandler segundoFactor;

    public SecurityConfig(com.caninoshop.security.InicioSesion2FAHandler segundoFactor) {
        this.segundoFactor = segundoFactor;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers("/", "/login", "/registro/**", "/acceso_denegado",
                        "/webjars/**", "/js/**", "/css/**", "/fav/**",
                        "/seguridad/2fa", "/seguridad/2fa/activar",
                        "/seguridad/2fa/verificar", "/seguridad/2fa/cancelar").permitAll()
                .requestMatchers("/seguridad/2fa/codigos").authenticated()
                .requestMatchers("/usuario/**").hasRole("ADMIN")
                .requestMatchers("/categoria/**", "/producto/guardar", "/producto/nuevo",
                        "/producto/modificar/**", "/producto/eliminar/**").hasRole("ADMIN")
                .requestMatchers("/pedido/gestion/**", "/pedido/estado/**").hasAnyRole("ADMIN", "VENDEDOR")
                .requestMatchers("/carrito/**", "/pedido/**").authenticated()
                .requestMatchers("/producto/listado", "/producto/detalle/**",
                        "/producto/buscar", "/producto/destacados").permitAll()
                .requestMatchers("/producto/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        ).formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(segundoFactor)
                .failureUrl("/login?error=true")
                .permitAll()
        ).logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        ).exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/acceso_denegado")
        );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
