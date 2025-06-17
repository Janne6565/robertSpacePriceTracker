package com.janne.robertspacetracker.model;

import java.util.List;

public record Ship(
    int id,
    String name,
    Medias medias,
    Manufacturer manufacturer,
    String focus,
    String type,
    String flyableStatus,
    boolean owned,
    int msrp,
    String link,
    List<Sku> skus
) {}

