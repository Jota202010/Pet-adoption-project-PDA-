package com.refugio.tienda.tienda_service.service;

import com.refugio.tienda.tienda_service.model.Producto;
import com.refugio.tienda.tienda_service.model.StockGratisUsado;
import com.refugio.tienda.tienda_service.repository.ProductoRepository;
import com.refugio.tienda.tienda_service.repository.StockGratisUsadoRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductoService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final ProductoRepository productoRepository;
    private final StockGratisUsadoRepository stockGratisRepository;
    private final CuentaService cuentaService;

    /**
     * Costo (% del valor de las unidades) de REPONER stock de un producto
     * que ya existe. Configurable en application.properties
     * (tienda.stock.costo-porcentaje) o con la variable de entorno
     * TIENDA_STOCK_COSTO_PORCENTAJE. Con 0 la reposición es gratis.
     *
     * El stock inicial GRATIS se da una sola vez por categoría: al primer
     * producto NUEVO con stock que se crea en cada categoría. Cada categoría
     * es independiente y el gratis se recuerda en la tabla stock_gratis_usado,
     * así que no depende de los productos que ya existían.
     */
    @Value("${tienda.stock.costo-porcentaje:5.0}")
    private BigDecimal porcentajeCostoStock;

    @PostConstruct
    void validarConfiguracionCostoStock() {
        if (porcentajeCostoStock == null
                || porcentajeCostoStock.signum() < 0
                || porcentajeCostoStock.compareTo(CIEN) > 0) {

            throw new IllegalStateException(
                    "tienda.stock.costo-porcentaje debe estar entre 0 y 100. "
                    + "Valor actual: " + porcentajeCostoStock);
        }
    }

    /**
     * Resultado de guardar un producto: cuánto se cobró (0 si nada),
     * cuántas unidades se agregaron al stock y si fue el stock gratis
     * de la categoría.
     */
    public record ResultadoGuardado(
            Producto producto,
            BigDecimal costoCobrado,
            int unidadesAgregadas,
            boolean stockGratis) {
    }

    public BigDecimal getPorcentajeCostoStock() {
        return porcentajeCostoStock;
    }

    /** Costo de agregar N unidades de un producto a ese precio (en pesos enteros). */
    public BigDecimal calcularCostoStock(int unidades, BigDecimal precio) {
        if (unidades <= 0 || precio == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return precio
                .multiply(BigDecimal.valueOf(unidades))
                .multiply(porcentajeCostoStock)
                .divide(CIEN, 0, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Guardado simple, sin cobrar nada (uso interno). */
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    /** true si esa categoría todavía no ha usado su stock inicial gratis. */
    @Transactional(readOnly = true)
    public boolean categoriaTieneStockGratis(Producto.Categoria categoria) {
        return categoria != null
                && !stockGratisRepository.existsByCategoria(categoria);
    }

    /** Categorías que aún conservan su stock inicial gratis. */
    @Transactional(readOnly = true)
    public List<Producto.Categoria> categoriasConStockGratis() {

        Set<Producto.Categoria> usadas = stockGratisRepository.findAll()
                .stream()
                .map(StockGratisUsado::getCategoria)
                .collect(Collectors.toSet());

        return Arrays.stream(Producto.Categoria.values())
                .filter(c -> !usadas.contains(c))
                .toList();
    }

    /**
     * Guarda el producto aplicando la regla del stock:
     *  - Producto NUEVO con stock, en una categoría que aún no usó su gratis:
     *    el stock inicial es gratis y la categoría queda marcada como usada.
     *  - Producto NUEVO con stock, en una categoría que ya usó su gratis:
     *    se cobra a la Tienda el porcentaje configurado del valor de todas
     *    sus unidades.
     *  - Producto EXISTENTE: si el stock SUBE, se cobra ese porcentaje del
     *    valor de las unidades agregadas. Si baja o no cambia, no se cobra.
     *
     * Todo o nada: si la Tienda no alcanza a pagar, se lanza
     * SaldoInsuficienteException y no se guarda nada.
     */
    public ResultadoGuardado guardarCobrandoStock(Producto producto) {

        BigDecimal costo = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int agregadas = 0;
        boolean gratis = false;

        if (producto.getStock() != null) {

            if (producto.getIdProducto() == null) {

                // Producto nuevo (con stock 0 no se gasta el gratis ni se cobra)
                if (producto.getStock() > 0) {

                    if (categoriaTieneStockGratis(producto.getCategoria())) {

                        gratis = true;

                        StockGratisUsado uso = new StockGratisUsado();
                        uso.setCategoria(producto.getCategoria());
                        uso.setNombreProducto(producto.getNombre());
                        stockGratisRepository.save(uso);

                    } else {

                        agregadas = producto.getStock();
                        costo = calcularCostoStock(agregadas, producto.getPrecio());

                        cuentaService.cobrarCostoStock(
                                costo,
                                "Costo de stock inicial: " + agregadas
                                        + " u. de " + producto.getNombre());
                    }
                }

            } else {

                // Producto existente
                Integer stockAnterior = productoRepository
                        .findById(producto.getIdProducto())
                        .map(Producto::getStock)
                        .orElse(null);

                if (stockAnterior != null && producto.getStock() > stockAnterior) {

                    agregadas = producto.getStock() - stockAnterior;
                    costo = calcularCostoStock(agregadas, producto.getPrecio());

                    cuentaService.cobrarCostoStock(
                            costo,
                            "Costo de reponer stock: +" + agregadas
                                    + " u. de " + producto.getNombre());
                }
            }
        }

        Producto guardado = productoRepository.save(producto);

        return new ResultadoGuardado(guardado, costo, agregadas, gratis);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

    public void eliminar(Integer id) {
        productoRepository.deleteById(id);
    }

    /** Reduce el stock en 1 (usado al confirmar un pedido) */
    public void descontarStock(Integer idProducto, int cantidad) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalStateException("Producto no encontrado: " + idProducto));
        if (producto.getStock() < cantidad) {
            throw new IllegalStateException("Stock insuficiente para " + producto.getNombre());
        }
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
    }

    /** Busca si hay stock de un producto por nombre + categoría (usado para la alerta de vacunas) */
    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorNombreYCategoria(String nombre, Producto.Categoria categoria) {
        return productoRepository.findFirstByNombreContainingIgnoreCaseAndCategoria(nombre, categoria);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorNombreExacto(String nombre) {
        return productoRepository
                .findFirstByNombreIgnoreCase(nombre);
    }
}