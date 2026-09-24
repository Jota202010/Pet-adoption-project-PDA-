package com.refugio.tienda.tienda_service.dto;

import lombok.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoDTO implements Serializable {
    private Integer idProducto;
    private String nombreProducto;
    private String categoria;   // VACUNA, ALIMENTO, MEDICAMENTO, ACCESORIO, HIGIENE (Producto.Categoria)
    private Integer cantidad;
}
