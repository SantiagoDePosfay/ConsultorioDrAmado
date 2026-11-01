package consultorio.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public class Turno {
    //Atributos
    private final int idTurno;
    private final String dniPaciente;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private final int duracionMinutos = 40; 
    private String motivo;
    private EstadoTurno estado;

    public Turno(int idTurno, String dniPaciente, LocalDate fecha, LocalTime horaInicio, String motivo, EstadoTurno estado) {
        this.idTurno = idTurno;
        this.dniPaciente = (dniPaciente != null) ? dniPaciente.trim() : "";
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.motivo = (motivo != null) ? motivo.replace(";", ",").trim() : "";
        this.estado = estado;
    }

    // Getters
    public int getIdTurno() {
        return idTurno;
    }

    public String getDniPaciente() {
        return dniPaciente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    // Calcula la hora de finalización sumando 40 minutos al inicio
    public LocalTime getHoraFin() {
        return horaInicio != null ? horaInicio.plusMinutes(duracionMinutos) : null;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public String getMotivo() {
        return motivo;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    // Setters (para modificaciones de turno y cambio de estado)
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public void setMotivo(String motivo) {
        this.motivo = (motivo != null) ? motivo.replace(";", ",").trim() : "";
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }

    // Formato CSV: idTurno;dniPaciente;AAAA-MM-DD;HH:mm;duracion;motivo;estado
    public String aLineaCSV() {
        return idTurno + ";" + dniPaciente + ";" + fecha.toString() + ";" + 
               horaInicio.toString() + ";" + duracionMinutos + ";" + motivo + ";" + estado.name();
    }

    // Reconstruye el Turno a partir de una línea de texto del archivo turnos.csv
    public static Turno desdeLineaCSV(String linea) {
        if (linea == null || linea.trim().isEmpty()) {
            return null;
        }

        String[] partes = linea.split(";");
        if (partes.length < 7) {
            return null;
        }

        try {
            int id = Integer.parseInt(partes[0].trim());
            String dni = partes[1].trim();
            LocalDate fecha = LocalDate.parse(partes[2].trim());
            LocalTime hora = LocalTime.parse(partes[3].trim());
            // partes[4] es la duracion fija (40), la salteamos
            String motivo = partes[5].trim();
            EstadoTurno estado = EstadoTurno.valueOf(partes[6].trim());

            return new Turno(id, dni, fecha, hora, motivo, estado);
        } catch (Exception e) {
            System.err.println("Error al interpretar línea de turno: " + linea + " (" + e.getMessage() + ")");
            return null;
        }
    }

    @Override
    public String toString() {
        return "Turno #" + idTurno + " | Fecha: " + fecha + " " + horaInicio + " a " + getHoraFin() +
               " | DNI: " + dniPaciente + " | Estado: " + estado + " | Motivo: " + motivo;
    }
}