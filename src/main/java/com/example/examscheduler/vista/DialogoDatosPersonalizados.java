package com.example.examscheduler.vista;

import com.example.examscheduler.modelo.Curso;
import com.example.examscheduler.modelo.Estudiante;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo para que el usuario ingrese sus propios datos de cursos y
 * estudiantes.
 */
public class DialogoDatosPersonalizados extends JDialog {

    private final DefaultTableModel modeloCursos;
    private final DefaultTableModel modeloEstudiantes;
    private JTable tablaCursos;
    private JTable tablaEstudiantes;
    private JTextField campoCursoId;
    private JTextField campoCursoNombre;
    private JTextField campoEstudianteId;
    private JTextField campoEstudianteCursos;

    private List<Curso> cursosResultado = null;
    private List<Estudiante> estudiantesResultado = null;
    private boolean confirmado = false;

    public DialogoDatosPersonalizados(Frame parent) {
        super(parent, "Ingresar Datos Personalizados", true);

        modeloCursos = new DefaultTableModel(new String[] { "ID", "Nombre" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        modeloEstudiantes = new DefaultTableModel(new String[] { "ID", "Cursos Inscritos" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        construirInterfaz();
        pack();
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(parent);
    }

    private void construirInterfaz() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        splitPane.setLeftComponent(crearPanelCursos());
        splitPane.setRightComponent(crearPanelEstudiantes());

        panelPrincipal.add(splitPane, BorderLayout.CENTER);
        panelPrincipal.add(crearPanelBotones(), BorderLayout.SOUTH);
        panelPrincipal.add(crearPanelInstrucciones(), BorderLayout.NORTH);

        setContentPane(panelPrincipal);
    }

    private JPanel crearPanelInstrucciones() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JTextArea instrucciones = new JTextArea(
                "Instrucciones:\n" +
                        "1. Primero agrega los cursos (ej: C1, Calculo)\n" +
                        "2. Luego agrega estudiantes con los IDs de los cursos que toman separados por coma\n" +
                        "   (ej: E1 toma C1,C2 significa que el estudiante E1 esta inscrito en C1 y C2)");
        instrucciones.setEditable(false);
        instrucciones.setBackground(new Color(255, 255, 220));
        instrucciones.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instrucciones.setBorder(new EmptyBorder(8, 8, 8, 8));

        panel.add(instrucciones, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelCursos() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Cursos"));

        tablaCursos = new JTable(modeloCursos);
        tablaCursos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollCursos = new JScrollPane(tablaCursos);
        scrollCursos.setPreferredSize(new Dimension(300, 200));

        JPanel panelEntrada = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelEntrada.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        campoCursoId = new JTextField(8);
        panelEntrada.add(campoCursoId, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panelEntrada.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        campoCursoNombre = new JTextField(15);
        panelEntrada.add(campoCursoNombre, gbc);

        JPanel panelBotonesCursos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregarCurso = new JButton("Agregar");
        JButton btnEliminarCurso = new JButton("Eliminar");

        btnAgregarCurso.addActionListener(e -> agregarCurso());
        btnEliminarCurso.addActionListener(e -> eliminarCurso());

        panelBotonesCursos.add(btnAgregarCurso);
        panelBotonesCursos.add(btnEliminarCurso);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelEntrada, BorderLayout.CENTER);
        panelInferior.add(panelBotonesCursos, BorderLayout.SOUTH);

        panel.add(scrollCursos, BorderLayout.CENTER);
        panel.add(panelInferior, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelEstudiantes() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Estudiantes"));

        tablaEstudiantes = new JTable(modeloEstudiantes);
        tablaEstudiantes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollEstudiantes = new JScrollPane(tablaEstudiantes);
        scrollEstudiantes.setPreferredSize(new Dimension(300, 200));

        JPanel panelEntrada = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelEntrada.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        campoEstudianteId = new JTextField(8);
        panelEntrada.add(campoEstudianteId, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panelEntrada.add(new JLabel("Cursos (ej: C1,C2):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        campoEstudianteCursos = new JTextField(15);
        panelEntrada.add(campoEstudianteCursos, gbc);

        JPanel panelBotonesEstudiantes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregarEstudiante = new JButton("Agregar");
        JButton btnEliminarEstudiante = new JButton("Eliminar");

        btnAgregarEstudiante.addActionListener(e -> agregarEstudiante());
        btnEliminarEstudiante.addActionListener(e -> eliminarEstudiante());

        panelBotonesEstudiantes.add(btnAgregarEstudiante);
        panelBotonesEstudiantes.add(btnEliminarEstudiante);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelEntrada, BorderLayout.CENTER);
        panelInferior.add(panelBotonesEstudiantes, BorderLayout.SOUTH);

        panel.add(scrollEstudiantes, BorderLayout.CENTER);
        panel.add(panelInferior, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnConfirmar = new JButton("Usar estos datos");
        JButton btnCancelar = new JButton("Cancelar");

        btnConfirmar.addActionListener(e -> confirmar());
        btnCancelar.addActionListener(e -> dispose());

        panel.add(btnCancelar);
        panel.add(btnConfirmar);

        return panel;
    }

    private void agregarCurso() {
        String id = campoCursoId.getText().trim().toUpperCase();
        String nombre = campoCursoNombre.getText().trim();

        if (id.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debes ingresar ID y nombre del curso.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (int i = 0; i < modeloCursos.getRowCount(); i++) {
            if (modeloCursos.getValueAt(i, 0).equals(id)) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe un curso con ese ID.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        modeloCursos.addRow(new Object[] { id, nombre });
        campoCursoId.setText("");
        campoCursoNombre.setText("");
        campoCursoId.requestFocus();
    }

    private void eliminarCurso() {
        int fila = tablaCursos.getSelectedRow();
        if (fila >= 0) {
            modeloCursos.removeRow(fila);
        }
    }

    private void agregarEstudiante() {
        String id = campoEstudianteId.getText().trim().toUpperCase();
        String cursosTexto = campoEstudianteCursos.getText().trim().toUpperCase();

        if (id.isEmpty() || cursosTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debes ingresar ID y cursos del estudiante.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (int i = 0; i < modeloEstudiantes.getRowCount(); i++) {
            if (modeloEstudiantes.getValueAt(i, 0).equals(id)) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe un estudiante con ese ID.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String[] cursosIds = cursosTexto.split(",");
        for (String cursoId : cursosIds) {
            cursoId = cursoId.trim();
            boolean encontrado = false;
            for (int i = 0; i < modeloCursos.getRowCount(); i++) {
                if (modeloCursos.getValueAt(i, 0).equals(cursoId)) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                JOptionPane.showMessageDialog(this,
                        "El curso '" + cursoId + "' no existe. Agregalo primero.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        modeloEstudiantes.addRow(new Object[] { id, cursosTexto });
        campoEstudianteId.setText("");
        campoEstudianteCursos.setText("");
        campoEstudianteId.requestFocus();
    }

    private void eliminarEstudiante() {
        int fila = tablaEstudiantes.getSelectedRow();
        if (fila >= 0) {
            modeloEstudiantes.removeRow(fila);
        }
    }

    private void confirmar() {
        if (modeloCursos.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Debes agregar al menos un curso.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (modeloEstudiantes.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Debes agregar al menos un estudiante.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        cursosResultado = new ArrayList<>();
        for (int i = 0; i < modeloCursos.getRowCount(); i++) {
            String id = (String) modeloCursos.getValueAt(i, 0);
            String nombre = (String) modeloCursos.getValueAt(i, 1);
            cursosResultado.add(new Curso(id, nombre));
        }

        estudiantesResultado = new ArrayList<>();
        for (int i = 0; i < modeloEstudiantes.getRowCount(); i++) {
            String id = (String) modeloEstudiantes.getValueAt(i, 0);
            String cursosTexto = (String) modeloEstudiantes.getValueAt(i, 1);

            List<Curso> cursosEstudiante = new ArrayList<>();
            String[] cursosIds = cursosTexto.split(",");
            for (String cursoId : cursosIds) {
                cursoId = cursoId.trim();
                for (Curso curso : cursosResultado) {
                    if (curso.id().equals(cursoId)) {
                        cursosEstudiante.add(curso);
                        break;
                    }
                }
            }

            estudiantesResultado.add(new Estudiante(id, cursosEstudiante));
        }

        confirmado = true;
        dispose();
    }

    public boolean fueConfirmado() {
        return confirmado;
    }

    public List<Curso> getCursos() {
        return cursosResultado;
    }

    public List<Estudiante> getEstudiantes() {
        return estudiantesResultado;
    }
}
