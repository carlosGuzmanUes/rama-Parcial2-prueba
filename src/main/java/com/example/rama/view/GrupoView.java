package com.example.rama.view;

import com.example.rama.model.Grupo;
import com.example.rama.service.GrupoService;
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
    private final Button cancelButton = new Button("Cancelar");
    
    private Grupo selectedGrupo;

    public GrupoView(GrupoService grupoService) {
        this.grupoService = grupoService;
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
        
        H1 title = new H1("👥 Gestión de Grupos");
        title.getStyle().set("color", "#28a745").set("margin-bottom", "5px");
        
        Span welcomeMsg = new Span("👋 Hola, " + userName + " - Aquí puedes gestionar los grupos");
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
        
        grupoField.setPlaceholder("Nombre del grupo");
        grupoField.setWidthFull();
        
        materiaField.setPlaceholder("Materia asignada");
        materiaField.setWidthFull();
        
        horarioField.setPlaceholder("Horario (ej: Lunes 8:00-10:00)");
        horarioField.setWidthFull();

        // Layout de campos
        HorizontalLayout fieldsLayout = new HorizontalLayout(idField, grupoField, materiaField, horarioField);
        fieldsLayout.setSpacing(true);
        fieldsLayout.setWidthFull();

        // Configurar botones
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveOrUpdateGrupo());
        
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
        grid.addColumn(Grupo::getId).setHeader("🆔 ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(Grupo::getGrupo).setHeader("👥 Grupo").setFlexGrow(1);
        grid.addColumn(Grupo::getMateria).setHeader("📚 Materia").setFlexGrow(1);
        grid.addColumn(Grupo::getHorario).setHeader("🕐 Horario").setFlexGrow(2);

        grid.addComponentColumn(grupo -> {
            Button editButton = new Button("✏️ Editar", e -> editGrupo(grupo));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            
            Button deleteButton = new Button("🗑️ Eliminar", e -> deleteGrupo(grupo));
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
                editGrupo(event.getValue());
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

        Button materiasButton = new Button("📖 Materias");
        materiasButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        materiasButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("materia")));

        Button actividadesButton = new Button("📚 Actividades");
        actividadesButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        actividadesButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("actividades")));

        Button logoutButton = new Button("🚪 Cerrar Sesión");
        logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        logoutButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.getPage().setLocation("/logout")));

        navigation.add(homeButton, materiasButton, actividadesButton, logoutButton);
        add(navigation);
    }

    private void updateGrid() {
        try {
            grid.setItems(grupoService.findAll());
            clearForm();
        } catch (Exception e) {
            Notification notification = Notification.show("❌ Error al cargar los grupos: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void saveOrUpdateGrupo() {
        // Validación básica
        if (grupoField.isEmpty() || materiaField.isEmpty() || horarioField.isEmpty()) {
            Notification notification = Notification.show("⚠️ Por favor, completa todos los campos obligatorios");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Grupo grupo;
            if (selectedGrupo == null) {
                // Crear nuevo grupo
                grupo = new Grupo(grupoField.getValue().trim(), materiaField.getValue().trim(), horarioField.getValue().trim());
            } else {
                // Actualizar grupo existente
                grupo = selectedGrupo;
                grupo.setGrupo(grupoField.getValue().trim());
                grupo.setMateria(materiaField.getValue().trim());
                grupo.setHorario(horarioField.getValue().trim());
            }

            grupoService.save(grupo);
            
            String message = selectedGrupo == null ? "✅ Grupo creado exitosamente" : "✅ Grupo actualizado exitosamente";
            Notification notification = Notification.show(message);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            updateGrid();
        } catch (Exception e) {
            Notification notification = Notification.show("❌ Error al guardar: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void editGrupo(Grupo grupo) {
        this.selectedGrupo = grupo;
        idField.setValue(grupo.getId() != null ? grupo.getId().toString() : "");
        grupoField.setValue(grupo.getGrupo() != null ? grupo.getGrupo() : "");
        materiaField.setValue(grupo.getMateria() != null ? grupo.getMateria() : "");
        horarioField.setValue(grupo.getHorario() != null ? grupo.getHorario() : "");
        
        // Cambiar texto del botón
        saveButton.setText("📝 Actualizar");
    }

    private void deleteGrupo(Grupo grupo) {
        try {
            grupoService.delete(grupo);
            Notification notification = Notification.show("🗑️ Grupo eliminado exitosamente");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification notification = Notification.show("❌ Error al eliminar: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearForm() {
        selectedGrupo = null;
        idField.clear();
        grupoField.clear();
        materiaField.clear();
        horarioField.clear();
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
