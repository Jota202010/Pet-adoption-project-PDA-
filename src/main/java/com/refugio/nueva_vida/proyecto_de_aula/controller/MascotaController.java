package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.service.CitaService;
import com.refugio.nueva_vida.proyecto_de_aula.service.FotoPerroService;
import com.refugio.nueva_vida.proyecto_de_aula.service.PerroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.List;

@Controller
public class MascotaController {

    private final PerroService perroService;
    private final CitaService citaService;
    private final FotoPerroService fotoService;

    public MascotaController(PerroService perroService, CitaService citaService, FotoPerroService fotoService) {
        this.perroService = perroService;
        this.citaService = citaService;
        this.fotoService = fotoService;
    }

    // ── Detalle público ───────────────────────────────────────────────────────
    @GetMapping("/mascota/{id}")
    public String detalleMascotaUser(@PathVariable Integer id, Model model) {
        Perro perro = perroService.buscarPorId(id).orElseThrow();
        List<com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro> fotos = fotoService.fotosDePerro(perro);
        // Foto de perfil: primero la marcada como perfil, si no existe usa la primera
        com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro fotoPerfil =
            fotoService.fotoPerfil(perro)
                .orElseGet(() -> fotos.isEmpty() ? null : fotos.get(0));
        model.addAttribute("perro", perro);
        model.addAttribute("fotos", fotos);
        model.addAttribute("fotoPerfil", fotoPerfil);
        return "usuario/detalle-perro-userview";
    }

    // ── Detalle admin ─────────────────────────────────────────────────────────
    @GetMapping("/admin/mascota/{id}")
    public String detalleMascotaAdmin(@PathVariable Integer id, Model model) {
        Perro perro = perroService.buscarPorId(id).orElseThrow();
        List<com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro> fotos = fotoService.fotosDePerro(perro);
        // Foto de perfil para mostrar en la cabecera del panel admin
        com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro fotoPerfil =
            fotoService.fotoPerfil(perro)
                .orElseGet(() -> fotos.isEmpty() ? null : fotos.get(0));
        model.addAttribute("perro", perro);
        model.addAttribute("sexos", Perro.Sexo.values());
        model.addAttribute("estados", Perro.Estado.values());
        model.addAttribute("nivelesS", Perro.NivelSalud.values());
        model.addAttribute("sociabilidades", Perro.Sociabilidad.values());
        model.addAttribute("citas", citaService.citasDePerro(perro));
        model.addAttribute("fotos", fotos);
        model.addAttribute("fotoPerfil", fotoPerfil);
        return "privilegiado/detalle-perro-adminview";
    }

    // ── Editar perro ──────────────────────────────────────────────────────────
    @PostMapping("/admin/mascota/{id}/editar")
    public String editarMascota(@PathVariable Integer id,
                                @ModelAttribute Perro perroActualizado,
                                @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
                                @RequestParam(value = "esPerfil", required = false, defaultValue = "false") boolean esPerfil,
                                RedirectAttributes ra) {
        Perro existente = perroService.buscarPorId(id).orElseThrow();
        existente.setNombre(perroActualizado.getNombre());
        existente.setEdad(perroActualizado.getEdad());
        existente.setSexo(perroActualizado.getSexo());
        existente.setEstado(perroActualizado.getEstado());
        existente.setNivelSalud(perroActualizado.getNivelSalud());
        existente.setSociabilidad(perroActualizado.getSociabilidad());
        existente.setEsterilizado(perroActualizado.getEsterilizado() != null && perroActualizado.getEsterilizado());
        existente.setVacunado(perroActualizado.getVacunado() != null && perroActualizado.getVacunado());
        existente.setAdoptado(perroActualizado.getAdoptado() != null && perroActualizado.getAdoptado());
        existente.setListaParaAdoptar(perroActualizado.getListaParaAdoptar() != null && perroActualizado.getListaParaAdoptar());
        existente.setDescripcion(perroActualizado.getDescripcion());
        existente.setRegistroMedico(perroActualizado.getRegistroMedico());
        perroService.guardar(existente);

        if (archivos != null) {
            boolean primeraComo = esPerfil;
            for (MultipartFile archivo : archivos) {
                if (!archivo.isEmpty()) {
                    try {
                        fotoService.guardarFoto(existente, archivo, primeraComo);
                        primeraComo = false;
                    } catch (IOException e) {
                        ra.addFlashAttribute("errorMsg", "Error al subir foto.");
                    }
                }
            }
        }

        ra.addFlashAttribute("mensajeExito", "Perfil de \"" + existente.getNombre() + "\" actualizado correctamente.");
        return "redirect:/admin/mascota/" + id;
    }

    // ── Eliminar perro ────────────────────────────────────────────────────────
    @PostMapping("/admin/mascota/{id}/eliminar")
    public String eliminarMascota(@PathVariable Integer id, RedirectAttributes ra) {
        Perro perro = perroService.buscarPorId(id).orElseThrow();
        String nombre = perro.getNombre();
        perroService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "El perro \"" + nombre + "\" fue eliminado.");
        return "redirect:/admin/panel";
    }

    // ── Formulario agregar ────────────────────────────────────────────────────
    @GetMapping("/admin/agregar-mascota")
    public String mostrarFormularioAgregar(Model model) {
        model.addAttribute("perro", new Perro());
        model.addAttribute("sexos", Perro.Sexo.values());
        model.addAttribute("estados", Perro.Estado.values());
        model.addAttribute("nivelesS", Perro.NivelSalud.values());
        model.addAttribute("sociabilidades", Perro.Sociabilidad.values());
        return "privilegiado/agregar-mascota-adminview";
    }

    // ── Guardar nuevo perro ───────────────────────────────────────────────────
    @PostMapping("/admin/agregar-mascota")
    public String guardarMascota(@ModelAttribute Perro perro,
                                 @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
                                 @RequestParam(value = "esPerfil", required = false, defaultValue = "false") boolean esPerfil,
                                 RedirectAttributes ra) {
        Perro guardado = perroService.guardar(perro);

        if (archivos != null) {
            boolean primeraComo = esPerfil;
            for (MultipartFile archivo : archivos) {
                if (!archivo.isEmpty()) {
                    try {
                        fotoService.guardarFoto(guardado, archivo, primeraComo);
                        primeraComo = false;
                    } catch (IOException e) {
                        ra.addFlashAttribute("errorMsg", "Error al subir foto.");
                    }
                }
            }
        }

        ra.addFlashAttribute("mensajeExito", "El perro \"" + guardado.getNombre() + "\" fue registrado.");
        return "redirect:/admin/panel";
    }
}
