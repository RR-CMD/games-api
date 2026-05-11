package com.osamah.games.game;

import com.osamah.games.game.dto.GameResponse;
import com.osamah.games.game.dto.GameSummaryResponse;
import com.osamah.games.security.CustomAuthenticationEntryPoint;
import com.osamah.games.security.JwtService;
import com.osamah.games.security.SecurityConfig;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class})
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    //SEARCH ENDPOINT TEST

    @Test
    @WithAnonymousUser
    void searchGames_ShouldReturnPaginatedData_WhenAnonymous() throws Exception {
        GameSummaryResponse summary = new GameSummaryResponse(1L, "Zelda", null, "url", 9.5, 100, List.of("Action"),
                List.of("Switch"));

        when(gameService.searchGames(any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(summary)));

        mockMvc.perform(get("/games").param("title", "Zelda")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Zelda"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchGames_ShouldReturnPaginatedData_WhenUser() throws Exception {
        GameSummaryResponse summary = new GameSummaryResponse(1L, "Zelda", null, "url", 9.5, 100, List.of("Action"),
                List.of("Switch"));

        when(gameService.searchGames(any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(summary)));

        mockMvc.perform(get("/games").param("title", "Zelda")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Zelda"));
    }

    //GET GAME DETAILS ENDPOINT TESTS

    @Test
    @WithAnonymousUser
    void getGameDetails_ShouldReturnGame_WhenExists() throws Exception {
        GameResponse gameResponse = new GameResponse(1L, "Elden Ring", "Desc", null, "url", 91, 9.8, List.of(),
                List.of(), null);
        when(gameService.getGameDetails(1L)).thenReturn(gameResponse);

        mockMvc.perform(get("/games/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Elden Ring"));
    }

    @Test
    @WithAnonymousUser
    void getGameDetails_ShouldReturn500_WhenGenericExceptionOccurs() throws Exception {

        when(gameService.getGameDetails(1L)).thenThrow(new RuntimeException("Database Error!"));

        mockMvc.perform(get("/games/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error: Database Error!"));
    }
    //CREATE GAME TESTS

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGame_ShouldSucceed_WhenAdmin() throws Exception {
        GameResponse gameResponse = new GameResponse(1L, "Mario", "Desc", null, "url", 75, 8.5, List.of(), List.of(),
                null);
        when(gameService.createGame("mario-slug")).thenReturn(gameResponse);

        mockMvc.perform(post("/games").param("slug", "mario-slug")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Game created successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createGame_ShouldReturn403_WhenUser() throws Exception {
        mockMvc.perform(post("/games").param("slug", "some-slug"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void createGame_ShouldReturn401_WhenAnonymous() throws Exception {
        mockMvc.perform(post("/games").param("slug", "some-slug"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGame_ShouldReturn400_WhenSlugIsMissing() throws Exception {

        mockMvc.perform(post("/games"))
                .andExpect(status().isBadRequest());
    }
}