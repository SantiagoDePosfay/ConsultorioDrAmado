package consultorio.modelo;

/**
 * Representa a un paciente registrado en el sistema del consultorio médico,
 * almacenando su información personal, de contacto y su cobertura de salud.
 */
public class Paciente {
    
    private String dni;
    private String nombreCompleto;
    private String telefono;
    private String obraSocial;

    /**
     * Constructor:
     * Construye una nueva instancia de {@code Paciente} sanitizando los campos de texto
     * para asegurar la correcta persistencia en archivos estructurados.
     * 
     * @param dni Documento Nacional de Identidad del paciente.
     * @param nombreCompleto Nombre y apellido del paciente.
     * @param telefono Teléfono de contacto.
     * @param obraSocial Obra social del paciente o "Particular".
     */
    public Paciente(String dni, String nombreCompleto, String telefono, String obraSocial) {
        this.dni = (dni != null) ? dni.trim() : "";
        // Reemplazamos ';' por ',' para evitar romper el formato del archivo CSV.
        this.nombreCompleto = (nombreCompleto != null) ? nombreCompleto.replace(";", ",").trim() : "";
        this.telefono = (telefono != null) ? telefono.replace(";", ",").trim() : "";
        this.obraSocial = (obraSocial != null) ? obraSocial.replace(";", ",").trim() : "";
    }

    // GETTERS.
    
    /**
     * Obtiene el DNI del paciente.
     * @return Una cadena con el DNI.
     */
    public String getDni() {
        return dni;
    }
    
    /**
     * Obtiene el nombre completo del paciente.
     * @return Una cadena con el nombre y apellido.
     */
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    
    /**
     * Obtiene el teléfono de contacto del paciente.
     * @return Una cadena con el número de teléfono.
     */
    public String getTelefono() {
        return telefono;
    }
    
    /**
     * Obtiene la obra social o cobertura médica del paciente.
     * @return Una cadena con la obra social.
     */
    public String getObraSocial() {
        return obraSocial;
    }

    // Setters por si se necesita actualizar algún dato de contacto.
    
    /**
     * Actualiza el nombre completo del paciente.
     * @param nombreCompleto El nuevo nombre completo.
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = (nombreCompleto != null) ? nombreCompleto.replace(";", ",").trim() : "";
    }
    
    /**
     * Actualiza el teléfono de contacto del paciente.
     * @param telefono El nuevo número telefónico.
     */
    public void setTelefono(String telefono) {
        this.telefono = (telefono != null) ? telefono.replace(";", ",").trim() : "";
    }
    
    /**
     * Actualiza la obra social del paciente.
     * @param obraSocial La nueva obra social o prepaga.
     */
    public void setObraSocial(String obraSocial) {
        this.obraSocial = (obraSocial != null) ? obraSocial.replace(";", ",").trim() : "";
    }

    /**
     * Convierte el objeto paciente a una línea de texto plano con formato CSV 
     * utilizando punto y coma como delimitador.
     * Formato: {@code DNI;Nombre;Telefono;ObraSocial}
     * 
     * @return Una cadena de texto lista para ser persistida en archivo.
     */
    public String aLineaCSV() {
        return dni + ";" + nombreCompleto + ";" + telefono + ";" + obraSocial;
    }

    /**
     * Método de fábrica (Factory) que recibe una línea proveniente del archivo CSV 
     * y reconstruye e instancia un objeto {@code Paciente}.
     * 
     * @param linea La línea de texto separada por punto y coma.
     * @return Una nueva instancia de {@code Paciente}, o {@code null} si la línea es inválida o incompleta.
     */
    public static Paciente desdeLineaCSV(String linea) {
        if (linea == null || linea.trim().isEmpty()) {
            return null;
        }
        
        String[] partes = linea.split(";");
        // Verificamos que al menos existan las 4 columnas.
        if (partes.length < 4) {
            return null;
        }
        
        return new Paciente(partes[0], partes[1], partes[2], partes[3]);
    }

    /**
     * Retorna una representación en formato de texto legible del paciente,
     * útil para impresión por consola.
     * 
     * @return Una cadena con los datos principales del paciente.
     */
    @Override
    public String toString() {
        return "DNI: " + dni + " | Nombre: " + nombreCompleto + 
               " | Tel: " + telefono + " | Cobertura: " + obraSocial;
    }
}