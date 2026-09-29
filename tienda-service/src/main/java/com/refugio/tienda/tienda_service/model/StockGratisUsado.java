package com.refugio.tienda.tienda_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Guarda qué categorías ya usaron su stock inicial gratis.
 * Una fila por categoría (categoría única). Si una categoría NO tiene
 * fila, todavía tiene su stock gratis disponible, aunque ya existan
 * productos creados antes.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stock_gratis_usado")
public class StockGratisUsado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock_gratis")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private Producto.Categoria categoria;

    /** Producto que consumió el gratis (solo informativo). */
    @Column(name = "nombre_producto", length = 150)
    private String nombreProducto;

    @Column(name = "fecha_uso", nullable = false, updatable = false)
    private LocalDateTime fechaUso = LocalDateTime.now();
}