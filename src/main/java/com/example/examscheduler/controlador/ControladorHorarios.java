package com.example.examscheduler.controlador;

import com.example.examscheduler.modelo.Curso;
import com.example.examscheduler.modelo.Estudiante;
import com.example.examscheduler.solucionador.OptimizadorHorarios;
import com.example.examscheduler.vista.VistaPrincipal;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * Controlador que maneja la lógica de la aplicación y coordina
 * la comunicación entre la vista y el modelo/solucionador.
 */
public class ControladorHorarios {

    private VistaPrincipal vista;

    public ControladorHorarios() {
    }

    /**
     * Establece la referencia a la vista principal.
     */
    public void setVista(VistaPrincipal vista) {
        this.vista = vista;
    }

    /**
     * Inicia la aplicación cargando los datos iniciales y mostrando la ventana.
     */
    public void iniciar() {
        this.vista.cargarDatosDeEjemplo(0);
        this.vista.setVisible(true);
    }

    /**
     * Solicita la optimización de horarios.
     * La ejecución se realiza en un hilo separado para no bloquear la UI.
     */
    public void solicitarOptimizacion() {
        int numeroFranjas = vista.obtenerNumeroFranjas();
        List<Curso> cursos = vista.obtenerCursos();
        List<Estudiante> estudiantes = vista.obtenerEstudiantes();

        if (cursos.isEmpty()) {
            mostrarError("No hay cursos para asignar.");
            return;
        }

        if (estudiantes.isEmpty()) {
            mostrarError("No hay estudiantes registrados.");
            return;
        }

        if (numeroFranjas < 1) {
            mostrarError("Debe haber al menos una franja horaria.");
            return;
        }

        vista.mostrarProgreso(true);

        SwingWorker<ResultadoOptimizacion, Void> worker = new SwingWorker<>() {
            @Override
            protected ResultadoOptimizacion doInBackground() {
                OptimizadorHorarios optimizador = new OptimizadorHorarios(
                        cursos, estudiantes, numeroFranjas);

                long tiempoInicio = System.currentTimeMillis();
                OptimizadorHorarios.Solucion solucion = optimizador.resolver();
                long tiempoFin = System.currentTimeMillis();

                return new ResultadoOptimizacion(solucion, tiempoFin - tiempoInicio, optimizador);
            }

            @Override
            protected void done() {
                try {
                    ResultadoOptimizacion resultado = get();
                    vista.mostrarSolucion(
                            resultado.solucion(),
                            resultado.duracion(),
                            resultado.optimizador());
                } catch (Exception e) {
                    mostrarError("Error durante la optimizacion: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    vista.mostrarProgreso(false);
                }
            }
        };

        worker.execute();
    }

    /**
     * Cambia los datos del ejemplo seleccionado.
     */
    public void cambiarDatosDeEjemplo(int indice) {
        vista.cargarDatosDeEjemplo(indice);
    }

    /**
     * Muestra un mensaje de error en la vista.
     */
    private void mostrarError(String mensaje) {
        javax.swing.JOptionPane.showMessageDialog(
                vista,
                mensaje,
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Record para encapsular el resultado de la optimización.
     */
    private record ResultadoOptimizacion(
            OptimizadorHorarios.Solucion solucion,
            long duracion,
            OptimizadorHorarios optimizador) {
    }
}
