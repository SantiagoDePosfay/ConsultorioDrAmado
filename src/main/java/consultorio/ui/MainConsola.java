package consultorio.ui;

import consultorio.modelo.EstadoTurno;
import consultorio.modelo.Paciente;
import consultorio.modelo.Turno;
import consultorio.negocio.ConsultorioServicio;
import consultorio.negocio.GeneradorPDF;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class MainConsola {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ConsultorioServicio servicio = new ConsultorioServicio();
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        int opcion = -1;

        while (opcion != 0) {
            mostrarMenuPrincipal();
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    registrarPaciente();
                    break;
                case 2:
                    registrarNuevoTurno();
                    break;
                case 3:
                    consultarAgendaDiaria();
                    break;
                case 4:
                    exportarAgendaDirectaPDF();
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
        }
    }

    // ==========================================
    // MENU PRINCIPAL
    // ==========================================

    private static void mostrarMenuPrincipal() {
        System.out.println("\n=======================================================");
        System.out.println("  SISTEMA DE GESTION DE TURNOS - DR. SALVADOR AMADO");
        System.out.println("=======================================================");
        System.out.println("1. Identificar y dar de alta paciente");
        System.out.println("2. Registrar nuevo turno");
        System.out.println("3. Consultar agenda diaria");
        System.out.println("4. Exportar agenda diaria a PDF");
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

        LocalDate fecha;
        while (true) {
            fecha = leerFecha("Ingrese fecha del turno (DD/MM/AAAA): ");
            DayOfWeek dia = fecha.getDayOfWeek();
            if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
                System.out.println("Error: El consultorio atiende unicamente de lunes a viernes.");
            } else {
                break;
            }
        }

        LocalTime hora = seleccionarHorarioDeGrilla();

        String error = servicio.validarDisponibilidad(fecha, hora, -1);
        if (error != null) {
            System.out.println("Error: " + error);
            return;
        }

        System.out.print("Ingrese motivo de la consulta: ");
        String motivo = leerTextoNoVacio();

        servicio.registrarTurno(dni, fecha, hora, motivo);
        System.out.println("Turno registrado exitosamente en estado PENDIENTE.");
    }

    // =========================================
    // FUNCIONALIDAD 3: CONSULTAR AGENDA DIARIA 
    // =========================================

    private static void consultarAgendaDiaria() {
        System.out.println("\n--- CONSULTAR AGENDA DIARIA ---");
        LocalDate fecha = leerFecha("Ingrese fecha a consultar (DD/MM/AAAA): ");

        List<Turno> turnos = servicio.obtenerAgendaDiaria(fecha);
        if (turnos.isEmpty()) {
            System.out.println("No hay turnos asignados para la fecha seleccionada.");
            return;
        }

        mostrarListadoTurnosConDetalle(turnos);

        System.out.print("\nDesea exportar esta agenda a PDF? (S/N): ");
        String respuesta = scanner.nextLine().trim();
        if (respuesta.equalsIgnoreCase("S")) {
            boolean exito = GeneradorPDF.exportarAgendaPDF(fecha, turnos, servicio);
            if (exito) {
                System.out.println("Archivo PDF 'agenda_" + fecha + ".pdf' generado correctamente.");
            }
        }
    }

    // ========================================
    // FUNCIONALIDAD 4: EXPORTAR A PDF DIRECTO 
    // ========================================

    private static void exportarAgendaDirectaPDF() {
        System.out.println("\n--- EXPORTAR AGENDA DIARIA A PDF ---");
        LocalDate fecha = leerFecha("Ingrese la fecha de la agenda (DD/MM/AAAA): ");

        List<Turno> turnos = servicio.obtenerAgendaDiaria(fecha);
        if (turnos.isEmpty()) {
            System.out.println("No hay turnos asignados para la fecha seleccionada. No se genera PDF.");
            return;
        }

        boolean exito = GeneradorPDF.exportarAgendaPDF(fecha, turnos, servicio);
        if (exito) {
            System.out.println("Archivo PDF 'agenda_" + fecha + ".pdf' generado correctamente.");
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

        int seleccion = leerEntero("\nSeleccione el numero de turno a cancelar (0 para volver): ");
        if (seleccion <= 0 || seleccion > pendientes.size()) {
            System.out.println("Operacion cancelada.");
            return;
        }

        Turno elegido = pendientes.get(seleccion - 1);
        System.out.print("Confirma la cancelacion del turno ID #" + elegido.getIdTurno() + "? (S/N): ");
        String confirmacion = scanner.nextLine().trim();

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

        int seleccion = leerEntero("\nSeleccione el numero de turno a modificar (0 para cancelar): ");
        if (seleccion <= 0 || seleccion > pendientes.size()) {
            System.out.println("Operacion cancelada.");
            return;
        }

        Turno elegido = pendientes.get(seleccion - 1);

        System.out.println("\nIngrese los nuevos datos del turno:");
        LocalDate nuevaFecha = leerFecha("Nueva fecha (DD/MM/AAAA): ");
        LocalTime nuevaHora = seleccionarHorarioDeGrilla();

        System.out.print("Nuevo motivo (Enter para conservar actual '" + elegido.getMotivo() + "'): ");
        String nuevoMotivo = scanner.nextLine().trim();
        if (nuevoMotivo.isEmpty()) {
            nuevoMotivo = elegido.getMotivo();
        }

        String error = servicio.modificarTurno(elegido.getIdTurno(), nuevaFecha, nuevaHora, nuevoMotivo);
        if (error != null) {
            System.out.println("Error: " + error);
            System.out.println("Se conserva el turno original sin modificaciones.");
        } else {
            System.out.println("Turno reprogramado exitosamente.");
        }
    }

    private static void registrarAsistencia() {
        System.out.println("\n--- REGISTRAR ASISTENCIA ---");
        int idTurno = leerEntero("Ingrese ID del turno: ");

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
        int opcionEstado = leerEntero("Seleccione opcion: ");

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
        String dni = scanner.nextLine().trim();

        System.out.print("Fecha Desde DD/MM/AAAA (o Enter para omitir): ");
        String desdeStr = scanner.nextLine().trim();
        LocalDate fechaDesde = parsearFechaOpcional(desdeStr);

        System.out.print("Fecha Hasta DD/MM/AAAA (o Enter para omitir): ");
        String hastaStr = scanner.nextLine().trim();
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

        // Resumen estadistico 
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

    private static LocalTime seleccionarHorarioDeGrilla() {
        System.out.println("\nSeleccione bloque de inicio (Grilla de 40 min):");
        for (int i = 0; i < ConsultorioServicio.GRILLA_HORARIOS.length; i++) {
            System.out.printf("[%2d] %s  ", (i + 1), ConsultorioServicio.GRILLA_HORARIOS[i]);
            if ((i + 1) % 6 == 0) System.out.println();
        }
        System.out.println();

        while (true) {
            int opcion = leerEntero("Ingrese numero de bloque (1 al 18): ");
            if (opcion >= 1 && opcion <= ConsultorioServicio.GRILLA_HORARIOS.length) {
                return ConsultorioServicio.GRILLA_HORARIOS[opcion - 1];
            }
            System.out.println("Numero fuera de rango. Ingrese un valor del 1 al 18.");
        }
    }

    private static String leerDni() {
        while (true) {
            System.out.print("Ingrese DNI del paciente (7 u 8 digitos): ");
            String entrada = scanner.nextLine().trim();
            if (entrada.matches("^\\d{7,8}$")) {
                return entrada;
            }
            System.out.println("Ingrese un numero de DNI valido (solo numeros, entre 7 y 8 digitos).");
        }
    }

    private static LocalDate leerFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();
            try {
                return LocalDate.parse(entrada, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Formato de fecha invalido. Ingrese en formato DD/MM/AAAA (ej. 15/11/2026).");
            }
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

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Debe ingresar un numero entero.");
            }
        }
    }

    private static String leerTextoNoVacio() {
        while (true) {
            String entrada = scanner.nextLine().trim();
            if (!entrada.isEmpty()) {
                return entrada;
            }
            System.out.print("El campo no puede estar vacio. Ingrese nuevamente: ");
        }
    }
}