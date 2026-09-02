package tss.controlador;

import tss.modelo.rechazo.DensidadPorTramos;
import tss.vista.PanelEjercicio;
import tss.vista.VentanaPrincipal;
import tss.vista.paneles.PanelInterferencia;
import tss.vista.paneles.PanelInventario;
import tss.vista.paneles.PanelRechazo;
import tss.vista.paneles.PanelRenta;
import tss.vista.paneles.PanelReemplazo;

/**
 * Arma la ventana, crea los seis paneles con sus controladores y conecta el
 * rail de navegacion con el CardLayout. Es el unico punto donde se conocen
 * todas las piezas a la vez.
 */
public final class ControladorPrincipal {

    /** Permite a cada controlador refrescar el lector del generador del rail. */
    public interface LecturaGenerador {
        void mostrar(long semilla, long consumidos);
    }

    private static final String[] TITULOS = {
            "Ejercicio 1", "Ejercicio 2", "Ejercicio 3",
            "Ejercicio 4", "Ejercicio 5", "Ejercicio 6" };

    private static final String[] NOMBRES = {
            "Rechazo I", "Rechazo II", "Renta de autos",
            "Interferencia", "Inventario", "Reemplazo" };

    private static final double[] SECUENCIA_UNO = {
            0.60, 0.50, 0.30, 0.50, 0.75, 0.70, 0.55, 0.60, 0.85, 0.20 };

    private static final double[] SECUENCIA_DOS = {
            0.60, 0.50, 0.90, 0.30, 0.78, 0.70, 0.66, 0.90, 0.72, 0.50 };

    private final ExportadorResultados exportador = new ExportadorResultados();
    private VentanaPrincipal ventana;

    public void iniciar() {
        ventana = new VentanaPrincipal(TITULOS, NOMBRES);
        LecturaGenerador lector = (semilla, consumidos) ->
                ventana.rail().actualizarGenerador(semilla, consumidos);

        PanelRechazo uno = new PanelRechazo(
                "Ejercicio 1 — Método de rechazo, densidad escalonada",
                "¿Cómo generar valores de una densidad escalonada sin invertir su acumulada?",
                "ejercicio1", DensidadPorTramos.escalonada(), SECUENCIA_UNO);
        PanelRechazo dos = new PanelRechazo(
                "Ejercicio 2 — Método de rechazo, escalón y rampa",
                "¿Cómo generar valores de una densidad con un escalón y una rampa descendente?",
                "ejercicio2", DensidadPorTramos.escalonConRampa(), SECUENCIA_DOS);
        PanelRenta renta = new PanelRenta();
        PanelInterferencia interferencia = new PanelInterferencia();
        PanelInventario inventario = new PanelInventario();
        PanelReemplazo reemplazo = new PanelReemplazo();

        new ControladorRechazo(uno, exportador, lector);
        new ControladorRechazo(dos, exportador, lector);
        new ControladorRenta(renta, exportador, lector);
        new ControladorInterferencia(interferencia, exportador, lector);
        new ControladorInventario(inventario, exportador, lector);
        new ControladorReemplazo(reemplazo, exportador, lector);

        PanelEjercicio[] paneles = { uno, dos, renta, interferencia, inventario, reemplazo };
        for (int i = 0; i < paneles.length; i++) {
            ventana.agregarPanel(String.valueOf(i), paneles[i]);
        }
        ventana.rail().alSeleccionar(indice -> ventana.mostrarPanel(String.valueOf(indice)));
        ventana.rail().seleccionar(0);
        ventana.rail().actualizarGenerador(12345L, 0L);
        ventana.setVisible(true);
    }
}
