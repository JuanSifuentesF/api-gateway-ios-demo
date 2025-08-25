package pe.edu.cibertec.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Ruta de prueba simple (para debugging)
                .route("test-route", r -> r
                        .path("/test")
                        .uri("http://httpbin.org:80"))

                // Rutas de autenticación (públicas) - A través del LB
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))

                // Rutas de administración (protegidas - solo ADMIN)
                .route("admin-service", r -> r
                        .path("/api/admin/**")
                        .uri("lb://user-service"))

                // Rutas protegidas de usuarios - El filtro se aplica globalmente desde
                // SecurityConfig
                .route("user-service", r -> r
                        .path("/api/usuarios/**")
                        .uri("lb://user-service"))

                // Rutas protegidas de productos
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .uri("lb://product-service"))

                // Rutas protegidas de órdenes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .uri("lb://order-service"))

                // Rutas para imágenes de productos (protegidas)
                .route("product-images", r -> r
                        .path("/portadas/**")
                        .uri("lb://product-service"))

                // Rutas para uploads (protegidas)
                .route("uploads", r -> r
                        .path("/uploads/**")
                        .uri("lb://product-service"))

                .build();
    }
}
