package com.epam.finaltask.service;

import com.epam.finaltask.dto.user.TransactionsDTO;

import java.util.List;

public interface TransactionService {
    List<TransactionsDTO> getOwnTransactionsHistory(String name);
    List<TransactionsDTO> getTransactionsHistoryByUserId(String userId);
}
