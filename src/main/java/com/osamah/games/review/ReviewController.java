package com.osamah.games.review;

import com.osamah.games.common.ApiResponse;
import com.osamah.games.review.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Operations related to game reviews left by users")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create review (Logged-In Only)", description = "Creates a new review by the logged-in user "
            + "for a game. Fails if a review already exists, " + "use patch to update an " + "existent review.")
    public ApiResponse<ReviewResponse> create(@Valid @RequestBody ReviewCreateRequest request, Authentication auth) {
        return ApiResponse.success(reviewService.create(request, auth.getName()), "Review saved successfully");
    }

    @PatchMapping("/{gameId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update review (Logged-In Only)", description = "Overwrites logged-in user's existing " +
            "review" + " " + "for a game in their list.")
    public ApiResponse<ReviewResponse> patch(@PathVariable Long gameId, @Valid @RequestBody ReviewPatchRequest request,
            Authentication auth) {
        return ApiResponse.success(reviewService.patch(gameId, request, auth.getName()),
                "Review updated " + "successfully");
    }

    @DeleteMapping("/{gameId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete review (Logged-In Only)", description = "Deletes logged-in user's review for" + " a "
            + "specific game in their list")
    public ApiResponse<String> delete(@PathVariable Long gameId, Authentication auth) {
        reviewService.delete(gameId, auth.getName());
        return ApiResponse.success(null, "Review deleted successfully");
    }

    @GetMapping("/game/{gameId}")
    @Operation(summary = "Get reviews by game (Public)", description = "Returns a paginated and sortable list of all "
            + "reviews left for a specific game")
    public ApiResponse<Page<ReviewForGameResponse>> getByGameId(@PathVariable Long gameId,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10) Pageable pageable) {
        return ApiResponse.success(reviewService.getByGameId(gameId, pageable));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews by user (Public)", description = "Returns a paginated and sortable list of all "
            + "reviews left by a specific user")
    public ApiResponse<Page<ReviewForUserResponse>> getByUserId(@PathVariable Long userId,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10) Pageable pageable) {
        return ApiResponse.success(reviewService.getByUserId(userId, pageable));
    }
}