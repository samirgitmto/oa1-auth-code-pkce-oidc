package com.cred.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.cred.dto.CreateProductRequest;
import com.cred.dto.ProductPublicResponse;

public interface ProductService {

    List<ProductPublicResponse> listProducts();

    /**
     * Same path for all callers with read scope; returns public or admin DTO based on role.
     */
    Object getProductById(String id, Authentication authentication);

    ProductPublicResponse createProduct(CreateProductRequest body);
}
