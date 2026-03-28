package com.pda.registro.service;

import com.pda.registro.model.PerroRegistro;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegistroService {

    private final List<PerroRegistro> perros = new ArrayList<>();

    public RegistroService() {
        PerroRegistro p1 = new PerroRegistro();
        p1.setNombre("Ralfi");
        p1.setRaza("Mestizado");
        p1.setEdad(8);
        p1.setPeso("25.0");
        p1.setSexo("MACHO");
        p1.setEstadoSalud("SANO");
        p1.setTemperamento("AMIGABLE");
        p1.setFechaIngreso("2026-01-10");
        p1.setResponsable("Carlos");

        PerroRegistro p2 = new PerroRegistro();
        p2.setNombre("Rocky");
        p2.setRaza("Labrador");
        p2.setEdad(5);
        p2.setPeso("27.0");
        p2.setSexo("MACHO");
        p2.setEstadoSalud("SANO");
        p2.setTemperamento("AGRESIVO");
        p2.setFechaIngreso("2026-02-05");
        p2.setResponsable("Ana");

        PerroRegistro p3 = new PerroRegistro();
        p3.setNombre("Zeus");
        p3.setRaza("Pastor Alemán");
        p3.setEdad(6);
        p3.setPeso("30.0");
        p3.setSexo("MACHO");
        p3.setEstadoSalud("SANO");
        p3.setTemperamento("TIMIDO");
        p3.setFechaIngreso("2026-03-01");
        p3.setResponsable("Juan");

        perros.add(p1);
        perros.add(p2);
        perros.add(p3);
    }

    public List<PerroRegistro> obtenerTodos() {
        return perros;
    }

    public int contarPerros() {
        return perros.size();
    }

    public void agregarPerro(PerroRegistro perro) {
        perros.add(perro);
    }
}
