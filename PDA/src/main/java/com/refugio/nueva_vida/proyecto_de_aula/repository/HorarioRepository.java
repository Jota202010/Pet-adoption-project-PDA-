package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.HorarioDisponible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<HorarioDisponible, Integer> {

    // Verifica si ya existe un horario ocupado en esa fecha y hora exacta
    boolean existsByFechaAndHoraAndOcupadoTrue(LocalDate fecha, LocalTime hora);

    // Todos los horarios de una fecha específica
    List<HorarioDisponible> findByFechaOrderByHoraAsc(LocalDate fecha);
}
