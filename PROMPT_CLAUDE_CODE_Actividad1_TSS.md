# Especificación de implementación — Actividad 1, Taller de Simulación de Sistemas

> **Cómo usar este archivo.** Es el brief completo para Claude Code. Léelo entero antes de
> escribir una sola línea. No pidas aclaraciones sobre decisiones que ya están tomadas aquí
> (sección 14). Trabaja por fases (sección 12) y no declares terminado nada que no pase la
> verificación automática (sección 11) ni el checklist final (sección 13).

---

## 1. Contexto y objetivo

Se debe construir **una sola aplicación de escritorio en Java** que implemente los seis
ejercicios de la Actividad 1 de la materia Taller de Simulación de Sistemas. El informe
teórico ya está escrito y **es la fuente de verdad**: el código no puede desviarse del
modelo, del algoritmo ni del pseudocódigo documentados. Los resultados numéricos del
informe ya están publicados, así que el programa debe reproducirlos dentro de la tolerancia
indicada en cada ejercicio.

Los seis ejercicios son:

| # | Nombre corto | Tipo de modelo | Sección del informe |
|---|---|---|---|
| 1 | Método de rechazo — densidad escalonada | Generación de variables aleatorias | 5.1 |
| 2 | Método de rechazo — densidad escalonada + rampa | Generación de variables aleatorias | 5.2 |
| 3 | Renta de autos | Monte Carlo, tiempo discreto (día) | 5.5 |
| 4 | Interferencia eje-cojinete | Monte Carlo, experimentos independientes | 5.6 |
| 5 | Política de inventario | Monte Carlo, tiempo discreto (día) | 5.7 |
| 6 | Reemplazo de componentes | Monte Carlo orientado a eventos | 5.8 |

Cada uno de los seis tiene **su propia pestaña / vista independiente** dentro de la misma
ventana. No se hacen seis programas separados.

Un objetivo secundario, pero importante: el autor va a documentar el código dentro del
informe. Por eso el proyecto debe generar `docs/funciones-clave.md` (sección 10) y las
funciones centrales deben estar marcadas de forma que se puedan copiar directamente.

---

## 2. Restricciones técnicas duras

1. **Java puro con biblioteca estándar. Cero dependencias externas.** Nada de Maven, Gradle,
   FlatLaf, JFreeChart, JavaFX ni jars descargados. Todo se dibuja con Swing + Graphics2D.
2. **Debe ejecutarse en Visual Studio Code** con el Extension Pack for Java, sin configurar
   nada más: abrir la carpeta, abrir `App.java`, presionar Run.
3. **Java 17 o superior.** Antes de escribir código ejecuta `java -version`. Si la máquina
   tiene Java 11, evita `record`, `switch` con flechas y bloques de texto, y usa clases
   normales; deja constancia en el README de la versión objetivo detectada.
4. **Swing, no JavaFX.** Swing viene con el JDK y no exige módulos ni SDK adicional; JavaFX
   rompería el requisito 2.
5. **Sin `Math.random()` ni `java.util.Random` en la lógica de simulación.** Todos los
   números uniformes salen del generador congruencial mixto propio (sección 6.1), tal como
   dice el informe. `java.util.Random` solo puede aparecer, si hace falta, para elegir una
   semilla inicial "aleatoria" cuando el usuario no fija una.
6. **Todo el código, nombres de clases, métodos, variables, comentarios y textos de la
   interfaz en español**, sin tildes ni eñes en los identificadores (`PoliticaInventario`,
   `generarDemandaDiaria`), con tildes normales en los textos visibles al usuario.
7. **La interfaz nunca se congela.** Toda corrida larga va en un `SwingWorker` con barra de
   progreso y botón de cancelar. Nada de bucles pesados en el Event Dispatch Thread.
8. **Todo se crea desde cero**, en una carpeta nueva y ordenada según la sección 3. No
   improvises rutas ni dejes archivos sueltos en la raíz.

---

## 3. Estructura de carpetas

Crea exactamente esta estructura. Los nombres de paquete acompañan a las carpetas dentro de
`src/`.

```
SimulacionTSS/
├── .vscode/
│   └── settings.json
├── README.md
├── docs/
│   ├── funciones-clave.md          (se genera en la fase final)
│   └── guia-de-uso.md
├── salidas/                        (vacía; aquí caen los CSV y PNG exportados)
│   └── .gitkeep
├── bin/                            (salida de compilación; no versionar)
└── src/
    └── tss/
        ├── App.java                            ← punto de entrada (main)
        │
        ├── modelo/                             ← MODELO: lógica de simulación pura
        │   ├── aleatorio/
        │   │   ├── GeneradorCongruencialMixto.java
        │   │   ├── FuenteAleatoria.java         (interfaz: double siguiente())
        │   │   └── GeneradorVariables.java      (discreta, normal TLC, binomial, Poisson)
        │   ├── comun/
        │   │   ├── ResumenEstadistico.java
        │   │   └── ProgresoSimulacion.java      (callback de avance/cancelación)
        │   ├── rechazo/
        │   │   ├── DensidadPorTramos.java
        │   │   ├── Tramo.java
        │   │   ├── IteracionRechazo.java
        │   │   ├── SimuladorRechazo.java
        │   │   └── ResultadoRechazo.java
        │   ├── renta/
        │   │   ├── ParametrosRenta.java
        │   │   ├── DiaRenta.java
        │   │   ├── SimuladorRenta.java
        │   │   ├── ResultadoAnioRenta.java
        │   │   └── ResumenFlota.java
        │   ├── interferencia/
        │   │   ├── ParametrosInterferencia.java
        │   │   ├── EnsambleSimulado.java
        │   │   ├── SimuladorInterferencia.java
        │   │   └── ResultadoInterferencia.java
        │   ├── inventario/
        │   │   ├── PoliticaInventario.java      (enum)
        │   │   ├── ParametrosInventario.java
        │   │   ├── DiaInventario.java
        │   │   ├── SimuladorInventario.java
        │   │   ├── ResultadoCorridaInventario.java
        │   │   └── ResumenPolitica.java
        │   └── reemplazo/
        │       ├── PoliticaReemplazo.java       (enum)
        │       ├── ParametrosReemplazo.java
        │       ├── EventoFalla.java
        │       ├── SimuladorReemplazo.java
        │       ├── ResultadoCorridaReemplazo.java
        │       └── ResumenReemplazo.java
        │
        ├── controlador/                        ← CONTROLADOR: orquesta modelo ↔ vista
        │   ├── ControladorPrincipal.java
        │   ├── TareaSimulacion.java            (SwingWorker genérico reutilizable)
        │   ├── ControladorRechazo.java         (sirve a los ejercicios 1 y 2)
        │   ├── ControladorRenta.java
        │   ├── ControladorInterferencia.java
        │   ├── ControladorInventario.java
        │   ├── ControladorReemplazo.java
        │   └── ExportadorResultados.java       (CSV y PNG hacia salidas/)
        │
        ├── vista/                              ← VISTA: todo lo visual
        │   ├── VentanaPrincipal.java
        │   ├── RailNavegacion.java
        │   ├── PanelEjercicio.java             (clase base abstracta de los 6 paneles)
        │   ├── paneles/
        │   │   ├── PanelRechazo.java           (parametrizable: se instancia 2 veces)
        │   │   ├── PanelRenta.java
        │   │   ├── PanelInterferencia.java
        │   │   ├── PanelInventario.java
        │   │   └── PanelReemplazo.java
        │   ├── componentes/
        │   │   ├── BotonAccion.java
        │   │   ├── CampoNumerico.java
        │   │   ├── SelectorOpcion.java
        │   │   ├── TarjetaMetrica.java
        │   │   ├── TablaTecnica.java
        │   │   ├── PanelParametros.java
        │   │   ├── BarraProgreso.java
        │   │   └── Bitacora.java               (consola de corrida visual)
        │   ├── graficas/
        │   │   ├── Grafica.java                (base: ejes, retícula, márgenes, export PNG)
        │   │   ├── GraficaDensidad.java        (f(x) + puntos aceptados/rechazados)
        │   │   ├── GraficaHistograma.java
        │   │   ├── GraficaLineas.java
        │   │   ├── GraficaConvergencia.java
        │   │   └── GraficaBarrasApiladas.java
        │   └── diseno/
        │       ├── Paleta.java
        │       ├── Tipografia.java
        │       ├── Medidas.java
        │       └── Pinceles.java               (utilidades Graphics2D: hints, sombras, bordes)
        │
        └── verificacion/
            └── Verificacion.java               ← main headless de autocomprobación
```

