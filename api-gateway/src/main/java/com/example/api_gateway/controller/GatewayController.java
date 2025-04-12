package com.example.api_gateway.controller;

import com.example.api_gateway.dto.Product ; // Импортируем DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api") // Все запросы к шлюзу будут начинаться с /api
public class GatewayController {

    private final WebClient coreServiceClient;

    @Autowired
    public GatewayController(WebClient coreServiceClient) {
        this.coreServiceClient = coreServiceClient;
    }

    // --- CRUD Операции для Продуктов ---

    // Create - POST /api/products -> core-service: POST /products
    @PostMapping("/products")
    public Mono<ResponseEntity<Product>> createProduct(@RequestBody Product product) {
        System.out.println("Gateway: Received POST /api/products, forwarding to core-service...");
        return coreServiceClient.post() // Делаем POST запрос
                .uri("/products")       // к эндпоинту /products в core-service
                .bodyValue(product)     // Отправляем тело запроса (продукт)
                .retrieve()             // Получаем ответ
                // Преобразуем ответ в Mono<Product>
                .bodyToMono(Product.class)
                // Создаем ResponseEntity с полученным продуктом и статусом CREATED
                .map(createdProduct -> {
                    System.out.println("Gateway: Forwarded POST, received response from core-service.");
                    return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
                })
                // Обработка ошибок (например, если core-service недоступен или вернул ошибку)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    System.err.println("Gateway: Error forwarding POST: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(null)); // Возвращаем статус ошибки от core-service
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()); // Если ответ пустой (неожиданно)
    }

    // Read All - GET /api/products -> core-service: GET /products
    @GetMapping("/products")
    public Mono<ResponseEntity<Flux<Product>>> getAllProducts() {
        System.out.println("Gateway: Received GET /api/products, forwarding to core-service...");
        Flux<Product> productFlux = coreServiceClient.get() // Делаем GET запрос
                .uri("/products")
                .retrieve()
                // Преобразуем ответ в Flux<Product> (поток продуктов)
                .bodyToFlux(Product.class)
                .doOnComplete(() -> System.out.println("Gateway: Forwarded GET All, received response stream from core-service."))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    System.err.println("Gateway: Error forwarding GET All: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString());
                    return Flux.error(ex); // Просто пробрасываем ошибку дальше
                });
        // Возвращаем Flux внутри ResponseEntity. OK статус устанавливается по умолчанию.
        // Мы оборачиваем Flux в Mono<ResponseEntity>, чтобы контролировать статус ответа
        return Mono.just(ResponseEntity.ok(productFlux))
                .onErrorResume(WebClientResponseException.class, ex ->
                        Mono.just(ResponseEntity.status(ex.getStatusCode()).body(null)) // Возвращаем статус ошибки, если сам запрос к core-service провалился
                );
    }

    // Read One - GET /api/products/{id} -> core-service: GET /products/{id}
    @GetMapping("/products/{id}")
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable Long id) {
        System.out.println("Gateway: Received GET /api/products/" + id + ", forwarding to core-service...");
        return coreServiceClient.get()
                .uri("/products/{id}", id) // Используем параметр {id}
                .retrieve()
                // Обработка случая 404 Not Found от core-service
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty()) // Если 404, возвращаем пустой Mono
                .bodyToMono(Product.class)
                .map(product -> {
                    System.out.println("Gateway: Forwarded GET by ID, received response from core-service.");
                    return ResponseEntity.ok(product);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build()) // Если Mono пустое (был 404), возвращаем 404
                .onErrorResume(WebClientResponseException.class, ex -> {
                    System.err.println("Gateway: Error forwarding GET by ID: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(null)); // Возвращаем статус ошибки от core-service
                });
    }

    // Update - PUT /api/products/{id} -> core-service: PUT /products/{id}
    @PutMapping("/products/{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        System.out.println("Gateway: Received PUT /api/products/" + id + ", forwarding to core-service...");
        return coreServiceClient.put()
                .uri("/products/{id}", id)
                .bodyValue(productDetails)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .bodyToMono(Product.class)
                .map(updatedProduct -> {
                    System.out.println("Gateway: Forwarded PUT, received response from core-service.");
                    return ResponseEntity.ok(updatedProduct);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(WebClientResponseException.class, ex -> {
                    System.err.println("Gateway: Error forwarding PUT: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(null));
                });
    }

    // Delete - DELETE /api/products/{id} -> core-service: DELETE /products/{id}
    @DeleteMapping("/products/{id}")
    public Mono<ResponseEntity<Object>> deleteProduct(@PathVariable Long id) {
        System.out.println("Gateway: Received DELETE /api/products/" + id + ", forwarding to core-service...");
        return coreServiceClient.delete()
                .uri("/products/{id}", id)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .toBodilessEntity() // Нам не нужно тело ответа, только статус
                .map(responseEntity -> {
                    // Core-service возвращает 204 No Content при успехе
                    System.out.println("Gateway: Forwarded DELETE, received response from core-service (Status: " + responseEntity.getStatusCode() + ").");
                    return ResponseEntity.status(responseEntity.getStatusCode()).build(); // Используем статус от core-service (должен быть 204)
                })
                .defaultIfEmpty(ResponseEntity.notFound().build()) // Если был 404 от core-service
                .onErrorResume(WebClientResponseException.class, ex -> {
                    System.err.println("Gateway: Error forwarding DELETE: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString());
                    // Если core-service вернул другую ошибку (не 404, которую мы обработали выше)
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                });
    }
}
