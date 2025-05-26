package com.example.rama.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import jakarta.annotation.PostConstruct;

@Route(value = "")
@PageTitle("Panel Principal")
@AnonymousAllowed // Permitir acceso para manejar redirección manual
public class MainRedirectView extends VerticalLayout {

    @PostConstruct
    private void init() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        
        // Verificar autenticación y mostrar contenido apropiado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (isUserAuthenticated(authentication)) {
            showAuthenticatedContent(authentication);
        } else {
            showLoginPrompt();
        }
    }

    private boolean isUserAuthenticated(Authentication authentication) {
        return authentication != null 
            && authentication.isAuthenticated() 
            && authentication.getPrincipal() instanceof OidcUser;
    }

    private void showAuthenticatedContent(Authentication authentication) {
        OidcUser user = (OidcUser) authentication.getPrincipal();
        
        // Título de bienvenida
        H1 welcomeTitle = new H1("🎉 ¡Bienvenido!");
        welcomeTitle.getStyle().set("color", "#1976D2").set("margin-bottom", "10px");
        
        // Información del usuario
        H2 userGreeting = new H2("Hola, " + getUserName(user));
        userGreeting.getStyle().set("color", "#333").set("margin-top", "0");
        
        Paragraph userInfo = new Paragraph("📧 " + getUserEmail(user));
        userInfo.getStyle().set("color", "#666");
        
        // Botones de navegación a tus vistas existentes
        VerticalLayout navigationSection = createNavigationSection();
        
        // Botón de logout
        Button logoutButton = new Button("🚪 Cerrar Sesión");
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.addClickListener(e -> logout());
        
        // Layout para el botón de logout
        HorizontalLayout logoutLayout = new HorizontalLayout(logoutButton);
        logoutLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        logoutLayout.getStyle().set("margin-top", "30px");
        
        add(welcomeTitle, userGreeting, userInfo, navigationSection, logoutLayout);
    }

    private VerticalLayout createNavigationSection() {
        VerticalLayout section = new VerticalLayout();
        section.setAlignItems(Alignment.CENTER);
        section.setSpacing(true);
        section.getStyle()
            .set("background", "#f8f9fa")
            .set("padding", "30px")
            .set("border-radius", "12px")
            .set("border", "1px solid #dee2e6")
            .set("margin-top", "20px");

        H2 sectionTitle = new H2("📚 Acceder a:");
        sectionTitle.getStyle().set("margin-top", "0").set("color", "#495057");
        
        // Primera fila de botones
        HorizontalLayout firstRow = new HorizontalLayout();
        firstRow.setJustifyContentMode(JustifyContentMode.CENTER);
        firstRow.setSpacing(true);
        
        Button activitiesBtn = createNavigationButton("📚 Actividades", "actividades", "#007bff");
        Button groupsBtn = createNavigationButton("👥 Grupos", "grupos", "#28a745");
        Button subjectsBtn = createNavigationButton("📖 Materias", "materias", "#ffc107");
        
        firstRow.add(activitiesBtn, groupsBtn, subjectsBtn);
        
        // Segunda fila de botones
        HorizontalLayout secondRow = new HorizontalLayout();
        secondRow.setJustifyContentMode(JustifyContentMode.CENTER);
        secondRow.setSpacing(true);
        
        Button pagesBtn = createNavigationButton("📄 Páginas", "pages", "#17a2b8");
        Button profileBtn = createNavigationButton("👤 Perfil", "profile", "#6f42c1");
        
        secondRow.add(pagesBtn, profileBtn);
        
        section.add(sectionTitle, firstRow, secondRow);
        return section;
    }

    private Button createNavigationButton(String text, String route, String color) {
        Button button = new Button(text);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        button.getStyle()
            .set("background-color", color)
            .set("color", "white")
            .set("border", "none")
            .set("min-width", "140px")
            .set("margin", "5px");
        
        button.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(route)));
        
        return button;
    }

    private void showLoginPrompt() {
        H1 title = new H1("🔐 Acceso Requerido");
        title.getStyle().set("color", "#dc3545");
        
        Paragraph description = new Paragraph(
            "Necesitas iniciar sesión para acceder a la aplicación."
        );
        description.getStyle()
            .set("text-align", "center")
            .set("color", "#666")
            .set("max-width", "400px");
        
        Button loginButton = new Button("🚀 Iniciar Sesión con Auth0");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.addClickListener(e -> 
            getUI().ifPresent(ui -> 
                ui.getPage().setLocation("/oauth2/authorization/auth0")));
        
        HorizontalLayout buttonLayout = new HorizontalLayout(loginButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        buttonLayout.getStyle().set("margin-top", "20px");
        
        add(title, description, buttonLayout);
    }

    private String getUserName(OidcUser user) {
        String name = user.getAttribute("name");
        if (name == null) name = user.getAttribute("nickname");
        if (name == null) name = user.getAttribute("given_name");
        if (name == null) name = "Usuario";
        return name;
    }

    private String getUserEmail(OidcUser user) {
        String email = user.getAttribute("email");
        return email != null ? email : "No disponible";
    }

    private void logout() {
        getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"));
    }
}