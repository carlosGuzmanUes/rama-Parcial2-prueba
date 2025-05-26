package com.example.rama.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        
        // Configuración de autorización
        http.authorizeHttpRequests(authz -> authz
            // Rutas públicas (sin autenticación)
            .requestMatchers("/login/**", "/oauth2/**", "/actuator/health/**").permitAll()
            // TODAS las demás rutas requieren autenticación
            .anyRequest().authenticated()
        );

        // Configuración OAuth2 Login
        http.oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .defaultSuccessUrl("/", true)  // Redirigir a página principal después del login
            .failureUrl("/login?error=true")
        );

        // Configuración de Logout
        http.logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout=true")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")
        );

        // Configuración de Vaadin (debe ir al final)
        super.configure(http);
    }
}