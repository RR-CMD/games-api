package com.osamah.games.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails dummyUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey",
                "289f0b7d421fb0e40894904bf367803e3988f38b31657dea550e2fd0b5e323ba");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 604800000L);

        dummyUser = new User("test@mail.com", "pass", Collections.emptyList());
    }

    @Test
    void generateToken_ShouldReturnValidString() {
        String token = jwtService.generateToken(dummyUser);
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(dummyUser);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("test@mail.com");
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForCorrectUser() {
        String token = jwtService.generateToken(dummyUser);
        assertThat(jwtService.isTokenValid(token, dummyUser)).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForWrongUser() {
        String token = jwtService.generateToken(dummyUser);
        UserDetails wrongUser = new User("wrong@mail.com", "pass", Collections.emptyList());
        assertThat(jwtService.isTokenValid(token, wrongUser)).isFalse();
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsExpired() {

        String expiredToken = Jwts.builder()
                .setSubject("test@mail.com")
                .setIssuedAt(new Date(3600000 - 1000))
                .setExpiration(new Date(3600000))
                .signWith(getTestKey(), SignatureAlgorithm.HS256)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(expiredToken));
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsMalformed() {
        String fakeToken = "this.is.not.a.real.token";

        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername(fakeToken));
    }


    private Key getTestKey() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(
                "289f0b7d421fb0e40894904bf367803e3988f38b31657dea550e2fd0b5e323ba");
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }
}