package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.model.HistorialEstado;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.List;

@Controller
public class MascotaController {

    private final PerroService           perroService;
    private final CitaService            citaService;
    private final FotoPerroService       fotoService;
    private final HistorialEstadoService historialService;
    private final UsuarioService         usuarioService;

    public MascotaController(PerroService perroService,
                             CitaService citaService,
                             FotoPerroService fotoService,
                             HistorialEstadoService historialService,
                             UsuarioService usuarioService) {
        this.perroService     = perroService;
        this.citaService      = citaService;
        this.fotoService      = fotoService;
        this.historialService = historialService;
        this.usuarioService   = usuarioService;
    }

    // ── Detalle público ───────────────────────────────────────────────────────
    @GetMapping("/mascota/{id}")
    public String detalleMascotaUser(@PathVariable Integer id,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes ra,
                                     Model model) {
        Perro perro = perroService.buscarPorId(id).orElse(null);
        if (perro == null) {
            ra.addFlashAttribute("errorMsg", "El animal que buscas no existe.");
            return "redirect:/inicio";
        }

        // Solo los admins pueden ver animales no publicados
        boolean esAdmin = userDetails != null && userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_administrador"));

        if (perro.getEstadoPublicacion() != Perro.EstadoPublicacion.PUBLICADO && !esAdmin) {
            ra.addFlashAttribute("errorMsg",
                "El animal '" + perro.getNombre() + "' ya no está disponible para adopción.");
            return "redirect:/inicio";
        }

        // Verificar si el usuario ya tiene una cita activa con este animal
        boolean tienesCita = false;
        if (userDetails != null && !esAdmin) {
            try {
                Usuario usuario = usuarioService.buscarPorUsername(userDetails.getUsername()).orElse(null);
                if (usuario != null) {
                    tienesCita = citaService.citasDeUsuario(usuario).stream()
                        .anyMatch(c -> c.getPerro().getIdPerro().equals(id)
                                   && c.getEstado() != Cita.EstadoCita.rechazada);
                }
            } catch (Exception ignored) {}
        }

        List<com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro> fotos = fotoService.fotosDePerro(perro);
        com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro fotoPerfil =
            fotoService.fotoPerfil(perro)
                .orElseGet(() -> fotos.isEmpty() ? null : fotos.get(0));

        model.addAttribute("perro",      perro);
        model.addAttribute("fotos",      fotos);
        model.addAttribute("fotoPerfil", fotoPerfil);
        model.addAttribute("tienesCita", tienesCita);
        return "usuario/detalle-perro-userview";
    }

    // ── Detalle admin (solo lectura) ──────────────────────────────────────────
    @GetMapping("/admin/mascota/{id}/ver")
    public String verMascota(@PathVariable Integer id, RedirectAttributes ra, Model model) {
        Perro perro = perroService.buscarPorId(id).orElse(null);
        if (perro == null) {
            ra.addFlashAttribute("errorMsg", "El animal con id " + id + " no existe.");
            return "redirect:/admin/panel";
        }
        model.addAttribute("perro",      perro);
        model.addAttribute("fotos",      fotoService.fotosDePerro(perro));
        model.addAttribute("fotoPerfil", fotoService.fotoPerfil(perro).orElse(null));
        model.addAttribute("citas",      citaService.citasDePerro(perro));
        model.addAttribute("historial",  historialService.historialDePerro(perro));
        return "privilegiado/ver-perro-adminview";
    }

    // ── Detalle admin (editar) ────────────────────────────────────────────────
    @GetMapping("/admin/mascota/{id}")
    public String detalleMascotaAdmin(@PathVariable Integer id, RedirectAttributes ra, Model model) {
        Perro perro = perroService.buscarPorId(id).orElse(null);
        if (perro == null) {
            ra.addFlashAttribute("errorMsg", "El animal con id " + id + " no existe.");
            return "redirect:/admin/panel";
        }
        List<com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro> fotos = fotoService.fotosDePerro(perro);
        com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro fotoPerfil =
            fotoService.fotoPerfil(perro)
                .orElseGet(() -> fotos.isEmpty() ? null : fotos.get(0));
        model.addAttribute("perro",              perro);
        model.addAttribute("sexos",              Perro.Sexo.values());
        model.addAttribute("estados",            Perro.Estado.values());
        model.addAttribute("nivelesS",           Perro.NivelSalud.values());
        model.addAttribute("sociabilidades",     Perro.Sociabilidad.values());
        model.addAttribute("estadosPublicacion", Perro.EstadoPublicacion.values());
        model.addAttribute("citas",              citaService.citasDePerro(perro));
        model.addAttribute("historial",          historialService.historialDePerro(perro));
        model.addAttribute("fotos",              fotos);
        model.addAttribute("fotoPerfil",         fotoPerfil);
        return "privilegiado/detalle-perro-adminview";
    }

