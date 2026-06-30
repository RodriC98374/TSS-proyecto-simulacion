package model;

public enum PerfilVehiculo {
    TRANSPORTE_PESADO("Transporte Pesado"),
    VEHICULO_PARTICULAR("Vehículo Particular");

    public final String nombre;

    PerfilVehiculo(String nombre) {
        this.nombre = nombre;
    }
}
