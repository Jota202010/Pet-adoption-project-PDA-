package com.refugio.tienda.tienda_service.util;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Genera y valida números de comprobante para los ingresos
 * que la Tienda registra hacia el Refugio.
 *
 * Formato: TRX-XXXX-XXXX-CC
 *   - "TRX"      : prefijo fijo.
 *   - XXXX-XXXX  : 8 caracteres tomados de un alfabeto sin
 *                  caracteres ambiguos (sin 0, O, 1, I).
 *   - CC         : 2 caracteres de verificación (checksum),
 *                  calculados a partir del cuerpo del código.
 *
 * Esto permite que el sistema sepa si un comprobante fue
 * "inventado" a mano (checksum no coincide) o si es uno
 * realmente generado/válido, sin necesidad de guardar una
 * lista de códigos usados.
 */
public final class ComprobanteUtil {

    private static final String ALFABETO =
            "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final String PREFIJO = "TRX";

    private static final int LONGITUD_CUERPO = 8;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Pattern PATRON = Pattern.compile(
            "^" + PREFIJO +
            "-([" + ALFABETO + "]{4})" +
            "-([" + ALFABETO + "]{4})" +
            "-([" + ALFABETO + "]{2})$"
    );

    private ComprobanteUtil() {
    }

    /**
     * Genera un comprobante nuevo, siempre válido.
     */
    public static String generar() {

        StringBuilder cuerpo = new StringBuilder();

        for (int i = 0; i < LONGITUD_CUERPO; i++) {
            cuerpo.append(
                    ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length()))
            );
        }

        String checksum = calcularChecksum(cuerpo.toString());

        return PREFIJO + "-" +
                cuerpo.substring(0, 4) + "-" +
                cuerpo.substring(4, 8) + "-" +
                checksum;
    }

    /**
     * Verifica formato + checksum de un comprobante ingresado
     * manualmente o pegado por el usuario.
     */
    public static boolean esValido(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            return false;
        }

        Matcher matcher = PATRON.matcher(normalizar(codigo));

        if (!matcher.matches()) {
            return false;
        }

        String cuerpo = matcher.group(1) + matcher.group(2);
        String checksumRecibido = matcher.group(3);

        return calcularChecksum(cuerpo).equals(checksumRecibido);
    }

    /**
     * Normaliza el texto ingresado (sin espacios y en mayúsculas)
     * para guardar siempre el mismo formato en la base de datos.
     */
    public static String normalizar(String codigo) {
        return codigo == null ? null : codigo.trim().toUpperCase();
    }

    private static String calcularChecksum(String cuerpo) {

        int suma = 0;

        for (int i = 0; i < cuerpo.length(); i++) {
            int valor = ALFABETO.indexOf(cuerpo.charAt(i));
            suma += valor * (i + 3);
        }

        int base = ALFABETO.length();

        char c1 = ALFABETO.charAt(suma % base);
        char c2 = ALFABETO.charAt((suma / base) % base);

        return "" + c1 + c2;
    }
}
