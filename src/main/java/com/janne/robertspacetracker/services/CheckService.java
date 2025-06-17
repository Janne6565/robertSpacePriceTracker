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
    private final MessageService messageService;

    private boolean isCurrentlyOnDiscount(Ship ship, Sku[] shipSkus) {
        return shipSkus.length > 1;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void checkUsers() {
        List<Ship> ships = shipService.getShips();
        List<ShipSkus> skus = shipService.getShipSkus();

        ships.forEach(ship -> {
            List<ShipSkus> shipSkus = skus.stream().filter(sku -> sku.id() == ship.id()).toList();
            if (shipSkus.isEmpty()) {
                return;
            }
            if (isCurrentlyOnDiscount(ship, shipSkus.getFirst().skus())) {
                int originalPrize = ship.msrp();
                int reducedPrize = Arrays.stream(shipSkus.getFirst().skus()).mapToInt(Sku::price).min().orElse(originalPrize);
                log.info("Ship is on discount: {} (Original Prize: {}) discounted prize: {}", ship.name(), originalPrize, reducedPrize);
                userService.getAllUsers().stream().forEach(u -> {
                    if (u.getMinAmount() != null && u.getMinAmount() > reducedPrize) {
                        return;
                    }
                    if (u.getMaxAmount() != null && u.getMaxAmount() < originalPrize) {
                        return;
                    }
                    String message = messageService.buildNotificationMessage(u.getEmail(), ship, originalPrize, reducedPrize);
                    String mailResponse = mailService.sendMail("alert@syncup.cloud", u.getEmail(), message, "Ship discount detected");
                    log.info("Sending mail ({}) to {} {}", mailResponse, u.getEmail(), message);
                });
            }
        });
    }
}
