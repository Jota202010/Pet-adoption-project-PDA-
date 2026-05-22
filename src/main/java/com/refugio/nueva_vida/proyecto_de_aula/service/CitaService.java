package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.model.HistorialEstado;
import com.refugio.nueva_vida.proyecto_de_aula.model.HorarioDisponible;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.repository.CitaRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.HistorialEstadoRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CitaService {

    private final CitaRepository            citaRepository;
    private final HorarioService            horarioService;
    private final PerroRepository           perroRepository;
    private final HistorialEstadoRepository historialRepo;

    public CitaService(CitaRepository citaRepository,
                       HorarioService horarioService,
                       PerroRepository perroRepository,
                       HistorialEstadoRepository historialRepo) {
        this.citaRepository  = citaRepository;
        this.horarioService  = horarioService;
        this.perroRepository = perroRepository;
        this.historialRepo   = historialRepo;
    }

    public Cita guardar(Cita cita) {
        // Validar que el perro existe y está disponible
        if (cita.getPerro() == null || cita.getPerro().getIdPerro() == null)
            throw new IllegalStateException("No se encontró el animal seleccionado.");
        Perro perro = perroRepository.findById(cita.getPerro().getIdPerro())
            .orElseThrow(() -> new IllegalStateException("El animal seleccionado no existe."));
        if (perro.getEstadoPublicacion() != Perro.EstadoPublicacion.PUBLICADO)
            throw new IllegalStateException("El animal '" + perro.getNombre() + "' ya no está disponible para adopción.");

        // Bloquear solicitudes duplicadas activas para el mismo perro y usuario
        boolean yaExiste = citaRepository.findByUsuario(cita.getUsuario()).stream()
            .anyMatch(c -> c.getPerro().getIdPerro().equals(perro.getIdPerro())
                       && c.getEstado() != Cita.EstadoCita.rechazada);
        if (yaExiste)
            throw new IllegalStateException(
                "¡Aviso! Ya tienes una solicitud activa para " + perro.getNombre() +
                ". Revisa tu perfil para ver el estado.");

        return citaRepository.save(cita);
    }

    @Transactional(readOnly = true)
    public List<Cita> listarTodas() { return citaRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<Cita> citasDeUsuario(Usuario usuario) { return citaRepository.findByUsuario(usuario); }

    @Transactional(readOnly = true)
    public List<Cita> citasDePerro(Perro perro) { return citaRepository.findByPerro(perro); }

    @Transactional(readOnly = true)
    public Optional<Cita> buscarPorId(Integer id) { return citaRepository.findById(id); }

    /** Admin pre-aprueba la solicitud — el usuario podrá elegir horario */
    public Cita preAprobar(Integer idCita, Usuario admin) {
        Cita cita = citaRepository.findById(idCita)
            .orElseThrow(() -> new IllegalStateException("La cita con id " + idCita + " no existe."));
        if (cita.getEstado() != Cita.EstadoCita.en_espera)
            throw new IllegalStateException(
                "Solo se puede pre-aprobar una solicitud que esté en estado 'en espera'. " +
                "Esta solicitud ya fue procesada (estado actual: " + cita.getEstado() + ").");
        cita.setEstado(Cita.EstadoCita.pre_aprobada);
        cita.setAdmin(admin);
        cita.setFechaDecision(LocalDateTime.now());
        return citaRepository.save(cita);
    }

    /** Admin rechaza la solicitud */
    public Cita rechazar(Integer idCita, Usuario admin) {
        Cita cita = citaRepository.findById(idCita)
            .orElseThrow(() -> new IllegalStateException("La cita con id " + idCita + " no existe."));
        if (cita.getEstado() == Cita.EstadoCita.confirmada)
            throw new IllegalStateException("No se puede rechazar una cita que ya fue confirmada con horario.");
        if (cita.getEstado() == Cita.EstadoCita.rechazada)
            throw new IllegalStateException("Esta solicitud ya fue rechazada anteriormente.");
        cita.setEstado(Cita.EstadoCita.rechazada);
        cita.setAdmin(admin);
        cita.setFechaDecision(LocalDateTime.now());
        return citaRepository.save(cita);
    }

    /** Usuario elige un horario disponible → cita queda CONFIRMADA */
    public Cita confirmar(Integer idCita, LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null)
            throw new IllegalStateException("Debes seleccionar una fecha y hora válidas.");

        Cita cita = citaRepository.findById(idCita)
            .orElseThrow(() -> new IllegalStateException("La cita no existe."));

        if (cita.getEstado() != Cita.EstadoCita.pre_aprobada)
            throw new IllegalStateException(
                "Esta cita no puede ser confirmada porque su estado actual es '" +
                cita.getEstado() + "'. Solo se pueden confirmar citas pre-aprobadas.");

        // Validar que la fecha sea futura
        if (fecha.isBefore(LocalDate.now()))
            throw new IllegalStateException("La fecha seleccionada ya pasó. Por favor elige una fecha futura.");

        // Crear y ocupar el horario en BD (lanza excepción si ya fue tomado)
        HorarioDisponible horario = horarioService.ocuparPorFechaHora(fecha, hora, cita);

        cita.setHorario(horario);
        cita.setFechaCita(fecha);
        cita.setHoraCita(hora);
        cita.setEstado(Cita.EstadoCita.confirmada);
        cita.setFechaDecision(LocalDateTime.now());
        return citaRepository.save(cita);
    }

    @Transactional(readOnly = true)
    public long contarPendientes() {
        return citaRepository.countByEstado(Cita.EstadoCita.en_espera)
             + citaRepository.countByEstado(Cita.EstadoCita.pre_aprobada);
    }
}
