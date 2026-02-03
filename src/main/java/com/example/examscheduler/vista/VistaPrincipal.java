package com.example.examscheduler.vista;

import com.example.examscheduler.controlador.ControladorHorarios;
import com.example.examscheduler.modelo.Curso;
import com.example.examscheduler.modelo.Estudiante;
import com.example.examscheduler.solucionador.OptimizadorHorarios;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

/**
 * Vista principal de la aplicación de asignación de horarios de exámenes.
 * Implementa una interfaz gráfica con:
 * - Panel de configuración (selección de ejemplo, número de franjas)
 * - Visualización del grafo de conflictos
 * - Área de resultados con detalle de la solución
 * - Leyenda de colores para las franjas horarias
 */
public class VistaPrincipal extends JFrame {

    private static final Color COLOR_FONDO = new Color(245, 247, 250);
    private static final Color COLOR_PANEL = new Color(255, 255, 255);
    private static final Color COLOR_PRIMARIO = new Color(52, 152, 219);
    private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FUENTE_MONO = new Font("Consolas", Font.PLAIN, 12);

    private final ControladorHorarios controlador;

    private JComboBox<String> selectorEjemplos;
    private JSpinner selectorFranjas;
    private JTextArea areaResultado;
    private JButton botonResolver;
    private JButton botonExportar;
    private JButton botonDatosPersonalizados;
    private JProgressBar barraProgreso;
    private PanelGrafo panelGrafo;
    private JPanel panelLeyenda;

    private List<Curso> cursos = new ArrayList<>();
    private List<Estudiante> estudiantes = new ArrayList<>();
    private final Color[] coloresFranja;
    private OptimizadorHorarios.Solucion ultimaSolucion;

    public VistaPrincipal(ControladorHorarios controlador) {
        this.controlador = controlador;
        this.coloresFranja = generarColoresPastel(10);

        configurarVentana();
        construirInterfaz();
        configurarAcciones();
    }

    /**
     * Configura las propiedades básicas de la ventana.
     */
    private void configurarVentana() {
        setTitle("Asignador de Horarios de Examenes - Branch & Bound");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
    }

