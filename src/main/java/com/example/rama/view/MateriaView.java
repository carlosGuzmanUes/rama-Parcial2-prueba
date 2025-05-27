package com.example.rama.view;

import com.example.rama.model.Materia;
import com.example.rama.service.MateriaService;
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
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import jakarta.annotation.PostConstruct;

@Route(value = "materia", layout = MainLayout.class)
@PageTitle("Materias | Sistema")
 @AnonymousAllowed
public class MateriaView extends VerticalLayout {

    private final MateriaService materiaService;
    private final Grid<Materia> grid = new Grid<>(Materia.class);

    private final TextField idField = new TextField("ID");
    private final TextField nombreField = new TextField("Nombre de la Materia");
    private final TextField docenteField = new TextField("Docente");
    private final TextField horarioField = new TextField("Horario");

    private final Button saveButton = new Button("Guardar");
    private final Button updateButton = new Button("Actualizar");
    private final Button deleteButton = new Button("Eliminar");
    private final Button clearButton = new Button("Limpiar");
    private final Button actividadesButton = new Button("Ver Actividades");

    private Materia selectedMateria;

    public MateriaView(MateriaService materiaService) {
        this.materiaService = materiaService;
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

        // Crear contenido para usuarios autenticados
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
        String userName = getCurrentUserName();
        
        H1 title = new H1("📖 Gestión de Materias");
        title.getStyle().set("color", "#ffc107").set("margin-bottom", "5px");
        
        Span welcomeMsg = new Span("👋 Hola, " + userName + " - Aquí puedes gestionar las materias");
        welcomeMsg.getStyle().set("color", "#666").set("font-style", "italic");
        
        VerticalLayout header = new VerticalLayout(title, welcomeMsg);
        header.setSpacing(false);
        header.setPadding(false);
        
        add(header);
    }

    private void createForm() {
        // Configurar campos
        idField.setPlaceholder("ID (se genera automáticamente)");
        idField.setReadOnly(true);
        idField.setWidth("100px");
        
        nombreField.setPlaceholder("Nombre de la materia");
        nombreField.setWidthFull();
        
        docenteField.setPlaceholder("Nombre del docente");
        docenteField.setWidthFull();
        
        horarioField.setPlaceholder("Horario (ej: Lunes 8:00-10:00)");
        horarioField.setWidthFull();

        // Layout de campos
        HorizontalLayout fieldsLayout = new HorizontalLayout(idField, nombreField, docenteField, horarioField);
        fieldsLayout.setSpacing(true);
        fieldsLayout.setWidthFull();

        // Configurar botones
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveMateria());
        
        updateButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        updateButton.addClickListener(e -> updateMateria());
        updateButton.setEnabled(false);
        
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> deleteMateria());
        deleteButton.setEnabled(false);
        
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearButton.addClickListener(e -> clearForm());
        
        actividadesButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        actividadesButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate("actividades")));

        HorizontalLayout buttonLayout = new HorizontalLayout(
            saveButton, updateButton, deleteButton, clearButton, actividadesButton);
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
        grid.addColumn(Materia::getId).setHeader("🆔 ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(Materia::getNombre).setHeader("📖 Nombre").setFlexGrow(2);
        grid.addColumn(Materia::getDocente).setHeader("👨‍🏫 Docente").setFlexGrow(1);
        grid.addColumn(Materia::getHorario).setHeader("🕐 Horario").setFlexGrow(1);

        grid.addComponentColumn(materia -> {
            Button selectButton = new Button("📋 Seleccionar", e -> selectMateria(materia));
            selectButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            
            return selectButton;
        }).setHeader("⚙️ Acciones").setFlexGrow(0);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeightFull();
        
        // Selección de filas
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                selectMateria(event.getValue());
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

        Button gruposButton = new Button("👥 Grupos");
        gruposButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        gruposButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("grupos")));

        Button actividadesNavButton = new Button("📚 Actividades");
        actividadesNavButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        actividadesNavButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("actividades")));

        Button logoutButton = new Button("🚪 Cerrar Sesión");
        logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        logoutButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.getPage().setLocation("/logout")));

        navigation.add(homeButton, gruposButton, actividadesNavButton, logoutButton);
        add(navigation);
    }

    private void updateGrid() {
        try {
            grid.setItems(materiaService.obtenerTodas());
            clearForm();
        } catch (Exception e) {
            Notification notification = Notification.show("❌ Error al cargar las materias: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void saveMateria() {
        if (nombreField.isEmpty() || docenteField.isEmpty() || horarioField.isEmpty()) {
            Notification notification = Notification.show("⚠️ Por favor, completa todos los campos");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Materia materia = new Materia();
            materia.setNombre(nombreField.getValue().trim());
            materia.setDocente(docenteField.getValue().trim());
            materia.setHorario(horarioField.getValue().trim());
            
            materiaService.crear(materia);
            
            Notification notification = Notification.show("✅ Materia guardada exitosamente");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            updateGrid();
        } catch (Exception e) {
            Notification notification = Notification.show("❌ Error al guardar: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateMateria() {
        if (selectedMateria == null || idField.isEmpty()) {
            Notification notification = Notification.show("⚠️ Selecciona una materia para actualizar");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            selectedMateria.setNombre(nombreField.getValue().trim());
            selectedMateria.setDocente(docenteField.getValue().trim());
            selectedMateria.setHorario(horarioField.getValue().trim());
            
            materiaService.crear(selectedMateria); // El servicio maneja create/update
            
            Notification notification = Notification.show("✅ Materia actualizada exitosamente");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            updateGrid();
        } catch (Exception e) {
            Notification notification = Notification.show("❌ Error al actualizar: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteMateria() {
        if (selectedMateria == null || idField.isEmpty()) {
            Notification notification = Notification.show("⚠️ Selecciona una materia para eliminar");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            materiaService.eliminar(selectedMateria.getId());
            
            Notification notification = Notification.show("🗑️ Materia eliminada exitosamente");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            updateGrid();
        } catch (Exception e) {
            Notification notification = Notification.show("❌ Error al eliminar: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void selectMateria(Materia materia) {
        this.selectedMateria = materia;
        idField.setValue(materia.getId() != null ? materia.getId().toString() : "");
        nombreField.setValue(materia.getNombre() != null ? materia.getNombre() : "");
        docenteField.setValue(materia.getDocente() != null ? materia.getDocente() : "");
        horarioField.setValue(materia.getHorario() != null ? materia.getHorario() : "");
        
        // Habilitar botones de edición
        saveButton.setEnabled(false);
        updateButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }

    private void clearForm() {
        selectedMateria = null;
        idField.clear();
        nombreField.clear();
        docenteField.clear();
        horarioField.clear();
        
        // Resetear estado de botones
        saveButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
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

