package com.example.rama.view;

import com.example.rama.model.ClassActivities;
import com.example.rama.model.ActivityEvidence;
import com.example.rama.service.ClassActivitiesService;
import com.example.rama.service.EvidenceService;
import com.example.rama.service.ReportService;
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
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
import com.example.rama.util.MultipartFileWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Route(value = "historial-actividades", layout = MainLayout.class)
@PageTitle("Historial de Actividades | Sistema")
@AnonymousAllowed
public class ActivitiesHistoryView extends VerticalLayout {

    @Autowired
    private ClassActivitiesService activitiesService;
    
    @Autowired
    private ClassActivitiesRepository activitiesRepository;
    
    @Autowired
    private EvidenceService evidenceService;
    
    @Autowired
    private ReportService reportService;

    // Componentes de filtro
    private ComboBox<String> materiaFilter = new ComboBox<>("Materia");
    private ComboBox<String> grupoFilter = new ComboBox<>("Grupo");
    private ComboBox<ClassActivities.TipoActividad> tipoFilter = new ComboBox<>("Tipo de Actividad");
    private DatePicker fechaInicioFilter = new DatePicker("Fecha Inicio");
    private DatePicker fechaFinFilter = new DatePicker("Fecha Fin");
    private TextField docenteFilter = new TextField("Docente");
    
    // Botones
    private Button searchButton = new Button("🔍 Buscar", new Icon(VaadinIcon.SEARCH));
    private Button clearButton = new Button("🗑️ Limpiar", new Icon(VaadinIcon.REFRESH));
    private Button exportButton = new Button("📄 Exportar PDF", new Icon(VaadinIcon.DOWNLOAD));
    
    // Grid
    private Grid<ClassActivities> grid = new Grid<>(ClassActivities.class, false);
    
    // Variables de estado
    private List<ClassActivities> allActivities;
    private ClassActivities selectedActivity;

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
        
        // Panel de filtros
        createFilterPanel();
        
        // Grid de actividades
        configureGrid();
        
        // Cargar datos iniciales
        loadInitialData();
        
