package com.epam.finaltask.service;

import com.epam.finaltask.dto.user.TransactionsDTO;
import com.epam.finaltask.mapper.interfaces.TransactionMapper;
import com.epam.finaltask.model.entities.BalanceTransaction;
import com.epam.finaltask.model.entities.User;
import com.epam.finaltask.repository.BalanceTransactionRepository;
import com.epam.finaltask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;
    private final BalanceTransactionRepository balanceTransactionRepository;

    @Override
    public List<TransactionsDTO> getOwnTransactionsHistory(String username) {
        log.info("Finding own transactions history for username={}", username);

        User userEntity = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Cannot change password. User not found: username={}", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        List<BalanceTransaction> balanceTransaction = balanceTransactionRepository.findByUserIdOrderByCreatedAtDesc(userEntity.getId());

        if (balanceTransaction.isEmpty()) {
            log.warn("There are no transactions for userId={}", userEntity.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transactions not found");
        }

        return balanceTransaction.stream().map(transactionMapper::toTransactionDto).toList();

    }

    @Override
    public List<TransactionsDTO> getTransactionsHistoryByUserId(String userId) {
        log.info("Finding transactions history for userId={}", userId);

        List<BalanceTransaction> balanceTransaction = balanceTransactionRepository.findByUserIdOrderByCreatedAtDesc(UUID.fromString(userId));

        if (balanceTransaction.isEmpty()) {
            log.warn("There are no transactions for userId={}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transactions not found");
        }

        return balanceTransaction.stream().map(transactionMapper::toTransactionDto).toList();
    }
}
