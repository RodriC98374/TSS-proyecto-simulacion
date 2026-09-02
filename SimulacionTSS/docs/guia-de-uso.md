# Guía de uso

## Abrir y ejecutar

1. Abra la carpeta `SimulacionTSS` en Visual Studio Code (Archivo, Abrir carpeta).
2. Instale el *Extension Pack for Java* si aún no lo tiene. No hace falta nada más:
   el proyecto no usa Maven, Gradle ni bibliotecas externas.
3. Abra `src/tss/App.java` y pulse **Run** sobre el método `main`.

La configuración ya está en `.vscode/settings.json`: las fuentes están en `src` y las
clases compiladas van a `bin`.

### Desde la línea de órdenes

```
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin tss.App
```

En PowerShell:

```powershell
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp bin tss.App
```

### Comprobación automática

`tss.verificacion.Verificacion` corre sin interfaz gráfica y compara los resultados
contra los valores publicados en el informe:

```
java -cp bin tss.verificacion.Verificacion
```

Imprime una tabla con `PRUEBA | ESPERADO | OBTENIDO | DESVIACIÓN | ESTADO` y termina con
código distinto de cero si alguna comprobación falla.

## La ventana

A la izquierda hay un rail con los seis ejercicios. Al pie del rail se lee el estado del
generador congruencial mixto: sus constantes, la semilla en uso y cuántos números
uniformes se consumieron en la última corrida. Los seis ejercicios beben de ese único
generador.

Todos los paneles tienen la misma anatomía: título y pregunta arriba, parámetros a la
izquierda, resultados a la derecha y barra de estado abajo.

- **Enter** en cualquier campo de parámetros lanza el experimento.
- **Tab** recorre los controles; el foco se ve con un anillo ámbar.
- Un campo fuera de rango se marca en rojo con el rango esperado bajo el campo, y el
  botón principal queda deshabilitado hasta corregirlo.
- Toda corrida larga va en segundo plano: la barra de progreso avanza y **Cancelar**
  la detiene sin cerrar nada.

El color siempre significa lo mismo: ámbar es lo generado o la acción del usuario, verde
es aceptado o ahorro, rojo es rechazado, faltante o costo.

## Qué hace cada panel

### Ejercicio 1 y Ejercicio 2 — Método de rechazo

Generan valores de una densidad definida por tramos. El ejercicio 1 usa dos escalones
sobre `[0, 2]`; el ejercicio 2, un escalón y una rampa descendente sobre `[0, 5/3]`.

- **Generar muestra** corre el número de iteraciones indicado.
- **Prueba de escritorio** reproduce las cinco iteraciones tabuladas en el informe
  inyectando los mismos valores de R1 y R2, para comparar renglón por renglón.
- La gráfica superior es la que explica el método: `f(x)` en trazo grueso, el rectángulo
  envolvente `M × (b − a)` en punteado y cada iteración como un punto en `(x, R2·M)`.
- La gráfica inferior compara la muestra aceptada con la densidad teórica.

Con 100.000 iteraciones la tasa de aceptación debe acercarse a 0,6667 en el ejercicio 1
y a 0,8000 en el ejercicio 2.

### Ejercicio 3 — Renta de autos

- **Ejecutar experimento** barre los tamaños de flota del rango indicado y promedia el
  número de corridas pedido. La fila del óptimo queda resaltada y su utilidad en verde.
- **Corrida visual** simula un solo año con la flota indicada y vuelca el detalle día a
  día en la bitácora.
- La gráfica marca con un círculo ámbar el tamaño de flota que maximiza la utilidad.

Con 5.000 corridas el óptimo es una flota de cinco autos.

### Ejercicio 4 — Interferencia eje-cojinete

- **Ejecutar experimento** genera el número de ensambles indicado y añade una fila a la
  tabla, de modo que se pueden acumular varios tamaños de muestra y compararlos.
- **Corrida visual** muestra en la bitácora los doce uniformes de cada pieza, los valores
  tipificados, los diámetros, la holgura y el veredicto.
- La gráfica de convergencia lleva el eje horizontal en escala logarítmica, la referencia
  analítica 0,34458 punteada, la banda de ±0,01 sombreada y una marca vertical en 8.677.
- El histograma de la holgura pinta en rojo el área negativa, que es la probabilidad
  buscada.

### Ejercicio 5 — Política de inventario

- El selector elige una política o **Comparar**, que corre las dos y las enfrenta.
- **Corrida visual** vuelca los primeros días con la demanda, el faltante, la posición del
  inventario y las órdenes colocadas, y dibuja el diente de sierra del inventario.
- La columna ganadora lleva una guía ámbar y su costo total va en verde.

Con 5.000 corridas la política 2, de punto de reorden, resulta la más económica.

### Ejercicio 6 — Reemplazo de componentes

- El selector elige una política o **Comparar**.
- **Corrida visual** lista los primeros eventos de falla con la vida generada, el instante,
  las horas de paro y el costo acumulado, y dibuja las intervenciones acumuladas de ambas
  políticas: la pendiente de cada línea es su frecuencia de intervención.

Con 5.000 corridas la política A, de reemplazo individual, resulta la más económica.

## Exportaciones

El botón **Exportar** de cada panel escribe en la carpeta `salidas/`:

- `ejercicioN_tabla_AAAAMMDD_HHMMSS.csv` con la tabla que está en pantalla. Separador
  punto y coma, codificación UTF-8 con marca de orden de bytes, para que Excel la abra
  con los acentos correctos.
- `ejercicioN_<gráfica>_AAAAMMDD_HHMMSS.png` con cada gráfica del panel al doble de
  resolución.

El nombre del archivo aparece durante cuatro segundos en la barra de estado. No hay
diálogos de confirmación.

## Reproducibilidad

Toda corrida parte de la semilla que indica el panel. La misma semilla produce exactamente
los mismos resultados en dos ejecuciones distintas. Dentro de un experimento, la corrida
número `c` usa la semilla `semillaBase + c × 7919`, de modo que el experimento completo es
reproducible y no sólo cada corrida por separado.
