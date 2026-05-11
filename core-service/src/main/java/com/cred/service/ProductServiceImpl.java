package com.cred.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cred.dto.CreateProductRequest;
import com.cred.dto.Product;
import com.cred.dto.ProductAdminResponse;
import com.cred.dto.ProductPublicResponse;
import com.cred.product.ProductStore;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductStore store;

    public ProductServiceImpl(ProductStore store) {
        this.store = store;
    }

    /**
     * Service-layer @PreAuthorize mirrors the controller: defense in depth + visible rules next to business logic.
     */
    @Override
    @PreAuthorize("hasAuthority('SCOPE_read')")
    public List<ProductPublicResponse> listProducts() {
        return store.findAll().stream()
                .map(p -> new ProductPublicResponse(p.id(), p.name(), p.price()))
                .toList();
    }

    /**
     * Public vs admin payload is decided here by role; SCOPE_read is required to enter.
     */
    @Override
    @PreAuthorize("hasAuthority('SCOPE_read')")
    public Object getProductById(String id, Authentication authentication) {
        Product p = store.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (isAdmin(authentication)) {
            return new ProductAdminResponse(p.id(), p.name(), p.price(), p.costPrice(), p.internalNote());
        }
        return new ProductPublicResponse(p.id(), p.name(), p.price());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ProductPublicResponse createProduct(CreateProductRequest body) {
        String id = body.id() != null && !body.id().isBlank() ? body.id() : UUID.randomUUID().toString();
        Product saved = store.save(
                new Product(id, body.name(), body.price(), body.costPrice(), body.internalNote()));
        return new ProductPublicResponse(saved.id(), saved.name(), saved.price());
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
