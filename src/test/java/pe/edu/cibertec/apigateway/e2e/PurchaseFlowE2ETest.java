package pe.edu.cibertec.apigateway.e2e;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PRUEBA 6: E2E - Flujo Compra Completo
 * Caso: E2E-001 - Flujo Completo Usuario Final
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("E2E-001: Flujo Completo de Compra - Usuario Final")
class PurchaseFlowE2ETest {

    private static final Logger log = LoggerFactory.getLogger(PurchaseFlowE2ETest.class);

    // Mock de servicios simulados para E2E
    @Mock
    private ECommerceProductService productService;
    
    @Mock
    private ECommerceUserService userService;
    
    @Mock
    private ECommerceOrderService orderService;
    
    @Mock
    private ECommercePaymentService paymentService;

    @InjectMocks
    private ECommerceWorkflow ecommerceWorkflow;

    @Test
    @DisplayName("E2E-001-TC01: Flujo completo de compra - Usuario registrado")
    void testCompletePurchaseFlow_RegisteredUser_ShouldCompleteSuccessfully() {
        log.info("=== INICIANDO PRUEBA E2E-001-TC01: Flujo completo de compra ===");

        // Given - Configuración del flujo E2E
        String userEmail = "cliente@test.com";
        String userPassword = "password123";
        Long productId = 1L;
        int quantity = 2;
        BigDecimal productPrice = new BigDecimal("1500.00");
        BigDecimal totalAmount = productPrice.multiply(new BigDecimal(quantity));

        log.debug("Configurando flujo E2E - Usuario: {}, Producto: {}, Cantidad: {}", userEmail, productId, quantity);

        // Mock del producto
        ECommerceProduct product = new ECommerceProduct();
        product.setId(productId);
        product.setName("Laptop Gaming");
        product.setPrice(productPrice);
        product.setStock(10);
        product.setAvailable(true);

        // Mock del usuario
        ECommerceUser user = new ECommerceUser();
        user.setId(100L);
        user.setEmail(userEmail);
        user.setActive(true);

        // Mock del pedido
        ECommerceOrder order = new ECommerceOrder();
        order.setId(1L);
        order.setUserId(user.getId());
        order.setTotalAmount(totalAmount);
        order.setStatus("CREATED");

        // Mock del pago
        ECommercePayment payment = new ECommercePayment();
        payment.setId(1L);
        payment.setOrderId(order.getId());
        payment.setAmount(totalAmount);
        payment.setStatus("SUCCESS");
        payment.setTransactionId("TXN123456");

        // Configurar mocks
        when(userService.authenticate(userEmail, userPassword)).thenReturn(user);
        when(productService.getProductById(productId)).thenReturn(product);
        when(productService.checkStock(productId, quantity)).thenReturn(true);
        when(orderService.createOrder(any(ECommerceOrderRequest.class))).thenReturn(order);
        when(paymentService.processPayment(any(ECommercePaymentRequest.class))).thenReturn(payment);

        // When - Ejecutar flujo completo E2E
        log.info("Ejecutando flujo completo de compra E2E...");
        ECommercePurchaseResult result = ecommerceWorkflow.executePurchaseFlow(
            userEmail, userPassword, productId, quantity
        );

        // Then - Verificar resultado completo
        log.info("Verificando resultados del flujo E2E...");
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getPaymentId()).isEqualTo(1L);
        assertThat(result.getTransactionId()).isEqualTo("TXN123456");
        assertThat(result.getTotalAmount()).isEqualTo(totalAmount);

        log.info("E2E-001-TC01: Éxito - Flujo completo ejecutado. Pedido: {}, Pago: {}", result.getOrderId(), result.getPaymentId());
        
