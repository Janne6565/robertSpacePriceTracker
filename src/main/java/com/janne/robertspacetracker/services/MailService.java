package com.janne.robertspacetracker.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class MailService {

    private final WebClient webClient;

    public MailService(WebClient.Builder webClientBuilder, @Value("${app.apis.email}") String mailUrl) {
        this.webClient = webClientBuilder
            .baseUrl(mailUrl)
            .build();
    }

    public String sendMail(String author, String receiver, String message, String subject) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("author", author);
        formData.add("receiver", receiver);
        formData.add("message", message);
        formData.add("subject", subject);

        String response = webClient.post()
            .uri("/send.php")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(formData)
            .retrieve()
            .bodyToMono(String.class)
            .block();
        return response;
    }
}