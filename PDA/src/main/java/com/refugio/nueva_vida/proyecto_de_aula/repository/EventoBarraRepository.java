package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.EventoBarra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoBarraRepository
        extends JpaRepository<EventoBarra, Long> {

    List<EventoBarra>
    findByPerroIdPerroOrderByFechaDesc(Integer idPerro);
}