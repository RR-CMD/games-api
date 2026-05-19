package com.osamah.games.game;

import com.osamah.games.exception.DuplicateResourceException;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.external.rawg.RawgClient;
import com.osamah.games.external.rawg.dto.RawgGameResponse;
import com.osamah.games.game.dto.GameResponse;
import com.osamah.games.game.dto.GameSummaryResponse;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private RawgClient rawgClient;

    @InjectMocks
    private GameService gameService;

    private Game testGame;

    @BeforeEach
    void setUp() {
        testGame = Game.builder()
                .slug("the-witcher-3")
                .title("The Witcher 3")
                .releaseDate(LocalDate.of(2010, 10, 10))
                .genres(List.of("RPG", "Action"))
                .platforms(List.of("PC", "PlayStation 4"))
                .build();
        setField(testGame, "id", 1L);
    }

    //CREATE TESTS

    @Test
    void createGame_ShouldThrowException_WhenSlugAlreadyExistsInDb() {
        when(gameRepository.findBySlug("the-witcher-3")).thenReturn(Optional.of(testGame));

        assertThrows(DuplicateResourceException.class, () -> gameService.createGame("the-witcher-3"));
        verify(rawgClient, never()).getGameDetails(anyString());
    }

    @Test
    void createGame_ShouldThrowResourceNotFound_WhenRawgReturns404() {
        String slug = "wrong-game";
        when(gameRepository.findBySlug(slug)).thenReturn(Optional.empty());

        FeignException.NotFound mock404 = mock(FeignException.NotFound.class);
        when(rawgClient.getGameDetails(slug)).thenThrow(mock404);

        assertThrows(ResourceNotFoundException.class, () -> gameService.createGame(slug));
    }

    @Test
    void createGame_ShouldSaveAndReturnResponse_WhenValid() {
        String slug = "elden-ring";
        RawgGameResponse rawgResp = new RawgGameResponse(slug, "Elden Ring", "A vast open world RPG",
                LocalDate.of(2020, 5, 10), "img-url", 85, List.of(new RawgGameResponse.RawgGenre("RPG")),
                List.of(new RawgGameResponse.PlatformWrapper(new RawgGameResponse.PlatformDetails("PC"))));

        Game eldenRing = Game.builder()
                .slug(slug)
                .title("Elden Ring")
                .genres(List.of("RPG"))
                .platforms((List.of("PC")))
                .build();
        setField(eldenRing, "id", 50L);

        when(gameRepository.findBySlug(slug)).thenReturn(Optional.empty());
        when(rawgClient.getGameDetails(slug)).thenReturn(rawgResp);
        when(gameRepository.save(any(Game.class))).thenReturn(eldenRing);


        GameResponse response = gameService.createGame(slug);

        assertThat(response.title()).isEqualTo("Elden Ring");
        assertThat(response.id()).isEqualTo(50L);
        verify(gameRepository).save(any(Game.class));
    }

    //SEARCH TESTS
    @Test
    void searchGames_ShouldCalculateGenreCountCorrectly() {
        List<String> genres = List.of("RPG", "Adventure");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Game> gamePage = new PageImpl<>(List.of(testGame));

        when(gameRepository.searchGames(any(), eq(genres), any(), any(), any(), any(), eq(2L),
                eq(pageable))).thenReturn(gamePage);

        Page<GameSummaryResponse> result = gameService.searchGames("Witcher", genres, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(gameRepository).searchGames(any(), any(), any(), any(), any(), any(), eq(2L), any());
    }

    @Test
    void getGameDetails_ShouldReturnResponse_WhenIdExists() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        GameResponse result = gameService.getGameDetails(1L);

        assertThat(result.title()).isEqualTo("The Witcher 3");
        assertThat(result.genres()).hasSize(2)
                .containsExactly("RPG", "Action");
        assertThat(result.platforms()).hasSize(2)
                .containsExactly("PC", "PlayStation 4");
    }

    @Test
    void getGameDetails_ShouldThrowException_WhenIdDoesNotExist() {
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gameService.getGameDetails(99L));
    }

    //UPDATE AVG SCORES TESTS

    @Test
    void updateAverageScores_ShouldCallRepositoryBulkUpdate() {
        gameService.updateAverageScores();

        verify(gameRepository).updateAllAverageScores();
    }
}