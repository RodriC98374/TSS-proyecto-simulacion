package tss.controlador;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import tss.modelo.aleatorio.FuenteSecuenciaFija;
import tss.modelo.aleatorio.GeneradorCongruencialMixto;
import tss.modelo.comun.ProgresoSimulacion;
import tss.modelo.rechazo.ResultadoRechazo;
import tss.modelo.rechazo.SimuladorRechazo;
import tss.vista.PanelEjercicio;
import tss.vista.paneles.PanelRechazo;

/**
 * Controlador de los ejercicios 1 y 2: valida los parametros, lanza el metodo
 * de rechazo en segundo plano y entrega el resultado al panel.
 * Informe: secciones 5.1 y 5.2.
 */
public final class ControladorRechazo implements PanelEjercicio.EscuchaPanel {

    private final PanelRechazo panel;
    private final ExportadorResultados exportador;
    private final ControladorPrincipal.LecturaGenerador lector;
    private TareaSimulacion<ResultadoRechazo> tarea;

    public ControladorRechazo(PanelRechazo panel, ExportadorResultados exportador,
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
            case PanelEjercicio.ACCION_EXPERIMENTO -> correr();
            case PanelEjercicio.ACCION_VISUAL -> correrPruebaDeEscritorio();
            case PanelEjercicio.ACCION_EXPORTAR -> exportar();
            default -> panel.limpiar();
        }
    }

    private void correr() {
        if (!panel.parametrosValidos()) {
            panel.barra().error("Revise los parámetros marcados en rojo.");
            return;
        }
        int iteraciones = panel.iteraciones();
        GeneradorCongruencialMixto fuente = new GeneradorCongruencialMixto(panel.semilla());
        SimuladorRechazo simulador = new SimuladorRechazo(panel.densidad(), fuente);

        panel.habilitarAcciones(false);
        panel.barra().iniciar("Generando " + iteraciones + " iteraciones...");
        tarea = new TareaSimulacion<>(
                monitor -> simulador.simular(iteraciones, monitor),
                panel::mostrarProgreso,
                resultado -> {
                    panel.mostrarResultado(resultado);
                    panel.habilitarAcciones(true);
                    panel.barra().terminar("Corrida terminada con " + iteraciones
                            + " iteraciones.");
                    lector.mostrar(fuente.semillaInicial(), fuente.generados());
                },
                error -> {
                    panel.habilitarAcciones(true);
                    panel.barra().error("La corrida falló: " + error.getMessage());
                },
                () -> {
                    panel.habilitarAcciones(true);
                    panel.barra().terminar("Corrida cancelada.");
                });
        tarea.execute();
    }

    /** Reproduce las cinco iteraciones tabuladas en el informe. */
    private void correrPruebaDeEscritorio() {
        double[] secuencia = panel.secuenciaPrueba();
        FuenteSecuenciaFija fuente = new FuenteSecuenciaFija(secuencia);
        SimuladorRechazo simulador = new SimuladorRechazo(panel.densidad(), fuente);
        ResultadoRechazo resultado = simulador.simular(secuencia.length / 2,
                ProgresoSimulacion.SILENCIOSO);
        panel.mostrarResultado(resultado);
        panel.barra().terminar("Prueba de escritorio del informe, con los R fijos de la tabla.");
        lector.mostrar(0L, fuente.generados());
    }

    private void exportar() {
        List<Path> escritos = new ArrayList<>();
        try {
            if (panel.tabla().tieneDatos()) {
                escritos.add(exportador.exportarTabla(panel.claveArchivo(), panel.tabla()));
            }
            if (panel.graficaDensidad().tieneDatos()) {
                escritos.add(exportador.exportarGrafica(panel.claveArchivo(),
                        "grafica", panel.graficaDensidad()));
                escritos.add(exportador.exportarGrafica(panel.claveArchivo(),
                        "histograma", panel.histograma()));
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
