package com.janne.robertspacetracker.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Builder
public class UserEntity {
    @Id
    private String email;
    @Column
    private Long minAmount;
    @Column
    private Long maxAmount;
}
