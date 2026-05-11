package com.cred.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cred.dto.CreateProductRequest;
import com.cred.dto.ProductPublicResponse;
import com.cred.service.ProductService;

/**
 * HTTP layer delegates to {@link ProductService}. Authorization is enforced at <strong>methods</strong>
 * ({@code @PreAuthorize}) both here (entry points) and again on {@link com.cred.service.ProductServiceImpl}
 * (service layer).
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_read')")
    public List<ProductPublicResponse> list() {
        return productService.listProducts();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_read')")
    public Object getById(@PathVariable String id, Authentication authentication) {
        return productService.getProductById(id, authentication);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductPublicResponse create(@RequestBody CreateProductRequest body) {
        return productService.createProduct(body);
    }
}