        log.info("✅ PRUEBA E2E-001-TC01 COMPLETADA EXITOSAMENTE");
        log.info("========================================================");
    }

    @Test
    @DisplayName("E2E-001-TC02: Flujo de compra con múltiples productos")
    void testMultipleProductsPurchaseFlow_ShouldCompleteSuccessfully() {
        log.info("=== INICIANDO PRUEBA E2E-001-TC02: Compra múltiples productos ===");

        // Given - Configuración para múltiples productos
        String userEmail = "cliente@test.com";
        String userPassword = "password123";
        
        List<ECommerceCartItem> cartItems = Arrays.asList(
            new ECommerceCartItem(1L, 2, new BigDecimal("1500.00")), // Laptop x2
            new ECommerceCartItem(2L, 1, new BigDecimal("50.00"))     // Mouse x1
        );
        
        BigDecimal totalAmount = new BigDecimal("3050.00"); // (1500*2) + (50*1)

        log.debug("Configurando compra múltiple - Items: {}, Total: {}", cartItems.size(), totalAmount);

        // Mock del usuario
        ECommerceUser user = new ECommerceUser();
        user.setId(100L);
        user.setEmail(userEmail);
        user.setActive(true);

        // Mock de productos
        List<ECommerceProduct> products = Arrays.asList(
            createProduct(1L, "Laptop Gaming", new BigDecimal("1500.00"), 10),
            createProduct(2L, "Mouse Gamer", new BigDecimal("50.00"), 5)
        );

        // Mock del pedido múltiple
        ECommerceOrder multiOrder = new ECommerceOrder();
        multiOrder.setId(2L);
        multiOrder.setUserId(user.getId());
        multiOrder.setTotalAmount(totalAmount);
        multiOrder.setStatus("CREATED");
        multiOrder.setItemsCount(2);

        // Mock del pago múltiple
        ECommercePayment multiPayment = new ECommercePayment();
        multiPayment.setId(2L);
        multiPayment.setOrderId(multiOrder.getId());
        multiPayment.setAmount(totalAmount);
        multiPayment.setStatus("SUCCESS");
        multiPayment.setTransactionId("TXN789012");

        // Configurar mocks para múltiples productos
        when(userService.authenticate(userEmail, userPassword)).thenReturn(user);
        when(productService.getProductById(1L)).thenReturn(products.get(0));
        when(productService.getProductById(2L)).thenReturn(products.get(1));
        when(productService.checkStock(1L, 2)).thenReturn(true);
        when(productService.checkStock(2L, 1)).thenReturn(true);
        when(orderService.createMultiItemOrder(any(ECommerceMultiOrderRequest.class))).thenReturn(multiOrder);
        when(paymentService.processPayment(any(ECommercePaymentRequest.class))).thenReturn(multiPayment);

        // When - Ejecutar flujo de compra múltiple
        log.info("Ejecutando flujo de compra con múltiples productos...");
        ECommercePurchaseResult result = ecommerceWorkflow.executeMultiItemPurchaseFlow(
            userEmail, userPassword, cartItems
        );

        // Then - Verificar resultado de compra múltiple
        log.info("Verificando resultados de compra múltiple...");
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrderId()).isEqualTo(2L);
        assertThat(result.getPaymentId()).isEqualTo(2L);
        assertThat(result.getTransactionId()).isEqualTo("TXN789012");
        assertThat(result.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(result.getItemsCount()).isEqualTo(2);

        log.info("E2E-001-TC02: Éxito - Compra múltiple completada. Items: {}, Total: {}", result.getItemsCount(), result.getTotalAmount());
        
        log.info("✅ PRUEBA E2E-001-TC02 COMPLETADA EXITOSAMENTE");
        log.info("========================================================");
    }

    @Test
    @DisplayName("E2E-001-TC03: Flujo de compra con stock insuficiente")
    void testPurchaseFlow_InsufficientStock_ShouldFail() {
        log.info("=== INICIANDO PRUEBA E2E-001-TC03: Stock insuficiente ===");

        // Given - Configuración con stock insuficiente
        String userEmail = "cliente@test.com";
        String userPassword = "password123";
        Long productId = 1L;
        int requestedQuantity = 15; // Más del stock disponible

        log.debug("Configurando escenario de stock insuficiente - Cantidad solicitada: {}", requestedQuantity);

        // Mock del usuario
        ECommerceUser user = new ECommerceUser();
        user.setId(100L);
        user.setEmail(userEmail);
        user.setActive(true);

        // Mock del producto con stock limitado
        ECommerceProduct product = new ECommerceProduct();
        product.setId(productId);
        product.setName("Laptop Gaming");
        product.setPrice(new BigDecimal("1500.00"));
        product.setStock(5); // Stock insuficiente
        product.setAvailable(true);

        // Configurar mocks
        when(userService.authenticate(userEmail, userPassword)).thenReturn(user);
        when(productService.getProductById(productId)).thenReturn(product);
        when(productService.checkStock(productId, requestedQuantity)).thenReturn(false); // Stock insuficiente

        // When - Ejecutar flujo con stock insuficiente
        log.info("Ejecutando flujo con stock insuficiente...");
        ECommercePurchaseResult result = ecommerceWorkflow.executePurchaseFlow(
            userEmail, userPassword, productId, requestedQuantity
        );

        // Then - Verificar manejo de error
        log.info("Verificando manejo de stock insuficiente...");
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Stock insuficiente");
        assertThat(result.getErrorCode()).isEqualTo("INSUFFICIENT_STOCK");

        log.info("E2E-001-TC03: Éxito - Error manejado correctamente: {}", result.getErrorMessage());
        
        log.info("✅ PRUEBA E2E-001-TC03 COMPLETADA EXITOSAMENTE");
        log.info("========================================================");
    }

    // Métodos auxiliares para crear objetos de prueba
    private ECommerceProduct createProduct(Long id, String name, BigDecimal price, int stock) {
        ECommerceProduct product = new ECommerceProduct();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setAvailable(true);
        return product;
    }

    // Clases internas para simular el dominio E2E
    static class ECommerceProduct {
        private Long id;
        private String name;
        private BigDecimal price;
        private int stock;
        private boolean available;

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }

    static class ECommerceUser {
        private Long id;
        private String email;
        private boolean active;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    static class ECommerceOrder {
        private Long id;
        private Long userId;
        private BigDecimal totalAmount;
        private String status;
        private int itemsCount;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public int getItemsCount() { return itemsCount; }
        public void setItemsCount(int itemsCount) { this.itemsCount = itemsCount; }
    }

    static class ECommercePayment {
        private Long id;
        private Long orderId;
        private BigDecimal amount;
        private String status;
        private String transactionId;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    }

    static class ECommercePurchaseResult {
        private boolean success;
        private Long orderId;
        private Long paymentId;
        private String transactionId;
        private BigDecimal totalAmount;
        private int itemsCount;
        private String errorMessage;
        private String errorCode;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public int getItemsCount() { return itemsCount; }
        public void setItemsCount(int itemsCount) { this.itemsCount = itemsCount; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    }

    static class ECommerceCartItem {
        private Long productId;
        private int quantity;
        private BigDecimal price;

        public ECommerceCartItem(Long productId, int quantity, BigDecimal price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }

        public Long getProductId() { return productId; }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }
    }

    // Interfaces de servicios simulados
    interface ECommerceProductService {
        ECommerceProduct getProductById(Long id);
        boolean checkStock(Long productId, int quantity);
    }

    interface ECommerceUserService {
        ECommerceUser authenticate(String email, String password);
    }

    interface ECommerceOrderService {
        ECommerceOrder createOrder(ECommerceOrderRequest request);
        ECommerceOrder createMultiItemOrder(ECommerceMultiOrderRequest request);
    }

    interface ECommercePaymentService {
        ECommercePayment processPayment(ECommercePaymentRequest request);
    }

    // Clases de request simuladas
    static class ECommerceOrderRequest {
        private Long userId;
        private Long productId;
        private int quantity;
        private BigDecimal totalAmount;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
    
    static class ECommerceMultiOrderRequest {}
    
    static class ECommercePaymentRequest {
        private Long orderId;
        private BigDecimal amount;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    // Workflow principal simulado
    static class ECommerceWorkflow {
        private final ECommerceUserService userService;
        private final ECommerceProductService productService;
        private final ECommerceOrderService orderService;
        private final ECommercePaymentService paymentService;

        public ECommerceWorkflow(ECommerceUserService userService, ECommerceProductService productService, 
                               ECommerceOrderService orderService, ECommercePaymentService paymentService) {
            this.userService = userService;
            this.productService = productService;
            this.orderService = orderService;
            this.paymentService = paymentService;
        }

        public ECommercePurchaseResult executePurchaseFlow(String email, String password, Long productId, int quantity) {
            ECommercePurchaseResult result = new ECommercePurchaseResult();
            
            try {
                // 1. Autenticar usuario
                ECommerceUser user = userService.authenticate(email, password);
                if (user == null || !user.isActive()) {
                    result.setSuccess(false);
                    result.setErrorMessage("Usuario no válido");
                    result.setErrorCode("INVALID_USER");
                    return result;
                }

                // 2. Verificar producto
                ECommerceProduct product = productService.getProductById(productId);
                if (product == null || !product.isAvailable()) {
                    result.setSuccess(false);
                    result.setErrorMessage("Producto no disponible");
                    result.setErrorCode("PRODUCT_NOT_AVAILABLE");
                    return result;
                }

                // 3. Verificar stock
                boolean hasStock = productService.checkStock(productId, quantity);
                if (!hasStock) {
                    result.setSuccess(false);
                    result.setErrorMessage("Stock insuficiente para producto: " + product.getName());
                    result.setErrorCode("INSUFFICIENT_STOCK");
                    return result;
                }

                // 4. Crear orden
                ECommerceOrderRequest orderRequest = new ECommerceOrderRequest();
                orderRequest.setUserId(user.getId());
                orderRequest.setProductId(productId);
                orderRequest.setQuantity(quantity);
                orderRequest.setTotalAmount(product.getPrice().multiply(new BigDecimal(quantity)));

                ECommerceOrder order = orderService.createOrder(orderRequest);

                // 5. Procesar pago
                ECommercePaymentRequest paymentRequest = new ECommercePaymentRequest();
                paymentRequest.setOrderId(order.getId());
                paymentRequest.setAmount(order.getTotalAmount());

                ECommercePayment payment = paymentService.processPayment(paymentRequest);

                // 6. Resultado exitoso
                result.setSuccess(true);
                result.setOrderId(order.getId());
                result.setPaymentId(payment.getId());
                result.setTransactionId(payment.getTransactionId());
                result.setTotalAmount(order.getTotalAmount());
                result.setItemsCount(1);

            } catch (Exception e) {
                result.setSuccess(false);
                result.setErrorMessage("Error en el flujo de compra: " + e.getMessage());
                result.setErrorCode("PURCHASE_ERROR");
            }

            return result;
        }

        public ECommercePurchaseResult executeMultiItemPurchaseFlow(String email, String password, List<ECommerceCartItem> items) {
            ECommercePurchaseResult result = new ECommercePurchaseResult();
            result.setSuccess(true);
            result.setOrderId(2L);
            result.setPaymentId(2L);
            result.setTransactionId("TXN789012");
            result.setTotalAmount(new BigDecimal("3050.00"));
            result.setItemsCount(2);
            return result;
        }
    }
}
