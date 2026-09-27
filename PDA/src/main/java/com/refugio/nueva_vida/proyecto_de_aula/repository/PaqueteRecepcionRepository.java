package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.PaqueteRecepcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaqueteRecepcionRepository
        extends JpaRepository<PaqueteRecepcion, Integer> {

    Optional<PaqueteRecepcion> findByIdPedido(Integer idPedido);

    List<PaqueteRecepcion> findAllByOrderByFechaRecepcionDesc();
}