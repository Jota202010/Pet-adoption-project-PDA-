package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.messaging.RecetaPublisher;
import com.refugio.nueva_vida.proyecto_de_aula.model.*;
import com.refugio.nueva_vida.proyecto_de_aula.repository.NotificacionRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.RecetaRepository;
import com.refugio.nueva_vida.proyecto_de_aula.config.BarrasConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecetaService {

    private final RecetaRepository recetaRepository;
    private final NotificacionRepository notificacionRepository;
    private final PerroRepository perroRepository;
    private final RecetaPublisher recetaPublisher;
    private final BarrasConfig barrasConfig;


    @Transactional
    public void eliminarPorPerro(Integer idPerro) {

        List<Receta> recetas =
                recetaRepository.findByPerroIdPerro(idPerro);

        if (recetas.isEmpty()) {
            return;
        }

        List<Notificacion> notificaciones =
                notificacionRepository.findByRecetaIn(recetas);

        if (!notificaciones.isEmpty()) {
            notificacionRepository.deleteAll(notificaciones);
        }

        recetaRepository.deleteAll(recetas);
    }


    @Transactional
    public List<Receta> generarRecetaSiNecesario(
            Perro perro) {

        List<Receta> generadas =
                new ArrayList<>();

        if (perro.getNutricion()
                < barrasConfig.getUmbralNutricion()) {

            if (!existePendiente(
                    perro,
                    TipoNecesidad.NUTRICION)) {

                generadas.add(
                        crearRecetaNutricion(perro)
                );
            }
        }

        if (perro.getHidratacion()
                < barrasConfig.getUmbralHidratacion()) {

            if (!existePendiente(
                    perro,
                    TipoNecesidad.HIDRATACION)) {

                generadas.add(
                        crearRecetaHidratacion(perro)
                );
            }
        }

        // VACUNACIÓN: se dispara directamente en cuanto el perro no está
        // vacunado (o no tiene fecha de última vacuna), sin depender del
        // umbral general de salud — igual que nutrición e hidratación.
        if (Boolean.FALSE.equals(perro.getVacunado())
                || perro.getUltimaVacuna() == null) {

            if (!existePendiente(
                    perro,
                    TipoNecesidad.VACUNACION)) {

                generadas.add(
                        crearRecetaSalud(
                                perro,
                                TipoNecesidad.VACUNACION
                        )
                );
            }
        }

        // ESTERILIZACIÓN: mismo criterio directo que vacunación.
        if (Boolean.FALSE.equals(perro.getEsterilizado())) {

            if (!existePendiente(
                    perro,
                    TipoNecesidad.ESTERILIZACION)) {

                generadas.add(
                        crearRecetaSalud(
                                perro,
                                TipoNecesidad.ESTERILIZACION
                        )
                );
            }
        }

        // DESPARASITACIÓN: se mantiene como respaldo general de salud,
        // solo cuando vacunación y esterilización ya están al día pero
        // el puntaje de salud sigue bajo (p. ej. por enfermedad).
        if (perro.getSalud()
                < barrasConfig.getUmbralSalud()
                && Boolean.TRUE.equals(perro.getVacunado())
                && Boolean.TRUE.equals(perro.getEsterilizado())) {

            if (!existePendiente(
                    perro,
                    TipoNecesidad.DESPARASITACION)) {

                generadas.add(
                        crearRecetaSalud(
                                perro,
                                TipoNecesidad.DESPARASITACION
                        )
                );
            }
        }

        return generadas;
    }


    private Receta crearRecetaNutricion(
            Perro perro) {

        PrioridadReceta prioridad =
                calcularPrioridad(
                        perro.getNutricion()
                );

        Receta receta =
                Receta.builder()

                        .perro(perro)

                        .tipoNecesidad(
                                TipoNecesidad.NUTRICION
                        )

                        .descripcion(
                                "La nutrición del perro está " +
                                "por debajo del nivel recomendado."
                        )

                        .productosSugeridos(
                                "[{\"producto\":\"Concentrado Premium\"," +
                                "\"cantidad\":1," +
                                "\"categoria\":\"ALIMENTO\"," +
                                "\"motivo\":\"Mejorar nutrición\"}]"
                        )

                        .prioridad(prioridad)

                        .estado(
                                EstadoReceta.PENDIENTE
                        )

                        .fechaGeneracion(
                                LocalDateTime.now()
                        )

                        .build();

        return guardarYNotificar(receta);
    }


    private Receta crearRecetaHidratacion(
            Perro perro) {

        PrioridadReceta prioridad =
                calcularPrioridad(
                        perro.getHidratacion()
                );

        Receta receta =
                Receta.builder()

                        .perro(perro)

                        .tipoNecesidad(
                                TipoNecesidad.HIDRATACION
                        )

                        .descripcion(
                                "La hidratación del perro " +
                                "está por debajo del nivel recomendado."
                        )

                        .productosSugeridos(
                                "[{\"producto\":\"Suero Oral\"," +
                                "\"cantidad\":1," +
                                "\"categoria\":\"HIDRATACION\"," +
                                "\"motivo\":\"Mejorar hidratación\"}]"
                        )

                        .prioridad(prioridad)

                        .estado(
                                EstadoReceta.PENDIENTE
                        )

                        .fechaGeneracion(
                                LocalDateTime.now()
                        )

                        .build();

        return guardarYNotificar(receta);
    }


    private Receta crearRecetaSalud(
            Perro perro,
            TipoNecesidad necesidad) {

        PrioridadReceta prioridad =
                calcularPrioridad(
                        perro.getSalud()
                );

        String productos;

        if (necesidad ==
                TipoNecesidad.VACUNACION) {

            productos =
                    "[{\"producto\":\"Vacuna Antirrábica\"," +
                    "\"cantidad\":1," +
                    "\"categoria\":\"VACUNA\"," +
                    "\"motivo\":\"Refuerzo de vacunación\"}]";

        } else if (necesidad ==
                TipoNecesidad.ESTERILIZACION) {

            productos =
                    "[{\"producto\":\"Cirugía de esterilización\"," +
                    "\"cantidad\":1," +
                    "\"categoria\":\"ESTERILIZACION\"," +
                    "\"motivo\":\"Esterilización pendiente\"}]";

        } else {

            productos =
                    "[{\"producto\":\"Desparasitante\"," +
                    "\"cantidad\":1," +
                    "\"categoria\":\"DESPARASITANTE\"," +
                    "\"motivo\":\"Control antiparasitario\"}]";
        }

        Receta receta =
                Receta.builder()

                        .perro(perro)

                        .tipoNecesidad(necesidad)

                        .descripcion(
                                "La salud del perro " +
                                "está por debajo del nivel recomendado."
                        )

                        .productosSugeridos(productos)

                        .prioridad(prioridad)

                        .estado(
                                EstadoReceta.PENDIENTE
                        )

                        .fechaGeneracion(
                                LocalDateTime.now()
                        )

                        .build();

        return guardarYNotificar(receta);
    }


    private Receta guardarYNotificar(
            Receta receta) {

        Receta guardada =
                recetaRepository.save(receta);

        Notificacion notificacion =
                Notificacion.builder()

                        .receta(guardada)

                        .titulo(
                                "Nueva receta para "
                                + guardada.getPerro().getNombre()
                        )

                        .mensaje(
                                guardada.getDescripcion()
                        )

                        .leida(false)

                        .fecha(
                                LocalDateTime.now()
                        )

                        .build();

        notificacionRepository.save(
                notificacion
        );

        try {

            recetaPublisher.publicar(
                    guardada
            );

        } catch (Exception e) {

            log.error(
                    "No se pudo publicar receta #{}",
                    guardada.getIdReceta(),
                    e
            );
        }

        return guardada;
    }


    // =========================================================
    // ACTUALIZAR PRODUCTOS DE UNA RECETA
    // =========================================================

    @Transactional
    public void actualizarProductosSugeridos(
            Long idNotificacion,
            String productosJson) {

        Notificacion notificacion =
                notificacionRepository
                        .findById(idNotificacion)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notificación no encontrada."
                                )
                        );

        if (notificacion.getReceta() == null) {

            throw new IllegalStateException(
                    "La notificación no tiene una receta."
            );
        }

        if (productosJson == null
                || productosJson.isBlank()
                || "[]".equals(productosJson)) {

            throw new IllegalArgumentException(
                    "Debes seleccionar al menos un producto."
            );
        }

        Receta receta =
                notificacion.getReceta();

        receta.setProductosSugeridos(
                productosJson
        );

        recetaRepository.save(receta);
    }


    private boolean existePendiente(
            Perro perro,
            TipoNecesidad tipo) {

        return recetaRepository
                .existsByPerroIdPerroAndTipoNecesidadAndEstado(
                        perro.getIdPerro(),
                        tipo,
                        EstadoReceta.PENDIENTE
                );
    }


    private PrioridadReceta calcularPrioridad(
            int valor) {

        if (valor < 25) {
            return PrioridadReceta.ALTA;
        }

        if (valor < 50) {
            return PrioridadReceta.MEDIA;
        }

        return PrioridadReceta.BAJA;
    }


    @Transactional
    public void resolverPendientesSiCorresponde(
            Perro perro) {

        List<Receta> pendientes =
                recetaRepository
                        .findByPerroIdPerro(
                                perro.getIdPerro()
                        );

        for (Receta receta : pendientes) {

            if (receta.getEstado()
                    != EstadoReceta.PENDIENTE) {

                continue;
            }

            boolean resuelta =
                    switch (receta.getTipoNecesidad()) {

                        case VACUNACION ->
                                Boolean.TRUE.equals(
                                        perro.getVacunado()
                                )
                                && perro.getUltimaVacuna() != null;

                        case ESTERILIZACION ->
                                Boolean.TRUE.equals(
                                        perro.getEsterilizado()
                                );

                        case NUTRICION ->
                                perro.getNutricion()
                                >= barrasConfig
                                    .getUmbralNutricion();

                        case HIDRATACION ->
                                perro.getHidratacion()
                                >= barrasConfig
                                    .getUmbralHidratacion();

                        case DESPARASITACION ->
                                perro.getSalud()
                                >= barrasConfig
                                    .getUmbralSalud();

                        default -> false;
                    };

            if (resuelta) {

                receta.setEstado(
                        EstadoReceta.DESCARTADA
                );

                recetaRepository.save(receta);

                notificacionRepository
                        .findByRecetaIn(
                                List.of(receta)
                        )
                        .forEach(n -> {

                            n.setLeida(true);

                            notificacionRepository.save(n);
                        });
            }
        }
    }


    /**
     * Se usa al RESETEAR las barras de un perro.
     * <p>
     * Como nutrición e hidratación vuelven a 100%, las recetas
     * PENDIENTES de esos dos tipos ya no hacen falta: se marcan como
     * DESCARTADAS y su notificación se marca como leída, con lo cual
     * desaparece de la lista de notificaciones y del contador.
     * <p>
     * La barra de energía no genera recetas, y las recetas de salud
     * (vacunación, esterilización, desparasitación) NO se tocan aquí.
     */
    @Transactional
    public void resolverPendientesPorReset(
            Perro perro) {

        List<Receta> recetas =
                recetaRepository
                        .findByPerroIdPerro(
                                perro.getIdPerro()
                        );

        for (Receta receta : recetas) {

            if (receta.getEstado()
                    != EstadoReceta.PENDIENTE) {

                continue;
            }

            TipoNecesidad tipo =
                    receta.getTipoNecesidad();

            if (tipo != TipoNecesidad.NUTRICION
                    && tipo != TipoNecesidad.HIDRATACION) {

                continue;
            }

            receta.setEstado(
                    EstadoReceta.DESCARTADA
            );

            recetaRepository.save(receta);

            notificacionRepository
                    .findByRecetaIn(
                            List.of(receta)
                    )
                    .forEach(n -> {

                        n.setLeida(true);

                        notificacionRepository.save(n);
                    });
        }
    }


    @Transactional
    public void generarRecetasParaTodos() {

        List<Perro> perros =
                perroRepository.findAll();

        for (Perro perro : perros) {

            generarRecetaSiNecesario(
                    perro
            );
        }
    }


    @Transactional
    public void marcarComoLeida(
            Long idNotificacion) {

        Notificacion notificacion =
                notificacionRepository
                        .findById(idNotificacion)
                        .orElseThrow();

        notificacion.setLeida(true);

        notificacion.getReceta()
                .setEstado(
                        EstadoReceta.LEIDA
                );

        notificacion.getReceta()
                .setFechaLectura(
                        LocalDateTime.now()
                );

        notificacionRepository.save(
                notificacion
        );
    }


    @Transactional
    public void descartarReceta(
            Long idNotificacion) {

        Notificacion notificacion =
                notificacionRepository
                        .findById(idNotificacion)
                        .orElseThrow();

        if (notificacion.getReceta() != null) {

            notificacion
                    .getReceta()
                    .setEstado(
                            EstadoReceta.DESCARTADA
                    );

            recetaRepository.save(
                    notificacion.getReceta()
            );
        }

        notificacionRepository.delete(
                notificacion
        );
    }


    public List<Notificacion>
    listarNotificacionesNoLeidas() {

        return notificacionRepository
                .findByLeidaFalseOrderByFechaDesc();
    }


    public long contarNoLeidas() {

        return notificacionRepository
                .countByLeidaFalse();
    }
}
