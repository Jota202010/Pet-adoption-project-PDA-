package com.refugio.tienda.tienda_service.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoPagadoDTO implements Serializable {
    private Integer idPedido;
    private Integer idPerro;
    private BigDecimal total;
    private Integer cantidadItems;
    private List<ItemPedidoDTO> items;
}