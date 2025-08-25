package pe.edu.cibertec.apigateway.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class MockAuthController {

    @Value("${jwt.secret:MiClaveSecretaSuperSeguraParaJWTEnMicroserviciosQueDebeSerMuyLarga}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        // Validar credenciales mock (para demo)
        if ("admin@admin.com".equals(request.getEmail()) && "123456".equals(request.getPassword())) {
            
            // Generar JWT token
            String token = Jwts.builder()
                    .subject(request.getEmail())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24 horas
                    .signWith(getSigningKey())
                    .compact();

            // Crear respuesta mock
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("id", 1);
            response.put("email", "admin@admin.com");
            response.put("nombre", "Juan Admin");
            response.put("roles", List.of("ADMIN"));

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(Map.of("message", "Credenciales incorrectas"));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            String email = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("email", email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "message", "Token inválido"));
        }
    }

    // Clase interna para el request
    public static class LoginRequest {
        private String email;
        private String password;

        // Getters y setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
