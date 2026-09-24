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
        List<Receta> recetas = recetaRepository.findByPerroIdPerro(idPerro);
        if (recetas.isEmpty()) return;

        List<Notificacion> notificaciones = notificacionRepository.findByRecetaIn(recetas);
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

                Receta receta =
                        crearRecetaNutricion(perro);

                generadas.add(receta);
            }
        }


        if (perro.getHidratacion()
                < barrasConfig.getUmbralHidratacion()) {

            if (!existePendiente(
                    perro,
                    TipoNecesidad.HIDRATACION)) {

                Receta receta =
                        crearRecetaHidratacion(perro);

                generadas.add(receta);
            }
        }


        if (perro.getSalud()
                < barrasConfig.getUmbralSalud()) {

            TipoNecesidad necesidad =
                    determinarNecesidadSalud(perro);

            if (!existePendiente(
                    perro,
                    necesidad)) {

                Receta receta =
                        crearRecetaSalud(
                                perro,
                                necesidad
                        );

                generadas.add(receta);
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
                                "La nutrición del perro está "
                                + "por debajo del nivel recomendado."
                        )

                        .productosSugeridos(
                                "[{\"producto\":\"Concentrado Premium\","
                                + "\"cantidad\":1,"
                                + "\"motivo\":\"Mejorar nutrición\"}]"
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
                                "La hidratación del perro "
                                + "está por debajo del nivel recomendado."
                        )

                        .productosSugeridos(
                                "[{\"producto\":\"Suero Oral\","
                                + "\"cantidad\":1,"
                                + "\"motivo\":\"Mejorar hidratación\"}]"
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
                    "[{\"producto\":\"Vacuna Antirrábica\","
                    + "\"cantidad\":1,"
                    + "\"motivo\":\"Refuerzo de vacunación\"}]";

        } else if (necesidad ==
                TipoNecesidad.ESTERILIZACION) {

            productos =
                    "[{\"producto\":\"Cirugía de esterilización\","
                    + "\"cantidad\":1,"
                    + "\"motivo\":\"Esterilización pendiente\"}]";

        } else {

            productos =
                    "[{\"producto\":\"Desparasitante\","
                    + "\"cantidad\":1,"
                    + "\"motivo\":\"Control antiparasitario\"}]";
        }


        Receta receta =
                Receta.builder()

                        .perro(perro)

                        .tipoNecesidad(necesidad)

                        .descripcion(
                                "La salud del perro "
                                + "está por debajo del nivel recomendado."
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


    private TipoNecesidad determinarNecesidadSalud(
            Perro perro) {

        if (Boolean.FALSE.equals(perro.getVacunado())
                || perro.getUltimaVacuna() == null) {

            return TipoNecesidad.VACUNACION;
        }

        if (Boolean.FALSE.equals(perro.getEsterilizado())) {

            return TipoNecesidad.ESTERILIZACION;
        }

        return TipoNecesidad.DESPARASITACION;
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


    /**
     * Descarta automáticamente las recetas PENDIENTES que ya no aplican
     * porque el estado real del perro cambió (se marcó vacunado,
     * esterilizado, o la barra correspondiente volvió a estar bien).
     * Así se evita que quede una receta pidiendo algo que el perro
     * ya tiene.
     */
    @Transactional
    public void resolverPendientesSiCorresponde(Perro perro) {

        List<Receta> pendientes =
                recetaRepository.findByPerroIdPerro(perro.getIdPerro());

        for (Receta receta : pendientes) {

            if (receta.getEstado() != EstadoReceta.PENDIENTE) {
                continue;
            }

            boolean resuelta = switch (receta.getTipoNecesidad()) {

                case VACUNACION ->
                        Boolean.TRUE.equals(perro.getVacunado())
                                && perro.getUltimaVacuna() != null;

                case ESTERILIZACION ->
                        Boolean.TRUE.equals(perro.getEsterilizado());

                case NUTRICION ->
                        perro.getNutricion() >= barrasConfig.getUmbralNutricion();

                case HIDRATACION ->
                        perro.getHidratacion() >= barrasConfig.getUmbralHidratacion();

                case DESPARASITACION ->
                        perro.getSalud() >= barrasConfig.getUmbralSalud();

                default -> false;
            };

            if (resuelta) {

                receta.setEstado(EstadoReceta.DESCARTADA);
                recetaRepository.save(receta);

                notificacionRepository
                        .findByRecetaIn(List.of(receta))
                        .forEach(n -> {
                            n.setLeida(true);
                            notificacionRepository.save(n);
                        });
            }
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