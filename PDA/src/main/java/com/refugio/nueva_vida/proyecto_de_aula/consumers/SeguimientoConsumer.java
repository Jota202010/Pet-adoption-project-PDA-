package com.refugio.nueva_vida.proyecto_de_aula.consumers;

import com.refugio.nueva_vida.proyecto_de_aula.config.RabbitMQConfig;
import com.refugio.nueva_vida.proyecto_de_aula.dtos.ItemPedidoDTO;
import com.refugio.nueva_vida.proyecto_de_aula.dtos.SeguimientoDTO;
import com.refugio.nueva_vida.proyecto_de_aula.model.SeguimientoEnvio;
import com.refugio.nueva_vida.proyecto_de_aula.model.TipoProducto;
import com.refugio.nueva_vida.proyecto_de_aula.repository.SeguimientoEnvioRepository;
import com.refugio.nueva_vida.proyecto_de_aula.service.BarraService;
import com.refugio.nueva_vida.proyecto_de_aula.service.EmailService;
import com.refugio.nueva_vida.proyecto_de_aula.service.InventarioService;

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
    private final BarraService barraService;
    private final InventarioService inventarioService;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SEGUIMIENTO)
    public void procesarSeguimiento(SeguimientoDTO seguimiento) {
        log.info("========================================");
        log.info("📦 Seguimiento recibido: pedido #{} | perro #{} | estado: {}",
                seguimiento.getIdPedido(),
                seguimiento.getIdPerro(),
                seguimiento.getEstado());

        // 1) Guardar el registro histórico en la base de datos
        try {
            SeguimientoEnvio registro = new SeguimientoEnvio();
            registro.setIdPedido(seguimiento.getIdPedido());
            registro.setIdPerro(seguimiento.getIdPerro());
            registro.setEstado(seguimiento.getEstado());
            registro.setDescripcion(seguimiento.getDescripcion());
            registro.setFecha(LocalDateTime.now());
            seguimientoRepository.save(registro);
            log.info("💾 Registro guardado en la base de datos con éxito.");
        } catch (Exception e) {
            log.error("❌ Error al guardar en base de datos: {}", e.getMessage());
        }

        // 2) Si el estado es ENTREGADO, los productos REALES del pedido pasan
        //    al inventario del refugio (ya NO se aplican directo al perro).
        //    Desde /admin/inventario o desde la ficha del perro, el admin
        //    decide a quién dárselos con el botón "Dar al perro".
        if ("ENTREGADO".equals(seguimiento.getEstado())) {
            try {
                log.info("✅ Pedido #{} ENTREGADO. Sumando productos al inventario del refugio.",
                        seguimiento.getIdPedido());

                if (seguimiento.getItems() != null && !seguimiento.getItems().isEmpty()) {
                    for (ItemPedidoDTO item : seguimiento.getItems()) {
                        TipoProducto tipo = InventarioService.tipoDesdeCategoria(item.getCategoria());
                        inventarioService.agregarStock(
                                item.getNombreProducto(),
                                tipo,
                                item.getCantidad() != null ? item.getCantidad() : 1,
                                iconoPara(tipo)
                        );
                    }
                } else {
                    // Compatibilidad con mensajes antiguos que no traían items:
                    // se usa un paquete estándar para no perder el evento.
                    log.warn("⚠️ El pedido #{} llegó sin detalle de productos. Se usa un paquete estándar.",
                            seguimiento.getIdPedido());
                    inventarioService.agregarStock("Vacuna Antirrábica", TipoProducto.VACUNA, 1, "💉");
                    inventarioService.agregarStock("Concentrado Premium", TipoProducto.ALIMENTO, 2, "🥩");
                    inventarioService.agregarStock("Suero Oral Canino", TipoProducto.HIDRATACION, 1, "💧");
                }

                log.info("📦 Inventario actualizado con el pedido #{}.", seguimiento.getIdPedido());
            } catch (Exception e) {
                log.error("❌ Error al sumar el pedido al inventario: {}", e.getMessage());
            }
        }

        // 3) Aviso en vivo al panel admin mediante WebSocket
        try {
            Map<String, Object> aviso = new HashMap<>();
            aviso.put("mensaje", seguimiento.getDescripcion());
            aviso.put("enlace", "/admin/panel");
            aviso.put("tipo", "PEDIDO");

            messagingTemplate.convertAndSend("/topic/admin-notificaciones", aviso);
            log.info("🔔 Aviso en vivo enviado al panel admin (WebSocket)");
        } catch (Exception e) {
            log.error("❌ No se pudo enviar el aviso en vivo por WebSocket: {}", e.getMessage());
        }

        // 4) Envío de correo electrónico con HTML bonito
        try {
            emailService.enviarSeguimientoPedido(
                    seguimiento.getIdPedido(),
                    seguimiento.getEstado(),
                    seguimiento.getDescripcion()
            );
            log.info("📧 Correo de seguimiento enviado correctamente.");
        } catch (Exception e) {
            log.error("❌ No se pudo enviar el correo de seguimiento: {}", e.getMessage());
        }

        log.info("========================================");
    }

    private String iconoPara(TipoProducto tipo) {
        return switch (tipo) {
            case VACUNA -> "💉";
            case ALIMENTO -> "🥩";
            case HIDRATACION -> "💧";
            case DESPARASITANTE -> "🪱";
            case MEDICAMENTO -> "💊";
            case ACCESORIO -> "🎒";
            case HIGIENE -> "🧴";
            case OTRO -> "📦";
        };
    }
}