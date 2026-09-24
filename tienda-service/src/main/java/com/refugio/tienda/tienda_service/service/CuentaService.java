package com.refugio.tienda.tienda_service.service;

import com.refugio.tienda.tienda_service.exception.SaldoInsuficienteException;
import com.refugio.tienda.tienda_service.model.Cuenta;
import com.refugio.tienda.tienda_service.model.TipoCuenta;
import com.refugio.tienda.tienda_service.model.Transferencia;
import com.refugio.tienda.tienda_service.repository.CuentaRepository;
import com.refugio.tienda.tienda_service.repository.TransferenciaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;


    // CONSULTAS

    public Cuenta obtenerCuentaPorTipo(TipoCuenta tipo) {
        return cuentaRepository.findByTipo(tipo)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + tipo));
    }

    public BigDecimal obtenerSaldo(TipoCuenta tipo) {
        return obtenerCuentaPorTipo(tipo).getSaldo();
    }

    public List<Transferencia> listarTransferencias() {
        return transferenciaRepository.findAllByOrderByFechaDesc();
    }

    public boolean saldoBajo() {
        return obtenerCuentaPorTipo(TipoCuenta.REFUGIO)
                .getSaldo()
                .compareTo(new BigDecimal("50000")) < 0;
    }

    
    // TRANSFERIR (mueve saldo + registra historial)

    @Transactional
    public Transferencia transferir(TipoCuenta origen,
                                    TipoCuenta destino,
                                    BigDecimal monto,
                                    String descripcion) {

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SaldoInsuficienteException("El monto debe ser mayor a cero.");
        }

        Cuenta cuentaOrigen  = obtenerCuentaPorTipo(origen);
        Cuenta cuentaDestino = obtenerCuentaPorTipo(destino);

        if (cuentaOrigen.getSaldo().compareTo(monto) < 0) {
            throw new SaldoInsuficienteException(
                "Saldo insuficiente. Disponible: $" + cuentaOrigen.getSaldo()
                + " · Solicitado: $" + monto
            );
        }

        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(monto));
        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(monto));

        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestino);

        Transferencia transferencia = new Transferencia();
        transferencia.setCuentaOrigen(cuentaOrigen);
        transferencia.setCuentaDestino(cuentaDestino);
        transferencia.setMonto(monto);
        transferencia.setDescripcion(descripcion);

        return transferenciaRepository.save(transferencia);
    }
    // DESCONTAR / AGREGAR SALDO (uso interno desde pedidos)

    @Transactional
    public void descontarSaldo(TipoCuenta tipo, BigDecimal monto) {
        Cuenta cuenta = obtenerCuentaPorTipo(tipo);

        if (cuenta.getSaldo().compareTo(monto) < 0) {
            throw new SaldoInsuficienteException(
                "Saldo insuficiente en " + tipo + ". Disponible: $" + cuenta.getSaldo()
            );
        }

        cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
        cuentaRepository.save(cuenta);
    }

    @Transactional
    public void agregarSaldo(TipoCuenta tipo, BigDecimal monto) {
        Cuenta cuenta = obtenerCuentaPorTipo(tipo);
        cuenta.setSaldo(cuenta.getSaldo().add(monto));
        cuentaRepository.save(cuenta);
    }

    // ═════════════════════════════════════════════════════
    // REGISTRAR TRANSFERENCIA (solo historial, NO mueve saldo)
    // Útil cuando ya movimos el saldo manualmente desde PedidoService
    // ═════════════════════════════════════════════════════

    @Transactional
    public Transferencia registrarTransferencia(TipoCuenta origen,
                                                TipoCuenta destino,
                                                BigDecimal monto,
                                                String descripcion) {

        Cuenta cuentaOrigen  = obtenerCuentaPorTipo(origen);
        Cuenta cuentaDestino = obtenerCuentaPorTipo(destino);

        Transferencia t = new Transferencia();
        t.setCuentaOrigen(cuentaOrigen);
        t.setCuentaDestino(cuentaDestino);
        t.setMonto(monto);
        t.setDescripcion(descripcion);

        return transferenciaRepository.save(t);
    }
}