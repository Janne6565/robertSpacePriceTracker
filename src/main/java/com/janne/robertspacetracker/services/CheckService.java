package com.janne.robertspacetracker.services;

import com.janne.robertspacetracker.model.Ship;
import com.janne.robertspacetracker.model.ShipSkus;
import com.janne.robertspacetracker.model.Sku;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckService {
    private final UserService userService;
    private final ShipService shipService;
    private final MailService mailService;

    private boolean isCurrentlyOnDiscount(Ship ship, Sku[] shipSkus) {
        return shipSkus.length > 1;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void checkUsers() {
        List<Ship> ships = shipService.getShips();
        List<ShipSkus> skusses = shipService.getShipSkusses();

        ships.forEach(ship -> {
            List<ShipSkus> shipSkusses = skusses.stream().filter(skus -> skus.id() == ship.id()).toList();
            if (shipSkusses.isEmpty()) {
                return;
            }
            if (isCurrentlyOnDiscount(ship, shipSkusses.getFirst().skus())) {
                int originalPrize = ship.msrp();
                int reducedPrize = Arrays.stream(shipSkusses.getFirst().skus()).mapToInt(Sku::price).min().orElse(originalPrize);
                log.info("Ship is on discount: {} (Original Prize: {}) discounted prize: {}", ship.name(), originalPrize, reducedPrize);
                userService.getAllUsers().stream().forEach(u -> {
                    if (u.getMinAmount() != null && u.getMinAmount() > reducedPrize) {
                        return;
                    }
                    if (u.getMaxAmount() != null && u.getMaxAmount() < originalPrize) {
                        return;
                    }
                    String message = String.format("Ship on discount which fits your search configurations <br>The %s is reduced from %d€ to %d€", ship.name(), originalPrize / 100, reducedPrize / 100);
                    String mailResponse = mailService.sendMail("alert@robertspacetracker.org", u.getEmail(), message, "Ship discount detected");
                    log.info("Sending mail ({}) to {} {}", mailResponse, u.getEmail(), message);
                });
            }
        });
    }
}
