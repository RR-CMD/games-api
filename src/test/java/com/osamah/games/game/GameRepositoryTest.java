package com.osamah.games.game;

import com.osamah.games.config.JpaConfig;
import com.osamah.games.user.User;
import com.osamah.games.user.enums.Role;
import com.osamah.games.usergame.UserGame;
import com.osamah.games.usergame.enums.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class GameRepositoryTest {

    Game g1;
    Game g2;
    Game g3;
    Game g4;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {

        g1 = new Game("slug1", "The Legend of Zelda: Breath of the Wild", "desc", LocalDate.of(2017, 3, 3), "url", 90,
                List.of("Action", "Adventure", "RPG"), List.of("Nintendo Switch", "Wii U"));
        g1.setAverageScore(7.0);

        g2 = new Game("slug2", "Super Mario Odyssey", "desc", LocalDate.of(2017, 10, 27), "url", 90,
                List.of("Platformer", "Action"), List.of("Nintendo Switch"));
        g2.setAverageScore(6.0);

        g3 = new Game("slug3", "The Witcher 3: Wild Hunt", "desc", LocalDate.of(2015, 5, 19), "url", 90,
                List.of("RPG", "Adventure"), List.of("PC", "PlayStation 4", "Xbox One"));
        g3.setAverageScore(5.0);

        g4 = new Game("slug4", "Cyberpunk 2077", "desc", LocalDate.of(2020, 12, 10), "url", 90, List.of("RPG"),
                List.of("PC", "PlayStation 4", "PlayStation 5"));
        g4.setAverageScore(4.0);

        g1 = entityManager.persistAndFlush(g1);
        g2 = entityManager.persistAndFlush(g2);
        g3 = entityManager.persistAndFlush(g3);
        g4 = entityManager.persistAndFlush(g4);

    }

    //UPDATE ALL AVERAGE SCORES QUERY TEST
    @Test
    void updateAllAverageScores_ShouldCalculateAndSaveAveragesProperly() {

        User user1 = entityManager.persist(new User("osamah", "os1@mail.com", "pass", Role.USER));
        User user2 = entityManager.persist(new User("john", "john@mail.com", "pass", Role.USER));

        entityManager.persist(new UserGame(user1, g1, GameStatus.COMPLETED, 8));
        entityManager.persist(new UserGame(user2, g1, GameStatus.COMPLETED, 6));
        entityManager.persist(new UserGame(user1, g2, GameStatus.PLANNED, null));
        entityManager.persist(new UserGame(user2, g2, GameStatus.COMPLETED, 10));

        entityManager.flush();

        gameRepository.updateAllAverageScores();

        entityManager.clear();

        Game fetchedG1 = gameRepository.findById(g1.getId())
                .orElseThrow();
        Game fetchedG2 = gameRepository.findById(g2.getId())
                .orElseThrow();

        assertThat(fetchedG1.getAverageScore()).isEqualTo(7.0);
        assertThat(fetchedG2.getAverageScore()).isEqualTo(10.0);
    }

    //SEARCH QUERY TESTS
    @Test
    void searchGames_shouldReturnAllGames_WhenAllFiltersAreNull() {
        Page<Game> result = gameRepository.searchGames(null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    void searchGames_shouldFilterByTitle_CaseInsensitiveAndPartialMatch() {
        Page<Game> result = gameRepository.searchGames("wItC", null, null, null, null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()
                .getFirst()
                .getTitle()).isEqualTo("The Witcher 3: Wild Hunt");
    }

    @Test
    void searchGames_shouldFilterByGenres_MustHaveAllRequestedGenres() {
        List<String> searchGenres = List.of("Adventure", "RPG");
        long genreCount = searchGenres.size();

        Page<Game> result = gameRepository.searchGames(null, searchGenres, null, null, null, null, genreCount,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Game::getTitle)
                .containsExactlyInAnyOrder("The Legend of Zelda: Breath of the Wild", "The Witcher 3: Wild Hunt");
    }

    @Test
    void searchGames_shouldFilterByPlatforms_MustHaveAnyRequestPlatform() {
        List<String> searchPlatforms = List.of("PlayStation 4", "PC", "Xbox One");

        Page<Game> result = gameRepository.searchGames(null, null, searchPlatforms, null, null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Game::getTitle)
                .containsExactlyInAnyOrder("The Witcher 3: Wild Hunt", "Cyberpunk 2077");
    }

    @Test
    void searchGames_shouldFilterByReleaseYearRange() {
        Page<Game> result = gameRepository.searchGames(null, null, null, 2016, 2018, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Game::getTitle)
                .containsExactlyInAnyOrder("The Legend of Zelda: Breath of the Wild", "Super Mario Odyssey");
    }

    @Test
    void searchGames_shouldFilterByMinimumAverageScore() {
        Page<Game> result = gameRepository.searchGames(null, null, null, null, null, 6.0, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Game::getTitle)
                .containsExactlyInAnyOrder("The Legend of Zelda: Breath of the Wild", "Super Mario Odyssey");
    }

    @Test
    void searchGames_shouldFilterByMultipleCriteriaCombined() {

        List<String> searchGenres = List.of("Action");
        List<String> searchPlatforms = List.of("Nintendo Switch");

        Page<Game> result = gameRepository.searchGames(null, searchGenres, searchPlatforms, 2016, null, 4.5, 1L,
                PageRequest.of(0, 10));


        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void searchGames_shouldSortGamesByAverageScoreDescending() {
        PageRequest sortedPage = PageRequest.of(0, 10, Sort.by("averageScore")
                .descending());

        Page<Game> result = gameRepository.searchGames(null, null, null, null, null, null, null, sortedPage);

        List<Game> content = result.getContent();


        assertThat(content).hasSize(4);
        assertThat(content.getFirst()
                .getAverageScore()).isEqualTo(7.0);
        assertThat(content.getLast()
                .getAverageScore()).isEqualTo(4.0);


        assertThat(content).isSortedAccordingTo(Comparator.comparing(Game::getAverageScore)
                .reversed());
    }

    @Test
    void searchGames_shouldApplyPaginationCorrectly() {

        Page<Game> page1 = gameRepository.searchGames(null, null, null, null, null, null, null,
                PageRequest.of(0, 2, Sort.by("title")));

        Page<Game> page2 = gameRepository.searchGames(null, null, null, null, null, null, null,
                PageRequest.of(1, 2, Sort.by("title")));

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(4);
        assertThat(page1.getTotalPages()).isEqualTo(2);

        assertThat(page1.getContent()).doesNotContainAnyElementsOf(page2.getContent());
    }


}