package com.example.rama.view;

import com.example.rama.model.Grupo;
import com.example.rama.service.GrupoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "grupos", layout = MainLayout.class)
@PageTitle("Grupos | Sistema")
@AnonymousAllowed
public class GrupoView extends VerticalLayout {

    private final GrupoService grupoService;
    private final Grid<Grupo> grid = new Grid<>(Grupo.class);
    private final TextField grupoField = new TextField("Grupo");
    private final TextField materiaField = new TextField("Materia");
    private final TextField horarioField = new TextField("Horario");
    private final TextField idField = new TextField("ID");
    private final Button saveButton = new Button("Guardar");

    public GrupoView(GrupoService grupoService) {
        this.grupoService = grupoService;

        add(new H1("Información de Grupos"));

        configureGrid();
        configureForm();

        HorizontalLayout formLayout = new HorizontalLayout(idField, grupoField, materiaField, horarioField);
        add(formLayout, saveButton, grid);
        formLayout.setSpacing(true);
        formLayout.setPadding(true);
        formLayout.setWidthFull();

        saveButton.getStyle().set("background-color", "#007bff").set("color", "white");

        updateGrid();
    }

    private void configureGrid() {
        grid.setColumns("id", "grupo", "materia", "horario");

        grid.addComponentColumn(grupo -> {
            Button editButton = new Button("Editar");
            Button deleteButton = new Button("Borrar");

            editButton.addClickListener(e -> editGrupo(grupo));
            deleteButton.addClickListener(e -> {
                grupoService.delete(grupo);
                updateGrid();
                Notification.show("Grupo eliminado");
            });

            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Acciones");
    }

    private void configureForm() {
        saveButton.addClickListener(event -> {
            if (!grupoField.isEmpty() && !materiaField.isEmpty() && !horarioField.isEmpty()) {
                Grupo grupo = new Grupo(grupoField.getValue(), materiaField.getValue(), horarioField.getValue());
                grupoService.save(grupo);
                updateGrid();
                clearForm();
                Notification.show("Grupo guardado correctamente");
            } else {
                Notification.show("Completa todos los campos");
            }
        });
    }

    private void editGrupo(Grupo grupo) {
        grupoField.setValue(grupo.getGrupo());
        materiaField.setValue(grupo.getMateria());
        horarioField.setValue(grupo.getHorario());

        grupoService.delete(grupo);
        updateGrid();
    }

    private void updateGrid() {
        grid.setItems(grupoService.findAll());
    }

    private void clearForm() {
        idField.clear();
        grupoField.clear();
        materiaField.clear();
        horarioField.clear();
    }
}
