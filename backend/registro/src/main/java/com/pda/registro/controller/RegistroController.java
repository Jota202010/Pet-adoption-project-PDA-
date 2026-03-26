package com.pda.registro.controller;

import com.pda.registro.model.PerroRegistro;
import com.pda.registro.service.RegistroService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistroController {


    private final RegistroService registroService;

    public RegistroController(RegistroService registroService) {
        this.registroService = registroService;
    }

    @GetMapping("/registro")
    public String registrar() {
        PerroRegistro perro = registroService.crearPerroDemo();
        return "Perro registrado: " + perro.getNombre() +
                ", raza: " + perro.getRaza() +
                ", edad: " + perro.getEdad() +
                ", fecha ingreso: " + perro.getFechaIngreso() +
                ", responsable: " + perro.getResponsable();
    }
}