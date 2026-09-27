package com.refugio.nueva_vida.proyecto_de_aula.model;

/**
 * Tipo de producto dentro del inventario del refugio.
 * Determina qué acción se ejecuta sobre el perro al usar "Dar al perro".
 */
public enum TipoProducto {
    VACUNA,
    ALIMENTO,
    HIDRATACION,
    DESPARASITANTE,
    MEDICAMENTO,
    ACCESORIO,
    HIGIENE,
    ESTERILIZACION,
    /** Categoría genérica de respaldo para productos con una categoría desconocida/antigua. */
    OTRO
}