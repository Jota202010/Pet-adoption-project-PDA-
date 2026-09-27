package com.refugio.tienda.tienda_service.controller;

import com.refugio.tienda.tienda_service.model.Producto;
import com.refugio.tienda.tienda_service.model.TipoCuenta;
import com.refugio.tienda.tienda_service.service.CuentaService;
import com.refugio.tienda.tienda_service.service.ProductoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Slf4j
@Controller
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
public class ProductoAdminController {

    private final ProductoService productoService;
    private final CuentaService   cuentaService;

    // ═════════════════════════════════════════════════════
    // LISTAR PRODUCTOS (con saldo de la tienda)
    // ═════════════════════════════════════════════════════

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("cuentaTienda",  cuentaService.obtenerCuentaPorTipo(TipoCuenta.TIENDA));
        model.addAttribute("cuentaRefugio", cuentaService.obtenerCuentaPorTipo(TipoCuenta.REFUGIO));
        return "admin/productos";
    }

    // FORMULARIO NUEVO (con pre-relleno opcional)

    @GetMapping("/nuevo")
    public String nuevo(
            @RequestParam(required = false) Boolean rellenar,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            Model model
    ) {
        Producto producto = new Producto();

        if (Boolean.TRUE.equals(rellenar)) {

            if (nombre != null && !nombre.isBlank()) {
                producto.setNombre(nombre);
            }

            if (descripcion != null && !descripcion.isBlank()) {
                String desc = descripcion.toLowerCase();

                if (desc.contains("categoría: esterilizacion") || desc.contains("categoría: esterilización")
                        || desc.contains("esteriliz")) {
                    producto.setCategoria(Producto.Categoria.ESTERILIZACION);
                } else if (desc.contains("categoría: vacuna") || desc.contains("vacun")) {
                    producto.setCategoria(Producto.Categoria.VACUNA);
                } else if (desc.contains("categoría: alimento") || desc.contains("nutri")) {
                    producto.setCategoria(Producto.Categoria.ALIMENTO);
                } else if (desc.contains("categoría: hidratacion") || desc.contains("categoría: hidratación")
                        || desc.contains("hidrat")) {
                    producto.setCategoria(Producto.Categoria.HIDRATACION);
                } else if (desc.contains("categoría: desparasitante") || desc.contains("desparas")) {
                    producto.setCategoria(Producto.Categoria.DESPARASITANTE);
                } else if (desc.contains("categoría: medicamento") || desc.contains("medicamento")) {
                    producto.setCategoria(Producto.Categoria.MEDICAMENTO);
                } else if (desc.contains("categoría: higiene") || desc.contains("higiene")) {
                    producto.setCategoria(Producto.Categoria.HIGIENE);
                } else if (desc.contains("categoría: accesorio") || desc.contains("accesorio")) {
                    producto.setCategoria(Producto.Categoria.ACCESORIO);
                }

                int idx = desc.indexOf("precio sugerido:");
                if (idx >= 0) {
                    try {
                        String resto = descripcion.substring(idx + 17);
                        String precioStr = resto.split("[\n\r]")[0].replaceAll("[^0-9]", "");
                        if (!precioStr.isBlank()) {
                            producto.setPrecio(new BigDecimal(precioStr));
                        }
                    } catch (Exception ignored) {}
                }
            }

            if (producto.getPrecio() == null) {
                producto.setPrecio(new BigDecimal("10000"));
            }

            if (producto.getStock() == null) {
                producto.setStock(10);
            }

            model.addAttribute("mensajeRellenar",
                "🪄 Los campos se rellenaron automáticamente desde la receta. Verifica y ajusta antes de guardar.");
        }

        model.addAttribute("producto",    producto);
        model.addAttribute("categorias",  Producto.Categoria.values());
        model.addAttribute("cuentaTienda", cuentaService.obtenerCuentaPorTipo(TipoCuenta.TIENDA));
        return "admin/producto-form";
    }

    // ═════════════════════════════════════════════════════
    // GUARDAR PRODUCTO (con validación + manejo de errores real)
    // ═════════════════════════════════════════════════════

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Producto producto,
            RedirectAttributes ra
    ) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            ra.addFlashAttribute("mensajeError", "❌ El nombre del producto es obligatorio.");
            return "redirect:/admin/productos/nuevo";
        }

        if (producto.getCategoria() == null) {
            ra.addFlashAttribute("mensajeError", "❌ Debes seleccionar una categoría.");
            return "redirect:/admin/productos/nuevo";
        }

        if (producto.getPrecio() == null || producto.getPrecio().doubleValue() <= 0) {
            ra.addFlashAttribute("mensajeError", "❌ El precio debe ser mayor a cero.");
            return "redirect:/admin/productos/nuevo";
        }

        if (producto.getStock() == null || producto.getStock() < 0) {
            ra.addFlashAttribute("mensajeError", "❌ El stock no puede ser negativo.");
            return "redirect:/admin/productos/nuevo";
        }

        // 🆕 Ya no deja que un error de base de datos tumbe la app con la
        // pantalla en blanco: lo atrapa y te dice exactamente qué pasó.
        try {
            productoService.guardar(producto);
            ra.addFlashAttribute("mensajeExito", "✅ Producto \"" + producto.getNombre() + "\" agregado correctamente.");
            return "redirect:/admin/productos";
        } catch (Exception e) {
            log.error("❌ Error guardando producto '{}' con categoría {}: {}",
                    producto.getNombre(), producto.getCategoria(), e.getMessage(), e);
            ra.addFlashAttribute("mensajeError",
                    "❌ No se pudo guardar el producto. Detalle técnico: " + e.getMessage());
            return "redirect:/admin/productos/nuevo";
        }
    }

    // ═════════════════════════════════════════════════════
    // FORMULARIO EDITAR
    // ═════════════════════════════════════════════════════

    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Integer id,
            Model model,
            RedirectAttributes ra
    ) {
        Producto producto = productoService.buscarPorId(id).orElse(null);

        if (producto == null) {
            ra.addFlashAttribute("mensajeError", "Producto no encontrado.");
            return "redirect:/admin/productos";
        }

        model.addAttribute("producto",    producto);
        model.addAttribute("categorias",  Producto.Categoria.values());
        model.addAttribute("cuentaTienda", cuentaService.obtenerCuentaPorTipo(TipoCuenta.TIENDA));
        return "admin/producto-form";
    }

    // ═════════════════════════════════════════════════════
    // ELIMINAR
    // ═════════════════════════════════════════════════════

    @PostMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Integer id,
            RedirectAttributes ra
    ) {
        productoService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Producto eliminado correctamente.");
        return "redirect:/admin/productos";
    }
}