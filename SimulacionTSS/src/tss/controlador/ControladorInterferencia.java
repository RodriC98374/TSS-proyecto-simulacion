package tss.controlador;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import tss.modelo.comun.ProgresoSimulacion;
import tss.modelo.interferencia.ResultadoInterferencia;
import tss.modelo.interferencia.SimuladorInterferencia;
import tss.vista.PanelEjercicio;
import tss.vista.paneles.PanelInterferencia;

/**
 * Controlador del ejercicio 4: lanza el experimento de ensambles en segundo
 * plano y atiende tambien la corrida visual de pocos ensambles.
 * Informe: seccion 5.6.
 */
public final class ControladorInterferencia implements PanelEjercicio.EscuchaPanel {

    private final PanelInterferencia panel;
    private final ExportadorResultados exportador;
    private final ControladorPrincipal.LecturaGenerador lector;
    private TareaSimulacion<ResultadoInterferencia> tarea;

    public ControladorInterferencia(PanelInterferencia panel, ExportadorResultados exportador,
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
        int ensambles = panel.ensambles();
        long semilla = panel.semilla();
        SimuladorInterferencia simulador =
                new SimuladorInterferencia(panel.parametros(), semilla);

        panel.habilitarAcciones(false);
        panel.barra().iniciar("Generando " + ensambles + " ensambles...");
        tarea = new TareaSimulacion<>(
                monitor -> simulador.simular(ensambles, monitor),
                panel::mostrarProgreso,
                resultado -> {
                    panel.mostrarResultado(resultado);
                    panel.habilitarAcciones(true);
                    panel.barra().terminar("Experimento terminado con "
                            + resultado.ensambles() + " ensambles.");
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

    private void correrVisual() {
        if (!panel.parametrosValidos()) {
            panel.barra().error("Revise los parámetros marcados en rojo.");
            return;
        }
        int ensambles = panel.ensamblesVisuales();
        SimuladorInterferencia simulador =
                new SimuladorInterferencia(panel.parametros(), panel.semilla());
        ResultadoInterferencia resultado =
                simulador.simular(ensambles, ProgresoSimulacion.SILENCIOSO);
        panel.mostrarCorridaVisual(resultado);
        panel.mostrarResultado(resultado);
        panel.barra().terminar("Corrida visual de " + ensambles + " ensambles.");
        lector.mostrar(panel.semilla(), simulador.uniformesConsumidos());
    }

    private void exportar() {
        List<Path> escritos = new ArrayList<>();
        try {
            if (panel.tabla().tieneDatos()) {
                escritos.add(exportador.exportarTabla(panel.claveArchivo(), panel.tabla()));
            }
            if (panel.convergencia().tieneDatos()) {
                escritos.add(exportador.exportarGrafica(panel.claveArchivo(),
                        "convergencia", panel.convergencia()));
                escritos.add(exportador.exportarGrafica(panel.claveArchivo(),
                        "holgura", panel.holgura()));
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
}
