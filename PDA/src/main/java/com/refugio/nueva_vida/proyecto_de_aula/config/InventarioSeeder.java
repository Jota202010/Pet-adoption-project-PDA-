package com.refugio.nueva_vida.proyecto_de_aula.config;

import com.refugio.nueva_vida.proyecto_de_aula.model.TipoProducto;
import com.refugio.nueva_vida.proyecto_de_aula.repository.ProductoInventarioRepository;
import com.refugio.nueva_vida.proyecto_de_aula.service.InventarioService;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga un inventario inicial de ejemplo solo si la tabla está vacía,
 * para que la pantalla no se vea vacía la primera vez que se levanta el proyecto.
 * Puedes borrar esta clase sin problema una vez tengas datos reales.
 */
@Component
@RequiredArgsConstructor
public class InventarioSeeder implements CommandLineRunner {

    private final ProductoInventarioRepository inventarioRepository;
    private final InventarioService inventarioService;

    @Override
    public void run(String... args) {
        if (inventarioRepository.count() > 0) {
            return;
        }

        inventarioService.agregarStock("Vacuna Antirrábica", TipoProducto.VACUNA, 5, "💉");
        inventarioService.agregarStock("Concentrado Premium", TipoProducto.ALIMENTO, 8, "🥩");
        inventarioService.agregarStock("Suero Oral Canino", TipoProducto.HIDRATACION, 3, "💧");
        inventarioService.agregarStock("Desparasitante Canino", TipoProducto.DESPARASITANTE, 2, "🪱");
        inventarioService.agregarStock("Antiinflamatorio Canino", TipoProducto.MEDICAMENTO, 4, "💊");
        inventarioService.agregarStock("Correa Reforzada", TipoProducto.ACCESORIO, 6, "🎒");
        inventarioService.agregarStock("Shampoo Antipulgas", TipoProducto.HIGIENE, 5, "🧴");
    }
}