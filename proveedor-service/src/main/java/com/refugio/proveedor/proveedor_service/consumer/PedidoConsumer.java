package com.refugio.proveedor.proveedor_service.consumer;

import com.refugio.proveedor.proveedor_service.config.RabbitMQConfig;
import com.refugio.proveedor.proveedor_service.dto.PedidoPagadoDTO;
import com.refugio.proveedor.proveedor_service.service.EnvioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoConsumer {

    private final EnvioService envioService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDOS_PAGADOS)
    public void procesarPedidoPagado(PedidoPagadoDTO pedido) {
        log.info("Nuevo pedido pagado recibido: #{} | perro #{} | {} producto(s)",
                pedido.getIdPedido(), pedido.getIdPerro(),
                pedido.getItems() == null ? 0 : pedido.getItems().size());
        envioService.iniciarEnvio(pedido.getIdPedido(), pedido.getIdPerro(), pedido.getItems());
    }
}