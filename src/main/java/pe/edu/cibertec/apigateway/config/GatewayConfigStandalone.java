package pe.edu.cibertec.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    @Value("${user.service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${product.service.url:http://localhost:8082}")
    private String productServiceUrl;

    @Value("${order.service.url:http://localhost:8083}")
    private String orderServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Ruta de prueba simple (para debugging)
                .route("test-route", r -> r
                        .path("/test")
                        .uri("http://httpbin.org:80"))

                // Rutas de autenticación (públicas) - Usando URL directa en producción
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri(userServiceUrl))

                // Rutas de administración (protegidas - solo ADMIN)
                .route("admin-service", r -> r
                        .path("/api/admin/**")
                        .uri(userServiceUrl))

                // Rutas protegidas de usuarios
                .route("user-service", r -> r
                        .path("/api/usuarios/**")
                        .uri(userServiceUrl))

                // Rutas protegidas de productos
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .uri(productServiceUrl))

                // Rutas protegidas de órdenes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .uri(orderServiceUrl))

                // Rutas para imágenes de productos (protegidas)
                .route("product-images", r -> r
                        .path("/portadas/**")
                        .uri(productServiceUrl))

                // Rutas para uploads (protegidas)
                .route("uploads", r -> r
                        .path("/uploads/**")
                        .uri(productServiceUrl))

                .build();
    }
}
