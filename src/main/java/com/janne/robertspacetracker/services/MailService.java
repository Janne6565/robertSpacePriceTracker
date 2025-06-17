package com.janne.robertspacetracker.services;

import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class MailService {

    private final WebClient webClient;
    private final Bucket bucket;
    private final String apiKey;

    public MailService(WebClient.Builder webClientBuilder, Bucket bucket, @Value("${app.apis.email.url}") String mailUrl, @Value("${app.apis.email.key}") String apiKey) {
        this.webClient = webClientBuilder
            .baseUrl(mailUrl)
            .build();
        this.apiKey = apiKey;
        this.bucket = bucket;
    }

    public String sendMail(String author, String receiver, String message, String subject) {
        if (!bucket.tryConsume(1)) {
            log.info("Too many requests while trying to send {} {} {} {}", author, receiver, message, subject);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, you've run into a rate limit");
        }

        log.info("Sending mail hihi");
        if (true) {
            return "";
        }

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("from", author);
        formData.add("to", receiver);
        formData.add("text", message);
        formData.add("subject", subject);

        String response = webClient.post()
            .uri("/messages")
            .headers(headers -> headers.setBasicAuth("api", apiKey))
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(formData)
            .retrieve()
            .bodyToMono(String.class)
            .block();
        return response;
    }
}