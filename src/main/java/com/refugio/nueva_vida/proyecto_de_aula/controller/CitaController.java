package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.beans.PropertyEditorSupport;

@Controller
public class CitaController {

    private final CitaService citaService;
    private final PerroService perroService;
    private final UsuarioService usuarioService;
    private final HorarioService horarioService;

    public CitaController(CitaService citaService, PerroService perroService,
                          UsuarioService usuarioService, HorarioService horarioService) {
        this.citaService = citaService;
        this.perroService = perroService;
        this.usuarioService = usuarioService;
        this.horarioService = horarioService;
    }

    // ── Convierte strings vacíos a null para evitar 400 en LocalDate/LocalTime ──
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(java.time.LocalDate.class, new PropertyEditorSupport() {
            @Override public void setAsText(String text) {
                setValue((text == null || text.trim().isEmpty()) ? null :
                    java.time.LocalDate.parse(text.trim()));
            }
        });
        binder.registerCustomEditor(java.time.LocalTime.class, new PropertyEditorSupport() {
            @Override public void setAsText(String text) {
                setValue((text == null || text.trim().isEmpty()) ? null :
                    java.time.LocalTime.parse(text.trim()));
            }
        });
    }

    // ── Formulario solicitud adopción (GET) ───────────────────────────────────
    @GetMapping("/agendar-cita/{perroId}")
    public String agendarForm(@PathVariable Integer perroId, Model model) {
        Perro perro = perroService.buscarPorId(perroId).orElseThrow();
        model.addAttribute("perro", perro);
        model.addAttribute("cita", new Cita());
        model.addAttribute("tiposVivienda",    Cita.TipoVivienda.values());
        model.addAttribute("propiedades",      Cita.Propiedad.values());
        model.addAttribute("opcionesMascotas", Cita.PermitenMascotas.values());
        return "usuario/agendar-cita";
    }

    // ── Enviar solicitud adopción (POST) — sin fecha ni hora ──────────────────
    @PostMapping("/agendar-cita/{perroId}")
    public String enviarSolicitud(@PathVariable Integer perroId,
                                  @ModelAttribute Cita cita,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes ra) {
        Perro perro = perroService.buscarPorId(perroId).orElseThrow();
        Usuario usuario = usuarioService.buscarPorUsername(userDetails.getUsername()).orElseThrow();
        cita.setPerro(perro);
        cita.setUsuario(usuario);
        cita.setEstado(Cita.EstadoCita.en_espera);
        cita.setFechaCita(null);
        cita.setHoraCita(null);
        try {
            citaService.guardar(cita);
            ra.addFlashAttribute("mensajeExito",
                "¡Solicitud enviada! Te notificaremos cuando sea pre-aprobada para que elijas tu horario.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/perfil";
    }

    // ── Elegir horario (GET) — solo si cita está pre_aprobada ─────────────────
    @GetMapping("/cita/{citaId}/elegir-horario")
    public String elegirHorarioForm(@PathVariable Integer citaId,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        Cita cita = citaService.buscarPorId(citaId).orElseThrow();
        Usuario usuario = usuarioService.buscarPorUsername(userDetails.getUsername()).orElseThrow();

        // Validar que la cita pertenece al usuario y está pre-aprobada
        if (!cita.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            return "redirect:/perfil";
        }
        if (cita.getEstado() != Cita.EstadoCita.pre_aprobada) {
            return "redirect:/perfil";
        }

        model.addAttribute("cita", cita);
        model.addAttribute("horariosDisponibles", horarioService.listarDisponibles());
        return "usuario/elegir-horario";
    }

    // ── Confirmar horario elegido (POST) ──────────────────────────────────────
    @PostMapping("/cita/{citaId}/confirmar-horario")
    public String confirmarHorario(@PathVariable Integer citaId,
                                   @RequestParam Integer idHorario,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes ra) {
        // BUG 4: verificar que la cita pertenece al usuario actual
        Cita cita = citaService.buscarPorId(citaId).orElseThrow();
        Usuario usuario = usuarioService.buscarPorUsername(userDetails.getUsername()).orElseThrow();
        if (!cita.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            ra.addFlashAttribute("errorMsg", "No tienes permiso para confirmar esta cita.");
            return "redirect:/perfil";
        }
        try {
            citaService.confirmar(citaId, idHorario);
            ra.addFlashAttribute("mensajeExito", "¡Cita confirmada! Ya tienes tu fecha y hora reservada.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/perfil";
    }

    // ── Detalle de cita (admin) ───────────────────────────────────────────────
    @GetMapping("/admin/cita/{id}")
    public String detalleCita(@PathVariable Integer id, Model model) {
        Cita cita = citaService.buscarPorId(id).orElseThrow();
        model.addAttribute("cita", cita);
        return "privilegiado/detalle-cita-admin";
    }
}
