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

                // Todas las rutas API se manejan localmente con MockAuthController
                // No necesitamos rutear a servicios externos ya que todo es mock
                
                .build();
    }
}
