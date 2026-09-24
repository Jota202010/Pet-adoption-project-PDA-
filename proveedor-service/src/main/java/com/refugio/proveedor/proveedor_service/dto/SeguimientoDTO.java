package com.refugio.proveedor.proveedor_service.dto;

import lombok.*;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoDTO implements Serializable {
    private Integer idPedido;
    private Integer idPerro;
    private String estado;
    private String descripcion;
    private List<ItemPedidoDTO> items;
}