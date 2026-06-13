package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
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
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
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
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String username,
            @Valid @RequestBody UserDTO userDTO
    ) {
        log.info("Request body for update user request: {}", userDTO);
        return ResponseEntity.ok(userService.updateUser(username, userDTO));
    }

    @PatchMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Block or unblock user",
            description = "Changes user account status"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<UserDTO> changeStatus(@Valid @RequestBody UserDTO userDTO) {
        log.info("Request body for changeStatus request: {}", userDTO);
        return ResponseEntity.ok(userService.changeAccountStatus(userDTO));
    }
}
