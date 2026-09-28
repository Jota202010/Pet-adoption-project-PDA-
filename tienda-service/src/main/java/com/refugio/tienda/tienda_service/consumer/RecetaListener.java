package com.refugio.tienda.tienda_service.consumer;

import com.refugio.tienda.tienda_service.config.RabbitMQConfig;
import com.refugio.tienda.tienda_service.model.NotificacionTienda;
import com.refugio.tienda.tienda_service.repository.NotificacionTiendaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
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

            crearNotificacion(payload);

        } catch (Exception e) {
            log.error("❌ Error procesando receta: {}", e.getMessage(), e);
        }
    }

    /**
     * PDA pide (botón "Crear Receta") que la receta aparezca en las
     * notificaciones de la tienda y espera una respuesta:
     *  - "YA_EXISTE": la notificación ya estaba, no se duplica.
     *  - "CREADA": no estaba (por ejemplo, se eliminó por error) y se creó.
     *  - "ERROR": algo falló al procesarla.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_RECETAS_SOLICITUD)
    public Map<String, Object> recibirSolicitudReceta(Map<String, Object> payload) {

        Map<String, Object> respuesta = new HashMap<>();

        try {
            log.info("📩 Solicitud de envío de receta desde PDA: {}", payload);

            Object idRecetaObj = payload.get("idReceta");

            // 1) ¿Ya existe una notificación de esta receta?
            if (idRecetaObj instanceof Number) {

                List<NotificacionTienda> porId =
                        notificacionRepository.findByIdReceta(
                                ((Number) idRecetaObj).longValue());

                if (!porId.isEmpty()) {
                    respuesta.put("estado", "YA_EXISTE");
                    return respuesta;
                }
            }

            // 2) Respaldo: notificación antigua (sin idReceta) del mismo
            //    perro y tipo. Se "adopta" guardándole el idReceta para no duplicarla.
            String nombrePerro   = String.valueOf(payload.getOrDefault("nombrePerro", "Un perro"));
            String tipoNecesidad = String.valueOf(payload.getOrDefault("tipoNecesidad", "OTRO"));
            String titulo = "🐶 Receta para " + nombrePerro + " — " + tipoNecesidad;

            List<NotificacionTienda> antiguas =
                    notificacionRepository.findByIdRecetaIsNullAndLeidaFalseAndTitulo(titulo);

            if (!antiguas.isEmpty() && idRecetaObj instanceof Number) {

                long idReceta = ((Number) idRecetaObj).longValue();

                for (NotificacionTienda antigua : antiguas) {
                    antigua.setIdReceta(idReceta);
                }

                notificacionRepository.saveAll(antiguas);

                respuesta.put("estado", "YA_EXISTE");
                return respuesta;
            }

            // 3) No existe: se crea de nuevo
            crearNotificacion(payload);

            respuesta.put("estado", "CREADA");
            return respuesta;

        } catch (Exception e) {
            log.error("❌ Error procesando solicitud de receta: {}", e.getMessage(), e);
            respuesta.put("estado", "ERROR");
            return respuesta;
        }
    }

    /**
     * Crea y guarda la notificación de la tienda a partir de los datos
     * de la receta que manda PDA.
     */
    private void crearNotificacion(Map<String, Object> payload) {

        String nombrePerro      = String.valueOf(payload.getOrDefault("nombrePerro", "Un perro"));
        String tipoNecesidad    = String.valueOf(payload.getOrDefault("tipoNecesidad", "OTRO"));
        String descripcion      = String.valueOf(payload.getOrDefault("descripcion", ""));
        String productos        = String.valueOf(payload.getOrDefault("productosSugeridos", "[]"));
        String prioridad        = String.valueOf(payload.getOrDefault("prioridad", "MEDIA"));
        String categoria        = String.valueOf(payload.getOrDefault("categoriaSugerida", "ACCESORIO"));
        String productoSug      = String.valueOf(payload.getOrDefault("productoSugerido", ""));
        String precioSug        = String.valueOf(payload.getOrDefault("precioSugerido", "0"));

        NotificacionTienda noti = new NotificacionTienda();

        Object idRecetaObj = payload.get("idReceta");
        if (idRecetaObj instanceof Number) {
            noti.setIdReceta(((Number) idRecetaObj).longValue());
        }

        noti.setTitulo("🐶 Receta para " + nombrePerro + " — " + tipoNecesidad);
        noti.setProductosSugeridos(productos);

        // Guardar más datos en el mensaje
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
    }

    /**
     * PDA avisa que una receta ya no hace falta (se reseteó la barra del
     * perro). Se elimina la notificación de esa receta en la tienda.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_RECETAS_RESUELTAS)
    public void recibirRecetaResuelta(Map<String, Object> payload) {
        try {
            log.info("📩 Receta resuelta recibida desde PDA: {}", payload);

            Object idRecetaObj = payload.get("idReceta");

            if (idRecetaObj instanceof Number) {

                List<NotificacionTienda> porId =
                        notificacionRepository.findByIdReceta(
                                ((Number) idRecetaObj).longValue());

                if (!porId.isEmpty()) {
                    notificacionRepository.deleteAll(porId);
                    log.info("🗑️ Notificaciones eliminadas por idReceta: {}", porId.size());
                    return;
                }
            }

            // Respaldo: notificaciones antiguas que no guardaron el idReceta
            String nombrePerro   = String.valueOf(payload.getOrDefault("nombrePerro", "Un perro"));
            String tipoNecesidad = String.valueOf(payload.getOrDefault("tipoNecesidad", "OTRO"));
            String titulo = "🐶 Receta para " + nombrePerro + " — " + tipoNecesidad;

            List<NotificacionTienda> porTitulo =
                    notificacionRepository.findByIdRecetaIsNullAndLeidaFalseAndTitulo(titulo);

            if (!porTitulo.isEmpty()) {
                notificacionRepository.deleteAll(porTitulo);
                log.info("🗑️ Notificaciones antiguas eliminadas por título: {}", porTitulo.size());
            }

        } catch (Exception e) {
            log.error("❌ Error procesando receta resuelta: {}", e.getMessage(), e);
        }
    }
}