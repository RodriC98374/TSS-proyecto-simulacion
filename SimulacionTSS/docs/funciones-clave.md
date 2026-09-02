# Funciones clave

Código listo para pegar en el informe. Cada bloque indica su marca, la sección a la
que corresponde, el archivo y la línea en la que empieza el método.

El archivo se recorre de arriba abajo: el orden es el de las secciones del informe.

Generado el 02/09/2026 a partir de las marcas CLAVE del código fuente.

---

## Sección 5.3 — Generación de números y variables aleatorias

Estos cuatro métodos son el núcleo aleatorio del que beben los seis ejercicios. Ninguna otra parte del programa produce números al azar.

### CLAVE 6.1 — Generador congruencial mixto

Archivo `src/tss/modelo/aleatorio/GeneradorCongruencialMixto.java`, línea 42, 11 líneas.

Produce cada número pseudoaleatorio uniforme del programa mediante la recurrencia congruencial mixta X(n+1) = (a·X(n) + c) mod m, y devuelve el cociente X/m, que cae en el intervalo [0, 1). Las constantes elegidas cumplen las condiciones de Hull-Dobell, de modo que la sucesión recorre el período máximo de 2³² valores antes de repetirse. La comparación siempre se hace sobre el valor real y nunca sobre los bits menos significativos, cuyo período es corto.

```java
@Override
public double siguiente() {
    // La mascara equivale a "mod 2^32" y evita el desbordamiento de long.
    estado = (m == M_POR_DEFECTO)
            ? ((a * estado + c) & 0xFFFFFFFFL)
            : Math.floorMod(a * estado + c, m);
    generados++;
    // Los bits bajos de un congruencial tienen periodo corto: por eso siempre
    // se compara el valor real en [0,1) y nunca un bit individual de X.
    return (double) estado / (double) m;
}
```

### CLAVE 6.2a — Transformada inversa para variables discretas

Archivo `src/tss/modelo/aleatorio/GeneradorVariables.java`, línea 30, 9 líneas.

Aplica el método de la transformada inversa a una variable discreta tabulada. Genera un uniforme y devuelve el primer valor de la tabla cuya probabilidad acumulada lo supera, con la convención de intervalos acum[i-1] ≤ R < acum[i] que usan las tablas del informe. Es el procedimiento con el que se obtienen los autos solicitados, la duración de la renta y el tiempo de entrega del proveedor.

```java
public int discretaPorTransformadaInversa(double[] acumuladas, int[] valores) {
    double r = fuente.siguiente();
    for (int i = 0; i < acumuladas.length; i++) {
        if (r < acumuladas[i]) {
            return valores[i];
        }
    }
    return valores[valores.length - 1];
}
```

### CLAVE 6.2b — Normal por el teorema del límite central

Archivo `src/tss/modelo/aleatorio/GeneradorVariables.java`, línea 43, 8 líneas.

Genera una variable normal por el teorema del límite central: suma exactamente doce números uniformes y les resta seis, con lo que obtiene un valor tipificado z de media cero y varianza uno. El valor de la variable buscada resulta de escalar ese z por la desviación y desplazarlo por la media. Es el método que emplean el ejercicio de interferencia y el de reemplazo.

```java
public double normalPorTeoremaLimiteCentral(double media, double desviacion) {
    double suma = 0.0;
    for (int i = 0; i < UNIFORMES_POR_NORMAL; i++) {
        suma += fuente.siguiente();
    }
    double z = suma - 6.0;
    return media + desviacion * z;
}
```

### CLAVE 6.2c — Binomial por ensayos de Bernoulli

Archivo `src/tss/modelo/aleatorio/GeneradorVariables.java`, línea 67, 9 líneas.

Obtiene una variable binomial simulando los ensayos de Bernoulli uno por uno: genera exactamente n números uniformes y cuenta cuántos resultan menores que la probabilidad de éxito. No se recurre a ninguna fórmula cerrada, de modo que el procedimiento reproduce el experimento tal como lo describe el enunciado. Con n = 6 y θ = 0,5 entrega la demanda diaria del problema de inventario.

