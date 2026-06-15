package com.epam.finaltask.restcontroller;

import com.epam.finaltask.config.security.SecurityConfig;
import com.epam.finaltask.dto.user.*;
import com.epam.finaltask.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@WebMvcTest(UserRestController.class)
@Import(SecurityConfig.class)
@EnableMethodSecurity
public class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Should get user by username")
    @WithMockUser(username = "user01", roles = "USER")
    void test1() throws Exception {
        UserResponseDTO response = createUserResponseDTO("user01", "USER");

        when(userService.getUserByUsername("user01"))
                .thenReturn(response);

        mockMvc.perform(get("/api/users/user01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user01"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getUserByUsername("user01");
    }

    @Test
    @DisplayName("Should allow admin to get any user by username")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void test2() throws Exception {
        UserResponseDTO response = createUserResponseDTO("user01", "USER");

        when(userService.getUserByUsername("user01"))
                .thenReturn(response);

        mockMvc.perform(get("/api/users/user01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user01"));

        verify(userService).getUserByUsername("user01");
    }

    @Test
    @DisplayName("Should deny getting another user for non-admin")
    @WithMockUser(username = "user02", roles = "USER")
    void test3() throws Exception {
        mockMvc.perform(get("/api/users/user01"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get all users for admin")
    @WithMockUser(roles = "ADMIN")
    void test4() throws Exception {
        UserResponseDTO user = createUserResponseDTO("user01", "USER");

        PageRequest pageable = PageRequest.of(0, 10);

        Page<UserResponseDTO> usersPage =
                new PageImpl<>(List.of(user), pageable, 1);

        when(userService.getAllUsers(any(Pageable.class)))
                .thenReturn(usersPage);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("user01"))
                .andExpect(jsonPath("$.content[0].role").value("USER"));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    @DisplayName("Should deny getting all users for non-admin")
    @WithMockUser(roles = "USER")
    void test5() throws Exception {
        mockMvc.perform(get("/api/users").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get user by id for admin")
    @WithMockUser(roles = "ADMIN")
    void test6() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponseDTO response = createUserResponseDTO("user01", "USER");
        response.setId(userId.toString());

        when(userService.getUserById(userId))
                .thenReturn(response);

        mockMvc.perform(get("/api/users/id/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("user01"));

        verify(userService).getUserById(userId);
    }

    @Test
    @DisplayName("Should update own user profile")
    @WithMockUser(username = "user01", roles = "USER")
    void test7() throws Exception {
        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setPhoneNumber("+380661111111");

        UserResponseDTO response = createUserResponseDTO("user01", "USER");
        response.setPhoneNumber("+380661111111");

        when(userService.updateUser(eq("user01"), any(UserUpdateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/users/user01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("+380661111111"));

        verify(userService).updateUser(eq("user01"), any(UserUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("Should deny updating another user for non-admin")
    @WithMockUser(username = "user02", roles = "USER")
    void test8() throws Exception {
        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setPhoneNumber("+380661111111");

        mockMvc.perform(patch("/api/users/user01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should change own password")
    @WithMockUser(username = "user01", roles = "USER")
    void test9() throws Exception {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setOldPassword("OldPassword123");
        request.setNewPassword("NewPassword123");

        doNothing().when(userService)
                .changePassword(eq("user01"), any(ChangePasswordRequestDTO.class));

        mockMvc.perform(patch("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(eq("user01"), any(ChangePasswordRequestDTO.class));
    }

    @Test
    @DisplayName("Should change user status by id for admin")
    @WithMockUser(roles = "ADMIN")
    void test10() throws Exception {
        String userId = UUID.randomUUID().toString();

        ChangeUserStatusDTO request = new ChangeUserStatusDTO();
        request.setActive(false);

        UserResponseDTO response = createUserResponseDTO("user01", "USER");
        response.setActive(false);

        when(userService.changeAccountStatusById(eq(userId), any(ChangeUserStatusDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/users/{userId}/status", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(userService).changeAccountStatusById(eq(userId), any(ChangeUserStatusDTO.class));
    }

    @Test
    @DisplayName("Should change user status by username for admin")
    @WithMockUser(roles = "ADMIN")
    void test11() throws Exception {
        ChangeUserStatusDTO request = new ChangeUserStatusDTO();
        request.setActive(false);

        UserResponseDTO response = createUserResponseDTO("user01", "USER");
        response.setActive(false);

        when(userService.changeAccountStatusByUsername(eq("user01"), any(ChangeUserStatusDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/users/username/user01/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(userService).changeAccountStatusByUsername(eq("user01"), any(ChangeUserStatusDTO.class));
    }

    @Test
    @DisplayName("Should set manager role for user")
    @WithMockUser(roles = "ADMIN")
    void test12() throws Exception {
        String userId = UUID.randomUUID().toString();

        UserResponseDTO response = createUserResponseDTO("user01", "MANAGER");

        when(userService.setRoleManagerForUser(userId))
                .thenReturn(response);

        mockMvc.perform(patch("/api/users/{userId}/manager", userId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"));

        verify(userService).setRoleManagerForUser(userId);
    }

    @Test
    @DisplayName("Should deposit own balance")
    @WithMockUser(username = "user01", roles = "USER")
    void test13() throws Exception {
        DepositRequestDTO request = new DepositRequestDTO();
        request.setAmount(BigDecimal.valueOf(100));

        UserResponseDTO response = createUserResponseDTO("user01", "USER");
        response.setBalance(1100.0);

        when(userService.depositOwnBalance(eq("user01"), any(DepositRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/users/me/balance/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1100.0));

        verify(userService).depositOwnBalance(eq("user01"), any(DepositRequestDTO.class));
    }

    @Test
    @DisplayName("Should deposit user balance for admin")
    @WithMockUser(roles = "ADMIN")
    void test14() throws Exception {
        String userId = UUID.randomUUID().toString();

        DepositRequestDTO request = new DepositRequestDTO();
        request.setAmount(BigDecimal.valueOf(100));

        UserResponseDTO response = createUserResponseDTO("user01", "USER");
        response.setBalance(1100.0);

        when(userService.depositUserBalance(eq(userId), any(DepositRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/users/{userId}/balance/deposit", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1100.0));

        verify(userService).depositUserBalance(eq(userId), any(DepositRequestDTO.class));
    }

    private UserResponseDTO createUserResponseDTO(String username, String role) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(UUID.randomUUID().toString());
        dto.setUsername(username);
        dto.setPhoneNumber("+380661234567");
        dto.setRole(role);
        dto.setBalance(1000.0);
        dto.setActive(true);
        return dto;
    }
}
