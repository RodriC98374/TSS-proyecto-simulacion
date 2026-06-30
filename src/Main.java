import controller.ControladorSimulacion;
import view.VentanaPrincipal;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use system look-and-feel for native feel on Windows
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal vista = new VentanaPrincipal();
            new ControladorSimulacion(vista);
            vista.setVisible(true);
        });
    }
}