```java
public int binomialPorEnsayosBernoulli(int n, double theta) {
    int exitos = 0;
    for (int i = 0; i < n; i++) {
        if (fuente.siguiente() < theta) {
            exitos++;
        }
    }
    return exitos;
}
```

---

## Secciones 5.1 y 5.2 — Método de rechazo

Los ejercicios 1 y 2 comparten simulador: sólo cambia la densidad que reciben.

### CLAVE 5.1b — Evaluación de la densidad por tramos

Archivo `src/tss/modelo/rechazo/DensidadPorTramos.java`, línea 51, 4 líneas.

Evalúa la función de densidad definida por partes en el punto solicitado, ubicando primero el tramo al que pertenece. El primer tramo incluye sus dos extremos y los siguientes excluyen su extremo izquierdo, de modo que un valor situado justo en la frontera se evalúa con el tramo anterior, tal como lo hacen las pruebas de escritorio del informe. La misma clase describe la densidad escalonada del ejercicio 1 y la de escalón con rampa del ejercicio 2.

```java
public double evaluar(double x) {
    int indice = indiceDeTramo(x);
    return indice < 0 ? 0.0 : tramos.get(indice).evaluar(x);
}
```

### CLAVE 5.1 — Algoritmo de resolución del método de rechazo

Archivo `src/tss/modelo/rechazo/SimuladorRechazo.java`, línea 35, 42 líneas.

Ejecuta el método de rechazo de Coss Bu durante el número de iteraciones pedido. En cada iteración genera dos uniformes: el primero ubica un punto x dentro del intervalo [a, b] y el segundo decide si ese punto se acepta, comparándolo contra el cociente f(x)/M. Además de la traza iteración por iteración, acumula el histograma de los valores aceptados y la tasa de aceptación observada, que debe acercarse a la eficiencia teórica 1/(M·(b−a)).

```java
public ResultadoRechazo simular(int iteraciones, ProgresoSimulacion.Monitor monitor) {
    double a = densidad.limiteInferior();
    double b = densidad.limiteSuperior();
    double m = densidad.maximo();

    List<IteracionRechazo> traza = new ArrayList<>(Math.min(iteraciones, MAXIMO_TRAZA));
    int[] conteos = new int[CLASES_HISTOGRAMA];
    int aceptados = 0;
    double sumaAceptados = 0.0;

    for (int i = 1; i <= iteraciones; i++) {
        if (monitor.cancelado()) {
            break;
        }
        double r1 = fuente.siguiente();
        double r2 = fuente.siguiente();
        double x = a + (b - a) * r1;
        int indiceTramo = densidad.indiceDeTramo(x);
        double fx = densidad.evaluar(x);
        double razon = fx / m;
        boolean aceptado = r2 <= razon;

        if (aceptado) {
            aceptados++;
            sumaAceptados += x;
            conteos[claseDe(x, a, b)]++;
        }
        if (traza.size() < MAXIMO_TRAZA) {
            traza.add(new IteracionRechazo(i, r1, r2, x, indiceTramo, fx, razon, aceptado));
        }
        if (i % 2000 == 0 || i == iteraciones) {
            monitor.reportar(new ProgresoSimulacion(i, iteraciones, "Iteracion " + i));
        }
    }

    int rechazados = iteraciones - aceptados;
    double tasa = iteraciones == 0 ? 0.0 : (double) aceptados / iteraciones;
    double media = aceptados == 0 ? 0.0 : sumaAceptados / aceptados;
    return new ResultadoRechazo(densidad, iteraciones, List.copyOf(traza),
            iteraciones > MAXIMO_TRAZA, aceptados, rechazados, tasa,
            densidad.eficienciaTeorica(), bordes(a, b), conteos, media);
}
```

---

## Sección 5.9.1 — Aplicación 1, renta de autos

Un año de operación de la flota y el barrido que compara tamaños de flota.

### CLAVE 5.5 — Algoritmo de resolución de la renta de autos

Archivo `src/tss/modelo/renta/SimuladorRenta.java`, línea 47, 39 líneas.

