package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.HorarioDisponible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<HorarioDisponible, Integer> {

    // Solo los horarios libres (ocupado = false) a partir de hoy,
    // excluyendo horas pasadas del día de hoy
    @Query("SELECT h FROM HorarioDisponible h WHERE h.ocupado = false AND " +
           "(h.fecha > :fecha OR (h.fecha = :fecha AND h.hora > :hora)) " +
           "ORDER BY h.fecha ASC, h.hora ASC")
    List<HorarioDisponible> findDisponiblesDesdeAhora(LocalDate fecha, LocalTime hora);

    // Todos los horarios de una fecha específica
    List<HorarioDisponible> findByFechaOrderByHoraAsc(LocalDate fecha);
}
