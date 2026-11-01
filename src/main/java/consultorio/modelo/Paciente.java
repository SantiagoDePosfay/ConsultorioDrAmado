package consultorio.modelo;

public class Paciente {
    
    private String dni;
    private String nombreCompleto;
    private String telefono;
    private String obraSocial;

    // Constructor
    public Paciente(String dni, String nombreCompleto, String telefono, String obraSocial) {
        this.dni = (dni != null) ? dni.trim() : "";
        // Reemplazamos ';' por ',' para evitar romper el formato del archivo CSV
        this.nombreCompleto = (nombreCompleto != null) ? nombreCompleto.replace(";", ",").trim() : "";
        this.telefono = (telefono != null) ? telefono.replace(";", ",").trim() : "";
        this.obraSocial = (obraSocial != null) ? obraSocial.replace(";", ",").trim() : "";
    }

    // Getters
    public String getDni() {
        return dni;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getObraSocial() {
        return obraSocial;
    }

    // Setters por si se necesita actualizar algún dato de contacto
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = (nombreCompleto != null) ? nombreCompleto.replace(";", ",").trim() : "";
    }

    public void setTelefono(String telefono) {
        this.telefono = (telefono != null) ? telefono.replace(";", ",").trim() : "";
    }

    public void setObraSocial(String obraSocial) {
        this.obraSocial = (obraSocial != null) ? obraSocial.replace(";", ",").trim() : "";
    }

    // Convierte el objeto a una línea de texto plano para pacientes.csv
    // Formato: DNI;Nombre;Telefono;ObraSocial
    public String aLineaCSV() {
        return dni + ";" + nombreCompleto + ";" + telefono + ";" + obraSocial;
    }

    // Método de fábrica (Factory): recibe una línea del CSV y crea el objeto Paciente
    public static Paciente desdeLineaCSV(String linea) {
        if (linea == null || linea.trim().isEmpty()) {
            return null;
        }
        
        String[] partes = linea.split(";");
        // Verificamos que al menos existan las 4 columnas
        if (partes.length < 4) {
            return null;
        }
        
        return new Paciente(partes[0], partes[1], partes[2], partes[3]);
    }

    // Útil para imprimir datos del paciente por consola de forma clara
    @Override
    public String toString() {
        return "DNI: " + dni + " | Nombre: " + nombreCompleto + 
               " | Tel: " + telefono + " | Cobertura: " + obraSocial;
    }
}