Simula un año completo de operación de la flota de renta. Cada día suma los autos que regresan, sortea la demanda, atiende lo que la disponibilidad permite y contabiliza por separado las solicitudes no atendidas y los autos que quedaron en el patio. Al cerrar el año calcula la utilidad restando al ingreso los costos de faltante, de ociosidad y de compra de la flota.

```java
public ResultadoAnioRenta simularAnio(int flota) {
    int dias = parametros.diasOperacion();
    // Se dimensiona a dias+5 (370) para tolerar t + duracion con t=365 y duracion=4.
    int[] retornos = new int[dias + 5];
    List<DiaRenta> traza = guardarTraza ? new ArrayList<>(dias) : List.of();

    int disponibles = flota;
    double ingreso = 0.0;
    int faltantes = 0;
    long ociosos = 0L;

    for (int t = 1; t <= dias; t++) {
        int inicio = disponibles;
        disponibles += retornos[t];
        double r1 = generador.uniforme();
        int demanda = GeneradorVariables.valorDiscreto(
                r1, ParametrosRenta.DEMANDA_ACUMULADAS, ParametrosRenta.DEMANDA_VALORES);
        int rentados = Math.min(demanda, disponibles);
        int faltanteDia = demanda - rentados;
        faltantes += faltanteDia;

        double ingresoDia = programarRetornos(t, rentados, retornos);
        ingreso += ingresoDia;
        disponibles -= rentados;
        ociosos += disponibles;

        if (guardarTraza) {
            traza.add(new DiaRenta(t, inicio, retornos[t], r1, demanda, rentados,
                    faltanteDia, disponibles, ingresoDia));
        }
    }

    double costoFlota = parametros.costoAnualPorAuto() * flota;
    double utilidad = ingreso
            - parametros.costoFaltante() * faltantes
            - parametros.costoOcioso() * ociosos
            - costoFlota;
    return new ResultadoAnioRenta(flota, ingreso, faltantes, ociosos, costoFlota, utilidad, traza);
}
```

### CLAVE 5.5c — Programación de retornos y cobro de la renta

Archivo `src/tss/modelo/renta/SimuladorRenta.java`, línea 91, 10 líneas.

Atiende los autos rentados de un día concreto. Para cada auto sortea la duración del contrato con la tabla de días, anota el día en que ese auto volverá al patio y suma el ingreso correspondiente. El ingreso se reconoce íntegro el día en que se firma la renta y no se prorratea entre los días del contrato.

```java
private double programarRetornos(int dia, int rentados, int[] retornos) {
    double ingresoDia = 0.0;
    for (int j = 1; j <= rentados; j++) {
        int duracion = generador.discretaPorTransformadaInversa(
                ParametrosRenta.DURACION_ACUMULADAS, ParametrosRenta.DURACION_VALORES);
        retornos[dia + duracion]++;
        ingresoDia += parametros.rentaPorDia() * duracion;
    }
    return ingresoDia;
}
```

### CLAVE 5.5b — Barrido del tamaño de flota

Archivo `src/tss/modelo/renta/SimuladorRenta.java`, línea 105, 35 líneas.

Repite el año de operación para cada tamaño de flota del rango pedido y promedia los resultados de corridas independientes. Cada corrida arranca de una semilla derivada de manera determinista de la semilla base, de modo que el experimento completo es reproducible. El resultado es la tabla de ingreso, faltantes, ociosidad y utilidad promedio con la que se elige el número óptimo de autos.

