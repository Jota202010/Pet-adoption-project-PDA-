package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.model.HorarioDisponible;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.repository.CitaRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CitaService {

    private final CitaRepository citaRepository;
    private final HorarioService horarioService;
    private final PerroRepository perroRepository;

    public CitaService(CitaRepository citaRepository, HorarioService horarioService,
                       PerroRepository perroRepository) {
        this.citaRepository = citaRepository;
        this.horarioService = horarioService;
        this.perroRepository = perroRepository;
    }

    public Cita guardar(Cita cita) {
        // BUG 3: bloquear solicitudes duplicadas activas para el mismo perro y usuario
        boolean yaExiste = citaRepository.findByUsuario(cita.getUsuario()).stream()
            .anyMatch(c -> c.getPerro().getIdPerro().equals(cita.getPerro().getIdPerro())
                       && c.getEstado() != Cita.EstadoCita.rechazada);
        if (yaExiste) {
            throw new IllegalStateException(
                "Ya tienes una solicitud activa para este perro. Revisa tu perfil.");
        }
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
        Cita cita = citaRepository.findById(idCita).orElseThrow();
        cita.setEstado(Cita.EstadoCita.pre_aprobada);
        cita.setAdmin(admin);
        cita.setFechaDecision(LocalDateTime.now());
        return citaRepository.save(cita);
    }

    /** Admin rechaza la solicitud — no se puede rechazar una ya confirmada */
    public Cita rechazar(Integer idCita, Usuario admin) {
        Cita cita = citaRepository.findById(idCita).orElseThrow();
        if (cita.getEstado() == Cita.EstadoCita.confirmada) {
            throw new IllegalStateException("No se puede rechazar una cita ya confirmada.");
        }
        cita.setEstado(Cita.EstadoCita.rechazada);
        cita.setAdmin(admin);
        cita.setFechaDecision(LocalDateTime.now());
        return citaRepository.save(cita);
    }

    /** Usuario elige un horario disponible → cita queda CONFIRMADA */
    public Cita confirmar(Integer idCita, Integer idHorario) {
        Cita cita = citaRepository.findById(idCita).orElseThrow();

        if (cita.getEstado() != Cita.EstadoCita.pre_aprobada) {
            throw new IllegalStateException("La cita no está pre-aprobada.");
        }

        // Marcar horario como ocupado
        HorarioDisponible horario = horarioService.ocupar(idHorario, cita);

        // Actualizar cita con fecha, hora y horario
        cita.setHorario(horario);
        cita.setFechaCita(horario.getFecha());
        cita.setHoraCita(horario.getHora());
        cita.setEstado(Cita.EstadoCita.confirmada);
        cita.setFechaDecision(LocalDateTime.now());

        // BUG 2: marcar el perro como adoptado para que no aparezca disponible
        cita.getPerro().setAdoptado(true);
        perroRepository.save(cita.getPerro());

        return citaRepository.save(cita);
    }

    @Transactional(readOnly = true)
    public long contarPendientes() {
        // Cuenta tanto en_espera (nuevas) como pre_aprobadas (esperando horario del usuario)
        long enEspera    = citaRepository.findByEstado(Cita.EstadoCita.en_espera).size();
        long preAprobadas = citaRepository.findByEstado(Cita.EstadoCita.pre_aprobada).size();
        return enEspera + preAprobadas;
    }
}