Contenido de `.vscode/settings.json`:

```json
{
  "java.project.sourcePaths": ["src"],
  "java.project.outputPath": "bin",
  "java.project.referencedLibraries": [],
  "files.encoding": "utf8"
}
```

---

## 4. Identidad visual

Esta es la parte donde el proyecto se gana o se pierde. El encargo explícito es que **no
parezca una interfaz genérica generada por IA**. Antes de codificar la vista, carga las
skills `ui-ux-pro-mas` y `frontend-design` y aplica su método de trabajo (plan de diseño →
crítica del plan → construcción → autocrítica).

### 4.1 Concepto

**"Mesa de laboratorio numérico".** El sujeto es un instrumento de medición: el usuario fija
parámetros, dispara una corrida y observa cómo se estabiliza un número. La metáfora visual
es el papel de registro técnico sobre una mesa oscura: retícula tenue, cifras alineadas,
trazos finos, y color usado como *semántica* y no como decoración.

La regla que gobierna todo el color: **el color significa un estado del modelo, nunca adorna**.
Ámbar = valor generado / acción del usuario. Verde = aceptado, ahorro, política ganadora.
Rojo = rechazado, faltante, costo, política perdedora. Si un elemento no representa ninguno
de esos estados, va en neutros.

### 4.2 Paleta (clase `Paleta`, constantes `Color` públicas)

| Token | Hex | Uso |
|---|---|---|
| `FONDO` | `#152A2E` | fondo de la ventana |
| `SUPERFICIE` | `#1D383D` | paneles de contenido |
| `SUPERFICIE_ALTA` | `#26474D` | cabeceras de tabla, campos, estados hover |
| `RETICULA` | `#2F565D` | líneas de retícula, separadores, bordes de 1 px |
| `TINTA` | `#EAF2EE` | texto principal |
| `TINTA_SUAVE` | `#9CB5B4` | etiquetas, texto secundario |
| `TINTA_TENUE` | `#647F80` | texto deshabilitado, marcas de eje |
| `AMBAR` | `#F2B134` | acento primario: acción, foco, serie de datos principal |
| `AMBAR_OSCURO` | `#C98F1F` | presionado |
| `VERDE` | `#5FC2A0` | aceptado, ahorro, óptimo |
| `ROJO` | `#E2664F` | rechazado, faltante, costo, sobrecosto |
| `AZUL` | `#6EA8D8` | segunda serie de datos cuando hacen falta dos neutras |

No uses gradientes decorativos, ni sombras difusas genéricas bajo cada tarjeta, ni un
`border-radius` uniforme en todo. Radio: `0 px` en tablas y campos, `4 px` en botones y
tarjetas de métrica. Un único radio adicional no está permitido.

### 4.3 Tipografía (clase `Tipografia`)

Dos familias, resueltas en tiempo de ejecución contra
`GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()` con cadena
de respaldo:

- **Interfaz (sans):** `Inter` → `Segoe UI Variable` → `Segoe UI` → `SF Pro Text` →
  `Helvetica Neue` → `Ubuntu` → `DejaVu Sans` → `Font.SANS_SERIF`.
- **Cifras (mono):** `JetBrains Mono` → `Cascadia Code` → `Consolas` → `SF Mono` → `Menlo` →
  `DejaVu Sans Mono` → `Font.MONOSPACED`.

La monoespaciada se usa **solo para números** (tablas, métricas, bitácora, marcas de eje).
Ahí está justificada por función: alinea las cifras en columnas. No la uses para etiquetas ni
títulos; eso es un tic visual conocido.

Escala: `11 / 13 / 15 / 19 / 26 / 34`. Pesos: regular para cuerpo, semibold para títulos de
sección y valores de métrica. Aplica `TextAttribute.TRACKING` positivo (≈ `0.06`) únicamente
en el título de la aplicación.

Prohibiciones tipográficas concretas:
- Nada de etiquetas en MAYÚSCULAS espaciadas encima de cada bloque.
- Nada de una sola palabra del título en otro color o en cursiva.
- Nada de cadenas tipo `A · B · C` unidas con punto medio.
- Nada de `→` pegado al texto de los botones.

### 4.4 Disposición

```
┌──────────────────────────────────────────────────────────────────────────┐
│  Simulación de sistemas · Actividad 1        [barra de título propia]    │
├───────────────┬──────────────────────────────────────────────────────────┤
│               │  Ejercicio 3 — Renta de autos                            │
│  ▸ Ejercicio 1│  Determinar el número óptimo de autos a comprar          │
│    Rechazo I  │  ──────────────────────────────────────────────────────  │
│               │ ┌────────────────┐ ┌───────────────────────────────────┐ │
│  ▸ Ejercicio 2│ │  PARÁMETROS    │ │  RESULTADOS                       │ │
│    Rechazo II │ │                │ │  [tarjetas de métrica]            │ │
│               │ │  campos        │ ├───────────────────────────────────┤ │
│  ▸ Ejercicio 3│ │  semilla       │ │  [tabla técnica | gráfica]        │ │
│    Renta      │ │                │ │                                   │ │
│               │ │  [Corrida      │ │                                   │ │
│  ▸ Ejercicio 4│ │   visual]      │ │                                   │ │
│    Interfer.  │ │  [Experimento] │ │                                   │ │
│               │ │  [Exportar]    │ │                                   │ │
│  ▸ Ejercicio 5│ └────────────────┘ └───────────────────────────────────┘ │
│    Inventario │  ────────────────────────────────────────────────────    │
│               │  [barra de progreso + estado + cancelar]                 │
│  ▸ Ejercicio 6│                                                          │
│    Reemplazo  │                                                          │
└───────────────┴──────────────────────────────────────────────────────────┘
```

- **Navegación: rail vertical izquierdo de ancho fijo (232 px)** con las seis entradas. Cumple
  el requisito de "una pestaña por ejercicio" mediante `CardLayout`, y se elige vertical
  porque seis títulos descriptivos no caben legiblemente en una fila horizontal. El indicador
  de selección es una **barra ámbar de 3 px sobre el borde izquierdo** del elemento activo,
  más un cambio de fondo a `SUPERFICIE_ALTA`; no uses subrayados ni pastillas redondeadas.
