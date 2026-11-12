package consultorio.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa un turno médico asignado a un paciente dentro de la agenda del consultorio,
 * administrando su horario, duración fija, motivo y estado actual.
 */
public class Turno {
    
    private final int idTurno;
    private final String dniPaciente;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private final int duracionMinutos = 40; 
    private String motivo;
    private EstadoTurno estado;
    
    /**
     * Constructor:
     * Construye una nueva instancia de {@code Turno} inicializando sus atributos principales 
     * y sanitizando el motivo de la consulta.
     * 
     * @param idTurno Identificador único numérico del turno.
     * @param dniPaciente DNI del paciente asociado al turno.
     * @param fecha Fecha programada del turno.
     * @param horaInicio Hora de inicio de la atención médica.
     * @param motivo Motivo o descripción de la consulta.
     * @param estado Estado inicial del turno (ej. {@link EstadoTurno#PENDIENTE}).
     */
    public Turno(int idTurno, String dniPaciente, LocalDate fecha, LocalTime horaInicio, String motivo, EstadoTurno estado) {
        this.idTurno = idTurno;
        this.dniPaciente = (dniPaciente != null) ? dniPaciente.trim() : "";
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.motivo = (motivo != null) ? motivo.replace(";", ",").trim() : "";
        this.estado = estado;
    }

    // Getters.
    
    /**
     * Obtiene el ID único del turno.
     * @return Un número entero con el ID.
     */
    public int getIdTurno() {
        return idTurno;
    }
    
    /**
     * Obtiene el DNI del paciente asignado al turno.
     * @return Una cadena con el DNI.
     */
    public String getDniPaciente() {
        return dniPaciente;
    }

    /**
     * Obtiene la fecha programada del turno.
     * @return Un objeto {@link LocalDate} con la fecha.
     */
    public LocalDate getFecha() {
        return fecha;
    }
    
    /**
     * Obtiene la hora de inicio del turno.
     * @return Un objeto {@link LocalTime} con la hora de inicio.
     */
    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    /**
     * Calcula y obtiene la hora de finalización del turno sumando automáticamente 
     * la duración fija de 40 minutos a la hora de inicio.
     * @return Un objeto {@link LocalTime} con la hora de cierre, o {@code null} si la hora de inicio no está definida.
     */
    public LocalTime getHoraFin() {
        return horaInicio != null ? horaInicio.plusMinutes(duracionMinutos) : null;
    }
    
    /**
     * Obtiene la duración en minutos de cada bloque de turno.
     * @return Un entero que representa los minutos de duración (por defecto 40).
     */
    public int getDuracionMinutos() {
        return duracionMinutos;
    }
    
    /**
     * Obtiene el motivo de la consulta médica.
     * @return Una cadena con el motivo.
     */
    public String getMotivo() {
        return motivo;
    }
    
    /**
     * Obtiene el estado actual del turno.
     * @return Una instancia de {@link EstadoTurno}.
     */
    public EstadoTurno getEstado() {
        return estado;
    }

    // Setters (para modificaciones de turno y cambio de estado).
    
    /**
     * Actualiza la fecha programada del turno.
     * @param fecha La nueva {@link LocalDate}.
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    /**
     * Actualiza la hora de inicio del turno.
     * @param horaInicio La nueva {@link LocalTime} de inicio.
     */
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }
    
    /**
     * Actualiza el motivo de la consulta médica sanitizando posibles caracteres conflictivos.
     * @param motivo El nuevo texto de motivo.
     */
    public void setMotivo(String motivo) {
        this.motivo = (motivo != null) ? motivo.replace(";", ",").trim() : "";
    }
    
    /**
     * Actualiza el estado actual del turno.
     * @param estado El nuevo {@link EstadoTurno}.
     */
    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }

    /**
     * Convierte el objeto turno a una línea de texto plano con formato CSV 
     * utilizando punto y coma como delimitador.
     * Formato: {@code idTurno;dniPaciente;AAAA-MM-DD;HH:mm;duracion;motivo;estado}
     * 
     * @return Una cadena de texto lista para ser persistida en archivo.
     */
    public String aLineaCSV() {
        return idTurno + ";" + dniPaciente + ";" + fecha.toString() + ";" + 
               horaInicio.toString() + ";" + duracionMinutos + ";" + motivo + ";" + estado.name();
    }

    /**
     * Método de fábrica (Factory) que procesa una línea del archivo CSV 
     * y reconstruye un objeto {@code Turno}.
     * 
     * @param linea La línea de texto con los datos separados por punto y coma.
     * @return Una nueva instancia de {@code Turno}, o {@code null} si ocurre un error de formato.
     */
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
            // partes[4] es la duracion fija (40), la salteamos.
            String motivo = partes[5].trim();
            EstadoTurno estado = EstadoTurno.valueOf(partes[6].trim());

            return new Turno(id, dni, fecha, hora, motivo, estado);
        } catch (Exception e) {
            System.err.println("Error al interpretar línea de turno: " + linea + " (" + e.getMessage() + ")");
            return null;
        }
    }
    
    /**
     * Retorna una representación formateada y legible del turno en texto,
     * útil para la visualización rápida por consola.
     * 
     * @return Una cadena con los detalles principales del turno.
     */
    @Override
    public String toString() {
        return "Turno #" + idTurno + " | Fecha: " + fecha + " " + horaInicio + " a " + getHoraFin() +
               " | DNI: " + dniPaciente + " | Estado: " + estado + " | Motivo: " + motivo;
    }
}