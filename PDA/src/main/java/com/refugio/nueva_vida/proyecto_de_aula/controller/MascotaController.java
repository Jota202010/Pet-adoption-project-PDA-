package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.exception.PerroConCitasException;
import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.model.HistorialEstado;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.service.*;

import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
public class MascotaController {

    private final PerroService           perroService;
    private final CitaService            citaService;
    private final FotoPerroService       fotoService;
    private final HistorialEstadoService historialService;
    private final UsuarioService         usuarioService;
    private final BarraService           barraService;
    private final SimulacionTiempoService simulacionService;
    private final RecetaService          recetaService;
    private final InventarioService      inventarioService;

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

        boolean esAdmin = userDetails != null && userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_administrador"));

        if (perro.getEstadoPublicacion() != Perro.EstadoPublicacion.PUBLICADO && !esAdmin) {
            ra.addFlashAttribute("errorMsg",
                "El animal '" + perro.getNombre() + "' ya no está disponible para adopción.");
            return "redirect:/inicio";
        }

        barraService.sincronizarSaludConEstado(perro);
        perro = perroService.buscarPorId(id).orElse(perro);

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
        model.addAttribute("totalNoLeidas", recetaService.contarNoLeidas());
        return "usuario/detalle-perro-userview";
    }

    @GetMapping("/admin/mascota/{id}/ver")
    public String verMascota(@PathVariable Integer id, RedirectAttributes ra, Model model) {
        Perro perro = perroService.buscarPorId(id).orElse(null);
        if (perro == null) {
            ra.addFlashAttribute("errorMsg", "El animal con id " + id + " no existe.");
            return "redirect:/admin/panel";
        }

        barraService.sincronizarSaludConEstado(perro);
        perro = perroService.buscarPorId(id).orElse(perro);

        model.addAttribute("perro",      perro);
        model.addAttribute("fotos",      fotoService.fotosDePerro(perro));
        model.addAttribute("fotoPerfil", fotoService.fotoPerfil(perro).orElse(null));
        model.addAttribute("citas",      citaService.citasDePerro(perro));
        model.addAttribute("historial",  historialService.historialDePerro(perro));
        model.addAttribute("eventos",    barraService.eventosDePerro(perro));
        model.addAttribute("totalNoLeidas", recetaService.contarNoLeidas());
        return "privilegiado/ver-perro-adminview";
    }

    @GetMapping("/admin/mascota/{id}")
    public String detalleMascotaAdmin(@PathVariable Integer id, RedirectAttributes ra, Model model) {
        Perro perro = perroService.buscarPorId(id).orElse(null);
        if (perro == null) {
            ra.addFlashAttribute("errorMsg", "El animal con id " + id + " no existe.");
            return "redirect:/admin/panel";
        }

        barraService.sincronizarSaludConEstado(perro);
        perro = perroService.buscarPorId(id).orElse(perro);

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
        model.addAttribute("eventos",            barraService.eventosDePerro(perro));
        model.addAttribute("totalNoLeidas",      recetaService.contarNoLeidas());
        model.addAttribute("inventario",         inventarioService.listarInventario());
        return "privilegiado/detalle-perro-adminview";
    }

    @PostMapping("/perros/{id}/comida")
    public String registrarComida(@PathVariable Integer id,
                                  @RequestParam(defaultValue = "normal") String tipo,
                                  RedirectAttributes ra) {
        try {
            barraService.registrarComida(id, tipo);
            ra.addFlashAttribute("mensajeExito",
                "Comida " + (tipo.equalsIgnoreCase("premium") ? "premium" : "normal") + " registrada.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/admin/mascota/" + id;
    }

    @PostMapping("/perros/{id}/agua")
    public String registrarAgua(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            barraService.registrarAgua(id);
            ra.addFlashAttribute("mensajeExito", "Agua registrada.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/admin/mascota/" + id;
    }

    @PostMapping("/perros/{id}/vacuna")
    public String registrarVacuna(@PathVariable Integer id,
                                  @RequestParam(defaultValue = "General") String tipoVacuna,
                                  RedirectAttributes ra) {
        try {
            barraService.registrarVacuna(id, tipoVacuna);
            ra.addFlashAttribute("mensajeExito", "Vacuna registrada.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/admin/mascota/" + id;
    }

    @PostMapping("/perros/{id}/desparasitante")
    public String registrarDesparasitante(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            barraService.registrarDesparasitante(id);
            ra.addFlashAttribute("mensajeExito", "Desparasitación registrada.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/admin/mascota/" + id;
    }

    @PostMapping("/perros/{id}/resetear")
    public String resetearBarras(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            barraService.resetearBarras(id);
            ra.addFlashAttribute("mensajeExito", "Barras reseteadas.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/admin/mascota/" + id;
    }

    @PostMapping("/perros/{id}/eventos/eliminar")
    public String eliminarEventos(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            barraService.eliminarEventos(id);
            ra.addFlashAttribute("mensajeExito", "Historial de eventos eliminado.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/admin/mascota/" + id;
    }

    @PostMapping("/admin/simular/{id}")
    public String simularTiempo(@PathVariable Integer id,
                                @RequestParam(defaultValue = "3") int dias,
                                RedirectAttributes ra) {
        try {
            simulacionService.simularPaso(id, dias);
            ra.addFlashAttribute("mensajeExito",
                "Simulación de " + dias + " día(s) completada.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al simular: " + e.getMessage());
        }
        return "redirect:/admin/mascota/" + id;
    }

    @PostMapping("/admin/mascota/{id}/editar")
    public String editarMascota(@PathVariable Integer id,
                                @ModelAttribute Perro perroActualizado,
                                @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
                                @RequestParam(value = "esPerfil", required = false, defaultValue = "false") boolean esPerfil,
                                RedirectAttributes ra) {
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

        barraService.sincronizarEstadoMedicoManual(existente);

        Perro perroGuardado = perroService.buscarPorId(id).orElse(existente);
        historialService.registrar(perroGuardado, estadoAnterior,
            perroGuardado.getEstadoPublicacion(), HistorialEstado.Origen.ADMIN);

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
        } catch (PerroConCitasException e) {
            // Única razón de negocio real para bloquear el borrado.
            ra.addFlashAttribute("errorMsg", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            // Red de seguridad por si aparece alguna otra FK contra id_perro
            // en el futuro que no se haya limpiado en PerroService.eliminar().
            ra.addFlashAttribute("errorMsg",
                "No se puede eliminar a \"" + nombre + "\" porque todavía tiene datos asociados " +
                "(revisa citas, recetas, fotos o historial).");
        }
        return "redirect:/admin/panel";
    }

    @GetMapping("/admin/agregar-mascota")
    public String mostrarFormularioAgregar(Model model) {
        model.addAttribute("perro",              new Perro());
        model.addAttribute("sexos",              Perro.Sexo.values());
        model.addAttribute("estados",            Perro.Estado.values());
        model.addAttribute("nivelesS",           Perro.NivelSalud.values());
        model.addAttribute("sociabilidades",     Perro.Sociabilidad.values());
        model.addAttribute("estadosPublicacion", Perro.EstadoPublicacion.values());
        model.addAttribute("totalNoLeidas",      recetaService.contarNoLeidas());
        return "privilegiado/agregar-mascota-adminview";
    }

    @PostMapping("/admin/agregar-mascota")
    public String guardarMascota(@ModelAttribute Perro perro,
                                 @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
                                 @RequestParam(value = "esPerfil", required = false, defaultValue = "false") boolean esPerfil,
                                 RedirectAttributes ra) {
        if (perro.getNombre() == null || perro.getNombre().isBlank()) {
            ra.addFlashAttribute("errorMsg", "El nombre del animal es obligatorio.");
            return "redirect:/admin/agregar-mascota";
        }
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

        if (perro.getNutricion() == null)    perro.setNutricion(100);
        if (perro.getHidratacion() == null)  perro.setHidratacion(100);
        if (perro.getSalud() == null)        perro.setSalud(100);

        Perro guardado = perroService.guardar(perro);

        barraService.sincronizarEstadoMedicoManual(guardado);

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