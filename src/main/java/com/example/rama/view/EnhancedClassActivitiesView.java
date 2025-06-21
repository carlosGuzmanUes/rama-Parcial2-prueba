package com.example.rama.view;

import com.example.rama.model.ClassActivities;
import com.example.rama.model.ActivityEvidence;
import com.example.rama.service.ClassActivitiesService;
import com.example.rama.service.EvidenceService;
import com.example.rama.repository.ClassActivitiesRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

@Route(value = "actividades-mejoradas", layout = MainLayout.class)
@PageTitle("Gestión Avanzada de Actividades | Sistema")
@AnonymousAllowed
public class EnhancedClassActivitiesView extends VerticalLayout {

    @Autowired
    private ClassActivitiesService activitiesService;
    
    @Autowired
    private ClassActivitiesRepository activitiesRepository;
    
    @Autowired
    private EvidenceService evidenceService;

    // Componentes del formulario
    private TextField descriptionField = new TextField("Descripción");
    private ComboBox<String> docenteField = new ComboBox<>("Docente");
    private DatePicker dateField = new DatePicker("Fecha");
    private ComboBox<String> materiaField = new ComboBox<>("Materia");
    private ComboBox<String> grupoField = new ComboBox<>("Grupo");
    private ComboBox<ClassActivities.TipoActividad> tipoActividadField = new ComboBox<>("Tipo de Actividad");
    private TextArea observacionesField = new TextArea("Observaciones");
    
    // Botones del formulario
    private Button saveButton = new Button("💾 Guardar");
    private Button updateButton = new Button("📝 Actualizar");
    private Button cancelButton = new Button("🚫 Cancelar");
    private Button historialButton = new Button("📚 Ver Historial");
    
    // Grid
    private Grid<ClassActivities> grid = new Grid<>(ClassActivities.class, false);
    
    // Variables de estado
    private ClassActivities selectedActivity;

    public EnhancedClassActivitiesView() {
        // Constructor vacío - la inicialización se hace en @PostConstruct
    }

    @PostConstruct
    private void init() {
        if (!isUserAuthenticated()) {
            showNotAuthenticatedMessage();
            return;
        }
        
        createAuthenticatedContent();
    }

