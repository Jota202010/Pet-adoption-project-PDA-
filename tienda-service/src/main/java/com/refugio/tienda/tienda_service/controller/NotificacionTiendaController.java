package com.refugio.tienda.tienda_service.controller;

import com.refugio.tienda.tienda_service.model.NotificacionTienda;
import com.refugio.tienda.tienda_service.repository.NotificacionTiendaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/notificaciones-tienda")
@RequiredArgsConstructor
public class NotificacionTiendaController {

    private final NotificacionTiendaRepository notificacionRepository;

    // ═════════════════════════════════════════════════════
    // LISTAR
    // ═════════════════════════════════════════════════════

    @GetMapping
    public String listar(Model model) {

        List<NotificacionTienda> notificaciones =
                notificacionRepository.findAllByOrderByFechaDesc();

        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("totalNoLeidas",
                notificacionRepository.countByLeidaFalse());

        return "admin/notificaciones-tienda";
    }

    // ═════════════════════════════════════════════════════
    // MARCAR COMO LEÍDA
    // ═════════════════════════════════════════════════════

    @PostMapping("/{id}/leer")
    public String marcarLeida(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            NotificacionTienda noti = notificacionRepository.findById(id).orElseThrow();
            noti.setLeida(true);
            notificacionRepository.save(noti);
            ra.addFlashAttribute("mensajeExito", "✅ Notificación marcada como leída.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensajeError", "Error: " + e.getMessage());
        }
        return "redirect:/admin/notificaciones-tienda";
    }

    // ═════════════════════════════════════════════════════
    // ELIMINAR
    // ═════════════════════════════════════════════════════

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            notificacionRepository.deleteById(id);
            ra.addFlashAttribute("mensajeExito", "🗑️ Notificación eliminada.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensajeError", "Error: " + e.getMessage());
        }
        return "redirect:/admin/notificaciones-tienda";
    }

    // ═════════════════════════════════════════════════════
    // MARCAR TODAS COMO LEÍDAS
    // ═════════════════════════════════════════════════════

    @PostMapping("/leer-todas")
    public String leerTodas(RedirectAttributes ra) {
        try {
            List<NotificacionTienda> noLeidas =
                    notificacionRepository.findByLeidaFalseOrderByFechaDesc();
            for (NotificacionTienda n : noLeidas) {
                n.setLeida(true);
            }
            notificacionRepository.saveAll(noLeidas);
            ra.addFlashAttribute("mensajeExito", "✅ Todas marcadas como leídas.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensajeError", "Error: " + e.getMessage());
        }
        return "redirect:/admin/notificaciones-tienda";
    }
}