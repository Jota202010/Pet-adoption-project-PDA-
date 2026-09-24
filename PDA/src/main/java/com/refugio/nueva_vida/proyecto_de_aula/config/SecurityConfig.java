package com.refugio.nueva_vida.proyecto_de_aula.config;

import com.refugio.nueva_vida.proyecto_de_aula.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor 
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    //  Duración de la cookie "Recuérdame" (viene de application.properties)
    @Value("${refugio.seguridad.remember-me.duracion-segundos:1209600}")
    private int rememberMeDuracionSegundos;

    //  Clave secreta con la que Spring firma la cookie "Recuérdame".
    // Puedes cambiarla libremente; solo invalida las cookies ya emitidas.
    private static final String REMEMBER_ME_KEY = "refugioNuevaVida-rememberMe-key-2025";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔑 Proveedor de autenticación que conecta tu servicio de usuarios con el encriptador de contraseñas
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Aquí se usa la variable de forma limpia
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Registramos el proveedor de autenticación personalizado en el flujo de seguridad
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: cualquiera puede acceder
                .requestMatchers("/", "/inicio", "/nosotros", "/mision",
                                 "/login", "/registro", "/css/**", "/js/**",
                                 "/images/**", "/fotos/**", "/mascota/**",
                                 "/olvide-contrasena", "/restablecer-contrasena",
                                 "/error", "/error/**").permitAll()
                // Rutas de admin: solo rol administrador
                .requestMatchers("/admin/**").hasRole("administrador")
                // Rutas de usuario autenticado
                .requestMatchers("/perfil", "/agendar-cita/**", "/testimonios").hasAnyRole("usuario", "administrador")
                
                // Todo lo demás requiere login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")                    // página de login personalizada
                .loginProcessingUrl("/login")           // Spring Security intercepta este POST
                .usernameParameter("usuario")           // nombre del campo en el form
                .passwordParameter("contrasena")        // nombre del campo password en el form
                .defaultSuccessUrl("/inicio", true)     // a dónde va si el login es exitoso
                .failureUrl("/login?error=true")        // a dónde va si falla
                .permitAll()
            )
            // 🆕 "Recuérdame": si el usuario marca la casilla en login.html,
            // Spring guarda una cookie persistente y no le pide iniciar sesión
            // de nuevo aunque cierre el navegador o se reinicie el servidor.
            .rememberMe(remember -> remember
                .key(REMEMBER_ME_KEY)
                .rememberMeParameter("recuerdame")          // nombre del checkbox en login.html
                .tokenValiditySeconds(rememberMeDuracionSegundos)
                .userDetailsService(userDetailsService)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            );

        return http.build();
    }
}