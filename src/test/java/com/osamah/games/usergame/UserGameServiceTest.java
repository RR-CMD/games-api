package com.osamah.games.usergame;

import com.osamah.games.exception.BadRequestException;
import com.osamah.games.exception.DuplicateResourceException;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.game.Game;
import com.osamah.games.game.GameRepository;
import com.osamah.games.user.User;
import com.osamah.games.user.UserRepository;
import com.osamah.games.user.enums.Role;
import com.osamah.games.usergame.dto.UserGameCreateRequest;
import com.osamah.games.usergame.dto.UserGamePatchRequest;
import com.osamah.games.usergame.dto.UserGameResponse;
import com.osamah.games.usergame.dto.UserGameStatsResponse;
import com.osamah.games.usergame.enums.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class UserGameServiceTest {

    @Mock
    private UserGameRepository userGameRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;
    @InjectMocks
    private UserGameService userGameService;
    private User testUser;
    private Game testGame;
    private UserGame testUserGame;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("osamah@example.com")
                .username("osamah")
                .password("Password1234!")
                .role(Role.USER)
                .build();
        setField(testUser, "id", 1L);

        testGame = Game.builder()
                .slug("the-witcher-3")
                .title("The Witcher 3")
                .releaseDate(LocalDate.of(2010, 10, 10))
                .genres(List.of("RPG", "Action"))
                .platforms(List.of("PC", "PlayStation 4"))
                .build();
        setField(testGame, "id", 100L);
        setField(testGame, "totalAdded", 10L);
        setField(testGame, "plannedCount", 5L);
        setField(testGame, "playingCount", 5L);
        setField(testGame, "completedCount", 0L);
        setField(testGame, "droppedCount", 0L);


        testUserGame = UserGame.builder()
                .user(testUser)
                .game(testGame)
                .gameStatus(GameStatus.PLAYING)
                .rating(9)
                .build();
        setField(testUserGame, "id", 500L);
    }

    //CREATE TESTS

    @Test
    void create_ShouldSaveAndIncrementCounts_WhenValid() {
        UserGameCreateRequest request = new UserGameCreateRequest(100L, GameStatus.COMPLETED, 10);

        when(userRepository.findByEmail("osamah@example.com")).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(100L)).thenReturn(Optional.of(testGame));
        when(userGameRepository.findByUserIdAndGameId(1L, 100L)).thenReturn(Optional.empty());
        when(userGameRepository.save(any(UserGame.class))).thenReturn(testUserGame);

        UserGameResponse response = userGameService.create("osamah@example.com", request);

        assertThat(response).isNotNull();
        assertThat(testGame.getTotalAdded()).isEqualTo(11);
        assertThat(testGame.getCompletedCount()).isEqualTo(1);
        verify(gameRepository).save(testGame);
        verify(userGameRepository).save(any(UserGame.class));
    }

    @Test
    void create_ShouldThrowDuplicateException_WhenAlreadyInList() {
        UserGameCreateRequest request = new UserGameCreateRequest(100L, GameStatus.PLAYING, 9);

        when(userRepository.findByEmail("osamah@example.com")).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(100L)).thenReturn(Optional.of(testGame));
        when(userGameRepository.findByUserIdAndGameId(1L, 100L)).thenReturn(Optional.of(testUserGame));

        assertThrows(DuplicateResourceException.class, () -> userGameService.create("osamah@example.com", request));
    }

    @Test
    void create_ShouldThrowBadRequest_WhenPlannedGameHasRating() {
        UserGameCreateRequest request = new UserGameCreateRequest(100L, GameStatus.PLANNED, 10);

        when(userRepository.findByEmail("osamah@example.com")).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(100L)).thenReturn(Optional.of(testGame));

        assertThrows(BadRequestException.class, () -> userGameService.create("osamah@example.com", request));
    }

    //PATCH TESTS

    @Test
    void patch_ShouldUpdateStatusAndAdjustCounts_WhenStatusChanges() {
        UserGamePatchRequest request = new UserGamePatchRequest(GameStatus.COMPLETED, 10);

        when(userRepository.findByEmail("osamah@example.com")).thenReturn(Optional.of(testUser));
        when(userGameRepository.findByUserIdAndGameId(1L, 100L)).thenReturn(Optional.of(testUserGame));
        when(userGameRepository.saveAndFlush(any(UserGame.class))).thenReturn(testUserGame);

        userGameService.patch("osamah@example.com", 100L, request);

        assertThat(testGame.getPlayingCount()).isEqualTo(4);
        assertThat(testGame.getCompletedCount()).isEqualTo(1);
        assertThat(testUserGame.getGameStatus()).isEqualTo(GameStatus.COMPLETED);
        verify(gameRepository).save(testGame);
        verify(userGameRepository).saveAndFlush(testUserGame);
    }

    @Test
    void patch_ShouldNullifyRating_WhenStatusIsChangedToPlanned() {
        testUserGame.setGameStatus(GameStatus.PLAYING);
        testUserGame.setRating(9);

        UserGamePatchRequest request = new UserGamePatchRequest(GameStatus.PLANNED, null);

        when(userRepository.findByEmail("osamah@example.com")).thenReturn(Optional.of(testUser));
        when(userGameRepository.findByUserIdAndGameId(1L, 100L)).thenReturn(Optional.of(testUserGame));
        when(userGameRepository.saveAndFlush(any(UserGame.class))).thenReturn(testUserGame);

        userGameService.patch("osamah@example.com", 100L, request);

        assertThat(testUserGame.getGameStatus()).isEqualTo(GameStatus.PLANNED);
        assertThat(testUserGame.getRating()).isNull();

        assertThat(testGame.getPlayingCount()).isEqualTo(4);
        assertThat(testGame.getPlannedCount()).isEqualTo(6);

        verify(userGameRepository).saveAndFlush(testUserGame);
    }

    @Test
    void patch_ShouldKeepExistingRating_WhenNewStatusIsNotPlannedAndRatingIsNullInRequest() {
        testUserGame.setRating(7);
        UserGamePatchRequest request = new UserGamePatchRequest(GameStatus.DROPPED, null);

        when(userRepository.findByEmail("osamah@example.com")).thenReturn(Optional.of(testUser));
        when(userGameRepository.findByUserIdAndGameId(1L, 100L)).thenReturn(Optional.of(testUserGame));
        when(userGameRepository.saveAndFlush(any(UserGame.class))).thenReturn(testUserGame);

        userGameService.patch("osamah@example.com", 100L, request);

        assertThat(testUserGame.getRating()).isEqualTo(7); // Kept the old rating
        assertThat(testUserGame.getGameStatus()).isEqualTo(GameStatus.DROPPED);
    }

    @Test
    void patch_ShouldThrowBadRequest_WhenPlannedGameHasRating() {
        UserGamePatchRequest request = new UserGamePatchRequest(GameStatus.PLANNED, 10);

        when(userRepository.findByEmail("osamah@example.com")).thenReturn(Optional.of(testUser));
        when(userGameRepository.findByUserIdAndGameId(1L, 100L)).thenReturn(Optional.of(testUserGame));

        assertThrows(BadRequestException.class, () -> userGameService.patch("osamah@example.com", 100L, request));
    }

    //DELETE TESTS

    @Test
    void delete_ShouldDecrementCountsAndDelete() {
        when(userRepository.findByEmail("osamah@example.com")).thenReturn(Optional.of(testUser));
        when(userGameRepository.findByUserIdAndGameId(1L, 100L)).thenReturn(Optional.of(testUserGame));

        userGameService.delete("osamah@example.com", 100L);

        assertThat(testGame.getTotalAdded()).isEqualTo(9);
        assertThat(testGame.getPlayingCount()).isEqualTo(4);
        verify(userGameRepository).delete(testUserGame);
        verify(gameRepository).save(testGame);
    }

    @Test
    void deleteAllForUser_ShouldCleanAllGameCounts() {
        when(userGameRepository.findByUserId(1L)).thenReturn(List.of(testUserGame));

        userGameService.deleteAllForUser(1L);

        assertThat(testGame.getTotalAdded()).isEqualTo(9);
        assertThat(testGame.getPlayingCount()).isEqualTo(4);
        verify(userGameRepository).deleteAll(anyList());
    }

    //GET BY USER TEST

    @Test
    void getByUserId_ShouldReturnPagedResponse_WhenUserExists() {
        Long userId = 1L;
        String statusStr = "playing";
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userGameRepository.searchUserGames(eq(userId), eq(GameStatus.PLAYING), any(), any(), any(), any(),
                eq(pageable))).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testUserGame)));

        var response = userGameService.getByUserId(userId, statusStr, null, null, null, null, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent()
                .getFirst()
                .gameTitle()).isEqualTo("The Witcher 3");
        verify(userGameRepository).searchUserGames(userId, GameStatus.PLAYING, null, null, null, null, pageable);
    }

    @Test
    void getByUserId_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.existsById(99L)).thenReturn(false);
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(ResourceNotFoundException.class,
                () -> userGameService.getByUserId(99L, null, null, null, null, null, pageable));
    }

    //GET SPECIFIC ENTRY TEST
    @Test
    void getUserGame_ShouldReturnResponse_WhenEntryExists() {

        String email = "osamah@example.com";
        Long gameId = 100L;

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userGameRepository.findByUserIdAndGameId(1L, gameId)).thenReturn(Optional.of(testUserGame));

        UserGameResponse response = userGameService.getUserGame(email, gameId);

        assertThat(response).isNotNull();
        assertThat(response.gameTitle()).isEqualTo("The Witcher 3");
        assertThat(response.rating()).isEqualTo(9);
    }

    @Test
    void getUserGame_ShouldThrowNotFound_WhenGameIdInvalid() {

        assertThrows(ResourceNotFoundException.class, () -> userGameService.getUserGame("osamah@example.com", 999L));
    }

    @Test
    void getStatsByUserId_ShouldMapResultsCorrectly() {
        List<Object[]> queryResults = new ArrayList<>();
        queryResults.add(new Object[]{GameStatus.COMPLETED, 5L});
        queryResults.add(new Object[]{GameStatus.PLAYING, 3L});

        when(userRepository.existsById(1L)).thenReturn(true);
        when(userGameRepository.statsByUserId(1L)).thenReturn(queryResults);

        UserGameStatsResponse stats = userGameService.getStatsByUserId(1L);

        assertThat(stats.totalPlayed()).isEqualTo(8);
        assertThat(stats.completed()).isEqualTo(5);
        assertThat(stats.playing()).isEqualTo(3);
        assertThat(stats.planned()).isEqualTo(0);
    }
}