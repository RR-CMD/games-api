package com.osamah.games.review;

import com.osamah.games.exception.DuplicateResourceException;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.game.Game;
import com.osamah.games.game.GameRepository;
import com.osamah.games.review.dto.*;
import com.osamah.games.user.User;
import com.osamah.games.user.UserRepository;
import com.osamah.games.user.enums.Role;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private final String userEmail = "osamah@example.com";
    private final Instant now = Instant.now();
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;
    @InjectMocks
    private ReviewService reviewService;
    private User testUser;
    private Game testGame;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email(userEmail)
                .username("osamah")
                .role(Role.USER)
                .build();
        setField(testUser, "id", 1L);

        testGame = Game.builder()
                .title("The Witcher 3")
                .slug("the-witcher-3")
                .build();
        setField(testGame, "id", 100L);

        testReview = Review.builder()
                .user(testUser)
                .game(testGame)
                .rating(9)
                .content("Amazing game!")
                .build();
        setField(testReview, "id", 500L);
        setField(testReview, "createdAt", now);
        setField(testReview, "updatedAt", now);
    }

    //CREATE TESTS

    @Test
    void create_ShouldSaveAndReturnResponse_WhenValid() {
        ReviewCreateRequest request = new ReviewCreateRequest(100L, 10, "Masterpiece");

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(100L)).thenReturn(Optional.of(testGame));
        when(reviewRepository.existsByUserIdAndGameId(1L, 100L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        ReviewResponse response = reviewService.create(request, userEmail);

        assertThat(response).isNotNull();
        assertThat(response.rating()).isEqualTo(9);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void create_ShouldThrowDuplicateException_WhenReviewAlreadyExists() {
        ReviewCreateRequest request = new ReviewCreateRequest(100L, 5, "Duplicate");

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(100L)).thenReturn(Optional.of(testGame));
        when(reviewRepository.existsByUserIdAndGameId(1L, 100L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> reviewService.create(request, userEmail));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowResourceNotFound_WhenGameDoesNotExist() {
        ReviewCreateRequest request = new ReviewCreateRequest(999L, 5, "No Game");

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.create(request, userEmail));
    }

    //PATCH TESTS

    @Test
    void patch_ShouldUpdateAndReturnResponse_WhenValid() {
        ReviewPatchRequest request = new ReviewPatchRequest(8, "Updated content");

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(reviewRepository.findByUserIdAndGameId(1L, 100L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.saveAndFlush(any(Review.class))).thenReturn(testReview);

        ReviewResponse response = reviewService.patch(100L, request, userEmail);

        assertThat(testReview.getRating()).isEqualTo(8);
        assertThat(testReview.getContent()).isEqualTo("Updated content");
        verify(reviewRepository).saveAndFlush(testReview);
    }

    @Test
    void patch_ShouldThrowNotFound_WhenReviewDoesNotExist() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(reviewRepository.findByUserIdAndGameId(1L, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.patch(999L, new ReviewPatchRequest(1, "x"), userEmail));
    }

    //DELETE TESTS

    @Test
    void delete_ShouldCallDelete_WhenReviewExists() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(reviewRepository.findByUserIdAndGameId(1L, 100L)).thenReturn(Optional.of(testReview));

        reviewService.delete(100L, userEmail);

        verify(reviewRepository).delete(testReview);
    }

    @Test
    void deleteAllForUser_ShouldDeleteAllFoundReviews() {
        when(userRepository.existsById(1L)).thenReturn(true);
        reviewService.deleteAllForUser(1L);

        verify(reviewRepository).deleteByUserId(1L);
    }

    //GET SPECIFIC REVIEW TEST

    @Test
    void getSpecificReview_ShouldReturnUserResponse_WhenFound() {
        when(reviewRepository.findByUserIdAndGameId(1L, 100L)).thenReturn(Optional.of(testReview));

        ReviewForUserResponse result = reviewService.getSpecificReview(1L, 100L);

        assertThat(result.gameTitle()).isEqualTo("The Witcher 3");
        assertThat(result.rating()).isEqualTo(9);
    }

    //GET BY GAME TEST
    @Test
    void getByGameId_ShouldReturnPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview));

        when(gameRepository.existsById(100L)).thenReturn(true);
        when(reviewRepository.findByGameId(eq(100L), any(Pageable.class))).thenReturn(reviewPage);

        Page<ReviewForGameResponse> result = reviewService.getByGameId(100L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()
                .getFirst()
                .username()).isEqualTo("osamah");
    }

    //GET BY USER TEST
    @Test
    void getByUserId_ShouldReturnPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(reviewPage);

        Page<ReviewForUserResponse> result = reviewService.getByUserId(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()
                .getFirst()
                .gameTitle()).isEqualTo("The Witcher 3");
    }


}