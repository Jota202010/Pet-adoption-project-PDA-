package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.config.BarrasConfig;
import com.refugio.nueva_vida.proyecto_de_aula.exception.BarraNoEncontradaException;
import com.refugio.nueva_vida.proyecto_de_aula.model.*;

import com.refugio.nueva_vida.proyecto_de_aula.repository.EventoBarraRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BarraService {

    private final PerroRepository perroRepository;
    private final EventoBarraRepository eventoRepository;
    private final RecetaService recetaService;
    private final BarrasConfig config;

    public NivelBarra calcularNivel(int valor) {
        if (valor <= 25) return NivelBarra.CRITICO;
        if (valor <= 50) return NivelBarra.BAJO;
        if (valor <= 75) return NivelBarra.MEDIO;
        return NivelBarra.ALTO;
    }

    @Transactional
    public void registrarComida(Integer idPerro, String tipo) {
        Perro perro = obtenerPerro(idPerro);
        int aumento = "premium".equalsIgnoreCase(tipo) ? 40 : 25;
        int anterior = perro.getNutricion();
        int nuevo = limitar(anterior + aumento);

        perro.setNutricion(nuevo);
        perro.setUltimaComida(LocalDateTime.now());
        perroRepository.save(perro);

        registrarEvento(perro, TipoBarra.NUTRICION, TipoEvento.COMIDA,
                nuevo - anterior, nuevo, "Comida " + tipo);

        recetaService.resolverPendientesSiCorresponde(perro);
        recetaService.generarRecetaSiNecesario(perro);
    }

    @Transactional
    public void registrarAgua(Integer idPerro) {
        Perro perro = obtenerPerro(idPerro);
        int anterior = perro.getHidratacion();
        int nuevo = limitar(anterior + 20);

        perro.setHidratacion(nuevo);
        perro.setUltimaHidratacion(LocalDateTime.now());
        perroRepository.save(perro);

        registrarEvento(perro, TipoBarra.HIDRATACION, TipoEvento.AGUA,
                nuevo - anterior, nuevo, "Agua registrada");

        recetaService.resolverPendientesSiCorresponde(perro);
        recetaService.generarRecetaSiNecesario(perro);
    }

    @Transactional
    public void registrarVacuna(Integer idPerro, String tipoVacuna) {
        Perro perro = obtenerPerro(idPerro);
        int anterior = perro.getSalud();
        int nuevo = limitar(anterior + 30);

        perro.setSalud(nuevo);
        perro.setUltimaVacuna(LocalDateTime.now());
        perro.setVacunado(true);
        perroRepository.save(perro);

        registrarEvento(perro, TipoBarra.SALUD, TipoEvento.VACUNA,
                nuevo - anterior, nuevo, "Vacuna: " + tipoVacuna);

        recetaService.resolverPendientesSiCorresponde(perro);
        recetaService.generarRecetaSiNecesario(perro);
    }

    /**
     * Interacción "Jugar" — usa un accesorio (ej. Correa Reforzada).
     * Sube la barra de Energía y guarda cuándo fue la última vez
     * que se jugó con el perro (lo usa el cooldown del frontend).
     */
    @Transactional
    public void registrarJuego(Integer idPerro) {
        Perro perro = obtenerPerro(idPerro);
        int anterior = perro.getEnergia();
        int nuevo = limitar(anterior + 30);

        perro.setEnergia(nuevo);
        perro.setUltimoJuego(LocalDateTime.now());
        perroRepository.save(perro);

        registrarEvento(perro, TipoBarra.ENERGIA, TipoEvento.JUEGO,
                nuevo - anterior, nuevo, "Sesión de juego con la correa");
    }

    /**
     * Interacción "Bañar" — usa 3 unidades de un producto de higiene
     * (ej. Shampoo Antipulgas). Sube la barra de Hidratación (+15).
     */
    @Transactional
    public void registrarBano(Integer idPerro) {
        Perro perro = obtenerPerro(idPerro);
        int anterior = perro.getHidratacion();
        int nuevo = limitar(anterior + 15);

        perro.setHidratacion(nuevo);
        perro.setUltimaHidratacion(LocalDateTime.now());
        perro.setUltimoBano(LocalDateTime.now());
        perroRepository.save(perro);

        registrarEvento(perro, TipoBarra.HIDRATACION, TipoEvento.BANO,
                nuevo - anterior, nuevo, "Baño completo (mojar, espumar, aclarar)");

        recetaService.resolverPendientesSiCorresponde(perro);
        recetaService.generarRecetaSiNecesario(perro);
    }

    @Transactional
    public void registrarDesparasitante(Integer idPerro) {
        Perro perro = obtenerPerro(idPerro);
        int anterior = perro.getSalud();
        int nuevo = limitar(anterior + 20);

        perro.setSalud(nuevo);
        perro.setUltimaDesparasitacion(LocalDateTime.now());
        perroRepository.save(perro);

        registrarEvento(perro, TipoBarra.SALUD, TipoEvento.DESPARASITANTE,
                nuevo - anterior, nuevo, "Desparasitación registrada");

        recetaService.resolverPendientesSiCorresponde(perro);
        recetaService.generarRecetaSiNecesario(perro);
    }

    /**
     * Interacción "Esterilizar" — usa 1 unidad de un producto de
     * tipo Esterilización desde el inventario. No sube ninguna
     * barra por sí sola (por eso el inventario no le exige margen
     * en ninguna barra antes de darla); simplemente marca al perro
     * como esterilizado, igual que registrarVacuna marca el
     * checkbox "vacunado".
     */
    @Transactional
    public void registrarEsterilizacion(Integer idPerro) {
        Perro perro = obtenerPerro(idPerro);

        perro.setEsterilizado(true);
        perroRepository.save(perro);

        registrarEvento(perro, TipoBarra.SALUD, TipoEvento.ESTERILIZACION,
                0, perro.getSalud(), "Esterilización realizada");

        recetaService.resolverPendientesSiCorresponde(perro);
        recetaService.generarRecetaSiNecesario(perro);
    }

    @Transactional
    public void registrarEnfermedad(Integer idPerro, String descripcion) {
        Perro perro = obtenerPerro(idPerro);
        int anterior = perro.getSalud();
        int nuevo = limitar(anterior - 40);

        perro.setSalud(nuevo);
        perro.setNivelSalud(Perro.NivelSalud.ENFERMO);
        perroRepository.save(perro);

        registrarEvento(perro, TipoBarra.SALUD, TipoEvento.ENFERMEDAD,
                nuevo - anterior, nuevo, descripcion);

        recetaService.generarRecetaSiNecesario(perro);
    }

    /**
     * Aplica el desgaste por tiempo de las 4 barras de un perro.
     * <p>
     * Cada barra usa su PROPIA fecha de referencia (última comida,
     * última hidratación, última vacuna, último juego) para calcular
     * cuántas horas reales han pasado. Tras aplicar el cambio, esa
     * fecha se "adelanta" exactamente por las horas ya consumidas en
     * intervalos completos (dejando el resto pendiente), de forma que
     * el mismo intervalo nunca se vuelva a contar y restar dos veces
     * en la siguiente ejecución del scheduler (bug de doble conteo).
     */
    @Transactional
    public void aplicarDesgastePorTiempo(Perro perro) {
        LocalDateTime ahora = LocalDateTime.now();

        // Modo demo: si está activo, el tiempo real transcurrido se
        // multiplica por este factor antes de compararlo con los
        // umbrales de desgaste (útil para pruebas/demostraciones).
        int factor = config.isAcelerarTiempo()
                ? Math.max(config.getFactorAceleracion(), 1)
                : 1;

        boolean huboCambios = false;

        huboCambios |= procesarDesgasteNutricion(perro, ahora, factor);
        huboCambios |= procesarDesgasteHidratacion(perro, ahora, factor);
        huboCambios |= procesarDesgasteSalud(perro, ahora, factor);
        huboCambios |= procesarDesgasteEnergia(perro, ahora, factor);

        if (huboCambios) {
            perroRepository.save(perro);
            recetaService.generarRecetaSiNecesario(perro);
        }
    }

    private boolean procesarDesgasteNutricion(Perro perro, LocalDateTime ahora, int factor) {
        LocalDateTime referencia = perro.getUltimaComida();
        if (referencia == null) return false;

        long horasReales = ChronoUnit.HOURS.between(referencia, ahora);
        if (horasReales <= 0) return false;

        long periodo = config.getNutricionDesgasteHoras();
        long intervalos = (horasReales * factor) / periodo;
        if (intervalos <= 0) return false;

        int cambio = (int) (intervalos * config.getNutricionDesgastePorcentaje());
        cambiarNutricion(perro, -cambio, "Desgaste por tiempo");

        long horasConsumidas = Math.max((intervalos * periodo) / factor, 1);
        perro.setUltimaComida(referencia.plusHours(horasConsumidas));
        return true;
    }

    private boolean procesarDesgasteHidratacion(Perro perro, LocalDateTime ahora, int factor) {
        LocalDateTime referencia = perro.getUltimaHidratacion();
        if (referencia == null) return false;

        long horasReales = ChronoUnit.HOURS.between(referencia, ahora);
        if (horasReales <= 0) return false;

        long periodo = config.getHidratacionDesgasteHoras();
        long intervalos = (horasReales * factor) / periodo;
        if (intervalos <= 0) return false;

        int cambio = (int) (intervalos * config.getHidratacionDesgastePorcentaje());
        cambiarHidratacion(perro, -cambio, "Desgaste por tiempo");

        long horasConsumidas = Math.max((intervalos * periodo) / factor, 1);
        perro.setUltimaHidratacion(referencia.plusHours(horasConsumidas));
        return true;
    }

    private boolean procesarDesgasteSalud(Perro perro, LocalDateTime ahora, int factor) {
        // Nota: reutiliza "última vacuna" como referencia de envejecimiento
        // general, igual que en el diseño original — no afecta las
        // comprobaciones de "¿está vacunado?" en otros lugares, que solo
        // revisan si el campo es null, no cuánto tiempo ha pasado.
        LocalDateTime referencia = perro.getUltimaVacuna();
        if (referencia == null) return false;

        long horasReales = ChronoUnit.HOURS.between(referencia, ahora);
        if (horasReales <= 0) return false;

        long periodo = config.getSaludDesgasteDias() * 24L;
        long intervalos = (horasReales * factor) / periodo;
        if (intervalos <= 0) return false;

        int cambio = (int) (intervalos * config.getSaludDesgastePorcentaje());
        cambiarSalud(perro, -cambio, "Envejecimiento natural");

        long horasConsumidas = Math.max((intervalos * periodo) / factor, 1);
        perro.setUltimaVacuna(referencia.plusHours(horasConsumidas));
        return true;
    }

    private boolean procesarDesgasteEnergia(Perro perro, LocalDateTime ahora, int factor) {
        LocalDateTime referencia = perro.getUltimoJuego();
        if (referencia == null) return false;

        long horasReales = ChronoUnit.HOURS.between(referencia, ahora);
        if (horasReales <= 0) return false;

        long periodo = config.getEnergiaDesgasteHoras();
        long intervalos = (horasReales * factor) / periodo;
        if (intervalos <= 0) return false;

        int cambio = (int) (intervalos * config.getEnergiaDesgastePorcentaje());
        cambiarEnergia(perro, -cambio, "Desgaste por tiempo (falta de juego)");

        long horasConsumidas = Math.max((intervalos * periodo) / factor, 1);
        perro.setUltimoJuego(referencia.plusHours(horasConsumidas));
        return true;
    }

    private void cambiarNutricion(Perro perro, int cambio, String descripcion) {
        int anterior = perro.getNutricion();
        int nuevo = limitar(anterior + cambio);
        perro.setNutricion(nuevo);
        registrarEvento(perro, TipoBarra.NUTRICION, TipoEvento.DESGASTE_TIEMPO,
                cambio, nuevo, descripcion);
    }

    private void cambiarHidratacion(Perro perro, int cambio, String descripcion) {
        int anterior = perro.getHidratacion();
        int nuevo = limitar(anterior + cambio);
        perro.setHidratacion(nuevo);
        registrarEvento(perro, TipoBarra.HIDRATACION, TipoEvento.DESGASTE_TIEMPO,
                cambio, nuevo, descripcion);
    }

    private void cambiarSalud(Perro perro, int cambio, String descripcion) {
        int anterior = perro.getSalud();
        int nuevo = limitar(anterior + cambio);
        perro.setSalud(nuevo);
        registrarEvento(perro, TipoBarra.SALUD, TipoEvento.DESGASTE_TIEMPO,
                cambio, nuevo, descripcion);
    }

    private void cambiarEnergia(Perro perro, int cambio, String descripcion) {
        int anterior = perro.getEnergia();
        int nuevo = limitar(anterior + cambio);
        perro.setEnergia(nuevo);
        registrarEvento(perro, TipoBarra.ENERGIA, TipoEvento.DESGASTE_TIEMPO,
                cambio, nuevo, descripcion);
    }

    @Transactional
    public void resetearBarras(Integer idPerro) {
        Perro perro = obtenerPerro(idPerro);

        perro.setNutricion(100);
        perro.setHidratacion(100);
        perro.setSalud(100);
        perro.setEnergia(100);
        perro.setVacunado(true);
        perro.setNivelSalud(Perro.NivelSalud.SANO);
        perro.setUltimaComida(LocalDateTime.now());
        perro.setUltimaHidratacion(LocalDateTime.now());
        perro.setUltimaVacuna(LocalDateTime.now());
        perro.setUltimaDesparasitacion(LocalDateTime.now());
        perro.setUltimoJuego(LocalDateTime.now());

        perroRepository.save(perro);

        registrarEvento(perro, TipoBarra.NUTRICION, TipoEvento.RESET, 100, 100, "Reset de barras");
        registrarEvento(perro, TipoBarra.HIDRATACION, TipoEvento.RESET, 100, 100, "Reset de barras");
        registrarEvento(perro, TipoBarra.SALUD, TipoEvento.RESET, 100, 100, "Reset de barras");
        registrarEvento(perro, TipoBarra.ENERGIA, TipoEvento.RESET, 100, 100, "Reset de barras");

        // Nutrición e hidratación quedaron en 100%: las recetas pendientes
        // de esos tipos (y sus notificaciones) ya no se necesitan.
        // Las recetas de salud no se tocan.
        recetaService.resolverPendientesPorReset(perro);
    }

    public List<EventoBarra> eventosDePerro(Perro perro) {
        if (perro == null || perro.getIdPerro() == null) {
            return List.of();
        }
        return eventoRepository.findByPerroIdPerroOrderByFechaDesc(perro.getIdPerro());
    }

    @Transactional
    public void eliminarEventos(Integer idPerro) {
        Perro perro = obtenerPerro(idPerro);
        List<EventoBarra> eventos = eventoRepository
                .findByPerroIdPerroOrderByFechaDesc(perro.getIdPerro());

        if (!eventos.isEmpty()) {
            eventoRepository.deleteAll(eventos);
        }
    }

    // SINCRONIZAR SALUD CON ESTADO MÉDICO
    

    @Transactional
    public void sincronizarSaludConEstado(Perro perro) {
        if (perro == null) return;

        int saludMaxima = 100;

        if (perro.getNivelSalud() != null) {
            switch (perro.getNivelSalud()) {
                case ENFERMO:
                    saludMaxima = Math.min(saludMaxima, 60);
                    break;
                case CRITICO:
                    saludMaxima = Math.min(saludMaxima, 30);
                    break;
                case SANO:
                default:
                    break;
            }
        }

        if (Boolean.FALSE.equals(perro.getVacunado())) {
            saludMaxima = Math.min(saludMaxima, 80);
        }

        if (Boolean.FALSE.equals(perro.getEsterilizado())) {
            saludMaxima = Math.min(saludMaxima, 90);
        }

        if (perro.getSalud() > saludMaxima) {
            perro.setSalud(saludMaxima);
            perroRepository.save(perro);
        }
    }

    /**
     * Sincronización FUERTE: se llama solo cuando el admin guarda el
     * formulario de edición del perro. A diferencia de
     * sincronizarSaludConEstado (que solo pone un techo hacia abajo),
     * este método FIJA el % de salud según nivel de salud + vacunado +
     * esterilizado, en ambas direcciones (sube y baja), sincroniza la
     * fecha de última vacuna con el checkbox "vacunado", y actualiza
     * las recetas para que dejen de aparecer si ya no aplican.
     */
    @Transactional
    public void sincronizarEstadoMedicoManual(Perro perro) {
        if (perro == null) return;

        int saludObjetivo;
        Perro.NivelSalud nivel = perro.getNivelSalud() != null
                ? perro.getNivelSalud() : Perro.NivelSalud.SANO;

        switch (nivel) {
            case CRITICO -> saludObjetivo = 15;
            case ENFERMO -> saludObjetivo = 45;
            default -> saludObjetivo = 100;
        }

        if (Boolean.FALSE.equals(perro.getVacunado())) {
            saludObjetivo -= 15;
        }
        if (Boolean.FALSE.equals(perro.getEsterilizado())) {
            saludObjetivo -= 10;
        }

        saludObjetivo = limitar(saludObjetivo);
        int anterior = perro.getSalud() != null ? perro.getSalud() : 100;

        perro.setSalud(saludObjetivo);

        // La casilla "vacunado" ahora manda sobre la fecha real de vacuna,
        // para que el sistema de recetas no siga pidiendo una vacuna que
        // el perro ya tiene (o si se desmarca, que vuelva a pedirla).
        if (Boolean.TRUE.equals(perro.getVacunado())) {
            if (perro.getUltimaVacuna() == null) {
                perro.setUltimaVacuna(LocalDateTime.now());
            }
        } else {
            perro.setUltimaVacuna(null);
        }

        perroRepository.save(perro);

        if (saludObjetivo != anterior) {
            registrarEvento(perro, TipoBarra.SALUD, TipoEvento.AJUSTE_MANUAL,
                    saludObjetivo - anterior, saludObjetivo,
                    "Ajuste por edición manual (nivel de salud / vacunación / esterilización)");
        }

        recetaService.resolverPendientesSiCorresponde(perro);
        recetaService.generarRecetaSiNecesario(perro);
    }

    private void registrarEvento(Perro perro, TipoBarra barra, TipoEvento tipoEvento,
                                  int cambio, int resultado, String descripcion) {
        EventoBarra evento = EventoBarra.builder()
                .perro(perro)
                .tipoBarra(barra)
                .tipoEvento(tipoEvento)
                .valorCambio(cambio)
                .valorResultante(resultado)
                .descripcion(descripcion)
                .fecha(LocalDateTime.now())
                .build();

        eventoRepository.save(evento);
    }

    private Perro obtenerPerro(Integer idPerro) {
        return perroRepository.findById(idPerro)
                .orElseThrow(() -> new BarraNoEncontradaException(
                        "No se encontró el perro #" + idPerro));
    }

    private int limitar(int valor) {
        return Math.max(0, Math.min(100, valor));
    }
}
