package com.example.rama.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Value("${auth0.audience}")
    private String audience;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configuración específica para Vaadin + Auth0
        http.authorizeHttpRequests(auth -> 
            auth.requestMatchers(
                    "/login**", 
                    "/oauth2/**", 
                    "/actuator/health",
                    "/VAADIN/**",
                    "/vaadinServlet/**"
                ).permitAll()
                .anyRequest().authenticated()
        );

        // Configuración OAuth2 Login
        http.oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .authorizationEndpoint(authorization -> authorization
                .authorizationRequestResolver(authorizationRequestResolver(null))
            )
            .defaultSuccessUrl("/", true)
            .failureUrl("/login?error")
        );

        // Configuración de logout
        http.logout(logout -> logout
            .logoutSuccessUrl("/login?logout")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")
        );

        // Configuración adicional para Vaadin
        super.configure(http);
    }

    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        
        DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
        
        // Personalizar la solicitud de autorización para incluir audience
        authorizationRequestResolver.setAuthorizationRequestCustomizer(
            this::customizeAuthorizationRequest);
        
        return authorizationRequestResolver;
    }

    private void customizeAuthorizationRequest(OAuth2AuthorizationRequest.Builder builder) {
        // Agregar el audience a los parámetros adicionales
        builder.additionalParameters(params -> {
            if (audience != null && !audience.isEmpty()) {
                params.put("audience", audience);
            }
        });
    }
}