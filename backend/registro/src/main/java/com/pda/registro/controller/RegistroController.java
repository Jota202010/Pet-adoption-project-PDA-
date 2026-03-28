package com.pda.registro.controller;

import com.pda.registro.model.PerroRegistro;
import com.pda.registro.service.RegistroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pda")
public class RegistroController {

    private final RegistroService registroService;

    public RegistroController(RegistroService registroService) {
        this.registroService = registroService;
    }

   
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    
    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    
    @GetMapping("/refugio")
    public String refugio(Model model) {
        model.addAttribute("perros", registroService.obtenerTodos());
        model.addAttribute("totalPerros", registroService.contarPerros());
        return "refugio";
    }

    
    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("nuevoPerro", new PerroRegistro());
        model.addAttribute("perros", registroService.obtenerTodos());
        model.addAttribute("totalPerros", registroService.contarPerros());
        return "admin";
    }

    @PostMapping("/admin/agregar")
    public String agregarPerro(@ModelAttribute("nuevoPerro") PerroRegistro perro) {
        registroService.agregarPerro(perro);
        return "redirect:/admin";
    }
}
