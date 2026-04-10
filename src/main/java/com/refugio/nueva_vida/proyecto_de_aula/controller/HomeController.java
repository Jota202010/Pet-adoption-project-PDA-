package com.refugio.nueva_vida.proyecto_de_aula.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class HomeController {

    @GetMapping({"/", "/inicio"})
    public String inicio(Model model) {
        List<Map<String, String>> perros = new ArrayList<>();

        Map<String, String> p1 = new HashMap<>();
        p1.put("nombre", "Toby"); p1.put("raza", "Labrador Retriever"); p1.put("sexo", "Macho");
        perros.add(p1);

        Map<String, String> p2 = new HashMap<>();
        p2.put("nombre", "Luna"); p2.put("raza", "Beagle"); p2.put("sexo", "Hembra");
        perros.add(p2);

        Map<String, String> p3 = new HashMap<>();
        p3.put("nombre", "Rocky"); p3.put("raza", "Pastor Alemán"); p3.put("sexo", "Macho");
        perros.add(p3);

        Map<String, String> p4 = new HashMap<>();
        p4.put("nombre", "Canela"); p4.put("raza", "Golden Retriever"); p4.put("sexo", "Hembra");
        perros.add(p4);

        model.addAttribute("perros", perros);
        model.addAttribute("totalResultados", perros.size());
        return "usuario/inicio";
    }

    @GetMapping("/nosotros")
    public String nosotros() { return "usuario/nosotros"; }

    @GetMapping("/mision")
    public String mision() { return "usuario/mision"; }

    @GetMapping("/login")
    public String login() { return "usuario/login"; }

    @GetMapping("/registro")
    public String registro() { return "usuario/registro"; }
}
