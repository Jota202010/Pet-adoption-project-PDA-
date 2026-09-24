package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor 
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    private static final int MINUTOS_VALIDEZ_CODIGO = 15;
    private static final SecureRandom RANDOM = new SecureRandom();
   
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

    // ══════════════════════════════════════════════════════════════════
    // Recuperación de contraseña
    // ══════════════════════════════════════════════════════════════════

    /**
     * Genera un código de 6 dígitos para el correo indicado y lo guarda con vencimiento.
     * Devuelve el código para que el controller se lo pase al EmailService.
     * Por seguridad, SIEMPRE se comporta igual exista o no el correo (no revela si existe).
     */
    public Optional<String> generarCodigoReset(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.trim().toLowerCase());
        if (usuarioOpt.isEmpty()) {
            return Optional.empty(); // el controller decide igual mostrar mensaje genérico
        }
        Usuario usuario = usuarioOpt.get();
        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000)); // 6 dígitos, con ceros a la izquierda
        usuario.setCodigoReset(codigo);
        usuario.setCodigoResetExpira(LocalDateTime.now().plusMinutes(MINUTOS_VALIDEZ_CODIGO));
        usuarioRepository.save(usuario);
        return Optional.of(codigo);
    }

    /**
     * Valida el código y, si es correcto y no ha vencido, cambia la contraseña.
     * Lanza IllegalStateException con un mensaje claro si algo no es válido.
     */
    public void restablecerContrasena(String email, String codigo, String nuevaContrasena) {
    Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
            .orElseThrow(() -> new IllegalStateException("Código inválido o vencido."));

    if (usuario.getCodigoReset() == null || !usuario.getCodigoReset().equals(codigo.trim())) {
        throw new IllegalStateException("El código ingresado no es correcto.");
    }
    if (usuario.getCodigoResetExpira() == null || usuario.getCodigoResetExpira().isBefore(LocalDateTime.now())) {
        throw new IllegalStateException("El código ha vencido. Solicita uno nuevo.");
    }
    if (nuevaContrasena == null || nuevaContrasena.length() < 8) {
        throw new IllegalStateException("La nueva contraseña debe tener al menos 8 caracteres.");
    }
    if (passwordEncoder.matches(nuevaContrasena, usuario.getContrasena())) {
        throw new IllegalStateException("Esta contraseña ya fue utilizada. Elige una diferente a la actual.");
    }

    usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
    usuario.setCodigoReset(null);
    usuario.setCodigoResetExpira(null);
    usuarioRepository.save(usuario);
}
}