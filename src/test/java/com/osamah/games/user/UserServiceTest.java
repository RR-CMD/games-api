package com.osamah.games.user;

import com.osamah.games.auth.OtpRepository;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.review.ReviewService;
import com.osamah.games.user.dto.UserResponse;
import com.osamah.games.user.dto.UserSearchResponse;
import com.osamah.games.usergame.UserGameRepository;
import com.osamah.games.usergame.UserGameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserGameRepository userGameRepository;
    @Mock
    private OtpRepository otpRepository;
    @Mock
    private UserGameService userGameService;
    @Mock
    private ReviewService reviewService;


    @InjectMocks
    private UserService userService;

    @Test
    void deleteUserById_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUserById(1L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUserById_ShouldCascadeDeleteEverything_WhenUserExists() {
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        userService.deleteUserById(1L);

        verify(userGameService).deleteAllForUser(1L);
        verify(reviewService).deleteAllForUser(1L);
        verify(otpRepository).deleteByEmail("test@test.com");
        verify(userRepository).delete(mockUser);
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsersExist() {

        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_ShouldReturnUserResponseList_WhenUsersExist() {
        User user1 = new User();
        ReflectionTestUtils.setField(user1, "id", 1L);
        user1.setUsername("osamah");
        user1.setEmail("osamah@mail.com");

        User user2 = new User();
        ReflectionTestUtils.setField(user2, "id", 2L);
        user2.setUsername("gamer1");
        user2.setEmail("gamer1@mail.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst()
                .username()).isEqualTo("osamah");
        assertThat(result.getLast()
                .email()).isEqualTo("gamer1@mail.com");
    }

    @Test
    void searchUsers_ShouldReturnPageOfSearchResponses() {
        String query = "osa";
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setUsername("osamah");

        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.searchUsers(eq(query), eq(pageable))).thenReturn(userPage);

        Page<UserSearchResponse> result = userService.searchUsers(query, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()
                .getFirst()
                .username()).isEqualTo("osamah");
        verify(userRepository).searchUsers(query, pageable);
    }
}