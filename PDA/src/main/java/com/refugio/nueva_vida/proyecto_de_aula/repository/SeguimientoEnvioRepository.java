package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.SeguimientoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeguimientoEnvioRepository extends JpaRepository<SeguimientoEnvio, Integer> {

    List<SeguimientoEnvio> findAllByOrderByFechaDesc();

    List<SeguimientoEnvio> findByIdPedidoOrderByFechaDesc(Integer idPedido);
}