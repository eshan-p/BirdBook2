package com.birdbook.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfiguration implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get the absolute path to the backend directory
        String baseDir = System.getProperty("user.dir");
        
        // Serve images from the images directory
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + baseDir + "/images/");
        
        // Serve profile pictures from the profile_pictures directory
        registry.addResourceHandler("/profile_pictures/**")
                .addResourceLocations("file:" + baseDir + "/profile_pictures/");
                
        // Serve backend_profile_pictures (referenced in MongoDataInitializer)
        registry.addResourceHandler("/backend_profile_pictures/**")
                .addResourceLocations("file:" + baseDir + "/backend_profile_pictures/");
    }
}
