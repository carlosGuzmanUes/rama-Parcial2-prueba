package com.example.rama.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@AnonymousAllowed
public class MainLayout extends AppLayout {

    public MainLayout() {
        createDrawer();
        createHeader();
    }

    private void createHeader() {
        // Header con información del usuario autenticado
        String userName = getCurrentUserName();
        String userEmail = getCurrentUserEmail();
        
        Span userInfo = new Span("👤 " + userName);
        userInfo.getStyle()
            .set("color", "#333")
            .set("font-weight", "500")
            .set("margin-right", "10px");
            
        Span emailInfo = new Span("📧 " + userEmail);
        emailInfo.getStyle()
            .set("color", "#666")
            .set("font-size", "12px")
            .set("margin-right", "15px");

        // Botón de logout
        Button logoutButton = new Button("Cerrar Sesión", new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        logoutButton.getStyle()
            .set("background-color", "#dc3545")
            .set("color", "white");
        logoutButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.getPage().setLocation("/login")));

        HorizontalLayout userSection = new HorizontalLayout();
        VerticalLayout userDetails = new VerticalLayout(userInfo, emailInfo);
        userDetails.setSpacing(false);
        userDetails.setPadding(false);
        
        userSection.add(userDetails, logoutButton);
        userSection.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        userSection.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        userSection.setWidthFull();
        userSection.setPadding(true);
        userSection.getStyle().set("background-color", "#f8f9fa");

        addToNavbar(userSection);
    }

    private void createDrawer() {
        // Logo y título (manteniendo tu estilo original)
        Image logo = new Image("images/logo.png", "logo");
        logo.setWidth("180px");
        
        Span title = new Span("Sistema de Gestión");
        title.getStyle()
            .set("color", "Black")
            .set("font-family", "'Georgia', serif")
            .set("font-size", "26px")
            .set("text-align", "center")
            .set("margin-top", "10px");

        // Información del usuario en el sidebar
        String userName = getCurrentUserName();
        Span welcomeMsg = new Span("¡Hola, " + userName + "!");
        welcomeMsg.getStyle()
            .set("color", "#666")
            .set("font-family", "'Georgia', serif")
            .set("font-size", "14px")
            .set("font-style", "italic")
            .set("text-align", "center")
            .set("margin-bottom", "20px");

        // Layout principal del sidebar
        VerticalLayout layout = new VerticalLayout(logo, title, welcomeMsg);
        layout.getStyle().set("background-color", "#fdf5e6");
        layout.getStyle().set("height", "100vh");
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setAlignItems(Alignment.CENTER);

        // Separador visual
        Div separator = new Div();
        separator.getStyle()
            .set("width", "80%")
            .set("height", "2px")
            .set("background-color", "#ddd")
            .set("margin", "10px 0");

        // Links de navegación (usando tu estilo original)
        RouterLink homeLink = new RouterLink("🏠 Inicio", MainRedirectView.class);
        homeLink.getStyle()
            .set("color", "Black")
            .set("font-family", "'Georgia', serif")
            .set("font-size", "16px");

        RouterLink materiasLink = new RouterLink("📖 Materia", MateriaView.class);
        materiasLink.getStyle()
            .set("color", "Black")
            .set("font-family", "'Georgia', serif")
            .set("font-size", "16px");

        RouterLink actividadesLink = new RouterLink("📚 Actividades", classActivitiesView.class);
        actividadesLink.getStyle()
            .set("color", "Black")
            .set("font-family", "'Georgia', serif")
            .set("font-size", "16px");

        RouterLink gruposLink = new RouterLink("👥 Grupo", GrupoView.class);
        gruposLink.getStyle()
            .set("color", "Black")
            .set("font-family", "'Georgia', serif")
            .set("font-size", "16px");

        // Agregar todos los elementos
        layout.add(
            separator,
            homeLink,
            materiasLink, 
            actividadesLink, 
            gruposLink
        );

        addToDrawer(layout);
    }

    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) authentication.getPrincipal();
            String name = user.getAttribute("name");
            if (name == null) name = user.getAttribute("nickname");
            if (name == null) name = user.getAttribute("given_name");
            if (name == null) name = "Usuario";
            return name;
        }
        return "Usuario";
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) authentication.getPrincipal();
            String email = user.getAttribute("email");
            return email != null ? email : "email@ejemplo.com";
        }
        return "No disponible";
    }
}