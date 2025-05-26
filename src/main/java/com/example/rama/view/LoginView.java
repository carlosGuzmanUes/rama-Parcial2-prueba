package com.example.rama.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    public LoginView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("Iniciar Sesión");
        Paragraph description = new Paragraph("Accede a tu cuenta usando Auth0");

        Button loginButton = new Button("Iniciar Sesión con Auth0");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickListener(e -> 
            getUI().ifPresent(ui -> 
                ui.getPage().setLocation("/oauth2/authorization/auth0")));

        add(title, description, loginButton);
    }
}