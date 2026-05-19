package com.osamah.games.usergame;

import com.osamah.games.common.ApiResponse;
import com.osamah.games.usergame.dto.UserGameCreateRequest;
import com.osamah.games.usergame.dto.UserGamePatchRequest;
import com.osamah.games.usergame.dto.UserGameResponse;
import com.osamah.games.usergame.dto.UserGameStatsResponse;
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
@RequestMapping("/user-games")
@RequiredArgsConstructor
@Tag(name = "User Games (Lists)", description = "Operations related to users' personal game tracking lists")
public class UserGameController {

    private final UserGameService userGameService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add to list (Logged-In Only)", description = "Adds a game to logged-in user's list." +
            "Fails if entry already exists, use patch to update an existent entry.")
    public ApiResponse<UserGameResponse> create(Authentication auth,
            @Valid @RequestBody UserGameCreateRequest request) {
        return ApiResponse.success(userGameService.create(auth.getName(), request),
                "Game added to list " + "successfully");
    }

    @PatchMapping("/{gameId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update list entry (Logged-In Only)", description =
            "Updates status or rating of a game in " + "logged-in user's list")
    public ApiResponse<UserGameResponse> patch(Authentication auth, @PathVariable Long gameId,
            @Valid @RequestBody UserGamePatchRequest request) {
        return ApiResponse.success(userGameService.patch(auth.getName(), gameId, request),
                "List updated " + "successfully");
    }

    @GetMapping("/{gameId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check a specific list entry (Logged-In Only)", description = "Retrieves specific list " +
            "entry" + " " + "details")
    public ApiResponse<UserGameResponse> getUserGame(Authentication auth, @PathVariable Long gameId) {
        return ApiResponse.success(userGameService.getUserGame(auth.getName(), gameId));
    }

    @DeleteMapping("/{gameId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove list entry (Logged-In Only)", description = "Removes a game from " + "logged-in " +
            "user's list completely")
    public ApiResponse<String> delete(@PathVariable Long gameId, Authentication auth) {
        userGameService.delete(auth.getName(), gameId);
        return ApiResponse.success(null, "Game removed from list successfully");
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "View any user list (Public)", description = "Retrieve any user's game list")
    public ApiResponse<Page<UserGameResponse>> getByUserId(@PathVariable Long userId,
            @RequestParam(required = false) String status, @RequestParam(required = false) String genre,
            @RequestParam(required = false) String title, @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @ParameterObject @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC, size = 25) Pageable pageable) {
        return ApiResponse.success(
                userGameService.getByUserId(userId, status, genre, title, startYear, endYear, pageable));
    }

    @GetMapping("/{userId}/stats")
    @Operation(summary = "Get user list stats (Public)", description = "Retrieves statistics for a user's list")
    public ApiResponse<UserGameStatsResponse> getStats(@PathVariable Long userId) {
        return ApiResponse.success(userGameService.getStatsByUserId(userId));
    }
}