package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.service.*;
import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalTime;

@Controller
public class AdminController {

    private final PerroService perroService;
    private final UsuarioService usuarioService;
    private final CitaService citaService;
    private final HorarioService horarioService;

    public AdminController(PerroService perroService, UsuarioService usuarioService,
                           CitaService citaService, HorarioService horarioService) {
        this.perroService = perroService;
        this.usuarioService = usuarioService;
        this.citaService = citaService;
        this.horarioService = horarioService;
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
        model.addAttribute("horarios",        horarioService.listarDisponibles());
        return "privilegiado/panel-general-adminview";
    }

    // ── Perfil del admin ──────────────────────────────────────────────────────
    @GetMapping("/admin/perfil")
    public String perfilAdmin(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario admin = usuarioService.buscarPorUsername(userDetails.getUsername()).orElseThrow();
        var todas = citaService.listarTodas();
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
        Usuario u = usuarioService.buscarPorId(id).orElseThrow();
        model.addAttribute("usuario", u);
        model.addAttribute("citas", citaService.citasDeUsuario(u));
        return "privilegiado/detalle-usuario-adminview";
    }

    // ── Pre-aprobar cita ──────────────────────────────────────────────────────
    @PostMapping("/admin/cita/{id}/pre-aprobar")
    public String preAprobar(@PathVariable Integer id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes ra) {
        Usuario admin = usuarioService.buscarPorUsername(userDetails.getUsername()).orElseThrow();
        citaService.preAprobar(id, admin);
        ra.addFlashAttribute("mensajeExito", "Solicitud pre-aprobada. El usuario podrá elegir su horario.");
        return "redirect:/admin/panel";
    }

    // ── Rechazar cita ─────────────────────────────────────────────────────────
    @PostMapping("/admin/cita/{id}/rechazar")
    public String rechazar(@PathVariable Integer id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes ra) {
        Usuario admin = usuarioService.buscarPorUsername(userDetails.getUsername()).orElseThrow();
        citaService.rechazar(id, admin);
        ra.addFlashAttribute("mensajeExito", "Solicitud rechazada.");
        return "redirect:/admin/panel";
    }

    // ── Crear horario disponible ──────────────────────────────────────────────
    @PostMapping("/admin/horario/crear")
    public String crearHorario(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha,
            @RequestParam @DateTimeFormat(pattern = "HH:mm")      LocalTime hora,
            RedirectAttributes ra) {
        horarioService.crear(fecha, hora);
        ra.addFlashAttribute("mensajeExito", "Horario " + fecha + " a las " + hora + " creado correctamente.");
        return "redirect:/admin/panel";
    }

    // ── Eliminar horario disponible ───────────────────────────────────────────
    @PostMapping("/admin/horario/{id}/eliminar")
    public String eliminarHorario(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            horarioService.eliminar(id);
            ra.addFlashAttribute("mensajeExito", "Horario eliminado.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/panel";
    }
}
