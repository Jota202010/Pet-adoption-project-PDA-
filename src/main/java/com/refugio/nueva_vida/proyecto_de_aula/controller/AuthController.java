package com.refugio.nueva_vida.proyecto_de_aula.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class AuthController {

    @GetMapping("/perfil")
    public String perfilUsuario(Model model) {
        model.addAttribute("nombre", "María López");
        model.addAttribute("usuario", "maria_lopez");
        model.addAttribute("email", "maria@email.com");
        model.addAttribute("telefono", "+57 300 123 4567");
        model.addAttribute("direccion", "Calle 25 #14-32, Manga, Cartagena");
        model.addAttribute("fecha_registro", "15 de enero de 2025");

        List<Map<String, String>> citas = new ArrayList<>();

        Map<String, String> c1 = new HashMap<>();
        c1.put("perro_nombre", "Toby");
        c1.put("fecha", "2025-05-10");
        c1.put("hora", "10:00 AM");
        c1.put("sede", "Refugio Central — Cartagena");
        c1.put("estado", "En espera");
        citas.add(c1);

        Map<String, String> c2 = new HashMap<>();
        c2.put("perro_nombre", "Luna");
        c2.put("fecha", "2025-04-28");
        c2.put("hora", "02:00 PM");
        c2.put("sede", "Punto de adopción — El Bosque");
        c2.put("estado", "Aprobada");
        citas.add(c2);

        model.addAttribute("citas", citas);
        return "usuario/mi-perfil-userview";
    }
}
