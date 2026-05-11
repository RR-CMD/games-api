package com.osamah.games.usergame;

import com.osamah.games.config.JpaConfig;
import com.osamah.games.game.Game;
import com.osamah.games.user.User;
import com.osamah.games.user.enums.Role;
import com.osamah.games.usergame.enums.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class UserGameRepositoryTest {

    @Autowired
    private UserGameRepository userGameRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("osamah")
                .email("test@osamah.gamesa")
                .password("test123!")
                .role(Role.USER)
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        Game g1 = Game.builder()
                .slug("elden-ring")
                .title("Elden Ring")
                .genres(List.of("Action", "RPG"))
                .releaseDate(LocalDate.of(2022, 2, 2))
                .build();
        g1 = entityManager.persistAndFlush(g1);

        Game g2 = Game.builder()
                .slug("stardew-valley")
                .title("Stardew Valley")
                .genres(List.of("Simulation", "Indie"))
                .releaseDate(LocalDate.of(2010, 6, 23))
                .build();
        g2 = entityManager.persistAndFlush(g2);

        UserGame ug1 = UserGame.builder()
                .user(testUser)
                .game(g1)
                .gameStatus(GameStatus.PLAYING)
                .rating(9)
                .build();
        entityManager.persistAndFlush(ug1);

        UserGame ug2 = UserGame.builder()
                .user(testUser)
                .game(g2)
                .gameStatus(GameStatus.COMPLETED)
                .rating(10)
                .build();
        entityManager.persistAndFlush(ug2);
    }

    @Test
    void statsByUserId_ShouldReturnCorrectCountsPerStatus() {
        List<Object[]> stats = userGameRepository.statsByUserId(testUser.getId());

        assertThat(stats).hasSize(2);

        java.util.Map<GameStatus, Long> statsMap = stats.stream()
                .collect(java.util.stream.Collectors.toMap(row -> (GameStatus) row[0], row -> (Long) row[1]));

        assertThat(statsMap.get(GameStatus.PLAYING)).isEqualTo(1L);
        assertThat(statsMap.get(GameStatus.COMPLETED)).isEqualTo(1L);
    }


    @Test
    void searchUserGames_ShouldFilterByGenre() {
        Page<UserGame> result = userGameRepository.searchUserGames(testUser.getId(), null, "Simulation", null, null,
                null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()
                .getFirst()
                .getGame()
                .getTitle()).isEqualTo("Stardew Valley");
    }

    @Test
    void searchUserGames_ShouldReturnAllWhenNullFilters() {
        Page<UserGame> result = userGameRepository.searchUserGames(testUser.getId(), null, null, null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}