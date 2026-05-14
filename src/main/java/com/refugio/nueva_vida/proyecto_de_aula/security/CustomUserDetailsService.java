package com.refugio.nueva_vida.proyecto_de_aula.security;

import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Spring Security llama a este servicio cuando alguien intenta hacer login.
 * Busca el usuario en la BD por su nombre de usuario o email.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Intenta buscar por usuario, si no por email
        Usuario usuario = usuarioRepository.findByUsuario(usernameOrEmail)
                .orElseGet(() -> usuarioRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "Usuario no encontrado: " + usernameOrEmail)));

        // Convierte el rol a formato Spring Security: "ROLE_usuario" o "ROLE_administrador"
        String role = "ROLE_" + usuario.getRol().name();

        return new org.springframework.security.core.userdetails.User(
                usuario.getUsuario(),
                usuario.getContrasena(),
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
