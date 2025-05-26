package com.example.rama.view;

import com.example.rama.model.Login;
import com.example.rama.repository.LoginService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "login")
@PageTitle("Login")
public class LogingView extends VerticalLayout{
    
    private final LoginService service;
    private final Grid<Login> grid = new Grid<>(Login.class);

    private final TextField userNameField = new TextField("username");
    private final TextField passwordField = new TextField("password");
    private final Button summitButton = new Button("Login");

    private Login selectedLogin;

    public LogingView(LoginService service){
        this.service = service;
        add (new H1("Login Rama"));

        VerticalLayout fieldsLayout = new VerticalLayout(userNameField, passwordField);
        fieldsLayout.setSpacing(true);

        summitButton.addAttachListener(e -> summitCredentials());
        summitButton.getStyle().set("background-color", "#007bff").set("color", "white");

        VerticalLayout formLayout = new VerticalLayout(fieldsLayout,summitButton);

        add(formLayout);
        updateGrid();
    
    }

    private void updateGrid() {
        grid.setItems(service.findAll());
        clearForm();
    }

    private void clearForm() {
        selectedLogin = null;
        userNameField.clear();
        passwordField.clear();
    }

    private void summitCredentials() {
        if (selectedLogin == null) {
            selectedLogin = new Login();
        }
        
        selectedLogin.setUserName(userNameField.getValue());
        selectedLogin.setPassWord(passwordField.getValue());

        service.save(selectedLogin);
        Notification.show("ingreso exitoso");
        updateGrid();
    }
    

}
