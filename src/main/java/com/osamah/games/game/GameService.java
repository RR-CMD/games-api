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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "gamesDefaultSearch", key = "#pageable.pageNumber + '-' + #pageable.sort.toString()",
            condition = "#title == null and (#genres == null or #genres.isEmpty()) and #startYear == null and " +
                    "#endYear == null and #minScore == null and (#platforms == null or #platforms.isEmpty()) and " +
                    "#pageable.pageNumber <= 4")
    public Page<GameSummaryResponse> searchGames(String title, List<String> genres, Integer startYear, Integer endYear,
            Double minScore, List<String> platforms, Pageable pageable) {

        Long genreCount = (genres != null && !genres.isEmpty()) ? (long) genres.size() : null;

        return gameRepository.searchGames(title, genres, platforms, startYear, endYear, minScore, genreCount, pageable)
                .map(this::toSummaryResponse);
    }

    @Cacheable(value = "gameDetails", key = "#id")
    public GameResponse getGameDetails(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        return toResponse(game);
    }

    @CacheEvict(value = "gamesDefaultSearch", allEntries = true)
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

        List<String> redisSafeGenres = new java.util.ArrayList<>(game.getGenres());
        List<String> redisSafePlatforms = new java.util.ArrayList<>(game.getPlatforms());

        return new GameSummaryResponse(game.getId(), game.getTitle(), game.getReleaseDate(), game.getImageUrl(),
                game.getAverageScore(), game.getTotalAdded(), redisSafeGenres, redisSafePlatforms);
    }

    private GameResponse toResponse(Game game) {
        GameStatsResponse stats = new GameStatsResponse(game.getTotalAdded(), game.getPlannedCount(),
                game.getPlayingCount(), game.getCompletedCount(), game.getDroppedCount());

        List<String> redisSafeGenres = new java.util.ArrayList<>(game.getGenres());
        List<String> redisSafePlatforms = new java.util.ArrayList<>(game.getPlatforms());

        return new GameResponse(game.getId(), game.getTitle(), game.getDescription(), game.getReleaseDate(),
                game.getImageUrl(), game.getMetacriticScore(), game.getAverageScore(), redisSafeGenres,
                redisSafePlatforms, stats);
    }
}