    // ── Editar perro ──────────────────────────────────────────────────────────
    @PostMapping("/admin/mascota/{id}/editar")
    public String editarMascota(@PathVariable Integer id,
                                @ModelAttribute Perro perroActualizado,
                                @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
                                @RequestParam(value = "esPerfil", required = false, defaultValue = "false") boolean esPerfil,
                                RedirectAttributes ra) {
        // Validar nombre
        if (perroActualizado.getNombre() == null || perroActualizado.getNombre().isBlank()) {
            ra.addFlashAttribute("errorMsg", "El nombre del animal es obligatorio.");
            return "redirect:/admin/mascota/" + id;
        }

        Perro existente = perroService.buscarPorId(id).orElse(null);
        if (existente == null) {
            ra.addFlashAttribute("errorMsg", "El animal con id " + id + " no existe.");
            return "redirect:/admin/panel";
        }

        Perro.EstadoPublicacion estadoAnterior = existente.getEstadoPublicacion();

        existente.setNombre(perroActualizado.getNombre().trim());
        existente.setEdad(perroActualizado.getEdad());
        existente.setSexo(perroActualizado.getSexo());
        existente.setEstado(perroActualizado.getEstado());
        existente.setNivelSalud(perroActualizado.getNivelSalud());
        existente.setSociabilidad(perroActualizado.getSociabilidad());
        existente.setEsterilizado(perroActualizado.getEsterilizado() != null && perroActualizado.getEsterilizado());
        existente.setVacunado(perroActualizado.getVacunado() != null && perroActualizado.getVacunado());
        existente.setEstadoPublicacion(
            perroActualizado.getEstadoPublicacion() != null
                ? perroActualizado.getEstadoPublicacion()
                : Perro.EstadoPublicacion.EN_REFUGIO);
        existente.setDescripcion(perroActualizado.getDescripcion());
        existente.setRegistroMedico(perroActualizado.getRegistroMedico());
        perroService.guardar(existente);

        historialService.registrar(existente, estadoAnterior,
            existente.getEstadoPublicacion(), HistorialEstado.Origen.ADMIN);

        if (archivos != null) {
            boolean primeraComo = esPerfil;
            for (MultipartFile archivo : archivos) {
                if (!archivo.isEmpty()) {
                    try {
                        fotoService.guardarFoto(existente, archivo, primeraComo);
                        primeraComo = false;
                    } catch (IOException e) {
                        ra.addFlashAttribute("errorMsg", "Error al subir foto: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        ra.addFlashAttribute("errorMsg", e.getMessage());
                    }
                }
            }
        }

        ra.addFlashAttribute("mensajeExito",
            "Perfil de \"" + existente.getNombre() + "\" actualizado correctamente.");
        return "redirect:/admin/mascota/" + id;
    }

    // ── Eliminar perro ────────────────────────────────────────────────────────
    @PostMapping("/admin/mascota/{id}/eliminar")
    public String eliminarMascota(@PathVariable Integer id, RedirectAttributes ra) {
        Perro perro = perroService.buscarPorId(id).orElse(null);
        if (perro == null) {
            ra.addFlashAttribute("errorMsg", "El animal no existe o ya fue eliminado.");
            return "redirect:/admin/panel";
        }
        String nombre = perro.getNombre();
        try {
            perroService.eliminar(id);
            ra.addFlashAttribute("mensajeExito", "El perro \"" + nombre + "\" fue eliminado.");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("errorMsg",
                "No se puede eliminar a \"" + nombre + "\" porque tiene citas registradas. " +
                "Elimina primero las citas asociadas.");
        }
        return "redirect:/admin/panel";
    }

    // ── Formulario agregar ────────────────────────────────────────────────────
    @GetMapping("/admin/agregar-mascota")
    public String mostrarFormularioAgregar(Model model) {
        model.addAttribute("perro",              new Perro());
        model.addAttribute("sexos",              Perro.Sexo.values());
        model.addAttribute("estados",            Perro.Estado.values());
        model.addAttribute("nivelesS",           Perro.NivelSalud.values());
        model.addAttribute("sociabilidades",     Perro.Sociabilidad.values());
        model.addAttribute("estadosPublicacion", Perro.EstadoPublicacion.values());
        return "privilegiado/agregar-mascota-adminview";
    }

    // ── Guardar nuevo perro ───────────────────────────────────────────────────
    @PostMapping("/admin/agregar-mascota")
    public String guardarMascota(@ModelAttribute Perro perro,
                                 @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
                                 @RequestParam(value = "esPerfil", required = false, defaultValue = "false") boolean esPerfil,
                                 RedirectAttributes ra) {
        // Validar nombre
        if (perro.getNombre() == null || perro.getNombre().isBlank()) {
            ra.addFlashAttribute("errorMsg", "El nombre del animal es obligatorio.");
            return "redirect:/admin/agregar-mascota";
        }
        // Validar campos requeridos de enums
        if (perro.getSexo() == null) {
            ra.addFlashAttribute("errorMsg", "Debes seleccionar el sexo del animal.");
            return "redirect:/admin/agregar-mascota";
        }
        if (perro.getEstado() == null) {
            ra.addFlashAttribute("errorMsg", "Debes seleccionar el estado de origen del animal.");
            return "redirect:/admin/agregar-mascota";
        }
        if (perro.getNivelSalud() == null) {
            ra.addFlashAttribute("errorMsg", "Debes seleccionar el nivel de salud del animal.");
            return "redirect:/admin/agregar-mascota";
        }
        if (perro.getSociabilidad() == null) {
            ra.addFlashAttribute("errorMsg", "Debes seleccionar la sociabilidad del animal.");
            return "redirect:/admin/agregar-mascota";
        }

        perro.setNombre(perro.getNombre().trim());
        Perro guardado = perroService.guardar(perro);

        if (archivos != null) {
            boolean primeraComo = esPerfil;
            for (MultipartFile archivo : archivos) {
                if (!archivo.isEmpty()) {
                    try {
                        fotoService.guardarFoto(guardado, archivo, primeraComo);
                        primeraComo = false;
                    } catch (IOException e) {
                        ra.addFlashAttribute("errorMsg", "Error al subir foto: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        ra.addFlashAttribute("errorMsg", e.getMessage());
                    }
                }
            }
        }

        ra.addFlashAttribute("mensajeExito",
            "El perro \"" + guardado.getNombre() + "\" fue registrado exitosamente.");
        return "redirect:/admin/panel";
    }
}
