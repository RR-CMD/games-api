package com.osamah.games.review;


import com.osamah.games.exception.DuplicateResourceException;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.exception.advice.GlobalExceptionHandler;
import com.osamah.games.review.dto.*;
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
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, GlobalExceptionHandler.class})
class ReviewControllerTest {

    private final String email = "osamah@example.com";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ReviewService reviewService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    //CREATE ENDPOINT TESTS

    @Test
    @WithMockUser(username = email)
    void create_ShouldReturn200_WhenUserAndValid() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest(100L, 9, "Masterpiece!");
        ReviewResponse response = new ReviewResponse(1L, 1L, "osamah", 100L, "Elden Ring", 9, "Masterpiece!",
                Instant.now(), Instant.now());

        when(reviewService.create(any(ReviewCreateRequest.class), eq(email))).thenReturn(response);

        mockMvc.perform(post("/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("osamah"))
                .andExpect(jsonPath("$.data.content").value("Masterpiece!"));
    }

    @Test
    @WithAnonymousUser
    void create_ShouldReturn401_WhenAnonymous() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest(100L, 9, "Masterpiece!");

        mockMvc.perform(post("/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = email)
    void create_ShouldReturn400_WhenInputInvalid() throws Exception {
        ReviewCreateRequest invalidRequest = new ReviewCreateRequest(100L, 11, "");

        mockMvc.perform(post("/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = email)
    void create_ShouldReturn409_WhenReviewExists() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest(100L, 5, "Good");

        when(reviewService.create(any(), eq(email))).thenThrow(
                new DuplicateResourceException("Review already exists for this game"));

        mockMvc.perform(post("/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = email)
    void create_ShouldReturn400_WhenRatingInvalid() throws Exception {
        ReviewPatchRequest invalidRequest = new ReviewPatchRequest(null, "Invalid rating");

        mockMvc.perform(post("/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    //PATCH ENDPOINT TESTS

    @Test
    @WithMockUser(username = email)
    void update_ShouldReturn200() throws Exception {
        ReviewPatchRequest request = new ReviewPatchRequest(10, "Updated content");
        ReviewResponse response = new ReviewResponse(1L, 1L, "osamah", 100L, "Elden Ring", 10, "Updated content",
                Instant.now(), Instant.now());

        when(reviewService.patch(eq(100L), any(ReviewPatchRequest.class), eq(email))).thenReturn(response);

        mockMvc.perform(patch("/reviews/100").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(10));
    }

    @Test
    @WithMockUser(username = email)
    void update_ShouldReturn404_WhenReviewNotFound() throws Exception {
        ReviewPatchRequest request = new ReviewPatchRequest(8, "Still good");

        when(reviewService.patch(eq(999L), any(), eq(email))).thenThrow(
                new ResourceNotFoundException("Review not found"));

        mockMvc.perform(patch("/reviews/999").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = email)
    void update_ShouldReturn400_WhenRatingInvalid() throws Exception {
        ReviewPatchRequest invalidRequest = new ReviewPatchRequest(0, "Invalid rating");

        mockMvc.perform(patch("/reviews/100").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void update_ShouldReturn401_WhenAnonymous() throws Exception {
        ReviewPatchRequest request = new ReviewPatchRequest(5, "Should fail");

        mockMvc.perform(put("/reviews/100").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    //DELETE ENDPOINT TESTS

    @Test
    @WithMockUser(username = email)
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(reviewService)
                .delete(100L, email);

        mockMvc.perform(delete("/reviews/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review deleted successfully"));
    }

    @Test
    @WithMockUser(username = email)
    void delete_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Review not found")).when(reviewService)
                .delete(eq(999L), anyString());

        mockMvc.perform(delete("/reviews/999"))
                .andExpect(status().isNotFound());
    }

    //GET BY GAME ENDPOINT TEST

    @Test
    @WithAnonymousUser
    void getByGameId_ShouldReturnPaginatedData() throws Exception {
        ReviewForGameResponse review = new ReviewForGameResponse(1L, 1L, "osamah", 10, "Classic", Instant.now(),
                Instant.now());
        PageImpl<ReviewForGameResponse> page = new PageImpl<>(List.of(review));

        when(reviewService.getByGameId(eq(100L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/reviews/game/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].username").value("osamah"))
                .andExpect(jsonPath("$.data.content[0].rating").value(10));
    }

    //GET BY USER ENDPOINT TEST
    @Test
    @WithAnonymousUser
    void getByUserId_ShouldReturnPaginatedData() throws Exception {

        ReviewForUserResponse review = new ReviewForUserResponse(1L, 100L, "Elden Ring", "img_url", 9, "Hard",
                Instant.now(), Instant.now());
        PageImpl<ReviewForUserResponse> page = new PageImpl<>(List.of(review));

        when(reviewService.getByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/reviews/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].gameTitle").value("Elden Ring"))
                .andExpect(jsonPath("$.data.content[0].imageUrl").value("img_url"));
    }
}