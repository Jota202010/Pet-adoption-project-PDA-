package com.pda.registro.service;

import com.pda.registro.model.PerroRegistro;
import org.springframework.stereotype.Service;

@Service
public class RegistroService {

    public PerroRegistro crearPerroDemo() {
        PerroRegistro perro = new PerroRegistro();
        perro.setNombre("Firulais");
        perro.setRaza("Labrador");
        perro.setEdad(3);
        perro.setFechaIngreso("2026-03-14");
        perro.setResponsable("Juan");
        return perro;
    }
}