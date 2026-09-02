# Simulación de sistemas — Actividad 1

Aplicación de escritorio en Java que implementa los seis ejercicios de la Actividad 1 de
Taller de Simulación de Sistemas. Una sola ventana, un panel por ejercicio, y un único
generador congruencial mixto del que salen todos los números aleatorios.

## Requisitos

- **Java 17 o superior.** Desarrollado y probado con **Java 21.0.10 LTS**.
- Visual Studio Code con el *Extension Pack for Java*.
- **Cero dependencias externas.** Sólo `java.*` y `javax.swing.*`; nada de Maven, Gradle,
  FlatLaf, JFreeChart ni JavaFX.

## Cómo ejecutar

Abrir la carpeta en VS Code, abrir `src/tss/App.java` y pulsar **Run**.

Desde la terminal:

```powershell
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp bin tss.App
```

Comprobación automática sin interfaz gráfica:

```powershell
java -cp bin tss.verificacion.Verificacion
```

## Estructura

- `src/tss/App.java` — punto de entrada.
- `src/tss/modelo/` — lógica de simulación pura, sin nada de Swing ni AWT.
  - `aleatorio/` — generador congruencial mixto y variables aleatorias derivadas.
  - `comun/` — resumen estadístico y contrato de avance y cancelación.
  - `rechazo/`, `renta/`, `interferencia/`, `inventario/`, `reemplazo/` — un paquete por
    modelo, con sus parámetros, su simulador y sus objetos de resultado inmutables.
- `src/tss/controlador/` — validación, `SwingWorker` genérico y exportación.
- `src/tss/vista/` — ventana, rail de navegación, paneles, componentes, gráficas y sistema
  de diseño.
- `src/tss/verificacion/Verificacion.java` — autocomprobación con `main` propio.
- `docs/` — `funciones-clave.md` (código listo para pegar en el informe) y `guia-de-uso.md`.
- `salidas/` — destino de los CSV y los PNG exportados.
- `bin/` — clases compiladas; no se versiona.

## Los seis ejercicios y su resultado esperado

| # | Ejercicio | Sección | Resultado esperado |
|---|---|---|---|
| 1 | Método de rechazo, densidad escalonada | 5.1 | Tasa de aceptación 0,6667 con 100.000 iteraciones |
| 2 | Método de rechazo, escalón y rampa | 5.2 | Tasa de aceptación 0,8000 con 100.000 iteraciones |
| 3 | Renta de autos | 5.5 | Flota óptima N = 5, utilidad ≈ Bs. 93.455 |
| 4 | Interferencia eje-cojinete | 5.6 | p ≈ 0,3446 y n = 8.677 ensambles requeridos |
| 5 | Política de inventario | 5.7 | Gana la política 2, con ≈ $7.371 anuales |
| 6 | Reemplazo de componentes | 5.8 | Gana la política A, con ≈ $39.157 por 20.000 horas |

Los valores de referencia son los publicados en el informe. `Verificacion` los comprueba
con las tolerancias que el propio informe fija para cada ejercicio.

## Notas de implementación

- Todos los uniformes vienen del generador congruencial mixto con `a = 1664525`,
  `c = 1013904223` y `m = 2³²`. No se usa `Math.random()` ni `java.util.Random` en la
  lógica de simulación.
- Cada corrida `c` de un experimento parte de la semilla `semillaBase + c × 7919`, de modo
  que un experimento completo es reproducible.
- Ninguna corrida se ejecuta en el hilo de interfaz: todas van en un `SwingWorker` con
  barra de progreso y cancelación.