- Los seis paneles comparten **exactamente la misma anatomía**: encabezado (título + una línea
  con la pregunta del enunciado), columna de parámetros a la izquierda, zona de resultados a
  la derecha, barra de estado abajo. La consistencia es lo que hace que se lea como un
  instrumento y no como seis pantallas distintas.
- Ventana: mínimo `1180 × 760`, inicial `1360 × 860`, redimensionable. Todo debe seguir siendo
  usable al mínimo.
- Márgenes y espaciado en múltiplos de 4 (`Medidas`: `4, 8, 12, 16, 24, 32, 48`).

### 4.5 Calidad de dibujo

En cada `paintComponent` personalizado activa:

```java
g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
```

Centraliza esto en `Pinceles.preparar(Graphics2D)`. El foco de teclado debe ser visible
(anillo ámbar de 1 px). Tab debe recorrer los controles en orden lógico. Enter en un campo
de parámetros dispara la acción primaria del panel.

### 4.6 Movimiento

Una sola animación en toda la aplicación: el **contador de la métrica principal**, que al
terminar una corrida interpola su valor durante 350 ms con `javax.swing.Timer` y easing
suave. Nada más se anima. Nada de transiciones al pasar el cursor sobre tarjetas, nada de
apariciones deslizadas por sección.

---

## 5. Arquitectura y contratos entre capas

Separación estricta:

- **`modelo/`** no importa nada de `javax.swing` ni de `java.awt`. Es lógica pura, testeable
  desde consola. Cada simulador recibe sus parámetros y una `FuenteAleatoria`, y devuelve
  objetos de resultado inmutables. Ningún simulador imprime nada.
- **`vista/`** no contiene lógica de simulación. Recibe objetos de resultado y los pinta.
  Los paneles exponen métodos como `mostrarResultado(...)`, `mostrarProgreso(...)`,
  `limpiar()` y notifican al controlador mediante interfaces de escucha propias
  (`EscuchaPanel`), no mediante referencias directas a clases de controlador.
- **`controlador/`** lee los parámetros del panel, valida, arma el objeto de parámetros,
  lanza el `SwingWorker`, recibe el resultado y se lo entrega al panel. También maneja
  cancelación, errores de validación y exportación.

`TareaSimulacion<T>` es un `SwingWorker<T, ProgresoSimulacion>` genérico: recibe un
`Supplier<T>` que corre en segundo plano, publica avance y entrega el resultado a un
`Consumer<T>` en el hilo de UI. Los cinco controladores concretos lo reutilizan.

Validación de entrada: `CampoNumerico` acepta solo dígitos y separador decimal, expone
`getEntero()` / `getDecimal()`, y marca el borde en `ROJO` con un mensaje corto bajo el campo
cuando el valor está fuera de rango. El botón de acción se deshabilita mientras haya un campo
inválido. Los mensajes dicen qué se espera: "Entre 1 y 100000 corridas", no "Valor inválido".

---

## 6. Núcleo aleatorio

### 6.1 `GeneradorCongruencialMixto`

El informe especifica en la Parte 1 que los uniformes provienen de un generador congruencial
mixto. Úsalo en **los seis ejercicios**, sin excepción.

Recurrencia: `X(n+1) = (a · X(n) + c) mod m`, con `R(n+1) = X(n+1) / m`.

Constantes por defecto: `a = 1664525`, `c = 1013904223`, `m = 2^32`, semilla por defecto
`12345`. Estas cumplen las condiciones de Hull–Dobell, así que el período es el máximo
posible, `2^32`. Implementa `m` con aritmética `long` y máscara `& 0xFFFFFFFFL`.

API mínima:

```java
public interface FuenteAleatoria {
    double siguiente();          // uniforme en [0, 1)
    long semillaInicial();
    long generados();            // cuántos números se han producido
}
```

`GeneradorCongruencialMixto` implementa `FuenteAleatoria`, expone `a`, `c`, `m` y la semilla
como propiedades consultables (la vista las muestra), y ofrece `reiniciar()` para volver al
estado inicial. Cada corrida de un experimento debe partir de una semilla derivada
determinista (`semillaBase + indiceCorrida * 7919`) para que un mismo experimento sea
reproducible completo.

Documenta en un comentario breve la advertencia real de estos generadores: los bits menos
significativos tienen período corto, por lo que las comparaciones deben hacerse sobre el
valor real en `[0,1)` y no sobre bits bajos. Todos los usos de este proyecto lo cumplen.

### 6.2 `GeneradorVariables`

Envuelve una `FuenteAleatoria` y ofrece los cuatro procedimientos que usa el informe:

```java
int    discretaPorTransformadaInversa(double[] acumuladas, int[] valores);
double normalPorTeoremaLimiteCentral(double media, double desviacion);   // suma de 12 uniformes − 6
int    binomialPorEnsayosBernoulli(int n, double theta);                 // cuenta R < theta
int    poissonPorTransformadaInversa(double[] acumuladas);               // tabla del enunciado
```

Detalles obligatorios:

- `normalPorTeoremaLimiteCentral` suma **exactamente 12** uniformes y resta 6. No uses
  Box-Muller ni `nextGaussian`. Debe existir además una variante que devuelva también los 12
  uniformes y el `z` intermedio, para poder mostrarlos en la corrida visual del ejercicio 4:
  `MuestraNormal generarNormalDetallada(double media, double desviacion)`.
- `binomialPorEnsayosBernoulli` genera **exactamente `n`** uniformes y cuenta cuántos son
  menores que `theta`. No uses fórmulas cerradas.
- `discretaPorTransformadaInversa` recorre las acumuladas y devuelve el primer valor cuya
  acumulada supere estrictamente a `R`. Documenta que la convención de intervalos es
  `acum[i-1] ≤ R < acum[i]`, coincidente con las tablas del informe.

---

## 7. Especificación ejercicio por ejercicio

Para cada uno se indica: parámetros, algoritmo exacto (transcrito del pseudocódigo del
informe, que es la referencia normativa), salidas y contenido del panel. **No optimices ni
"mejores" los algoritmos.** Si algo parece redundante, es porque el informe lo documenta así
y las pruebas de escritorio ya lo validaron.

---

### 7.1 Ejercicio 1 — Método de rechazo, densidad escalonada

**Densidad.** Definida por tramos sobre `[0, 2]`:

```
f(x) = 3/4   para 0 ≤ x ≤ 1
f(x) = 1/4   para 1 < x ≤ 2
```

`a = 0`, `b = 2`, `M = 3/4`. Verificación de que integra a 1: `3/4·1 + 1/4·1 = 1`.

**Algoritmo (Coss Bu, método de rechazo).**

```
1. Generar R1 y R2 con el generador congruencial mixto.
2. x = a + (b − a)·R1
3. Evaluar f(x) según el tramo al que pertenece x.
4. Si R2 ≤ f(x)/M  →  aceptar x.
   Si no            →  rechazar y volver al paso 1.
```

**Valor de referencia.** La eficiencia teórica de aceptación es
`1 / (M·(b − a)) = 1 / (0,75 · 2) = 0,6667`. Con 100000 iteraciones la tasa observada debe
caer en `0,6667 ± 0,005`.

