package consultorio.modelo;

/**
 * Enumeración que define los diferentes estados posibles en los que puede encontrarse 
 * un turno médico dentro del sistema de gestión.
 */
public enum EstadoTurno {
    PENDIENTE,
    CANCELADO,
    ATENDIDO,
    AUSENTE
}