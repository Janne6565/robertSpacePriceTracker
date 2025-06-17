package com.janne.robertspacetracker.controllers;

import com.janne.robertspacetracker.model.Ship;
import com.janne.robertspacetracker.model.ShipSkus;
import com.janne.robertspacetracker.services.ShipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShipController {

    private final ShipService shipService;

    @GetMapping
    public ResponseEntity<String> statusCheck() {
        return ResponseEntity.ok("Running");
    }

    @GetMapping("/ships")
    public ResponseEntity<Ship[]> getShips() {
        return ResponseEntity.ok(shipService.getShips().toArray(new Ship[0]));
    }

    @GetMapping("/skus")
    public ResponseEntity<ShipSkus[]> getSkus() {
        return ResponseEntity.ok(shipService.getShipSkus().toArray(new ShipSkus[0]));
    }

}