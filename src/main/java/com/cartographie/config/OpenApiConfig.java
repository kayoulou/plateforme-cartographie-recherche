package com.cartographie.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Plateforme Cartographie")
                        .description("API simplifiée pour les besoins essentiels")
                        .version("1.0"));
    }

    @Bean
    public GroupedOpenApi essentialApi() {
        return GroupedOpenApi.builder()
                .group("Essentiel")
                .packagesToScan("com.cartographie.controller")
                .pathsToMatch(
                        "/projets/mes-projets",
                        "/projets/enregistrer",
                        "/projets/tous",
                        "/admin/users",
                        "/admin/users/role",
                        "/admin/config/save",
                        "/stats/dashboard",
                        "/stats/rapport")
                .build();
    }
}
