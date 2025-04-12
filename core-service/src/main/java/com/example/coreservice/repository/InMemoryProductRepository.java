package com.example.coreservice.repository;

import com.example.coreservice.model.Product ;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository // Помечаем класс как компонент Spring для хранения данных
public class InMemoryProductRepository implements ProductRepository {

    // Потокобезопасная мапа для хранения продуктов <ID, Product>
    private final Map<Long, Product> productStore = new ConcurrentHashMap<>();
    // Потокобезопасный счетчик для генерации ID
    private final AtomicLong idCounter = new AtomicLong(0);

    @Override
    public Product save(Product product) {
        // Генерируем новый ID
        long newId = idCounter.incrementAndGet();
        product.setId(newId);
        productStore.put(newId, product);
        System.out.println("Saved product: " + product); // Логирование для отладки
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(productStore.get(id));
    }

    @Override
    public List<Product> findAll() {
        return productStore.values().stream().collect(Collectors.toList());
    }

    @Override
    public Optional<Product> update(Long id, Product product) {
        // Проверяем, существует ли продукт с таким ID
        return Optional.ofNullable(productStore.computeIfPresent(id, (key, existingProduct) -> {
            // Обновляем только если продукт существует
            product.setId(id); // Устанавливаем ID обновляемому объекту
            System.out.println("Updating product: " + product); // Логирование
            return product; // Возвращаем обновленный продукт
        }));
    }

    @Override
    public boolean deleteById(Long id) {
        // Удаляем продукт и возвращаем true, если он был удален, иначе false
        boolean removed = productStore.remove(id) != null;
        if (removed) {
            System.out.println("Deleted product with id: " + id); // Логирование
        }
        return removed;
    }
}
