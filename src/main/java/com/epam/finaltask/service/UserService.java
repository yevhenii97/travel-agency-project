package com.epam.finaltask.service;

import java.util.List;
import java.util.UUID;

import com.epam.finaltask.dto.user.*;
import org.springframework.data.domain.Pageable;

public interface UserService {
    List<UserResponseDTO> getAllUsers(Pageable pageable);
    UserResponseDTO updateUser(String username, UserUpdateRequestDTO userDTO);
    UserResponseDTO getUserByUsername(String username);
    UserResponseDTO changeAccountStatusById(String userId, ChangeUserStatusDTO userDTO);
    UserResponseDTO changeAccountStatusByUsername(String username, ChangeUserStatusDTO userDTO);
    UserResponseDTO getUserById(UUID id);
    UserResponseDTO setRoleManagerForUser(String userId);
    void changePassword(String username, ChangePasswordRequestDTO request);
    UserResponseDTO depositOwnBalance(String username, DepositRequestDTO depositRequestDTO);
    UserResponseDTO depositUserBalance(String userId, DepositRequestDTO depositRequestDTO);
}
