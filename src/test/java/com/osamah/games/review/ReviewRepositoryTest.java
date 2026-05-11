package com.osamah.games.review;

import com.osamah.games.config.JpaConfig;
import com.osamah.games.game.Game;
import com.osamah.games.user.User;
import com.osamah.games.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestEntityManager entityManager;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("osamah")
                .email("test@mail.com")
                .password("pass")
                .role(Role.USER)
                .build();
        entityManager.persistAndFlush(testUser);

        Game testGame = Game.builder()
                .slug("test-game-1")
                .title("Test Game 1")
                .build();
        entityManager.persistAndFlush(testGame);


        Game testGame2 = Game.builder()
                .slug("test-game-2")
                .title("Test Game 2")
                .build();
        entityManager.persistAndFlush(testGame2);


        Review review1 = Review.builder()
                .user(testUser)
                .game(testGame)
                .rating(10)
                .content("Review 1")
                .build();


        Review review2 = Review.builder()
                .user(testUser)
                .game(testGame2)
                .rating(8)
                .content("Review 2")
                .build();

        entityManager.persist(review1);
        entityManager.persist(review2);
        entityManager.flush();
    }


    @Test
    void deleteByUserId_ShouldDeleteAllReviewsForGivenUser() {

        assertThat(reviewRepository.findAll()).hasSize(2);

        reviewRepository.deleteByUserId(testUser.getId());

        assertThat(reviewRepository.findAll()).isEmpty();
    }
}