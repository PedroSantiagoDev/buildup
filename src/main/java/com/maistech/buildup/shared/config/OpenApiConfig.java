package com.maistech.buildup.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearer-jwt";

        return new OpenAPI()
            .info(new Info()
                .title("BuildUp API")
                .description("""
                    Multi-tenant B2B application with role-based access control.
                    
                    ## Features
                    - JWT Authentication
                    - Multi-tenant row-level isolation
                    - Role-based permissions (USER, ADMIN, SUPER_ADMIN)
                    - Automatic tenant filtering
                    
                    ## Authentication
                    1. Login with POST /api/auth/login to get JWT token
                    2. Click 'Authorize' button and paste token
                    3. Token contains user info and company (tenant) context
                    
                    ## Multi-Tenancy
                    All data is automatically filtered by company. Users only see their company's data.
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("MaisTech Support")
                    .email("support@maistech.com")
                    .url("https://maistech.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Development Server"),
                new Server()
                    .url("https://api.buildup.com")
                    .description("Production Server")))
            .addSecurityItem(new SecurityRequirement()
                .addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Enter JWT token obtained from /api/auth/login endpoint")));
    }
}
