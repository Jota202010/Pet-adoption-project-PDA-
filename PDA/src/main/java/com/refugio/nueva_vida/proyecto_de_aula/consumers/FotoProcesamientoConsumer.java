package com.refugio.nueva_vida.proyecto_de_aula.consumers;

import com.refugio.nueva_vida.proyecto_de_aula.config.RabbitMQConfig;
import com.refugio.nueva_vida.proyecto_de_aula.dtos.FotoProcesamientoDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class FotoProcesamientoConsumer {

    @Value("${app.upload.dir:uploads/fotos}")
    private String uploadDir;

    private static final int ANCHO_MINIATURA = 400; // px
    private static final String TEXTO_MARCA_AGUA = "Refugio Nueva Vida";

    @RabbitListener(queues = RabbitMQConfig.QUEUE_FOTOS)
    public void procesarFoto(FotoProcesamientoDTO evento) {
        log.info("========================================");
        log.info("Procesando imagen en segundo plano");
        log.info("ID Foto: {}", evento.getIdFoto());
        log.info("Archivo: {}", evento.getNombreArchivo());

        try {
            Path carpeta = Paths.get(uploadDir).toAbsolutePath().normalize();
            File archivoOriginal = carpeta.resolve(evento.getNombreArchivo()).toFile();

            if (!archivoOriginal.exists()) {
                log.warn("El archivo original no existe, se omite el procesamiento: {}", archivoOriginal);
                return;
            }

            BufferedImage original = ImageIO.read(archivoOriginal);
            if (original == null) {
                log.warn("No se pudo leer la imagen (formato no soportado): {}", archivoOriginal);
                return;
            }

            BufferedImage miniatura = redimensionar(original, ANCHO_MINIATURA);
            aplicarMarcaDeAgua(miniatura, TEXTO_MARCA_AGUA);

            // La miniatura siempre se guarda como .jpg, sin importar el formato original
            // (más compatible y liviano; evita problemas al escribir de vuelta en .webp)
            String nombreSinExtension = evento.getNombreArchivo().substring(0, evento.getNombreArchivo().lastIndexOf('.'));
            String nombreMiniatura = "thumb_" + nombreSinExtension + ".jpg";
            File archivoMiniatura = carpeta.resolve(nombreMiniatura).toFile();

            // Convertir a RGB plano (necesario porque algunos formatos traen canal alfa que JPG no soporta)
            BufferedImage miniaturaRgb = new BufferedImage(
                    miniatura.getWidth(), miniatura.getHeight(), BufferedImage.TYPE_INT_RGB);
            miniaturaRgb.createGraphics().drawImage(miniatura, 0, 0, Color.WHITE, null);

            ImageIO.write(miniaturaRgb, "jpg", archivoMiniatura);

            log.info("Miniatura generada exitosamente: {}", nombreMiniatura);
        } catch (IOException e) {
            log.error("Error al procesar la imagen {}: {}", evento.getNombreArchivo(), e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado procesando la imagen {}: {}", evento.getNombreArchivo(), e.getMessage());
        }

        log.info("========================================");
    }

    private BufferedImage redimensionar(BufferedImage original, int anchoDeseado) {
        int alturaDeseada = (int) ((double) original.getHeight() / original.getWidth() * anchoDeseado);

        BufferedImage redimensionada = new BufferedImage(anchoDeseado, alturaDeseada, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = redimensionada.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(original, 0, 0, anchoDeseado, alturaDeseada, null);
        g.dispose();
        return redimensionada;
    }

    private void aplicarMarcaDeAgua(BufferedImage imagen, String texto) {
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int tamanioFuente = Math.max(12, imagen.getWidth() / 20);
        g.setFont(new Font("Arial", Font.BOLD, tamanioFuente));

        FontMetrics metrics = g.getFontMetrics();
        int anchoTexto = metrics.stringWidth(texto);
        int x = imagen.getWidth() - anchoTexto - 10;
        int y = imagen.getHeight() - 10;

        // Sombra para que se lea bien sobre cualquier fondo
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(texto, x + 1, y + 1);

        g.setColor(new Color(255, 255, 255, 200));
        g.drawString(texto, x, y);

        g.dispose();
    }

}