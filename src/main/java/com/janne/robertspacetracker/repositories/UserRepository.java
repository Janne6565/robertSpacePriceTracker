package com.janne.robertspacetracker.repositories;

import com.janne.robertspacetracker.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {
}
