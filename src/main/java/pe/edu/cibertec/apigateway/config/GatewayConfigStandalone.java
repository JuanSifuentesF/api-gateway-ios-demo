package pe.edu.cibertec.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfigStandalone {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Ruta de prueba simple (para debugging)
                .route("test-route", r -> r
                        .path("/test")
                        .uri("http://httpbin.org:80"))

                // Ruta para Product Service (datos reales)
                .route("product-service", r -> r
                        .path("/product/**")
                        .uri("lb://product-service"))

                // Ruta para User Service (autenticación real)
                .route("user-service", r -> r
                        .path("/user/**")
                        .uri("lb://user-service"))

                // Ruta para Order Service
                .route("order-service", r -> r
                        .path("/order/**")
                        .uri("lb://order-service"))
                
                .build();
    }
}
