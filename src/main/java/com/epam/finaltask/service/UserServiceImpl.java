package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.epam.finaltask.dto.user.*;
import com.epam.finaltask.mapper.interfaces.UserMapper;
import com.epam.finaltask.model.entities.BalanceTransaction;
import com.epam.finaltask.model.entities.User;
import com.epam.finaltask.model.enums.Role;
import com.epam.finaltask.model.enums.TransactionType;
import com.epam.finaltask.repository.BalanceTransactionRepository;
import com.epam.finaltask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class  UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BalanceTransactionRepository balanceTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        Page<UserResponseDTO> users = userRepository
                .findAllByRole(Role.USER, pageable)
                .map(userMapper::toUserResponseDTO);

        log.debug("Found {} users", users.getTotalElements());

        return users;
    }

    @Override
    public UserResponseDTO updateUser(String username, UserUpdateRequestDTO userDTO) {
        log.info("Updating user profile: username={}", username);
        log.debug("New phone number for username={}: {}", username, userDTO.getPhoneNumber());

        User userEntity = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Cannot update user. User not found: username={}", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        userEntity.setPhoneNumber(userDTO.getPhoneNumber());
        User saved = userRepository.save(userEntity);
        log.info("User updated successfully: username={}", saved.getUsername());

        return userMapper.toUserResponseDTO(saved);
    }

    @Override
    public UserResponseDTO getUserByUsername(String username) {
        log.info("Finding user by username={}", username);

        User userEntity = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: username={}", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        log.debug("User found: id={}, username={}, role={}", userEntity.getId(), userEntity.getUsername(), userEntity.getRole());
        return userMapper.toUserResponseDTO(userEntity);
    }

    @Override
    public UserResponseDTO changeAccountStatusById(String userId, ChangeUserStatusDTO changeUserStatusDTO) {

        log.info("Changing account status by userId={}, active={}",
                userId,
                changeUserStatusDTO.isActive());

        User existingUser = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    log.warn("Cannot change account status. User not found: userId={}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        existingUser.setActive(changeUserStatusDTO.isActive());

        User saved = userRepository.save(existingUser);

        log.info("Account status changed successfully: userId={}, active={}", saved.getId(), saved.isActive());
        return userMapper.toUserResponseDTO(saved);
    }

    @Override
    public UserResponseDTO changeAccountStatusByUsername(String username, ChangeUserStatusDTO changeUserStatusDTO) {
        log.info("Changing account status by username={}, active={}",
                username,
                changeUserStatusDTO.isActive());

        User existingUser = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Cannot change account status. User not found: username={}", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        existingUser.setActive(changeUserStatusDTO.isActive());

        User saved = userRepository.save(existingUser);

        log.info("Account status changed successfully: username={}, active={}", saved.getUsername(), saved.isActive());
        return userMapper.toUserResponseDTO(saved);
    }

    @Override
    public UserResponseDTO getUserById(UUID id) {
        log.info("Finding user by id={}", id);

        User userEntity = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found: id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        log.debug("User found: username={}, role={}", userEntity.getUsername(), userEntity.getRole());
        return userMapper.toUserResponseDTO(userEntity);
    }

    @Override
    public UserResponseDTO setRoleManagerForUser(String userId) {
        log.info("Promoting user to MANAGER: userId={}", userId);

        User userEntity = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    log.warn("Cannot promote user. User not found: userId={}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        if (userEntity.getRole() == Role.ADMIN) {
            log.warn("Attempt to change ADMIN role: userId={}", userId);
            return userMapper.toUserResponseDTO(userEntity);
        }

        if (userEntity.getRole() != Role.MANAGER) {
            userEntity.setRole(Role.MANAGER);
        }

        User saved = userRepository.save(userEntity);

        log.info("User promoted successfully: userId={}, role={}", saved.getId(), saved.getRole());
        return userMapper.toUserResponseDTO(saved);
    }

    @Override
    public void changePassword(String username, ChangePasswordRequestDTO request) {
        log.info("Changing password for username={}", username);

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Cannot change password. User not found: username={}", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Invalid old password provided: username={}", username);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Old password is incorrect"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully: username={}", username);
    }

    @Override
    @Transactional
    public UserResponseDTO depositOwnBalance(String username, DepositRequestDTO depositRequestDTO) {
        log.info("Changing balance for username={}", username);

        User userEntity = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Cannot change password. User not found: username={}", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        if (!userEntity.isActive()) {
            log.warn("Blocked user tried to add deposit: userId={}, username={}",
                    userEntity.getId(), userEntity.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is blocked");
        }

        if (depositRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Deposit amount must be greater than zero"
            );
        }

        return deposit(userEntity, depositRequestDTO.getAmount());
    }

    @Override
    @Transactional
    public UserResponseDTO depositUserBalance(String userId, DepositRequestDTO depositRequestDTO) {
        log.info("Changing balance for userId={}", userId);

        User userEntity = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    log.warn("Cannot deposit balance. User not found: userId={}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        if (depositRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Deposit amount must be greater than zero"
            );
        }

        return deposit(userEntity, depositRequestDTO.getAmount());
    }

    private UserResponseDTO deposit(User userEntity, BigDecimal amount) {

        BigDecimal userCurrentBalance = userEntity.getBalance();
        BigDecimal userNewBalance = amount.add(userCurrentBalance);

        userEntity.setBalance(userNewBalance);

        User user = userRepository.save(userEntity);

        log.info(
                "Balance deposited successfully: username={}, amount={}, newBalance={}",
                userEntity.getUsername(),
                amount,
                userNewBalance
        );

        BalanceTransaction transaction =
                BalanceTransaction.builder()
                        .user(userEntity)
                        .amount(amount)
                        .type(TransactionType.DEPOSIT)
                        .createdAt(LocalDateTime.now())
                        .build();

        balanceTransactionRepository.save(transaction);

        return userMapper.toUserResponseDTO(user);
    }


}
