package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.service.InventarioService;
import com.refugio.nueva_vida.proyecto_de_aula.service.PerroService;
import com.refugio.nueva_vida.proyecto_de_aula.service.RecetaService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final PerroService perroService;
    private final RecetaService recetaService;

    /** Pantalla general del inventario del refugio (para cualquier perro). */
    @GetMapping("/admin/inventario")
    public String verInventario(Model model) {
        model.addAttribute("productos", inventarioService.listarInventario());
        model.addAttribute("perros", perroService.listarTodos());
        model.addAttribute("totalNoLeidas", recetaService.contarNoLeidas());
        return "privilegiado/inventario-adminview";
    }

    /** Dar un producto del inventario a un perro específico, desde la pantalla general. */
    @PostMapping("/admin/inventario/dar")
    public String darDesdeInventarioGeneral(@RequestParam Integer idProducto,
                                             @RequestParam Integer idPerro,
                                             RedirectAttributes ra) {
        try {
            inventarioService.darAlPerro(idProducto, idPerro);
            ra.addFlashAttribute("mensajeExito", "Producto entregado al perro correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/inventario";
    }

    /** Dar un producto del inventario directamente desde la ficha de un perro. */
    @PostMapping("/perros/{id}/inventario/dar/{idProducto}")
    public String darDesdeFichaPerro(@PathVariable Integer id,
                                      @PathVariable Integer idProducto,
                                      RedirectAttributes ra) {
        try {
            inventarioService.darAlPerro(idProducto, id);
            ra.addFlashAttribute("mensajeExito", "Producto entregado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/mascota/" + id;
    }

    /** Elimina un producto del inventario del refugio (definitivo). */
    @PostMapping("/admin/inventario/eliminar/{id}")
    public String eliminarProducto(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            inventarioService.eliminarProducto(id);
            ra.addFlashAttribute("mensajeExito", "Producto eliminado del inventario correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/inventario";
    }
}