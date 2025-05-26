package com.example.rama.view;
import com.example.rama.model.ClassActivities;
import com.example.rama.service.ClassActivitiesService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import jakarta.annotation.PostConstruct;

@Route(value = "actividades", layout = MainLayout.class)
@PageTitle("Actividades | Sistema")
// NO agregar @AnonymousAllowed - requiere autenticación
public class classActivitiesView extends VerticalLayout {

    private final ClassActivitiesService service;
    private final Grid<ClassActivities> grid = new Grid<>(ClassActivities.class);

    private final TextField descriptionField = new TextField("Descripción");
    private final TextField docenteField = new TextField("Docente");
    private final TextField dateField = new TextField("Fecha");
    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");

    private ClassActivities selectedActivity;

    public classActivitiesView(ClassActivitiesService service) {
        this.service = service;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @PostConstruct
    private void init() {
        // Verificar autenticación
        if (!isUserAuthenticated()) {
            showNotAuthenticatedMessage();
            return;
        }

        // Crear la interfaz para usuarios autenticados
        createAuthenticatedContent();
    }

    private boolean isUserAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null 
            && auth.isAuthenticated() 
            && auth.getPrincipal() instanceof OidcUser;
    }

    private void showNotAuthenticatedMessage() {
        H1 errorTitle = new H1("🔒 Acceso Denegado");
        errorTitle.getStyle().set("color", "#dc3545");
        
        Span message = new Span("Necesitas iniciar sesión para acceder a esta página.");
        
        Button loginButton = new Button("🚀 Ir al Login");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate("login")));
        
        add(errorTitle, message, loginButton);
    }

    private void createAuthenticatedContent() {
        // Header con información del usuario
        createHeader();
        
        // Formulario
        createForm();
        
        // Grid
        configureGrid();
        
        // Botones de navegación
        createNavigationButtons();
        
        add(grid);
        updateGrid();
    }

    private void createHeader() {
        // Obtener información del usuario autenticado
        String userName = getCurrentUserName();
        
        H1 title = new H1("📚 Gestión de Actividades de Clase");
        title.getStyle().set("color", "#1976D2").set("margin-bottom", "5px");
        
        Span welcomeMsg = new Span("👋 Hola, " + userName + " - Aquí puedes gestionar las actividades de clase");
        welcomeMsg.getStyle().set("color", "#666").set("font-style", "italic");
        
        VerticalLayout header = new VerticalLayout(title, welcomeMsg);
        header.setSpacing(false);
        header.setPadding(false);
        
        add(header);
    }

    private void createForm() {
        // Configurar campos
        descriptionField.setPlaceholder("Ingresa la descripción de la actividad");
        descriptionField.setWidthFull();
        
        docenteField.setPlaceholder("Nombre del docente");
        docenteField.setWidthFull();
        
        dateField.setPlaceholder("Fecha (ej: 2024-01-15)");
        dateField.setWidthFull();

        // Layout de campos
        HorizontalLayout fieldsLayout = new HorizontalLayout(descriptionField, docenteField, dateField);
        fieldsLayout.setSpacing(true);
        fieldsLayout.setWidthFull();

        // Configurar botones
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveOrUpdateActivity());
        
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> clearForm());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        VerticalLayout formLayout = new VerticalLayout(fieldsLayout, buttonLayout);
        formLayout.getStyle()
            .set("background", "#f8f9fa")
            .set("padding", "20px")
            .set("border-radius", "8px")
            .set("border", "1px solid #dee2e6")
            .set("margin-bottom", "20px");

        add(formLayout);
    }

    private void configureGrid() {
        grid.removeAllColumns();
        grid.addColumn(ClassActivities::getDescription).setHeader("📝 Descripción").setFlexGrow(2);
        grid.addColumn(ClassActivities::getDocente).setHeader("👨‍🏫 Docente").setFlexGrow(1);
        grid.addColumn(ClassActivities::getDate).setHeader("📅 Fecha").setFlexGrow(1);

        grid.addComponentColumn(activity -> {
            Button editButton = new Button("✏️ Editar", e -> editActivity(activity));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            
            Button deleteButton = new Button("🗑️ Eliminar", e -> deleteActivity(activity));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            
            HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
            actions.setSpacing(true);
            return actions;
        }).setHeader("⚙️ Acciones").setFlexGrow(0);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeightFull();
        
        // Selección de filas
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                editActivity(event.getValue());
            }
        });
    }

    private void createNavigationButtons() {
        HorizontalLayout navigation = new HorizontalLayout();
        navigation.setJustifyContentMode(JustifyContentMode.CENTER);
        navigation.setSpacing(true);
        navigation.getStyle().set("margin-top", "20px");

        Button homeButton = new Button("🏠 Inicio");
        homeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        homeButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        Button groupsButton = new Button("👥 Grupos");
        groupsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        groupsButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("grupos")));

        Button materiasButton = new Button("📖 Materias");
        materiasButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        materiasButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("materias")));

        Button logoutButton = new Button("🚪 Cerrar Sesión");
        logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        logoutButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.getPage().setLocation("/logout")));

        navigation.add(homeButton, groupsButton, materiasButton, logoutButton);
        add(navigation);
    }

    private void updateGrid() {
        try {
            grid.setItems(service.findAll());
            clearForm();
        } catch (Exception e) {
            Notification notification = Notification.show("❌ Error al cargar las actividades: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void saveOrUpdateActivity() {
        // Validación básica
        if (descriptionField.isEmpty() || docenteField.isEmpty() || dateField.isEmpty()) {
            Notification notification = Notification.show("⚠️ Por favor, completa todos los campos");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            if (selectedActivity == null) {
                selectedActivity = new ClassActivities();
            }

            selectedActivity.setDescription(descriptionField.getValue().trim());
            selectedActivity.setDocente(docenteField.getValue().trim());
            selectedActivity.setDate(dateField.getValue().trim());

            service.save(selectedActivity);
            
            String message = selectedActivity.getId() != null ? "✅ Actividad actualizada exitosamente" : "✅ Actividad creada exitosamente";
            Notification notification = Notification.show(message);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            updateGrid();
        } catch (Exception e) {
            Notification notification = Notification.show("❌ Error al guardar: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void editActivity(ClassActivities activity) {
        this.selectedActivity = activity;
        descriptionField.setValue(activity.getDescription() != null ? activity.getDescription() : "");
        docenteField.setValue(activity.getDocente() != null ? activity.getDocente() : "");
        dateField.setValue(activity.getDate() != null ? activity.getDate() : "");
        
        // Cambiar texto del botón
        saveButton.setText("📝 Actualizar");
    }

    private void deleteActivity(ClassActivities activity) {
        try {
            service.delete(activity);
            Notification notification = Notification.show("🗑️ Actividad eliminada exitosamente");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification notification = Notification.show("❌ Error al eliminar: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearForm() {
        selectedActivity = null;
        descriptionField.clear();
        docenteField.clear();
        dateField.clear();
        saveButton.setText("💾 Guardar");
    }

    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) authentication.getPrincipal();
            String name = user.getAttribute("name");
            if (name == null) name = user.getAttribute("nickname");
            if (name == null) name = "Usuario";
            return name;
        }
        return "Usuario";
    }
}
