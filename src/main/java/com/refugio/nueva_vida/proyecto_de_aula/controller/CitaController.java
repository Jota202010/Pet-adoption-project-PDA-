package com.refugio.nueva_vida.proyecto_de_aula.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

@Controller
public class CitaController {

    private Map<String, Object> getCitaFake() {
        Map<String, Object> cita = new HashMap<>();
        cita.put("id", 1);
        cita.put("fecha", "2025-05-10");
        cita.put("hora", "10:00 AM");
        cita.put("sede", "Refugio Central — Cartagena");
        cita.put("estado", "EN ESPERA");
        cita.put("fecha_solicitud", "2025-04-20");

        Map<String, String> usuario = new HashMap<>();
        usuario.put("nombre", "María López");
        usuario.put("email", "maria@email.com");
        usuario.put("usuario", "maria_lopez");
        cita.put("usuario", usuario);

        Map<String, String> perro = new HashMap<>();
        perro.put("nombre", "Toby");
        perro.put("raza", "Labrador Retriever");
        perro.put("estado", "RESCATADO");
        cita.put("perro", perro);

        cita.put("vivienda", "Casa");
        cita.put("propiedad", "Propia");
        cita.put("permiten_mascotas", "Sí");
        cita.put("num_personas", "4");
        cita.put("todos_acuerdo", "Sí");
        cita.put("perros_antes", "Sí");
        cita.put("mascotas_actual", "No");
        cita.put("mascotas_anteriores", "Tuve un perro llamado Max que falleció de vejez hace 2 años.");
        cita.put("horas_solo", "2-4 horas");
        cita.put("puede_pasear", "Sí");
        cita.put("responsable", "María López");
        cita.put("cubre_vet", "Sí");
        cita.put("cubre_emergencias", "Sí");
        cita.put("motivacion", "Quiero darle un hogar a un perro que lo necesite. Siempre he amado los animales.");
        cita.put("tipo_perro_buscado", "Perro adulto, de tamaño mediano, tranquilo y afectuoso.");
        return cita;
    }

    @GetMapping("/agendar-cita/{perroId}")
    public String agendarCita(@PathVariable int perroId, Model model) {
        model.addAttribute("nombre", "Toby");
        return "usuario/agendar-cita";
    }

    @GetMapping("/admin/cita/{id}")
    public String detalleCita(@PathVariable int id, Model model) {
        model.addAttribute("cita", getCitaFake());
        return "privilegiado/detalle-cita-admin";
    }
}
