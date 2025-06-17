package com.janne.robertspacetracker.services;

import com.janne.robertspacetracker.model.Ship;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    public String buildNotificationMessage(String receiver, Ship ship, int originalPrice, int reducedPrice) {
        return String.format("<img src=\"%s\"/><br/>The %s is reduced from %d€ to %d€", ship.medias().slideShow(), ship.name(), originalPrice / 100, reducedPrice / 100);
    }
}
