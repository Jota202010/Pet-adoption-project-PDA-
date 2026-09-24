package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.model.ProductoInventario;
import com.refugio.nueva_vida.proyecto_de_aula.model.TipoProducto;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.ProductoInventarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioService {

    private static final int BARRA_MAXIMA = 100;

    private final ProductoInventarioRepository inventarioRepository;
    private final PerroRepository perroRepository;
    private final BarraService barraService;

    @Transactional(readOnly = true)
    public List<ProductoInventario> listarInventario() {
        return inventarioRepository.findAllByOrderByTipoAscNombreAsc();
    }

    /**
     * Suma stock al inventario. Si ya existe un producto con el mismo
     * nombre y tipo, simplemente incrementa la cantidad; si no, lo crea.
     */
    @Transactional
    public ProductoInventario agregarStock(String nombre, TipoProducto tipo, int cantidad, String icono) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor a 0.");
        }

        ProductoInventario producto = inventarioRepository
                .findByNombreIgnoreCaseAndTipo(nombre, tipo)
                .orElseGet(() -> {
                    ProductoInventario nuevo = new ProductoInventario();
                    nuevo.setNombre(nombre);
                    nuevo.setTipo(tipo);
                    nuevo.setCantidad(0);
                    nuevo.setIcono(icono);
                    return nuevo;
                });

        producto.setCantidad(producto.getCantidad() + cantidad);
        ProductoInventario guardado = inventarioRepository.save(producto);

        log.info("📦 Inventario actualizado: {} ({}) -> {} unidades",
                guardado.getNombre(), guardado.getTipo(), guardado.getCantidad());

        return guardado;
    }

    /**
     * Le da 1 unidad del producto al perro indicado:
     * valida que la barra correspondiente tenga margen, descuenta stock
     * y aplica el efecto correspondiente en sus barras.
     *
     * Si la barra ya está al 100%, NO se descuenta stock ni se registra
     * ningún evento: se lanza un error explicando por qué, para que el
     * admin no desperdicie el producto en un perro que ya no lo necesita.
     */
    @Transactional
    public void darAlPerro(Integer idProducto, Integer idPerro) {
        ProductoInventario producto = inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en el inventario."));

        if (!producto.tieneStock()) {
            throw new IllegalStateException("No queda stock de \"" + producto.getNombre() + "\".");
        }

        Perro perro = perroRepository.findById(idPerro)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el perro #" + idPerro));

        validarBarraConMargen(producto, perro);

        aplicarEfecto(producto, idPerro);

        producto.setCantidad(producto.getCantidad() - 1);
        inventarioRepository.save(producto);

        log.info("🐶 Se entregó 1x {} al perro #{}. Quedan {} en inventario.",
                producto.getNombre(), idPerro, producto.getCantidad());
    }

    /**
     * Revisa si la barra que este producto afectaría ya está al 100%.
     * Si es así, lanza una excepción con un mensaje claro y creativo
     * para el admin, y el producto NO se descuenta del inventario.
     */
    private void validarBarraConMargen(ProductoInventario producto, Perro perro) {
        Integer valorActual;
        String nombreBarra;
        String emoji;

        switch (producto.getTipo()) {
            case VACUNA, DESPARASITANTE, MEDICAMENTO -> {
                valorActual = perro.getSalud();
                nombreBarra = "salud";
                emoji = "❤️";
            }
            case HIDRATACION -> {
                valorActual = perro.getHidratacion();
                nombreBarra = "hidratación";
                emoji = "💧";
            }
            case ALIMENTO -> {
                valorActual = perro.getNutricion();
                nombreBarra = "nutrición";
                emoji = "🍖";
            }
            default -> {
                // ACCESORIO, HIGIENE, OTRO: no llenan ninguna barra,
                // así que siempre se pueden entregar.
                return;
            }
        }

        if (valorActual != null && valorActual >= BARRA_MAXIMA) {
            throw new IllegalStateException(
                    emoji + " La barra de " + nombreBarra + " de " + perro.getNombre() +
                    " ya está al 100%. Esta acción no es posible ahora mismo — guarda \"" +
                    producto.getNombre() + "\" para cuando " + perro.getNombre() +
                    " realmente lo necesite. 🐾"
            );
        }
    }

    private void aplicarEfecto(ProductoInventario producto, Integer idPerro) {
        switch (producto.getTipo()) {
            case VACUNA -> barraService.registrarVacuna(idPerro, producto.getNombre());
            case DESPARASITANTE, MEDICAMENTO -> barraService.registrarDesparasitante(idPerro);
            case HIDRATACION -> barraService.registrarAgua(idPerro);
            case ALIMENTO -> {
                String tipoComida = producto.getNombre().toLowerCase().contains("premium")
                        ? "premium" : "normal";
                barraService.registrarComida(idPerro, tipoComida);
            }
            case ACCESORIO, HIGIENE, OTRO -> log.info(
                    "🎒 \"{}\" es un producto sin efecto en barras (accesorio/higiene); "
                    + "solo se descuenta del inventario.", producto.getNombre());
        }
    }

    /**
     * Traduce la categoría de un producto de la tienda (Producto.Categoria:
     * VACUNA, ALIMENTO, MEDICAMENTO, ACCESORIO, HIGIENE, HIDRATACION,
     * DESPARASITANTE) al TipoProducto que usa el inventario del refugio.
     * Es un mapeo 1 a 1: cada categoría de la tienda tiene su propio tipo aquí.
     */
    public static TipoProducto tipoDesdeCategoria(String categoria) {
        if (categoria == null) return TipoProducto.OTRO;
        return switch (categoria.toUpperCase()) {
            case "VACUNA" -> TipoProducto.VACUNA;
            case "ALIMENTO" -> TipoProducto.ALIMENTO;
            case "HIDRATACION", "HIDRATACIÓN" -> TipoProducto.HIDRATACION;
            case "DESPARASITANTE" -> TipoProducto.DESPARASITANTE;
            case "MEDICAMENTO" -> TipoProducto.MEDICAMENTO;
            case "ACCESORIO" -> TipoProducto.ACCESORIO;
            case "HIGIENE" -> TipoProducto.HIGIENE;
            default -> TipoProducto.OTRO;
        };
    }

    /**
     * Elimina un producto del inventario del refugio (por ejemplo, si se
     * cargó por error o ya no se va a usar). Es una eliminación definitiva.
     */
    @Transactional
    public void eliminarProducto(Integer idProducto) {
        ProductoInventario producto = inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en el inventario."));

        inventarioRepository.delete(producto);

        log.info("🗑️ Producto \"{}\" ({}) eliminado del inventario del refugio.",
                producto.getNombre(), producto.getTipo());
    }
}