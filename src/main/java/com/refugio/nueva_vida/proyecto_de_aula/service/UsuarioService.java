package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    /** Registra un usuario nuevo con contraseña encriptada con BCrypt */
    public Usuario registrar(Usuario usuario) {

        // ── Validaciones de campos obligatorios ───────────────────────────────
        if (usuario.getUsuario() == null || usuario.getUsuario().isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        if (usuario.getUsuario().trim().length() < 3)
            throw new IllegalArgumentException("El nombre de usuario debe tener al menos 3 caracteres.");
        if (usuario.getUsuario().trim().length() > 50)
            throw new IllegalArgumentException("El nombre de usuario no puede superar 50 caracteres.");
        if (!usuario.getUsuario().trim().matches("[a-zA-Z0-9_.]+"))
            throw new IllegalArgumentException("El nombre de usuario solo puede contener letras, números, puntos y guiones bajos.");

        if (usuario.getNombre() == null || usuario.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        if (usuario.getNombre().trim().length() < 2)
            throw new IllegalArgumentException("El nombre completo debe tener al menos 2 caracteres.");

        if (usuario.getEmail() == null || usuario.getEmail().isBlank())
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        if (!usuario.getEmail().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");

        if (usuario.getContrasena() == null || usuario.getContrasena().isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        if (usuario.getContrasena().length() < 8)
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        if (usuario.getContrasena().length() > 100)
            throw new IllegalArgumentException("La contraseña no puede superar 100 caracteres.");

        // ── Unicidad ──────────────────────────────────────────────────────────
        if (usuarioRepository.existsByUsuario(usuario.getUsuario().trim()))
            throw new IllegalArgumentException("El nombre de usuario '" + usuario.getUsuario() + "' ya está en uso.");
        if (usuarioRepository.existsByEmail(usuario.getEmail().trim().toLowerCase()))
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");

        // ── Normalizar y guardar ──────────────────────────────────────────────
        usuario.setUsuario(usuario.getUsuario().trim());
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setRol(Usuario.Rol.usuario);
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsuario(username);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() { return usuarioRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Integer id) { return usuarioRepository.findById(id); }

    public long contarTodos() { return usuarioRepository.count(); }
}
