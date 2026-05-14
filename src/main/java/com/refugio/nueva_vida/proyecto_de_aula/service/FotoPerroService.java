package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.repository.FotoPerroRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FotoPerroService {

    private final FotoPerroRepository fotoRepository;

    @Value("${app.upload.dir:uploads/fotos}")
    private String uploadDir;

    public FotoPerroService(FotoPerroRepository fotoRepository) {
        this.fotoRepository = fotoRepository;
    }

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
        return fotoRepository.save(foto);
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
        fotoRepository.deleteById(idFoto);
    }
}