**Casos de prueba del informe** (deben reproducirse exactamente si se inyectan esos
uniformes, y así lo hará `Verificacion`):

| It. | R1 | R2 | x | Tramo | f(x) | f(x)/M | Resultado |
|---|---|---|---|---|---|---|---|
| 1 | 0,60 | 0,50 | 1,2 | 2.º | 1/4 | 0,333 | Rechazado |
| 2 | 0,30 | 0,50 | 0,6 | 1.º | 3/4 | 1,000 | Aceptado, x = 0,6 |
| 3 | 0,75 | 0,70 | 1,5 | 2.º | 1/4 | 0,333 | Rechazado |
| 4 | 0,55 | 0,60 | 1,1 | 2.º | 1/4 | 0,333 | Rechazado |
| 5 | 0,85 | 0,20 | 1,7 | 2.º | 1/4 | 0,333 | Aceptado, x = 1,7 |

**Modelado.** `DensidadPorTramos` guarda una lista de `Tramo(inicio, fin, evaluador)` y
resuelve `evaluar(x)`, `maximo()`, `limiteInferior()`, `limiteSuperior()`. Cada `Tramo` puede
ser constante o lineal (`f(x) = pendiente·x + intercepto`), lo que permite que **el ejercicio 2
reutilice la misma clase**. `SimuladorRechazo` recibe la densidad y el número de iteraciones y
devuelve `ResultadoRechazo` con la lista de `IteracionRechazo` (`numero`, `r1`, `r2`, `x`,
`indiceTramo`, `fx`, `razon`, `aceptado`), el conteo de aceptados y rechazados, la tasa de
aceptación y un histograma de los valores aceptados.

**Panel.**
- Parámetros: número de iteraciones (por defecto 30, que replica el Excel del informe),
  semilla, y un botón que carga la secuencia de la prueba de escritorio.
- Resultados: tarjetas con aceptados, rechazados, tasa de aceptación y tasa teórica.
- Tabla técnica con las columnas exactas del informe: `It. | R1 | R2 | x | Tramo | f(x) | f(x)/M | ¿Acepta? | Resultado`.
  Las filas aceptadas llevan el valor en `VERDE`, las rechazadas en `ROJO`. Solo esa celda va
  coloreada; no pintes la fila entera.
- `GraficaDensidad`: dibuja `f(x)` como escalón grueso, el rectángulo envolvente `M×(b−a)` en
  línea punteada, y cada iteración como un punto en `(x, R2·M)` — verde si fue aceptado, rojo
  si fue rechazado. Esta gráfica es la que explica visualmente el método y es el elemento con
  más peso del panel.
- `GraficaHistograma`: distribución de los valores aceptados contra la densidad teórica
  superpuesta, para verificar que el método reproduce `f(x)`.

---

### 7.2 Ejercicio 2 — Método de rechazo, escalón + rampa descendente

**Densidad.** Sobre `[0, 5/3]`:

```
f(x) = 3/4                para 0 ≤ x ≤ 1
f(x) = −(9/8)x + 15/8     para 1 < x ≤ 5/3
```

`a = 0`, `b = 5/3`, `M = 3/4`. La rampa decrece desde `(1, 3/4)` hasta `(5/3, 0)` con
pendiente `−9/8`. Verificación: `3/4 + (1/2)(2/3)(3/4) = 3/4 + 1/4 = 1`.

**Algoritmo.** Idéntico al ejercicio 1, con `x = a + (b − a)·R1 = (5/3)·R1`. La única
diferencia real es la densidad, por eso `SimuladorRechazo`, `PanelRechazo` y
`ControladorRechazo` se reutilizan con distinta configuración. **No dupliques clases**: el
panel se instancia dos veces con una `DensidadPorTramos` distinta, un título distinto y un
texto de enunciado distinto.

**Valor de referencia.** Eficiencia `1 / (0,75 · 5/3) = 0,80`. Con 100000 iteraciones,
`0,80 ± 0,005`.

**Casos de prueba del informe:**

| It. | R1 | R2 | x | Tramo | f(x) | f(x)/M | Resultado |
|---|---|---|---|---|---|---|---|
| 1 | 0,60 | 0,50 | 1,00 | 1.º | 0,7500 | 1,00 | Aceptado, x = 1 |
| 2 | 0,90 | 0,30 | 1,50 | 2.º | 0,1875 | 0,25 | Rechazado |
| 3 | 0,78 | 0,70 | 1,30 | 2.º | 0,4125 | 0,55 | Rechazado |
| 4 | 0,66 | 0,90 | 1,10 | 2.º | 0,6375 | 0,85 | Rechazado |
| 5 | 0,72 | 0,50 | 1,20 | 2.º | 0,5250 | 0,70 | Aceptado, x = 1,2 |

Cuidado con el borde: en `x = 1` el informe evalúa con el **primer** tramo. La regla de
pertenencia debe ser `inicio ≤ x ≤ fin` para el primer tramo y `inicio < x ≤ fin` para los
siguientes, evaluados en orden.

---

### 7.3 Ejercicio 3 — Renta de autos

**Enunciado.** Determinar cuántos autos comprar. Costo anual por auto Bs. 75.000; renta
Bs. 350 por auto y por día contratado; costo de faltante Bs. 200 por solicitud no atendida;
costo de ociosidad Bs. 50 por auto y día en patio; 365 días de operación.

**Distribuciones (transformada inversa discreta).**

Autos solicitados por día:

| Valor | Prob. | Acumulada | Intervalo |
|---|---|---|---|
| 0 | 0,10 | 0,10 | 0,00 ≤ R < 0,10 |
| 1 | 0,10 | 0,20 | 0,10 ≤ R < 0,20 |
| 2 | 0,25 | 0,45 | 0,20 ≤ R < 0,45 |
| 3 | 0,30 | 0,75 | 0,45 ≤ R < 0,75 |
| 4 | 0,25 | 1,00 | 0,75 ≤ R < 1,00 |

Días que dura la renta:

| Valor | Prob. | Acumulada | Intervalo |
|---|---|---|---|
| 1 | 0,40 | 0,40 | 0,00 ≤ R < 0,40 |
| 2 | 0,35 | 0,75 | 0,40 ≤ R < 0,75 |
| 3 | 0,15 | 0,90 | 0,75 ≤ R < 0,90 |
| 4 | 0,10 | 1,00 | 0,90 ≤ R < 1,00 |

**Pseudocódigo normativo.**

```
Definir N, numero_de_corridas
renta ← 350; costo_faltante ← 200; costo_ocioso ← 50; costo_auto ← 75000
suma_utilidad ← 0
Para cada corrida c:
    disponibles ← N
    retornos[1..369] ← 0
    ingreso ← 0; faltantes ← 0; ociosos ← 0
    Para t = 1 hasta 365:
        disponibles ← disponibles + retornos[t]
        Generar R1;  demanda ← ValorDiscreto(R1, tabla_demanda)
        rentados ← min(demanda, disponibles)
        faltantes ← faltantes + (demanda − rentados)
        Para j = 1 hasta rentados:
            Generar R2;  dias ← ValorDiscreto(R2, tabla_duracion)
            retornos[t + dias] ← retornos[t + dias] + 1
            ingreso ← ingreso + renta * dias
        disponibles ← disponibles − rentados
        ociosos ← ociosos + disponibles
    utilidad ← ingreso − costo_faltante*faltantes − costo_ocioso*ociosos − costo_auto*N
    suma_utilidad ← suma_utilidad + utilidad
utilidad_promedio ← suma_utilidad / numero_de_corridas
```

