package consultorio.ui;

import consultorio.modelo.EstadoTurno;
import consultorio.modelo.Paciente;
import consultorio.modelo.Turno;
import consultorio.negocio.ConsultorioServicio;
import consultorio.negocio.GeneradorPDF;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class MainConsola {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ConsultorioServicio servicio = new ConsultorioServicio();
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Excepcion interna para cancelar cualquier carga y retornar al menu principal
    private static class OperacionCanceladaException extends RuntimeException {}

    public static void main(String[] args) {
        int opcion = -1;

        while (opcion != 0) {
            mostrarMenuPrincipal();
            try {
                opcion = leerEnteroOpcion("Seleccione una opcion: ");

                switch (opcion) {
                    case 1:
                        registrarPaciente();
                        break;
                    case 2:
                        registrarNuevoTurno();
                        break;
                    case 3:
                        consultarAgendaHoy();
                        break;
                    case 4:
                        consultarAgendaPorFecha();
                        break;
                    case 5:
                        cancelarTurno();
                        break;
                    case 6:
                        modificarTurno();
                        break;
                    case 7:
                        registrarAsistencia();
                        break;
                    case 8:
                        consultarHistorial();
                        break;
                    case 0:
                        System.out.println("\nGracias por utilizar el sistema del Consultorio Dr. Salvador Amado. Hasta luego.");
                        break;
                    default:
                        System.out.println("Opcion no valida. Ingrese un numero del 0 al 8.");
                }
            } catch (OperacionCanceladaException e) {
                System.out.println("\n>> Operacion cancelada por el usuario. Retornando al menu principal...");
            }
        }
    }

    // ==========================================
    // MENU PRINCIPAL
    // ==========================================

    private static void mostrarMenuPrincipal() {
        System.out.println("\n=======================================================");
        System.out.println("  SISTEMA DE GESTION DE TURNOS - DR. SALVADOR AMADO");
        System.out.println("  (Escriba 'MENU' en cualquier campo para cancelar)");
        System.out.println("=======================================================");
        System.out.println("1. Identificar y dar de alta paciente");
        System.out.println("2. Registrar nuevo turno");
        System.out.println("3. Ver / Exportar agenda de hoy");
        System.out.println("4. Ver / Exportar agenda por fecha");
        System.out.println("5. Cancelar turno (regla 24 hs)");
        System.out.println("6. Modificar / reprogramar turno");
        System.out.println("7. Registrar asistencia (Atendido / Ausente)");
        System.out.println("8. Consultar historial de turnos");
        System.out.println("0. Salir");
        System.out.println("-------------------------------------------------------");
    }

    // ==================================
    // FUNCIONALIDAD 1: ALTA DE PACIENTE 
    // ==================================

    private static void registrarPaciente() {
        System.out.println("\n--- IDENTIFICAR / DAR DE ALTA PACIENTE ---");
        String dni = leerDni();

        Paciente existente = servicio.buscarPacientePorDni(dni);
        if (existente != null) {
            System.out.println("El paciente ya se encuentra registrado:");
            System.out.println(existente);
            return;
        }

        System.out.print("Ingrese nombre completo: ");
        String nombre = leerTextoNoVacio();

        System.out.print("Ingrese telefono: ");
        String telefono = leerTextoNoVacio();

        System.out.print("Ingrese obra social (o 'Particular'): ");
        String obraSocial = leerTextoNoVacio();

        servicio.registrarPaciente(dni, nombre, telefono, obraSocial);
        System.out.println("Paciente registrado exitosamente en el sistema.");
    }

    // ================================
    // FUNCIONALIDAD 2: REGISTRAR TURNO 
    // ================================

    private static void registrarNuevoTurno() {
        System.out.println("\n--- REGISTRAR NUEVO TURNO ---");
        String dni = leerDni();

        Paciente paciente = servicio.buscarPacientePorDni(dni);
        if (paciente == null) {
            System.out.println("Paciente no registrado. Ingrese sus datos para darlo de alta:");
            System.out.print("Nombre completo: ");
            String nombre = leerTextoNoVacio();
            System.out.print("Telefono: ");
            String telefono = leerTextoNoVacio();
            System.out.print("Obra social (o 'Particular'): ");
            String obraSocial = leerTextoNoVacio();

            servicio.registrarPaciente(dni, nombre, telefono, obraSocial);
            System.out.println("Paciente registrado correctamente.");
        } else {
            System.out.println("Paciente encontrado: " + paciente.getNombreCompleto() + " (" + paciente.getObraSocial() + ")");
        }

        LocalDate fecha = leerFechaTurnoValidada("Ingrese fecha del turno (DD/MM/AAAA): ");

        // Alerta preventiva si ya tiene otro turno pendiente el mismo dia
        if (servicio.tieneTurnoPendienteEnFecha(dni, fecha)) {
            System.out.println("\n[!] ADVERTENCIA: El paciente ya posee otro turno PENDIENTE para el dia " + fecha.format(FORMATO_FECHA) + ".");
            System.out.print("Desea asignarle un turno adicional para este mismo dia? (S/N): ");
            String confirmacion = leerCadenaTeclado();
            if (!confirmacion.equalsIgnoreCase("S")) {
                System.out.println("Registro de turno cancelado.");
                return;
            }
        }

        LocalTime hora = seleccionarHorarioDeGrilla(fecha, -1);

        System.out.print("Ingrese motivo de la consulta: ");
        String motivo = leerTextoNoVacio();

        servicio.registrarTurno(dni, fecha, hora, motivo);
        System.out.println("Turno registrado exitosamente en estado PENDIENTE.");
    }

    // ===============================================
    // FUNCIONALIDAD 3: VER / EXPORTAR AGENDA DE HOY 
    // ===============================================

    private static void consultarAgendaHoy() {
        LocalDate hoy = LocalDate.now();
        System.out.println("\n--- AGENDA DEL DIA DE HOY (" + hoy.format(FORMATO_FECHA) + ") ---");
        procesarConsultaAgenda(hoy);
    }

    // ==================================================
    // FUNCIONALIDAD 4: VER / EXPORTAR AGENDA POR FECHA 
    // ==================================================

    private static void consultarAgendaPorFecha() {
        System.out.println("\n--- CONSULTAR AGENDA POR FECHA ---");
        LocalDate fecha = leerFechaFormato("Ingrese fecha a consultar (DD/MM/AAAA): ");
        procesarConsultaAgenda(fecha);
    }

    private static void procesarConsultaAgenda(LocalDate fecha) {
        List<Turno> turnos = servicio.obtenerAgendaDiaria(fecha);
        if (turnos.isEmpty()) {
            System.out.println("No hay turnos asignados para la fecha seleccionada (" + fecha.format(FORMATO_FECHA) + ").");
            return;
        }

        mostrarListadoTurnosConDetalle(turnos);

        System.out.print("\nDesea exportar esta agenda a PDF? (S/N): ");
        String respuesta = leerCadenaTeclado();
        if (respuesta.equalsIgnoreCase("S")) {
            boolean exito = GeneradorPDF.exportarAgendaPDF(fecha, turnos, servicio);
            if (exito) {
                System.out.println("Archivo PDF 'agenda_" + fecha + ".pdf' generado correctamente.");
            }
        }
    }

    // ================================
    // FUNCIONALIDAD 5: CANCELAR TURNO
    // ================================

    private static void cancelarTurno() {
        System.out.println("\n--- CANCELAR TURNO (REGLA 24 HS) ---");
        String dni = leerDni();

        List<Turno> pendientes = servicio.obtenerTurnosPendientesPorDni(dni);
        if (pendientes.isEmpty()) {
            System.out.println("El paciente no tiene turnos PENDIENTES.");
            return;
        }

        System.out.println("\nTurnos pendientes del paciente:");
        for (int i = 0; i < pendientes.size(); i++) {
            Turno t = pendientes.get(i);
            System.out.println((i + 1) + ". ID #" + t.getIdTurno() + " | Fecha: " + t.getFecha().format(FORMATO_FECHA) + 
                               " | Hora: " + t.getHoraInicio() + " a " + t.getHoraFin() + " | Motivo: " + t.getMotivo());
        }

        int seleccion = leerEnteroOpcion("\nSeleccione el numero de turno a cancelar (0 para volver): ");
        if (seleccion <= 0 || seleccion > pendientes.size()) {
            System.out.println("Operacion cancelada.");
            return;
        }

        Turno elegido = pendientes.get(seleccion - 1);
        System.out.print("Confirma la cancelacion del turno ID #" + elegido.getIdTurno() + "? (S/N): ");
        String confirmacion = leerCadenaTeclado();

        if (!confirmacion.equalsIgnoreCase("S")) {
            System.out.println("Cancelacion abortada.");
            return;
        }

        String error = servicio.cancelarTurno(elegido.getIdTurno());
        if (error != null) {
            System.out.println("Error: " + error);
        } else {
            System.out.println("El turno fue CANCELADO exitosamente. El bloque horario quedo liberado.");
        }
    }

    // =========================================
    // FUNCIONALIDAD 6: MODIFICAR / REPROGRAMAR 
    // =========================================

    private static void modificarTurno() {
        System.out.println("\n--- MODIFICAR / REPROGRAMAR TURNO ---");
        String dni = leerDni();

        List<Turno> pendientes = servicio.obtenerTurnosPendientesPorDni(dni);
        if (pendientes.isEmpty()) {
            System.out.println("El paciente no posee turnos pendientes.");
            return;
        }

        for (int i = 0; i < pendientes.size(); i++) {
            Turno t = pendientes.get(i);
            System.out.println((i + 1) + ". ID #" + t.getIdTurno() + " | " + t.getFecha().format(FORMATO_FECHA) + 
                               " " + t.getHoraInicio() + " | " + t.getMotivo());
        }

        int seleccion = leerEnteroOpcion("\nSeleccione el numero de turno a modificar (0 para cancelar): ");
        if (seleccion <= 0 || seleccion > pendientes.size()) {
            System.out.println("Operacion cancelada.");
            return;
        }

        Turno elegido = pendientes.get(seleccion - 1);

        System.out.println("\nIngrese los nuevos datos del turno:");
        LocalDate nuevaFecha = leerFechaTurnoValidada("Nueva fecha (DD/MM/AAAA): ");
        LocalTime nuevaHora = seleccionarHorarioDeGrilla(nuevaFecha, elegido.getIdTurno());

        System.out.print("Nuevo motivo (Enter para conservar actual '" + elegido.getMotivo() + "'): ");
        String nuevoMotivo = leerCadenaTeclado();
        if (nuevoMotivo.isEmpty()) {
            nuevoMotivo = elegido.getMotivo();
        } else {
            nuevoMotivo = nuevoMotivo.replace(",", " - ");
        }

        String error = servicio.modificarTurno(elegido.getIdTurno(), nuevaFecha, nuevaHora, nuevoMotivo);
        if (error != null) {
            System.out.println("Error: " + error);
            System.out.println("Se conserva el turno original sin modificaciones.");
        } else {
            System.out.println("Turno reprogramado exitosamente.");
        }
    }

    // ==========================================
    // FUNCIONALIDAD 7: ASISTENCIA
    // ==========================================

    private static void registrarAsistencia() {
        System.out.println("\n--- REGISTRAR ASISTENCIA ---");
        int idTurno = leerEnteroOpcion("Ingrese ID del turno (0 para volver): ");
        if (idTurno == 0) return;

        Turno turno = servicio.buscarTurnoPorId(idTurno);
        if (turno == null) {
            System.out.println("No existe turno con ese ID.");
            return;
        }

        if (turno.getEstado() != EstadoTurno.PENDIENTE) {
            System.out.println("El turno no esta PENDIENTE. Estado actual: " + turno.getEstado());
            return;
        }

        System.out.println("Turno #" + turno.getIdTurno() + " | Fecha: " + turno.getFecha().format(FORMATO_FECHA) + 
                           " " + turno.getHoraInicio() + " | Paciente DNI: " + turno.getDniPaciente());
        System.out.println("1. Marcar como ATENDIDO (Asistio a la consulta)");
        System.out.println("2. Marcar como AUSENTE (No concurrio)");
        int opcionEstado = leerEnteroOpcion("Seleccione opcion: ");

        EstadoTurno nuevoEstado;
        if (opcionEstado == 1) {
            nuevoEstado = EstadoTurno.ATENDIDO;
        } else if (opcionEstado == 2) {
            nuevoEstado = EstadoTurno.AUSENTE;
        } else {
            System.out.println("Opcion no valida.");
            return;
        }

        String error = servicio.registrarAsistencia(idTurno, nuevoEstado);
        if (error != null) {
            System.out.println("Error: " + error);
        } else {
            System.out.println("Asistencia registrada correctamente como: " + nuevoEstado);
        }
    }

    // ===========================
    // FUNCIONALIDAD 8: HISTORIAL 
    // ===========================

    private static void consultarHistorial() {
        System.out.println("\n--- CONSULTAR HISTORIAL DE TURNOS ---");
        System.out.println("Opciones de filtrado (Enter en blanco para omitir filtro):");

        System.out.print("DNI del paciente (o Enter para omitir): ");
        String dni = leerCadenaTeclado();

        System.out.print("Fecha Desde DD/MM/AAAA (o Enter para omitir): ");
        String desdeStr = leerCadenaTeclado();
        LocalDate fechaDesde = parsearFechaOpcional(desdeStr);

        System.out.print("Fecha Hasta DD/MM/AAAA (o Enter para omitir): ");
        String hastaStr = leerCadenaTeclado();
        LocalDate fechaHasta = parsearFechaOpcional(hastaStr);
        
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            System.out.println("Error: La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
            return;
        }

        List<Turno> historial = servicio.filtrarHistorial(dni, fechaDesde, fechaHasta);
        if (historial.isEmpty()) {
            System.out.println("No se encontraron registros para los criterios indicados.");
            return;
        }

        mostrarListadoTurnosConDetalle(historial);

        int cantPendientes = 0, cantAtendidos = 0, cantCancelados = 0, cantAusentes = 0;
        for (Turno t : historial) {
            switch (t.getEstado()) {
                case PENDIENTE: cantPendientes++; break;
                case ATENDIDO: cantAtendidos++; break;
                case CANCELADO: cantCancelados++; break;
                case AUSENTE: cantAusentes++; break;
            }
        }

        System.out.println("\n--- RESUMEN DE TOTALES ---");
        System.out.println("Pendientes: " + cantPendientes + " | Atendidos: " + cantAtendidos +
                           " | Cancelados: " + cantCancelados + " | Ausentes: " + cantAusentes);
        System.out.println("Total turnos evaluados: " + historial.size());
    }

    // ==========================================
    // METODOS AUXILIARES Y LECTURAS VALIDATORIAS
    // ==========================================

    private static void mostrarListadoTurnosConDetalle(List<Turno> turnos) {
        System.out.println("\n------------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-10s | %-13s | %-8s | %-20s | %-12s | %-10s\n", 
                          "ID", "FECHA", "HORARIO", "DNI", "PACIENTE", "COBERTURA", "ESTADO");
        System.out.println("------------------------------------------------------------------------------------------------------");

        for (Turno t : turnos) {
            Paciente p = servicio.buscarPacientePorDni(t.getDniPaciente());
            String nombre = (p != null) ? p.getNombreCompleto() : "No registrado";
            String cobertura = (p != null) ? p.getObraSocial() : "-";
            String rangoHorario = t.getHoraInicio() + " a " + t.getHoraFin();

            System.out.printf("%-4d | %-10s | %-13s | %-8s | %-20s | %-12s | %-10s\n",
                              t.getIdTurno(),
                              t.getFecha().format(FORMATO_FECHA),
                              rangoHorario,
                              t.getDniPaciente(),
                              nombre,
                              cobertura,
                              t.getEstado());
        }
        System.out.println("------------------------------------------------------------------------------------------------------");
    }

    private static LocalTime seleccionarHorarioDeGrilla(LocalDate fecha, int idTurnoIgnorar) {
        Set<LocalTime> ocupados = servicio.obtenerHorariosOcupados(fecha, idTurnoIgnorar);
        boolean esHoy = fecha.equals(LocalDate.now());
        LocalTime ahora = LocalTime.now();

        int disponibles = 0;
        for (LocalTime h : ConsultorioServicio.GRILLA_HORARIOS) {
            boolean bloqueYaPaso = esHoy && h.isBefore(ahora);
            boolean bloqueOcupado = ocupados.contains(h);
            if (!bloqueYaPaso && !bloqueOcupado) {
                disponibles++;
            }
        }

        if (disponibles == 0) {
            System.out.println(">> No hay horarios disponibles para el dia " + fecha.format(FORMATO_FECHA) + ".");
            throw new OperacionCanceladaException();
        }

        while (true) {
            System.out.println("\n--- HORARIOS DISPONIBLES PARA EL DIA " + fecha.format(FORMATO_FECHA) + " ---");
            for (int i = 0; i < ConsultorioServicio.GRILLA_HORARIOS.length; i++) {
                LocalTime h = ConsultorioServicio.GRILLA_HORARIOS[i];
                boolean bloqueYaPaso = esHoy && h.isBefore(ahora);
                boolean bloqueOcupado = ocupados.contains(h);

                String estado;
                if (bloqueOcupado) {
                    estado = "[OCUPADO] ";
                } else if (bloqueYaPaso) {
                    estado = "[PASADO]  ";
                } else {
                    estado = "[LIBRE]   ";
                }

                System.out.printf("[%2d] %s %s  ", (i + 1), h, estado);
                if ((i + 1) % 3 == 0) System.out.println();
            }
            System.out.println();

            int opcion = leerEnteroOpcion("Seleccione numero de bloque (1 al 18): ");
            if (opcion < 1 || opcion > ConsultorioServicio.GRILLA_HORARIOS.length) {
                System.out.println("Numero fuera de rango. Ingrese un valor del 1 al 18.");
                continue;
            }

            LocalTime seleccionado = ConsultorioServicio.GRILLA_HORARIOS[opcion - 1];
            if (ocupados.contains(seleccionado)) {
                System.out.println(">> Error: El bloque " + seleccionado + " ya esta OCUPADO. Por favor, seleccione otro horario.");
                continue;
            }
            if (esHoy && seleccionado.isBefore(ahora)) {
                System.out.println(">> Error: El bloque " + seleccionado + " ya paso. Seleccione un horario posterior al actual.");
                continue;
            }

            return seleccionado;
        }
    }

    private static LocalDate leerFechaTurnoValidada(String mensaje) {
        while (true) {
            LocalDate fecha = leerFechaFormato(mensaje);
            String errorNegocio = servicio.validarFechaParaTurno(fecha);
            if (errorNegocio != null) {
                System.out.println("Error: " + errorNegocio);
                continue;
            }
            return fecha;
        }
    }

    private static LocalDate leerFechaFormato(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = leerCadenaTeclado();
            try {
                return LocalDate.parse(entrada, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Formato de fecha invalido. Ingrese en formato DD/MM/AAAA (ej. 15/11/2026).");
            }
        }
    }

    private static String leerDni() {
        while (true) {
            System.out.print("Ingrese DNI del paciente (7 u 8 digitos): ");
            String entrada = leerCadenaTeclado();
            if (entrada.matches("^\\d{7,8}$")) {
                return entrada;
            }
            System.out.println("Ingrese un numero de DNI valido (solo numeros, entre 7 y 8 digitos).");
        }
    }

    private static LocalDate parsearFechaOpcional(String entrada) {
        if (entrada == null || entrada.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(entrada.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            System.out.println("Fecha con formato invalido, se omitira el filtro de fecha.");
            return null;
        }
    }

    private static int leerEnteroOpcion(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = leerCadenaTeclado();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Debe ingresar un numero entero.");
            }
        }
    }

    private static String leerTextoNoVacio() {
        while (true) {
            String entrada = leerCadenaTeclado();
            if (!entrada.isEmpty()) {
                // Proteccion para persistencia CSV: reemplaza comas por guiones
                return entrada.replace(",", " - ");
            }
            System.out.print("El campo no puede estar vacio. Ingrese nuevamente: ");
        }
    }

    private static String leerCadenaTeclado() {
        String entrada = scanner.nextLine().trim();
        if (entrada.equalsIgnoreCase("MENU")) {
            throw new OperacionCanceladaException();
        }
        return entrada;
    }
}