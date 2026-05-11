package com.osamah.games.usergame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.security.CustomAuthenticationEntryPoint;
import com.osamah.games.security.JwtService;
import com.osamah.games.security.SecurityConfig;
import com.osamah.games.usergame.dto.UserGameCreateRequest;
import com.osamah.games.usergame.dto.UserGamePatchRequest;
import com.osamah.games.usergame.dto.UserGameResponse;
import com.osamah.games.usergame.dto.UserGameStatsResponse;
import com.osamah.games.usergame.enums.GameStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserGameController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class})
class UserGameControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String email = "osamah@example.com";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserGameService userGameService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    //CREATE ENDPOINT TEST
    @Test
    @WithMockUser(username = email)
    void create_ShouldReturn200_WhenAuthenticated() throws Exception {

        UserGameCreateRequest request = new UserGameCreateRequest(101L, GameStatus.PLAYING, 9);
        UserGameResponse response = new UserGameResponse(1L, 101L, "Elden Ring", "img_url", GameStatus.PLAYING, 9,
                Instant.now(), Instant.now());

        when(userGameService.create(eq(email), any(UserGameCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/user-games").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gameTitle").value("Elden Ring"))
                .andExpect(jsonPath("$.data.rating").value(9));
    }

    @Test
    @WithAnonymousUser
    void create_ShouldReturn401_WhenAnonymous() throws Exception {
        UserGameCreateRequest request = new UserGameCreateRequest(101L, GameStatus.PLAYING, 9);

        mockMvc.perform(post("/user-games").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void create_ShouldReturn400_WhenGameStatusInvalid() throws Exception {

        String jsonRequest = """
                {
                    "gameId": 101,
                    "gameStatus": "NOT_A_REAL_STATUS",
                    "rating": 5
                }
                """;

        mockMvc.perform(post("/user-games").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Accepted values are")));
    }

    @Test
    @WithMockUser
    void create_ShouldReturn400_WhenRatingInvalid() throws Exception {

        String jsonRequest = """
                {
                    "gameId": 101,
                    "gameStatus": "COMPLETED",
                    "rating": 50
                }
                """;

        mockMvc.perform(post("/user-games").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("rating")));
    }

    @Test
    @WithMockUser(username = email)
    void create_ShouldReturn409_WhenGameAlreadyInList() throws Exception {

        when(userGameService.create(eq(email), any())).thenThrow(
                new com.osamah.games.exception.DuplicateResourceException("Game already in list"));

        String jsonRequest = "{\"gameId\": 101, \"gameStatus\": \"PLAYING\"}";

        mockMvc.perform(post("/user-games").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Game already in list"));
    }

    //PATCH ENDPOINT TEST
    @Test
    @WithMockUser(username = email)
    void patch_ShouldReturn200_WhenValid() throws Exception {
        UserGamePatchRequest request = new UserGamePatchRequest(GameStatus.COMPLETED, 10);
        UserGameResponse response = new UserGameResponse(1L, 101L, "Elden Ring", "img_url", GameStatus.COMPLETED, 10,
                Instant.now(), Instant.now());

        when(userGameService.patch(eq(email), eq(101L), any(UserGamePatchRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/user-games/101").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gameStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.rating").value(10));
    }

    @Test
    @WithAnonymousUser
    void patch_ShouldReturn401_WhenAnonymous() throws Exception {
        UserGameCreateRequest request = new UserGameCreateRequest(101L, GameStatus.PLAYING, 9);

        mockMvc.perform(patch("/user-games").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = email)
    void patch_ShouldReturn404_WhenUserGameNotFound() throws Exception {
        when(userGameService.patch(eq(email), eq(9L), any())).thenThrow(
                new com.osamah.games.exception.ResourceNotFoundException("Entry not found"));

        String jsonRequest = "{\"gameStatus\": \"COMPLETED\"}";

        mockMvc.perform(patch("/user-games/9").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Entry not found"));
    }

    @Test
    @WithMockUser
    void patch_ShouldReturn400_WhenGameStatusInvalid() throws Exception {

        String jsonRequest = """
                {
                    "gameId": 101,
                    "gameStatus": "NOT_A_REAL_STATUS",
                    "rating": 5
                }
                """;

        mockMvc.perform(patch("/user-games/101").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Accepted values are")));
    }

    @Test
    @WithMockUser
    void patch_ShouldReturn400_WhenRatingInvalid() throws Exception {

        String jsonRequest = """
                {
                    "gameId": 101,
                    "gameStatus": "COMPLETED",
                    "rating": 50
                }
                """;

        mockMvc.perform(patch("/user-games/101").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("rating")));
    }

    @Test
    @WithMockUser
    void patch_ShouldReturn409_WhenOptimisticLockingFails() throws Exception {
        UserGamePatchRequest request = new UserGamePatchRequest(GameStatus.COMPLETED, 10);

        when(userGameService.patch(any(), eq(101L), eq(request))).thenThrow(
                new org.springframework.orm.ObjectOptimisticLockingFailureException("Game", 101L));

        mockMvc.perform(patch("/user-games/101").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Error! Please try again."));
    }

    //DELETE ENDPOINT TEST

    @Test
    @WithMockUser(username = email)
    void delete_ShouldReturn200_WhenOwner() throws Exception {
        doNothing().when(userGameService)
                .delete(email, 101L);

        mockMvc.perform(delete("/user-games/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Game removed from list successfully"));
    }

    //GET USERLIST TEST

    @Test
    @WithAnonymousUser
    void getByUserId_ShouldReturnPaginatedData_WhenPublic() throws Exception {
        UserGameResponse response = new UserGameResponse(1L, 101L, "Zelda", "img_url", GameStatus.PLANNED, null,
                Instant.now(), Instant.now());

        when(userGameService.getByUserId(eq(1L), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/user-games/user/1").param("status", "PLANNED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].gameTitle").value("Zelda"));
    }

    //GET STATS ENDPOINT TEST
    @Test
    @WithAnonymousUser
    void getStats_ShouldReturnStats_WhenPublic() throws Exception {
        UserGameStatsResponse stats = new UserGameStatsResponse(10, 5, 2, 2, 1);
        when(userGameService.getStatsByUserId(1L)).thenReturn(stats);

        mockMvc.perform(get("/user-games/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPlayed").value(10))
                .andExpect(jsonPath("$.data.completed").value(2));
    }

    //GET USERGAME ENDPOINT TEST

    @Test
    @WithMockUser(username = email)
    void getUserGame_ShouldReturn200_WhenEntryExists() throws Exception {
        UserGameResponse response = new UserGameResponse(1L, 100L, "Elden Ring", "img-url", GameStatus.PLAYING, 8,
                Instant.now(), Instant.now()

        );

        when(userGameService.getUserGame(eq(email), eq(100L))).thenReturn(response);


        mockMvc.perform(get("/user-games/100").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gameId").value(100L))
                .andExpect(jsonPath("$.data.gameStatus").value("PLAYING"));
    }

    @Test
    @WithMockUser(username = email)
    void getUserGame_ShouldReturn404_WhenEntryDoesNotExist() throws Exception {
        when(userGameService.getUserGame(anyString(), eq(999L))).thenThrow(
                new ResourceNotFoundException("Game entry not found for this user."));


        mockMvc.perform(get("/user-games/999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void getUserGame_ShouldReturn401_WhenUserIsAnonymous() throws Exception {
        mockMvc.perform(get("/user-games/100").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}