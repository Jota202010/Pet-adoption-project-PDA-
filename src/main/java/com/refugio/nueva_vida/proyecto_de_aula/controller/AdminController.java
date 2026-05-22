package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.service.*;
import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final PerroService perroService;
    private final UsuarioService usuarioService;
    private final CitaService citaService;

    public AdminController(PerroService perroService, UsuarioService usuarioService,
                           CitaService citaService) {
        this.perroService  = perroService;
        this.usuarioService = usuarioService;
        this.citaService   = citaService;
    }

    // ── Panel principal ───────────────────────────────────────────────────────
    @GetMapping("/admin/panel")
    public String panelAdmin(Model model) {
        model.addAttribute("perros",          perroService.listarTodos());
        model.addAttribute("totalPerros",     perroService.contarTodos());
        model.addAttribute("usuarios",        usuarioService.listarTodos());
        model.addAttribute("totalUsuarios",   usuarioService.contarTodos());
        model.addAttribute("citas",           citaService.listarTodas());
        model.addAttribute("citasPendientes", citaService.contarPendientes());
        return "privilegiado/panel-general-adminview";
    }

    // ── Perfil del admin ──────────────────────────────────────────────────────
    @GetMapping("/admin/perfil")
    public String perfilAdmin(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario admin = usuarioService.buscarPorUsername(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("Admin no encontrado."));
        var todas       = citaService.listarTodas();
        long aprobadas  = todas.stream().filter(c -> c.getEstado() == Cita.EstadoCita.confirmada).count();
        long rechazadas = todas.stream().filter(c -> c.getEstado() == Cita.EstadoCita.rechazada).count();
        long espera     = todas.stream().filter(c -> c.getEstado() == Cita.EstadoCita.en_espera).count();
        model.addAttribute("admin",            admin);
        model.addAttribute("total_aprobadas",  aprobadas);
        model.addAttribute("total_rechazadas", rechazadas);
        model.addAttribute("total_espera",     espera);
        model.addAttribute("citasAprobadas",   todas.stream().filter(c -> c.getEstado() == Cita.EstadoCita.confirmada).toList());
        model.addAttribute("citasRechazadas",  todas.stream().filter(c -> c.getEstado() == Cita.EstadoCita.rechazada).toList());
        return "privilegiado/mi-perfil-adminview";
    }

    // ── Detalle de usuario ────────────────────────────────────────────────────
    @GetMapping("/admin/usuario/{id}")
    public String detalleUsuario(@PathVariable Integer id, Model model) {
        Usuario u = usuarioService.buscarPorId(id)
            .orElseThrow(() -> new IllegalStateException("Usuario con id " + id + " no encontrado."));
        model.addAttribute("usuario", u);
        model.addAttribute("citas",   citaService.citasDeUsuario(u));
        return "privilegiado/detalle-usuario-adminview";
    }

    // ── Pre-aprobar cita ──────────────────────────────────────────────────────
    @PostMapping("/admin/cita/{id}/pre-aprobar")
    public String preAprobar(@PathVariable Integer id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes ra) {
        try {
            Usuario admin = usuarioService.buscarPorUsername(userDetails.getUsername()).orElseThrow();
            citaService.preAprobar(id, admin);
            ra.addFlashAttribute("mensajeExito", "Solicitud pre-aprobada. El usuario podrá elegir su horario.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    // ── Rechazar cita ─────────────────────────────────────────────────────────
    @PostMapping("/admin/cita/{id}/rechazar")
    public String rechazar(@PathVariable Integer id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes ra) {
        try {
            Usuario admin = usuarioService.buscarPorUsername(userDetails.getUsername()).orElseThrow();
            citaService.rechazar(id, admin);
            ra.addFlashAttribute("mensajeExito", "Solicitud rechazada correctamente.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/panel";
    }
}
