package com.example.rama.view;

import com.example.rama.model.Materia;
import com.example.rama.service.MateriaService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "materia", layout = MainLayout.class)
@PageTitle("Materia | Sistema")
@AnonymousAllowed
public class MateriaView extends VerticalLayout {

    private final MateriaService materiaService;
    private final Grid<Materia> grid = new Grid<>(Materia.class);

    private final TextField id = new TextField("ID (opcional)");
    private final TextField nombre = new TextField("Nombre de la Materia");
    private final TextField docente = new TextField("ID Docente");
    private final TextField horario = new TextField("Horario");

    private final Button btnGuardar = new Button("Guardar");
    private final Button btnEditar = new Button("Editar");
    private final Button btnEliminar = new Button("Eliminar");
    private final Button btnActividad = new Button("Actividades");

    public MateriaView(MateriaService materiaService) {
        this.materiaService = materiaService;

        add(new H1("Información de Materias"));

        HorizontalLayout formulario = new HorizontalLayout(id, nombre, docente, horario);
        formulario.setWidthFull();

        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnEditar.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);
        btnActividad.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout botones = new HorizontalLayout(btnGuardar, btnEditar, btnEliminar, btnActividad);

        configurarGrid();
        configurarEventos();

        add(formulario, botones, grid);
        actualizarGrid();
    }

    private void configurarGrid() {
        grid.setColumns("id", "nombre", "docente", "horario");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        grid.addSelectionListener(event ->
                event.getFirstSelectedItem().ifPresent(this::llenarFormulario)
        );
    }

    private void configurarEventos() {
        btnGuardar.addClickListener(e -> {
            if (!nombre.isEmpty() && !docente.isEmpty() && !horario.isEmpty()) {
                Materia materia = new Materia();
                materia.setNombre(nombre.getValue());
                materia.setDocente(docente.getValue());
                materia.setHorario(horario.getValue());
                materiaService.crear(materia);
                Notification.show("Materia guardada correctamente");
                actualizarGrid();
                limpiarCampos();
            } else {
                Notification.show("Por favor, complete todos los campos");
            }
        });

        btnEditar.addClickListener(e -> {
            if (!id.isEmpty()) {
                try {
                    Long idValue = Long.parseLong(id.getValue());
                    Materia materia = new Materia();
                    materia.setId(idValue);
                    materia.setNombre(nombre.getValue());
                    materia.setDocente(docente.getValue());
                    materia.setHorario(horario.getValue());
                    materiaService.crear(materia);
                    Notification.show("Materia actualizada correctamente");
                    actualizarGrid();
                    limpiarCampos();
                } catch (NumberFormatException ex) {
                    Notification.show("ID inválido");
                }
            }
        });

        btnEliminar.addClickListener(e -> {
            if (!id.isEmpty()) {
                try {
                    Long idValue = Long.parseLong(id.getValue());
                    materiaService.eliminar(idValue);
                    Notification.show("Materia eliminada correctamente");
                    actualizarGrid();
                    limpiarCampos();
                } catch (NumberFormatException ex) {
                    Notification.show("ID inválido");
                }
            }
        });

        btnActividad.addClickListener(e ->{
            UI.getCurrent().getPage().setLocation("/activities");
        });
    }

    private void actualizarGrid() {
        grid.setItems(materiaService.obtenerTodas());
    }

    private void llenarFormulario(Materia materia) {
        id.setValue(String.valueOf(materia.getId()));
        nombre.setValue(materia.getNombre());
        docente.setValue(materia.getDocente());
        horario.setValue(materia.getHorario());
    }

    private void limpiarCampos() {
        id.clear();
        nombre.clear();
        docente.clear();
        horario.clear();
    }
}


