package com.osamah.games.review;

import com.osamah.games.exception.DuplicateResourceException;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.game.Game;
import com.osamah.games.game.GameRepository;
import com.osamah.games.review.dto.*;
import com.osamah.games.user.User;
import com.osamah.games.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    @Transactional
    public ReviewResponse create(ReviewCreateRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Game game = gameRepository.findById(request.gameId())
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

        if (reviewRepository.existsByUserIdAndGameId(user.getId(), game.getId())) {
            throw new DuplicateResourceException("You have already reviewed this game. Use PATCH to update it.");
        }

        Review review = Review.builder()
                .user(user)
                .game(game)
                .rating(request.rating())
                .content(request.content())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse patch(Long gameId, ReviewPatchRequest request, String userEmail) {

        User user = getUserByEmail(userEmail);
        Review review = getEntity(user.getId(), gameId);

        review.setRating(request.rating());
        review.setContent(request.content());

        return toResponse(reviewRepository.saveAndFlush(review));
    }

    @Transactional
    public void delete(Long gameId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Review review = getEntity(user.getId(), gameId);
        reviewRepository.delete(review);
    }

    public ReviewForUserResponse getSpecificReview(Long userId, Long gameId) {
        Review review = getEntity(userId, gameId);

        return toUserResponse(review);
    }

    @Transactional
    public void deleteAllForUser(Long userId) {
        ensureUserExists(userId);
        reviewRepository.deleteByUserId(userId);

    }

    public Page<ReviewForGameResponse> getByGameId(Long gameId, Pageable pageable) {
        ensureGameExists(gameId);

        return reviewRepository.findByGameId(gameId, pageable)
                .map(this::toGameResponse);
    }

    public Page<ReviewForUserResponse> getByUserId(Long userId, Pageable pageable) {
        ensureUserExists(userId);
        return reviewRepository.findByUserId(userId, pageable)
                .map(this::toUserResponse);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));
    }


    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(review.getId(), review.getUser()
                .getId(), review.getUser()
                .getUsername(), review.getGame()
                .getId(), review.getGame()
                .getTitle(), review.getRating(), review.getContent(), review.getCreatedAt(), review.getUpdatedAt());
    }

    private ReviewForGameResponse toGameResponse(Review review) {
        return new ReviewForGameResponse(review.getId(), review.getUser()
                .getId(), review.getUser()
                .getUsername(), review.getRating(), review.getContent(), review.getCreatedAt(), review.getUpdatedAt());
    }

    private Review getEntity(Long userId, Long gameId) {
        return reviewRepository.findByUserIdAndGameId(userId, gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private void ensureGameExists(Long gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new ResourceNotFoundException("Game not found");
        }
    }

    private ReviewForUserResponse toUserResponse(Review review) {
        return new ReviewForUserResponse(review.getId(), review.getGame()
                .getId(), review.getGame()
                .getTitle(), review.getGame()
                .getImageUrl(), review.getRating(), review.getContent(), review.getCreatedAt(), review.getUpdatedAt());
    }
}
