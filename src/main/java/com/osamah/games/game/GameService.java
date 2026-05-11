package com.osamah.games.game;

import com.osamah.games.exception.DuplicateResourceException;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.external.rawg.RawgClient;
import com.osamah.games.external.rawg.dto.RawgGameResponse;
import com.osamah.games.game.dto.GameResponse;
import com.osamah.games.game.dto.GameStatsResponse;
import com.osamah.games.game.dto.GameSummaryResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final RawgClient rawgClient;

    public Page<GameSummaryResponse> searchGames(String title, List<String> genres, Integer startYear, Integer endYear,
            Double minScore, List<String> platforms, Pageable pageable) {

        Long genreCount = (genres != null && !genres.isEmpty()) ? (long) genres.size() : null;

        return gameRepository.searchGames(title, genres, platforms, startYear, endYear, minScore, genreCount, pageable)
                .map(this::toSummaryResponse);
    }

    public GameResponse getGameDetails(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        return toResponse(game);
    }


    @Transactional
    public GameResponse createGame(String slug) {

        if (gameRepository.findBySlug(slug)
                .isPresent()) {
            throw new DuplicateResourceException("Game already exists in the database");
        }

        RawgGameResponse rawgData;
        try {
            rawgData = rawgClient.getGameDetails(slug);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Game not found on RAWG API for slug: " + slug);
        }
        List<String> genres = rawgData.genres()
                .stream()
                .map(RawgGameResponse.RawgGenre::name)
                .toList();

        List<String> platforms = rawgData.platforms()
                .stream()
                .map(wrapper -> wrapper.platform()
                        .name())
                .toList();

        Game newGame = Game.builder()
                .slug(rawgData.slug())
                .title(rawgData.title())
                .description(rawgData.description())
                .releaseDate(rawgData.released())
                .imageUrl(rawgData.imageUrl())
                .metacriticScore(rawgData.metacritic())
                .genres(genres)
                .platforms(platforms)
                .build();

        return toResponse(gameRepository.save(newGame));
    }


    /*
    NOTE: The scheduling below is heavy and should only be used for small-scale projects and testing.
             For real-world apps, we should instead use something like @Scheduled(cron = "0 0 3 * * MON") to
             calculate the average every Monday at 3 AM (server-time).
    */
    @Scheduled(fixedRate = 120000)
    @Transactional
    public void updateAverageScores() {
        gameRepository.updateAllAverageScores();

    }

    private GameSummaryResponse toSummaryResponse(Game game) {
        return new GameSummaryResponse(game.getId(), game.getTitle(), game.getReleaseDate(), game.getImageUrl(),
                game.getAverageScore(), game.getTotalAdded(), game.getGenres(), game.getPlatforms());
    }

    private GameResponse toResponse(Game game) {
        GameStatsResponse stats = new GameStatsResponse(game.getTotalAdded(), game.getPlannedCount(),
                game.getPlayingCount(), game.getCompletedCount(), game.getDroppedCount());

        return new GameResponse(game.getId(), game.getTitle(), game.getDescription(), game.getReleaseDate(),
                game.getImageUrl(), game.getMetacriticScore(), game.getAverageScore(), game.getGenres(),
                game.getPlatforms(), stats);
    }
}