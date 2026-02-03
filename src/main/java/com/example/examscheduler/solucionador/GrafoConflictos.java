package com.example.examscheduler.solucionador;

import com.example.examscheduler.modelo.Curso;
import com.example.examscheduler.modelo.Estudiante;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Representa el grafo de conflictos entre cursos.
 * 
 * En este grafo:
 * - Cada vértice representa un curso
 * - Cada arista representa un conflicto potencial entre dos cursos
 * - El peso de cada arista indica el número de estudiantes inscritos en ambos
 * cursos
 * 
 * Si dos cursos tienen una arista entre ellos, programarlos en la misma
 * franja horaria causará conflictos para los estudiantes que tienen ambos.
 */
public class GrafoConflictos {

    private final Graph<Curso, DefaultWeightedEdge> grafo;

    /**
     * Construye el grafo de conflictos a partir de la lista de cursos y
     * estudiantes.
     * 
     * @param cursos      Lista de cursos
     * @param estudiantes Lista de estudiantes con sus inscripciones
     */
    public GrafoConflictos(List<Curso> cursos, List<Estudiante> estudiantes) {
        this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        construir(cursos, estudiantes);
    }

    /**
     * Construye el grafo añadiendo vértices (cursos) y aristas (conflictos).
     */
    private void construir(List<Curso> cursos, List<Estudiante> estudiantes) {
        for (Curso curso : cursos) {
            grafo.addVertex(curso);
        }

        for (Estudiante estudiante : estudiantes) {
            List<Curso> cursosDelEstudiante = estudiante.cursos().stream()
                    .filter(grafo::containsVertex)
                    .collect(Collectors.toList());

            for (int i = 0; i < cursosDelEstudiante.size(); i++) {
                for (int j = i + 1; j < cursosDelEstudiante.size(); j++) {
                    Curso c1 = cursosDelEstudiante.get(i);
                    Curso c2 = cursosDelEstudiante.get(j);

                    DefaultWeightedEdge arista = grafo.getEdge(c1, c2);
                    if (arista == null) {
                        arista = grafo.addEdge(c1, c2);
                        grafo.setEdgeWeight(arista, 1);
                    } else {
                        double pesoActual = grafo.getEdgeWeight(arista);
                        grafo.setEdgeWeight(arista, pesoActual + 1);
                    }
                }
            }
        }
    }

    /**
     * Obtiene el grafo JGraphT subyacente.
     */
    public Graph<Curso, DefaultWeightedEdge> obtenerGrafo() {
        return grafo;
    }

    /**
     * Obtiene el número de conflictos (estudiantes compartidos) entre dos cursos.
     * 
     * @return Número de estudiantes que tienen ambos cursos, o 0 si no hay
     *         conflicto
     */
    public int obtenerConflictos(Curso curso1, Curso curso2) {
        if (curso1.equals(curso2))
            return 0;

        DefaultWeightedEdge arista = grafo.getEdge(curso1, curso2);
        if (arista != null) {
            return (int) grafo.getEdgeWeight(arista);
        }
        return 0;
    }

    /**
     * Obtiene el grado de conflicto de un curso (número de cursos con los que
     * conflicta).
     */
    public int obtenerGradoDeConflicto(Curso curso) {
        if (!grafo.containsVertex(curso))
            return 0;
        return grafo.degreeOf(curso);
    }

    /**
     * Obtiene la suma total de pesos de conflicto para un curso.
     * (Más preciso que el grado para priorizar cursos problemáticos)
     */
    public int obtenerPesoTotalDeConflictos(Curso curso) {
        if (!grafo.containsVertex(curso))
            return 0;

        int pesoTotal = 0;
        for (DefaultWeightedEdge arista : grafo.edgesOf(curso)) {
            pesoTotal += (int) grafo.getEdgeWeight(arista);
        }
        return pesoTotal;
    }

    /**
     * Obtiene los cursos adyacentes (en conflicto) con un curso dado.
     */
    public List<Curso> obtenerCursosEnConflicto(Curso curso) {
        List<Curso> cursosEnConflicto = new ArrayList<>();
        if (!grafo.containsVertex(curso))
            return cursosEnConflicto;

        for (DefaultWeightedEdge arista : grafo.edgesOf(curso)) {
            Curso fuente = grafo.getEdgeSource(arista);
            Curso destino = grafo.getEdgeTarget(arista);
            cursosEnConflicto.add(fuente.equals(curso) ? destino : fuente);
        }
        return cursosEnConflicto;
    }

    /**
     * Obtiene el número total de aristas (pares de cursos en conflicto).
     */
    public int obtenerNumeroDeAristas() {
        return grafo.edgeSet().size();
    }

    /**
     * Obtiene el número total de conflictos potenciales (suma de todos los pesos).
     */
    public int obtenerTotalConflictosPotenciales() {
        int total = 0;
        for (DefaultWeightedEdge arista : grafo.edgeSet()) {
            total += (int) grafo.getEdgeWeight(arista);
        }
        return total;
    }

    /**
     * Verifica si el grafo está vacío (sin conflictos).
     */
    public boolean estaVacio() {
        return grafo.edgeSet().isEmpty();
    }

    /**
     * Obtiene todos los cursos del grafo.
     */
    public Set<Curso> obtenerCursos() {
        return grafo.vertexSet();
    }
}
