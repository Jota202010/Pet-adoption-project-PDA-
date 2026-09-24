package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.dtos.AdminNotificacionDTO;
import com.refugio.nueva_vida.proyecto_de_aula.model.Testimonio;
import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.service.TestimonioService;
import com.refugio.nueva_vida.proyecto_de_aula.service.UsuarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TestimonioController {

    private final TestimonioService testimonioService;
    private final UsuarioService usuarioService;
    private final SimpMessagingTemplate messagingTemplate; // envío directo por WebSocket

    /** Página de testimonios — requiere sesión (lo exige SecurityConfig) */
    @GetMapping("/testimonios")
    public String testimonios(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        Usuario usuario = usuarioService.buscarPorUsername(userDetails.getUsername()).orElse(null);

        double promedio = testimonioService.promedio();
        model.addAttribute("promedio",        promedio);
        model.addAttribute("estrellasLlenas", (int) Math.round(promedio));
        model.addAttribute("total",           testimonioService.total());
        model.addAttribute("testimonios",     testimonioService.ultimos());

        if (usuario != null) {
            testimonioService.testimonioDe(usuario)
                    .ifPresent(t -> model.addAttribute("miTestimonio", t));
        }
        return "usuario/testimonios";
    }

    /** Guardar la calificación y avisarle al admin en vivo */
    @PostMapping("/testimonios")
    public String calificar(@RequestParam("calificacion") Integer calificacion,
                            @RequestParam(value = "comentario", required = false) String comentario,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes ra) {

        Usuario usuario = usuarioService.buscarPorUsername(userDetails.getUsername()).orElse(null);
        if (usuario == null) {
            ra.addFlashAttribute("errorMsg", "Sesión expirada. Inicia sesión de nuevo.");
            return "redirect:/login";
        }

        try {
            Testimonio testimonio = testimonioService.calificar(usuario, calificacion, comentario);

            // ── Aviso en vivo al panel del admin (mismo canal de la campanita) ──
            enviarAvisoAdmin(testimonio);

            ra.addFlashAttribute("mensajeExito", "¡Gracias por calificarnos! Tu opinión nos ayuda muchísimo.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/testimonios";
    }

    private void enviarAvisoAdmin(Testimonio testimonio) {
        String estrellas = "⭐".repeat(testimonio.getCalificacion());
        String mensaje = "<strong>" + HtmlUtils.htmlEscape(testimonio.getUsuario().getNombre()) + "</strong>"
                + " calificó el refugio con " + estrellas;

        AdminNotificacionDTO aviso = AdminNotificacionDTO.builder()
                .tipo(AdminNotificacionDTO.Tipo.TESTIMONIO)
                .icono("⭐")
                .mensaje(mensaje)
                .enlace("/testimonios")
                .build();

        messagingTemplate.convertAndSend("/topic/admin-notificaciones", aviso);
        log.info("Aviso de testimonio enviado al panel admin (WebSocket): {}", mensaje);
    }
}