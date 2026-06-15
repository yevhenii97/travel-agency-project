package com.epam.finaltask.services;

import com.epam.finaltask.dto.user.TransactionsDTO;
import com.epam.finaltask.mapper.interfaces.TransactionMapper;
import com.epam.finaltask.model.entities.BalanceTransaction;
import com.epam.finaltask.model.entities.User;
import com.epam.finaltask.model.enums.Role;
import com.epam.finaltask.model.enums.TransactionType;
import com.epam.finaltask.repository.BalanceTransactionRepository;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.TransactionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private BalanceTransactionRepository balanceTransactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    @DisplayName("Should return own transactions history")
    void test1() {
        User user = createUser();

        BalanceTransaction transaction = createTransaction(user);
        TransactionsDTO dto = new TransactionsDTO();

        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(balanceTransactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of(transaction));
        when(transactionMapper.toTransactionDto(transaction)).thenReturn(dto);

        List<TransactionsDTO> result = transactionService.getOwnTransactionsHistory(user.getUsername());

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository).findUserByUsername(user.getUsername());
        verify(balanceTransactionRepository).findByUserIdOrderByCreatedAtDesc(user.getId());
        verify(transactionMapper).toTransactionDto(transaction);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when user does not exist for own transactions history")
    void test2() {
        String username = "missingUser";

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.getOwnTransactionsHistory(username)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());

        verify(userRepository).findUserByUsername(username);
        verify(balanceTransactionRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
        verifyNoInteractions(transactionMapper);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when own transactions history is empty")
    void test3() {
        User user = createUser();

        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(balanceTransactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.getOwnTransactionsHistory(user.getUsername())
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Transactions not found", ex.getReason());

        verify(userRepository).findUserByUsername(user.getUsername());
        verify(balanceTransactionRepository).findByUserIdOrderByCreatedAtDesc(user.getId());
        verifyNoInteractions(transactionMapper);
    }

    @Test
    @DisplayName("Should return transactions history by user id")
    void test4() {
        UUID userId = UUID.randomUUID();

        User user = createUser();
        user.setId(userId);

        BalanceTransaction transaction = createTransaction(user);
        TransactionsDTO dto = new TransactionsDTO();

        when(balanceTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(transaction));
        when(transactionMapper.toTransactionDto(transaction)).thenReturn(dto);

        List<TransactionsDTO> result = transactionService.getTransactionsHistoryByUserId(userId.toString());

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(balanceTransactionRepository).findByUserIdOrderByCreatedAtDesc(userId);
        verify(transactionMapper).toTransactionDto(transaction);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when transactions history by user id is empty")
    void test5() {
        UUID userId = UUID.randomUUID();

        when(balanceTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.getTransactionsHistoryByUserId(userId.toString())
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Transactions not found", ex.getReason());

        verify(balanceTransactionRepository).findByUserIdOrderByCreatedAtDesc(userId);
        verifyNoInteractions(transactionMapper);
        verifyNoInteractions(userRepository);
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("user01")
                .password("encodedPassword")
                .phoneNumber("+380661234567")
                .role(Role.USER)
                .active(true)
                .balance(BigDecimal.valueOf(1000))
                .build();
    }

    private BalanceTransaction createTransaction(User user) {
        return BalanceTransaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .amount(BigDecimal.valueOf(100))
                .type(TransactionType.DEPOSIT)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
