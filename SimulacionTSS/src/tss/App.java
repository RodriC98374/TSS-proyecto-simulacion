package tss;

import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import tss.controlador.ControladorPrincipal;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Tipografia;

/**
 * Punto de entrada de la aplicacion de la Actividad 1. Ajusta unos pocos
 * valores globales de Swing y entrega el control al ControladorPrincipal.
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        ajustarValoresGlobales();
        SwingUtilities.invokeLater(() -> new ControladorPrincipal().iniciar());
    }

    private static void ajustarValoresGlobales() {
        UIManager.put("Panel.background", Paleta.FONDO);
        UIManager.put("Viewport.background", Paleta.SUPERFICIE);
        UIManager.put("ScrollPane.background", Paleta.SUPERFICIE);
        UIManager.put("SplitPane.background", Paleta.FONDO);
        UIManager.put("SplitPaneDivider.draggingColor", Paleta.AMBAR);
        UIManager.put("ToolTip.background", Paleta.SUPERFICIE_ALTA);
        UIManager.put("ToolTip.foreground", Paleta.TINTA);
        UIManager.put("ToolTip.font", Tipografia.interfaz(Tipografia.PEQUENA));
        ToolTipManager.sharedInstance().setInitialDelay(400);
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
    }
}
