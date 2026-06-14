package com.epam.finaltask.mapper.interfaces;

import com.epam.finaltask.dto.user.TransactionsDTO;
import com.epam.finaltask.model.entities.BalanceTransaction;

public interface TransactionMapper {
    TransactionsDTO toTransactionDto(BalanceTransaction balanceTransaction);
}