Notas de implementación: el arreglo de retornos se dimensiona a `370` para tolerar
`t + dias` con `t = 365` y `dias = 4`; los retornos programados más allá del día 365 no se
usan y eso es correcto. El ingreso se contabiliza el día en que se firma la renta, no
prorrateado.

**Salidas.** `ResultadoAnioRenta` (traza día a día para la corrida visual: `dia`,
`disponiblesInicio`, `retornos`, `r1`, `demanda`, `rentados`, `faltantes`, `ociosos`,
`ingresoDia`) y `ResumenFlota` por tamaño de flota (`n`, `ingresoPromedio`,
`faltantesPromedio`, `ociososPromedio`, `costoFlota`, `utilidadPromedio`).

**Valores de referencia (5.000 corridas), tolerancia ±2 %:**

| N | Ingreso | Faltantes | Ociosos | Utilidad |
|---|---|---|---|---|
| 1 | 121.072 | 735,03 | 19,75 | −101.921,00 |
| 2 | 238.046 | 564,04 | 51,19 | −27.320,66 |
| 3 | 346.408 | 404,82 | 107,21 | 35.082,70 |
| 4 | 441.026 | 265,96 | 202,35 | 77.717,07 |
| 5 | 516.971 | 154,89 | 350,78 | **93.455,04** |
| 6 | 570.788 | 77,03 | 562,42 | 77.261,30 |
| 7 | 601.962 | 30,50 | 838,52 | 28.937,04 |
| 8 | 616.226 | 9,26 | 1.162,84 | −43.767,85 |

**El óptimo debe salir N = 5.** Esto es criterio de aceptación, no una sugerencia.

**Panel.**
- Parámetros: flota mínima y máxima a barrer (1 a 8 por defecto), número de corridas
  (1.000 / 5.000 como accesos rápidos), semilla, y los cinco parámetros económicos editables.
- Modo "Corrida visual": un solo año con un `N` fijo, tabla día a día en `Bitacora` /
  `TablaTecnica`, avanzando con paso configurable.
- Modo "Experimento": barrido de `N` con la barra de progreso.
- Resultados: tabla con las seis columnas de arriba; la fila del óptimo se resalta con fondo
  `SUPERFICIE_ALTA` y el valor de utilidad en `VERDE`.
- `GraficaLineas`: utilidad promedio contra `N`, con marcador circular ámbar sobre el máximo
  y su valor etiquetado. Es la gráfica que el informe pide insertar.

---

### 7.4 Ejercicio 4 — Interferencia eje-cojinete

**Enunciado.** Diámetro interior del cojinete `x1 ~ Normal(1,50 ; varianza 0,0016)`, es decir
`σ1 = 0,04`. Diámetro de la flecha `x2 ~ Normal(1,48 ; varianza 0,0009)`, `σ2 = 0,03`. Hay
interferencia cuando `x2 > x1`. Se pide (a) la probabilidad de interferencia y (b) el número
de repeticiones para que el error sea menor que 0,01 con 95 % de seguridad.

**Generación.** Teorema del límite central, doce uniformes por variable:

```
z = (R1 + R2 + ... + R12) − 6
x1 = 1,50 + 0,04 · z1
x2 = 1,48 + 0,03 · z2
holgura = x1 − x2
Si holgura < 0  →  interferencia
```

**Valor de referencia analítico.** `D = x1 − x2` es normal con `E(D) = 0,02` y
`Var(D) = 0,0025`, o sea `σ(D) = 0,05`. Entonces
`P(interferencia) = P(Z < −0,40) = 0,34458`. Este número debe aparecer en la interfaz como
línea de referencia y usarse para calcular la diferencia absoluta.

**Tamaño de muestra.** `n ≥ Z²·p·(1−p) / e²` con `Z = 1,96`, `e = 0,01` y `p = 0,34458`,
lo que da `8.676,03` y, redondeando hacia arriba, **8.677**. Impleméntalo como método
`calcularTamanioMuestra(double p, double error, double z)` con `Math.ceil`. Muestra también,
como dato secundario, el valor conservador con `p = 0,5`, que es `9.604`.

**Valores de referencia (tolerancia ±0,01 en la probabilidad):**

| Ensambles | Con interferencia | p estimada | Diferencia vs 0,34458 |
|---|---|---|---|
| 100 | 42 | 0,4200 | 0,0754 |
| 1.000 | 329 | 0,3290 | 0,0156 |
| 5.000 | 1.736 | 0,3472 | 0,0026 |
| 8.677 | 2.986 | 0,3441 | 0,0005 |
| 20.000 | 6.912 | 0,3456 | 0,0010 |
| 100.000 | 34.572 | 0,3457 | 0,0011 |

Los conteos exactos dependen de la semilla; lo que se exige es que `p` converja a `0,3446`
y que con `n = 8.677` la diferencia sea menor que `0,01`.

**Panel.**
- Parámetros: medias y desviaciones de ambas piezas (editables), número de ensambles con
  accesos rápidos a los seis tamaños de la tabla, semilla.
- Modo "Corrida visual" (10 a 20 ensambles): la `Bitacora` muestra, por ensamble, los doce
  uniformes de cada pieza, `z1`, `z2`, `x1`, `x2`, la holgura y el veredicto. Los doce
  uniformes se muestran en una fila monoespaciada con cuatro decimales. Esto es exactamente
  lo que el informe describe como experimentación de primera etapa.
- Resultados: tarjetas con `p` estimada, valor de referencia `0,34458`, diferencia absoluta y
  `n` requerido. Además, promedio de `x1` y de `x2` generados, que deben acercarse a 1,50 y
  1,48 (control de calidad del generador).
- `GraficaConvergencia`: `p` estimada acumulada contra el número de ensambles, en escala
  logarítmica en el eje horizontal, con línea horizontal punteada en `0,34458` y una banda de
  `±0,01` alrededor. Marca vertical en `n = 8.677`.
- `GraficaHistograma` opcional de la holgura, con el área negativa en `ROJO`, que es la
  probabilidad buscada. Este es un buen candidato a ser el elemento memorable del panel.

---

### 7.5 Ejercicio 5 — Política de inventario

**Enunciado.** Demanda diaria `Binomial(n = 6 ; θ = 0,5)`. Tiempo de entrega
`Poisson(λ = 3)`. Costo de mantener `$1` por unidad y día; faltante `$10` por unidad;
ordenar `$50` por orden. Política 1: ordenar cada 8 días hasta 30 artículos. Política 2:
ordenar hasta 30 artículos cuando el inventario sea menor o igual a 10. Los faltantes de un
ciclo se surten con la orden que llega en el ciclo siguiente. Horizonte: 365 días, inventario
inicial 30.

**Tabla de tiempo de entrega (Poisson λ = 3, transformada inversa):**

