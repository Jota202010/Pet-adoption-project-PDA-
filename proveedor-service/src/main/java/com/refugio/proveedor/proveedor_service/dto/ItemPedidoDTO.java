package com.refugio.proveedor.proveedor_service.dto;

import lombok.*;
import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoDTO implements Serializable {
    private Integer idProducto;
    private String nombreProducto;
    private String categoria;
    private Integer cantidad;
}
