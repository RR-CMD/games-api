package com.osamah.games.config;

import com.osamah.games.game.GameRepository;
import com.osamah.games.game.GameService;
import com.osamah.games.game.dto.GameResponse;
import com.osamah.games.review.ReviewService;
import com.osamah.games.review.dto.ReviewCreateRequest;
import com.osamah.games.user.User;
import com.osamah.games.user.UserRepository;
import com.osamah.games.user.enums.Role;
import com.osamah.games.usergame.UserGameService;
import com.osamah.games.usergame.dto.UserGameCreateRequest;
import com.osamah.games.usergame.enums.GameStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseDataFiller implements CommandLineRunner {

    // NOTE: This is used for fast setup and testing of the application.
    // It will take the app a little longer to initialize on the first startup
    // as it fetches data for 10 games from the RAWG API.
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PasswordEncoder passwordEncoder;

    private final GameService gameService;
    private final UserGameService userGameService;
    private final ReviewService reviewService;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0 && gameRepository.count() == 0) {
            log.info("Database is empty. Fetching real games from RAWG API and creating default data...");
            fillData();
            log.info("Database seeding completed successfully!");
        }
    }

    private void fillData() {
        String defaultPassword = passwordEncoder.encode("Password1234");

        userRepository.save(new User("admin", "admin@games.com", defaultPassword, Role.ADMIN));
        userRepository.save(new User("osamah", "osamah@games.com", defaultPassword, Role.USER));
        userRepository.save(new User("john", "john@games.com", defaultPassword, Role.USER));
        userRepository.save(new User("sarah", "sarah@games.com", defaultPassword, Role.USER));
        userRepository.save(new User("mike", "mike@games.com", defaultPassword, Role.USER));
        userRepository.save(new User("emma", "emma@games.com", defaultPassword, Role.USER));


        String[] defaultSlugs = {"the-witcher-3-wild-hunt", "hollow-knight", "bloodborne", "cyberpunk-2077",
                "minecraft", "grand-theft-auto-v", "stardew-valley", "portal-2", "half-life",
                "resident" + "-evil-9" + "-requiem"};

        List<Long> gameIds = new ArrayList<>();

        for (String slug : defaultSlugs) {
            try {

                GameResponse response = gameService.createGame(slug);
                gameIds.add(response.id());
                log.info("Successfully fetched and saved: {}", response.title());
            } catch (Exception e) {
                log.error("Failed to fetch game '{}': {}", slug, e.getMessage());
            }
        }

        if (gameIds.size() < 10) {
            return;
        }

        Long g1 = gameIds.get(0);
        Long g2 = gameIds.get(1);
        Long g3 = gameIds.get(2);
        Long g4 = gameIds.get(3);
        Long g5 = gameIds.get(4);
        Long g6 = gameIds.get(5);
        Long g7 = gameIds.get(6);
        Long g10 = gameIds.get(9);

        userGameService.create("osamah@games.com", new UserGameCreateRequest(g1, GameStatus.COMPLETED, 10));
        userGameService.create("osamah@games.com", new UserGameCreateRequest(g2, GameStatus.PLAYING, 8));
        userGameService.create("osamah@games.com", new UserGameCreateRequest(g10, GameStatus.PLANNED, null));
        userGameService.create("john@games.com", new UserGameCreateRequest(g1, GameStatus.COMPLETED, 9));
        userGameService.create("john@games.com", new UserGameCreateRequest(g3, GameStatus.DROPPED, 5));
        userGameService.create("sarah@games.com", new UserGameCreateRequest(g5, GameStatus.PLAYING, 10));
        userGameService.create("sarah@games.com", new UserGameCreateRequest(g7, GameStatus.COMPLETED, 9));
        userGameService.create("mike@games.com", new UserGameCreateRequest(g6, GameStatus.COMPLETED, 10));
        userGameService.create("mike@games.com", new UserGameCreateRequest(g4, GameStatus.COMPLETED, 10));
        userGameService.create("emma@games.com", new UserGameCreateRequest(g4, GameStatus.PLAYING, 7));


        reviewService.create(new ReviewCreateRequest(g1, 10, "Masterpiece. Best RPG ever made."), "osamah@games.com");
        reviewService.create(new ReviewCreateRequest(g1, 8, "Great story, but combat is clunky."), "john@games.com");
        reviewService.create(new ReviewCreateRequest(g7, 10, "So relaxing! Highly recommend."), "sarah@games.com");
        reviewService.create(new ReviewCreateRequest(g6, 9, "Still playing it 10 years later."), "mike@games.com");
        reviewService.create(new ReviewCreateRequest(g3, 5, "Too hard for me, couldn't get past the first boss."),
                "john@games.com");
    }
}