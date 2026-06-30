package model;

import java.util.ArrayList;
import java.util.List;

public class ResultadoTrimestre {
    public int numeroTrimestre;
    public double precioSubvencionado;
    public List<ResultadoVehiculo> vehiculos = new ArrayList<>();

    public int getTotalVehiculos() {
        return vehiculos.size();
    }

    public int getMigrantes() {
        int count = 0;
        for (ResultadoVehiculo v : vehiculos) {
            if (v.migroInternacional) count++;
        }
        return count;
    }

    public double getPorcentajeMigracion() {
        if (vehiculos.isEmpty()) return 0;
        return (getMigrantes() * 100.0) / vehiculos.size();
    }

    public double getEsperaPromedioSubvencionados() {
        double suma = 0;
        int count = 0;
        for (ResultadoVehiculo v : vehiculos) {
            if (!v.migroInternacional) {
                suma += v.esperaEstimada;
                count++;
            }
        }
        return count == 0 ? 0 : suma / count;
    }

    public double getCostoPromedioSubvencionados() {
        double suma = 0;
        int count = 0;
        for (ResultadoVehiculo v : vehiculos) {
            if (!v.migroInternacional) {
                suma += v.costoSubvencionado;
                count++;
            }
        }
        return count == 0 ? 0 : suma / count;
    }
}
