package com.cred.dto;

public record Product(
        String id,
        String name,
        double price,
        /** Admin-only: internal cost / margin input */
        double costPrice,
        String internalNote
) {}
