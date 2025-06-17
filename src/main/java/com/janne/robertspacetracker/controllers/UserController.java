package com.janne.robertspacetracker.controllers;

import com.janne.robertspacetracker.entities.UserEntity;
import com.janne.robertspacetracker.services.JwtService;
import com.janne.robertspacetracker.services.UserService;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/user")
    public ResponseEntity<UserEntity> getUser(@RequestParam("accessToken") String accessToken) {
        try {
            if (accessToken == null) {
                throw new RuntimeException();
            }
            String user = jwtService.getJwtOwner(accessToken);
            if (user == null) {
                throw new RuntimeException();
            }
            return ResponseEntity.ok(userService.getUserEntity(user));
        } catch (Exception ignored) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/cookie")
    public ResponseEntity<String> setCookie(@RequestParam("accessToken") String jwt) {
        ResponseCookie responseCookie = ResponseCookie.from("accessToken", jwt).httpOnly(false).secure(false).path("/").maxAge(jwtService.getJwtValidityDuration() / 1000).build();

        return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body("Success");
    }

    @PostMapping("/login/{email}")
    public ResponseEntity<String> sendLoginRequest(@PathVariable("email") String email) {
        userService.sendLoginRequest(email);
        return ResponseEntity.ok("Mail send");
    }

    @PostMapping("/user/{email}")
    public ResponseEntity<UserEntity> createUser(@PathVariable("email") String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUserEntity(email));
    }

    @PatchMapping("/user/config")
    public ResponseEntity<UserEntity> updateUser(@RequestBody UserEntity userEntity, @RequestParam("accessToken") String accessToken) {
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String authorizedEmail = jwtService.getJwtOwner(accessToken);
        if (authorizedEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        userEntity.setEmail(authorizedEmail);
        userService.updateUserConfig(userEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.getUserEntity(authorizedEmail));
    }

    @DeleteMapping("/user")
    public ResponseEntity<UserEntity> deleteUser(@RequestParam("accessToken") String accessToken) {
        String authorizedEmail = jwtService.getJwtOwner(accessToken);
        if (authorizedEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (userService.getUserEntity(authorizedEmail) != null) {
            userService.deleteUser(authorizedEmail);
        }
        return ResponseEntity.ok(userService.getUserEntity(authorizedEmail));
    }

}