```java
public List<ResumenFlota> evaluarFlotas(int minimo, int maximo, int corridas,
        ProgresoSimulacion.Monitor monitor) {
    List<ResumenFlota> resumenes = new ArrayList<>();
    int totalPasos = (maximo - minimo + 1) * corridas;
    int paso = 0;
    guardarTraza = false;

    for (int n = minimo; n <= maximo; n++) {
        ResumenEstadistico ingreso = new ResumenEstadistico();
        ResumenEstadistico faltantes = new ResumenEstadistico();
        ResumenEstadistico ociosos = new ResumenEstadistico();
        ResumenEstadistico utilidad = new ResumenEstadistico();

        for (int c = 0; c < corridas; c++) {
            if (monitor.cancelado()) {
                guardarTraza = true;
                return resumenes;
            }
            reiniciarGenerador(GeneradorCongruencialMixto.semillaDerivada(semillaBase, c));
            ResultadoAnioRenta r = simularAnio(n);
            ingreso.agregar(r.ingreso());
            faltantes.agregar(r.faltantes());
            ociosos.agregar(r.ociosos());
            utilidad.agregar(r.utilidad());
            paso++;
            if (paso % 200 == 0 || paso == totalPasos) {
                monitor.reportar(new ProgresoSimulacion(paso, totalPasos, "Flota de " + n + " autos"));
            }
        }
        resumenes.add(new ResumenFlota(n, corridas, ingreso.media(), faltantes.media(),
                ociosos.media(), parametros.costoAnualPorAuto() * n, utilidad.media()));
    }
    guardarTraza = true;
    return resumenes;
}
```

---

## Sección 5.9.2 — Aplicación 2, interferencia eje-cojinete

Estimación de la probabilidad de interferencia y cálculo del tamaño de muestra.

### CLAVE 5.6 — Algoritmo de resolución de la interferencia eje-cojinete

Archivo `src/tss/modelo/interferencia/SimuladorInterferencia.java`, línea 40, 52 líneas.

Genera ensambles independientes de cojinete y flecha y estima la probabilidad de interferencia. Cada ensamble produce los dos diámetros por el teorema del límite central, calcula la holgura como su diferencia y la declara interferencia cuando resulta negativa. Junto con la probabilidad estimada acumula la serie de convergencia y el histograma de la holgura, cuya área negativa es precisamente la probabilidad buscada.

```java
public ResultadoInterferencia simular(int ensambles, ProgresoSimulacion.Monitor monitor) {
    GeneradorVariables generador = new GeneradorVariables(new GeneradorCongruencialMixto(semilla));
    boolean guardarTraza = ensambles <= MAXIMO_TRAZA;
    List<EnsambleSimulado> traza = guardarTraza ? new ArrayList<>(ensambles) : List.of();

    double[] bordes = bordesHolgura();
    int[] conteos = new int[CLASES_HOLGURA];
    List<Integer> marcasX = new ArrayList<>();
    List<Double> marcasY = new ArrayList<>();

    int conInterferencia = 0;
    double sumaX1 = 0.0;
    double sumaX2 = 0.0;
    int proximaMarca = 1;
    int hechos = 0;

    for (int i = 1; i <= ensambles; i++) {
        if (monitor.cancelado()) {
            break;
        }
        MuestraNormal cojinete = generador.generarNormalDetallada(
                parametros.mediaCojinete(), parametros.desviacionCojinete());
        MuestraNormal flecha = generador.generarNormalDetallada(
                parametros.mediaFlecha(), parametros.desviacionFlecha());
        double holgura = cojinete.valor() - flecha.valor();
        boolean interfiere = holgura < 0.0;

        hechos = i;
        sumaX1 += cojinete.valor();
        sumaX2 += flecha.valor();
        if (interfiere) {
            conInterferencia++;
        }
        conteos[claseDe(holgura, bordes)]++;
        if (guardarTraza) {
            traza.add(new EnsambleSimulado(i, cojinete, flecha, holgura, interfiere));
        }
        if (i >= proximaMarca) {
            marcasX.add(i);
            marcasY.add((double) conInterferencia / i);
            proximaMarca = Math.max(i + 1, (int) Math.ceil(i * 1.06));
        }
        if (i % 2000 == 0 || i == ensambles) {
            monitor.reportar(new ProgresoSimulacion(i, ensambles, "Ensamble " + i));
        }
    }

    uniformesConsumidos = generador.fuente().generados();
    double probabilidad = hechos == 0 ? 0.0 : (double) conInterferencia / hechos;
    return armarResultado(hechos, conInterferencia, probabilidad, sumaX1, sumaX2,
            traza, marcasX, marcasY, bordes, conteos);
}
```

### CLAVE 5.6b — Tamaño de muestra para un error dado

Archivo `src/tss/modelo/interferencia/SimuladorInterferencia.java`, línea 96, 4 líneas.

