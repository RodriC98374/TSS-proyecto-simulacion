package tss.controlador;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import tss.modelo.renta.ResultadoAnioRenta;
import tss.modelo.renta.ResumenFlota;
import tss.modelo.renta.SimuladorRenta;
import tss.vista.PanelEjercicio;
import tss.vista.paneles.PanelRenta;

/**
 * Controlador del ejercicio 3: lanza el barrido de tamanos de flota en segundo
 * plano y atiende la corrida visual de un solo ano.
 * Informe: seccion 5.5.
 */
public final class ControladorRenta implements PanelEjercicio.EscuchaPanel {

    private final PanelRenta panel;
    private final ExportadorResultados exportador;
    private final ControladorPrincipal.LecturaGenerador lector;
    private TareaSimulacion<List<ResumenFlota>> tarea;

    public ControladorRenta(PanelRenta panel, ExportadorResultados exportador,
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
        int minimo = panel.flotaMinima();
        int maximo = panel.flotaMaxima();
        int corridas = panel.corridas();
        long semilla = panel.semilla();
        SimuladorRenta simulador = new SimuladorRenta(panel.parametrosModelo(), semilla);

        panel.habilitarAcciones(false);
        panel.barra().iniciar("Barriendo flotas de " + minimo + " a " + maximo + " autos...");
        tarea = new TareaSimulacion<>(
                monitor -> simulador.evaluarFlotas(minimo, maximo, corridas, monitor),
                panel::mostrarProgreso,
                resumenes -> {
                    ResumenFlota mejor = SimuladorRenta.mejorFlota(resumenes);
                    panel.mostrarResumen(resumenes, mejor);
                    panel.habilitarAcciones(true);
                    panel.barra().terminar(mejor == null ? "Sin resultados."
                            : "La flota óptima es de " + mejor.n() + " autos.");
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
        SimuladorRenta simulador = new SimuladorRenta(panel.parametrosModelo(), panel.semilla());
        ResultadoAnioRenta resultado = simulador.corridaVisual(panel.flotaVisual());
        panel.mostrarCorridaVisual(resultado);
        panel.barra().terminar("Corrida visual de un año con flota de "
                + resultado.flota() + " autos.");
        lector.mostrar(panel.semilla(), simulador.uniformesConsumidos());
    }

    private void exportar() {
        List<Path> escritos = new ArrayList<>();
        try {
            if (panel.tabla().tieneDatos()) {
                escritos.add(exportador.exportarTabla(panel.claveArchivo(), panel.tabla()));
            }
            if (panel.grafica().tieneDatos()) {
                escritos.add(exportador.exportarGrafica(panel.claveArchivo(),
                        "grafica", panel.grafica()));
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
