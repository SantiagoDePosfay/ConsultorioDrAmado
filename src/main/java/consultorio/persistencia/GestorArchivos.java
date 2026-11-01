package consultorio.persistencia;

import consultorio.modelo.Paciente;
import consultorio.modelo.Turno;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GestorArchivos {

    // Nombres fijos de los archivos en la raíz del proyecto
    private static final String ARCHIVO_PACIENTES = "pacientes.csv";
    private static final String ARCHIVO_TURNOS = "turnos.csv";

    // ==========================================
    // PERSISTENCIA DE PACIENTES
    // ==========================================

    public static void guardarPacientes(List<Paciente> pacientes) {
        // try-with-resources asegura el cierre del archivo incluso si ocurre un error
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_PACIENTES))) {
            for (Paciente p : pacientes) {
                writer.println(p.aLineaCSV());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar pacientes en disco: " + e.getMessage());
        }
    }

    public static List<Paciente> cargarPacientes() {
        List<Paciente> lista = new ArrayList<>();
        File archivo = new File(ARCHIVO_PACIENTES);

        // Si el archivo aún no existe (primera ejecución), retorna la lista vacía
        if (!archivo.exists()) {
            return lista;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    Paciente p = Paciente.desdeLineaCSV(linea);
                    if (p != null) {
                        lista.add(p);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer pacientes de disco: " + e.getMessage());
        }

        return lista;
    }

    // ==========================================
    // PERSISTENCIA DE TURNOS
    // ==========================================

    public static void guardarTurnos(List<Turno> turnos) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_TURNOS))) {
            for (Turno t : turnos) {
                writer.println(t.aLineaCSV());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar turnos en disco: " + e.getMessage());
        }
    }

    public static List<Turno> cargarTurnos() {
        List<Turno> lista = new ArrayList<>();
        File archivo = new File(ARCHIVO_TURNOS);

        // Si el archivo aún no existe, retorna la lista vacía
        if (!archivo.exists()) {
            return lista;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    Turno t = Turno.desdeLineaCSV(linea);
                    if (t != null) {
                        lista.add(t);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer turnos de disco: " + e.getMessage());
        }

        return lista;
    }
}