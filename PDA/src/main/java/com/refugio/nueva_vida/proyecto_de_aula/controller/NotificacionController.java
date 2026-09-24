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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/admin/notificaciones")
    public String notificaciones(Model model) {

        List<Notificacion> notificaciones = recetaService.listarNotificacionesNoLeidas();

        // Parsear el JSON de productos sugeridos a una lista de Map
        for (Notificacion noti : notificaciones) {
            if (noti.getReceta() != null && noti.getReceta().getProductosSugeridos() != null) {
                try {
                    String json = noti.getReceta().getProductosSugeridos();
                    List<Map<String, Object>> productos = objectMapper.readValue(
                            json,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );
                    noti.getReceta().setProductosParseados(productos);
                } catch (Exception e) {
                    log.warn("Error parseando productos: {}", e.getMessage());
                }
            }
        }

        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("totalNoLeidas", recetaService.contarNoLeidas());
        return "privilegiado/notificaciones-adminview";
    }

    @PostMapping("/admin/notificaciones/{id}/leer")
    public String marcarLeida(@PathVariable Long id, RedirectAttributes ra) {
        try {
            recetaService.marcarComoLeida(id);
            ra.addFlashAttribute("mensajeExito", "✅ Notificación marcada como leída.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensajeError", "Error: " + e.getMessage());
        }
        return "redirect:/admin/notificaciones";
    }

    @PostMapping("/admin/notificaciones/{id}/descartar")
    public String descartar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            recetaService.descartarReceta(id);
            ra.addFlashAttribute("mensajeExito", "🗑️ Receta descartada.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensajeError", "Error: " + e.getMessage());
        }
        return "redirect:/admin/notificaciones";
    }

    @PostMapping("/admin/notificaciones/generar-todas")
    public String generarTodas(RedirectAttributes ra) {
        try {
            recetaService.generarRecetasParaTodos();
            ra.addFlashAttribute("mensajeExito", "🔄 Recetas regeneradas para todos los perros.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensajeError", "Error: " + e.getMessage());
        }
        return "redirect:/admin/notificaciones";
    }
}