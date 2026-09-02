package tss.controlador;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import tss.vista.componentes.TablaTecnica;
import tss.vista.graficas.Grafica;

/**
 * Escribe en la carpeta salidas la tabla que esta en pantalla y las graficas
 * del panel. El CSV lleva marca de orden de bytes para que Excel lo abra con
 * los acentos correctos.
 */
public final class ExportadorResultados {

    private static final DateTimeFormatter SELLO =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path carpeta;

    public ExportadorResultados() {
        this(Path.of(System.getProperty("user.dir"), "salidas"));
    }

    public ExportadorResultados(Path carpeta) {
        this.carpeta = carpeta;
    }

    public Path exportarTabla(String clave, TablaTecnica tabla) throws IOException {
        Files.createDirectories(carpeta);
        Path destino = carpeta.resolve(clave + "_tabla_" + sello() + ".csv");
        try (Writer escritor = new OutputStreamWriter(
                Files.newOutputStream(destino), StandardCharsets.UTF_8)) {
            escritor.write('﻿');
            escritor.write(unir(tabla.titulos()));
            escritor.write(System.lineSeparator());
            for (Object[] fila : tabla.filas()) {
                escritor.write(unir(fila));
                escritor.write(System.lineSeparator());
            }
        }
        return destino;
    }

    public Path exportarGrafica(String clave, String nombre, Grafica grafica) throws IOException {
        Files.createDirectories(carpeta);
        Path destino = carpeta.resolve(clave + "_" + nombre + "_" + sello() + ".png");
        grafica.exportarPNG(destino.toFile());
        return destino;
    }

    public Path exportarTexto(String clave, String nombre, String contenido) throws IOException {
        Files.createDirectories(carpeta);
        Path destino = carpeta.resolve(clave + "_" + nombre + "_" + sello() + ".txt");
        Files.writeString(destino, contenido, StandardCharsets.UTF_8);
        return destino;
    }

    public static String resumenDeArchivos(List<Path> archivos) {
        if (archivos.isEmpty()) {
            return "No había nada que exportar.";
        }
        StringBuilder texto = new StringBuilder("Exportado a salidas/");
        texto.append(archivos.get(0).getFileName());
        if (archivos.size() > 1) {
            texto.append(" y ").append(archivos.size() - 1)
                    .append(archivos.size() == 2 ? " archivo más" : " archivos más");
        }
        return texto.toString();
    }

    private static String sello() {
        return LocalDateTime.now().format(SELLO);
    }

    private static String unir(Object[] valores) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                linea.append(';');
            }
            linea.append(escapar(String.valueOf(valores[i])));
        }
        return linea.toString();
    }

    private static String escapar(String valor) {
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return '"' + valor.replace("\"", "\"\"") + '"';
        }
        return valor;
    }
}
