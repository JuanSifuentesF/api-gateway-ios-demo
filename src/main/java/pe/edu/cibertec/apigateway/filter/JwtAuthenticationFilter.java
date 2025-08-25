package pe.edu.cibertec.apigateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret:MiClaveSecretaSuperSeguraParaJWTEnMicroserviciosQueDebeSerMuyLarga}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.info("=== JWT Filter - Procesando ruta: {} ===", path);

        // Rutas públicas que no requieren autenticación
        if (isPublicPath(path)) {
            log.info("Ruta pública detectada, permitiendo acceso: {}", path);
            return chain.filter(exchange);
        }

        // Extraer token del header Authorization
        String token = extractToken(request);
        if (token == null) {
            log.error("Token JWT no encontrado en el header Authorization para ruta: {}", path);
            return onError(exchange, "Token JWT no encontrado", HttpStatus.UNAUTHORIZED);
        }

        log.info("Token JWT encontrado: {}...", token.substring(0, Math.min(20, token.length())));

        try {
            // Validar token
            if (!validateToken(token)) {
                log.error("Token JWT inválido para ruta: {}", path);
                return onError(exchange, "Token JWT inválido", HttpStatus.UNAUTHORIZED);
            }

            log.info("Token JWT validado correctamente para ruta: {}", path);

            // Extraer información del usuario del token
            Claims claims = getClaimsFromToken(token);
            String email = claims.getSubject();

            log.info("Usuario extraído del token: {}", email);

            // Agregar headers para los microservicios downstream
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Email", email)
                    .header("Authorization", "Bearer " + token)
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            log.info("Petición modificada con headers JWT, enviando a microservicio downstream");

            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            log.error("Error al validar JWT para ruta {}: ", path, e);
            return onError(exchange, "Error al validar token", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicPath(String path) {
        List<String> publicPaths = List.of(
                "/api/auth/login",
                "/api/auth/register",
                "/api/auth/validate",
                "/api/auth/check-email",
                "/api/products",  // Agregar acceso público a productos
                "/api/products/portadas",
                "/portadas",
                "/uploads",
                "/actuator",
                "/eureka");

        return publicPaths.stream().anyMatch(path::startsWith);
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Error validando token JWT: ", e);
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        log.error("Error de autenticación: {}", message);
        return response.setComplete();
    }
}
