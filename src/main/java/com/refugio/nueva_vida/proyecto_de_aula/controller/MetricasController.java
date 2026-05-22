package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.repository.CitaRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Duration;
import java.util.List;
import java.util.OptionalDouble;

@Controller
public class MetricasController {

    private final PerroRepository  perroRepository;
    private final CitaRepository   citaRepository;
    private final UsuarioRepository usuarioRepository;

    public MetricasController(PerroRepository perroRepository,
                              CitaRepository citaRepository,
                              UsuarioRepository usuarioRepository) {
        this.perroRepository  = perroRepository;
        this.citaRepository   = citaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/admin/metricas")
    public String metricas(Model model) {

        List<Perro> todosPerros  = perroRepository.findAll();
        List<Cita>  todasCitas   = citaRepository.findAll();

        // ── Métricas de animales ─────────────────────────────────────────────
        long totalPerros      = todosPerros.size();
        long adoptados        = todosPerros.stream().filter(p -> p.getEstadoPublicacion() == Perro.EstadoPublicacion.ADOPTADO).count();
        long disponibles      = todosPerros.stream().filter(p -> p.getEstadoPublicacion() == Perro.EstadoPublicacion.PUBLICADO).count();
        long listosAdoptar    = disponibles;
        long enProceso        = todosPerros.stream().filter(p -> p.getEstadoPublicacion() == Perro.EstadoPublicacion.EN_PROCESO).count();
        long devueltos        = todosPerros.stream().filter(p -> p.getEstadoPublicacion() == Perro.EstadoPublicacion.DEVUELTO).count();
        long vacunados        = todosPerros.stream().filter(Perro::getVacunado).count();
        long esterilizados    = todosPerros.stream().filter(Perro::getEsterilizado).count();

        // Por nivel de salud
        long sanos    = todosPerros.stream().filter(p -> p.getNivelSalud() == Perro.NivelSalud.SANO).count();
        long enfermos = todosPerros.stream().filter(p -> p.getNivelSalud() == Perro.NivelSalud.ENFERMO).count();
        long criticos = todosPerros.stream().filter(p -> p.getNivelSalud() == Perro.NivelSalud.CRITICO).count();

        // Por estado de publicación (para reemplazar la gráfica de salud)
        long epEnRefugio = todosPerros.stream().filter(p -> p.getEstadoPublicacion() == Perro.EstadoPublicacion.EN_REFUGIO).count();
        long epPublicado = todosPerros.stream().filter(p -> p.getEstadoPublicacion() == Perro.EstadoPublicacion.PUBLICADO).count();
        long epEnProceso = todosPerros.stream().filter(p -> p.getEstadoPublicacion() == Perro.EstadoPublicacion.EN_PROCESO).count();
        long epAdoptado  = todosPerros.stream().filter(p -> p.getEstadoPublicacion() == Perro.EstadoPublicacion.ADOPTADO).count();
        long epDevuelto  = todosPerros.stream().filter(p -> p.getEstadoPublicacion() == Perro.EstadoPublicacion.DEVUELTO).count();

        // Por estado de origen
        long rescatados = todosPerros.stream().filter(p -> p.getEstado() == Perro.Estado.RESCATADO).count();
        long abandonados = todosPerros.stream().filter(p -> p.getEstado() == Perro.Estado.ABANDONADO).count();
        long acogidos   = todosPerros.stream().filter(p -> p.getEstado() == Perro.Estado.ACOGIDO).count();

        // ── Métricas de citas ────────────────────────────────────────────────
        long totalCitas    = todasCitas.size();
        long enEspera      = todasCitas.stream().filter(c -> c.getEstado() == Cita.EstadoCita.en_espera).count();
        long preAprobadas  = todasCitas.stream().filter(c -> c.getEstado() == Cita.EstadoCita.pre_aprobada).count();
        long confirmadas   = todasCitas.stream().filter(c -> c.getEstado() == Cita.EstadoCita.confirmada).count();
        long rechazadas    = todasCitas.stream().filter(c -> c.getEstado() == Cita.EstadoCita.rechazada).count();

        // Tasa de aprobación (citas confirmadas sobre el total con decisión tomada)
        long conDecision    = confirmadas + rechazadas;
        long pctAprobacion  = conDecision == 0 ? 0 : Math.round((double) confirmadas / conDecision * 100);

        // Tiempo promedio de respuesta del admin en horas
        OptionalDouble promedioHoras = todasCitas.stream()
                .filter(c -> c.getFechaDecision() != null && c.getFechaSolicitud() != null)
                .mapToLong(c -> Duration.between(c.getFechaSolicitud(), c.getFechaDecision()).toHours())
                .average();
        long horasPromedio = promedioHoras.isPresent() ? Math.round(promedioHoras.getAsDouble()) : 0;

        // ── Métricas de usuarios ─────────────────────────────────────────────
        long totalUsuarios = usuarioRepository.count();

        // ── Porcentajes para barras visuales ─────────────────────────────────
        long pctVacunados    = totalPerros == 0 ? 0 : Math.round((double) vacunados    / totalPerros * 100);
        long pctEsterilizados = totalPerros == 0 ? 0 : Math.round((double) esterilizados / totalPerros * 100);
        long pctAdoptados    = totalPerros == 0 ? 0 : Math.round((double) adoptados    / totalPerros * 100);

        model.addAttribute("enProceso",  enProceso);
        model.addAttribute("devueltos",  devueltos);

        // ── Pasar al modelo ──────────────────────────────────────────────────
        model.addAttribute("totalPerros",      totalPerros);
        model.addAttribute("adoptados",        adoptados);
        model.addAttribute("disponibles",      disponibles);
        model.addAttribute("listosAdoptar",    listosAdoptar);
        model.addAttribute("vacunados",        vacunados);
        model.addAttribute("esterilizados",    esterilizados);
        model.addAttribute("pctVacunados",     pctVacunados);
        model.addAttribute("pctEsterilizados", pctEsterilizados);
        model.addAttribute("pctAdoptados",     pctAdoptados);

        // Salud (para gráfica)
        model.addAttribute("sanos",    sanos);
        model.addAttribute("enfermos", enfermos);
        model.addAttribute("criticos", criticos);
        model.addAttribute("epEnRefugio", epEnRefugio);
        model.addAttribute("epPublicado", epPublicado);
        model.addAttribute("epEnProceso", epEnProceso);
        model.addAttribute("epAdoptado",  epAdoptado);
        model.addAttribute("epDevuelto",  epDevuelto);

        // Estado origen (para gráfica)
        model.addAttribute("rescatados", rescatados);
        model.addAttribute("abandonados", abandonados);
        model.addAttribute("acogidos",   acogidos);

        // Citas
        model.addAttribute("totalCitas",   totalCitas);
        model.addAttribute("enEspera",     enEspera);
        model.addAttribute("preAprobadas", preAprobadas);
        model.addAttribute("confirmadas",  confirmadas);
        model.addAttribute("rechazadas",   rechazadas);
        model.addAttribute("pctAprobacion", pctAprobacion);
        model.addAttribute("horasPromedio", horasPromedio);

        // Usuarios
        model.addAttribute("totalUsuarios", totalUsuarios);

        return "privilegiado/metricas-adminview";
    }
}
