package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.model.HorarioDisponible;
import com.refugio.nueva_vida.proyecto_de_aula.repository.HorarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class HorarioService {

    private final HorarioRepository horarioRepository;

    // Horarios fijos: mañana 8:00–12:00 y tarde 13:00–16:00 cada 30 min
    private static final List<LocalTime> SLOTS_MANANA = List.of(
        LocalTime.of(8,  0), LocalTime.of(8,  30),
        LocalTime.of(9,  0), LocalTime.of(9,  30),
        LocalTime.of(10, 0), LocalTime.of(10, 30),
        LocalTime.of(11, 0), LocalTime.of(11, 30)
    );
    private static final List<LocalTime> SLOTS_TARDE = List.of(
        LocalTime.of(13, 0), LocalTime.of(13, 30),
        LocalTime.of(14, 0), LocalTime.of(14, 30),
        LocalTime.of(15, 0), LocalTime.of(15, 30)
    );

    public HorarioService(HorarioRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    /**
     * Genera los slots disponibles para los próximos 'diasHabiles' días hábiles (lun-vie).
     * Un slot es disponible si NO existe ya un HorarioDisponible ocupado con esa fecha+hora.
     * Los objetos retornados NO están persistidos — son solo para mostrar en la vista.
     */
    @Transactional(readOnly = true)
    public List<HorarioDisponible> generarSlotsDisponibles(int diasHabiles) {
        List<HorarioDisponible> slots = new ArrayList<>();
        LocalDate fecha    = LocalDate.now();
        LocalTime ahora    = LocalTime.now();
        int diasContados   = 0;

        while (diasContados < diasHabiles) {
            DayOfWeek dia = fecha.getDayOfWeek();

            // Solo lunes a viernes
            if (dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY) {

                List<LocalTime> todosSlots = new ArrayList<>(SLOTS_MANANA);
                todosSlots.addAll(SLOTS_TARDE);

                for (LocalTime hora : todosSlots) {
                    // Saltar slots pasados de hoy
                    if (fecha.equals(LocalDate.now()) && !hora.isAfter(ahora)) continue;
                    // Saltar si ya está ocupado
                    if (horarioRepository.existsByFechaAndHoraAndOcupadoTrue(fecha, hora)) continue;

                    slots.add(new HorarioDisponible(fecha, hora));
                }
                diasContados++;
            }
            fecha = fecha.plusDays(1);
        }
        return slots;
    }

    /**
     * Cuando el usuario confirma un slot, lo crea en BD y lo marca como ocupado.
     * Lanza IllegalStateException si el slot ya fue tomado (race condition).
     */
    public HorarioDisponible ocuparPorFechaHora(LocalDate fecha, LocalTime hora, Cita cita) {
        if (horarioRepository.existsByFechaAndHoraAndOcupadoTrue(fecha, hora)) {
            throw new IllegalStateException(
                "Este horario acaba de ser tomado por otro usuario. Por favor elige otro.");
        }
        HorarioDisponible horario = new HorarioDisponible(fecha, hora);
        horario.setOcupado(true);
        horario.setCita(cita);
        return horarioRepository.save(horario);
    }
}
