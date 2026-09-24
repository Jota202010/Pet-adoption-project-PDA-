package com.refugio.tienda.tienda_service.repository;

import com.refugio.tienda.tienda_service.model.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferenciaRepository extends JpaRepository<Transferencia, Integer> {

    List<Transferencia> findAllByOrderByFechaDesc();
}