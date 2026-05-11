package com.osamah.games.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "otps")
@Getter
@Setter
@NoArgsConstructor
public class Otp {
    //NOTE: For a true production environment, OTPs should be hashed
    //(e.g., using SHA-256 or BCrypt) before being stored in the database,
    //just like passwords, to prevent exploitation in the event of a data leak.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private Instant expiryDate;

    @Builder
    public Otp(String email, String code, Instant expiryDate) {
        this.email = email;
        this.code = code;
        this.expiryDate = expiryDate;
    }
}