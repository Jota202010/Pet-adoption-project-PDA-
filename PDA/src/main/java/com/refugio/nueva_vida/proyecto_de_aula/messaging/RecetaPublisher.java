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

    private String categoriaSegunNecesidad(String necesidad) {
        return switch (necesidad) {
            case "NUTRICION"       -> "ALIMENTO";
            case "HIDRATACION"     -> "MEDICAMENTO";
            case "VACUNACION"      -> "VACUNA";
            case "DESPARASITACION" -> "MEDICAMENTO";
            default                -> "ACCESORIO";
        };
    }

    private String productoSegunNecesidad(String necesidad) {
        return switch (necesidad) {
            case "NUTRICION"       -> "Concentrado Premium";
            case "HIDRATACION"     -> "Suero Oral Canino";
            case "VACUNACION"      -> "Vacuna Antirrábica";
            case "DESPARASITACION" -> "Desparasitante Canino";
            default                -> "Producto para el refugio";
        };
    }

    private int precioSegunNecesidad(String necesidad) {
        return switch (necesidad) {
            case "NUTRICION"       -> 45000;
            case "HIDRATACION"     -> 12000;
            case "VACUNACION"      -> 25000;
            case "DESPARASITACION" -> 18000;
            default                -> 20000;
        };
    }
}