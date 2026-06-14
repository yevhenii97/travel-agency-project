package com.epam.finaltask.service.auth;

import com.epam.finaltask.dto.user.UserResponseDTO;
import com.epam.finaltask.dto.auth.LoginRequest;
import com.epam.finaltask.dto.auth.RegisterRequest;
import com.epam.finaltask.mapper.UserMapperImpl;
import com.epam.finaltask.model.enums.Role;
import com.epam.finaltask.model.entities.User;
import com.epam.finaltask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapperImpl userMapper;

    public UserResponseDTO register(RegisterRequest request) {
        log.info("Registering new user: username={}", request.getUsername());

        if (userRepository.findUserByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(Role.USER);
        user.setBalance(new BigDecimal("0.0"));
        user.setActive(true);
        log.debug("Creating user entity: username={}, role={}, phoneNumber={}", user.getUsername(), user.getRole(), user.getPhoneNumber());

        User saved = userRepository.save(user);
        log.info("User registered successfully: userId={}, username={}", saved.getId(), saved.getUsername());

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(saved.getId().toString());
        dto.setUsername(saved.getUsername());
        dto.setPhoneNumber(saved.getPhoneNumber());
        dto.setRole(saved.getRole().name());
        dto.setBalance(Double.parseDouble(saved.getBalance().toString()));
        dto.setActive(saved.isActive());

        return dto;
    }

    public UserResponseDTO login(LoginRequest request) {
        log.info("Login attempt: username={}", request.getUsername());

        User user = userRepository.findUserByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed. User not found: username={}", request.getUsername());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed. Invalid password: username={}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (!user.isActive()) {
            log.warn("Login failed. User is blocked: username={}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is blocked");
        }

        log.info("Login successful: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        return userMapper.toUserUserResponseDTO(user);
    }

}
