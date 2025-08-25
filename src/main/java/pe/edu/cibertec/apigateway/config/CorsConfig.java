package pe.edu.cibertec.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class CorsConfig {

    @Bean
    public GlobalFilter corsGlobalFilter() {
        return new CorsGlobalFilter();
    }

    private static class CorsGlobalFilter implements GlobalFilter, Ordered {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            String origin = request.getHeaders().getOrigin();

            if (request.getMethod() == HttpMethod.OPTIONS) {
                ServerHttpResponse response = exchange.getResponse();
                HttpHeaders headers = response.getHeaders();

                // Solo permitir localhost:4200 para OPTIONS requests
                if ("http://localhost:4200".equals(origin)) {
                    headers.set("Access-Control-Allow-Origin", "http://localhost:4200");
                    headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
                    headers.set("Access-Control-Allow-Headers", "*");
                    headers.set("Access-Control-Allow-Credentials", "true");
                    headers.set("Access-Control-Max-Age", "3600");
                }
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }

            // Decorar la respuesta para interceptar headers
            ServerHttpResponse originalResponse = exchange.getResponse();
            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                @NonNull
                public Mono<Void> writeWith(
                        @NonNull org.reactivestreams.Publisher<? extends org.springframework.core.io.buffer.DataBuffer> body) {
                    HttpHeaders headers = getHeaders();
                    if ("http://localhost:4200".equals(origin)) {
                        // Remover cualquier header CORS existente
                        headers.remove("Access-Control-Allow-Origin");
                        headers.remove("Access-Control-Allow-Methods");
                        headers.remove("Access-Control-Allow-Headers");
                        headers.remove("Access-Control-Allow-Credentials");

                        // Establecer headers correctos
                        headers.set("Access-Control-Allow-Origin", "http://localhost:4200");
                        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
                        headers.set("Access-Control-Allow-Headers", "*");
                        headers.set("Access-Control-Allow-Credentials", "true");
                    }
                    return super.writeWith(body);
                }
            };

            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }
}
