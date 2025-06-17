package com.janne.robertspacetracker.services;

import com.janne.robertspacetracker.entities.UserEntity;
import com.janne.robertspacetracker.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final MailService mailService;
    @Value("${app.frontendbaseurl}")
    private String frontendBaseUrl;

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public void sendLoginRequest(String email) {
        if (!userRepository.existsById(email)) {
            return;
        }
        String jwt = jwtService.generateToken(Map.of("email", email));
        String link = frontendBaseUrl + "/login/" + jwt;
        mailService.sendMail("login@roberSpaceTracker.org", email, "Hi, please click this link to log into the Robert Space Tracker " + link, "Robert space Ship tracker login");
    }

    public void updateUserConfig(UserEntity userEntity) {
        if (userEntity.getMaxAmount() != null) {
            setMaxAmount(userEntity.getEmail(), userEntity.getMaxAmount());
        }
        if (userEntity.getMinAmount() != null) {
            setMinAmount(userEntity.getEmail(), userEntity.getMinAmount());
        }
    }

    public UserEntity createUserEntity(String email) {
        String mailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!email.matches(mailRegex)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        UserEntity userEntity = UserEntity.builder()
            .email(email)
            .build();
        if (userRepository.existsById(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with email " + email + " already exists");
        }
        log.info("User created: {}", userEntity);
        return userRepository.save(userEntity);
    }

    public UserEntity getUserEntity(String email) {
        UserEntity userEntity = userRepository.findById(email).orElse(null);
        if (userEntity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with email " + email + " does not exist");
        }
        return userEntity;
    }

    public void setMinAmount(String email, Long minAmount) {
        UserEntity userEntity = getUserEntity(email);
        userEntity.setMinAmount(minAmount);
        userRepository.save(userEntity);
    }

    public void setMaxAmount(String email, Long maxAmount) {
        UserEntity userEntity = getUserEntity(email);
        userEntity.setMaxAmount(maxAmount);
        userRepository.save(userEntity);
    }

    public void deleteUser(String authorizedEmail) {
        userRepository.deleteById(authorizedEmail);
    }
}
