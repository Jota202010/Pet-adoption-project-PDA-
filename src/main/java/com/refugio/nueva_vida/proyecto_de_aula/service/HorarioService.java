package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.model.HorarioDisponible;
import com.refugio.nueva_vida.proyecto_de_aula.repository.HorarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HorarioService {

    private final HorarioRepository horarioRepository;

    public HorarioService(HorarioRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    /** Retorna todos los horarios disponibles (no ocupados) desde ahora — excluye horas pasadas de hoy */
    @Transactional(readOnly = true)
    public List<HorarioDisponible> listarDisponibles() {
        return horarioRepository.findDisponiblesDesdeAhora(LocalDate.now(), LocalTime.now());
    }

    /** El admin crea un nuevo horario disponible */
    public HorarioDisponible crear(LocalDate fecha, LocalTime hora) {
        return horarioRepository.save(new HorarioDisponible(fecha, hora));
    }

    /** El usuario confirma un horario — lo marca como ocupado y lo vincula a la cita */
    public HorarioDisponible ocupar(Integer idHorario, Cita cita) {
        HorarioDisponible horario = horarioRepository.findById(idHorario)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado"));
        if (horario.getOcupado()) {
            throw new IllegalStateException("Este horario ya fue tomado por otro usuario.");
        }
        horario.setOcupado(true);
        horario.setCita(cita);
        return horarioRepository.save(horario);
    }

    @Transactional(readOnly = true)
    public Optional<HorarioDisponible> buscarPorId(Integer id) {
        return horarioRepository.findById(id);
    }

    /** El admin elimina un horario — solo si no está vinculado a una cita */
    public void eliminar(Integer id) {
        HorarioDisponible h = horarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado"));
        if (h.getOcupado()) {
            throw new IllegalStateException(
                "No se puede eliminar un horario que ya fue reservado por un usuario.");
        }
        horarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<HorarioDisponible> listarTodos() {
        return horarioRepository.findAll();
    }
}
