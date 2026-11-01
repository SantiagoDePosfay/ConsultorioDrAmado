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
import java.util.List;

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

    // Al instanciarse, carga los datos que existan en disco (Ciclo de Vida 1)
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
        Paciente nuevo = new Paciente(dni, nombre, telefono, obraSocial);
        listaPacientes.add(nuevo);
        // Persistencia inmediata
        GestorArchivos.guardarPacientes(listaPacientes);
    }

    // ======================
    // VALIDACIONES DE TURNOS 
    // ======================

    // Retorna null si la fecha y hora estan disponibles; caso contrario devuelve el mensaje de error exacto
    public String validarDisponibilidad(LocalDate fecha, LocalTime hora, int idTurnoIgnorar) {
        // Solo lunes a viernes
        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return "El consultorio atiende unicamente de lunes a viernes.";
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
        for (Turno t : listaTurnos) {
            // Ignoramos el propio turno si estamos modificando/reprogramando
            if (t.getIdTurno() == idTurnoIgnorar) {
                continue;
            }
            if (t.getFecha().equals(fecha) && t.getHoraInicio().equals(hora)) {
                if (t.getEstado() == EstadoTurno.PENDIENTE || t.getEstado() == EstadoTurno.ATENDIDO) {
                    return "El bloque horario seleccionado ya se encuentra ocupado por otro turno pendiente.";
                }
            }
        }

        return null; // Todo en orden, bloque disponible
    }

    // Calcula el siguiente ID autoincremental buscando el id mas alto guardado
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

    // Registrar un nuevo turno
    public void registrarTurno(String dniPaciente, LocalDate fecha, LocalTime hora, String motivo) {
        int nuevoId = generarProximoIdTurno();
        Turno nuevoTurno = new Turno(nuevoId, dniPaciente, fecha, hora, motivo, EstadoTurno.PENDIENTE);
        listaTurnos.add(nuevoTurno);
        // Persistencia inmediata
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

    // Cancelacion con regla de 24 horas
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

        // Verificamos si faltan al menos 24 horas (1440 minutos)
        long minutosHastaTurno = Duration.between(ahora, fechaHoraTurno).toMinutes();
        if (minutosHastaTurno < 1440) {
            return "No es posible realizar la cancelacion con menos de 24 horas de anticipacion.";
        }

        // No se borra fisicamente, cambia a CANCELADO (libera el horario de inmediato)
        turno.setEstado(EstadoTurno.CANCELADO);
        GestorArchivos.guardarTurnos(listaTurnos);
        return null;
    }

    // Modificar / Reprogramar turno PENDIENTE
    public String modificarTurno(int idTurno, LocalDate nuevaFecha, LocalTime nuevaHora, String nuevoMotivo) {
        Turno turno = buscarTurnoPorId(idTurno);
        if (turno == null) {
            return "No se encontro el turno especificado.";
        }
        if (turno.getEstado() != EstadoTurno.PENDIENTE) {
            return "Solo se pueden reprogramar turnos en estado PENDIENTE.";
        }

        // Validamos disponibilidad ignorando este mismo idTurno
        String error = validarDisponibilidad(nuevaFecha, nuevaHora, idTurno);
        if (error != null) {
            return error;
        }

        turno.setFecha(nuevaFecha);
        turno.setHoraInicio(nuevaHora);
        turno.setMotivo(nuevoMotivo);

        GestorArchivos.guardarTurnos(listaTurnos);
        return null; // Exito
    }

    // Registrar asistencia (ATENDIDO o AUSENTE)
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

        turno.setEstado(nuevoEstado);
        GestorArchivos.guardarTurnos(listaTurnos);
        return null; // Exito
    }

    // =============================
    // CONSULTAS, HISTORIAL Y AGENDA 
    // =============================

    // Retorna todos los turnos PENDIENTES de un paciente (para cancelar o reprogramar)
    public List<Turno> obtenerTurnosPendientesPorDni(String dni) {
        List<Turno> resultado = new ArrayList<>();
        for (Turno t : listaTurnos) {
            if (t.getDniPaciente().equalsIgnoreCase(dni.trim()) && t.getEstado() == EstadoTurno.PENDIENTE) {
                resultado.add(t);
            }
        }
        resultado.sort(Comparator.comparing(Turno::getFecha).thenComparing(Turno::getHoraInicio));
        return resultado;
    }

    // Agenda diaria de un dia especifico (solo turnos activos, ordenados de menor a mayor)
    public List<Turno> obtenerAgendaDiaria(LocalDate fecha) {
        List<Turno> resultado = new ArrayList<>();
        for (Turno t : listaTurnos) {
            // En la agenda diaria se muestran los que no estan CANCELADOS
            if (t.getFecha().equals(fecha) && t.getEstado() != EstadoTurno.CANCELADO) {
                resultado.add(t);
            }
        }
        // Orden cronologico por hora
        resultado.sort(Comparator.comparing(Turno::getHoraInicio));
        return resultado;
    }

    // Historial completo filtrado por DNI y/o rango de fechas
    public List<Turno> filtrarHistorial(String dni, LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Turno> resultado = new ArrayList<>();

        for (Turno t : listaTurnos) {
            // Filtro por DNI (si se especifico)
            if (dni != null && !dni.trim().isEmpty()) {
                if (!t.getDniPaciente().equalsIgnoreCase(dni.trim())) {
                    continue;
                }
            }
            // Filtro por fecha desde (si se especifico)
            if (fechaDesde != null && t.getFecha().isBefore(fechaDesde)) {
                continue;
            }
            // Filtro por fecha hasta (si se especifico)
            if (fechaHasta != null && t.getFecha().isAfter(fechaHasta)) {
                continue;
            }

            resultado.add(t);
        }

        // Ordenar por Fecha ASC y luego Hora ASC
        resultado.sort(Comparator.comparing(Turno::getFecha).thenComparing(Turno::getHoraInicio));
        return resultado;
    }
}