package com.example.examscheduler;

import com.example.examscheduler.controlador.ControladorHorarios;
import com.example.examscheduler.vista.VistaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class AplicacionPrincipal {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ControladorHorarios controlador = new ControladorHorarios();
            VistaPrincipal vista = new VistaPrincipal(controlador);
            controlador.setVista(vista);
            controlador.iniciar();
        });
    }
}
