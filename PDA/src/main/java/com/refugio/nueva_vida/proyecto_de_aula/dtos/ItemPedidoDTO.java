package com.refugio.nueva_vida.proyecto_de_aula.dtos;

import java.io.Serializable;

public class ItemPedidoDTO implements Serializable {

    private Integer idProducto;
    private String nombreProducto;
    private String categoria; // VACUNA, ALIMENTO, MEDICAMENTO, ACCESORIO, HIGIENE
    private Integer cantidad;

    public ItemPedidoDTO() {}

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
