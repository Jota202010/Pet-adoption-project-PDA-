package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.EventoBarra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import com.refugio.nueva_vida.proyecto_de_aula.model.TipoEvento;

public interface EventoBarraRepository
        extends JpaRepository<EventoBarra, Long> {

    List<EventoBarra>
    findByPerroIdPerroOrderByFechaDesc(Integer idPerro);

    /**
     * Dosis registradas (de un tipo de evento y con cierto prefijo en la
     * descripción) desde una fecha, de la más antigua a la más reciente.
     * Se usa para el tope de dosis de medicamento / desparasitante.
     */
    List<EventoBarra>
    findByPerroIdPerroAndTipoEventoAndDescripcionStartingWithAndFechaAfterOrderByFechaAsc(
            Integer idPerro, TipoEvento tipoEvento,
            String prefijoDescripcion, LocalDateTime desde);
}