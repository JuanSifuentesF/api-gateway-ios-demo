package pe.edu.cibertec.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {
    
    private final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    
    public LoggingFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            logger.info("Gateway Request: {} {} from {}", 
                       request.getMethod(), 
                       request.getURI(), 
                       request.getRemoteAddress());
            
            return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    logger.info("Gateway Response: {} {} completed with status {}", 
                               request.getMethod(), 
                               request.getURI(), 
                               exchange.getResponse().getStatusCode());
                })
            );
        };
    }
    
    public static class Config {
        // Configuración del filtro si es necesaria
        private String message;
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
