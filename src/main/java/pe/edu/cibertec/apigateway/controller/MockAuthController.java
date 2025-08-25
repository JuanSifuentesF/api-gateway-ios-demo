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
import org.springframework.web.bind.annotation.PathVariable;
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

    // Endpoints de Productos (públicos - sin autenticación)
    @GetMapping("/products")
    public ResponseEntity<List<Map<String, Object>>> getProducts() {
        List<Map<String, Object>> products = List.of(
            Map.of("id", 12, "name", "Laptop X", "description", "Laptop de alta performance", "price", 2499, "portada", "laptop.jpg", "stock", 25),
            Map.of("id", 33, "name", "Mouse Óptico", "description", "Mouse ergonómico inalámbrico", "price", 59, "portada", "mouse.jpg", "stock", 100),
            Map.of("id", 45, "name", "Teclado Mecánico", "description", "Teclado gaming RGB", "price", 189, "portada", "keyboard.jpg", "stock", 50),
            Map.of("id", 67, "name", "Monitor 24\"", "description", "Monitor Full HD IPS", "price", 799, "portada", "monitor.jpg", "stock", 15),
            Map.of("id", 89, "name", "Webcam HD", "description", "Cámara web 1080p", "price", 129, "portada", "webcam.jpg", "stock", 75)
        );
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable int id) {
        // Simulación de búsqueda por ID
        Map<String, Object> product = switch (id) {
            case 12 -> Map.of("id", 12, "name", "Laptop X", "description", "Laptop de alta performance con procesador Intel i7, 16GB RAM, SSD 512GB", "price", 2499, "portada", "laptop.jpg", "stock", 25);
            case 33 -> Map.of("id", 33, "name", "Mouse Óptico", "description", "Mouse ergonómico inalámbrico con sensor óptico de alta precisión", "price", 59, "portada", "mouse.jpg", "stock", 100);
            case 45 -> Map.of("id", 45, "name", "Teclado Mecánico", "description", "Teclado gaming mecánico con switches azules y retroiluminación RGB", "price", 189, "portada", "keyboard.jpg", "stock", 50);
            case 67 -> Map.of("id", 67, "name", "Monitor 24\"", "description", "Monitor Full HD IPS de 24 pulgadas con conectividad HDMI y USB-C", "price", 799, "portada", "monitor.jpg", "stock", 15);
            case 89 -> Map.of("id", 89, "name", "Webcam HD", "description", "Cámara web 1080p con micrófono integrado y enfoque automático", "price", 129, "portada", "webcam.jpg", "stock", 75);
            default -> null;
        };
        
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    // Endpoints de Órdenes (requieren autenticación)
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getOrdersByUser(@PathVariable int userId, @RequestHeader("Authorization") String authorization) {
        try {
            // Validar token
            String token = authorization.replace("Bearer ", "");
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);

            // Órdenes mock para el usuario
            List<Map<String, Object>> orders = List.of(
                Map.of("id", 101, "orderDate", "2025-08-25T15:43:10.123", "userId", userId,
                       "items", List.of(
                           Map.of("id", 1, "productId", 12, "productName", "Laptop X", "productPrice", 2499, "quantity", 1),
                           Map.of("id", 2, "productId", 33, "productName", "Mouse Óptico", "productPrice", 59, "quantity", 2)
                       )),
                Map.of("id", 102, "orderDate", "2025-08-20T10:30:45.456", "userId", userId,
                       "items", List.of(
                           Map.of("id", 3, "productId", 45, "productName", "Teclado Mecánico", "productPrice", 189, "quantity", 1)
                       ))
            );
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
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
