package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.refugio.nueva_vida.proyecto_de_aula.dtos.ItemPedidoDTO;
import com.refugio.nueva_vida.proyecto_de_aula.model.PaqueteRecepcion;
import com.refugio.nueva_vida.proyecto_de_aula.model.TipoProducto;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PaqueteRecepcionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaqueteRecepcionService {

    private final PaqueteRecepcionRepository paqueteRepository;

    private final InventarioService inventarioService;

    private final ObjectMapper objectMapper;


    // =========================================================
    // RECIBIR PAQUETE
    // =========================================================

    @Transactional
    public PaqueteRecepcion recibirPaquete(
            Integer idPedido,
            Integer idPerro,
            List<ItemPedidoDTO> items) {

        if (paqueteRepository.findByIdPedido(idPedido).isPresent()) {

            log.info(
                    "📦 El paquete del pedido #{} ya existe.",
                    idPedido
            );

            return paqueteRepository
                    .findByIdPedido(idPedido)
                    .orElseThrow();
        }

        PaqueteRecepcion paquete =
                new PaqueteRecepcion();

        paquete.setIdPedido(idPedido);
        paquete.setIdPerro(idPerro);
        paquete.setEstado(
                PaqueteRecepcion.Estado.RECIBIDO
        );
        paquete.setFechaRecepcion(
                LocalDateTime.now()
        );

        paquete.setItemsJson(
                serializarItems(items)
        );

        return paqueteRepository.save(paquete);
    }


    // =========================================================
    // LISTAR
    // =========================================================

    @Transactional(readOnly = true)
    public List<PaqueteRecepcion> listarPaquetes() {

        List<PaqueteRecepcion> paquetes =
                paqueteRepository
                        .findAllByOrderByFechaRecepcionDesc();

        for (PaqueteRecepcion paquete : paquetes) {

            paquete.setItems(
                    deserializarItems(
                            paquete.getItemsJson()
                    )
            );
        }

        return paquetes;
    }


    // =========================================================
    // ABRIR
    // =========================================================

    @Transactional
    public void abrirPaquete(Integer idPaquete) {

        PaqueteRecepcion paquete =
                obtener(idPaquete);

        if (paquete.getEstado() ==
                PaqueteRecepcion.Estado.ENVIADO_INVENTARIO) {

            throw new IllegalStateException(
                    "Este paquete ya fue enviado al inventario."
            );
        }

        paquete.setEstado(
                PaqueteRecepcion.Estado.ABIERTO
        );

        paquete.setFechaApertura(
                LocalDateTime.now()
        );

        paqueteRepository.save(paquete);

        log.info(
                "📦 Paquete #{} abierto.",
                idPaquete
        );
    }


    // =========================================================
    // ENVIAR AL INVENTARIO
    // =========================================================

    @Transactional
    public void enviarAlInventario(Integer idPaquete) {

        PaqueteRecepcion paquete =
                obtener(idPaquete);

        if (paquete.getEstado() ==
                PaqueteRecepcion.Estado.RECIBIDO) {

            throw new IllegalStateException(
                    "Primero debes abrir el paquete."
            );
        }

        if (paquete.getEstado() ==
                PaqueteRecepcion.Estado.ENVIADO_INVENTARIO) {

            throw new IllegalStateException(
                    "Los productos de este paquete ya están en el inventario."
            );
        }

        List<ItemPedidoDTO> items =
                deserializarItems(
                        paquete.getItemsJson()
                );

        if (items.isEmpty()) {

            throw new IllegalStateException(
                    "El paquete no contiene productos."
            );
        }

        for (ItemPedidoDTO item : items) {

            TipoProducto tipo =
                    InventarioService.tipoDesdeCategoria(
                            item.getCategoria()
                    );

            int cantidad =
                    item.getCantidad() == null
                            ? 1
                            : item.getCantidad();

            inventarioService.agregarStock(
                    item.getNombreProducto(),
                    tipo,
                    cantidad,
                    iconoPara(tipo)
            );
        }

        paquete.setEstado(
                PaqueteRecepcion.Estado.ENVIADO_INVENTARIO
        );

        paquete.setFechaEnvioInventario(
                LocalDateTime.now()
        );

        paqueteRepository.save(paquete);

        log.info(
                "📦➡️📦 Productos del paquete #{} enviados al inventario.",
                idPaquete
        );
    }


    // =========================================================
    // BUSCAR
    // =========================================================

    private PaqueteRecepcion obtener(
            Integer idPaquete) {

        return paqueteRepository
                .findById(idPaquete)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Paquete no encontrado."
                        )
                );
    }


    // =========================================================
    // JSON
    // =========================================================

    private String serializarItems(
            List<ItemPedidoDTO> items) {

        try {

            return objectMapper.writeValueAsString(
                    items == null
                            ? Collections.emptyList()
                            : items
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "No se pudo guardar el contenido del paquete.",
                    e
            );
        }
    }


    private List<ItemPedidoDTO> deserializarItems(
            String json) {

        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }

        try {

            return objectMapper.readValue(
                    json,
                    new TypeReference<List<ItemPedidoDTO>>() {}
            );

        } catch (Exception e) {

            log.error(
                    "Error leyendo contenido del paquete: {}",
                    e.getMessage()
            );

            return Collections.emptyList();
        }
    }


    private String iconoPara(
            TipoProducto tipo) {

        return switch (tipo) {

            case VACUNA -> "💉";

            case ALIMENTO -> "🥩";

            case HIDRATACION -> "💧";

            case DESPARASITANTE -> "🪱";

            case MEDICAMENTO -> "💊";

            case ACCESORIO -> "🎒";

            case HIGIENE -> "🧴";

            case ESTERILIZACION -> "✂️";

            case OTRO -> "📦";
        };
    }
}