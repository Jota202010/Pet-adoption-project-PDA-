package com.refugio.tienda.tienda_service.consumer;

import com.refugio.tienda.tienda_service.config.RabbitMQConfig;
import com.refugio.tienda.tienda_service.model.NotificacionTienda;
import com.refugio.tienda.tienda_service.repository.NotificacionTiendaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecetaListener {

    private final NotificacionTiendaRepository notificacionRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RECETAS)
    public void recibirReceta(Map<String, Object> payload) {
        try {
            log.info("📩 Receta recibida desde PDA: {}", payload);

            String nombrePerro      = String.valueOf(payload.getOrDefault("nombrePerro", "Un perro"));
            String tipoNecesidad    = String.valueOf(payload.getOrDefault("tipoNecesidad", "OTRO"));
            String descripcion      = String.valueOf(payload.getOrDefault("descripcion", ""));
            String productos        = String.valueOf(payload.getOrDefault("productosSugeridos", "[]"));
            String prioridad        = String.valueOf(payload.getOrDefault("prioridad", "MEDIA"));
            String categoria        = String.valueOf(payload.getOrDefault("categoriaSugerida", "ACCESORIO"));
            String productoSug      = String.valueOf(payload.getOrDefault("productoSugerido", ""));
            String precioSug        = String.valueOf(payload.getOrDefault("precioSugerido", "0"));

            NotificacionTienda noti = new NotificacionTienda();
            noti.setTitulo("🐶 Receta para " + nombrePerro + " — " + tipoNecesidad);
            noti.setMensaje(descripcion);
            noti.setProductosSugeridos(productos);

            // 🆕 Guardar más datos en el mensaje
            noti.setMensaje(descripcion
                + "\n\n📦 Producto: " + productoSug
                + "\n🏷️ Categoría: " + categoria
                + "\n💰 Precio sugerido: $" + precioSug);

            try {
                noti.setPrioridad(NotificacionTienda.Prioridad.valueOf(prioridad));
            } catch (IllegalArgumentException e) {
                noti.setPrioridad(NotificacionTienda.Prioridad.MEDIA);
            }

            noti.setLeida(false);

            notificacionRepository.save(noti);

            log.info("✅ Notificación guardada (perro={})", nombrePerro);

        } catch (Exception e) {
            log.error("❌ Error procesando receta: {}", e.getMessage(), e);
        }
    }
}