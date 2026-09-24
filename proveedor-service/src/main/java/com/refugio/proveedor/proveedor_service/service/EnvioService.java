package com.refugio.proveedor.proveedor_service.service;

import com.refugio.proveedor.proveedor_service.config.RabbitMQConfig;
import com.refugio.proveedor.proveedor_service.dto.ItemPedidoDTO;
import com.refugio.proveedor.proveedor_service.dto.SeguimientoDTO;
import com.refugio.proveedor.proveedor_service.model.Envio;
import com.refugio.proveedor.proveedor_service.repository.EnvioRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void iniciarEnvio(Integer idPedido, Integer idPerro, List<ItemPedidoDTO> items) {
        if (envioRepository.findByIdPedido(idPedido).isPresent()) {
            log.info("El pedido #{} ya tiene un envío registrado.", idPedido);
            return;
        }

        Envio envio = new Envio();
        envio.setIdPedido(idPedido);
        envio.setIdPerro(idPerro);
        envio.setEstado(Envio.Estado.PREPARANDO);
        envio.setFechaActualizacion(LocalDateTime.now());
        envio.setItemsJson(serializarItems(items));

        envioRepository.save(envio);
        log.info("📦 Envío creado para el pedido #{} — PREPARANDO ({} producto(s))",
                idPedido, items == null ? 0 : items.size());
        publicarSeguimiento(envio);
    }

    @Scheduled(fixedRate = 15000)
    @Transactional
    public void avanzarEnvios() {
        List<Envio> pendientes = envioRepository.findByEstadoNot(Envio.Estado.ENTREGADO);
        if (pendientes.isEmpty()) return;

        for (Envio envio : pendientes) {
            Envio.Estado siguiente = siguienteEstado(envio.getEstado());
            if (siguiente == envio.getEstado()) continue;

            envio.setEstado(siguiente);
            envio.setFechaActualizacion(LocalDateTime.now());
            envioRepository.save(envio);

            log.info("🚚 Pedido #{} avanzó a: {}", envio.getIdPedido(), siguiente);
            publicarSeguimiento(envio);
        }
    }

    private Envio.Estado siguienteEstado(Envio.Estado actual) {
        return switch (actual) {
            case PREPARANDO   -> Envio.Estado.DESPACHADO;
            case DESPACHADO   -> Envio.Estado.EN_TRANSITO;
            case EN_TRANSITO  -> Envio.Estado.EN_REPARTO;
            case EN_REPARTO   -> Envio.Estado.ENTREGADO;
            case ENTREGADO    -> Envio.Estado.ENTREGADO;
        };
    }

    private void publicarSeguimiento(Envio envio) {
        try {
            SeguimientoDTO evento = SeguimientoDTO.builder()
                    .idPedido(envio.getIdPedido())
                    .idPerro(envio.getIdPerro())
                    .estado(envio.getEstado().name())
                    .descripcion(descripcionParaEstado(envio.getEstado(), envio.getIdPedido()))
                    .items(deserializarItems(envio.getItemsJson()))
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_SEGUIMIENTO,
                    RabbitMQConfig.ROUTING_KEY_SEGUIMIENTO,
                    evento
            );

            log.info("📡 Seguimiento publicado: pedido #{} → {}", envio.getIdPedido(), envio.getEstado());
        } catch (Exception e) {
            log.error("❌ Error publicando seguimiento: {}", e.getMessage());
        }
    }

    private String serializarItems(List<ItemPedidoDTO> items) {
        try {
            return objectMapper.writeValueAsString(items == null ? Collections.emptyList() : items);
        } catch (Exception e) {
            log.error("❌ No se pudieron serializar los items del pedido: {}", e.getMessage());
            return "[]";
        }
    }

    private List<ItemPedidoDTO> deserializarItems(String itemsJson) {
        if (itemsJson == null || itemsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(itemsJson, new TypeReference<List<ItemPedidoDTO>>() {});
        } catch (Exception e) {
            log.error("❌ No se pudieron leer los items guardados del envío: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String descripcionParaEstado(Envio.Estado estado, Integer idPedido) {
        return switch (estado) {
            case PREPARANDO  -> "Tu pedido #" + idPedido + " está siendo preparado.";
            case DESPACHADO  -> "Tu pedido #" + idPedido + " fue despachado.";
            case EN_TRANSITO -> "Tu pedido #" + idPedido + " está en tránsito.";
            case EN_REPARTO  -> "Tu pedido #" + idPedido + " está en reparto.";
            case ENTREGADO   -> "Tu pedido #" + idPedido + " fue entregado correctamente.";
        };
    }

    public Envio buscarPorPedido(Integer idPedido) {
        return envioRepository.findByIdPedido(idPedido).orElse(null);
    }

    public List<Envio> listarTodos() {
        return envioRepository.findAll();
    }
}