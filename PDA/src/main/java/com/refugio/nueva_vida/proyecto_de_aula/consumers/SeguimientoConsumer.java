package com.refugio.nueva_vida.proyecto_de_aula.consumers;

import com.refugio.nueva_vida.proyecto_de_aula.config.RabbitMQConfig;
import com.refugio.nueva_vida.proyecto_de_aula.dtos.SeguimientoDTO;
import com.refugio.nueva_vida.proyecto_de_aula.model.SeguimientoEnvio;
import com.refugio.nueva_vida.proyecto_de_aula.repository.SeguimientoEnvioRepository;
import com.refugio.nueva_vida.proyecto_de_aula.service.EmailService;
import com.refugio.nueva_vida.proyecto_de_aula.service.PaqueteRecepcionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeguimientoConsumer {

    private final SeguimientoEnvioRepository seguimientoRepository;
    private final PaqueteRecepcionService paqueteRecepcionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;


    @RabbitListener(
            queues = RabbitMQConfig.QUEUE_SEGUIMIENTO
    )
    public void procesarSeguimiento(
            SeguimientoDTO seguimiento) {

        log.info(
                "📦 Seguimiento recibido: pedido #{} | estado: {}",
                seguimiento.getIdPedido(),
                seguimiento.getEstado()
        );


        // =====================================================
        // 1. GUARDAR HISTORIAL
        // =====================================================

        try {

            SeguimientoEnvio registro =
                    new SeguimientoEnvio();

            registro.setIdPedido(
                    seguimiento.getIdPedido()
            );

            registro.setIdPerro(
                    seguimiento.getIdPerro()
            );

            registro.setEstado(
                    seguimiento.getEstado()
            );

            registro.setDescripcion(
                    seguimiento.getDescripcion()
            );

            registro.setFecha(
                    LocalDateTime.now()
            );

            seguimientoRepository.save(
                    registro
            );

        } catch (Exception e) {

            log.error(
                    "❌ Error guardando seguimiento: {}",
                    e.getMessage()
            );
        }


        // =====================================================
        // 2. ENTREGADO = CREAR PAQUETE
        //
        // YA NO VA DIRECTO AL INVENTARIO
        // =====================================================

        if ("ENTREGADO".equals(
                seguimiento.getEstado())) {

            try {

                paqueteRecepcionService.recibirPaquete(
                        seguimiento.getIdPedido(),
                        seguimiento.getIdPerro(),
                        seguimiento.getItems()
                );

                log.info(
                        "📦 Pedido #{} convertido en paquete recibido.",
                        seguimiento.getIdPedido()
                );

            } catch (Exception e) {

                log.error(
                        "❌ Error creando paquete: {}",
                        e.getMessage()
                );
            }
        }


        // =====================================================
        // 3. AVISO WEBSOCKET
        // =====================================================

        try {

            Map<String, Object> aviso =
                    new HashMap<>();

            aviso.put(
                    "mensaje",
                    seguimiento.getDescripcion()
            );

            aviso.put(
                    "enlace",
                    "/admin/pedidos"
            );

            aviso.put(
                    "tipo",
                    "PEDIDO"
            );

            messagingTemplate.convertAndSend(
                    "/topic/admin-notificaciones",
                    aviso
            );

        } catch (Exception e) {

            log.error(
                    "❌ Error enviando aviso WebSocket: {}",
                    e.getMessage()
            );
        }


        // =====================================================
        // 4. EMAIL
        // =====================================================

        try {

            emailService.enviarSeguimientoPedido(
                    seguimiento.getIdPedido(),
                    seguimiento.getEstado(),
                    seguimiento.getDescripcion()
            );

        } catch (Exception e) {

            log.error(
                    "❌ Error enviando correo: {}",
                    e.getMessage()
            );
        }
    }
}