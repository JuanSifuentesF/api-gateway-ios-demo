package pe.edu.cibertec.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user")
    @PostMapping("/user")
    @PutMapping("/user")
    @DeleteMapping("/user")
    public Mono<String> userServiceFallback() {
        return Mono.just("{\"error\":\"User service is temporarily unavailable\",\"status\":503,\"timestamp\":\"" + 
                         System.currentTimeMillis() + "\",\"message\":\"Please try again later\"}");
    }

    @GetMapping("/product")
    @PostMapping("/product")
    @PutMapping("/product")
    @DeleteMapping("/product")
    public Mono<String> productServiceFallback() {
        return Mono.just("{\"error\":\"Product service is temporarily unavailable\",\"status\":503,\"timestamp\":\"" + 
                         System.currentTimeMillis() + "\",\"message\":\"Please try again later\"}");
    }

    @GetMapping("/order")
    @PostMapping("/order")
    @PutMapping("/order")
    @DeleteMapping("/order")
    public Mono<String> orderServiceFallback() {
        return Mono.just("{\"error\":\"Order service is temporarily unavailable\",\"status\":503,\"timestamp\":\"" + 
                         System.currentTimeMillis() + "\",\"message\":\"Please try again later\"}");
    }
}
