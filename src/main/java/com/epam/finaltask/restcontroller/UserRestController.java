package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.user.*;
import com.epam.finaltask.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "User API Controller",
        description = "User API for managing users"
)
public class UserRestController {

    private final UserService userService;

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == #username")
    @Operation(
            summary = "Get user by username",
            description = "Returns user information by username"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get user successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<UserResponseDTO> getUserByUsername(@PathVariable String username) {
        log.info("Request to get user by username: {}", username);
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all users",
            description = "Returns user information by username"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get users successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            @PageableDefault(size = 10, sort = "username") Pageable pageable
    ) {
        log.info("Request to get users with USER role");
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user by id",
            description = "Returns user information by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get user successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        log.info("Request to get user by userId={}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == #username")
    @Operation(
            summary = "Update user by username"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User was updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable String username,
            @Valid @RequestBody UserUpdateRequestDTO userDTO
    ) {
        log.info("Request to update user with username={}, request body : {}", username, userDTO);
        return ResponseEntity.ok(userService.updateUser(username, userDTO));
    }

    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Change user password",
            description = "Allows user to change own password"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or wrong old password"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequestDTO userDTO
    ) {
        String username = authentication.getName();
        log.info("Request to change password for user={}", username);
        userService.changePassword(username, userDTO);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Block or unblock user by userId",
            description = "Changes user account status"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<UserResponseDTO> changeStatusByUserId(
            @PathVariable String userId,
            @RequestBody ChangeUserStatusDTO userDTO) {
        log.info("Request to change status by userId={} with status={}", userId, userDTO.isActive());
        return ResponseEntity.ok(userService.changeAccountStatusById(userId, userDTO));
    }

    @PatchMapping("/username/{username}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Block or unblock user by username",
            description = "Changes user account status"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<UserResponseDTO> changeStatusByUsername(
            @PathVariable String username,
            @RequestBody ChangeUserStatusDTO userDTO) {
        log.info("Request to change status by username={} with status={}", username, userDTO.isActive());
        return ResponseEntity.ok(userService.changeAccountStatusByUsername(username, userDTO));
    }

    @PatchMapping("/{userId}/manager")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set MANAGER role for user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<UserResponseDTO> setManagerRoleForUser(
            @PathVariable String userId) {
        log.info("Request to set MANAGER role for userid={}", userId);
        return ResponseEntity.ok(userService.setRoleManagerForUser(userId));
    }

    @PostMapping("/me/balance/deposit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add deposit for own balance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposit changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<UserResponseDTO> depositOwnBalance(
            Authentication authentication,
            @Valid @RequestBody DepositRequestDTO depositRequestDTO
    ) {
        log.info("Request to add deposit for own balance for username={}", authentication.getName());

        return ResponseEntity.ok(userService.depositOwnBalance(authentication.getName(), depositRequestDTO));
    }

    @PostMapping("/{userId}/balance/deposit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add deposit for own balance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposit changed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<UserResponseDTO> depositOwnBalance(
            @PathVariable String userId,
            @Valid @RequestBody DepositRequestDTO depositRequestDTO
    ) {
        log.info("Request to add deposit for own balance for userId={}", userId);

        return ResponseEntity.ok(userService.depositUserBalance(userId, depositRequestDTO));
    }
}
