package consultorio.negocio;

import consultorio.modelo.Turno;
import java.util.Comparator;
import java.util.List;

/**
 * Clase utilitaria encargada de aplicar criterios de ordenamiento sobre colecciones
 * de turnos médicos en memoria.
 */
public class OrdenadorTurnos {

    /**
     * Ordena cronológicamente una lista de turnos en memoria basándose primero en la fecha 
     * y, en caso de coincidir, en la hora de inicio del turno.
     * 
     * @param turnos La lista de objetos {@link Turno} que se desea ordenar.
     */
    public static void ordenarCronologicamente(List<Turno> turnos) {
        if (turnos == null || turnos.isEmpty()) return;

        turnos.sort(Comparator
            .comparing(Turno::getFecha)
            .thenComparing(Turno::getHoraInicio)
        );
    }
}