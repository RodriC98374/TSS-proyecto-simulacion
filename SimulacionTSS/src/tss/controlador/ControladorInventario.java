package tss.controlador;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import tss.modelo.comun.ProgresoSimulacion;
import tss.modelo.inventario.PoliticaInventario;
import tss.modelo.inventario.ResultadoCorridaInventario;
import tss.modelo.inventario.ResumenPolitica;
import tss.modelo.inventario.SimuladorInventario;
import tss.vista.PanelEjercicio;
import tss.vista.paneles.PanelInventario;

/**
 * Controlador del ejercicio 5: corre una politica o las dos y entrega la
 * comparacion al panel, ademas de la corrida visual de pocos dias.
 * Informe: seccion 5.7.
 */
public final class ControladorInventario implements PanelEjercicio.EscuchaPanel {

    private final PanelInventario panel;
    private final ExportadorResultados exportador;
    private final ControladorPrincipal.LecturaGenerador lector;
    private TareaSimulacion<Comparacion> tarea;

    public ControladorInventario(PanelInventario panel, ExportadorResultados exportador,
            ControladorPrincipal.LecturaGenerador lector) {
        this.panel = panel;
        this.exportador = exportador;
        this.lector = lector;
        panel.setEscucha(this);
        panel.barra().alCancelar(this::cancelar);
    }

    @Override
    public void alPedirAccion(String accion) {
        switch (accion) {
            case PanelEjercicio.ACCION_EXPERIMENTO -> correrExperimento();
            case PanelEjercicio.ACCION_VISUAL -> correrVisual();
            case PanelEjercicio.ACCION_EXPORTAR -> exportar();
            default -> panel.limpiar();
        }
    }

    private void correrExperimento() {
        if (!panel.parametrosValidos()) {
            panel.barra().error("Revise los parámetros marcados en rojo.");
            return;
        }
        PoliticaInventario elegida = panel.politicaElegida();
        int corridas = panel.corridas();
        long semilla = panel.semilla();
        SimuladorInventario simulador =
                new SimuladorInventario(panel.parametrosModelo(), semilla);

        panel.habilitarAcciones(false);
        panel.barra().iniciar(elegida == null
                ? "Comparando las dos políticas con " + corridas + " corridas..."
                : "Corriendo " + elegida.nombreCorto() + " con " + corridas + " corridas...");
        tarea = new TareaSimulacion<>(
                monitor -> evaluar(simulador, elegida, corridas, monitor),
                panel::mostrarProgreso,
                comparacion -> {
                    panel.mostrarComparacion(comparacion.uno(), comparacion.dos());
                    panel.habilitarAcciones(true);
                    panel.barra().terminar(comparacion.mensaje());
                    lector.mostrar(semilla, simulador.uniformesConsumidos());
                },
                error -> {
                    panel.habilitarAcciones(true);
                    panel.barra().error("El experimento falló: " + error.getMessage());
                },
                () -> {
                    panel.habilitarAcciones(true);
                    panel.barra().terminar("Experimento cancelado.");
                });
        tarea.execute();
    }

    private static Comparacion evaluar(SimuladorInventario simulador, PoliticaInventario elegida,
            int corridas, ProgresoSimulacion.Monitor monitor) {
        ResumenPolitica uno = null;
        ResumenPolitica dos = null;
        if (elegida == null || elegida == PoliticaInventario.PERIODICA) {
            uno = simulador.evaluarPolitica(PoliticaInventario.PERIODICA, corridas, monitor);
        }
        if (elegida == null || elegida == PoliticaInventario.PUNTO_REORDEN) {
            dos = simulador.evaluarPolitica(PoliticaInventario.PUNTO_REORDEN, corridas, monitor);
        }
        return new Comparacion(uno, dos);
    }

    private void correrVisual() {
        if (!panel.parametrosValidos()) {
            panel.barra().error("Revise los parámetros marcados en rojo.");
            return;
        }
        PoliticaInventario politica = panel.politicaVisual();
        SimuladorInventario simulador =
                new SimuladorInventario(panel.parametrosModelo(), panel.semilla());
        ResultadoCorridaInventario resultado = simulador.corridaVisual(politica);
        panel.mostrarCorridaVisual(resultado);
        panel.barra().terminar("Corrida visual de " + politica.nombreCorto()
                + ", primeros " + panel.diasVisibles() + " días.");
        lector.mostrar(panel.semilla(), simulador.uniformesConsumidos());
    }

    private void exportar() {
        List<Path> escritos = new ArrayList<>();
        try {
            if (panel.tabla().tieneDatos()) {
                escritos.add(exportador.exportarTabla(panel.claveArchivo(), panel.tabla()));
            }
            if (panel.barras().tieneDatos()) {
                escritos.add(exportador.exportarGrafica(panel.claveArchivo(),
                        "costos", panel.barras()));
            }
            if (panel.evolucion().tieneDatos()) {
                escritos.add(exportador.exportarGrafica(panel.claveArchivo(),
                        "inventario", panel.evolucion()));
            }
            panel.barra().avisoTemporal(ExportadorResultados.resumenDeArchivos(escritos));
        } catch (Exception e) {
            panel.barra().error("No se pudo exportar: " + e.getMessage());
        }
    }

    private void cancelar() {
        if (tarea != null && !tarea.isDone()) {
            tarea.cancel(true);
        }
    }

    private record Comparacion(ResumenPolitica uno, ResumenPolitica dos) {

        private String mensaje() {
            if (uno == null) {
                return "Política 2 corrida por separado.";
            }
            if (dos == null) {
                return "Política 1 corrida por separado.";
            }
            return uno.costoTotal() <= dos.costoTotal()
                    ? "La política 1 resulta más económica."
                    : "La política 2 resulta más económica.";
        }
    }
}
