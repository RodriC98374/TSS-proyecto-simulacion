package controller;

import model.Parametros;
import model.ResultadoTrimestre;
import model.SimuladorMotor;
import view.VentanaPrincipal;

import javax.swing.*;
import java.util.List;

public class ControladorSimulacion {

    private final VentanaPrincipal vista;
    private final SimuladorMotor motor = new SimuladorMotor();

    public ControladorSimulacion(VentanaPrincipal vista) {
        this.vista = vista;
        registrarListeners();
    }

    private void registrarListeners() {
        vista.getPanelParametros().addSimularListener(e -> ejecutarSimulacion());
        vista.getPanelParametros().addRestaurarListener(e -> {
            vista.getPanelParametros().restaurarDefaults();
            vista.setStatus("Parametros restaurados a valores por defecto.");
        });
    }

    private void ejecutarSimulacion() {
        Parametros params;
        try {
            params = vista.getPanelParametros().leerParametros();
        } catch (IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
            return;
        }

        vista.setSimulando(true);
        vista.setStatus("Simulando " + params.trimestres + " trimestres con "
                + params.vehiculosPorTrimestre + " vehiculos cada uno...");

        Parametros finalParams = params;
        SwingWorker<List<ResultadoTrimestre>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ResultadoTrimestre> doInBackground() {
                return motor.simular(finalParams);
            }

            @Override
            protected void done() {
                try {
                    List<ResultadoTrimestre> resultados = get();
                    vista.mostrarResultados(resultados);

                    int totalVeh = resultados.stream().mapToInt(t -> t.getTotalVehiculos()).sum();
                    int totalMig = resultados.stream().mapToInt(t -> t.getMigrantes()).sum();
                    double pct = totalVeh == 0 ? 0 : totalMig * 100.0 / totalVeh;

                    vista.setStatus(String.format(
                        "Simulacion completada. %d trimestres | %d vehiculos totales | %d migrantes (%.1f%%).",
                        resultados.size(), totalVeh, totalMig, pct));

                } catch (Exception ex) {
                    vista.mostrarError("Error durante la simulacion: " + ex.getMessage());
                    vista.setStatus("Error en la simulacion.");
                } finally {
                    vista.setSimulando(false);
                }
            }
        };

        worker.execute();
    }
}
