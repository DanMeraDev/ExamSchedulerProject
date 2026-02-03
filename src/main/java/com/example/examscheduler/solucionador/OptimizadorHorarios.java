package com.example.examscheduler.solucionador;

import com.example.examscheduler.modelo.Curso;
import com.example.examscheduler.modelo.Estudiante;

import java.util.*;

/**
 * Optimizador de horarios de exámenes usando el algoritmo de Ramificación y
 * Poda (Branch & Bound).
 * 
 * Características principales:
 * - Ramificación por asignación de curso a franja horaria
 * - Cota inferior basada en conflictos mínimos restantes (heurística)
 * - Poda cuando el costo estimado supera la mejor solución conocida
 * - Ordenamiento de cursos por grado de conflicto (más conflictivos primero)
 */
public class OptimizadorHorarios {

    /**
     * Record que representa una solución encontrada.
     */
    public record Solucion(Map<Curso, Integer> horario, int conflictos) {
    }

    private final List<Curso> cursos;
    private final int numeroFranjasHorarias;
    private final GrafoConflictos grafoConflictos;
    private int estadosExplorados = 0;
    private int estadosPodados = 0;

    /**
     * Constructor del optimizador.
     * 
     * @param cursos                Lista de cursos a asignar
     * @param estudiantes           Lista de estudiantes con sus inscripciones
     * @param numeroFranjasHorarias Número de franjas horarias disponibles
     */
    public OptimizadorHorarios(List<Curso> cursos, List<Estudiante> estudiantes, int numeroFranjasHorarias) {
        this.grafoConflictos = new GrafoConflictos(cursos, estudiantes);
        this.cursos = new ArrayList<>(cursos);
        this.numeroFranjasHorarias = numeroFranjasHorarias;
        this.cursos.sort(Comparator.comparingInt(grafoConflictos::obtenerGradoDeConflicto).reversed());
    }

    /**
     * Resuelve el problema de asignación de horarios usando Branch & Bound.
     * 
     * @return La mejor solución encontrada
     */
    public Solucion resolver() {
        PriorityQueue<EstadoSolucion> colaPrioridad = new PriorityQueue<>();
        int cotaSuperior = Integer.MAX_VALUE;
        Solucion mejorSolucion = new Solucion(null, cotaSuperior);
        estadosExplorados = 0;
        estadosPodados = 0;

        int cotaInicial = calcularCotaInferior(new HashMap<>(), 0);
        colaPrioridad.add(new EstadoSolucion(new HashMap<>(), 0, 0, cotaInicial));

        while (!colaPrioridad.isEmpty()) {
            EstadoSolucion estadoActual = colaPrioridad.poll();
            estadosExplorados++;

            if (estadoActual.getCostoEstimado() >= cotaSuperior) {
                estadosPodados++;
                continue;
            }

            if (estadoActual.nivel == cursos.size()) {
                if (estadoActual.costo < cotaSuperior) {
                    cotaSuperior = estadoActual.costo;
                    mejorSolucion = new Solucion(
                            new HashMap<>(estadoActual.horario),
                            estadoActual.costo);

                    if (cotaSuperior == 0) {
                        break;
                    }
                }
                continue;
            }

            Curso cursoParaAsignar = cursos.get(estadoActual.nivel);

            List<FranjaConflicto> franjasOrdenadas = new ArrayList<>();
            for (int franja = 0; franja < numeroFranjasHorarias; franja++) {
                int conflictosConFranja = calcularConflictosConFranja(
                        cursoParaAsignar, franja, estadoActual.horario);
                franjasOrdenadas.add(new FranjaConflicto(franja, conflictosConFranja));
            }
            franjasOrdenadas.sort(Comparator.comparingInt(fc -> fc.conflictos));

            for (FranjaConflicto fc : franjasOrdenadas) {
                int nuevoCosto = estadoActual.costo + fc.conflictos;

                if (nuevoCosto >= cotaSuperior) {
                    estadosPodados++;
                    continue;
                }

                Map<Curso, Integer> nuevoHorario = new HashMap<>(estadoActual.horario);
                nuevoHorario.put(cursoParaAsignar, fc.franja);

                int nuevaCotaInferior = calcularCotaInferior(nuevoHorario, estadoActual.nivel + 1);

                if (nuevoCosto + nuevaCotaInferior < cotaSuperior) {
                    colaPrioridad.add(new EstadoSolucion(
                            nuevoHorario, nuevoCosto, estadoActual.nivel + 1, nuevaCotaInferior));
                } else {
                    estadosPodados++;
                }
            }
        }

        return mejorSolucion;
    }

