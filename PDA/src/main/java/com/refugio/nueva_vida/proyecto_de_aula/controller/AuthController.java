package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.service.CitaService;
import com.refugio.nueva_vida.proyecto_de_aula.service.EmailService;
import com.refugio.nueva_vida.proyecto_de_aula.service.UsuarioService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor 
public class AuthController {

    private final UsuarioService usuarioService;
    private final CitaService    citaService;
    private final EmailService   emailService;

    

    // Login GET 
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error  != null) model.addAttribute("errorMsg",  "Usuario o contraseña incorrectos. Verifica tus datos.");
        if (logout != null) model.addAttribute("logoutMsg", "Sesión cerrada correctamente.");
        return "usuario/login";
    }

    //  Registro GET 
    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("nuevoUsuario", new Usuario());
        return "usuario/registro";
    }

    //  Registro POST 
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

        //  Validaciones rápidas antes de llamar el servicio
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

    // Perfil del usuario logueado
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

    // ══════════════════════════════════════════════════════════════════
    // Recuperación de contraseña
    // ══════════════════════════════════════════════════════════════════

    // Paso 1: formulario para pedir el correo (GET)
    @GetMapping("/olvide-contrasena")
    public String olvideContrasenaForm() {
        return "usuario/olvide-contrasena";
    }

    // Paso 1: enviar el código al correo (POST)
    @PostMapping("/olvide-contrasena")
    public String enviarCodigo(@RequestParam String email, RedirectAttributes ra) {
        if (email == null || email.isBlank()) {
            ra.addFlashAttribute("errorMsg", "Debes ingresar tu correo electrónico.");
            return "redirect:/olvide-contrasena";
        }

        usuarioService.generarCodigoReset(email).ifPresent(codigo ->
            emailService.enviarCodigoRecuperacion(email.trim().toLowerCase(), codigo)
        );

        // Mensaje SIEMPRE igual, exista o no el correo (no revelamos si un email está registrado)
        ra.addFlashAttribute("mensajeExito",
            "Si el correo está registrado, te enviamos un código de 6 dígitos. Revisa tu bandeja de entrada (y spam).");
        ra.addFlashAttribute("emailEnviado", email.trim().toLowerCase());
        return "redirect:/restablecer-contrasena";
    }

    // Paso 2: formulario para ingresar código + nueva contraseña (GET)
    @GetMapping("/restablecer-contrasena")
    public String restablecerForm() {
        return "usuario/restablecer-contrasena";
    }

    // Paso 2: validar código y cambiar la contraseña (POST)
    @PostMapping("/restablecer-contrasena")
    public String restablecerContrasena(@RequestParam String email,
                                        @RequestParam String codigo,
                                        @RequestParam String nuevaContrasena,
                                        @RequestParam String confirmarContrasena,
                                        RedirectAttributes ra) {
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            ra.addFlashAttribute("errorMsg", "Las contraseñas no coinciden.");
            ra.addFlashAttribute("emailEnviado", email);
            return "redirect:/restablecer-contrasena";
        }

        try {
            usuarioService.restablecerContrasena(email, codigo, nuevaContrasena);
            ra.addFlashAttribute("mensajeExito", "¡Contraseña actualizada! Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            ra.addFlashAttribute("emailEnviado", email);
            return "redirect:/restablecer-contrasena";
        }
    }
}