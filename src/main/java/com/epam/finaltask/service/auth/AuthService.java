package com.epam.finaltask.service.auth;

import com.epam.finaltask.dto.auth.LoginRequest;
import com.epam.finaltask.dto.auth.RegisterRequest;
import com.epam.finaltask.dto.user.UserResponseDTO;

public interface AuthService {
    UserResponseDTO register(RegisterRequest request);
    UserResponseDTO login(LoginRequest request);
}
