package com.example.coreservice.model;

import java.util.Objects; // Добавь импорт для equals/hashCode

// Убираем аннотации Lombok (@Data, @NoArgsConstructor, @AllArgsConstructor)

public class Product {
    private Long id;
    private String name;
    private double price;

    // 1. Конструктор без аргументов (нужен для некоторых фреймворков)
    public Product() {
    }

    // 2. Конструктор со всеми аргументами (может быть полезен)
    public Product(Long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // 3. Геттеры (методы для получения значений полей)
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    // 4. Сеттеры (методы для установки значений полей) - ВОТ ОНИ!
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // 5. equals() и hashCode() (важны для сравнения объектов, например, в коллекциях)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Double.compare(product.price, price) == 0 &&
                Objects.equals(id, product.id) &&
                Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price);
    }

    // 6. toString() (полезен для вывода информации об объекте в лог)
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}