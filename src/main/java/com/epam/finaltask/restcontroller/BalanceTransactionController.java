package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.user.TransactionsDTO;
import com.epam.finaltask.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Transactions API Controller",
        description = "Transactions API to manage transactions history"
)
public class BalanceTransactionController {

    private final TransactionService transactionService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Get own transactions history",
            description = "Returns transactions history for user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get transactions successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<List<TransactionsDTO>> getTransactionsHistory(Authentication authentication) {
        log.info("Request to get user by username: {}", authentication.getName());
        return ResponseEntity.ok(transactionService.getOwnTransactionsHistory(authentication.getName()));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get transactions history by userId",
            description = "Returns transactions history by userId"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get transactions successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public ResponseEntity<List<TransactionsDTO>> getTransactionsHistory(@PathVariable String userId) {
        log.info("Request to get user by userId: {}", userId);
        return ResponseEntity.ok(transactionService.getTransactionsHistoryByUserId(userId));
    }

}
