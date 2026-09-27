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

    /** El baño completo (mojar + espumar + aclarar) gasta 3 unidades de higiene. */
    private static final int UNIDADES_POR_BANO = 3;

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
     * Le da el producto al perro indicado: valida que la barra
     * correspondiente tenga margen, descuenta stock y aplica el efecto
     * correspondiente en sus barras.
     *
     * Normalmente descuenta 1 unidad, excepto los productos de HIGIENE
     * (un baño completo — mojar, espumar, aclarar — gasta
     * {@value #UNIDADES_POR_BANO} unidades de una sola vez).
     *
     * Si la barra ya está al 100%, o si no hay stock suficiente, NO se
     * descuenta nada ni se registra ningún evento: se lanza un error
     * explicando por qué, para que el admin no desperdicie el producto.
     */
    @Transactional
    public void darAlPerro(Integer idProducto, Integer idPerro) {
        ProductoInventario producto = inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en el inventario."));

        int unidadesNecesarias = producto.getTipo() == TipoProducto.HIGIENE
                ? UNIDADES_POR_BANO
                : 1;

        if (producto.getCantidad() == null || producto.getCantidad() < unidadesNecesarias) {
            if (producto.getTipo() == TipoProducto.HIGIENE) {
                throw new IllegalStateException(
                        "🛁 Un baño completo gasta " + UNIDADES_POR_BANO + " unidades de \"" +
                        producto.getNombre() + "\" y solo quedan " +
                        (producto.getCantidad() == null ? 0 : producto.getCantidad()) + ".");
            }
            throw new IllegalStateException("No queda stock de \"" + producto.getNombre() + "\".");
        }

        Perro perro = perroRepository.findById(idPerro)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el perro #" + idPerro));

        validarBarraConMargen(producto, perro);

        aplicarEfecto(producto, idPerro);

        producto.setCantidad(producto.getCantidad() - unidadesNecesarias);
        inventarioRepository.save(producto);

        log.info("🐶 Se entregó {}x {} al perro #{}. Quedan {} en inventario.",
                unidadesNecesarias, producto.getNombre(), idPerro, producto.getCantidad());
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
            case ACCESORIO -> {
                valorActual = perro.getEnergia();
                nombreBarra = "energía";
                emoji = "⚡";
            }
            case HIGIENE -> {
                valorActual = perro.getHidratacion();
                nombreBarra = "hidratación";
                emoji = "💧";
            }
            default -> {
                // OTRO: no llena ninguna barra, siempre se puede entregar.
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
            case ACCESORIO -> barraService.registrarJuego(idPerro);
            case HIGIENE -> barraService.registrarBano(idPerro);
            case ESTERILIZACION -> barraService.registrarEsterilizacion(idPerro);
            case OTRO -> log.info(
                    "📦 \"{}\" es un producto sin efecto en barras; "
                    + "solo se descuenta del inventario.", producto.getNombre());
        }
    }

    /**
     * Traduce la categoría de un producto de la tienda (Producto.Categoria:
     * VACUNA, ALIMENTO, MEDICAMENTO, ACCESORIO, HIGIENE, HIDRATACION,
     * DESPARASITANTE, ESTERILIZACION) al TipoProducto que usa el inventario
     * del refugio. Es un mapeo 1 a 1: cada categoría de la tienda tiene su
     * propio tipo aquí.
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
            case "ESTERILIZACION", "ESTERILIZACIÓN" -> TipoProducto.ESTERILIZACION;
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