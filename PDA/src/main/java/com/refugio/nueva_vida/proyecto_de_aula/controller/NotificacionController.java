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

                for (Map<String, Object> producto : productos) {

                    String categoria =
                            categoriaProducto(producto);

                    producto.put(
                            "categoria",
                            categoria
                    );

                    producto.put(
                            "accion",
                            accionParaCategoria(categoria)
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
    // CREAR / ACTUALIZAR RECETA CON PRODUCTOS
    // =========================================================

    @PostMapping(
            "/admin/notificaciones/{id}/crear-receta"
    )
    public String crearRecetaConProductos(
            @PathVariable Long id,
            @RequestParam String productosSeleccionados,
            RedirectAttributes ra) {

        try {

            recetaService.actualizarProductosSugeridos(
                    id,
                    productosSeleccionados
            );

            ra.addFlashAttribute(
                    "mensajeExito",
                    "📝 Receta actualizada con los productos seleccionados."
            );

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
            String categoria) {

        return switch (categoria) {

            case "HIGIENE" ->
                    "🧴 Bañar a Sebass";

            case "ACCESORIO" ->
                    "🎒 Equipar a Sebass";

            case "ALIMENTO" ->
                    "🍖 Dar a Sebass";

            case "VACUNA" ->
                    "💉 Dar a Sebass";

            case "MEDICAMENTO" ->
                    "💊 Dar a Sebass";

            case "DESPARASITANTE" ->
                    "🪱 Dar a Sebass";

            case "HIDRATACION" ->
                    "💧 Dar a Sebass";

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