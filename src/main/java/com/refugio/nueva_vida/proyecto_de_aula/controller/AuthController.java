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
    private final CitaService citaService;

    public AuthController(UsuarioService usuarioService, CitaService citaService) {
        this.usuarioService = usuarioService;
        this.citaService = citaService;
    }

    // ── Login GET ─────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null)  model.addAttribute("errorMsg",  "Usuario o contraseña incorrectos.");
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
            @RequestParam("usuarioNombre")  String usuarioNombre,
            @RequestParam("nombre")         String nombre,
            @RequestParam("email")          String email,
            @RequestParam("contrasena")     String contrasena,
            @RequestParam(value = "telefono",  required = false) String telefono,
            @RequestParam(value = "direccion", required = false) String direccion,
            RedirectAttributes redirectAttrs) {

        try {
            Usuario nuevo = new Usuario();
            nuevo.setUsuario(usuarioNombre);
            nuevo.setNombre(nombre);
            nuevo.setEmail(email);
            nuevo.setContrasena(contrasena);
            nuevo.setTelefono(telefono);
            nuevo.setDireccion(direccion);

            usuarioService.registrar(nuevo);

            redirectAttrs.addFlashAttribute("mensajeExito",
                    "¡Cuenta creada exitosamente! Ya puedes iniciar sesión.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/registro";
        }
    }

    // ── Perfil del usuario logueado ───────────────────────────────────────────
    @GetMapping("/perfil")
    public String perfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // BUG 8: admin que visita /perfil va a su panel
        boolean esAdmin = userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_administrador"));
        if (esAdmin) return "redirect:/admin/perfil";

        Usuario usuario = usuarioService.buscarPorUsername(userDetails.getUsername())
                .orElseThrow();
        var citas = citaService.citasDeUsuario(usuario);
        model.addAttribute("usuario", usuario);
        model.addAttribute("citas", citas);
        return "usuario/mi-perfil-userview";
    }
}
