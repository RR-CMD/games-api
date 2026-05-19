package com.osamah.games.usergame;

import com.osamah.games.exception.BadRequestException;
import com.osamah.games.exception.DuplicateResourceException;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.game.Game;
import com.osamah.games.game.GameRepository;
import com.osamah.games.user.User;
import com.osamah.games.user.UserRepository;
import com.osamah.games.usergame.dto.UserGameCreateRequest;
import com.osamah.games.usergame.dto.UserGamePatchRequest;
import com.osamah.games.usergame.dto.UserGameResponse;
import com.osamah.games.usergame.dto.UserGameStatsResponse;
import com.osamah.games.usergame.enums.GameStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserGameService {

    private final UserGameRepository userGameRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    @Caching(evict = {@CacheEvict(value = "userDefaultList", allEntries = true), @CacheEvict(value = "userStats",
            key = "#userEmail") // Note: Make sure the key matches however you keyed the @Cacheable method!
    })
    @Transactional
    public UserGameResponse create(String userEmail, UserGameCreateRequest request) {

        User user = findUserByEmail(userEmail);
        Game game = findGame(request.gameId());

        if (userGameRepository.findByUserIdAndGameId(user.getId(), game.getId())
                .isPresent()) {
            throw new DuplicateResourceException("Game is already in your list. Use PATCH to update it.");
        }

        validateStatusAndRating(request.gameStatus(), request.rating());

        UserGame ug = UserGame.builder()
                .user(user)
                .game(game)
                .gameStatus(request.gameStatus())
                .rating(request.rating())
                .build();


        game.setTotalAdded(game.getTotalAdded() + 1);
        incrementCount(game, request.gameStatus());
        gameRepository.save(game);

        return toResponse(userGameRepository.save(ug));
    }

    @Caching(evict = {@CacheEvict(value = "userDefaultList", allEntries = true), @CacheEvict(value = "userStats",
            key = "#userEmail")})
    @Transactional
    public UserGameResponse patch(String userEmail, Long gameId, UserGamePatchRequest request) {
        User user = findUserByEmail(userEmail);
        UserGame ug = getEntity(user.getId(), gameId);
        Game game = ug.getGame();

        GameStatus oldStatus = ug.getGameStatus();
        GameStatus newStatus = request.gameStatus() != null ? request.gameStatus() : ug.getGameStatus();

        Integer rating;
        if (request.rating() != null) {
            rating = request.rating();
        } else if (newStatus == GameStatus.PLANNED) {
            rating = null;
        } else {
            rating = ug.getRating();
        }

        validateStatusAndRating(newStatus, rating);

        if (oldStatus != newStatus) {
            decrementCount(game, oldStatus);
            incrementCount(game, newStatus);
            gameRepository.save(game);
        }

        ug.setGameStatus(newStatus);
        ug.setRating(rating);

        return toResponse(userGameRepository.saveAndFlush(ug));
    }

    @Caching(evict = {@CacheEvict(value = "userDefaultList", allEntries = true), @CacheEvict(value = "userStats",
            key = "#userEmail")})
    @Transactional
    public void delete(String userEmail, Long gameId) {
        User user = findUserByEmail(userEmail);
        UserGame ug = getEntity(user.getId(), gameId);
        Game game = ug.getGame();

        game.setTotalAdded(game.getTotalAdded() - 1);
        decrementCount(game, ug.getGameStatus());
        gameRepository.save(game);

        userGameRepository.delete(ug);
    }

    @Transactional
    public void deleteAllForUser(Long userId) {
        List<UserGame> userGames = userGameRepository.findByUserId(userId);

        userGames.forEach(ug -> {
            Game game = ug.getGame();
            game.setTotalAdded(game.getTotalAdded() - 1);
            decrementCount(game, ug.getGameStatus());
            gameRepository.save(game);
        });

        userGameRepository.deleteAll(userGames);
    }

    public Page<UserGameResponse> getByUserId(Long userId, String status, String genre, String title, Integer startYear,
            Integer endYear, Pageable pageable) {

        ensureUserExists(userId);

        GameStatus gameStatus = null;

        if (status != null) {
            gameStatus = GameStatus.valueOf(status.toUpperCase());
        }

        return userGameRepository.searchUserGames(userId, gameStatus, genre, title, startYear, endYear, pageable)
                .map(this::toResponse);
    }

    public UserGameResponse getUserGame(String userEmail, Long gameId) {

        User user = findUserByEmail(userEmail);
        UserGame ug = getEntity(user.getId(), gameId);

        return toResponse(ug);
    }

    public UserGameStatsResponse getStatsByUserId(Long userId) {

        ensureUserExists(userId);

        List<Object[]> results = userGameRepository.statsByUserId(userId);

        Map<GameStatus, Long> statsMap = results.stream()
                .collect(Collectors.toMap(row -> (GameStatus) row[0], row -> (Long) row[1]));

        int planned = statsMap.getOrDefault(GameStatus.PLANNED, 0L)
                .intValue();
        int playing = statsMap.getOrDefault(GameStatus.PLAYING, 0L)
                .intValue();
        int completed = statsMap.getOrDefault(GameStatus.COMPLETED, 0L)
                .intValue();
        int dropped = statsMap.getOrDefault(GameStatus.DROPPED, 0L)
                .intValue();
        int total = planned + playing + completed + dropped;

        return new UserGameStatsResponse(total, planned, playing, completed, dropped);
    }


    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));
    }


    private Game findGame(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
    }


    private UserGame getEntity(Long userId, Long gameId) {
        return userGameRepository.findByUserIdAndGameId(userId, gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game entry not found on your list"));
    }

    private void validateStatusAndRating(GameStatus status, Integer rating) {

        if (status == GameStatus.PLANNED && rating != null) {
            throw new BadRequestException("Planned games cannot have a rating");
        }
    }

    private UserGameResponse toResponse(UserGame ug) {

        return new UserGameResponse(ug.getId(), ug.getGame()
                .getId(), ug.getGame()
                .getTitle(), ug.getGame()
                .getImageUrl(), ug.getGameStatus(), ug.getRating(), ug.getCreatedAt(), ug.getUpdatedAt());
    }


    private void updateCount(Game game, GameStatus status, int x) {
        switch (status) {
            case PLANNED -> game.setPlannedCount(game.getPlannedCount() + x);
            case PLAYING -> game.setPlayingCount(game.getPlayingCount() + x);
            case COMPLETED -> game.setCompletedCount(game.getCompletedCount() + x);
            case DROPPED -> game.setDroppedCount(game.getDroppedCount() + x);
        }
    }

    private void incrementCount(Game game, GameStatus status) {
        updateCount(game, status, 1);
    }

    private void decrementCount(Game game, GameStatus status) {
        updateCount(game, status, -1);
    }


}