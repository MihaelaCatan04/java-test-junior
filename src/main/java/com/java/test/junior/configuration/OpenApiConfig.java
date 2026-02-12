package com.java.test.junior.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public io.swagger.v3.oas.models.OpenAPI customOpenAPI() {
        return new io.swagger.v3.oas.models.OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Java Test Junior API")
                        .version("1.0")
                        .description("API documentation for the Junior Test project."))
                        .addSecurityItem(new SecurityRequirement().addList("JavaTestJuniorScheme"))
                        .components(new Components().addSecuritySchemes("JavaTestJuniorScheme", new SecurityScheme()
                        .name("JavaTestJuniorScheme").type(SecurityScheme.Type.HTTP).scheme("basic")));
    }
}