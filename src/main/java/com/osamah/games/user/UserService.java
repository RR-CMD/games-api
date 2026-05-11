package com.osamah.games.user;

import com.osamah.games.auth.OtpRepository;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.review.ReviewService;
import com.osamah.games.user.dto.UserResponse;
import com.osamah.games.user.dto.UserSearchResponse;
import com.osamah.games.usergame.UserGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final UserGameService userGameService;
    private final OtpRepository otpRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<UserSearchResponse> searchUsers(String username, Pageable pageable) {
        return userRepository.searchUsers(username, pageable)
                .map(this::toSearchResponse);
    }


    @Transactional
    public void deleteUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userGameService.deleteAllForUser(id);
        reviewService.deleteAllForUser(id);
        otpRepository.deleteByEmail(user.getEmail());

        userRepository.delete(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    private UserSearchResponse toSearchResponse(User user) {
        return new UserSearchResponse(user.getId(), user.getUsername());
    }
}
