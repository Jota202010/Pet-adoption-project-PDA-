package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.service.FotoPerroService;
import com.refugio.nueva_vida.proyecto_de_aula.service.PerroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.*;

@Controller
public class HomeController {

    private final PerroService perroService;
    private final FotoPerroService fotoService;

    public HomeController(PerroService perroService, FotoPerroService fotoService) {
        this.perroService = perroService;
        this.fotoService = fotoService;
    }

    @GetMapping({"/", "/inicio"})
    public String inicio(Model model) {
        List<Perro> perros = perroService.listarDisponibles();
        Map<Integer, String> fotosPerfil = new HashMap<>();
        for (Perro p : perros) {
            // Primero intenta foto de perfil; si no hay, usa la primera disponible
            Optional<FotoPerro> foto = fotoService.fotoPerfil(p);
            if (!foto.isPresent()) {
                List<FotoPerro> todas = fotoService.fotosDePerro(p);
                if (!todas.isEmpty()) foto = Optional.of(todas.get(0));
            }
            foto.map(FotoPerro::getUrlFoto)
                .ifPresent(url -> fotosPerfil.put(p.getIdPerro(), url));
        }
        model.addAttribute("perros", perros);
        model.addAttribute("fotosPerfil", fotosPerfil);
        model.addAttribute("totalResultados", perros.size());
        return "usuario/inicio";
    }

    @GetMapping("/nosotros")
    public String nosotros() { return "usuario/nosotros"; }

    @GetMapping("/mision")
    public String mision() { return "usuario/mision"; }
}
