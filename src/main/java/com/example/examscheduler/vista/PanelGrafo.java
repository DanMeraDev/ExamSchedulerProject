package com.example.examscheduler.vista;

import com.example.examscheduler.modelo.Curso;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel personalizado para visualizar el grafo de conflictos.
 * Muestra los cursos como nodos y las aristas representan conflictos
 * (estudiantes que tienen ambos cursos).
 */
public class PanelGrafo extends JPanel {

    private static final int PADDING = 50;
    private static final int NODE_WIDTH = 80;
    private static final int NODE_HEIGHT = 40;
    private static final int MIN_DIMENSION = 400;

    private JGraphXAdapter<Curso, DefaultWeightedEdge> adaptadorGrafo;
    private mxGraphComponent componenteGrafo;
    private Graph<Curso, DefaultWeightedEdge> grafoActual;
    private final Map<String, Object> estiloBaseNodo;

    public PanelGrafo() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Grafo de Conflictos"));
        setPreferredSize(new Dimension(MIN_DIMENSION, MIN_DIMENSION));
        setMinimumSize(new Dimension(MIN_DIMENSION, MIN_DIMENSION));

        this.estiloBaseNodo = new HashMap<>();
        estiloBaseNodo.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
        estiloBaseNodo.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_ELLIPSE);
        estiloBaseNodo.put(mxConstants.STYLE_STROKECOLOR, "#2C3E50");
        estiloBaseNodo.put(mxConstants.STYLE_STROKEWIDTH, 2);
        estiloBaseNodo.put(mxConstants.STYLE_FONTCOLOR, "#2C3E50");
        estiloBaseNodo.put(mxConstants.STYLE_FONTSIZE, 11);
        estiloBaseNodo.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        estiloBaseNodo.put(mxConstants.STYLE_SHADOW, true);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (grafoActual != null && getWidth() > 0 && getHeight() > 0) {
                    aplicarLayoutCircular();
                }
            }
        });
    }

    /**
     * Dibuja el grafo de conflictos en el panel.
     * 
     * @param grafo El grafo JGraphT con cursos como vértices y conflictos como
     *              aristas
     */
    public void dibujarGrafo(final Graph<Curso, DefaultWeightedEdge> grafo) {
        this.grafoActual = grafo;

        if (componenteGrafo != null) {
            remove(componenteGrafo);
        }

        adaptadorGrafo = new JGraphXAdapter<Curso, DefaultWeightedEdge>(grafo) {
            @Override
            public String getLabel(Object cell) {
                if (cell instanceof mxICell) {
                    mxICell mxCell = (mxICell) cell;
                    if (mxCell.isVertex()) {
                        Curso curso = this.getCellToVertexMap().get(cell);
                        if (curso != null) {
                            return curso.id();
                        }
                    } else if (mxCell.isEdge()) {
                        DefaultWeightedEdge edge = this.getCellToEdgeMap().get(cell);
                        if (edge != null) {
                            int peso = (int) grafo.getEdgeWeight(edge);
                            return peso > 0 ? String.valueOf(peso) : "";
                        }
                    }
                }
                return super.getLabel(cell);
            }
        };

        configurarEstilosGrafo();

        componenteGrafo = new mxGraphComponent(adaptadorGrafo);
        componenteGrafo.setConnectable(false);
        componenteGrafo.getGraph().setCellsEditable(false);
        componenteGrafo.getGraph().setCellsMovable(true);
        componenteGrafo.getGraph().setCellsResizable(false);
        componenteGrafo.setGridVisible(false);
        componenteGrafo.getViewport().setBackground(new Color(250, 250, 255));
        componenteGrafo.setBorder(BorderFactory.createEmptyBorder());

        add(componenteGrafo, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            aplicarLayoutCircular();
            colorearNodos(null, null);
        });

        revalidate();
        repaint();
    }

    /**
     * Configura los estilos globales del grafo (aristas, nodos por defecto)
     */
    private void configurarEstilosGrafo() {
        Map<String, Object> estiloArista = new HashMap<>();
        estiloArista.put(mxConstants.STYLE_STROKECOLOR, "#E74C3C");
        estiloArista.put(mxConstants.STYLE_STROKEWIDTH, 2);
        estiloArista.put(mxConstants.STYLE_FONTCOLOR, "#C0392B");
        estiloArista.put(mxConstants.STYLE_FONTSIZE, 10);
        estiloArista.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        estiloArista.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "#FFFFFF");
        estiloArista.put(mxConstants.STYLE_ROUNDED, true);
        estiloArista.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
        adaptadorGrafo.getStylesheet().putCellStyle("ARISTA_CONFLICTO", estiloArista);

        adaptadorGrafo.setCellStyle("ARISTA_CONFLICTO",
                adaptadorGrafo.getEdgeToCellMap().values().toArray());
    }

    /**
     * Aplica el layout circular con dimensiones calculadas correctamente.
     */
    private void aplicarLayoutCircular() {
        if (adaptadorGrafo == null || grafoActual == null)
            return;

        int numVertices = grafoActual.vertexSet().size();
        if (numVertices == 0)
            return;

        int anchoDisponible = Math.max(getWidth() - 2 * PADDING, MIN_DIMENSION);
        int altoDisponible = Math.max(getHeight() - 2 * PADDING, MIN_DIMENSION);

        double radioMinimo = (numVertices * NODE_WIDTH) / (2 * Math.PI);
        double radioMaximo = Math.min(anchoDisponible, altoDisponible) / 2.0 - NODE_WIDTH;
        double radio = Math.max(radioMinimo, Math.min(radioMaximo, 150));

        adaptadorGrafo.getModel().beginUpdate();
        try {
            mxCircleLayout layout = new mxCircleLayout(adaptadorGrafo);
            layout.setX0((anchoDisponible - 2 * radio) / 2.0 + PADDING);
            layout.setY0((altoDisponible - 2 * radio) / 2.0 + PADDING);
            layout.setRadius(radio);
            layout.setMoveCircle(false);
            layout.execute(adaptadorGrafo.getDefaultParent());

            for (mxICell celda : adaptadorGrafo.getVertexToCellMap().values()) {
                adaptadorGrafo.getModel().getGeometry(celda).setWidth(NODE_WIDTH);
                adaptadorGrafo.getModel().getGeometry(celda).setHeight(NODE_HEIGHT);
            }
        } finally {
            adaptadorGrafo.getModel().endUpdate();
        }

        if (componenteGrafo != null) {
            componenteGrafo.refresh();
        }
    }

    /**
     * Colorea los nodos del grafo según el horario asignado.
     * 
     * @param horario Mapa de curso a franja horaria (puede ser null)
     * @param colores Array de colores para cada franja
     */
    public void colorearNodos(Map<Curso, Integer> horario, Color[] colores) {
        if (adaptadorGrafo == null)
            return;

        Map<mxICell, Curso> celdaACurso = new HashMap<>();
        for (Map.Entry<Curso, mxICell> entry : adaptadorGrafo.getVertexToCellMap().entrySet()) {
            celdaACurso.put(entry.getValue(), entry.getKey());
        }

        adaptadorGrafo.getModel().beginUpdate();
        try {
            for (mxICell celda : adaptadorGrafo.getVertexToCellMap().values()) {
                Curso curso = celdaACurso.get(celda);
                Map<String, Object> nuevoEstilo = new HashMap<>(estiloBaseNodo);

                if (horario != null && horario.containsKey(curso) && colores != null) {
                    int franja = horario.get(curso);
                    Color color = colores[franja % colores.length];
                    nuevoEstilo.put(mxConstants.STYLE_FILLCOLOR, toHexString(color));
                    nuevoEstilo.put(mxConstants.STYLE_GRADIENTCOLOR, toHexString(color.brighter()));
                    nuevoEstilo.put(mxConstants.STYLE_GRADIENT_DIRECTION, mxConstants.DIRECTION_SOUTH);
                } else {
                    nuevoEstilo.put(mxConstants.STYLE_FILLCOLOR, "#AED6F1");
                    nuevoEstilo.put(mxConstants.STYLE_GRADIENTCOLOR, "#D6EAF8");
                    nuevoEstilo.put(mxConstants.STYLE_GRADIENT_DIRECTION, mxConstants.DIRECTION_SOUTH);
                }

                String nombreEstilo = "ESTILO_" + curso.id().replaceAll("[^A-Za-z0-9_]", "_");
                adaptadorGrafo.getStylesheet().putCellStyle(nombreEstilo, nuevoEstilo);
                adaptadorGrafo.setCellStyle(nombreEstilo, new Object[] { celda });
            }
        } finally {
            adaptadorGrafo.getModel().endUpdate();
        }

        if (componenteGrafo != null) {
            componenteGrafo.refresh();
        }
    }

    /**
     * Convierte un color a formato hexadecimal.
     */
    private String toHexString(Color color) {
        return "#" + String.format("%02X%02X%02X",
                color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Obtiene el componente del grafo (para acceso externo si es necesario).
     */
    public mxGraphComponent getComponenteGrafo() {
        return componenteGrafo;
    }
}
