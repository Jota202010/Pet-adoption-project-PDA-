package com.refugio.nueva_vida.proyecto_de_aula.dtos;

import java.io.Serializable;
import java.util.List; // Importación necesaria para la lista

public class SeguimientoDTO implements Serializable {

    private Integer idPedido;
    private Integer idPerro;
    private String estado;
    private String descripcion;
    private List<ItemPedidoDTO> items; // <-- PROPIEDAD FALTANTE AGREGADA

    public SeguimientoDTO() {}

    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public Integer getIdPerro() { return idPerro; }
    public void setIdPerro(Integer idPerro) { this.idPerro = idPerro; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // <-- MÉTODOS GETTER Y SETTER FALTANTES AGREGADOS
    public List<ItemPedidoDTO> getItems() { return items; }
    public void setItems(List<ItemPedidoDTO> items) { this.items = items; }
}
