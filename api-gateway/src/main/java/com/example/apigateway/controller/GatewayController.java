package com.example.apigateway.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/products") // Базовый путь для запросов через Gateway
public class GatewayController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String coreServiceUrl = "http://localhost:8081/products"; // URL core-service

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody String productJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(productJson, headers);
        return restTemplate.postForEntity(coreServiceUrl, requestEntity, String.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getProductById(@PathVariable Long id) {
        return restTemplate.getForEntity(coreServiceUrl + "/" + id, String.class);
    }

    @GetMapping
    public ResponseEntity<String> getAllProducts() {
        return restTemplate.getForEntity(coreServiceUrl, String.class);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable Long id, @RequestBody String productJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(productJson, headers);
        return restTemplate.exchange(coreServiceUrl + "/" + id, HttpMethod.PUT, requestEntity, String.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        restTemplate.delete(coreServiceUrl + "/" + id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
