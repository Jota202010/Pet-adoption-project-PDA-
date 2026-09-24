package com.refugio.nueva_vida.proyecto_de_aula.consumers;

import com.refugio.nueva_vida.proyecto_de_aula.config.RabbitMQConfig;
import com.refugio.nueva_vida.proyecto_de_aula.dtos.AdminNotificacionDTO;
import com.refugio.nueva_vida.proyecto_de_aula.dtos.CitaNotificationDTO;
import com.refugio.nueva_vida.proyecto_de_aula.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate; // clase de spring websocket para enviar mensajes a los clientes conectados

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void procesarNotificacion(CitaNotificationDTO notificacion) {
        log.info("========================================");
        log.info("Procesando notificación en segundo plano");
        log.info("Tipo evento: {}", notificacion.getTipoEvento());
        log.info("ID Cita: {}", notificacion.getIdCita());
        log.info("Adoptante: {}", notificacion.getNombreAdoptante());
        log.info("Email destino: {}", notificacion.getEmailAdoptante());
        log.info("Perro: {}", notificacion.getNombrePerro());

        try {
            emailService.enviarNotificacion(notificacion);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo para la cita {}: {}", notificacion.getIdCita(), e.getMessage());
            
        }

            if (notificacion.getTipoEvento() == CitaNotificationDTO.TipoEvento.SOLICITUD_RECIBIDA) {
            String mensaje = "<strong>" + HtmlUtils.htmlEscape(notificacion.getNombreAdoptante()) + "</strong>"
                    + " solicitó adoptar a "
                    + "<strong>" + HtmlUtils.htmlEscape(notificacion.getNombrePerro()) + "</strong>";

            AdminNotificacionDTO avisoAdmin = AdminNotificacionDTO.builder()
                    .tipo(AdminNotificacionDTO.Tipo.CITA)
                    .icono("🐶")
                    .mensaje(mensaje)
                    .enlace("/admin/cita/" + notificacion.getIdCita())
                    .build();

            messagingTemplate.convertAndSend("/topic/admin-notificaciones", avisoAdmin);
            log.info("Aviso en vivo enviado al panel admin (WebSocket)");
      }
   }
}