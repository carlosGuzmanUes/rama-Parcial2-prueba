package com.example.rama.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.PostConstruct;

@Route(value = "")
@PageTitle("Redireccionando...")
public class MainRedirectView extends VerticalLayout {

    @PostConstruct
    private void init() {
        UI.getCurrent().getPage().setLocation("login");
    }
}
