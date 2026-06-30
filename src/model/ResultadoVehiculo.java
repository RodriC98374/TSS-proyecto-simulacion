package model;

public class ResultadoVehiculo {
    public final int id;
    public final double minutoArribo;
    public final PerfilVehiculo perfil;
    public final double volumen;
    public final double esperaEstimada;
    public final double costoSubvencionado;
    public final double costoInternacional;
    public final String surtidorAsignado;
    public final double finServicio;
    public final boolean migroInternacional;
    public final int trimestre;
    public final double precioTrimestre;

    public ResultadoVehiculo(int id, double minutoArribo, PerfilVehiculo perfil,
            double volumen, double esperaEstimada,
            double costoSub, double costoInt,
            String surtidor, double finServicio,
            boolean migro, int trimestre, double precioTrimestre) {
        this.id = id;
        this.minutoArribo = minutoArribo;
        this.perfil = perfil;
        this.volumen = volumen;
        this.esperaEstimada = esperaEstimada;
        this.costoSubvencionado = costoSub;
        this.costoInternacional = costoInt;
        this.surtidorAsignado = surtidor;
        this.finServicio = finServicio;
        this.migroInternacional = migro;
        this.trimestre = trimestre;
        this.precioTrimestre = precioTrimestre;
    }
}
