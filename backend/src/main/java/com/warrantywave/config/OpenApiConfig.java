package com.warrantywave.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI warrantyWaveOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
            .info(new Info()
                .title("WarrantyWave Automotive Warranty & Financing API")
                .description("Enterprise REST API specifications for Automotive Warranty Plans, Claims Adjudication, Credit Risk Financing, and Vehicle Policy Contracts.")
                .version("1.0.0")
                .contact(new Contact().name("WarrantyWave Enterprise Architecture").email("support@warrantywave.com"))
                .license(new License().name("Proprietary Automotive Platform")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
