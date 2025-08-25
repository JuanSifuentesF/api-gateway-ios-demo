package pe.edu.cibertec.apigateway.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class MockDataController {

    // Mock data para productos
    private static final List<Map<String, Object>> MOCK_PRODUCTS = Arrays.asList(
        Map.of(
            "id", 1,
            "nombre", "iPhone 14 Pro",
            "precio", 1299.99,
            "descripcion", "Smartphone Apple con chip A16 Bionic",
            "categoria", "Smartphones",
            "stock", 25,
            "imagen", "https://via.placeholder.com/300x300/007ACC/FFFFFF?text=iPhone+14"
        ),
        Map.of(
            "id", 2,
            "nombre", "Samsung Galaxy S23",
            "precio", 899.99,
            "descripcion", "Smartphone Samsung con cámara de 200MP",
            "categoria", "Smartphones", 
            "stock", 15,
            "imagen", "https://via.placeholder.com/300x300/FF6B35/FFFFFF?text=Galaxy+S23"
        ),
        Map.of(
            "id", 3,
            "nombre", "MacBook Pro M2",
            "precio", 1999.99,
            "descripcion", "Laptop Apple con chip M2 de alto rendimiento",
            "categoria", "Laptops",
            "stock", 10,
            "imagen", "https://via.placeholder.com/300x300/28A745/FFFFFF?text=MacBook+Pro"
        ),
        Map.of(
            "id", 4,
            "nombre", "Dell XPS 13",
            "precio", 1299.99,
            "descripcion", "Laptop ultrabook con pantalla InfinityEdge",
            "categoria", "Laptops",
            "stock", 8,
            "imagen", "https://via.placeholder.com/300x300/6C757D/FFFFFF?text=Dell+XPS"
        ),
        Map.of(
            "id", 5,
            "nombre", "AirPods Pro",
            "precio", 249.99,
            "descripcion", "Audífonos inalámbricos con cancelación de ruido",
            "categoria", "Accesorios",
            "stock", 50,
            "imagen", "https://via.placeholder.com/300x300/DC3545/FFFFFF?text=AirPods+Pro"
        )
    );

    // Endpoints de productos (públicos)
    @GetMapping("/api/products")
    public ResponseEntity<List<Map<String, Object>>> getAllProducts() {
        return ResponseEntity.ok(MOCK_PRODUCTS);
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        Optional<Map<String, Object>> product = MOCK_PRODUCTS.stream()
                .filter(p -> p.get("id").equals(id.intValue()))
                .findFirst();
        
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        }
        return ResponseEntity.notFound().build();
    }

    // Endpoints de órdenes (requieren autenticación)
    @GetMapping("/api/orders/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserOrders(
            @PathVariable Long userId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        // Verificar que hay token (validación básica)
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(null);
        }

        // Mock data de órdenes para el usuario
        List<Map<String, Object>> orders = Arrays.asList(
            Map.of(
                "id", 1,
                "userId", userId,
                "total", 1549.98,
                "fecha", "2025-08-20T10:30:00",
                "estado", "Entregado",
                "productos", Arrays.asList(
                    Map.of("id", 1, "nombre", "iPhone 14 Pro", "cantidad", 1, "precio", 1299.99),
                    Map.of("id", 5, "nombre", "AirPods Pro", "cantidad", 1, "precio", 249.99)
                )
            ),
            Map.of(
                "id", 2,
                "userId", userId,
                "total", 899.99,
                "fecha", "2025-08-22T15:45:00",
                "estado", "En proceso",
                "productos", Arrays.asList(
                    Map.of("id", 2, "nombre", "Samsung Galaxy S23", "cantidad", 1, "precio", 899.99)
                )
            )
        );

        return ResponseEntity.ok(orders);
    }

    // Endpoint de salud para verificar que el servicio funciona
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", new Date());
        response.put("service", "API Gateway Mock");
        return ResponseEntity.ok(response);
    }
}
