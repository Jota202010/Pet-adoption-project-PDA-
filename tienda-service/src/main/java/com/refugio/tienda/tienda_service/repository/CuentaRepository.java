package com.refugio.tienda.tienda_service.repository;

import com.refugio.tienda.tienda_service.model.Cuenta;
import com.refugio.tienda.tienda_service.model.TipoCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Integer> {

    Optional<Cuenta> findByTipo(TipoCuenta tipo);
}