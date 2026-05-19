package com.osamah.games.game;

import com.osamah.games.common.ApiResponse;
import com.osamah.games.game.dto.GameResponse;
import com.osamah.games.game.dto.GameSummaryResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "Operations related to viewing and managing the public game library")
public class GameController {

    private final GameService gameService;

    @GetMapping
    @Operation(summary = "Search for games (Public)", description = "Retrieve a paginated list of games with " +
            "optional" + " " + "filtering by title, genre, year, etc.")
    public ApiResponse<Page<GameSummaryResponse>> searchGames(@Valid @RequestParam(required = false) String title,
            @RequestParam(required = false) List<String> genres, @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear, @RequestParam(required = false) Double minScore,
            @RequestParam(required = false) List<String> platforms,
            @ParameterObject @PageableDefault(sort = "totalAdded", direction = Sort.Direction.ASC, size = 50) Pageable pageable) {
        return ApiResponse.success(
                gameService.searchGames(title, genres, startYear, endYear, minScore, platforms, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full game details (Public)", description =
            "Retrieve all details and global stats for " + "a" + " " + "specific game by its ID")
    public ApiResponse<GameResponse> getGameDetails(@PathVariable Long id) {
        return ApiResponse.success(gameService.getGameDetails(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new game (Admin Only)", description =
            "Fetches game details from RAWG API via slug " + "and" + " saves it to the local database")
    public ApiResponse<GameResponse> createGame(@RequestParam String slug) {
        return ApiResponse.success(gameService.createGame(slug), "Game created successfully");
    }
}