    /**
     * Calcula los conflictos que genera asignar un curso a una franja específica.
     */
    private int calcularConflictosConFranja(Curso curso, int franja, Map<Curso, Integer> horarioActual) {
        int conflictos = 0;
        for (Map.Entry<Curso, Integer> asignacion : horarioActual.entrySet()) {
            if (asignacion.getValue() == franja) {
                conflictos += grafoConflictos.obtenerConflictos(curso, asignacion.getKey());
            }
        }
        return conflictos;
    }

    /**
     * Calcula una cota inferior (heurística) para el número mínimo de conflictos
     * adicionales.
     * Esta estimación nunca sobreestima el costo real (admisible).
     * 
     * Estrategia: Para cada curso no asignado, calcula el mínimo de conflictos
     * que tendría con los cursos ya asignados en cualquier franja.
     */
    private int calcularCotaInferior(Map<Curso, Integer> horarioActual, int nivelActual) {
        int cotaInferior = 0;

        for (int i = nivelActual; i < cursos.size(); i++) {
            Curso cursoNoAsignado = cursos.get(i);
            int minConflicto = Integer.MAX_VALUE;

            for (int franja = 0; franja < numeroFranjasHorarias; franja++) {
                int conflictosFranja = 0;
                for (Map.Entry<Curso, Integer> asignacion : horarioActual.entrySet()) {
                    if (asignacion.getValue() == franja) {
                        conflictosFranja += grafoConflictos.obtenerConflictos(
                                cursoNoAsignado, asignacion.getKey());
                    }
                }
                minConflicto = Math.min(minConflicto, conflictosFranja);

                if (minConflicto == 0)
                    break;
            }

            if (minConflicto != Integer.MAX_VALUE) {
                cotaInferior += minConflicto;
            }
        }

        return cotaInferior;
    }

    /**
     * Obtiene el número de estados explorados en la última ejecución.
     */
    public int obtenerEstadosExplorados() {
        return estadosExplorados;
    }

    /**
     * Obtiene el número de estados podados en la última ejecución.
     */
    public int obtenerEstadosPodados() {
        return estadosPodados;
    }

    /**
     * Obtiene los detalles de los conflictos en un horario dado.
     */
    public List<String> obtenerDetallesDeConflictos(Map<Curso, Integer> horario) {
        List<String> detalles = new ArrayList<>();
        if (horario == null)
            return detalles;

        Map<Integer, List<Curso>> franjaACursos = new HashMap<>();
        for (Map.Entry<Curso, Integer> asignacion : horario.entrySet()) {
            franjaACursos.computeIfAbsent(asignacion.getValue(), k -> new ArrayList<>())
                    .add(asignacion.getKey());
        }

        for (Map.Entry<Integer, List<Curso>> entrada : franjaACursos.entrySet()) {
            List<Curso> cursosEnFranja = entrada.getValue();
            for (int i = 0; i < cursosEnFranja.size(); i++) {
                for (int j = i + 1; j < cursosEnFranja.size(); j++) {
                    Curso c1 = cursosEnFranja.get(i);
                    Curso c2 = cursosEnFranja.get(j);
                    int conflictos = grafoConflictos.obtenerConflictos(c1, c2);
                    if (conflictos > 0) {
                        detalles.add(String.format(
                                "Franja %d: %s (%s) y %s (%s) - %d estudiante(s) afectado(s)",
                                entrada.getKey() + 1,
                                c1.id(), c1.nombre(),
                                c2.id(), c2.nombre(),
                                conflictos));
                    }
                }
            }
        }
        return detalles;
    }

    /**
     * Obtiene el grafo de conflictos.
     */
    public GrafoConflictos getGrafoConflictos() {
        return grafoConflictos;
    }

    /**
     * Clase auxiliar para ordenar franjas por conflicto.
     */
    private record FranjaConflicto(int franja, int conflictos) {
    }
}