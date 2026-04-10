package com.refugio.nueva_vida.proyecto_de_aula.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

@Controller
public class MascotaController {

    private List<Map<String, Object>> getPerrosFake() {
        List<Map<String, Object>> lista = new ArrayList<>();

        Map<String, Object> p1 = new HashMap<>();
        p1.put("id", 1);
        p1.put("nombre", "Toby");
        p1.put("raza", "Labrador Retriever");
        p1.put("edad", "3 años");
        p1.put("sexo", "Macho");
        p1.put("estado", "RESCATADO");
        p1.put("nivel_salud", "SANO");
        p1.put("sociabilidad", "ALTA");
        p1.put("esterilizado", "Sí");
        p1.put("vacunado", "Sí");
        p1.put("adoptado", "No");
        p1.put("listaParaAdoptar", "DISPONIBLE");
        p1.put("descripcion", "Toby es un perro alegre y lleno de energía. Le encanta jugar con pelotas y es muy amigable con niños y otros perros. Llegó al refugio hace 6 meses y está listo para encontrar su hogar definitivo.");

        List<Map<String, String>> citas1 = new ArrayList<>();
        Map<String, String> c1 = new HashMap<>();
        c1.put("humano_nombre", "María López"); c1.put("humano_email", "maria@email.com");
        c1.put("fecha", "2025-05-10"); c1.put("hora", "10:00 AM");
        c1.put("motivo", "Visita de adopción"); c1.put("estado", "EN ESPERA");
        citas1.add(c1);
        p1.put("citas", citas1);
        lista.add(p1);

        Map<String, Object> p2 = new HashMap<>();
        p2.put("id", 2);
        p2.put("nombre", "Luna");
        p2.put("raza", "Beagle");
        p2.put("edad", "1 año");
        p2.put("sexo", "Hembra");
        p2.put("estado", "ABANDONADO");
        p2.put("nivel_salud", "SANO");
        p2.put("sociabilidad", "MEDIA");
        p2.put("esterilizado", "No");
        p2.put("vacunado", "Sí");
        p2.put("adoptado", "No");
        p2.put("listaParaAdoptar", "DISPONIBLE");
        p2.put("descripcion", "Luna es una beagle curiosa y juguetona. Fue encontrada en el barrio Manga y llevada al refugio. Se lleva bien con otros perros pero necesita tiempo para adaptarse a personas nuevas.");
        p2.put("citas", new ArrayList<>());
        lista.add(p2);

        Map<String, Object> p3 = new HashMap<>();
        p3.put("id", 3);
        p3.put("nombre", "Rocky");
        p3.put("raza", "Pastor Alemán");
        p3.put("edad", "5 años");
        p3.put("sexo", "Macho");
        p3.put("estado", "STRAY");
        p3.put("nivel_salud", "ENFERMO");
        p3.put("sociabilidad", "BAJA");
        p3.put("esterilizado", "Sí");
        p3.put("vacunado", "No");
        p3.put("adoptado", "No");
        p3.put("listaParaAdoptar", "NO DISPONIBLE");
        p3.put("descripcion", "Rocky es un perro adulto que llegó con heridas menores. Está en proceso de recuperación y rehabilitación conductual. Necesita un hogar paciente y con experiencia en perros grandes.");
        p3.put("citas", new ArrayList<>());
        lista.add(p3);

        return lista;
    }

    @GetMapping("/mascota/{id}")
    public String detalleMascotaUser(@PathVariable int id, Model model) {
        List<Map<String, Object>> perros = getPerrosFake();
        Map<String, Object> perro = perros.stream()
            .filter(p -> (int)p.get("id") == id)
            .findFirst()
            .orElse(perros.get(0));
        model.addAllAttributes(perro);
        return "usuario/detalle-perro-userview";
    }

    @GetMapping("/admin/mascota/{id}")
    public String detalleMascotaAdmin(@PathVariable int id, Model model) {
        List<Map<String, Object>> perros = getPerrosFake();
        Map<String, Object> perro = perros.stream()
            .filter(p -> (int)p.get("id") == id)
            .findFirst()
            .orElse(perros.get(0));
        model.addAllAttributes(perro);
        return "privilegiado/detalle-perro-adminview";
    }

    @GetMapping("/admin/agregar-mascota")
    public String agregarMascota() {
        return "privilegiado/agregar-mascota-adminview";
    }
}
