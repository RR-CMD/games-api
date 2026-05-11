package com.osamah.games.user;

import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.security.CustomAuthenticationEntryPoint;
import com.osamah.games.security.JwtService;
import com.osamah.games.security.SecurityConfig;
import com.osamah.games.user.dto.UserResponse;
import com.osamah.games.user.dto.UserSearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    //GET ALL ENDPOINT TESTS

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnList_WhenAdmin() throws Exception {
        UserResponse user = new UserResponse(1L, "adminUser", "admin@mail.com");
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("adminUser"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_ShouldReturn403_WhenUser() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void getAllUsers_ShouldReturn401_WhenAnonymous() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    //SEARCH ENDPOINT TESTS

    @Test
    @WithAnonymousUser
    void searchUsers_ShouldReturnPage_WhenPublic() throws Exception {
        UserSearchResponse searchResult = new UserSearchResponse(1L, "osamah");
        when(userService.searchUsers(eq("osamah"), any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(searchResult)));

        mockMvc.perform(get("/users/search").param("username", "osamah"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].username").value("osamah"));
    }

    @Test
    @WithAnonymousUser
    void searchUsers_ShouldReturn400_WhenUsernameMissing() throws Exception {
        mockMvc.perform(get("/users/search"))
                .andExpect(status().isBadRequest());
    }

    //DELETE ENDPOINT TESTS

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldSucceed_WhenAdmin() throws Exception {

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).deleteUserById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_ShouldReturn403_WhenUserAttemptsDelete() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void deleteUser_ShouldReturn401_WhenAnonymousAttemptsDelete() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {

        doThrow(new ResourceNotFoundException("User not found")).when(userService)
                .deleteUserById(1L);


        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNotFound());
    }
}