| Días | Prob. | Acumulada | Intervalo |
|---|---|---|---|
| 0 | 0,0498 | 0,0498 | 0,0000 ≤ R < 0,0498 |
| 1 | 0,1494 | 0,1991 | 0,0498 ≤ R < 0,1991 |
| 2 | 0,2240 | 0,4232 | 0,1991 ≤ R < 0,4232 |
| 3 | 0,2240 | 0,6472 | 0,4232 ≤ R < 0,6472 |
| 4 | 0,1680 | 0,8153 | 0,6472 ≤ R < 0,8153 |
| 5 | 0,1008 | 0,9161 | 0,8153 ≤ R < 0,9161 |
| 6 | 0,0504 | 0,9665 | 0,9161 ≤ R < 0,9665 |
| 7 | 0,0216 | 0,9881 | 0,9665 ≤ R < 0,9881 |
| 8 o más | 0,0119 | 1,0000 | 0,9881 ≤ R < 1,0000 |

Codifica esta tabla literalmente como constante; no la recalcules con la fórmula de Poisson,
porque el informe documenta estos valores redondeados y las pruebas de escritorio se apoyan
en ellos. El valor "8 o más" se trata como 8.

**Pseudocódigo normativo.**

```
nivel_objetivo ← 30; c_inv ← 1; c_falt ← 10; c_ord ← 50
Para cada corrida:
    inventario ← 30; pendientes ← 0; llegadas[1..400] ← 0
    costo_inventario ← 0; costo_faltante ← 0; costo_ordenar ← 0
    Para t = 1 hasta 365:
        inventario ← inventario + llegadas[t]
        pendientes ← pendientes − llegadas[t]
        demanda ← 0
        Para j = 1 hasta 6:
            Generar R;  Si R < 0.5 entonces demanda ← demanda + 1
        disponible ← max(inventario, 0)
        faltante  ← max(demanda − disponible, 0)
        costo_faltante ← costo_faltante + c_falt * faltante
        inventario ← inventario − demanda
        Si inventario > 0 entonces
            costo_inventario ← costo_inventario + c_inv * inventario
        posicion ← inventario + pendientes
        Si politica = 1 entonces  ordenar ← (t mod 8 = 0)
        Sino                      ordenar ← (posicion ≤ 10) y (pendientes = 0)
        Si ordenar y posicion < nivel_objetivo entonces
            q ← nivel_objetivo − posicion
            Generar R;  L ← ValorPoisson(R, tabla_entrega)
            llegadas[t + 1 + L] ← llegadas[t + 1 + L] + q
            pendientes ← pendientes + q
            costo_ordenar ← costo_ordenar + c_ord
    costo_total ← costo_inventario + costo_faltante + costo_ordenar
```

Puntos delicados que **no** debes cambiar:
- El inventario puede volverse negativo; su valor absoluto son las unidades pendientes de
  entregar, que se surten solas al sumar la siguiente llegada.
- El costo de faltante se cobra una sola vez, el día en que se produce.
- El costo de mantener se cobra solo si el inventario de cierre es positivo.
- En la política 2 no se coloca orden si ya hay una pendiente. En la política 1 sí se ordena
  por calendario, siempre que la posición esté por debajo del objetivo.
- El arreglo de llegadas se dimensiona a `400` para tolerar `t + 1 + L` con `t = 365` y
  `L = 8`.

**Valores de referencia (5.000 corridas), tolerancia ±1 %:**

| Indicador | Política 1 (cada 8 días) | Política 2 (punto de reorden) |
|---|---|---|
| Costo de mantener inventario | $3.235 | $3.575 |
| Costo de faltante | $1.924 | $1.267 |
| Costo de ordenar | $2.250 | $2.528 |
| **Costo total anual** | **$7.409,04** | **$7.370,91** |
| Costo promedio por día | $20,30 | $20,19 |
| Órdenes por año | 45,00 | 50,56 |
| Unidades faltantes por año | 192,43 | 126,75 |

**La política 2 debe resultar la más económica.** Criterio de aceptación.

**Panel.**
- Parámetros: política (selector de dos opciones, más un modo "Comparar ambas" que corre las
  dos y las muestra lado a lado), número de corridas, horizonte, nivel objetivo, punto de
  reorden, periodo de revisión, los tres costos, semilla.
- Modo "Corrida visual" de 12 a 30 días con tabla `dia | inv. inicial | llegadas | R's |
  demanda | faltante | inv. final | posición | ¿ordena? | q | L | llega el día`.
- Resultados: tabla comparativa de las dos políticas con las siete filas de arriba. La
  columna ganadora se marca con el borde izquierdo ámbar; el costo total ganador en `VERDE`.
- `GraficaBarrasApiladas`: dos barras (una por política) apiladas en costo de inventario,
  faltante y ordenar. Es la gráfica que el informe pide.
- Segunda gráfica, `GraficaLineas`: evolución del inventario día a día de la corrida visual,
  con la zona negativa sombreada en `ROJO` y la línea del punto de reorden punteada. Muestra
  el dientes de sierra característico y es muy explicativa.

---

### 7.6 Ejercicio 6 — Reemplazo de componentes

**Enunciado.** Equipo con 4 componentes idénticos. Vida de un componente
`Normal(600 ; 100)` horas. Reemplazar uno cuesta 1 hora de paro; reemplazar los cuatro,
2 horas. Componente nuevo `$200`; hora de paro `$100`. Horizonte 20.000 horas. Comparar:
política A, reemplazar solo el que falla; política B, reemplazar los cuatro cuando falla
cualquiera.

**Generación de vida.** Teorema del límite central, doce uniformes:
`v = 600 + 100·(ΣR − 6)`. Acota a valores positivos (con estos parámetros es prácticamente
imposible, pero el informe lo declara como supuesto).

**Pseudocódigo normativo.**

```
horizonte ← 20000; media ← 600; desv ← 100
costo_componente ← 200; costo_hora ← 100
Para cada corrida:
    reloj ← 0; componentes ← 0; horas_paro ← 0; intervenciones ← 0
    Para i = 1 hasta 4:  falla[i] ← GenerarVidaNormal(media, desv)
    Repetir:
        m ← indice del menor valor de falla[1..4]
        Si falla[m] > horizonte entonces salir
        reloj ← falla[m]
        intervenciones ← intervenciones + 1
        Si politica = A entonces
            componentes ← componentes + 1
            horas_paro  ← horas_paro + 1
            Para i = 1 hasta 4:  falla[i] ← falla[i] + 1
            falla[m] ← reloj + 1 + GenerarVidaNormal(media, desv)
        Sino
            componentes ← componentes + 4
            horas_paro  ← horas_paro + 2
            Para i = 1 hasta 4:  falla[i] ← reloj + 2 + GenerarVidaNormal(media, desv)
    costo_total ← costo_componente * componentes + costo_hora * horas_paro
```

Detalle crítico de la política A: **primero se desplaza una hora el instante de falla de los
cuatro componentes** (porque durante la desconexión el equipo no opera y los componentes no
envejecen) y **después** se reprograma el componente reemplazado con `reloj + 1 + vida`. Ese
orden importa y está así en el informe. No lo "simplifiques".

**Valores de referencia (5.000 corridas), tolerancia ±1 %:**

| Indicador | Política A (individual) | Política B (los 4) |
|---|---|---|
| Intervenciones por corrida | 130,52 | 39,59 |
| Componentes consumidos | 130,52 | 158,36 |
| Horas de desconexión | 130,52 | 79,18 |
| Costo en componentes | $26.104 | $31.673 |
| Costo por desconexión | $13.052 | $7.918 |
| **Costo total de 20.000 horas** | **$39.156,60** | **$39.591,00** |
| Costo por hora de operación | $1,96 | $1,98 |

