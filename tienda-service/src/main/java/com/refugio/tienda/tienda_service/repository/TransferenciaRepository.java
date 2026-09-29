package com.refugio.tienda.tienda_service.repository;

import com.refugio.tienda.tienda_service.model.TipoCuenta;
import com.refugio.tienda.tienda_service.model.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransferenciaRepository extends JpaRepository<Transferencia, Integer> {

    List<Transferencia> findAllByOrderByFechaDesc();

    /** Historial visible (las ocultadas con "Borrar historial" no salen). */
    List<Transferencia> findByOcultaFalseOrderByFechaDesc();

    /** Oculta todo el historial visible. Devuelve cuántos registros ocultó. */
    @Modifying
    @Query("update Transferencia t set t.oculta = true where t.oculta = false")
    int ocultarTodas();

    /**
     * Cuenta las transferencias entre dos tipos de cuenta en un rango de
     * fechas. Se usa para saber si ya se hizo una liquidación hoy.
     */
    @Query("""
            select count(t)
            from Transferencia t
            where t.cuentaOrigen.tipo = :origen
              and t.cuentaDestino.tipo = :destino
              and t.fecha >= :desde
              and t.fecha < :hasta
            """)
    long contarEntre(@Param("origen") TipoCuenta origen,
                     @Param("destino") TipoCuenta destino,
                     @Param("desde") LocalDateTime desde,
                     @Param("hasta") LocalDateTime hasta);

    /** true si ya existe una transferencia con ese comprobante. */
    boolean existsByReferenciaComprobante(String referenciaComprobante);

    /**
     * Suma el valor BRUTO liquidado de todos los tiempos entre dos tipos
     * de cuenta (sin límite de mes). Para registros antiguos (sin
     * monto_bruto) usa "monto". Devuelve null si no hay registros.
     */
    @Query("""
            select sum(coalesce(t.montoBruto, t.monto))
            from Transferencia t
            where t.cuentaOrigen.tipo = :origen
              and t.cuentaDestino.tipo = :destino
            """)
    BigDecimal sumarBrutoTotal(@Param("origen") TipoCuenta origen,
                               @Param("destino") TipoCuenta destino);

    /**
     * Suma el valor BRUTO ya liquidado entre dos tipos de cuenta en un rango.
     * Para registros antiguos (sin monto_bruto) usa "monto", que entonces
     * era el bruto porque no existía comisión.
     * Devuelve null si no hay registros.
     */
    @Query("""
            select sum(coalesce(t.montoBruto, t.monto))
            from Transferencia t
            where t.cuentaOrigen.tipo = :origen
              and t.cuentaDestino.tipo = :destino
              and t.fecha >= :desde
              and t.fecha < :hasta
            """)
    BigDecimal sumarBrutoEntre(@Param("origen") TipoCuenta origen,
                               @Param("destino") TipoCuenta destino,
                               @Param("desde") LocalDateTime desde,
                               @Param("hasta") LocalDateTime hasta);


    /**
     * Suma la comisión cobrada entre dos tipos de cuenta (todos los tiempos,
     * incluidos los registros ocultos del historial).
     * Devuelve null si no hay registros.
     */
    @Query("""
            select sum(t.montoComision)
            from Transferencia t
            where t.cuentaOrigen.tipo = :origen
              and t.cuentaDestino.tipo = :destino
            """)
    BigDecimal sumarComision(@Param("origen") TipoCuenta origen,
                             @Param("destino") TipoCuenta destino);

    /**
     * Suma el monto movido entre dos tipos de cuenta (todos los tiempos).
     * Con TIENDA -> TIENDA da lo gastado en reponer stock.
     * Devuelve null si no hay registros.
     */
    @Query("""
            select sum(t.monto)
            from Transferencia t
            where t.cuentaOrigen.tipo = :origen
              and t.cuentaDestino.tipo = :destino
            """)
    BigDecimal sumarMonto(@Param("origen") TipoCuenta origen,
                          @Param("destino") TipoCuenta destino);
}