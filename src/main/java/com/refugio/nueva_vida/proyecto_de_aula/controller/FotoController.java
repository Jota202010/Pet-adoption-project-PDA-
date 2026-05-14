package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.service.FotoPerroService;
import com.refugio.nueva_vida.proyecto_de_aula.service.PerroService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.List;

@Controller
public class FotoController {

    private final FotoPerroService fotoService;
    private final PerroService perroService;

    public FotoController(FotoPerroService fotoService, PerroService perroService) {
        this.fotoService = fotoService;
        this.perroService = perroService;
    }

    @PostMapping("/admin/mascota/{id}/fotos")
    public String subirFotos(
            @PathVariable Integer id,
            @RequestParam("archivos") List<MultipartFile> archivos,
            @RequestParam(value = "esPerfil", defaultValue = "false") boolean esPerfil,
            RedirectAttributes ra) {

        Perro perro = perroService.buscarPorId(id).orElseThrow();
        boolean primeraComo = esPerfil;
        int subidas = 0;

        for (MultipartFile archivo : archivos) {
            if (!archivo.isEmpty()) {
                try {
                    fotoService.guardarFoto(perro, archivo, primeraComo);
                    primeraComo = false;
                    subidas++;
                } catch (IOException e) {
                    ra.addFlashAttribute("errorMsg", "Error al subir: " + archivo.getOriginalFilename());
                }
            }
        }

        if (subidas > 0) {
            ra.addFlashAttribute("mensajeExito", subidas + " foto(s) subida(s) correctamente.");
        }
        return "redirect:/admin/mascota/" + id;
    }

    @PostMapping("/admin/foto/{idFoto}/eliminar")
    public String eliminarFoto(
            @PathVariable Integer idFoto,
            @RequestParam Integer perroId,
            RedirectAttributes ra) {
        try {
            fotoService.eliminarFoto(idFoto);
            ra.addFlashAttribute("mensajeExito", "Foto eliminada.");
        } catch (IOException e) {
            ra.addFlashAttribute("errorMsg", "Error al eliminar la foto.");
        }
        return "redirect:/admin/mascota/" + perroId;
    }
}
