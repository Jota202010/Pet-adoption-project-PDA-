package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.refugio.nueva_vida.proyecto_de_aula.model.Notificacion;
import com.refugio.nueva_vida.proyecto_de_aula.service.RecetaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificacionController {

    private final RecetaService recetaService;

    private final ObjectMapper objectMapper =
            new ObjectMapper();


    @GetMapping("/admin/notificaciones")
    public String notificaciones(Model model) {

        List<Notificacion> notificaciones =
                recetaService.listarNotificacionesNoLeidas();

        for (Notificacion noti : notificaciones) {

            if (noti.getReceta() == null) {
                continue;
            }

            String json =
                    noti.getReceta()
                            .getProductosSugeridos();

            if (json == null || json.isBlank()) {
                continue;
            }

            try {

                List<Map<String, Object>> productos =
                        objectMapper.readValue(
                                json,
                                new TypeReference<
                                        List<Map<String, Object>>>() {}
                        );

                // Nombre real del perro al que pertenece la receta
                String nombrePerro = "el perro";

                if (noti.getReceta().getPerro() != null
                        && noti.getReceta().getPerro().getNombre() != null
                        && !noti.getReceta().getPerro().getNombre().isBlank()) {

                    nombrePerro =
                            noti.getReceta().getPerro().getNombre();
                }

                for (Map<String, Object> producto : productos) {

                    String categoria =
                            categoriaProducto(producto);

                    producto.put(
                            "categoria",
                            categoria
                    );

                    producto.put(
                            "accion",
                            accionParaCategoria(categoria, nombrePerro)
                    );

                    producto.put(
                            "icono",
                            iconoParaCategoria(categoria)
                    );
                }

                noti.getReceta()
                        .setProductosParseados(productos);

            } catch (Exception e) {

                log.warn(
                        "Error parseando productos: {}",
                        e.getMessage()
                );
            }
        }

        model.addAttribute(
                "notificaciones",
                notificaciones
        );

        model.addAttribute(
                "totalNoLeidas",
                recetaService.contarNoLeidas()
        );

        return "privilegiado/notificaciones-adminview";
    }


    // =========================================================
    // CREAR RECETA Y ENVIARLA A LA TIENDA
    // (si la tienda ya la tiene, avisa; si no, la crea de nuevo)
    // =========================================================

    @PostMapping(
            "/admin/notificaciones/{id}/crear-receta"
    )
    public String crearRecetaConProductos(
            @PathVariable Long id,
            @RequestParam(
                    defaultValue = "[]"
            ) String productosSeleccionados,
            RedirectAttributes ra) {

        try {

            RecetaService.ResultadoEnvioTienda resultado =
                    recetaService.crearRecetaYEnviarATienda(
                            id,
                            productosSeleccionados
                    );

            switch (resultado) {

                case ENVIADA ->
                        ra.addFlashAttribute(
                                "mensajeExito",
                                "📨 Receta enviada a la tienda. "
                                + "Ya aparece en sus notificaciones."
                        );

                case YA_EXISTE ->
                        ra.addFlashAttribute(
                                "mensajeExito",
                                "🐾 Esta receta ya está en las "
                                + "notificaciones de la tienda, "
                                + "no hace falta enviarla otra vez."
                        );

                case SIN_RESPUESTA ->
                        ra.addFlashAttribute(
                                "mensajeError",
                                "⚠️ La tienda no respondió. "
                                + "Revisa que el servicio de tienda "
                                + "esté encendido e inténtalo de nuevo."
                        );
            }

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "mensajeError",
                    "❌ " + e.getMessage()
            );
        }

        return "redirect:/admin/notificaciones";
    }


    @PostMapping(
            "/admin/notificaciones/{id}/leer"
    )
    public String marcarLeida(
            @PathVariable Long id,
            RedirectAttributes ra) {

        try {

            recetaService.marcarComoLeida(id);

            ra.addFlashAttribute(
                    "mensajeExito",
                    "✅ Notificación marcada como leída."
            );

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "mensajeError",
                    "Error: " + e.getMessage()
            );
        }

        return "redirect:/admin/notificaciones";
    }


    @PostMapping(
            "/admin/notificaciones/{id}/descartar"
    )
    public String descartar(
            @PathVariable Long id,
            RedirectAttributes ra) {

        try {

            recetaService.descartarReceta(id);

            ra.addFlashAttribute(
                    "mensajeExito",
                    "❌ Receta descartada."
            );

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "mensajeError",
                    "Error: " + e.getMessage()
            );
        }

        return "redirect:/admin/notificaciones";
    }


    @PostMapping(
            "/admin/notificaciones/generar-todas"
    )
    public String generarTodas(
            RedirectAttributes ra) {

        try {

            recetaService.generarRecetasParaTodos();

            ra.addFlashAttribute(
                    "mensajeExito",
                    "🔄 Recetas generadas automáticamente."
            );

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "mensajeError",
                    "Error: " + e.getMessage()
            );
        }

        return "redirect:/admin/notificaciones";
    }


    // =========================================================
    // CATEGORÍAS
    // =========================================================

    private String categoriaProducto(
            Map<String, Object> producto) {

        Object categoria =
                producto.get("categoria");

        if (categoria != null
                && !categoria.toString().isBlank()) {

            return categoria
                    .toString()
                    .toUpperCase();
        }

        String nombre =
                String.valueOf(
                        producto.get("producto")
                ).toLowerCase();

        if (nombre.contains("shampoo")
                || nombre.contains("jabón")
                || nombre.contains("jabon")
                || nombre.contains("limpieza")) {

            return "HIGIENE";
        }

        if (nombre.contains("correa")
                || nombre.contains("pelota")
                || nombre.contains("collar")
                || nombre.contains("juguete")) {

            return "ACCESORIO";
        }

        if (nombre.contains("vacuna")) {
            return "VACUNA";
        }

        if (nombre.contains("desparasitante")) {
            return "DESPARASITANTE";
        }

        if (nombre.contains("suero")
                || nombre.contains("agua")) {

            return "HIDRATACION";
        }

        if (nombre.contains("concentrado")
                || nombre.contains("comida")
                || nombre.contains("alimento")) {

            return "ALIMENTO";
        }

        if (nombre.contains("medicamento")
                || nombre.contains("medicina")) {

            return "MEDICAMENTO";
        }

        if (nombre.contains("esteriliz")
                || nombre.contains("cirugía")
                || nombre.contains("cirugia")) {

            return "ESTERILIZACION";
        }

        return "OTRO";
    }


    private String accionParaCategoria(
            String categoria,
            String nombrePerro) {

        return switch (categoria) {

            case "HIGIENE" ->
                    "🧴 Bañar a " + nombrePerro;

            case "ACCESORIO" ->
                    "🎒 Equipar a " + nombrePerro;

            case "ALIMENTO" ->
                    "🍖 Dar a " + nombrePerro;

            case "VACUNA" ->
                    "💉 Dar a " + nombrePerro;

            case "MEDICAMENTO" ->
                    "💊 Dar a " + nombrePerro;

            case "DESPARASITANTE" ->
                    "🪱 Dar a " + nombrePerro;

            case "HIDRATACION" ->
                    "💧 Dar a " + nombrePerro;

            case "ESTERILIZACION" ->
                    "✂️ Gestionar cirugía";

            default ->
                    "📋 Gestionar producto";
        };
    }


    private String iconoParaCategoria(
            String categoria) {

        return switch (categoria) {

            case "HIGIENE" -> "🧴";

            case "ACCESORIO" -> "🎒";

            case "ALIMENTO" -> "🥩";

            case "VACUNA" -> "💉";

            case "MEDICAMENTO" -> "💊";

            case "DESPARASITANTE" -> "🪱";

            case "HIDRATACION" -> "💧";

            case "ESTERILIZACION" -> "✂️";

            default -> "📦";
        };
    }
}