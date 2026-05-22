package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.service.CitaService;
import com.refugio.nueva_vida.proyecto_de_aula.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final CitaService    citaService;

    public AuthController(UsuarioService usuarioService, CitaService citaService) {
        this.usuarioService = usuarioService;
        this.citaService    = citaService;
    }

    // ── Login GET ─────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error  != null) model.addAttribute("errorMsg",  "Usuario o contraseña incorrectos. Verifica tus datos.");
        if (logout != null) model.addAttribute("logoutMsg", "Sesión cerrada correctamente.");
        return "usuario/login";
    }

    // ── Registro GET ──────────────────────────────────────────────────────────
    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("nuevoUsuario", new Usuario());
        return "usuario/registro";
    }

    // ── Registro POST ─────────────────────────────────────────────────────────
    @PostMapping("/registro")
    public String registrar(
            @RequestParam(value = "usuarioNombre",       defaultValue = "") String usuarioNombre,
            @RequestParam(value = "nombre",              defaultValue = "") String nombre,
            @RequestParam(value = "email",               defaultValue = "") String email,
            @RequestParam(value = "contrasena",          defaultValue = "") String contrasena,
            @RequestParam(value = "confirmarContrasena", defaultValue = "") String confirmarContrasena,
            @RequestParam(value = "telefono",  required = false) String telefono,
            @RequestParam(value = "direccion", required = false) String direccion,
            RedirectAttributes redirectAttrs) {

        // ── Validaciones rápidas antes de llamar el servicio ──────────────────
        if (usuarioNombre.isBlank()) {
            redirectAttrs.addFlashAttribute("errorMsg", "El nombre de usuario es obligatorio.");
            return "redirect:/registro";
        }
        if (nombre.isBlank()) {
            redirectAttrs.addFlashAttribute("errorMsg", "El nombre completo es obligatorio.");
            return "redirect:/registro";
        }
        if (email.isBlank()) {
            redirectAttrs.addFlashAttribute("errorMsg", "El correo electrónico es obligatorio.");
            return "redirect:/registro";
        }
        if (contrasena.isBlank()) {
            redirectAttrs.addFlashAttribute("errorMsg", "La contraseña es obligatoria.");
            return "redirect:/registro";
        }
        if (contrasena.length() < 8) {
            redirectAttrs.addFlashAttribute("errorMsg", "La contraseña debe tener al menos 8 caracteres.");
            return "redirect:/registro";
        }
        if (!contrasena.equals(confirmarContrasena)) {
            redirectAttrs.addFlashAttribute("errorMsg", "Las contraseñas no coinciden. Verifica que ambas sean iguales.");
            return "redirect:/registro";
        }

        try {
            Usuario nuevo = new Usuario();
            nuevo.setUsuario(usuarioNombre);
            nuevo.setNombre(nombre);
            nuevo.setEmail(email);
            nuevo.setContrasena(contrasena);
            nuevo.setTelefono((telefono != null && !telefono.isBlank()) ? telefono : null);
            nuevo.setDireccion((direccion != null && !direccion.isBlank()) ? direccion : null);

            usuarioService.registrar(nuevo);
            redirectAttrs.addFlashAttribute("mensajeExito",
                "¡Cuenta creada exitosamente! Ya puedes iniciar sesión.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/registro";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg",
                "Ocurrió un error inesperado al crear la cuenta. Intenta de nuevo.");
            return "redirect:/registro";
        }
    }

    // ── Perfil del usuario logueado ───────────────────────────────────────────
    @GetMapping("/perfil")
    public String perfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        boolean esAdmin = userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_administrador"));
        if (esAdmin) return "redirect:/admin/perfil";

        Usuario usuario = usuarioService.buscarPorUsername(userDetails.getUsername()).orElse(null);
        if (usuario == null) return "redirect:/login";

        model.addAttribute("usuario", usuario);
        model.addAttribute("citas",   citaService.citasDeUsuario(usuario));
        return "usuario/mi-perfil-userview";
    }
}
