package com.osamah.games.security;

import com.osamah.games.user.User;
import com.osamah.games.user.UserRepository;
import com.osamah.games.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("osamah@example.com")
                .username("osamah")
                .password("Password1234")
                .role(Role.USER)
                .build();
        setField(testUser, "id", 1L);
    }

    @Test
    void loadUserByUsername_ShouldLoadUser_WhenEmailValid() {

        when(userRepository.findByUsernameOrEmail("osamah@example.com", "osamah@example.com")).thenReturn(
                Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername("osamah@example.com");

        assertThat(result.getUsername()).isEqualTo("osamah@example.com");
        assertThat(result.getPassword()).isEqualTo("Password1234");
        assertThat(result.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_USER");

        verify(userRepository).findByUsernameOrEmail("osamah@example.com", "osamah@example.com");
    }

    @Test
    void loadUserByUsername_ShouldLoadUser_WhenUsernameValid() {
        when(userRepository.findByUsernameOrEmail("osamah", "osamah")).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername("osamah");

        assertThat(result.getUsername()).isEqualTo("osamah@example.com");

        verify(userRepository).findByUsernameOrEmail("osamah", "osamah");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsernameOrEmail("unknown", "unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknown"));

        verify(userRepository).findByUsernameOrEmail("unknown", "unknown");
    }
}