    /**
     * Construye la interfaz completa.
     */
    private void construirInterfaz() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));
        panelPrincipal.setBackground(COLOR_FONDO);
        setContentPane(panelPrincipal);

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 0));
        panelSuperior.setOpaque(false);
        panelSuperior.add(crearPanelDeControles(), BorderLayout.CENTER);
        panelSuperior.add(crearPanelLeyenda(), BorderLayout.EAST);
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        panelPrincipal.add(crearPanelDeResultados(), BorderLayout.CENTER);
        panelPrincipal.add(crearPanelProgreso(), BorderLayout.SOUTH);
    }

    /**
     * Configura los listeners de los componentes.
     */
    private void configurarAcciones() {
        botonResolver.addActionListener(e -> controlador.solicitarOptimizacion());
        selectorEjemplos.addActionListener(e -> {
            int indice = selectorEjemplos.getSelectedIndex();
            if (indice == 3) {
                abrirDialogoDatosPersonalizados();
            } else {
                controlador.cambiarDatosDeEjemplo(indice);
            }
            actualizarLeyenda();
        });
        selectorFranjas.addChangeListener(e -> actualizarLeyenda());
        botonExportar.addActionListener(e -> exportarResultados());
        botonDatosPersonalizados.addActionListener(e -> abrirDialogoDatosPersonalizados());
    }

    /**
     * Abre el dialogo para ingresar datos personalizados.
     */
    private void abrirDialogoDatosPersonalizados() {
        DialogoDatosPersonalizados dialogo = new DialogoDatosPersonalizados(this);
        dialogo.setVisible(true);

        if (dialogo.fueConfirmado()) {
            cursos = new ArrayList<>(dialogo.getCursos());
            estudiantes = new ArrayList<>(dialogo.getEstudiantes());
            ultimaSolucion = null;
            botonExportar.setEnabled(false);

            selectorEjemplos.setSelectedIndex(3);

            mostrarDatosDelProblema();
            if (!cursos.isEmpty()) {
                OptimizadorHorarios optimizadorTemp = new OptimizadorHorarios(
                        cursos, estudiantes, obtenerNumeroFranjas());
                actualizarGrafo(optimizadorTemp, null);
            }
            actualizarLeyenda();
        }
    }

    /**
     * Crea el panel de controles de configuración.
     */
    private JPanel crearPanelDeControles() {
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelControles.setBackground(COLOR_PANEL);
        panelControles.setBorder(crearBordeTitulado("Configuracion"));

        String[] ejemplos = {
                "Ejemplo 1: Simple (4 cursos)",
                "Ejemplo 2: Conflicto inevitable (3 cursos)",
                "Ejemplo 3: Complejo (6 cursos)",
                "-- Datos personalizados --"
        };
        selectorEjemplos = new JComboBox<>(ejemplos);
        selectorEjemplos.setFont(FUENTE_NORMAL);
        selectorEjemplos.setPreferredSize(new Dimension(280, 30));

        selectorFranjas = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        selectorFranjas.setFont(FUENTE_NORMAL);
        ((JSpinner.DefaultEditor) selectorFranjas.getEditor()).getTextField().setColumns(3);

        botonDatosPersonalizados = crearBoton("Editar Datos", new Color(46, 204, 113));
        botonDatosPersonalizados.setPreferredSize(new Dimension(120, 35));

        botonResolver = crearBoton("Asignar Horarios", COLOR_PRIMARIO);

        botonExportar = crearBoton("Exportar", new Color(155, 89, 182));
        botonExportar.setEnabled(false);

        panelControles.add(crearEtiqueta("Datos:"));
        panelControles.add(selectorEjemplos);
        panelControles.add(botonDatosPersonalizados);
        panelControles.add(Box.createHorizontalStrut(10));
        panelControles.add(crearEtiqueta("Franjas:"));
        panelControles.add(selectorFranjas);
        panelControles.add(Box.createHorizontalStrut(10));
        panelControles.add(botonResolver);
        panelControles.add(botonExportar);

        return panelControles;
    }

    /**
     * Crea el panel de leyenda de colores.
     */
    private JPanel crearPanelLeyenda() {
        panelLeyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panelLeyenda.setBackground(COLOR_PANEL);
        panelLeyenda.setBorder(crearBordeTitulado("Leyenda de Franjas"));
        panelLeyenda.setPreferredSize(new Dimension(350, 60));
        actualizarLeyenda();
        return panelLeyenda;
    }

    /**
     * Actualiza la leyenda según el número de franjas seleccionado.
     */
    private void actualizarLeyenda() {
        panelLeyenda.removeAll();
        int numFranjas = obtenerNumeroFranjas();

        for (int i = 0; i < numFranjas; i++) {
            JPanel itemLeyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
            itemLeyenda.setOpaque(false);

            JPanel cuadroColor = new JPanel();
            cuadroColor.setPreferredSize(new Dimension(16, 16));
            cuadroColor.setBackground(coloresFranja[i % coloresFranja.length]);
            cuadroColor.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

            JLabel etiqueta = new JLabel("F" + (i + 1));
            etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 11));

            itemLeyenda.add(cuadroColor);
            itemLeyenda.add(etiqueta);
            panelLeyenda.add(itemLeyenda);
        }

        panelLeyenda.revalidate();
        panelLeyenda.repaint();
    }

    /**
     * Crea el panel dividido con grafo y resultados.
     */
    private JSplitPane crearPanelDeResultados() {
        panelGrafo = new PanelGrafo();
        panelGrafo.setBackground(COLOR_PANEL);

        areaResultado = new JTextArea();
        areaResultado.setEditable(false);
        areaResultado.setFont(FUENTE_MONO);
        areaResultado.setBackground(new Color(253, 254, 255));
        areaResultado.setMargin(new Insets(10, 10, 10, 10));
        areaResultado.setText("Selecciona un ejemplo y presiona 'Asignar Horarios' para comenzar.");

        JScrollPane scrollResultado = new JScrollPane(areaResultado);
        scrollResultado.setBorder(crearBordeTitulado("Resultados y Estadisticas"));
        scrollResultado.setBackground(COLOR_PANEL);

        JSplitPane panelDividido = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, panelGrafo, scrollResultado);
        panelDividido.setResizeWeight(0.55);
        panelDividido.setDividerSize(8);
        panelDividido.setBorder(null);

        return panelDividido;
    }

    /**
     * Crea el panel de progreso inferior.
     */
    private JPanel crearPanelProgreso() {
        JPanel panelProgreso = new JPanel(new BorderLayout(10, 0));
        panelProgreso.setBackground(COLOR_PANEL);
        panelProgreso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                new EmptyBorder(8, 10, 8, 10)));

        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Listo");
        barraProgreso.setFont(FUENTE_NORMAL);

        panelProgreso.add(barraProgreso, BorderLayout.CENTER);
        return panelProgreso;
    }

    public int obtenerNumeroFranjas() {
        return (int) selectorFranjas.getValue();
    }

    public List<Curso> obtenerCursos() {
        return new ArrayList<>(cursos);
    }

    public List<Estudiante> obtenerEstudiantes() {
        return new ArrayList<>(estudiantes);
    }

    /**
     * Carga los datos de un ejemplo predefinido.
     */
    public void cargarDatosDeEjemplo(int indiceEjemplo) {
        cursos.clear();
        estudiantes.clear();
        ultimaSolucion = null;
        botonExportar.setEnabled(false);

        switch (indiceEjemplo) {
            case 0 -> cargarEjemploSimple();
            case 1 -> cargarEjemploConflictoInevitable();
            case 2 -> cargarEjemploComplejo();
        }

        mostrarDatosDelProblema();
        OptimizadorHorarios optimizadorTemp = new OptimizadorHorarios(
                cursos, estudiantes, obtenerNumeroFranjas());
        actualizarGrafo(optimizadorTemp, null);
        actualizarLeyenda();
    }

    private void cargarEjemploSimple() {
        cursos = new ArrayList<>(List.of(
                new Curso("C1", "Calculo"),
                new Curso("C2", "Algebra"),
                new Curso("C3", "Fisica"),
                new Curso("C4", "Programacion")));
        estudiantes.add(new Estudiante("E1", List.of(cursos.get(0), cursos.get(2))));
        estudiantes.add(new Estudiante("E2", List.of(cursos.get(1), cursos.get(3))));
        estudiantes.add(new Estudiante("E3", List.of(cursos.get(0), cursos.get(1))));
        selectorFranjas.setValue(3);
    }

    private void cargarEjemploConflictoInevitable() {
        cursos = new ArrayList<>(List.of(
                new Curso("C1", "IA"),
                new Curso("C2", "Compiladores"),
                new Curso("C3", "Redes")));
        estudiantes.add(new Estudiante("E1", List.of(cursos.get(0), cursos.get(1))));
        estudiantes.add(new Estudiante("E2", List.of(cursos.get(1), cursos.get(2))));
        estudiantes.add(new Estudiante("E3", List.of(cursos.get(0), cursos.get(2))));
        selectorFranjas.setValue(2);
    }

    private void cargarEjemploComplejo() {
        cursos = new ArrayList<>(List.of(
                new Curso("C1", "Matematicas"),
                new Curso("C2", "Literatura"),
                new Curso("C3", "Historia"),
                new Curso("C4", "Quimica"),
                new Curso("C5", "Biologia"),
                new Curso("C6", "Geografia")));
        estudiantes.add(new Estudiante("E1", List.of(cursos.get(0), cursos.get(3))));
        estudiantes.add(new Estudiante("E2", List.of(cursos.get(0), cursos.get(4))));
        estudiantes.add(new Estudiante("E3", List.of(cursos.get(1), cursos.get(2))));
        estudiantes.add(new Estudiante("E4", List.of(cursos.get(3), cursos.get(4), cursos.get(5))));
        estudiantes.add(new Estudiante("E5", List.of(cursos.get(0), cursos.get(5))));
        estudiantes.add(new Estudiante("E6", List.of(cursos.get(2), cursos.get(5))));
        selectorFranjas.setValue(3);
    }

    /**
     * Muestra la solución encontrada en el área de resultados.
     */
    public void mostrarSolucion(OptimizadorHorarios.Solucion solucion, long duracion,
            OptimizadorHorarios optimizador) {
        this.ultimaSolucion = solucion;
        actualizarGrafo(optimizador, solucion.horario());

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("                 RESULTADO DE LA OPTIMIZACIÓN\n");
        sb.append("═══════════════════════════════════════════════════\n\n");

        if (solucion.horario() == null) {
            sb.append("[X] No se encontro una solucion valida.\n");
        } else {
            if (solucion.conflictos() == 0) {
                sb.append("[OK] SOLUCION OPTIMA ENCONTRADA!\n");
                sb.append("     No hay conflictos entre estudiantes.\n\n");
            } else {
                sb.append(String.format("[!] Solucion encontrada con %d conflicto(s)\n\n",
                        solucion.conflictos()));
            }

            sb.append("HORARIO DE EXAMENES:\n");
            sb.append("───────────────────────────────────────────────────\n");

            Map<Integer, List<String>> franjaACursos = new TreeMap<>();
            solucion.horario().forEach((curso, franja) -> franjaACursos.computeIfAbsent(franja, k -> new ArrayList<>())
                    .add(String.format("%s (%s)", curso.id(), curso.nombre())));

            franjaACursos.forEach((franja, cursosEnFranja) -> {
                cursosEnFranja.sort(String::compareTo);
                sb.append(String.format("   Franja %d │ %s\n",
                        franja + 1, String.join(", ", cursosEnFranja)));
            });

            List<String> detallesConflicto = optimizador.obtenerDetallesDeConflictos(solucion.horario());
            if (!detallesConflicto.isEmpty()) {
                sb.append("\nDETALLE DE CONFLICTOS:\n");
                sb.append("───────────────────────────────────────────────────\n");
                detallesConflicto.forEach(detalle -> sb.append("   • ").append(detalle).append("\n"));
            }
        }

        sb.append("\nESTADISTICAS DEL ALGORITMO:\n");
        sb.append("───────────────────────────────────────────────────\n");
        sb.append(String.format("   • Tiempo de ejecucion: %d ms\n", duracion));
        sb.append(String.format("   • Estados explorados:  %,d\n", optimizador.obtenerEstadosExplorados()));
        sb.append(String.format("   • Estados podados:     %,d\n", optimizador.obtenerEstadosPodados()));
        sb.append(String.format("   • Cursos asignados:    %d\n", cursos.size()));
        sb.append(String.format("   • Franjas utilizadas:  %d\n", obtenerNumeroFranjas()));

        sb.append("\n═══════════════════════════════════════════════════\n");

        areaResultado.setText(sb.toString());
        areaResultado.setCaretPosition(0);

        botonExportar.setEnabled(solucion.horario() != null);
    }

    /**
     * Actualiza el grafo visual.
     */
    public void actualizarGrafo(OptimizadorHorarios optimizador, Map<Curso, Integer> horario) {
        panelGrafo.dibujarGrafo(optimizador.getGrafoConflictos().obtenerGrafo());
        panelGrafo.colorearNodos(horario, coloresFranja);
    }

    /**
     * Muestra los datos del problema en el área de texto.
     */
    private void mostrarDatosDelProblema() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("              DATOS DEL PROBLEMA\n");
        sb.append("═══════════════════════════════════════════════════\n\n");

        sb.append(String.format("CURSOS A ASIGNAR (%d):\n", cursos.size()));
        sb.append("───────────────────────────────────────────────────\n");
        cursos.forEach(c -> sb.append(String.format("   • %s: %s\n", c.id(), c.nombre())));

        sb.append(String.format("\nESTUDIANTES E INSCRIPCIONES (%d):\n", estudiantes.size()));
        sb.append("───────────────────────────────────────────────────\n");
        estudiantes.forEach(e -> {
            List<String> ids = e.cursos().stream().map(Curso::id).toList();
            sb.append(String.format("   • %s → %s\n", e.id(), String.join(", ", ids)));
        });

        sb.append("\n═══════════════════════════════════════════════════\n");
        sb.append("   Presiona 'Asignar Horarios' para optimizar\n");
        sb.append("═══════════════════════════════════════════════════\n");

        areaResultado.setText(sb.toString());
        areaResultado.setCaretPosition(0);
    }

    /**
     * Muestra el estado de optimización en progreso.
     */
    public void mostrarProgreso(boolean enProgreso) {
        barraProgreso.setIndeterminate(enProgreso);
        barraProgreso.setString(enProgreso ? "Optimizando..." : "Listo");
        botonResolver.setEnabled(!enProgreso);
        selectorEjemplos.setEnabled(!enProgreso);
        selectorFranjas.setEnabled(!enProgreso);
    }

    /**
     * Exporta los resultados a un archivo de texto.
     */
    private void exportarResultados() {
        if (ultimaSolucion == null || ultimaSolucion.horario() == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay resultados para exportar.",
                    "Exportar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("horario_examenes.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                writer.println(areaResultado.getText());
                JOptionPane.showMessageDialog(this,
                        "Resultados exportados exitosamente.",
                        "Exportar", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al exportar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_NORMAL);
        return etiqueta;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(FUENTE_TITULO);
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(160, 35));
        return boton;
    }

    private TitledBorder crearBordeTitulado(String titulo) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                titulo);
        border.setTitleFont(FUENTE_TITULO);
        border.setTitleColor(new Color(60, 60, 60));
        return border;
    }

    /**
     * Genera colores pastel distinguibles para las franjas horarias.
     */
    private Color[] generarColoresPastel(int cantidad) {
        Color[] colores = new Color[cantidad];
        float[] saturaciones = { 0.5f, 0.6f, 0.55f, 0.65f, 0.5f };
        float[] brillos = { 0.95f, 0.9f, 0.92f, 0.88f, 0.93f };

        for (int i = 0; i < cantidad; i++) {
            float hue = (float) i / cantidad;
            float sat = saturaciones[i % saturaciones.length];
            float bri = brillos[i % brillos.length];
            colores[i] = Color.getHSBColor(hue, sat, bri);
        }
        return colores;
    }
}