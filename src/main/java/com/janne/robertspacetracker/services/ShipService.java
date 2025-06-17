package com.janne.robertspacetracker.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.janne.robertspacetracker.model.Ship;
import com.janne.robertspacetracker.model.ShipSkus;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ShipService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    @Getter
    private List<Ship> ships;
    @Getter
    private List<ShipSkus> shipSkus;

    public ShipService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
            .baseUrl("https://robertsspaceindustries.com")
            .build();
        this.objectMapper = objectMapper;
    }

    private String getAuthToken() {
        return webClient.post()
            .uri("/api/account/v2/setAuthToken")
            .retrieve()
            .bodyToMono(AuthResponse.class)
            .map(AuthResponse::getData)
            .block(); // Blocking call to keep it simple and match the Python version
    }


    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void fetchStuff() {
        this.shipSkus = fetchSkus();
        this.ships = fetchShips();
    }

    public List<ShipSkus> fetchSkus() {
        String authToken = getAuthToken();
        String graphqlQuery = """
                query filterShips($fromId: Int, $toId: Int, $fromFilters: [FilterConstraintValues], $toFilters: [FilterConstraintValues]) {
                  from(to: $toId, filters: $fromFilters) {
                    ships {
                      id
                    }
                  }
                  to(from: $fromId, filters: $toFilters) {
                    featured {
                      reason
                      style
                      tagLabel
                      tagStyle
                      footNotes
                      shipId
                    }
                    ships {
                      id
                      skus {
                        id
                        price
                        upgradePrice
                        unlimitedStock
                        showStock
                        available
                        availableStock
                      }
                    }
                  }
                }
            """;

        Map<String, Object> payload = Map.of(
            "operationName", "filterShips",
            "variables", Map.of(
                "fromFilters", List.of(),
                "toFilters", List.of()
            ),
            "query", graphqlQuery
        );

        JsonNode rootNode = webClient.post()
            .uri("/pledge-store/api/upgrade/graphql")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.COOKIE, "Rsi-Account-Auth=" + authToken)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();


        if (rootNode.get("data") == null || rootNode.get("data").get("to") == null || rootNode.get("data").get("to").get("ships") == null) {
            log.info("{}", rootNode.get("data"));
            return List.of(); // empty list fallback
        }

        JsonNode shipsNode = rootNode.get("data").get("to").get("ships");
        try {
            return objectMapper.readerForListOf(ShipSkus.class).readValue(shipsNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ships list", e);
        }
    }

    public List<Ship> fetchShips() {
        String authToken = getAuthToken();
        String graphqlQuery = """
                query initShipUpgrade {
                  ships {
                    id
                    name
                    medias {
                      productThumbMediumAndSmall
                      slideShow
                    }
                    manufacturer {
                      id
                      name
                    }
                    focus
                    type
                    flyableStatus
                    owned
                    msrp
                    link
                    skus {
                      id
                      title
                      available
                      price
                      body
                      unlimitedStock
                      availableStock
                    }
                  }
                  manufacturers {
                    id
                    name
                  }
                  app {
                    version
                    env
                    cookieName
                    sentryDSN
                    pricing {
                      currencyCode
                      currencySymbol
                      exchangeRate
                      exponent
                      taxRate
                      isTaxInclusive
                    }
                    mode
                    isAnonymous
                    buyback {
                      credit
                    }
                  }
                }
            """;

        Map<String, Object> payload = Map.of(
            "operationName", "initShipUpgrade",
            "variables", new HashMap<>(),
            "query", graphqlQuery
        );

        JsonNode rootNode = webClient.post()
            .uri("/pledge-store/api/upgrade/graphql")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.COOKIE, "Rsi-Account-Auth=" + authToken)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        if (rootNode == null || rootNode.get("data") == null || rootNode.get("data").get("ships") == null) {
            return List.of(); // empty list fallback
        }

        JsonNode shipsNode = rootNode.get("data").get("ships");
        try {
            return objectMapper.readerForListOf(Ship.class).readValue(shipsNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ships list", e);
        }
    }


    @Setter
    @Getter
    public static class AuthResponse {
        private String data;
    }
}
