package com.refugio.nueva_vida.proyecto_de_aula.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un producto disponible en el inventario del refugio
 * (lo que llegó del proveedor y aún no se le ha dado a ningún perro).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "producto_inventario")
public class ProductoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoProducto tipo;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 0;

    /** Emoji/ícono para mostrar en la tarjeta del inventario. */
    @Column(name = "icono", length = 10)
    private String icono;

    public boolean tieneStock() {
        return cantidad != null && cantidad > 0;
    }
}
