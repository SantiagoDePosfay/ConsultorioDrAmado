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

/**
 * Clase utilitaria encargada de gestionar la persistencia y lectura de datos 
 * en archivos de formato CSV ubicados en la raíz del proyecto (pacientes y turnos).
 */
public class GestorArchivos {

    /** Nombre del archivo CSV para la persistencia de los registros de pacientes. */
    private static final String ARCHIVO_PACIENTES = "pacientes.csv";
    
    /** Nombre del archivo CSV para la persistencia de los registros de turnos. */
    private static final String ARCHIVO_TURNOS = "turnos.csv";

    // ==========================================
    // PERSISTENCIA DE PACIENTES
    // ==========================================
    
    /**
     * Guarda la lista completa de pacientes en el archivo CSV correspondiente en disco.
     * 
     * @param pacientes La {@link List} de objetos {@link Paciente} a persistir.
     */
    public static void guardarPacientes(List<Paciente> pacientes) {
        // try-with-resources asegura el cierre del archivo incluso si ocurre un error.
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_PACIENTES))) {
            for (Paciente p : pacientes) {
                writer.println(p.aLineaCSV());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar pacientes en disco: " + e.getMessage());
        }
    }
    
    /**
     * Carga y recupera la lista de pacientes almacenada en el archivo CSV de disco.
     * Si el archivo aún no existe (primera ejecución), retorna una lista vacía.
     * 
     * @return Una {@link List} con los objetos {@link Paciente} recuperados.
     */
    public static List<Paciente> cargarPacientes() {
        List<Paciente> lista = new ArrayList<>();
        File archivo = new File(ARCHIVO_PACIENTES);

        // Si el archivo aún no existe (primera ejecución), retorna la lista vacía.
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
    
    /**
     * Guarda la lista completa de turnos en el archivo CSV correspondiente en disco.
     * 
     * @param turnos La {@link List} de objetos {@link Turno} a persistir.
     */
    public static void guardarTurnos(List<Turno> turnos) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_TURNOS))) {
            for (Turno t : turnos) {
                writer.println(t.aLineaCSV());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar turnos en disco: " + e.getMessage());
        }
    }
    
    /**
     * Carga y recupera la lista de turnos almacenada en el archivo CSV de disco.
     * Si el archivo aún no existe, retorna una lista vacía.
     * 
     * @return Una {@link List} con los objetos {@link Turno} recuperados.
     */
    public static List<Turno> cargarTurnos() {
        List<Turno> lista = new ArrayList<>();
        File archivo = new File(ARCHIVO_TURNOS);

        // Si el archivo aún no existe, retorna la lista vacía.
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