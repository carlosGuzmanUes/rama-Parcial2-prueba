package com.example.rama.service;

import com.example.rama.model.ClassActivities;
import com.example.rama.repository.ClassActivitiesRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ClassActivitiesRepository activitiesRepository;

    // Colores y fuentes
    private static final BaseColor HEADER_COLOR = new BaseColor(52, 73, 94);
    private static final BaseColor LIGHT_GRAY = new BaseColor(245, 245, 245);
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);

    public byte[] generateActivityReport(String materia, String grupo, 
                                       LocalDate startDate, LocalDate endDate) throws Exception {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Título del reporte
        addReportTitle(document, "Reporte de Actividades", materia, grupo, startDate, endDate);
        
        // Obtener actividades filtradas
        List<ClassActivities> activities = getFilteredActivities(materia, grupo, startDate, endDate);
        
        if (activities.isEmpty()) {
            document.add(new Paragraph("No se encontraron actividades para los criterios especificados.", NORMAL_FONT));
        } else {
            // Resumen estadístico
            addStatisticsSummary(document, activities);
            
            // Tabla de actividades
            addActivitiesTable(document, activities);
            
            // Gráfico de actividades por tipo
            addActivitiesByTypeChart(document, activities);
        }
        
        // Pie de página
        addFooter(document);
        
        document.close();
        return baos.toByteArray();
    }

    public byte[] generateMateriaReport(String materia, LocalDate startDate, LocalDate endDate) throws Exception {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Título del reporte
        addReportTitle(document, "Reporte por Materia", materia, null, startDate, endDate);
        
        // Obtener actividades por materia
        List<ClassActivities> activities = getFilteredActivities(materia, null, startDate, endDate);
        
        if (activities.isEmpty()) {
            document.add(new Paragraph("No se encontraron actividades para la materia especificada.", NORMAL_FONT));
        } else {
            // Resumen por grupos
            addGroupSummary(document, activities);
            
            // Estadísticas generales
            addStatisticsSummary(document, activities);
            
            // Tabla detallada
            addActivitiesTable(document, activities);
        }
        
        addFooter(document);
        document.close();
        return baos.toByteArray();
    }

    private void addReportTitle(Document document, String reportType, String materia, 
                              String grupo, LocalDate startDate, LocalDate endDate) throws DocumentException {
        
        // Título principal
        Paragraph title = new Paragraph(reportType, TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        // Información del filtro
        StringBuilder filterInfo = new StringBuilder();
        if (materia != null) filterInfo.append("Materia: ").append(materia).append(" | ");
        if (grupo != null) filterInfo.append("Grupo: ").append(grupo).append(" | ");
        if (startDate != null && endDate != null) {
            filterInfo.append("Período: ")
                     .append(startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                     .append(" - ")
                     .append(endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        
        if (filterInfo.length() > 0) {
            if (filterInfo.toString().endsWith(" | ")) {
                filterInfo.setLength(filterInfo.length() - 3);
            }
            Paragraph filterParagraph = new Paragraph(filterInfo.toString(), NORMAL_FONT);
            filterParagraph.setAlignment(Element.ALIGN_CENTER);
            filterParagraph.setSpacingAfter(15);
            document.add(filterParagraph);
        }
        
        // Fecha de generación
        Paragraph dateGenerated = new Paragraph("Generado el: " + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), SMALL_FONT);
        dateGenerated.setAlignment(Element.ALIGN_RIGHT);
        dateGenerated.setSpacingAfter(20);
        document.add(dateGenerated);
    }

    private void addStatisticsSummary(Document document, List<ClassActivities> activities) throws DocumentException {
        
        Paragraph summaryTitle = new Paragraph("Resumen Estadístico", TITLE_FONT);
        summaryTitle.setSpacingBefore(10);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);
        
        // Crear tabla de estadísticas
        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(50);
        statsTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        // Encabezados
        addTableCell(statsTable, "Métrica", HEADER_FONT, HEADER_COLOR);
        addTableCell(statsTable, "Valor", HEADER_FONT, HEADER_COLOR);
        
        // Total de actividades
        addTableCell(statsTable, "Total de Actividades", NORMAL_FONT, BaseColor.WHITE);
        addTableCell(statsTable, String.valueOf(activities.size()), NORMAL_FONT, BaseColor.WHITE);
        
        // Actividades por tipo
        Map<ClassActivities.TipoActividad, Long> actividadesPorTipo = activities.stream()
            .collect(Collectors.groupingBy(ClassActivities::getTipoActividad, Collectors.counting()));
        
        for (Map.Entry<ClassActivities.TipoActividad, Long> entry : actividadesPorTipo.entrySet()) {
            if (entry.getKey() != null) {
                addTableCell(statsTable, entry.getKey().getDisplayName(), NORMAL_FONT, LIGHT_GRAY);
                addTableCell(statsTable, String.valueOf(entry.getValue()), NORMAL_FONT, LIGHT_GRAY);
            }
        }
        
        // Docentes únicos
        long docentesUnicos = activities.stream()
            .map(ClassActivities::getDocente)
            .distinct()
            .count();
        
        addTableCell(statsTable, "Docentes Participantes", NORMAL_FONT, BaseColor.WHITE);
        addTableCell(statsTable, String.valueOf(docentesUnicos), NORMAL_FONT, BaseColor.WHITE);
        
        document.add(statsTable);
        document.add(new Paragraph(" ", NORMAL_FONT)); // Espacio
    }

    private void addGroupSummary(Document document, List<ClassActivities> activities) throws DocumentException {
        
        Paragraph groupTitle = new Paragraph("Resumen por Grupos", TITLE_FONT);
        groupTitle.setSpacingBefore(10);
        groupTitle.setSpacingAfter(10);
        document.add(groupTitle);
        
        // Agrupar actividades por grupo
        Map<String, Long> actividadesPorGrupo = activities.stream()
            .filter(a -> a.getGrupo() != null)
            .collect(Collectors.groupingBy(ClassActivities::getGrupo, Collectors.counting()));
        
        if (actividadesPorGrupo.isEmpty()) {
            document.add(new Paragraph("No hay información de grupos disponible.", NORMAL_FONT));
            return;
        }
        
        PdfPTable groupTable = new PdfPTable(3);
        groupTable.setWidthPercentage(70);
        groupTable.setWidths(new float[]{3, 2, 2});
        
        // Encabezados
        addTableCell(groupTable, "Grupo", HEADER_FONT, HEADER_COLOR);
        addTableCell(groupTable, "Actividades", HEADER_FONT, HEADER_COLOR);
        addTableCell(groupTable, "Porcentaje", HEADER_FONT, HEADER_COLOR);
        
        int totalActivities = activities.size();
        
        for (Map.Entry<String, Long> entry : actividadesPorGrupo.entrySet()) {
            addTableCell(groupTable, entry.getKey(), NORMAL_FONT, BaseColor.WHITE);
            addTableCell(groupTable, String.valueOf(entry.getValue()), NORMAL_FONT, BaseColor.WHITE);
            
            double percentage = (entry.getValue().doubleValue() / totalActivities) * 100;
            addTableCell(groupTable, String.format("%.1f%%", percentage), NORMAL_FONT, BaseColor.WHITE);
        }
        
        document.add(groupTable);
        document.add(new Paragraph(" ", NORMAL_FONT)); // Espacio
    }

    private void addActivitiesTable(Document document, List<ClassActivities> activities) throws DocumentException {
        
        Paragraph tableTitle = new Paragraph("Detalle de Actividades", TITLE_FONT);
        tableTitle.setSpacingBefore(15);
        tableTitle.setSpacingAfter(10);
        document.add(tableTitle);
        
        // Crear tabla
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2.5f, 1.5f, 1.5f, 1f, 2f});
        
        // Encabezados
        addTableCell(table, "Fecha", HEADER_FONT, HEADER_COLOR);
        addTableCell(table, "Descripción", HEADER_FONT, HEADER_COLOR);
        addTableCell(table, "Docente", HEADER_FONT, HEADER_COLOR);
        addTableCell(table, "Grupo", HEADER_FONT, HEADER_COLOR);
        addTableCell(table, "Tipo", HEADER_FONT, HEADER_COLOR);
        addTableCell(table, "Observaciones", HEADER_FONT, HEADER_COLOR);
        
        // Datos
        for (ClassActivities activity : activities) {
            BaseColor rowColor = activities.indexOf(activity) % 2 == 0 ? BaseColor.WHITE : LIGHT_GRAY;
            
            String fechaStr = activity.getDate() != null ? 
                activity.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
            addTableCell(table, fechaStr, NORMAL_FONT, rowColor);
            
            addTableCell(table, activity.getDescription() != null ? activity.getDescription() : "", NORMAL_FONT, rowColor);
            addTableCell(table, activity.getDocente() != null ? activity.getDocente() : "", NORMAL_FONT, rowColor);
            addTableCell(table, activity.getGrupo() != null ? activity.getGrupo() : "", NORMAL_FONT, rowColor);
            
            String tipo = activity.getTipoActividad() != null ? 
                activity.getTipoActividad().getDisplayName() : "N/A";
            addTableCell(table, tipo, NORMAL_FONT, rowColor);
            
            String observaciones = activity.getObservaciones() != null ? 
                (activity.getObservaciones().length() > 50 ? 
                    activity.getObservaciones().substring(0, 47) + "..." : 
                    activity.getObservaciones()) : "";
            addTableCell(table, observaciones, SMALL_FONT, rowColor);
        }
        
        document.add(table);
    }

    private void addActivitiesByTypeChart(Document document, List<ClassActivities> activities) throws DocumentException {
        
        Paragraph chartTitle = new Paragraph("Distribución por Tipo de Actividad", TITLE_FONT);
        chartTitle.setSpacingBefore(20);
        chartTitle.setSpacingAfter(10);
        document.add(chartTitle);
        
        // Contar actividades por tipo
        Map<ClassActivities.TipoActividad, Long> actividadesPorTipo = activities.stream()
            .filter(a -> a.getTipoActividad() != null)
            .collect(Collectors.groupingBy(ClassActivities::getTipoActividad, Collectors.counting()));
        
        if (actividadesPorTipo.isEmpty()) {
            document.add(new Paragraph("No hay datos suficientes para generar el gráfico.", NORMAL_FONT));
            return;
        }
        
        // Crear tabla simple como representación del gráfico
        PdfPTable chartTable = new PdfPTable(3);
        chartTable.setWidthPercentage(70);
        chartTable.setWidths(new float[]{3, 1, 2});
        
        addTableCell(chartTable, "Tipo de Actividad", HEADER_FONT, HEADER_COLOR);
        addTableCell(chartTable, "Cantidad", HEADER_FONT, HEADER_COLOR);
        addTableCell(chartTable, "Porcentaje", HEADER_FONT, HEADER_COLOR);
        
        int total = activities.size();
        
        for (Map.Entry<ClassActivities.TipoActividad, Long> entry : actividadesPorTipo.entrySet()) {
            addTableCell(chartTable, entry.getKey().getDisplayName(), NORMAL_FONT, BaseColor.WHITE);
            addTableCell(chartTable, String.valueOf(entry.getValue()), NORMAL_FONT, BaseColor.WHITE);
            
            double percentage = (entry.getValue().doubleValue() / total) * 100;
            addTableCell(chartTable, String.format("%.1f%%", percentage), NORMAL_FONT, BaseColor.WHITE);
        }
        
        document.add(chartTable);
    }

    private void addFooter(Document document) throws DocumentException {
        
        Paragraph footer = new Paragraph("\n\n" + 
            "Este reporte fue generado automáticamente por el Sistema de Gestión de Actividades Académicas.", 
            SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);
        
        Paragraph contact = new Paragraph("Para más información, contacte al administrador del sistema.", SMALL_FONT);
        contact.setAlignment(Element.ALIGN_CENTER);
        document.add(contact);
    }

    private void addTableCell(PdfPTable table, String content, Font font, BaseColor backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setPadding(8);
        cell.setBorderWidth(1);
        cell.setBorderColor(BaseColor.GRAY);
        table.addCell(cell);
    }

    private List<ClassActivities> getFilteredActivities(String materia, String grupo, 
                                                       LocalDate startDate, LocalDate endDate) {
        return activitiesRepository.findActivitiesWithFilters(
            materia, grupo, null, startDate, endDate, null);
    }

    // Métodos adicionales para reportes específicos
    public byte[] generateFullReport() throws Exception {
        return generateActivityReport(null, null, null, null);
    }

    public byte[] generateReportByDateRange(LocalDate startDate, LocalDate endDate) throws Exception {
        return generateActivityReport(null, null, startDate, endDate);
    }

    public byte[] generateReportByDocente(String docente, LocalDate startDate, LocalDate endDate) throws Exception {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        addReportTitle(document, "Reporte por Docente: " + docente, null, null, startDate, endDate);
        
        List<ClassActivities> activities = activitiesRepository.findActivitiesWithFilters(
            null, null, null, startDate, endDate, docente);
        
        if (activities.isEmpty()) {
            document.add(new Paragraph("No se encontraron actividades para el docente especificado.", NORMAL_FONT));
        } else {
            addStatisticsSummary(document, activities);
            addActivitiesTable(document, activities);
        }
        
        addFooter(document);
        document.close();
        return baos.toByteArray();
    }
}