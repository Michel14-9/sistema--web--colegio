package com.universidad.sistema_academico.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HtmlAnalyzerService {

    /**
     * Lee un archivo HTML
     */
    public String leerArchivoHtml(String nombreArchivo) throws Exception {
        ClassPathResource resource = new ClassPathResource("templates/" + nombreArchivo);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Analiza el HTML y extrae una guía básica (fallback)
     */
    public String analizarVista(String html, String nombreArchivo) {
        StringBuilder guia = new StringBuilder();
        guia.append("📋 **Guía para ").append(obtenerTitulo(html, nombreArchivo)).append("**\n\n");

        List<String> pasos = new ArrayList<>();
        int pasoNumero = 1;

        // Formularios
        if (html.contains("<form")) {
            String action = extraerAtributo(html, "form", "action");
            String method = extraerAtributo(html, "form", "method");
            pasos.add(pasoNumero++ + ". **Completa el formulario**" +
                    (action != null ? " (Envía a: " + action + ")" : "") +
                    (method != null ? " [Método: " + method + "]" : ""));
        }

        // Campos de entrada
        Pattern pattern = Pattern.compile("input\\s+[^>]*name=[\"']([^\"']+)[\"'][^>]*");
        Matcher matcher = pattern.matcher(html);
        List<String> campos = new ArrayList<>();
        while (matcher.find()) {
            String campo = matcher.group(1);
            if (!campo.isEmpty() && !campo.equals("_csrf")) {
                campos.add(campo);
            }
        }
        if (!campos.isEmpty()) {
            pasos.add(pasoNumero++ + ". **Ingresa:** " + String.join(", ", campos));
        }

        // Botones
        Pattern btnPattern = Pattern.compile("button[^>]*type=[\"']submit[\"'][^>]*>([^<]*)</button>");
        Matcher btnMatcher = btnPattern.matcher(html);
        List<String> botones = new ArrayList<>();
        while (btnMatcher.find()) {
            botones.add(btnMatcher.group(1).trim());
        }
        if (!botones.isEmpty()) {
            pasos.add(pasoNumero++ + ". **Haz clic en:** " + String.join(" o ", botones));
        }

        // Enlaces
        Pattern linkPattern = Pattern.compile("<a\\s+[^>]*href=[\"']([^\"']+)[\"'][^>]*>([^<]*)</a>");
        Matcher linkMatcher = linkPattern.matcher(html);
        List<String> enlaces = new ArrayList<>();
        while (linkMatcher.find()) {
            String href = linkMatcher.group(1);
            String texto = linkMatcher.group(2).trim();
            if (!href.contains("css") && !href.contains("js") && !href.startsWith("#") && texto.length() > 0) {
                enlaces.add(texto + " → " + href);
            }
        }
        if (!enlaces.isEmpty()) {
            pasos.add(pasoNumero++ + ". **Enlaces disponibles:** " + String.join(", ", enlaces));
        }

        for (String paso : pasos) {
            guia.append("🔹 ").append(paso).append("\n");
        }

        guia.append("\n📌 **Recuerda:** Sigue estos pasos para completar el proceso correctamente.");
        guia.append("\n🔗 **Volver al inicio:** [Inicio](/)");

        return guia.toString();
    }

    private String extraerAtributo(String html, String tag, String atributo) {
        Pattern pattern = Pattern.compile("<" + tag + "\\s+[^>]*" + atributo + "=[\"']([^\"']+)[\"'][^>]*>");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String obtenerTitulo(String html, String nombreArchivo) {
        Pattern pattern = Pattern.compile("<title[^>]*>(.*?)</title>");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return nombreArchivo.replace(".html", "").replace("_", " ").toUpperCase();
    }
}