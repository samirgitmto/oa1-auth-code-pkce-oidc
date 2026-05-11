package com.cred.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cred.dto.Product;
import com.cred.dto.ProductAdminResponse;
import com.cred.dto.ProductPublicResponse;
import com.cred.product.ProductStore;

@RestController
@RequestMapping("/products/v1")
public class ProductControllerOld {

    private final ProductStore store;

    public ProductControllerOld(ProductStore store) {
        this.store = store;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_read')")
    public List<ProductPublicResponse> list() {
        return store.findAll().stream()
                .map(p -> new ProductPublicResponse(p.id(), p.name(), p.price()))
                .toList();
    }

    /**
     * Same URL for everyone with read scope; response shape differs for admins (extra fields).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_read')")
    public Object getById(@PathVariable String id, Authentication authentication) {
        Product p = store.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (isAdmin(authentication)) {
            return new ProductAdminResponse(p.id(), p.name(), p.price(), p.costPrice(), p.internalNote());
        }
        return new ProductPublicResponse(p.id(), p.name(), p.price());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductPublicResponse create(@RequestBody CreateProductRequest body) {
        String id = body.id() != null && !body.id().isBlank() ? body.id() : java.util.UUID.randomUUID().toString();
        Product saved = store.save(new Product(id, body.name(), body.price(), body.costPrice(), body.internalNote()));
        return new ProductPublicResponse(saved.id(), saved.name(), saved.price());
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    public record CreateProductRequest(
            String id,
            String name,
            double price,
            double costPrice,
            String internalNote
    ) {}
}
