package com.cred.product;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.cred.dto.Product;

@Component
public class ProductStore {

    private final Map<String, Product> products = new ConcurrentHashMap<>();

    public ProductStore() {
        products.put("p1", new Product("p1", "Sample Product", 99.99, 45.00, "Internal: supplier contract #X"));
        products.put("p2", new Product("p2", "Another Item", 19.50, 8.00, "Low stock alert"));
    }

    public Collection<Product> findAll() {
        return products.values();
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    public Product save(Product product) {
        products.put(product.id(), product);
        return product;
    }
}
