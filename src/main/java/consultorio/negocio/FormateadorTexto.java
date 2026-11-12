package consultorio.negocio;

/**
 * Clase utilitaria encargada del tratamiento, formateo y sanitización de cadenas de texto
 * utilizadas en el sistema del consultorio (como nombres de pacientes o motivos de consulta).
 */
public class FormateadorTexto {

    /**
     * Convierte un texto a formato Título (ej: "juan perez" -> "Juan Perez").
     * Limpia espacios repetidos o en los extremos, dejando la primera letra de cada palabra
     * en mayúscula y el resto en minúscula.
     * 
     * @param texto La cadena de texto original ingresada por teclado o archivo.
     * @return El texto capitalizado y sin espacios sobrantes; o una cadena vacía si el texto es nulo o en blanco.
     */
    public static String capitalizarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }
        String[] palabras = texto.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                sb.append(Character.toUpperCase(palabra.charAt(0)))
                  .append(palabra.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Sanitiza cadenas de texto para evitar que rompan la estructura de almacenamiento
     * en archivos con formato CSV (reemplazando comas y puntos y comas).
     * 
     * @param entrada La cadena de texto a sanitizar.
     * @return La cadena limpia sin comas ni puntos y comas, o una cadena vacía si la entrada es nula.
     */
    public static String sanitizarParaCsv(String entrada) {
        if (entrada == null) return "";
        return entrada.replace(",", " ").replace(";", " ").trim();
    }
}