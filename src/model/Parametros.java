package model;

public class Parametros {

    // Precios
    public double precioInternacional = 12.50;
    public double precioInicialSubvencionado = 6.96;
    public double incrementoTrimestral = 1.35;
    public int trimestres = 6;

    // Costos de oportunidad (Bs/hora)
    public double costoOptPesado = 150.0;
    public double costoOptParticular = 30.0;

    // Rangos de volumen (litros)
    public double volPesadoMin = 200.0;
    public double volPesadoMax = 400.0;
    public double volParticularMin = 20.0;
    public double volParticularMax = 45.0;

    // Servicio: tiempo = tiempoValidacion + volumen / caudal
    public double tiempoValidacion = 2.0;   // minutos (B-SISA)
    public double caudal = 100.0;           // litros por minuto

    // Simulacion
    public double tasaArribo = 1.0;         // vehiculos por minuto (lambda)
    public int vehiculosPorTrimestre = 100;
    public int numeroDeSurtidores = 4;
    public double proporcionPesado = 0.35;  // 35% transporte pesado
    public long semilla = 12345L;

    public double[] calcularPrecios() {
        double[] precios = new double[trimestres];
        double p = precioInicialSubvencionado;
        for (int i = 0; i < trimestres; i++) {
            precios[i] = Math.min(p, precioInternacional);
            p += incrementoTrimestral;
        }
        return precios;
    }

    public Parametros copia() {
        Parametros c = new Parametros();
        c.precioInternacional = this.precioInternacional;
        c.precioInicialSubvencionado = this.precioInicialSubvencionado;
        c.incrementoTrimestral = this.incrementoTrimestral;
        c.trimestres = this.trimestres;
        c.costoOptPesado = this.costoOptPesado;
        c.costoOptParticular = this.costoOptParticular;
        c.volPesadoMin = this.volPesadoMin;
        c.volPesadoMax = this.volPesadoMax;
        c.volParticularMin = this.volParticularMin;
        c.volParticularMax = this.volParticularMax;
        c.tiempoValidacion = this.tiempoValidacion;
        c.caudal = this.caudal;
        c.tasaArribo = this.tasaArribo;
        c.vehiculosPorTrimestre = this.vehiculosPorTrimestre;
        c.numeroDeSurtidores = this.numeroDeSurtidores;
        c.proporcionPesado = this.proporcionPesado;
        c.semilla = this.semilla;
        return c;
    }
}
