package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.config.RabbitMQConfig;
import com.refugio.nueva_vida.proyecto_de_aula.dtos.CitaNotificationDTO;
import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.service.*;

import lombok.RequiredArgsConstructor;

import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@RequiredArgsConstructor 
@Controller  
public class AdminController {

    private final PerroService perroService;
    private final UsuarioService usuarioService;
    private final CitaService citaService;
    private final RabbitTemplate rabbitTemplate;

  
    // Panel principal 
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

    // Perfil del admin 
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

    // Detalle de usuario 
    @GetMapping("/admin/usuario/{id}")
    public String detalleUsuario(@PathVariable Integer id, Model model) {
        Usuario u = usuarioService.buscarPorId(id)
            .orElseThrow(() -> new IllegalStateException("Usuario con id " + id + " no encontrado."));
        model.addAttribute("usuario", u);
        model.addAttribute("citas",   citaService.citasDeUsuario(u));
        return "privilegiado/detalle-usuario-adminview";
    }

    //  Pre-aprobar cita 
    @PostMapping("/admin/cita/{id}/pre-aprobar")
    public String preAprobar(@PathVariable Integer id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes ra) {
        try {
            Usuario admin = usuarioService.buscarPorUsername(userDetails.getUsername()).orElseThrow();
            citaService.preAprobar(id, admin);

            Cita cita = citaService.buscarPorId(id).orElseThrow();
            CitaNotificationDTO notificacion = CitaNotificationDTO.builder()
                    .idCita(cita.getIdCita())
                    .nombreAdoptante(cita.getUsuario().getNombre())
                    .emailAdoptante(cita.getUsuario().getEmail())
                    .nombrePerro(cita.getPerro().getNombre())
                    .tipoEvento(CitaNotificationDTO.TipoEvento.PRE_APROBADA)
                    .build();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    notificacion
            );

            ra.addFlashAttribute("mensajeExito", "Solicitud pre-aprobada. El usuario podrá elegir su horario.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/cita/" + id;
    }

    // Rechazar cita 
    @PostMapping("/admin/cita/{id}/rechazar")
    public String rechazar(@PathVariable Integer id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes ra) {
        try {
            Usuario admin = usuarioService.buscarPorUsername(userDetails.getUsername()).orElseThrow();

            // Se obtiene la cita ANTES de rechazar, por si el método la modifica o desvincula datos
            Cita cita = citaService.buscarPorId(id).orElseThrow();
            String nombreAdoptante = cita.getUsuario().getNombre();
            String emailAdoptante  = cita.getUsuario().getEmail();
            String nombrePerro     = cita.getPerro().getNombre();

            citaService.rechazar(id, admin);

            CitaNotificationDTO notificacion = CitaNotificationDTO.builder()
                    .idCita(id)
                    .nombreAdoptante(nombreAdoptante)
                    .emailAdoptante(emailAdoptante)
                    .nombrePerro(nombrePerro)
                    .tipoEvento(CitaNotificationDTO.TipoEvento.RECHAZADA)
                    .build();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    notificacion
            );

            ra.addFlashAttribute("mensajeExito", "Solicitud rechazada correctamente.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/cita/" + id;
    }
}