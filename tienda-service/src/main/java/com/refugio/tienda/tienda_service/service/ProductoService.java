package com.refugio.tienda.tienda_service.service;

import com.refugio.tienda.tienda_service.model.Producto;
import com.refugio.tienda.tienda_service.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
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
