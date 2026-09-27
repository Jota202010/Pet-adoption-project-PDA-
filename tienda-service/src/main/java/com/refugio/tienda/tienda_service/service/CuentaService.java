package com.refugio.tienda.tienda_service.service;

import com.refugio.tienda.tienda_service.dto.LiquidacionDTO;
import com.refugio.tienda.tienda_service.exception.ComprobanteInvalidoException;
import com.refugio.tienda.tienda_service.exception.SaldoInsuficienteException;
import com.refugio.tienda.tienda_service.model.Cuenta;
import com.refugio.tienda.tienda_service.model.MetodoPago;
import com.refugio.tienda.tienda_service.model.TipoCuenta;
import com.refugio.tienda.tienda_service.model.Transferencia;
import com.refugio.tienda.tienda_service.repository.CuentaRepository;
import com.refugio.tienda.tienda_service.repository.PedidoRepository;
import com.refugio.tienda.tienda_service.repository.TransferenciaRepository;
import com.refugio.tienda.tienda_service.util.ComprobanteUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final PedidoRepository pedidoRepository;


    // =========================================================
    // CUENTAS
    // =========================================================

    public Cuenta obtenerCuentaPorTipo(TipoCuenta tipo) {
        return cuentaRepository.findByTipo(tipo)
                .orElseThrow(() ->
                        new RuntimeException("Cuenta no encontrada: " + tipo));
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


    // =========================================================
    // LIQUIDACIÓN
    // =========================================================

    @Transactional(readOnly = true)
    public LiquidacionDTO obtenerLiquidacionActual() {

        LocalDate hoy = LocalDate.now();

        LocalDateTime inicioMes = hoy
                .withDayOfMonth(1)
                .atStartOfDay();

        LocalDateTime finMes = hoy
                .plusDays(1)
                .atStartOfDay();

        BigDecimal ventasMes = pedidoRepository
                .findByFechaPedidoBetween(inicioMes, finMes)
                .stream()
                .map(p -> p.getTotal() == null
                        ? BigDecimal.ZERO
                        : p.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        /*
         * Actualmente el proyecto no tiene una regla de comisión
         * configurada.
         *
         * Por eso queda en cero.
         */
        BigDecimal comisiones = BigDecimal.ZERO;

        BigDecimal ajustes = BigDecimal.ZERO;

        BigDecimal totalARecibir = ventasMes
                .subtract(comisiones)
                .add(ajustes);

        return LiquidacionDTO.builder()
                .ventasMes(ventasMes)
                .comisiones(comisiones)
                .ajustes(ajustes)
                .totalARecibir(totalARecibir)
                .build();
    }


    // =========================================================
    // COMPROBANTE
    // =========================================================

    /**
     * Genera un número de comprobante nuevo y válido,
     * listo para usarse en el formulario de ingreso.
     */
    public String generarComprobante() {
        return ComprobanteUtil.generar();
    }


    // =========================================================
    // NUEVO:
    // TIENDA → REFUGIO
    // =========================================================

    @Transactional
    public Transferencia registrarIngresoRefugio(
            MetodoPago metodoPago,
            String referenciaComprobante) {

        if (metodoPago == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un método de pago."
            );
        }

        if (referenciaComprobante == null
                || referenciaComprobante.isBlank()) {

            throw new ComprobanteInvalidoException(
                    "Debes ingresar un número de comprobante. " +
                    "Usa el botón «Generar» para crear uno válido " +
                    "automáticamente."
            );
        }

        if (!ComprobanteUtil.esValido(referenciaComprobante)) {

            throw new ComprobanteInvalidoException(
                    "El comprobante \"" + referenciaComprobante.trim() +
                    "\" no es válido. Verifica que lo hayas copiado " +
                    "completo o genera uno nuevo con el botón «Generar»."
            );
        }

        String comprobanteNormalizado =
                ComprobanteUtil.normalizar(referenciaComprobante);

        LiquidacionDTO liquidacion = obtenerLiquidacionActual();

        BigDecimal monto = liquidacion.getTotalARecibir();

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "No existe un saldo pendiente por liquidar."
            );
        }

        Cuenta cuentaTienda =
                obtenerCuentaPorTipo(TipoCuenta.TIENDA);

        Cuenta cuentaRefugio =
                obtenerCuentaPorTipo(TipoCuenta.REFUGIO);

        if (cuentaTienda.getSaldo().compareTo(monto) < 0) {

            throw new SaldoInsuficienteException(
                    "La Tienda no tiene saldo suficiente. " +
                    "Disponible: $" + cuentaTienda.getSaldo() +
                    " · Requerido: $" + monto
            );
        }

        /*
         * TIENDA DISMINUYE
         */
        cuentaTienda.setSaldo(
                cuentaTienda.getSaldo().subtract(monto)
        );

        /*
         * REFUGIO AUMENTA
         */
        cuentaRefugio.setSaldo(
                cuentaRefugio.getSaldo().add(monto)
        );

        cuentaRepository.save(cuentaTienda);
        cuentaRepository.save(cuentaRefugio);

        Transferencia transferencia =
                new Transferencia();

        transferencia.setCuentaOrigen(cuentaTienda);
        transferencia.setCuentaDestino(cuentaRefugio);
        transferencia.setMonto(monto);
        transferencia.setMetodoPago(metodoPago);
        transferencia.setReferenciaComprobante(
                comprobanteNormalizado
        );

        transferencia.setDescripcion(
                "Liquidación mensual de la Tienda al Refugio"
        );

        return transferenciaRepository.save(
                transferencia
        );
    }


    // =========================================================
    // MÉTODOS EXISTENTES UTILIZADOS POR PEDIDOS
    // =========================================================

    @Transactional
    public void descontarSaldo(
            TipoCuenta tipo,
            BigDecimal monto) {

        Cuenta cuenta =
                obtenerCuentaPorTipo(tipo);

        if (cuenta.getSaldo().compareTo(monto) < 0) {

            throw new SaldoInsuficienteException(
                    "Saldo insuficiente en " + tipo +
                    ". Disponible: $" + cuenta.getSaldo()
            );
        }

        cuenta.setSaldo(
                cuenta.getSaldo().subtract(monto)
        );

        cuentaRepository.save(cuenta);
    }


    @Transactional
    public void agregarSaldo(
            TipoCuenta tipo,
            BigDecimal monto) {

        Cuenta cuenta =
                obtenerCuentaPorTipo(tipo);

        cuenta.setSaldo(
                cuenta.getSaldo().add(monto)
        );

        cuentaRepository.save(cuenta);
    }


    // =========================================================
    // HISTORIAL DE TRANSFERENCIAS
    // =========================================================

    @Transactional
    public Transferencia registrarTransferencia(
            TipoCuenta origen,
            TipoCuenta destino,
            BigDecimal monto,
            String descripcion) {

        Cuenta cuentaOrigen =
                obtenerCuentaPorTipo(origen);

        Cuenta cuentaDestino =
                obtenerCuentaPorTipo(destino);

        Transferencia t =
                new Transferencia();

        t.setCuentaOrigen(cuentaOrigen);
        t.setCuentaDestino(cuentaDestino);
        t.setMonto(monto);
        t.setDescripcion(descripcion);

        return transferenciaRepository.save(t);
    }
}
