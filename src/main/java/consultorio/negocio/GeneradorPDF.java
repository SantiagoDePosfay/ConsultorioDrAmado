package consultorio.negocio;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import consultorio.modelo.Paciente;
import consultorio.modelo.Turno;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GeneradorPDF {

    public static boolean exportarAgendaPDF(LocalDate fecha, List<Turno> turnos, ConsultorioServicio servicio) {
        String nombreArchivo = "agenda_" + fecha.toString() + ".pdf";
        Document documento = new Document();

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));
            documento.open();

            // Título del informe 
            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
            String fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Paragraph titulo = new Paragraph("Consultorio Dr. Salvador Amado\nAgenda del día: " + fechaFormateada + "\n\n", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            // Tabla con 7 columnas
            PdfPTable tabla = new PdfPTable(7);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{1.5f, 1.8f, 2.5f, 2.0f, 1.8f, 2.5f, 1.8f});

            // Encabezados de tabla
            String[] encabezados = {"Hora", "DNI", "Paciente", "Teléfono", "Obra Social", "Motivo", "Estado"};
            Font fuenteCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            for (String enc : encabezados) {
                PdfPCell celda = new PdfPCell(new Phrase(enc, fuenteCabecera));
                celda.setBackgroundColor(Color.GRAY);
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                celda.setPadding(5);
                tabla.addCell(celda);
            }

            // Filas con los turnos del día
            Font fuenteCuerpo = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            for (Turno t : turnos) {
                Paciente p = servicio.buscarPacientePorDni(t.getDniPaciente());
                String nombre = (p != null) ? p.getNombreCompleto() : "No registrado";
                String tel = (p != null) ? p.getTelefono() : "-";
                String os = (p != null) ? p.getObraSocial() : "-";

                tabla.addCell(new Phrase(t.getHoraInicio().toString() + " a " + t.getHoraFin().toString(), fuenteCuerpo));
                tabla.addCell(new Phrase(t.getDniPaciente(), fuenteCuerpo));
                tabla.addCell(new Phrase(nombre, fuenteCuerpo));
                tabla.addCell(new Phrase(tel, fuenteCuerpo));
                tabla.addCell(new Phrase(os, fuenteCuerpo));
                tabla.addCell(new Phrase(t.getMotivo(), fuenteCuerpo));
                tabla.addCell(new Phrase(t.getEstado().name(), fuenteCuerpo));
            }

            documento.add(tabla);
            documento.close();
            return true;

        } catch (FileNotFoundException e) {
            System.err.println("Error: El archivo PDF está abierto en otro programa o no se puede escribir.");
            return false;
        } catch (DocumentException e) {
            System.err.println("Error al generar el documento PDF: " + e.getMessage());
            return false;
        }
    }
}