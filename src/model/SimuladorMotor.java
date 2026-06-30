package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimuladorMotor {

    public List<ResultadoTrimestre> simular(Parametros p) {
        List<ResultadoTrimestre> resultados = new ArrayList<>();
        double[] precios = p.calcularPrecios();
        Random rng = new Random(p.semilla);

        for (int t = 0; t < p.trimestres; t++) {
            double precioT = precios[t];
            ResultadoTrimestre trimestre = new ResultadoTrimestre();
            trimestre.numeroTrimestre = t + 1;
            trimestre.precioSubvencionado = precioT;

            // ocupacion[s] = minuto en que el surtidor s quedara libre
            double[] ocupacion = new double[p.numeroDeSurtidores];
            double reloj = 0.0;

            for (int v = 0; v < p.vehiculosPorTrimestre; v++) {
                // Generar tiempo entre arribos: transformada inversa exponencial
                double r1 = rng.nextDouble();
                double tEntreArribos = -(1.0 / p.tasaArribo) * Math.log(1.0 - r1);
                reloj += tEntreArribos;

                // Perfil y volumen
                PerfilVehiculo perfil = rng.nextDouble() < p.proporcionPesado
                        ? PerfilVehiculo.TRANSPORTE_PESADO
                        : PerfilVehiculo.VEHICULO_PARTICULAR;

                double volMin = (perfil == PerfilVehiculo.TRANSPORTE_PESADO)
                        ? p.volPesadoMin : p.volParticularMin;
                double volMax = (perfil == PerfilVehiculo.TRANSPORTE_PESADO)
                        ? p.volPesadoMax : p.volParticularMax;
                double volumen = volMin + rng.nextDouble() * (volMax - volMin);

                double costoOpt = (perfil == PerfilVehiculo.TRANSPORTE_PESADO)
                        ? p.costoOptPesado : p.costoOptParticular;
                double costoOptMinuto = costoOpt / 60.0;

                // Elegir mejor surtidor subvencionado (menor ocupacion)
                int mejorIdx = 0;
                for (int s = 1; s < p.numeroDeSurtidores; s++) {
                    if (ocupacion[s] < ocupacion[mejorIdx]) mejorIdx = s;
                }
                double esperaEst = Math.max(0.0, ocupacion[mejorIdx] - reloj);

                // Tiempo de servicio: formula del modelo
                double tServicio = p.tiempoValidacion + volumen / p.caudal;

                // Comparar costos
                double costoSub = volumen * precioT + esperaEst * costoOptMinuto;
                double costoInt = volumen * p.precioInternacional;

                boolean migro = costoInt < costoSub;
                String surtidorAsig;
                double finServicio;

                if (!migro) {
                    double inicioServicio = Math.max(reloj, ocupacion[mejorIdx]);
                    finServicio = inicioServicio + tServicio;
                    ocupacion[mejorIdx] = finServicio;
                    surtidorAsig = "S" + (mejorIdx + 1);
                } else {
                    finServicio = reloj + tServicio;
                    surtidorAsig = "S_int";
                }

                trimestre.vehiculos.add(new ResultadoVehiculo(
                        v + 1, reloj, perfil, volumen, esperaEst,
                        costoSub, costoInt, surtidorAsig, finServicio,
                        migro, t + 1, precioT));
            }

            resultados.add(trimestre);
        }

        return resultados;
    }
}