    private boolean isUserAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof OidcUser;
    }

    private void showNotAuthenticatedMessage() {
        H1 errorTitle = new H1("🔒 Acceso Denegado");
        errorTitle.getStyle().set("color", "#dc3545");
        
        Span message = new Span("Necesitas iniciar sesión para acceder a esta página.");
        
        Button loginButton = new Button("🚀 Ir al Login");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("login")));
        
        add(errorTitle, message, loginButton);
    }

    private void createAuthenticatedContent() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        // Header
        createHeader();
        
        // Formulario
        createForm();
        
        // Grid
        configureGrid();
        
        // Cargar datos iniciales
        loadInitialData();
        
        add(grid);
    }

    private void createHeader() {
        String userName = getCurrentUserName();
        
        H1 title = new H1("📚 Gestión Avanzada de Actividades");
        title.getStyle().set("color", "#1976D2").set("margin-bottom", "5px");
        
        Span welcomeMsg = new Span("👋 Hola, " + userName + " - Crea y gestiona actividades con evidencias");
        welcomeMsg.getStyle().set("color", "#666").set("font-style", "italic");
        
        VerticalLayout header = new VerticalLayout(title, welcomeMsg);
        header.setSpacing(false);
        header.setPadding(false);
        
        add(header);
    }

    private void createForm() {
        // Configurar campos
        descriptionField.setPlaceholder("Descripción detallada de la actividad");
        descriptionField.setWidthFull();
        descriptionField.setRequired(true);
        
        docenteField.setPlaceholder("Seleccionar docente");
        docenteField.setClearButtonVisible(true);
        docenteField.setAllowCustomValue(true);
        docenteField.setWidthFull();
        
        dateField.setValue(LocalDate.now());
        dateField.setWidthFull();
        dateField.setRequired(true);
        
        materiaField.setPlaceholder("Seleccionar materia");
        materiaField.setClearButtonVisible(true);
        materiaField.setAllowCustomValue(true);
        materiaField.setWidthFull();
        
        grupoField.setPlaceholder("Seleccionar grupo");
        grupoField.setClearButtonVisible(true);
        grupoField.setAllowCustomValue(true);
        grupoField.setWidthFull();
        
        tipoActividadField.setPlaceholder("Tipo de actividad");
        tipoActividadField.setItems(ClassActivities.TipoActividad.values());
        tipoActividadField.setItemLabelGenerator(ClassActivities.TipoActividad::getDisplayName);
        tipoActividadField.setWidthFull();
        tipoActividadField.setRequired(true);
        
        observacionesField.setPlaceholder("Observaciones adicionales (opcional)");
        observacionesField.setWidthFull();
        observacionesField.setHeight("100px");
        
        // Layout del formulario
        HorizontalLayout row1 = new HorizontalLayout(descriptionField, dateField);
        row1.setWidthFull();
        
        HorizontalLayout row2 = new HorizontalLayout(materiaField, grupoField, tipoActividadField);
        row2.setWidthFull();
        
        HorizontalLayout row3 = new HorizontalLayout(docenteField);
        row3.setWidthFull();
        
        // Configurar botones
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveActivity());
        
        updateButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        updateButton.addClickListener(e -> updateActivity());
        updateButton.setVisible(false);
        
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> clearForm());
        
        historialButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        historialButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate("historial-actividades")));
        
        HorizontalLayout buttonLayout = new HorizontalLayout(
            saveButton, updateButton, cancelButton, historialButton);
        buttonLayout.setSpacing(true);
        
        VerticalLayout formLayout = new VerticalLayout(
            row1, row2, row3, observacionesField, buttonLayout);
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
        
        grid.addColumn(activity -> activity.getDate() != null ? 
            activity.getDate().toString() : "N/A")
            .setHeader("📅 Fecha")
            .setWidth("120px")
            .setFlexGrow(0);
            
        grid.addColumn(ClassActivities::getDescription)
            .setHeader("📝 Descripción")
            .setFlexGrow(2);
            
        grid.addColumn(ClassActivities::getMateria)
            .setHeader("📖 Materia")
            .setWidth("130px")
            .setFlexGrow(0);
            
        grid.addColumn(ClassActivities::getGrupo)
            .setHeader("👥 Grupo")
            .setWidth("100px")
            .setFlexGrow(0);
            
        grid.addColumn(activity -> activity.getTipoActividad() != null ? 
            activity.getTipoActividad().getDisplayName() : "N/A")
            .setHeader("📋 Tipo")
            .setWidth("130px")
            .setFlexGrow(0);
            
        grid.addColumn(ClassActivities::getDocente)
            .setHeader("👨‍🏫 Docente")
            .setWidth("150px")
            .setFlexGrow(0);

        // Columna de evidencias
        grid.addComponentColumn(this::createEvidenceIndicator)
            .setHeader("📎 Evidencias")
            .setWidth("100px")
            .setFlexGrow(0);

        // Columna de acciones
        grid.addComponentColumn(this::createActionButtons)
            .setHeader("⚙️ Acciones")
            .setWidth("180px")
            .setFlexGrow(0);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeightFull();
        
        // Selección de filas
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                editActivity(event.getValue());
            }
        });
    }

    private Span createEvidenceIndicator(ClassActivities activity) {
        List<ActivityEvidence> evidences = evidenceService.findByActivityId(activity.getId());
        
        if (evidences.isEmpty()) {
            Span noEvidence = new Span("📄 0");
            noEvidence.getStyle().set("color", "#999");
            return noEvidence;
        }
        
        Span evidenceCount = new Span("📎 " + evidences.size());
        evidenceCount.getStyle()
            .set("color", "#28a745")
            .set("font-weight", "bold")
            .set("cursor", "pointer");
        evidenceCount.setTitle("Clic para ver evidencias");
        evidenceCount.getElement().addEventListener("click", e -> 
            showEvidenceDialog(activity));
        
        return evidenceCount;
    }

    private HorizontalLayout createActionButtons(ClassActivities activity) {
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        editButton.setTooltipText("Editar actividad");
        editButton.addClickListener(e -> editActivity(activity));
        
        Button evidenceButton = new Button(new Icon(VaadinIcon.PAPERCLIP));
        evidenceButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        evidenceButton.setTooltipText("Gestionar evidencias");
        evidenceButton.addClickListener(e -> showEvidenceDialog(activity));
        
        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Eliminar actividad");
        deleteButton.addClickListener(e -> deleteActivity(activity));
        
        HorizontalLayout actions = new HorizontalLayout(editButton, evidenceButton, deleteButton);
        actions.setSpacing(true);
        return actions;
    }

    private void loadInitialData() {
        try {
            // Cargar actividades
            updateGrid();
            
            // Cargar opciones para comboboxes
            List<String> materias = activitiesRepository.findDistinctMaterias();
            materiaField.setItems(materias);
            
            List<String> grupos = activitiesRepository.findDistinctGrupos();
            grupoField.setItems(grupos);
            
            List<String> docentes = activitiesRepository.findDistinctDocentes();
            docenteField.setItems(docentes);
            
        } catch (Exception e) {
            Notification.show("❌ Error al cargar datos: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

private void saveActivity() {
    if (!validateForm()) return;
    
    try {
        // ✅ CORRECCIÓN: Usar ClassActivities() en lugar de ClassActivitiesService()
        ClassActivities activity = new ClassActivities();
        populateActivityFromForm(activity);
        
        ClassActivities savedActivity = activitiesService.save(activity);
        
        Notification.show("✅ Actividad guardada exitosamente")
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        
        updateGrid();
        clearForm();
        
        // Preguntar si desea agregar evidencias
        showEvidencePrompt(savedActivity);
        
    } catch (Exception e) {
        Notification.show("❌ Error al guardar: " + e.getMessage())
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}

    private void updateActivity() {
        if (selectedActivity == null || !validateForm()) return;
        
        try {
            populateActivityFromForm(selectedActivity);
            
            activitiesService.save(selectedActivity);
            
            Notification.show("✅ Actividad actualizada exitosamente")
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            updateGrid();
            clearForm();
            
        } catch (Exception e) {
            Notification.show("❌ Error al actualizar: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void editActivity(ClassActivities activity) {
        selectedActivity = activity;
        
        descriptionField.setValue(activity.getDescription() != null ? activity.getDescription() : "");
        docenteField.setValue(activity.getDocente());
        dateField.setValue(activity.getDate());
        materiaField.setValue(activity.getMateria());
        grupoField.setValue(activity.getGrupo());
        tipoActividadField.setValue(activity.getTipoActividad());
        observacionesField.setValue(activity.getObservaciones() != null ? activity.getObservaciones() : "");
        
        saveButton.setVisible(false);
        updateButton.setVisible(true);
    }

    private void deleteActivity(ClassActivities activity) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("⚠️ Confirmar Eliminación");
        
        VerticalLayout content = new VerticalLayout();
        content.add(new Span("¿Estás seguro de que deseas eliminar esta actividad?"));
        content.add(new Span("Se eliminarán también todas las evidencias asociadas."));
        content.add(new Span("Esta acción no se puede deshacer."));
        
        Button confirmButton = new Button("🗑️ Eliminar", e -> {
            try {
                // Eliminar evidencias primero
                evidenceService.deleteByActivityId(activity.getId());
                
                // Eliminar actividad
                activitiesService.delete(activity);
                
                updateGrid();
                confirmDialog.close();
                
                Notification.show("✅ Actividad eliminada exitosamente")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    
            } catch (Exception ex) {
                Notification.show("❌ Error al eliminar: " + ex.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        
        Button cancelButton = new Button("Cancelar", e -> confirmDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        confirmDialog.add(content);
        confirmDialog.getFooter().add(cancelButton, confirmButton);
        confirmDialog.open();
    }

    private void showEvidencePrompt(ClassActivities activity) {
        Dialog promptDialog = new Dialog();
        promptDialog.setHeaderTitle("📎 Agregar Evidencias");
        
        VerticalLayout content = new VerticalLayout();
        content.add(new Span("Actividad creada exitosamente."));
        content.add(new Span("¿Deseas agregar evidencias a esta actividad?"));
        
        Button yesButton = new Button("📎 Sí, agregar evidencias", e -> {
            promptDialog.close();
            showEvidenceDialog(activity);
        });
        yesButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        
        Button noButton = new Button("Más tarde", e -> promptDialog.close());
        noButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        HorizontalLayout buttons = new HorizontalLayout(noButton, yesButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        content.add(buttons);
        promptDialog.add(content);
        promptDialog.open();
    }

    private void showEvidenceDialog(ClassActivities activity) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("📎 Gestión de Evidencias - " + activity.getDescription());
        dialog.setWidth("800px");
        dialog.setHeight("600px");
        
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setSpacing(true);
        content.setPadding(true);
        
        // Panel informativo (simplificado)
        createUploadPanel(content, activity);
        
        // Grid de evidencias existentes
        Grid<ActivityEvidence> evidenceGrid = new Grid<>(ActivityEvidence.class, false);
        configureEvidenceGrid(evidenceGrid, activity);
        
        content.add(evidenceGrid);
        content.setFlexGrow(1, evidenceGrid);
        
        dialog.add(content);
        
        Button closeButton = new Button("Cerrar", e -> {
            dialog.close();
            updateGrid(); // Refrescar para mostrar el indicador de evidencias actualizado
        });
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeButton);
        
        dialog.open();
    }

    private void createUploadPanel(VerticalLayout parent, ClassActivities activity) {
        H3 uploadTitle = new H3("📤 Gestión de Evidencias");
        
        Span uploadInfo = new Span("💡 Usa la vista 'Historial de Actividades' para subir y gestionar archivos de evidencia.");
        uploadInfo.getStyle().set("color", "#666").set("font-style", "italic");
        
        Button goToHistoryButton = new Button("📊 Ir al Historial", new Icon(VaadinIcon.EXTERNAL_LINK));
        goToHistoryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        goToHistoryButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate("historial-actividades"));
        });
        
        Span fileInfo = new Span("📋 Archivos soportados: PDF, JPG, PNG, DOC, DOCX, XLS, XLSX, PPT, PPTX");
        fileInfo.getStyle().set("color", "#666").set("font-size", "12px");
        
        VerticalLayout uploadPanel = new VerticalLayout(uploadTitle, uploadInfo, goToHistoryButton, fileInfo);
        uploadPanel.getStyle()
            .set("background", "#f8f9fa")
            .set("padding", "15px")
            .set("border-radius", "8px")
            .set("border", "1px solid #dee2e6")
            .set("margin-bottom", "20px");
        
        parent.add(uploadPanel);
    }

    private void configureEvidenceGrid(Grid<ActivityEvidence> evidenceGrid, ClassActivities activity) {
        evidenceGrid.removeAllColumns();
        
        evidenceGrid.addComponentColumn(evidence -> {
            Span icon = new Span(evidence.getFileIcon());
            icon.getStyle().set("font-size", "16px");
            return icon;
        }).setHeader("").setWidth("50px").setFlexGrow(0);
        
        evidenceGrid.addColumn(ActivityEvidence::getOriginalFilename)
            .setHeader("📄 Archivo")
            .setFlexGrow(2);
            
        evidenceGrid.addColumn(ActivityEvidence::getFormattedFileSize)
            .setHeader("📊 Tamaño")
            .setWidth("100px")
            .setFlexGrow(0);
            
        evidenceGrid.addColumn(evidence -> evidence.getUploadedAt() != null ? 
            evidence.getUploadedAt().toLocalDate().toString() : "N/A")
            .setHeader("📅 Fecha")
            .setWidth("120px")
            .setFlexGrow(0);
            
        evidenceGrid.addColumn(ActivityEvidence::getUploadedBy)
            .setHeader("👤 Subido por")
            .setWidth("150px")
            .setFlexGrow(0);

        evidenceGrid.addComponentColumn(evidence -> {
            Button infoBtn = new Button(new Icon(VaadinIcon.INFO_CIRCLE));
            infoBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            infoBtn.setTooltipText("Ver información");
            infoBtn.addClickListener(e -> showEvidenceInfo(evidence));
            
            return infoBtn;
        }).setHeader("⚙️").setWidth("60px").setFlexGrow(0);

        evidenceGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        evidenceGrid.setHeightFull();
        
        // Cargar evidencias
        refreshEvidenceGrid(evidenceGrid, activity);
    }

    private void refreshEvidenceGrid(Grid<ActivityEvidence> evidenceGrid, ClassActivities activity) {
        List<ActivityEvidence> evidences = evidenceService.findByActivityId(activity.getId());
        evidenceGrid.setItems(evidences);
    }

    private void showEvidenceInfo(ActivityEvidence evidence) {
        Dialog infoDialog = new Dialog();
        infoDialog.setHeaderTitle("📄 Información del Archivo");
        
        VerticalLayout content = new VerticalLayout();
        content.add(new Span("📁 Archivo: " + evidence.getOriginalFilename()));
        content.add(new Span("📊 Tamaño: " + evidence.getFormattedFileSize()));
        content.add(new Span("📅 Subido: " + evidence.getUploadedAt().toLocalDate()));
        content.add(new Span("👤 Por: " + evidence.getUploadedBy()));
        content.add(new Span("💡 Para descargar o eliminar, usa la vista 'Historial de Actividades'"));
        
        Button closeBtn = new Button("Cerrar", e -> infoDialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        content.add(closeBtn);
        infoDialog.add(content);
        infoDialog.open();
    }

    private boolean validateForm() {
        if (descriptionField.getValue() == null || descriptionField.getValue().trim().isEmpty()) {
            Notification.show("⚠️ La descripción es requerida")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            descriptionField.focus();
            return false;
        }
        
        if (dateField.getValue() == null) {
            Notification.show("⚠️ La fecha es requerida")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            dateField.focus();
            return false;
        }
        
        if (tipoActividadField.getValue() == null) {
            Notification.show("⚠️ El tipo de actividad es requerido")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            tipoActividadField.focus();
            return false;
        }
        
        return true;
    }

    private void populateActivityFromForm(ClassActivities activity) {
        activity.setDescription(descriptionField.getValue() != null ? descriptionField.getValue().trim() : "");
        activity.setDocente(docenteField.getValue() != null ? docenteField.getValue().trim() : "");
        activity.setDate(dateField.getValue() != null ? dateField.getValue() : LocalDate.now());
        activity.setMateria(materiaField.getValue() != null ? materiaField.getValue().trim() : "");
        activity.setGrupo(grupoField.getValue() != null ? grupoField.getValue().trim() : "");
        activity.setTipoActividad(tipoActividadField.getValue());
        activity.setObservaciones(observacionesField.getValue() != null ? observacionesField.getValue().trim() : "");
    }

    private void clearForm() {
        selectedActivity = null;
        descriptionField.clear();
        docenteField.clear();
        dateField.setValue(LocalDate.now());
        materiaField.clear();
        grupoField.clear();
        tipoActividadField.clear();
        observacionesField.clear();
        
        saveButton.setVisible(true);
        updateButton.setVisible(false);
        
        descriptionField.focus();
    }

    private void updateGrid() {
        try {
            List<ClassActivities> activities = activitiesService.findAll();
            grid.setItems(activities);
        } catch (Exception e) {
            Notification.show("❌ Error al cargar actividades: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
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
