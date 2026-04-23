package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.service.PerroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MascotaController {

    private final PerroService perroService;

    public MascotaController(PerroService perroService) {
        this.perroService = perroService;
    }

    // Vista de usuario: detalle de un perro
    @GetMapping("/mascota/{id}")
    public String detalleMascotaUser(@PathVariable Integer id, Model model) {
        Perro perro = perroService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Perro no encontrado: " + id));
        model.addAttribute("perro", perro);
        return "usuario/detalle-perro-userview";
    }

    // Vista de admin: detalle de un perro
    @GetMapping("/admin/mascota/{id}")
    public String detalleMascotaAdmin(@PathVariable Integer id, Model model) {
        Perro perro = perroService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Perro no encontrado: " + id));
        model.addAttribute("perro", perro);
        return "privilegiado/detalle-perro-adminview";
    }

    // Formulario agregar mascota (GET)
    @GetMapping("/admin/agregar-mascota")
    public String mostrarFormularioAgregar(Model model) {
        model.addAttribute("perro", new Perro());
        model.addAttribute("sexos", Perro.Sexo.values());
        model.addAttribute("estados", Perro.Estado.values());
        model.addAttribute("nivelesS", Perro.NivelSalud.values());
        model.addAttribute("sociabilidades", Perro.Sociabilidad.values());
        return "privilegiado/agregar-mascota-adminview";
    }

    // Guardar nueva mascota (POST)
    @PostMapping("/admin/agregar-mascota")
    public String guardarMascota(@ModelAttribute Perro perro,
                                 RedirectAttributes redirectAttrs) {
        perroService.guardar(perro);
        redirectAttrs.addFlashAttribute("mensajeExito",
                "El perro \"" + perro.getNombre() + "\" fue registrado exitosamente.");
        return "redirect:/admin/panel";
    }
}