**La política A debe resultar la más económica.** Criterio de aceptación. Referencias
adicionales para validar: 4 componentes con vida media 600 h producen unas 133 fallas en
20.000 h; el mínimo de cuatro normales tiene esperanza cercana a 497 h, lo que da unos
40 ciclos.

**Panel.**
- Parámetros: política (con modo "Comparar ambas"), número de corridas, horizonte, media y
  desviación de vida, costos, horas de paro por tipo de intervención, semilla.
- Modo "Corrida visual" de 5 a 10 eventos: `Bitacora` con el evento, el componente que falla,
  su vida generada, el instante de falla, las horas de paro y el costo acumulado.
- Resultados: tabla comparativa con las siete filas de arriba.
- `GraficaBarrasApiladas`: costo en componentes contra costo por desconexión, una barra por
  política. Es la gráfica que pide el informe y muestra con claridad el intercambio: la
  política B ahorra en paros lo que pierde en componentes.
- Gráfica secundaria opcional: línea de tiempo de los primeros 3.000 h con una marca vertical
  por intervención, ámbar para A y azul para B superpuestas, que hace evidente la diferencia
  de frecuencia.

---

## 8. Componentes de interfaz compartidos

- **`TablaTecnica`**: `JTable` con `TableCellRenderer` propio. Sin bordes verticales; una
  línea horizontal de 1 px en `RETICULA` cada fila; cabecera en `SUPERFICIE_ALTA` con texto
  en `TINTA_SUAVE`; números alineados a la derecha en monoespaciada; filas alternas **sin**
  franjas de color (usa el espaciado para separar, no el zebra striping). Altura de fila 26 px.
- **`TarjetaMetrica`**: etiqueta pequeña arriba en `TINTA_SUAVE`, valor grande en
  monoespaciada, y una línea inferior de 2 px cuyo color codifica el estado. Sin sombras.
- **`BotonAccion`**: dos variantes, primaria (fondo `AMBAR`, texto `FONDO`) y secundaria
  (contorno de 1 px en `RETICULA`, texto `TINTA`). Estados hover, presionado, deshabilitado y
  foco, todos dibujados a mano.
- **`Bitacora`**: `JTextPane` monoespaciado con estilos por color para marcar aceptado,
  rechazado, orden colocada, faltante, etc. Autoscroll al final, con tope de líneas.
- **`Grafica`** (base): márgenes, ejes con marcas y etiquetas legibles, retícula tenue,
  título opcional, `exportarPNG(File)` mediante `BufferedImage` a 2× para que las capturas
  del informe salgan nítidas. **Todas las gráficas deben ser exportables**, porque el informe
  tiene huecos explícitos donde insertarlas.
- **Formato de números**: crea `Formatos` con `NumberFormat` de locale `es-BO`: separador de
  miles con punto, decimal con coma, dos decimales para dinero, cuatro para probabilidades.
  Todas las cifras visibles pasan por ahí, para que coincidan con la notación del informe.

---

## 9. Política de comentarios y marcado de funciones clave

El autor del informe pidió explícitamente: **nada de párrafos de comentarios**. Aplica esta
política sin excepciones.

1. Cada clase lleva un encabezado de **3 a 5 líneas** que dice qué modela y, cuando
   corresponde, a qué sección del informe pertenece. Ejemplo:

   ```java
   /**
    * Simula un ano de operacion de la flota de renta y agrega los resultados
    * de multiples corridas independientes.
    * Informe: seccion 5.5 (Aplicacion 1 - Renta de autos).
    */
   ```

2. **Nada de Javadoc en getters, setters ni constructores triviales.** Si el nombre del método
   ya lo dice, no lo comentes.

3. Comentarios inline solo donde la razón no sea evidente del código: la dimensión `370` del
   arreglo de retornos, el orden del desplazamiento de horas en la política A, la convención
   de intervalos de la transformada inversa, la advertencia sobre bits bajos del generador.

4. **Marcado de funciones clave.** Los métodos que el autor va a llevar al informe se marcan
   con este bloque exacto, tres líneas, sin más:

   ```java
   // ═══ CLAVE 5.5.10 · Algoritmo de resolucion — Renta de autos ═════════════
   // Implementa el pseudocodigo del punto 5.5.12 del informe.
   // ═════════════════════════════════════════════════════════════════════════
   public ResultadoAnioRenta simularAnio(int flota) { ... }
   ```

   Los métodos marcados obligatoriamente son, como mínimo:

   | Marca | Clase | Método |
   |---|---|---|
   | `CLAVE 6.1` | `GeneradorCongruencialMixto` | `siguiente()` |
   | `CLAVE 6.2a` | `GeneradorVariables` | `discretaPorTransformadaInversa(...)` |
   | `CLAVE 6.2b` | `GeneradorVariables` | `normalPorTeoremaLimiteCentral(...)` |
   | `CLAVE 6.2c` | `GeneradorVariables` | `binomialPorEnsayosBernoulli(...)` |
   | `CLAVE 5.1` | `SimuladorRechazo` | `simular(int iteraciones)` |
   | `CLAVE 5.1b` | `DensidadPorTramos` | `evaluar(double x)` |
   | `CLAVE 5.5` | `SimuladorRenta` | `simularAnio(int flota)` |
   | `CLAVE 5.5b` | `SimuladorRenta` | `evaluarFlotas(int min, int max, int corridas)` |
   | `CLAVE 5.6` | `SimuladorInterferencia` | `simular(int ensambles)` |
   | `CLAVE 5.6b` | `SimuladorInterferencia` | `calcularTamanioMuestra(...)` |
   | `CLAVE 5.7` | `SimuladorInventario` | `simularCorrida(PoliticaInventario)` |
   | `CLAVE 5.8` | `SimuladorReemplazo` | `simularCorrida(PoliticaReemplazo)` |

5. **Los métodos marcados deben ser autocontenidos y legibles en una pantalla.** Si uno supera
   las 60 líneas, extrae submétodos con nombres del dominio (`atenderDemanda`,
   `programarRetornos`, `evaluarReabastecimiento`) y márcalos también. Que se puedan pegar en
   el informe sin recortar es un requisito de diseño del código, no un detalle estético.

---

## 10. Documentación generada y exportaciones

### 10.1 `docs/funciones-clave.md`

Genera este archivo al final. Para cada marca `CLAVE`, incluye: el identificador, la sección
del informe a la que corresponde, la ruta del archivo, el número de línea y el código
completo del método en un bloque `java`. Va ordenado por sección del informe, de modo que el
autor recorra el archivo de arriba abajo y vaya pegando en los puntos 5.3, 5.9.1, 5.9.2,
5.9.3 y 5.9.4, que están marcados como pendientes en el informe.

Añade, antes de cada bloque de código, **dos o tres frases** que describan qué hace el método
en lenguaje de informe (no de programador): útil para que el autor las adapte como texto
propio. No escribas párrafos largos.

### 10.2 `docs/guia-de-uso.md`

Cómo abrir el proyecto en VS Code, cómo ejecutarlo, qué hace cada panel, y dónde caen los
archivos exportados.

### 10.3 `README.md`

Corto: qué es, requisitos, cómo ejecutar, estructura de carpetas en una lista, y la tabla de
los seis ejercicios con su resultado esperado.

### 10.4 Exportaciones

