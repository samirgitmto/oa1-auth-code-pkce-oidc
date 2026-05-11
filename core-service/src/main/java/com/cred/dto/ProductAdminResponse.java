package com.cred.dto;

public record ProductAdminResponse(
        String id,
        String name,
        double price,
        double costPrice,
        String internalNote
) {}
