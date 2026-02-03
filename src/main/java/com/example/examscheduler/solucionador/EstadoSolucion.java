package com.example.examscheduler.solucionador;

import com.example.examscheduler.modelo.Curso;
import java.util.Map;

/**
 * Representa un estado parcial en el árbol de búsqueda del algoritmo Branch &
 * Bound.
 * Cada estado contiene:
 * - horario: asignaciones parciales de cursos a franjas horarias
 * - costo: número de conflictos acumulados hasta este estado
 * - nivel: número de cursos ya asignados
 * - cotaInferior: estimación del costo mínimo adicional necesario
 */
public class EstadoSolucion implements Comparable<EstadoSolucion> {

    final Map<Curso, Integer> horario;
    final int costo;
    final int nivel;
    final int cotaInferior;

    /**
     * Constructor principal con cota inferior.
     */
    public EstadoSolucion(Map<Curso, Integer> horario, int costo, int nivel, int cotaInferior) {
        this.horario = horario;
        this.costo = costo;
        this.nivel = nivel;
        this.cotaInferior = cotaInferior;
    }

    /**
     * Constructor de compatibilidad (sin cota inferior).
     */
    public EstadoSolucion(Map<Curso, Integer> horario, int costo, int nivel) {
        this(horario, costo, nivel, 0);
    }

    /**
     * Obtiene el costo total estimado (f = g + h).
     * g = costo actual (conflictos acumulados)
     * h = cota inferior (estimación de conflictos adicionales mínimos)
     */
    public int getCostoEstimado() {
        return costo + cotaInferior;
    }

    /**
     * Compara estados por su costo estimado (para la cola de prioridad).
     * Prioriza estados con menor costo estimado total.
     * En caso de empate, prioriza estados más profundos (más cercanos a una
     * solución).
     */
    @Override
    public int compareTo(EstadoSolucion otro) {
        int comparacionCosto = Integer.compare(this.getCostoEstimado(), otro.getCostoEstimado());
        if (comparacionCosto != 0) {
            return comparacionCosto;
        }
        return Integer.compare(otro.nivel, this.nivel);
    }

    @Override
    public String toString() {
        return String.format("Estado[nivel=%d, costo=%d, cotaInf=%d, estimado=%d]",
                nivel, costo, cotaInferior, getCostoEstimado());
    }
}