        add(grid);
    }

    private void createHeader() {
        String userName = getCurrentUserName();
        
        H1 title = new H1("📚 Historial de Actividades");
        title.getStyle().set("color", "#1976D2").set("margin-bottom", "5px");
        
        Span welcomeMsg = new Span("👋 Hola, " + userName + " - Consulta el historial cronológico de actividades");
        welcomeMsg.getStyle().set("color", "#666").set("font-style", "italic");
        
        VerticalLayout header = new VerticalLayout(title, welcomeMsg);
        header.setSpacing(false);
        header.setPadding(false);
        
        add(header);
    }

    private void createFilterPanel() {
        // Configurar filtros
        materiaFilter.setPlaceholder("Seleccionar materia");
        materiaFilter.setClearButtonVisible(true);
        materiaFilter.setWidth("200px");
        
        grupoFilter.setPlaceholder("Seleccionar grupo");
        grupoFilter.setClearButtonVisible(true);
        grupoFilter.setWidth("150px");
        
        tipoFilter.setPlaceholder("Tipo de actividad");
        tipoFilter.setClearButtonVisible(true);
        tipoFilter.setItems(ClassActivities.TipoActividad.values());
        tipoFilter.setItemLabelGenerator(ClassActivities.TipoActividad::getDisplayName);
        tipoFilter.setWidth("180px");
        
        fechaInicioFilter.setPlaceholder("dd/mm/aaaa");
        fechaInicioFilter.setClearButtonVisible(true);
        fechaInicioFilter.setWidth("140px");
        
        fechaFinFilter.setPlaceholder("dd/mm/aaaa");
        fechaFinFilter.setClearButtonVisible(true);
        fechaFinFilter.setWidth("140px");
        
        docenteFilter.setPlaceholder("Nombre del docente");
        docenteFilter.setClearButtonVisible(true);
        docenteFilter.setWidth("200px");
        
        // Configurar botones
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> performSearch());
        
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearButton.addClickListener(e -> clearFilters());
        
        exportButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        exportButton.addClickListener(e -> exportToPdf());
        
        // Layout de filtros
        HorizontalLayout filtersRow1 = new HorizontalLayout(
            materiaFilter, grupoFilter, tipoFilter);
        filtersRow1.setSpacing(true);
        filtersRow1.setAlignItems(FlexComponent.Alignment.END);
        
        HorizontalLayout filtersRow2 = new HorizontalLayout(
            fechaInicioFilter, fechaFinFilter, docenteFilter);
        filtersRow2.setSpacing(true);
        filtersRow2.setAlignItems(FlexComponent.Alignment.END);
        
        HorizontalLayout buttonsRow = new HorizontalLayout(
            searchButton, clearButton, exportButton);
        buttonsRow.setSpacing(true);
        
        VerticalLayout filterPanel = new VerticalLayout(filtersRow1, filtersRow2, buttonsRow);
        filterPanel.getStyle()
            .set("background", "#f8f9fa")
            .set("padding", "20px")
            .set("border-radius", "8px")
            .set("border", "1px solid #dee2e6")
            .set("margin-bottom", "20px");
        
        add(filterPanel);
    }

    private void configureGrid() {
        grid.removeAllColumns();
        
        // Columnas básicas
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
            .setWidth("150px")
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

        // Columna de acciones
        grid.addComponentColumn(this::createActionButtons)
            .setHeader("⚙️ Acciones")
            .setWidth("200px")
            .setFlexGrow(0);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeightFull();
        
        // Selección de filas
        grid.asSingleSelect().addValueChangeListener(event -> {
            selectedActivity = event.getValue();
        });
    }

    private HorizontalLayout createActionButtons(ClassActivities activity) {
        Button viewButton = new Button(new Icon(VaadinIcon.EYE));
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewButton.setTooltipText("Ver detalles");
        viewButton.addClickListener(e -> showActivityDetails(activity));
        
        Button evidenceButton = new Button(new Icon(VaadinIcon.PAPERCLIP));
        evidenceButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        evidenceButton.setTooltipText("Gestionar evidencias");
        evidenceButton.addClickListener(e -> showEvidenceDialog(activity));
        
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        editButton.setTooltipText("Editar actividad");
        editButton.addClickListener(e -> editActivity(activity));
        
        HorizontalLayout actions = new HorizontalLayout(viewButton, evidenceButton, editButton);
        actions.setSpacing(true);
        return actions;
    }

    private void loadInitialData() {
        try {
            // Cargar todas las actividades
            allActivities = activitiesService.findAll();
            grid.setItems(allActivities);
            
            // Cargar opciones para filtros
            List<String> materias = activitiesRepository.findDistinctMaterias();
            materiaFilter.setItems(materias);
            
            List<String> grupos = activitiesRepository.findDistinctGrupos();
            grupoFilter.setItems(grupos);
            
        } catch (Exception e) {
            Notification.show("❌ Error al cargar datos: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void performSearch() {
        try {
            String materia = materiaFilter.getValue();
            String grupo = grupoFilter.getValue();
            ClassActivities.TipoActividad tipo = tipoFilter.getValue();
            LocalDate fechaInicio = fechaInicioFilter.getValue();
            LocalDate fechaFin = fechaFinFilter.getValue();
            String docente = docenteFilter.getValue();
            
            List<ClassActivities> filteredActivities = activitiesRepository.findActivitiesWithFilters(
                materia, grupo, tipo, fechaInicio, fechaFin, docente);
            
            grid.setItems(filteredActivities);
            
            Notification.show("✅ Búsqueda completada. " + filteredActivities.size() + " resultados encontrados")
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
        } catch (Exception e) {
            Notification.show("❌ Error en la búsqueda: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearFilters() {
        materiaFilter.clear();
        grupoFilter.clear();
        tipoFilter.clear();
        fechaInicioFilter.clear();
        fechaFinFilter.clear();
        docenteFilter.clear();
        
        grid.setItems(allActivities);
        
        Notification.show("🗑️ Filtros limpiados")
            .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

private void exportToPdf() {
    try {
        String materia = materiaFilter.getValue();
        String grupo = grupoFilter.getValue();
        LocalDate fechaInicio = fechaInicioFilter.getValue();
        LocalDate fechaFin = fechaFinFilter.getValue();
        
        byte[] pdfContent = reportService.generateActivityReport(materia, grupo, fechaInicio, fechaFin);
        
        String filename = "reporte_actividades_" + LocalDate.now().toString() + ".pdf";
        
        // ✅ CORRECCIÓN: Usar el patrón robusta que ya te funcionó
        StreamResource resource = new StreamResource(filename, 
            () -> new ByteArrayInputStream(pdfContent));
        resource.setContentType("application/pdf");
        
        // Configurar headers para forzar descarga
        resource.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        // Abrir en nueva pestaña/ventana usando el mismo patrón
        getUI().ifPresent(ui -> {
            String url = ui.getSession().getResourceRegistry().registerResource(resource).getResourceUri().toString();
            ui.getPage().executeJs("window.open($0, '_blank')", url);
        });
        
        Notification.show("📄 Reporte PDF generado exitosamente")
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
    } catch (Exception e) {
        Notification.show("❌ Error al generar reporte: " + e.getMessage())
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
            
        // Log del error para debugging
        System.err.println("Error generando reporte PDF: " + e.getMessage());
        e.printStackTrace();
    }
}

    private void showActivityDetails(ClassActivities activity) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("📋 Detalles de la Actividad");
        dialog.setWidth("600px");
        
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        
        // Información básica
        content.add(new H3("Información General"));
        content.add(createDetailRow("📝 Descripción:", activity.getDescription()));
        content.add(createDetailRow("📅 Fecha:", activity.getDate() != null ? activity.getDate().toString() : "N/A"));
        content.add(createDetailRow("📖 Materia:", activity.getMateria()));
        content.add(createDetailRow("👥 Grupo:", activity.getGrupo()));
        content.add(createDetailRow("👨‍🏫 Docente:", activity.getDocente()));
        content.add(createDetailRow("📋 Tipo:", activity.getTipoActividad() != null ? 
            activity.getTipoActividad().getDisplayName() : "N/A"));
        
        if (activity.getObservaciones() != null && !activity.getObservaciones().trim().isEmpty()) {
            content.add(new H3("Observaciones"));
            Span observaciones = new Span(activity.getObservaciones());
            observaciones.getStyle().set("font-style", "italic");
            content.add(observaciones);
        }
        
        // Evidencias
        List<ActivityEvidence> evidences = evidenceService.findByActivityId(activity.getId());
        if (!evidences.isEmpty()) {
            content.add(new H3("📎 Evidencias Adjuntas (" + evidences.size() + ")"));
            for (ActivityEvidence evidence : evidences) {
                HorizontalLayout evidenceRow = new HorizontalLayout();
                evidenceRow.setAlignItems(FlexComponent.Alignment.CENTER);
                
                Span icon = new Span(evidence.getFileIcon());
                Span filename = new Span(evidence.getOriginalFilename());
                Span size = new Span(evidence.getFormattedFileSize());
                size.getStyle().set("color", "#666").set("font-size", "12px");
                
                Button downloadBtn = new Button(new Icon(VaadinIcon.DOWNLOAD));
                downloadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                downloadBtn.addClickListener(e -> downloadEvidence(evidence));
                
                evidenceRow.add(icon, filename, size, downloadBtn);
                evidenceRow.setSpacing(true);
                content.add(evidenceRow);
            }
        }
        
        dialog.add(content);
        
        Button closeButton = new Button("Cerrar", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(closeButton);
        
        dialog.open();
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
        
        // Panel de carga de archivos
        createUploadPanel(content, activity);
        
        // Grid de evidencias existentes
        Grid<ActivityEvidence> evidenceGrid = new Grid<>(ActivityEvidence.class, false);
        configureEvidenceGrid(evidenceGrid, activity);
        
        content.add(evidenceGrid);
        content.setFlexGrow(1, evidenceGrid);
        
        dialog.add(content);
        
        Button closeButton = new Button("Cerrar", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeButton);
        
        dialog.open();
    }

    private void createUploadPanel(VerticalLayout parent, ClassActivities activity) {
        H3 uploadTitle = new H3("📤 Subir Nueva Evidencia");
        
        TextField descriptionField = new TextField("Descripción (opcional)");
        descriptionField.setWidthFull();
        descriptionField.setPlaceholder("Descripción de la evidencia...");
        
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(
            ".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(10 * 1024 * 1024); // 10MB
        upload.setDropLabel(new Span("Arrastra el archivo aquí o haz clic para seleccionar"));
        upload.setUploadButton(new Button("📁 Seleccionar archivo"));
        
        upload.addSucceededListener(event -> {
            try {
                String userName = getCurrentUserName();
                evidenceService.uploadFile(
                    new MultipartFileWrapper(buffer, event.getFileName(), event.getMIMEType()),
                    activity,
                    descriptionField.getValue(),
                    userName
                );
                
                Notification.show("✅ Evidencia subida exitosamente")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                descriptionField.clear();
                
                // Refrescar el grid si está visible
                getUI().ifPresent(ui -> ui.access(() -> {
                    // Aquí podrías refrescar el grid de evidencias
                }));
                
            } catch (Exception e) {
                Notification.show("❌ Error al subir evidencia: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        upload.addFailedListener(event -> {
            Notification.show("❌ Error en la carga: " + event.getReason().getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        
        // Información sobre archivos permitidos
        Span fileInfo = new Span("📋 Archivos permitidos: PDF, JPG, PNG, DOC, DOCX, XLS, XLSX, PPT, PPTX (máx. 10MB)");
        fileInfo.getStyle().set("color", "#666").set("font-size", "12px");
        
        VerticalLayout uploadPanel = new VerticalLayout(uploadTitle, descriptionField, upload, fileInfo);
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
            Button downloadBtn = new Button(new Icon(VaadinIcon.DOWNLOAD));
            downloadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            downloadBtn.setTooltipText("Descargar");
            downloadBtn.addClickListener(e -> downloadEvidence(evidence));
            
            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.setTooltipText("Eliminar");
            deleteBtn.addClickListener(e -> deleteEvidence(evidence, evidenceGrid, activity));
            
            HorizontalLayout actions = new HorizontalLayout(downloadBtn, deleteBtn);
            actions.setSpacing(true);
            return actions;
        }).setHeader("⚙️ Acciones").setWidth("120px").setFlexGrow(0);

        evidenceGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        evidenceGrid.setHeightFull();
        
        // Cargar evidencias
        refreshEvidenceGrid(evidenceGrid, activity);
    }

    private void refreshEvidenceGrid(Grid<ActivityEvidence> evidenceGrid, ClassActivities activity) {
        List<ActivityEvidence> evidences = evidenceService.findByActivityId(activity.getId());
        evidenceGrid.setItems(evidences);
    }

private void downloadEvidence(ActivityEvidence evidence) {
    try {
        byte[] fileContent = evidenceService.getFileContent(evidence.getId());
        
        // Crear el recurso de descarga
        StreamResource resource = new StreamResource(
            evidence.getOriginalFilename(),
            () -> new ByteArrayInputStream(fileContent)
        );
        
        // Configurar el tipo de contenido
        if (evidence.getContentType() != null && !evidence.getContentType().isEmpty()) {
            resource.setContentType(evidence.getContentType());
        } else {
            resource.setContentType("application/octet-stream");
        }
        
        // Configurar headers para forzar descarga
        resource.setHeader("Content-Disposition", "attachment; filename=\"" + evidence.getOriginalFilename() + "\"");
        
        // Abrir en nueva pestaña/ventana
        getUI().ifPresent(ui -> {
            String url = ui.getSession().getResourceRegistry().registerResource(resource).getResourceUri().toString();
            ui.getPage().executeJs("window.open($0, '_blank')", url);
        });
        
        Notification.show("⬇️ Iniciando descarga: " + evidence.getOriginalFilename())
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
    } catch (Exception e) {
        Notification.show("❌ Error al descargar archivo: " + e.getMessage())
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
        
        // Log del error para debugging
        System.err.println("Error descargando evidencia ID " + evidence.getId() + ": " + e.getMessage());
        e.printStackTrace();
    }
}

    private void deleteEvidence(ActivityEvidence evidence, Grid<ActivityEvidence> evidenceGrid, ClassActivities activity) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("⚠️ Confirmar Eliminación");
        
        VerticalLayout content = new VerticalLayout();
        content.add(new Span("¿Estás seguro de que deseas eliminar la evidencia '" + 
            evidence.getOriginalFilename() + "'?"));
        content.add(new Span("Esta acción no se puede deshacer."));
        
        Button confirmButton = new Button("🗑️ Eliminar", e -> {
            try {
                evidenceService.delete(evidence.getId());
                refreshEvidenceGrid(evidenceGrid, activity);
                confirmDialog.close();
                
                Notification.show("✅ Evidencia eliminada exitosamente")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    
            } catch (Exception ex) {
                Notification.show("❌ Error al eliminar evidencia: " + ex.getMessage())
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

    private void editActivity(ClassActivities activity) {
        // Navegar a la vista de edición de actividades
        getUI().ifPresent(ui -> ui.navigate("actividades"));
        Notification.show("📝 Redirigiendo a la edición de actividades...")
            .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    private HorizontalLayout createDetailRow(String label, String value) {
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "bold").set("min-width", "120px");
        
        Span valueSpan = new Span(value != null ? value : "N/A");
        
        HorizontalLayout row = new HorizontalLayout(labelSpan, valueSpan);
        row.setSpacing(true);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        
        return row;
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