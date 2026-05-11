package com.cred.dto;

public record CreateProductRequest(
        String id,
        String name,
        double price,
        double costPrice,
        String internalNote
) {}
