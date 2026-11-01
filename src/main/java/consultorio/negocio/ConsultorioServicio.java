package consultorio.negocio;

import consultorio.modelo.EstadoTurno;
import consultorio.modelo.Paciente;
import consultorio.modelo.Turno;
import consultorio.persistencia.GestorArchivos;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConsultorioServicio {

    // Grilla oficial de 18 bloques diarios de 40 minutos 
    public static final LocalTime[] GRILLA_HORARIOS = {
        LocalTime.of(8, 0),  LocalTime.of(8, 40), LocalTime.of(9, 20),
        LocalTime.of(10, 0), LocalTime.of(10, 40), LocalTime.of(11, 20),
        LocalTime.of(12, 0), LocalTime.of(12, 40), LocalTime.of(13, 20),
        LocalTime.of(14, 0), LocalTime.of(14, 40), LocalTime.of(15, 20),
        LocalTime.of(16, 0), LocalTime.of(16, 40), LocalTime.of(17, 20),
        LocalTime.of(18, 0), LocalTime.of(18, 40), LocalTime.of(19, 20)
    };

    private List<Paciente> listaPacientes;
    private List<Turno> listaTurnos;

    public ConsultorioServicio() {
        this.listaPacientes = GestorArchivos.cargarPacientes();
        this.listaTurnos = GestorArchivos.cargarTurnos();
    }

    // =========================
    // OPERACIONES DE PACIENTES 
    // =========================

    public Paciente buscarPacientePorDni(String dni) {
        if (dni == null) return null;
        for (Paciente p : listaPacientes) {
            if (p.getDni().equalsIgnoreCase(dni.trim())) {
                return p;
            }
        }
        return null;
    }

    public void registrarPaciente(String dni, String nombre, String telefono, String obraSocial) {
        if (dni == null || !dni.matches("^\\d{7,8}$")) {
            throw new IllegalArgumentException("DNI invalido: debe contener 7 u 8 digitos numericos.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del paciente no puede estar vacio.");
        }
        Paciente nuevo = new Paciente(dni.trim(), nombre.trim(), telefono.trim(), obraSocial.trim());
        listaPacientes.add(nuevo);
        GestorArchivos.guardarPacientes(listaPacientes);
    }

    // ===================================
    // VALIDACIONES CENTRALIZADAS DE TURNOS
    // ===================================

    public String validarFechaParaTurno(LocalDate fecha) {
        if (fecha == null) {
            return "La fecha no puede ser nula.";
        }
        if (fecha.isBefore(LocalDate.now())) {
            return "La fecha no puede ser anterior al dia de hoy (" + LocalDate.now() + ").";
        }
        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return "El consultorio atiende unicamente de lunes a viernes.";
        }
        return null;
    }

    public Set<LocalTime> obtenerHorariosOcupados(LocalDate fecha, int idTurnoIgnorar) {
        Set<LocalTime> ocupados = new HashSet<>();
        if (fecha == null) return ocupados;

        for (Turno t : listaTurnos) {
            if (t.getIdTurno() == idTurnoIgnorar) {
                continue;
            }
            if (t.getFecha().equals(fecha)) {
                if (t.getEstado() == EstadoTurno.PENDIENTE || t.getEstado() == EstadoTurno.ATENDIDO) {
                    ocupados.add(t.getHoraInicio());
                }
            }
        }
        return ocupados;
    }

    public String validarDisponibilidad(LocalDate fecha, LocalTime hora, int idTurnoIgnorar) {
        String errorFecha = validarFechaParaTurno(fecha);
        if (errorFecha != null) {
            return errorFecha;
        }

        // Si la fecha es hoy, el horario no puede haber pasado
        if (fecha.equals(LocalDate.now()) && hora.isBefore(LocalTime.now())) {
            return "El horario seleccionado (" + hora + ") ya paso en el dia de hoy.";
        }

        // Horario dentro de la grilla (08:00 a 19:20)
        boolean horaValida = false;
        for (LocalTime h : GRILLA_HORARIOS) {
            if (h.equals(hora)) {
                horaValida = true;
                break;
            }
        }
        if (!horaValida) {
            return "El horario de inicio debe estar comprendido entre las 08:00 y las 19:20 hs (grilla de 40 min).";
        }

        // No superposicion con turnos PENDIENTE o ATENDIDO
        Set<LocalTime> ocupados = obtenerHorariosOcupados(fecha, idTurnoIgnorar);
        if (ocupados.contains(hora)) {
            return "El bloque horario seleccionado ya se encuentra ocupado por otro turno.";
        }

        return null;
    }

    public boolean tieneTurnoPendienteEnFecha(String dni, LocalDate fecha) {
        if (dni == null || fecha == null) return false;
        for (Turno t : listaTurnos) {
            if (t.getDniPaciente().equalsIgnoreCase(dni.trim()) 
                    && t.getFecha().equals(fecha) 
                    && t.getEstado() == EstadoTurno.PENDIENTE) {
                return true;
            }
        }
        return false;
    }

    private int generarProximoIdTurno() {
        int maxId = 0;
        for (Turno t : listaTurnos) {
            if (t.getIdTurno() > maxId) {
                maxId = t.getIdTurno();
            }
        }
        return maxId + 1;
    }

    // ======================
    // OPERACIONES DE TURNOS 
    // ======================

    public void registrarTurno(String dniPaciente, LocalDate fecha, LocalTime hora, String motivo) {
        String error = validarDisponibilidad(fecha, hora, -1);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        if (buscarPacientePorDni(dniPaciente) == null) {
            throw new IllegalArgumentException("No se puede registrar un turno a un paciente no registrado.");
        }

        int nuevoId = generarProximoIdTurno();
        Turno nuevoTurno = new Turno(nuevoId, dniPaciente.trim(), fecha, hora, motivo.trim(), EstadoTurno.PENDIENTE);
        listaTurnos.add(nuevoTurno);
        GestorArchivos.guardarTurnos(listaTurnos);
    }

    public Turno buscarTurnoPorId(int idTurno) {
        for (Turno t : listaTurnos) {
            if (t.getIdTurno() == idTurno) {
                return t;
            }
        }
        return null;
    }

    public String cancelarTurno(int idTurno) {
        Turno turno = buscarTurnoPorId(idTurno);
        if (turno == null) {
            return "No se encontro el turno especificado.";
        }
        if (turno.getEstado() != EstadoTurno.PENDIENTE) {
            return "Solo se pueden cancelar turnos que se encuentren en estado PENDIENTE.";
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaHoraTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());

        // Si el turno ya paso temporalmente, no corresponde cancelarlo sino marcar asistencia
        if (fechaHoraTurno.isBefore(ahora)) {
            return "El turno ya paso. No corresponde cancelarlo; registreselo como AUSENTE en la opcion 7.";
        }

        long minutosHastaTurno = Duration.between(ahora, fechaHoraTurno).toMinutes();
        if (minutosHastaTurno < 1440) {
            return "No es posible realizar la cancelacion con menos de 24 horas de anticipacion.";
        }

        turno.setEstado(EstadoTurno.CANCELADO);
        GestorArchivos.guardarTurnos(listaTurnos);
        return null;
    }

    public String modificarTurno(int idTurno, LocalDate nuevaFecha, LocalTime nuevaHora, String nuevoMotivo) {
        Turno turno = buscarTurnoPorId(idTurno);
        if (turno == null) {
            return "No se encontro el turno especificado.";
        }
        if (turno.getEstado() != EstadoTurno.PENDIENTE) {
            return "Solo se pueden reprogramar turnos en estado PENDIENTE.";
        }

        String error = validarDisponibilidad(nuevaFecha, nuevaHora, idTurno);
        if (error != null) {
            return error;
        }

        turno.setFecha(nuevaFecha);
        turno.setHoraInicio(nuevaHora);
        turno.setMotivo(nuevoMotivo);

        GestorArchivos.guardarTurnos(listaTurnos);
        return null;
    }

    public String registrarAsistencia(int idTurno, EstadoTurno nuevoEstado) {
        Turno turno = buscarTurnoPorId(idTurno);
        if (turno == null) {
            return "No se encontro el turno especificado.";
        }
        if (turno.getEstado() != EstadoTurno.PENDIENTE) {
            return "Solo se puede registrar asistencia para turnos en estado PENDIENTE.";
        }
        if (nuevoEstado != EstadoTurno.ATENDIDO && nuevoEstado != EstadoTurno.AUSENTE) {
            return "El estado debe ser ATENDIDO o AUSENTE.";
        }

        // No permite marcar asistencia antes de que llegue la hora del turno
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaHoraTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
        if (fechaHoraTurno.isAfter(ahora)) {
            return "No es posible registrar asistencia para un turno futuro (Fecha: " 
                    + turno.getFecha() + " " + turno.getHoraInicio() + ").";
        }

        turno.setEstado(nuevoEstado);
        GestorArchivos.guardarTurnos(listaTurnos);
        return null;
    }

    // =============================
    // CONSULTAS, HISTORIAL Y AGENDA 
    // =============================

    public List<Turno> obtenerTurnosPendientesPorDni(String dni) {
        List<Turno> resultado = new ArrayList<>();
        for (Turno t : listaTurnos) {
            if (t.getDniPaciente().equalsIgnoreCase(dni.trim())) {
                if (t.getEstado() == EstadoTurno.PENDIENTE) {
                    resultado.add(t);
                }
            }
        }
        resultado.sort(Comparator.comparing(Turno::getFecha).thenComparing(Turno::getHoraInicio));
        return resultado;
    }

    public List<Turno> obtenerAgendaDiaria(LocalDate fecha) {
        List<Turno> resultado = new ArrayList<>();
        for (Turno t : listaTurnos) {
            if (t.getFecha().equals(fecha) && t.getEstado() != EstadoTurno.CANCELADO) {
                resultado.add(t);
            }
        }
        resultado.sort(Comparator.comparing(Turno::getHoraInicio));
        return resultado;
    }

    public List<Turno> filtrarHistorial(String dni, LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Turno> resultado = new ArrayList<>();

        for (Turno t : listaTurnos) {
            if (dni != null && !dni.trim().isEmpty()) {
                if (!t.getDniPaciente().equalsIgnoreCase(dni.trim())) {
                    continue;
                }
            }
            if (fechaDesde != null && t.getFecha().isBefore(fechaDesde)) {
                continue;
            }
            if (fechaHasta != null && t.getFecha().isAfter(fechaHasta)) {
                continue;
            }
            resultado.add(t);
        }

        resultado.sort(Comparator.comparing(Turno::getFecha).thenComparing(Turno::getHoraInicio));
        return resultado;
    }
}