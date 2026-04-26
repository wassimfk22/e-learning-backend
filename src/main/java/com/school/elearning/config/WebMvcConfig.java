// ══════════════════════════════════════════════════════
// 2. WebMvcConfig.java  → à créer dans config/
// Expose le dossier uploads/ comme ressource statique HTTP
// Sans ça, l'URL /uploads/photos/xxx.jpg retourne 404
// ══════════════════════════════════════════════════════
package com.school.elearning.config;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
import java.nio.file.Paths;
 
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
 
    @Value("${file.upload-dir:uploads/photos}")
    private String uploadDir;
 
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mappe l'URL /uploads/photos/** vers le dossier physique
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        registry.addResourceHandler("/uploads/photos/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
    
    
    
}