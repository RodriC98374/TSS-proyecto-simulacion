package tss.controlador;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import tss.modelo.comun.ProgresoSimulacion;
import tss.modelo.reemplazo.PoliticaReemplazo;
import tss.modelo.reemplazo.ResultadoCorridaReemplazo;
import tss.modelo.reemplazo.ResumenReemplazo;
import tss.modelo.reemplazo.SimuladorReemplazo;
import tss.vista.PanelEjercicio;
import tss.vista.paneles.PanelReemplazo;

/**
 * Controlador del ejercicio 6: corre una politica de reemplazo o las dos y
 * entrega la comparacion, ademas de la traza de los primeros eventos.
 * Informe: seccion 5.8.
 */
public final class ControladorReemplazo implements PanelEjercicio.EscuchaPanel {

    private final PanelReemplazo panel;
    private final ExportadorResultados exportador;
    private final ControladorPrincipal.LecturaGenerador lector;
    private TareaSimulacion<Comparacion> tarea;

    public ControladorReemplazo(PanelReemplazo panel, ExportadorResultados exportador,
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
        PoliticaReemplazo elegida = panel.politicaElegida();
        int corridas = panel.corridas();
        long semilla = panel.semilla();
        SimuladorReemplazo simulador = new SimuladorReemplazo(panel.parametrosModelo(), semilla);

        panel.habilitarAcciones(false);
        panel.barra().iniciar(elegida == null
                ? "Comparando las dos políticas con " + corridas + " corridas..."
                : "Corriendo " + elegida.nombreCorto() + " con " + corridas + " corridas...");
        tarea = new TareaSimulacion<>(
                monitor -> evaluar(simulador, elegida, corridas, monitor),
                panel::mostrarProgreso,
                comparacion -> {
                    panel.mostrarComparacion(comparacion.a(), comparacion.b());
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

    private static Comparacion evaluar(SimuladorReemplazo simulador, PoliticaReemplazo elegida,
            int corridas, ProgresoSimulacion.Monitor monitor) {
        ResumenReemplazo a = null;
        ResumenReemplazo b = null;
        if (elegida == null || elegida == PoliticaReemplazo.INDIVIDUAL) {
            a = simulador.evaluarPolitica(PoliticaReemplazo.INDIVIDUAL, corridas, monitor);
        }
        if (elegida == null || elegida == PoliticaReemplazo.TOTAL) {
            b = simulador.evaluarPolitica(PoliticaReemplazo.TOTAL, corridas, monitor);
        }
        return new Comparacion(a, b);
    }

    private void correrVisual() {
        if (!panel.parametrosValidos()) {
            panel.barra().error("Revise los parámetros marcados en rojo.");
            return;
        }
        SimuladorReemplazo simulador =
                new SimuladorReemplazo(panel.parametrosModelo(), panel.semilla());
        ResultadoCorridaReemplazo individual =
                simulador.corridaVisual(PoliticaReemplazo.INDIVIDUAL);
        ResultadoCorridaReemplazo total = simulador.corridaVisual(PoliticaReemplazo.TOTAL);
        PoliticaReemplazo elegida = panel.politicaVisual();

        panel.mostrarCorridaVisual(
                elegida == PoliticaReemplazo.INDIVIDUAL ? individual : total);
        panel.mostrarRitmo(individual, total);
        panel.barra().terminar("Corrida visual de " + elegida.nombreCorto()
                + ", primeros " + panel.eventosVisibles() + " eventos.");
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
            if (panel.ritmo().tieneDatos()) {
                escritos.add(exportador.exportarGrafica(panel.claveArchivo(),
                        "ritmo", panel.ritmo()));
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

    private record Comparacion(ResumenReemplazo a, ResumenReemplazo b) {

        private String mensaje() {
            if (a == null) {
                return "Política B corrida por separado.";
            }
            if (b == null) {
                return "Política A corrida por separado.";
            }
            return a.costoTotal() <= b.costoTotal()
                    ? "La política A resulta más económica."
                    : "La política B resulta más económica.";
        }
    }
}
