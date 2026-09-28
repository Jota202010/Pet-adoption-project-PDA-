package com.refugio.nueva_vida.proyecto_de_aula.messaging;

import com.refugio.nueva_vida.proyecto_de_aula.config.RabbitMQConfig;
import com.refugio.nueva_vida.proyecto_de_aula.model.Receta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecetaPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicar(Receta receta) {

        Map<String, Object> payload = construirPayload(receta);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_RECETAS,
                RabbitMQConfig.ROUTING_KEY_RECETAS,
                payload
        );

        log.info("📤 Receta publicada: id={}, perro={}, necesidad={}",
                receta.getIdReceta(),
                receta.getPerro().getNombre(),
                receta.getTipoNecesidad());
    }

    /**
     * Le pide a la tienda que cree la notificación de esta receta y ESPERA
     * su respuesta (máx. 5 segundos). La tienda contesta con un mapa que
     * trae la clave "estado": "CREADA" si la creó, o "YA_EXISTE" si la
     * notificación ya estaba. Devuelve null si la tienda no respondió.
     */
    public Map<String, Object> solicitarEnvio(Receta receta) {

        Map<String, Object> payload = construirPayload(receta);

        Object respuesta = rabbitTemplate.convertSendAndReceive(
                RabbitMQConfig.EXCHANGE_RECETAS,
                RabbitMQConfig.ROUTING_KEY_RECETAS_SOLICITUD,
                payload,
                mensaje -> {
                    // Si la tienda está apagada, el mensaje caduca y no
                    // se procesa más tarde de forma inesperada.
                    mensaje.getMessageProperties().setExpiration("5000");
                    return mensaje;
                }
        );

        log.info("📤 Solicitud de envío de receta: id={}, perro={}, respuesta={}",
                receta.getIdReceta(),
                receta.getPerro().getNombre(),
                respuesta);

        if (respuesta instanceof Map<?, ?> mapa) {

            Map<String, Object> resultado = new HashMap<>();

            mapa.forEach((k, v) -> resultado.put(String.valueOf(k), v));

            return resultado;
        }

        return null;
    }

    private Map<String, Object> construirPayload(Receta receta) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("idReceta", receta.getIdReceta());
        payload.put("idPerro", receta.getPerro().getIdPerro());
        payload.put("nombrePerro", receta.getPerro().getNombre());
        payload.put("tipoNecesidad", receta.getTipoNecesidad().name());
        payload.put("descripcion", receta.getDescripcion());
        payload.put("productosSugeridos", receta.getProductosSugeridos());
        payload.put("prioridad", receta.getPrioridad().name());
        payload.put("fechaGeneracion", receta.getFechaGeneracion().toString());

        // 🆕 Categoría sugerida según el tipo
        payload.put("categoriaSugerida", categoriaSegunNecesidad(receta.getTipoNecesidad().name()));

        // 🆕 Producto + Precio sugerido
        payload.put("productoSugerido", productoSegunNecesidad(receta.getTipoNecesidad().name()));
        payload.put("precioSugerido", precioSegunNecesidad(receta.getTipoNecesidad().name()));

        return payload;
    }

    /**
     * Avisa a la tienda que una receta ya no hace falta (por ejemplo,
     * porque se reseteó la barra del perro), para que borre su
     * notificación.
     */
    public void publicarResuelta(Receta receta) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("idReceta", receta.getIdReceta());
        payload.put("idPerro", receta.getPerro().getIdPerro());
        payload.put("nombrePerro", receta.getPerro().getNombre());
        payload.put("tipoNecesidad", receta.getTipoNecesidad().name());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_RECETAS,
                RabbitMQConfig.ROUTING_KEY_RECETAS_RESUELTAS,
                payload
        );

        log.info("📤 Receta resuelta publicada: id={}, perro={}, necesidad={}",
                receta.getIdReceta(),
                receta.getPerro().getNombre(),
                receta.getTipoNecesidad());
    }

    private String categoriaSegunNecesidad(String necesidad) {
        return switch (necesidad) {
            case "NUTRICION"       -> "ALIMENTO";
            case "HIDRATACION"     -> "MEDICAMENTO";
            case "VACUNACION"      -> "VACUNA";
            case "DESPARASITACION" -> "MEDICAMENTO";
            case "ESTERILIZACION"  -> "ESTERILIZACION";
            default                -> "ACCESORIO";
        };
    }

    private String productoSegunNecesidad(String necesidad) {
        return switch (necesidad) {
            case "NUTRICION"       -> "Concentrado Premium";
            case "HIDRATACION"     -> "Suero Oral Canino";
            case "VACUNACION"      -> "Vacuna Antirrábica";
            case "DESPARASITACION" -> "Desparasitante Canino";
            case "ESTERILIZACION"  -> "Cirugía de esterilización";
            default                -> "Producto para el refugio";
        };
    }

    private int precioSegunNecesidad(String necesidad) {
        return switch (necesidad) {
            case "NUTRICION"       -> 45000;
            case "HIDRATACION"     -> 12000;
            case "VACUNACION"      -> 25000;
            case "DESPARASITACION" -> 18000;
            case "ESTERILIZACION"  -> 20000;
            default                -> 20000;
        };
    }
}