package com.epam.finaltask.service.auth;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.auth.LoginRequest;
import com.epam.finaltask.dto.auth.RegisterRequest;
import com.epam.finaltask.mapper.UserMapperImpl;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapperImpl userMapper;

    public UserDTO register(RegisterRequest request) {
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

        User saved = userRepository.save(user);

        UserDTO dto = new UserDTO();
        dto.setId(saved.getId().toString());
        dto.setUsername(saved.getUsername());
        dto.setPhoneNumber(saved.getPhoneNumber());
        dto.setRole(saved.getRole().name());
        dto.setBalance(Double.parseDouble(saved.getBalance().toString()));
        dto.setActive(saved.isActive());

        return dto;
    }

    public UserDTO login(LoginRequest request) {

        User user = userRepository.findUserByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return userMapper.toUserDTO(user);
    }

}
