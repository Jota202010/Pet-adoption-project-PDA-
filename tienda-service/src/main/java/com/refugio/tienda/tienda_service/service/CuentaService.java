package com.refugio.tienda.tienda_service.service;

import com.refugio.tienda.tienda_service.dto.ComisionResumenDTO;
import com.refugio.tienda.tienda_service.dto.LiquidacionDTO;
import com.refugio.tienda.tienda_service.dto.VentanaLiquidacionDTO;
import com.refugio.tienda.tienda_service.exception.ComprobanteInvalidoException;
import com.refugio.tienda.tienda_service.exception.FueraDeVentanaException;
import com.refugio.tienda.tienda_service.exception.SaldoInsuficienteException;
import com.refugio.tienda.tienda_service.model.Cuenta;
import com.refugio.tienda.tienda_service.model.MetodoPago;
import com.refugio.tienda.tienda_service.model.TipoCuenta;
import com.refugio.tienda.tienda_service.model.Transferencia;
import com.refugio.tienda.tienda_service.repository.CuentaRepository;
import com.refugio.tienda.tienda_service.repository.PedidoRepository;
import com.refugio.tienda.tienda_service.repository.TransferenciaRepository;
import com.refugio.tienda.tienda_service.util.ComprobanteUtil;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final PedidoRepository pedidoRepository;

    private static final BigDecimal CIEN = new BigDecimal("100");

    /** Escala monetaria de las columnas (DECIMAL(15,2)). */
    private static final int ESCALA = 2;

    /**
     * Comisión (%) configurable en application.properties
     * (transferencia.comision.porcentaje) o por variable de entorno
     * TRANSFERENCIA_COMISION_PORCENTAJE.
     */
    @Value("${transferencia.comision.porcentaje:5.0}")
    private BigDecimal porcentajeComision;

    /**
     * Si es true, solo se permite UNA liquidación por día. Se apaga con
     * transferencia.liquidacion.una-por-dia=false (útil para demos y pruebas).
     */
    @Value("${transferencia.liquidacion.una-por-dia:true}")
    private boolean unaPorDia;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern(
                    "d 'de' MMMM", Locale.forLanguageTag("es-CO"));

    @PostConstruct
    void validarConfiguracionComision() {
        if (porcentajeComision == null
                || porcentajeComision.signum() < 0
                || porcentajeComision.compareTo(CIEN) >= 0) {

            throw new IllegalStateException(
                    "transferencia.comision.porcentaje debe estar entre 0 y 99.99. " +
                    "Valor actual: " + porcentajeComision);
        }
    }


    // =========================================================
    // CUENTAS
    // =========================================================

    public Cuenta obtenerCuentaPorTipo(TipoCuenta tipo) {
        return cuentaRepository.findByTipo(tipo)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe la cuenta " + tipo + ". " +
                                "Verifica que init.sql se haya ejecutado."));
    }


    public BigDecimal obtenerSaldo(TipoCuenta tipo) {
        return obtenerCuentaPorTipo(tipo).getSaldo();
    }


    public List<Transferencia> listarTransferencias() {
        return transferenciaRepository.findByOcultaFalseOrderByFechaDesc();
    }


    /**
     * Limpia la lista del historial. No borra registros: solo los oculta,
     * para que los saldos y el cálculo de lo ya liquidado no cambien.
     */
    @Transactional
    public int borrarHistorial() {
        return transferenciaRepository.ocultarTodas();
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

        /*
         * Se liquida TODO lo vendido que aún no se ha liquidado, sin
         * importar el día en que se hizo la venta. Así, lo que se venda
         * después de liquidar no se pierde: aparece en la siguiente
         * liquidación, sea mañana o dentro de varios días.
         */
        BigDecimal ventasTotales = pedidoRepository
                .findAll()
                .stream()
                .map(p -> p.getTotal() == null
                        ? BigDecimal.ZERO
                        : p.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ESCALA, RoundingMode.HALF_UP);

        /*
         * Todo lo ya liquidado (TIENDA -> REFUGIO) no se puede volver a
         * liquidar ni a cobrar comisión otra vez.
         */
        BigDecimal yaLiquidado = transferenciaRepository
                .sumarBrutoTotal(
                        TipoCuenta.TIENDA,
                        TipoCuenta.REFUGIO);

        yaLiquidado = (yaLiquidado == null ? BigDecimal.ZERO : yaLiquidado)
                .setScale(ESCALA, RoundingMode.HALF_UP);

        BigDecimal totalBruto = ventasTotales
                .subtract(yaLiquidado)
                .max(BigDecimal.ZERO);

        /*
         * Comisión real: bruto * porcentaje / 100, redondeada a peso
         * entero (COP no usa centavos) con HALF_UP y guardada con
         * escala 2. Así lo que se muestra es exactamente lo que se guarda
         * y siempre se cumple: bruto = neto + comisión.
         */
        BigDecimal comision = totalBruto
                .multiply(porcentajeComision)
                .divide(CIEN, 0, RoundingMode.HALF_UP)
                .setScale(ESCALA, RoundingMode.HALF_UP);

        BigDecimal neto = totalBruto.subtract(comision);

        return LiquidacionDTO.builder()
                .ventasTotales(ventasTotales)
                .yaLiquidado(yaLiquidado)
                .totalBruto(totalBruto)
                .porcentajeComision(porcentajeComision)
                .comisiones(comision)
                .totalARecibir(neto)
                .build();
    }


    // =========================================================
    // VENTANA DE LIQUIDACIÓN (fechas permitidas)
    // =========================================================

    public VentanaLiquidacionDTO obtenerVentanaLiquidacion() {

        LocalDate hoy = LocalDate.now();

        // Restricción apagada, o hoy todavía no se ha liquidado: se puede
        if (!unaPorDia || !yaSeLiquidoHoy(hoy)) {
            return VentanaLiquidacionDTO.builder()
                    .restringida(false)
                    .habilitada(true)
                    .build();
        }

        // Hoy ya se liquidó: se bloquea hasta mañana
        LocalDate manana = hoy.plusDays(1);

        return VentanaLiquidacionDTO.builder()
                .restringida(true)
                .habilitada(false)
                .liquidadaHoy(true)
                .dias(1)
                .desde(manana.format(FORMATO_FECHA))
                .hasta(manana.format(FORMATO_FECHA))
                .etiqueta("día para volver a liquidar")
                .titulo("Ya hiciste la liquidación de hoy")
                .detalle("Podrás volver a liquidar mañana ("
                        + manana.format(FORMATO_FECHA) + ").")
                .build();
    }


    /** true si hoy ya existe una liquidación TIENDA -> REFUGIO. */
    private boolean yaSeLiquidoHoy(LocalDate hoy) {

        return transferenciaRepository.contarEntre(
                TipoCuenta.TIENDA,
                TipoCuenta.REFUGIO,
                hoy.atStartOfDay(),
                hoy.plusDays(1).atStartOfDay()) > 0;
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

    /**
     * Todo o nada: si falla cualquier paso (saldo, cuenta inexistente,
     * guardado de la transferencia...) se revierte la transacción completa
     * y no queda ningún saldo ni registro a medias.
     */
    @Transactional(rollbackFor = Exception.class)
    public Transferencia registrarIngresoRefugio(
            MetodoPago metodoPago,
            String referenciaComprobante) {

        VentanaLiquidacionDTO ventana = obtenerVentanaLiquidacion();

        if (!ventana.isHabilitada()) {
            throw new FueraDeVentanaException(ventana.getDetalle());
        }

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

        if (transferenciaRepository
                .existsByReferenciaComprobante(comprobanteNormalizado)) {

            throw new ComprobanteInvalidoException(
                    "El comprobante \"" + comprobanteNormalizado +
                    "\" ya fue utilizado en otro ingreso. " +
                    "Genera uno nuevo con el botón «Generar»."
            );
        }

        LiquidacionDTO liquidacion = obtenerLiquidacionActual();

        BigDecimal bruto    = liquidacion.getTotalBruto();
        BigDecimal comision = liquidacion.getComisiones();
        BigDecimal neto     = liquidacion.getTotalARecibir();

        if (bruto == null || bruto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "No existe un saldo pendiente por liquidar."
            );
        }

        if (neto == null || neto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "El neto a transferir debe ser mayor que cero."
            );
        }

        // Lanza IllegalStateException si alguna de las dos no existe
        Cuenta cuentaTienda =
                obtenerCuentaPorTipo(TipoCuenta.TIENDA);

        Cuenta cuentaRefugio =
                obtenerCuentaPorTipo(TipoCuenta.REFUGIO);

        /*
         * Sale de la TIENDA solo el NETO: la comisión es ingreso de la
         * Tienda y se queda en su cuenta. Así el dinero total del sistema
         * no cambia (bruto = neto pagado + comisión retenida).
         */
        if (cuentaTienda.getSaldo().compareTo(neto) < 0) {

            throw new SaldoInsuficienteException(
                    "La Tienda no tiene saldo suficiente. " +
                    "Disponible: $" + cuentaTienda.getSaldo() +
                    " · Requerido: $" + neto
            );
        }

        /*
         * TIENDA DISMINUYE (neto)
         */
        cuentaTienda.setSaldo(
                cuentaTienda.getSaldo().subtract(neto)
        );

        /*
         * REFUGIO AUMENTA (neto)
         */
        cuentaRefugio.setSaldo(
                cuentaRefugio.getSaldo().add(neto)
        );

        cuentaRepository.save(cuentaTienda);
        cuentaRepository.save(cuentaRefugio);

        Transferencia transferencia =
                new Transferencia();

        transferencia.setCuentaOrigen(cuentaTienda);
        transferencia.setCuentaDestino(cuentaRefugio);
        transferencia.setMonto(neto);
        transferencia.setMontoBruto(bruto);
        transferencia.setPorcentajeComision(
                liquidacion.getPorcentajeComision()
                        .setScale(ESCALA, RoundingMode.HALF_UP)
        );
        transferencia.setMontoComision(comision);
        transferencia.setMetodoPago(metodoPago);
        transferencia.setReferenciaComprobante(
                comprobanteNormalizado
        );

        transferencia.setDescripcion(
                "Liquidación de la Tienda al Refugio"
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
    // COSTO DE REGISTRAR / REPONER STOCK
    // =========================================================

    /**
     * Dinero de la Tienda que se puede gastar en stock sin poner en riesgo
     * la próxima liquidación: saldo de la Tienda menos lo que hay que
     * pasarle al Refugio (neto) por las ventas que aún no se liquidan.
     * En la práctica es la comisión acumulada.
     */
    @Transactional(readOnly = true)
    public BigDecimal disponibleParaStock() {

        BigDecimal saldo = obtenerSaldo(TipoCuenta.TIENDA);

        BigDecimal pendienteNeto =
                obtenerLiquidacionActual().getTotalARecibir();

        return saldo
                .subtract(pendienteNeto == null
                        ? BigDecimal.ZERO
                        : pendienteNeto)
                .max(BigDecimal.ZERO)
                .setScale(ESCALA, RoundingMode.HALF_UP);
    }


    /**
     * Resumen de la comisión de la Tienda: cuánto lleva acumulado, cuánto
     * gastó en stock y cuánto queda disponible.
     */
    @Transactional(readOnly = true)
    public ComisionResumenDTO obtenerResumenComision() {

        BigDecimal liquidada = sinNulo(transferenciaRepository
                .sumarComision(TipoCuenta.TIENDA, TipoCuenta.REFUGIO));

        BigDecimal pendiente = sinNulo(
                obtenerLiquidacionActual().getComisiones());

        BigDecimal gastada = sinNulo(transferenciaRepository
                .sumarMonto(TipoCuenta.TIENDA, TipoCuenta.TIENDA));

        return ComisionResumenDTO.builder()
                .porcentaje(porcentajeComision)
                .liquidada(liquidada)
                .pendiente(pendiente)
                .acumulada(liquidada.add(pendiente))
                .gastadaEnStock(gastada)
                .disponible(disponibleParaStock())
                .build();
    }

    private static BigDecimal sinNulo(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor)
                .setScale(ESCALA, RoundingMode.HALF_UP);
    }


    /**
     * Cobra a la Tienda el costo de reponer stock y lo deja en el
     * historial (TIENDA -> TIENDA). Ese dinero sale del sistema porque
     * se "gasta" en el producto. Todo o nada: si no alcanza, no se
     * descuenta nada y se lanza SaldoInsuficienteException.
     */
    @Transactional(rollbackFor = Exception.class)
    public void cobrarCostoStock(BigDecimal costo, String descripcion) {

        if (costo == null || costo.signum() <= 0) {
            return;
        }

        BigDecimal disponible = disponibleParaStock();

        if (disponible.compareTo(costo) < 0) {
            throw new SaldoInsuficienteException(
                    "La Tienda no tiene comisión suficiente para reponer "
                    + "este stock. Costo: $"
                    + costo.setScale(0, RoundingMode.HALF_UP).toPlainString()
                    + " · Disponible: $"
                    + disponible.setScale(0, RoundingMode.HALF_UP)
                            .toPlainString()
                    + ". El resto del saldo es dinero de ventas por "
                    + "liquidar al Refugio."
            );
        }

        descontarSaldo(TipoCuenta.TIENDA, costo);

        String texto = descripcion == null ? "Costo de stock" : descripcion;
        if (texto.length() > 255) {
            texto = texto.substring(0, 255);
        }

        registrarTransferencia(
                TipoCuenta.TIENDA,
                TipoCuenta.TIENDA,
                costo,
                texto);
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