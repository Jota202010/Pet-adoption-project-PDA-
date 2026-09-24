package com.refugio.nueva_vida.proyecto_de_aula.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.NoSuchElementException;

/**
 * Manejador global de excepciones no controladas.
 * Reemplaza la página de error Whitelabel con mensajes amigables.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** Recurso no encontrado: ID que no existe en la BD (orElseThrow) */
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex, Model model) {
        model.addAttribute("titulo",  "Recurso no encontrado");
        model.addAttribute("mensaje", "El elemento que buscas no existe o fue eliminado.");
        model.addAttribute("detalle", ex.getMessage());
        return "error/error-pagina";
    }

    /** Ruta inexistente */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoRoute(Exception ex, Model model) {
        model.addAttribute("titulo",  "Página no encontrada");
        model.addAttribute("mensaje", "La página que buscas no existe.");
        model.addAttribute("detalle", null);
        return "error/error-pagina";
    }

    /** Acceso denegado (usuario sin rol suficiente) */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(Model model) {
        model.addAttribute("titulo",  "Acceso denegado");
        model.addAttribute("mensaje", "No tienes permiso para ver esta página.");
        model.addAttribute("detalle", null);
        return "error/error-pagina";
    }

    /** Estado inválido en el negocio (orElseThrow con mensaje propio) */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalState(IllegalStateException ex, Model model) {
        model.addAttribute("titulo",  "Operación no permitida");
        model.addAttribute("mensaje", ex.getMessage());
        model.addAttribute("detalle", null);
        return "error/error-pagina";
    }

    /** Cualquier excepción inesperada */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model) {
        model.addAttribute("titulo",  "Error inesperado");
        model.addAttribute("mensaje",
            "Ocurrió un error en el servidor. Por favor intenta de nuevo o contacta al administrador.");
        model.addAttribute("detalle", ex.getMessage());
        return "error/error-pagina";
    }
}
