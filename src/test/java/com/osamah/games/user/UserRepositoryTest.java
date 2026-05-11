package com.osamah.games.user;

import com.osamah.games.config.JpaConfig;
import com.osamah.games.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(new User("osamah", "os1@mail.com", "pass", Role.ADMIN));
        userRepository.save(new User("osera", "osera@mail.com", "pass", Role.USER));
        userRepository.save(new User("john", "john@mail.com", "pass", Role.USER));

    }

    @Test
    void searchUsers_ShouldReturnUsersMatchingUsernamePartially() {

        Page<User> result = userRepository.searchUsers("os", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(User::getUsername)
                .containsExactlyInAnyOrder("osamah", "osera");
    }

    @Test
    void searchUsers_ShouldReturnAll_WhenUsernameIsNull() {

        Page<User> result = userRepository.searchUsers(null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void searchUsers_ShouldReturnNothing_WhenUsernameDoesNotExist() {
        Page<User> result = userRepository.searchUsers("ghost", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void shouldApplyPaginationCorrectly() {

        Page<User> page1 = userRepository.searchUsers("o", PageRequest.of(0, 2));
        Page<User> page2 = userRepository.searchUsers("o", PageRequest.of(1, 2));

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page1.getTotalElements()).isEqualTo(3);
        assertThat(page1.getTotalPages()).isEqualTo(2);

        assertThat(page1.getContent()).doesNotContainAnyElementsOf(page2.getContent());
    }
}