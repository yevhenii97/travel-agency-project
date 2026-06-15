package com.epam.finaltask.services;

import com.epam.finaltask.mapper.UserMapperImpl;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.auth.AuthServiceImp;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.epam.finaltask.dto.auth.LoginRequest;
import com.epam.finaltask.dto.auth.RegisterRequest;
import com.epam.finaltask.dto.user.UserResponseDTO;
import com.epam.finaltask.model.entities.User;
import com.epam.finaltask.model.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImpTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapperImpl userMapper;

    @InjectMocks
    private AuthServiceImp authService;

    @Test
    @DisplayName("Should register new user successfully")
    void test1() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user01");
        request.setPassword("Password123");
        request.setPhoneNumber("+380661234567");

        User savedUser = createUser();
        savedUser.setPassword("encodedPassword");

        when(userRepository.findUserByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDTO result = authService.register(request);

        assertNotNull(result);
        assertEquals(savedUser.getId().toString(), result.getId());
        assertEquals(savedUser.getUsername(), result.getUsername());
        assertEquals(savedUser.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(Role.USER.name(), result.getRole());
        assertEquals(0.0, result.getBalance());
        assertTrue(result.getActive());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User userToSave = userCaptor.getValue();

        assertEquals("user01", userToSave.getUsername());
        assertEquals("encodedPassword", userToSave.getPassword());
        assertEquals("+380661234567", userToSave.getPhoneNumber());
        assertEquals(Role.USER, userToSave.getRole());
        assertEquals(new BigDecimal("0.0"), userToSave.getBalance());
        assertTrue(userToSave.isActive());

        verify(userRepository).findUserByUsername(request.getUsername());
        verify(passwordEncoder).encode(request.getPassword());
    }

    @Test
    @DisplayName("Should throw BAD_REQUEST when registering existing user")
    void test2() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user01");
        request.setPassword("Password123");
        request.setPhoneNumber("+380661234567");

        User existingUser = createUser();

        when(userRepository.findUserByUsername(request.getUsername())).thenReturn(Optional.of(existingUser));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("User already exists", ex.getReason());

        verify(userRepository).findUserByUsername(request.getUsername());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login user successfully")
    void test3() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user01");
        request.setPassword("Password123");

        User user = createUser();
        user.setPassword("encodedPassword");
        user.setActive(true);

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(user.getId().toString());
        responseDTO.setUsername(user.getUsername());
        responseDTO.setPhoneNumber(user.getPhoneNumber());
        responseDTO.setRole(user.getRole().name());
        responseDTO.setBalance(user.getBalance().doubleValue());
        responseDTO.setActive(user.isActive());

        when(userRepository.findUserByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(userMapper.toUserResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = authService.login(request);

        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(Role.USER.name(), result.getRole());
        assertTrue(result.getActive());

        verify(userRepository).findUserByUsername(request.getUsername());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
        verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    @DisplayName("Should throw UNAUTHORIZED when login user not found")
    void test4() {
        LoginRequest request = new LoginRequest();
        request.setUsername("missingUser");
        request.setPassword("Password123");

        when(userRepository.findUserByUsername(request.getUsername())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Invalid credentials", ex.getReason());

        verify(userRepository).findUserByUsername(request.getUsername());
        verify(passwordEncoder, never()).matches(any(), any());
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should throw UNAUTHORIZED when login password is incorrect")
    void test5() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user01");
        request.setPassword("WrongPassword");

        User user = createUser();
        user.setPassword("encodedPassword");
        user.setActive(true);

        when(userRepository.findUserByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Invalid credentials", ex.getReason());

        verify(userRepository).findUserByUsername(request.getUsername());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should throw FORBIDDEN when blocked user tries to login")
    void test6() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user01");
        request.setPassword("Password123");

        User user = createUser();
        user.setPassword("encodedPassword");
        user.setActive(false);

        when(userRepository.findUserByUsername(request.getUsername())).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("User is blocked", ex.getReason());

        verify(userRepository).findUserByUsername(request.getUsername());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
        verifyNoInteractions(userMapper);
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("user01")
                .password("encodedPassword")
                .phoneNumber("+380661234567")
                .role(Role.USER)
                .balance(new BigDecimal("0.0"))
                .active(true)
                .build();
    }
}
