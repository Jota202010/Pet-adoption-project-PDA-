/**
 * Validación visual en vivo del número de comprobante.
 *
 * Esto es solo un ayudante visual (pinta el campo de verde o
 * rojo mientras el usuario escribe). No genera comprobantes,
 * no llama a ningún servidor ni usa JSON: el botón "Generar"
 * funciona con un submit normal de formulario hacia el
 * Controller, que responde con HTML.
 *
 * La validación real -la que decide si el ingreso se puede
 * confirmar- ocurre siempre en el backend (ComprobanteUtil /
 * CuentaService), usando el mismo algoritmo que ves aquí abajo.
 */
(function () {
    "use strict";

    var ALFABETO = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    var PATRON = /^TRX-([2-9A-HJ-NP-Z]{4})-([2-9A-HJ-NP-Z]{4})-([2-9A-HJ-NP-Z]{2})$/;

    function calcularChecksum(cuerpo) {
        var suma = 0;

        for (var i = 0; i < cuerpo.length; i++) {
            suma += ALFABETO.indexOf(cuerpo.charAt(i)) * (i + 3);
        }

        var base = ALFABETO.length;
        var c1 = ALFABETO.charAt(suma % base);
        var c2 = ALFABETO.charAt(Math.floor(suma / base) % base);

        return c1 + c2;
    }

    function esValido(codigo) {
        if (!codigo) {
            return false;
        }

        var normalizado = codigo.trim().toUpperCase();
        var match = normalizado.match(PATRON);

        if (!match) {
            return false;
        }

        return calcularChecksum(match[1] + match[2]) === match[3];
    }

    document.addEventListener("DOMContentLoaded", function () {

        var input = document.getElementById("referenciaComprobante");
        var badge = document.getElementById("comprobanteBadge");

        if (!input || !badge) {
            return;
        }

        function actualizarEstado() {
            var valor = input.value.trim();

            input.classList.remove("input-valido", "input-invalido");

            if (!valor) {
                badge.className = "comprobante-badge";
                badge.textContent = "";
                return;
            }

            if (esValido(valor)) {
                input.value = valor.toUpperCase();
                input.classList.add("input-valido");
                badge.className = "comprobante-badge badge-valido";
                badge.textContent = "✓ Comprobante válido";
            } else {
                input.classList.add("input-invalido");
                badge.className = "comprobante-badge badge-invalido";
                badge.textContent = "✗ El comprobante no es válido";
            }
        }

        input.addEventListener("input", actualizarEstado);
        input.addEventListener("blur", actualizarEstado);

        // Si la página se recarga con un comprobante ya
        // generado por el Controller, se pinta como válido
        // de inmediato, sin esperar a que el usuario escriba.
        actualizarEstado();
    });
})();