Cada panel tiene un botón "Exportar" que escribe en `salidas/`:

- `ejercicioN_tabla_AAAAMMDD_HHMMSS.csv` con la tabla que está en pantalla, separador `;`,
  codificación UTF-8 con BOM para que Excel lo abra correcto.
- `ejercicioN_grafica_AAAAMMDD_HHMMSS.png` con la gráfica a 2×.

Después de exportar, muestra el nombre del archivo en la barra de estado durante 4 segundos.
Nada de diálogos modales de confirmación.

---

## 11. Verificación automática

`tss.verificacion.Verificacion` tiene su propio `main`, corre sin interfaz gráfica y es la
prueba de que la implementación es correcta. Debe:

1. Reproducir las cinco iteraciones de la prueba de escritorio de los ejercicios 1 y 2
   inyectando una `FuenteAleatoria` de secuencia fija, y comparar contra las tablas de la
   sección 7.1 y 7.2 de este documento.
2. Correr 100.000 iteraciones de cada método de rechazo y comprobar las tasas de aceptación
   `0,6667` y `0,80` con tolerancia `0,005`.
3. Correr el barrido de flotas del ejercicio 3 con 5.000 corridas y comprobar que el óptimo
   es `N = 5` y que la utilidad está dentro de ±2 % de `93.455`.
4. Correr el ejercicio 4 con 8.677 ensambles y comprobar `|p − 0,34458| < 0,01`, y que
   `calcularTamanioMuestra(0,34458 ; 0,01 ; 1,96) == 8677`.
5. Correr el ejercicio 5 con 5.000 corridas por política y comprobar que la política 2 gana y
   que ambos costos totales están dentro de ±1 % de `7.409,04` y `7.370,91`.
6. Correr el ejercicio 6 con 5.000 corridas por política y comprobar que la política A gana y
   que los costos están dentro de ±1 % de `39.156,60` y `39.591,00`.

Salida: una tabla en consola con `PRUEBA | ESPERADO | OBTENIDO | DESVIACIÓN | ESTADO`, y un
código de salida distinto de cero si alguna prueba falla. **Ejecútala tú mismo antes de
declarar terminado el trabajo** y pega el resultado en el mensaje final.

Si alguna referencia no se alcanza, **el error está en el código, no en el informe**.
Revisa el pseudocódigo línea por línea antes de tocar las tolerancias. Nunca ajustes una
tolerancia para que una prueba pase.

---

## 12. Plan de trabajo por fases

Trabaja en este orden y compila al final de cada fase.

**Fase 0 — Preparación.** Verifica `java -version`. Crea el árbol de carpetas y
`.vscode/settings.json`. Crea un `App.java` mínimo que abra una ventana vacía y confirma que
arranca.

**Fase 1 — Núcleo aleatorio.** `GeneradorCongruencialMixto`, `FuenteAleatoria`,
`GeneradorVariables`. Escribe un `main` temporal que compruebe la media y la varianza de
100.000 uniformes (deben dar `0,5` y `1/12`) y de 100.000 normales generadas por el TLC.

**Fase 2 — Modelo completo, sin interfaz.** Los cinco paquetes de simulación. Al terminar,
escribe `Verificacion` y **hazla pasar entera**. Hasta aquí no has escrito una línea de UI, y
eso es deliberado: si los números no salen, no tiene sentido pintar nada.

**Fase 3 — Sistema de diseño.** `Paleta`, `Tipografia`, `Medidas`, `Pinceles`, y los
componentes de `vista/componentes/`. Antes de codificar, redacta el plan de diseño que pide
la skill `frontend-design` (paleta, tipografía, layout, principios), critícalo contra los
antipatrones de la sección 4.3 y ajústalo. Construye una ventana de muestra que exhiba todos
los componentes juntos y revísala.

**Fase 4 — Cáscara de la aplicación.** `VentanaPrincipal`, `RailNavegacion`, `PanelEjercicio`
base, `CardLayout` con seis paneles vacíos pero ya con encabezado y anatomía correcta.

**Fase 5 — Gráficas.** La clase base y las cinco especializadas, probadas con datos ficticios
antes de conectarlas.

**Fase 6 — Paneles y controladores, uno por uno.** Orden sugerido: rechazo (cubre dos
ejercicios), interferencia, renta, inventario, reemplazo. Cada panel se termina y se prueba
antes de pasar al siguiente.

**Fase 7 — Exportación y documentación.** `ExportadorResultados`, `docs/funciones-clave.md`,
`docs/guia-de-uso.md`, `README.md`.

**Fase 8 — Pulido y autocrítica.** Ejecuta `Verificacion` de nuevo. Recorre la aplicación
panel por panel al tamaño mínimo de ventana. Aplica la regla de quitar un accesorio: elimina
el elemento decorativo que menos aporte. Revisa que ningún panel tarde en responder y que la
cancelación funcione.

---

## 13. Checklist final

Antes de entregar, confirma cada punto:

- [ ] `javac` compila sin advertencias en toda la fuente.
- [ ] La aplicación arranca desde VS Code con el botón Run sobre `App.java`.
- [ ] Cero dependencias externas; solo `java.*` y `javax.swing.*`.
- [ ] `Verificacion` pasa las seis pruebas y su salida está pegada en el mensaje final.
- [ ] Los seis ejercicios tienen su vista propia y funcionan de forma independiente.
- [ ] Ninguna corrida bloquea la interfaz; la barra de progreso avanza y cancelar funciona.
- [ ] Todos los números uniformes provienen del generador congruencial mixto.
- [ ] La misma semilla produce exactamente los mismos resultados en dos ejecuciones.
- [ ] Todas las gráficas exportan PNG y todas las tablas exportan CSV.
- [ ] `docs/funciones-clave.md` existe, tiene las doce marcas y el código está completo.
- [ ] Ningún método marcado como clave supera las 60 líneas.
- [ ] No hay bloques de comentarios de más de 5 líneas en ninguna clase.
- [ ] La interfaz no cae en ninguno de los antipatrones de la sección 4.3.
- [ ] Todos los textos visibles están en español, con tildes correctas.
- [ ] La ventana es usable a `1180 × 760`.

---

## 14. Decisiones ya tomadas

No preguntes por estas; están cerradas:

- Swing, no JavaFX. Sin FlatLaf ni ningún otro Look and Feel externo.
- Sin Maven ni Gradle. Carpeta de fuentes plana `src/`.
- Navegación en rail vertical izquierdo con seis entradas, implementada con `CardLayout`.
- Generador congruencial mixto propio con `a = 1664525`, `c = 1013904223`, `m = 2^32`.
- Paleta, tipografía y espaciado de la sección 4. Puedes refinar matices dentro de esa
  dirección, pero no cambiar el concepto.
- Nombres de clases y paquetes de la sección 3.
- Ejercicios 1 y 2 comparten simulador, panel y controlador, parametrizados por la densidad.

Sí conviene consultar al autor si:

- `java -version` reporta una versión anterior a Java 11.
- Alguna referencia numérica no se alcanza después de revisar el pseudocódigo dos veces.
- Detectas una inconsistencia real entre el pseudocódigo del informe y el enunciado original.
  En ese caso, señálala con precisión en lugar de resolverla por tu cuenta: el informe ya está
  entregado y cualquier cambio tiene que ser una decisión consciente del autor.
