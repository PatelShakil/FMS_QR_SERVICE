package com.shakilpatel.qr.service.config;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ws.rs.ApplicationPath;
import org.eclipse.microprofile.auth.LoginConfig;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

@SuppressWarnings({"EmptyClass", "SuppressionAnnotation"})
@ApplicationPath("/api")
//@LoginConfig(authMethod = "MP-JWT")
@DeclareRoles({"PLATFORM_ADMIN", "ORG_OWNER", "ORG_ADMIN","DISTRIBUTOR"})
@SecurityScheme(
        securitySchemeName = "jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        info = @Info(
                title = "BoloIndia QR Service API",
                version = "1.0",
                description = "QR Management Service",
                contact = @Contact(
                        name = "MuhammadShakil Patel",
                        email = "shakil@boloindia.in"
                )
        ),
        servers = {
            @Server(url = "http://localhost:8089/qr-service", description = "Development")
        },
        security = {
            @SecurityRequirement(name = "jwt")
        }
)
public class BootStrap extends jakarta.ws.rs.core.Application {
}
