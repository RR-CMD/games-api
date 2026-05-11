package com.osamah.games.user;

import com.osamah.games.common.ApiResponse;
import com.osamah.games.user.dto.UserResponse;
import com.osamah.games.user.dto.UserSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "Operations related to viewing and managing users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin Only)", description =
            "Retrieves a complete list of all registered " + "users in the system")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.success(userService.getAllUsers());
    }

    @GetMapping("/search")
    @Operation(summary = "Search users (Public)", description = "Search for a user by their username. Returns a " +
            "paginated list.")
    public ApiResponse<Page<UserSearchResponse>> searchUsers(@RequestParam String username,
            @ParameterObject @PageableDefault(sort = "username", direction = Sort.Direction.ASC, size = 20) Pageable pageable) {
        return ApiResponse.success(userService.searchUsers(username, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (Admin Only)", description =
            "Permanently deletes a user from the database by " + "ID")
    public ApiResponse<String> deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ApiResponse.success(null, "User deleted successfully");
    }
}