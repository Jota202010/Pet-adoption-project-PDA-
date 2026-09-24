package com.refugio.tienda.tienda_service.service;

import com.refugio.tienda.tienda_service.config.RabbitMQConfig;
import com.refugio.tienda.tienda_service.dto.ItemPedidoDTO;
import com.refugio.tienda.tienda_service.dto.PedidoPagadoDTO;
import com.refugio.tienda.tienda_service.exception.SaldoInsuficienteException;
import com.refugio.tienda.tienda_service.model.ItemPedido;
import com.refugio.tienda.tienda_service.model.Pedido;
import com.refugio.tienda.tienda_service.model.Producto;
import com.refugio.tienda.tienda_service.model.TipoCuenta;
import com.refugio.tienda.tienda_service.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoService productoService;
    private final RabbitTemplate rabbitTemplate;

    //  Servicio de cuenta para validar / mover saldo
    private final CuentaService cuentaService;

    /**
     * Recibe el carrito (idProducto -> cantidad), valida el saldo del
     * refugio, crea el pedido, descuenta el stock de cada producto,
     * mueve el dinero entre cuentas, guarda todo y avisa a
     * proveedor-service por RabbitMQ.
     */
    public Pedido confirmarPedido(Map<Integer, Integer> carrito) {

        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío.");
        }

        Pedido pedido = new Pedido();
        pedido.setIdPerro(1);
        BigDecimal totalPedido = BigDecimal.ZERO;


        // 1. Construir el pedido y calcular el total
        for (Map.Entry<Integer, Integer> entrada : carrito.entrySet()) {
            Integer idProducto = entrada.getKey();
            Integer cantidad   = entrada.getValue();

            Producto producto = productoService.buscarPorId(idProducto)
                    .orElseThrow(() -> new IllegalStateException(
                            "Producto no encontrado: " + idProducto));

            // Descontar stock del producto
            productoService.descontarStock(idProducto, cantidad);

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setIdProducto(idProducto);
            item.setNombreProducto(producto.getNombre());
            item.setPrecioUnitario(producto.getPrecio());
            item.setCantidad(cantidad);

            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(cantidad));
            item.setSubtotal(subtotal);

            pedido.getItems().add(item);
            totalPedido = totalPedido.add(subtotal);
        }

        pedido.setTotal(totalPedido);


        // 2. Validar saldo del refugio ANTES de guardar
        BigDecimal saldoRefugio = cuentaService.obtenerSaldo(TipoCuenta.REFUGIO);

        if (saldoRefugio.compareTo(totalPedido) < 0) {
            log.warn("Saldo insuficiente. Refugio: ${} · Pedido: ${}",
                    saldoRefugio, totalPedido);
            throw new SaldoInsuficienteException(
                "El refugio no tiene saldo suficiente. " +
                "Disponible: $" + saldoRefugio +
                " · Total del pedido: $" + totalPedido
            );
        }


        // 3. Guardar el pedido
        Pedido pedidoGuardado = pedidoRepository.save(pedido);


        // 4. Mover el dinero entre cuentas

        cuentaService.descontarSaldo(TipoCuenta.REFUGIO, totalPedido);
        cuentaService.agregarSaldo(TipoCuenta.TIENDA, totalPedido);
        cuentaService.registrarTransferencia(
            TipoCuenta.REFUGIO,
            TipoCuenta.TIENDA,
            totalPedido,
            "Compra Pedido #" + pedidoGuardado.getIdPedido()
        );


        // 5. Notificar a proveedor-service por RabbitMQ

        try {
            List<ItemPedidoDTO> itemsDTO = pedidoGuardado.getItems().stream()
                    .map(item -> {
                        String categoria = productoService.buscarPorId(item.getIdProducto())
                                .map(p -> p.getCategoria().name())
                                .orElse("ACCESORIO"); // por si el producto ya no existe, no debe romper el flujo
                        return ItemPedidoDTO.builder()
                                .idProducto(item.getIdProducto())
                                .nombreProducto(item.getNombreProducto())
                                .categoria(categoria)
                                .cantidad(item.getCantidad())
                                .build();
                    })
                    .toList();

            PedidoPagadoDTO evento = PedidoPagadoDTO.builder()
                    .idPedido(pedidoGuardado.getIdPedido())
                    .idPerro(pedidoGuardado.getIdPerro())
                    .total(pedidoGuardado.getTotal())
                    .cantidadItems(pedidoGuardado.getItems().size())
                    .items(itemsDTO)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_PEDIDOS,
                    RabbitMQConfig.ROUTING_KEY_PEDIDOS,
                    evento
            );

            log.info("Pedido #{} pagado, evento enviado a proveedor-service",
                    pedidoGuardado.getIdPedido());

        } catch (Exception e) {
            log.error("No se pudo notificar a proveedor-service del pedido {}: {}",
                    pedidoGuardado.getIdPedido(), e.getMessage());
        }

        return pedidoGuardado;
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(Integer id) {
        return pedidoRepository.findById(id);
    }
}