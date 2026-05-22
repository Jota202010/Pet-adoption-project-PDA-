package com.refugio.nueva_vida.proyecto_de_aula.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/fotos}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Recursos estáticos del classpath (CSS, JS, imágenes)
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        // Fotos subidas por el admin — directorio externo en disco
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        if (!absolutePath.endsWith("/")) absolutePath += "/";
        registry.addResourceHandler("/fotos/**")
                .addResourceLocations(absolutePath);
    }
}