Calcula cuántas repeticiones hacen falta para que el error de la estimación no supere un valor dado con la confianza pedida, mediante n ≥ z²·p·(1−p)/e². El resultado se redondea hacia arriba porque el número de repeticiones es entero. Con p = 0,34458, un error de 0,01 y una confianza del 95 % el procedimiento devuelve 8.677 ensambles.

```java
public static int calcularTamanioMuestra(double p, double error, double z) {
    double n = (z * z * p * (1.0 - p)) / (error * error);
    return (int) Math.ceil(n);
}
```

---

## Sección 5.9.3 — Aplicación 3, política de inventario

Recorrido diario del inventario y la regla que decide cuándo colocar una orden.

### CLAVE 5.7 — Algoritmo de resolución de la política de inventario

Archivo `src/tss/modelo/inventario/SimuladorInventario.java`, línea 48, 52 líneas.

Recorre el horizonte día a día bajo la política de reabastecimiento indicada. Cada día recibe las órdenes que llegan, sortea la demanda, cobra el costo de faltante por las unidades que no pudo atender y el costo de mantener sobre el inventario de cierre positivo. El inventario puede quedar negativo: ese saldo representa las unidades pendientes de entrega, que se cubren solas con la siguiente llegada.

```java
public ResultadoCorridaInventario simularCorrida(PoliticaInventario politica) {
    int horizonte = parametros.horizonte();
    // Dimensionado a horizonte+35 para tolerar t + 1 + L con L maximo de 8.
    int[] llegadas = new int[horizonte + 35];
    List<DiaInventario> traza = guardarTraza ? new ArrayList<>(horizonte) : List.of();

    int inventario = parametros.inventarioInicial();
    int pendientes = 0;
    double costoMantener = 0.0;
    double costoFaltante = 0.0;
    double costoOrdenar = 0.0;
    int ordenes = 0;
    int unidadesFaltantes = 0;

    for (int t = 1; t <= horizonte; t++) {
        int llegadaHoy = llegadas[t];
        int inicio = inventario;
        inventario += llegadaHoy;
        pendientes -= llegadaHoy;

        double[] uniformes = guardarTraza ? new double[parametros.ensayosDemanda()] : null;
        int demanda = generarDemandaDiaria(uniformes);
        int disponible = Math.max(inventario, 0);
        int faltante = Math.max(demanda - disponible, 0);
        costoFaltante += parametros.costoFaltante() * faltante;
        unidadesFaltantes += faltante;

        // El inventario puede quedar negativo: son las unidades por surtir,
        // que se cubren solas al sumar la siguiente llegada.
        inventario -= demanda;
        if (inventario > 0) {
            costoMantener += parametros.costoMantener() * inventario;
        }

        int posicion = inventario + pendientes;
        int[] orden = evaluarReabastecimiento(politica, t, posicion, pendientes, llegadas);
        if (orden != null) {
            pendientes += orden[0];
            costoOrdenar += parametros.costoOrdenar();
            ordenes++;
        }
        if (guardarTraza) {
            traza.add(new DiaInventario(t, inicio, llegadaHoy, uniformes, demanda, faltante,
                    inventario, posicion, orden != null, orden == null ? 0 : orden[0],
                    orden == null ? -1 : orden[1], orden == null ? -1 : orden[2]));
        }
    }

    double total = costoMantener + costoFaltante + costoOrdenar;
    return new ResultadoCorridaInventario(politica, costoMantener, costoFaltante,
            costoOrdenar, total, ordenes, unidadesFaltantes, traza);
}
```

### CLAVE 5.7b — Decisión de reabastecimiento

Archivo `src/tss/modelo/inventario/SimuladorInventario.java`, línea 105, 15 líneas.

Decide si en el día en curso corresponde colocar una orden y de qué tamaño. Bajo la política de revisión periódica la orden se coloca por calendario; bajo la política de punto de reorden se coloca cuando la posición del inventario baja al nivel de disparo y no hay ninguna orden en camino. En ambos casos se pide la cantidad que falta para alcanzar el nivel objetivo y se sortea el tiempo de entrega con la tabla de Poisson del enunciado.

