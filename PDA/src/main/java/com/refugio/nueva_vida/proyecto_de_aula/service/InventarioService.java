package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.EventoBarra;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.model.ProductoInventario;
import com.refugio.nueva_vida.proyecto_de_aula.model.TipoEvento;
import com.refugio.nueva_vida.proyecto_de_aula.model.TipoProducto;
import com.refugio.nueva_vida.proyecto_de_aula.repository.EventoBarraRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.ProductoInventarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioService {

    private static final int BARRA_MAXIMA = 100;

    /** El baño completo (mojar + espumar + aclarar) gasta 2 unidades de higiene. */
    private static final int UNIDADES_POR_BANO = 2;

    /** Tope de dosis dentro de la ventana de {@value #VENTANA_DOSIS_HORAS} horas. */
    private static final int MAX_DOSIS_MEDICAMENTO = 3;
    private static final int MAX_DOSIS_DESPARASITANTE = 2;
    private static final int VENTANA_DOSIS_HORAS = 24;

    private final EventoBarraRepository eventoRepository;
    private final ProductoInventarioRepository inventarioRepository;
    private final PerroRepository perroRepository;
    private final BarraService barraService;
    private final RecetaService recetaService;

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
        validarTopeDeDosis(producto, perro);

        aplicarEfecto(producto, idPerro);

        // El producto se entregó: cerrar la receta que ese producto atendía
        // (nutrición, hidratación, desparasitación/medicamento, etc.).
        recetaService.resolverPorProductoEntregado(perro, producto.getTipo());

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
        // Vacuna: si el perro ya está vacunado no tiene sentido dársela otra vez.
        if (producto.getTipo() == TipoProducto.VACUNA
                && Boolean.TRUE.equals(perro.getVacunado())) {
            throw new IllegalStateException(
                    "💉 " + perro.getNombre() + " ya está vacunado. No es necesario darle \"" +
                    producto.getNombre() + "\" — guárdala para otro perro que la necesite. 🐾");
        }

        // Esterilización: si ya está esterilizado, no se repite la cirugía.
        if (producto.getTipo() == TipoProducto.ESTERILIZACION
                && Boolean.TRUE.equals(perro.getEsterilizado())) {
            throw new IllegalStateException(
                    "✂️ " + perro.getNombre() + " ya está esterilizado. No es necesario repetir " +
                    "la esterilización — guarda \"" + producto.getNombre() +
                    "\" para otro perro. 🐾");
        }

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

    /**
     * Validaciones extra para MEDICAMENTO y DESPARASITANTE:
     *  1) Si la salud ya llegó al máximo que su historial permite (le falta
     *     vacuna o esterilización), más medicina no sirve: se le dice qué necesita.
     *  2) Tope de dosis en las últimas 24 h (medicamento: 3, desparasitante: 2).
     *     Al llegar al tope se avisa y se sugiere el otro tratamiento.
     * Si algo falla NO se descuenta stock ni se registra evento.
     */
    private void validarTopeDeDosis(ProductoInventario producto, Perro perro) {
        TipoProducto tipo = producto.getTipo();
        if (tipo != TipoProducto.MEDICAMENTO && tipo != TipoProducto.DESPARASITANTE) return;

        String nombre = perro.getNombre();
        boolean esMedicamento = tipo == TipoProducto.MEDICAMENTO;

        // 1) Salud en su techo por falta de vacuna / esterilización
        List<String> faltantes = new ArrayList<>();
        if (Boolean.FALSE.equals(perro.getVacunado()))     faltantes.add("la vacuna 💉");
        if (Boolean.FALSE.equals(perro.getEsterilizado())) faltantes.add("la esterilización ✂️");

        if (!faltantes.isEmpty() && perro.getSalud() != null
                && perro.getSalud() >= barraService.techoSaludPorChequeos(perro)) {
            throw new IllegalStateException(
                    "🩺 " + nombre + " ya alcanzó el máximo de salud que puede tener por ahora. " +
                    (esMedicamento ? "Más medicamento" : "Más desparasitante") +
                    " no lo mejora: lo que realmente necesita es " +
                    String.join(" y ", faltantes) + ". 🐾");
        }

        // 2) Tope de dosis en las últimas 24 h
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime desde = ahora.minusHours(VENTANA_DOSIS_HORAS);

        List<EventoBarra> dosisMed = eventoRepository
                .findByPerroIdPerroAndTipoEventoAndDescripcionStartingWithAndFechaAfterOrderByFechaAsc(
                        perro.getIdPerro(), TipoEvento.DESPARASITANTE,
                        BarraService.DESC_MEDICAMENTO, desde);
        List<EventoBarra> dosisDesp = eventoRepository
                .findByPerroIdPerroAndTipoEventoAndDescripcionStartingWithAndFechaAfterOrderByFechaAsc(
                        perro.getIdPerro(), TipoEvento.DESPARASITANTE,
                        BarraService.DESC_DESPARASITANTE, desde);

        boolean medLleno  = dosisMed.size()  >= MAX_DOSIS_MEDICAMENTO;
        boolean despLleno = dosisDesp.size() >= MAX_DOSIS_DESPARASITANTE;

        if (esMedicamento && medLleno) {
            String cuando = tiempoHastaLiberar(
                    dosisMed.get(dosisMed.size() - MAX_DOSIS_MEDICAMENTO).getFecha(), ahora);
            throw new IllegalStateException(
                    "💊 ¡Suficiente medicamento por hoy! " + nombre + " ya recibió " +
                    MAX_DOSIS_MEDICAMENTO + " dosis en las últimas " + VENTANA_DOSIS_HORAS +
                    " horas y más sería una sobredosis. " +
                    (despLleno
                        ? "Tampoco puede recibir más desparasitante, así que lo mejor es dejarlo descansar 😴. Podrás darle otra dosis " + cuando + "."
                        : "Lo que necesita ahora es desparasitante 🪱."));
        }

        if (!esMedicamento && despLleno) {
            String cuando = tiempoHastaLiberar(
                    dosisDesp.get(dosisDesp.size() - MAX_DOSIS_DESPARASITANTE).getFecha(), ahora);
            throw new IllegalStateException(
                    "🪱 ¡Desparasitante de sobra! " + nombre + " ya recibió " +
                    MAX_DOSIS_DESPARASITANTE + " dosis en las últimas " + VENTANA_DOSIS_HORAS +
                    " horas. " +
                    (medLleno
                        ? "Tampoco puede recibir más medicamento, así que toca dejarlo descansar 😴. Podrás darle otra dosis " + cuando + "."
                        : "Ahora lo que necesita es medicamento 💊."));
        }
    }

    /** Texto tipo: en 3 h 20 min, con lo que falta para que la dosis más antigua salga de la ventana. */
    private String tiempoHastaLiberar(LocalDateTime fechaDosis, LocalDateTime ahora) {
        Duration falta = Duration.between(ahora, fechaDosis.plusHours(VENTANA_DOSIS_HORAS));
        long minutos = Math.max(1, falta.toMinutes());
        long h = minutos / 60;
        long m = minutos % 60;
        if (h == 0) return "en " + m + " min";
        return "en " + h + " h" + (m > 0 ? " " + m + " min" : "");
    }

    private void aplicarEfecto(ProductoInventario producto, Integer idPerro) {
        switch (producto.getTipo()) {
            case VACUNA -> barraService.registrarVacuna(idPerro, producto.getNombre());
            case DESPARASITANTE -> barraService.registrarDesparasitante(idPerro);
            case MEDICAMENTO -> barraService.registrarMedicamento(idPerro, producto.getNombre());
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