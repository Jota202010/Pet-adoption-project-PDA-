package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.service.PerroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

@Controller
public class AdminController {

    private final PerroService perroService;

    public AdminController(PerroService perroService) {
        this.perroService = perroService;
    }

    // ── Datos falsos temporales para usuarios y citas (aún no conectados a BD) ──

    private List<Map<String, String>> getUsuariosFake() {
        List<Map<String, String>> lista = new ArrayList<>();
        String[][] datos = {
            {"María López",    "maria@email.com",   "12 ene 2025"},
            {"Carlos Pérez",   "carlos@email.com",  "20 feb 2025"},
            {"Ana Martínez",   "ana@email.com",      "05 mar 2025"}
        };
        for (String[] d : datos) {
            Map<String, String> u = new HashMap<>();
            u.put("nombre", d[0]); u.put("email", d[1]); u.put("fecha_registro", d[2]);
            lista.add(u);
        }
        return lista;
    }

    private List<Map<String, Object>> getCitasFake() {
        List<Map<String, Object>> lista = new ArrayList<>();
        Object[][] datos = {
            {"María López",  "maria@email.com",  "Toby",   "2025-05-10", "10:00 AM", "En espera"},
            {"Carlos Pérez", "carlos@email.com", "Luna",   "2025-05-12", "02:00 PM", "Aprobada"},
            {"Ana Martínez", "ana@email.com",    "Canela", "2025-05-15", "11:00 AM", "Rechazada"}
        };
        for (Object[] d : datos) {
            Map<String, Object> c = new HashMap<>();
            Map<String, String> user = new HashMap<>();
            user.put("nombre", (String)d[0]); user.put("email", (String)d[1]);
            Map<String, String> perro = new HashMap<>();
            perro.put("nombre", (String)d[2]);
            c.put("usuario", user); c.put("perro", perro);
            c.put("fecha", d[3]); c.put("hora", d[4]); c.put("estado", d[5]);
            lista.add(c);
        }
        return lista;
    }

    // ── Panel principal ───────────────────────────────────────────────────────

    @GetMapping("/admin/panel")
    public String panelAdmin(Model model) {
        List<Perro> perros = perroService.listarTodos();
        model.addAttribute("perros", perros);
        model.addAttribute("totalPerros", perros.size());
        model.addAttribute("usuarios", getUsuariosFake());
        model.addAttribute("citas", getCitasFake());
        model.addAttribute("totalUsuarios", getUsuariosFake().size());
        model.addAttribute("citasPendientes", 12);
        return "privilegiado/panel-general-adminview";
    }

    // ── Perfil de administrador ───────────────────────────────────────────────

    @GetMapping("/admin/perfil")
    public String perfilAdmin(Model model) {
        Map<String, String> admin = new HashMap<>();
        admin.put("nombre", "Administrador Principal");
        admin.put("usuario", "admin_refugio");
        admin.put("email", "admin@refugionuevavida.com");
        admin.put("telefono", "+57 300 999 0001");
        admin.put("fecha_registro", "10 de enero de 2019");
        model.addAttribute("admin", admin);
        model.addAttribute("total_aprobadas", 47);
        model.addAttribute("total_rechazadas", 13);
        model.addAttribute("total_espera", 12);

        List<Map<String, Object>> citasAprobadas = new ArrayList<>();
        List<Map<String, Object>> citasRechazadas = new ArrayList<>();
        for (Map<String, Object> c : getCitasFake()) {
            String estado = (String)c.get("estado");
            c.put("fecha_decision", "2025-05-01");
            if ("Aprobada".equals(estado)) citasAprobadas.add(c);
            else if ("Rechazada".equals(estado)) citasRechazadas.add(c);
        }
        model.addAttribute("citasAprobadas", citasAprobadas);
        model.addAttribute("citasRechazadas", citasRechazadas);
        return "privilegiado/mi-perfil-adminview";
    }

    // ── Detalle de usuario ────────────────────────────────────────────────────

    @GetMapping("/admin/usuario/{id}")
    public String detalleUsuario(@PathVariable String id, Model model) {
        model.addAttribute("nombre", "María López");
        model.addAttribute("usuario", "maria_lopez");
        model.addAttribute("email", "maria@email.com");
        model.addAttribute("telefono", "+57 300 123 4567");
        model.addAttribute("direccion", "Calle 25 #14-32, Manga, Cartagena");
        model.addAttribute("fecha_registro", "12 de enero de 2025");

        List<Map<String, String>> citas = new ArrayList<>();
        Map<String, String> c1 = new HashMap<>();
        c1.put("perro_nombre", "Toby"); c1.put("perro_raza", "Labrador Retriever");
        c1.put("fecha", "2025-05-10"); c1.put("hora", "10:00 AM");
        c1.put("motivo", "Visita de adopción"); c1.put("estado", "EN ESPERA");
        citas.add(c1);
        model.addAttribute("citas", citas);
        return "privilegiado/detalle-usuario-adminview";
    }
}
