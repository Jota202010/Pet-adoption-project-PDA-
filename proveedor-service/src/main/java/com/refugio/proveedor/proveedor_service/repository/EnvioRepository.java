package com.refugio.proveedor.proveedor_service.repository;

import com.refugio.proveedor.proveedor_service.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    Optional<Envio> findByIdPedido(Integer idPedido);
    List<Envio> findByEstadoNot(Envio.Estado estado);
}
