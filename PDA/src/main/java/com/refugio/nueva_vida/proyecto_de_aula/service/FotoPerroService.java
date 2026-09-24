package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.config.RabbitMQConfig;
import com.refugio.nueva_vida.proyecto_de_aula.dtos.FotoProcesamientoDTO;
import com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.repository.FotoPerroRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor 
public class FotoPerroService {

    private final FotoPerroRepository fotoRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.upload.dir:uploads/fotos}")
    private String uploadDir;

    public FotoPerro guardarFoto(Perro perro, MultipartFile archivo, boolean esPerfil) throws IOException {
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen.");
        }

        // Crear carpeta si no existe
        Path carpeta = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(carpeta);

        // Nombre único
        String original = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "foto.jpg";
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".jpg";
        String nombreArchivo = "perro_" + perro.getIdPerro() + "_" + UUID.randomUUID() + extension;

        // Guardar en disco
        Path destino = carpeta.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        // Obtener fotos existentes ANTES de guardar la nueva
        List<FotoPerro> fotosExistentes = fotoRepository.findByPerroOrderByOrdenAsc(perro);
        int orden = fotosExistentes.size();

        // AUTO-PERFIL: si el perro no tiene fotos todavía, esta primera foto
        // se convierte automáticamente en la de perfil aunque no se marque el checkbox
        boolean noTieneFotos = fotosExistentes.isEmpty();
        boolean debeSerPerfil = esPerfil || noTieneFotos;

        // Si debe ser perfil, quitar el perfil anterior si existe
        if (debeSerPerfil) {
            fotoRepository.findByPerroAndEsPerfilTrue(perro)
                    .ifPresent(f -> { f.setEsPerfil(false); fotoRepository.save(f); });
        }

        String urlRelativa = "/fotos/" + nombreArchivo;
        FotoPerro foto = new FotoPerro(perro, urlRelativa, debeSerPerfil, orden);
        FotoPerro fotoGuardada = fotoRepository.save(foto);

        // Publicar evento para procesamiento asíncrono (miniatura + marca de agua)
        try {
            FotoProcesamientoDTO evento = FotoProcesamientoDTO.builder()
                    .idFoto(fotoGuardada.getIdFoto())
                    .nombreArchivo(nombreArchivo)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_FOTOS,
                    RabbitMQConfig.ROUTING_KEY_FOTOS,
                    evento
            );
            log.info("Evento de procesamiento de foto enviado: {}", nombreArchivo);
        } catch (Exception e) {
            // No se debe romper la subida de la foto si RabbitMQ falla.
            // La foto original ya quedó guardada correctamente.
            log.error("No se pudo encolar el procesamiento de la foto {}: {}", nombreArchivo, e.getMessage());
        }

        return fotoGuardada;
    }

    @Transactional(readOnly = true)
    public List<FotoPerro> fotosDePerro(Perro perro) {
        return fotoRepository.findByPerroOrderByOrdenAsc(perro);
    }

    @Transactional(readOnly = true)
    public Optional<FotoPerro> fotoPerfil(Perro perro) {
        return fotoRepository.findByPerroAndEsPerfilTrue(perro);
    }

    public void eliminarFoto(Integer idFoto) throws IOException {
        FotoPerro foto = fotoRepository.findById(idFoto).orElseThrow();
        String nombreArchivo = foto.getUrlFoto().replace("/fotos/", "");
        Path archivo = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(nombreArchivo);
        Files.deleteIfExists(archivo);

        // Borrar también la miniatura si existe
        Path thumb = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("thumb_" + nombreArchivo);
        Files.deleteIfExists(thumb);

        fotoRepository.deleteById(idFoto);
    }

    
    @Transactional
    public void eliminarFotosDe(Perro perro) {
        List<FotoPerro> fotos = fotoRepository.findByPerroOrderByOrdenAsc(perro);
        for (FotoPerro foto : fotos) {
            try {
                eliminarFoto(foto.getIdFoto());
            } catch (IOException e) {
                log.warn("No se pudo borrar el archivo de la foto #{} en disco ({}). " +
                        "Se elimina igualmente el registro para no bloquear el borrado del perro.",
                        foto.getIdFoto(), e.getMessage());
                fotoRepository.deleteById(foto.getIdFoto());
            }
        }
    }
}