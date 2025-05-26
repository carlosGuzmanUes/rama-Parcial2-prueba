package com.example.rama.view;

import com.example.rama.model.Login;
import com.example.rama.repository.LoginService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
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
        
        configureGrid();

        add(formLayout, grid);
        updateGrid();
    
    }

    private void updateGrid() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateGrid'");
    }

    private void configureGrid() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'configureGrid'");
    }

    private void summitCredentials() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'summitCredentials'");
    }
    

}