```java
private int[] evaluarReabastecimiento(PoliticaInventario politica, int dia, int posicion,
        int pendientes, int[] llegadas) {
    boolean ordenar = (politica == PoliticaInventario.PERIODICA)
            ? (dia % parametros.periodoRevision() == 0)
            : (posicion <= parametros.puntoReorden() && pendientes == 0);
    if (!ordenar || posicion >= parametros.nivelObjetivo()) {
        return null;
    }
    int cantidad = parametros.nivelObjetivo() - posicion;
    double r = generador.uniforme();
    int entrega = GeneradorVariables.indiceDeIntervalo(r, ParametrosInventario.ENTREGA_ACUMULADAS);
    int diaLlegada = dia + 1 + entrega;
    llegadas[diaLlegada] += cantidad;
    return new int[] { cantidad, entrega, diaLlegada };
}
```

---

## Sección 5.9.4 — Aplicación 4, reemplazo de componentes

Simulación orientada a eventos del equipo de cuatro componentes.

### CLAVE 5.8 — Algoritmo de resolución del reemplazo de componentes

Archivo `src/tss/modelo/reemplazo/SimuladorReemplazo.java`, línea 51, 45 líneas.

Simula el equipo de cuatro componentes avanzando de un evento de falla al siguiente. En cada iteración localiza el componente cuya falla está más próxima, y si ese instante todavía cae dentro del horizonte, ejecuta la intervención que la política ordena y reprograma las fallas. El costo total resulta de sumar los componentes consumidos y las horas que el equipo estuvo desconectado.

```java
public ResultadoCorridaReemplazo simularCorrida(PoliticaReemplazo politica) {
    int piezas = parametros.componentes();
    double[] falla = new double[piezas];
    for (int i = 0; i < piezas; i++) {
        falla[i] = generarVida();
    }
    List<EventoFalla> traza = guardarTraza ? new ArrayList<>() : List.of();

    int intervenciones = 0;
    int consumidos = 0;
    double horasParo = 0.0;

    while (true) {
        int m = indiceDeMenor(falla);
        if (falla[m] > parametros.horizonte()) {
            break;
        }
        double reloj = falla[m];
        intervenciones++;
        double vida;
        if (politica == PoliticaReemplazo.INDIVIDUAL) {
            consumidos += 1;
            horasParo += parametros.horasParoIndividual();
            vida = reemplazarIndividual(falla, m, reloj);
        } else {
            consumidos += piezas;
            horasParo += parametros.horasParoTotal();
            vida = reemplazarTodos(falla, reloj);
        }
        if (guardarTraza && traza.size() < MAXIMO_TRAZA) {
            double costo = parametros.costoComponente() * consumidos
                    + parametros.costoHoraParo() * horasParo;
            traza.add(new EventoFalla(intervenciones, reloj, m + 1, vida,
                    politica == PoliticaReemplazo.INDIVIDUAL
                            ? parametros.horasParoIndividual()
                            : parametros.horasParoTotal(),
                    consumidos, costo));
        }
    }

    double costoComponentes = parametros.costoComponente() * consumidos;
    double costoParo = parametros.costoHoraParo() * horasParo;
    return new ResultadoCorridaReemplazo(politica, intervenciones, consumidos, horasParo,
            costoComponentes, costoParo, costoComponentes + costoParo, traza);
}
```

### CLAVE 5.8b — Reemplazo individual, política A

Archivo `src/tss/modelo/reemplazo/SimuladorReemplazo.java`, línea 102, 9 líneas.

Aplica la política de reemplazo individual, que sustituye solamente el componente que falló. Primero desplaza una hora el instante de falla de los cuatro componentes, porque durante la desconexión el equipo no opera y ninguna pieza envejece, y sólo después reprograma el componente sustituido con una vida nueva. Ese orden es el que documenta el informe y no debe alterarse.

```java
private double reemplazarIndividual(double[] falla, int m, double reloj) {
    double paro = parametros.horasParoIndividual();
    for (int i = 0; i < falla.length; i++) {
        falla[i] += paro;
    }
    double vida = generarVida();
    falla[m] = reloj + paro + vida;
    return vida;
}
```

---

