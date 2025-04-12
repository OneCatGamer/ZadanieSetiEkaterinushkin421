package com.example.coreservice.controller;

import com.example.coreservice.model.Product ;
import com.example.coreservice.repository.ProductRepository ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController // Помечаем класс как REST контроллер
@RequestMapping("/products") // Базовый путь для всех эндпоинтов в этом контроллере
public class ProductController {

    private final ProductRepository productRepository;

    // Внедряем зависимость ProductRepository через конструктор
    @Autowired
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Create (Создание) - POST /products
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        // @RequestBody говорит Spring взять тело запроса и преобразовать его в объект Product
        Product savedProduct = productRepository.save(product);
        // Возвращаем созданный продукт и статус 201 Created
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    // Read (Получение всех) - GET /products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        // Возвращаем список продуктов и статус 200 OK
        return ResponseEntity.ok(products);
    }

    // Read (Получение одного по ID) - GET /products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        // @PathVariable говорит Spring взять значение {id} из пути запроса
        Optional<Product> productOptional = productRepository.findById(id);
        // Если продукт найден, возвращаем его и статус 200 OK
        // Если не найден, возвращаем статус 404 Not Found
        return productOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update (Обновление) - PUT /products/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Optional<Product> updatedProductOptional = productRepository.update(id, productDetails);
        // Если продукт обновлен, возвращаем его и статус 200 OK
        // Если продукт с таким ID не найден для обновления, возвращаем 404 Not Found
        return updatedProductOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete (Удаление) - DELETE /products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productRepository.deleteById(id);
        // Если продукт удален, возвращаем статус 204 No Content (успешно, но нет тела ответа)
        // Если продукт не найден для удаления, возвращаем 404 Not Found
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
