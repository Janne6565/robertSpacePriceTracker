package com.janne.robertspacetracker.model;

public record Sku(
    int id,
    String title,
    boolean available,
    int price,
    String body,
    boolean unlimitedStock,
    int availableStock
) {}
