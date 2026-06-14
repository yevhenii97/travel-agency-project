package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.user.TransactionsDTO;
import com.epam.finaltask.mapper.interfaces.TransactionMapper;
import com.epam.finaltask.model.entities.BalanceTransaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapperImp implements TransactionMapper {
    @Override
    public TransactionsDTO toTransactionDto(BalanceTransaction balanceTransaction) {
        return TransactionsDTO.builder()
                .type(balanceTransaction.getType().name())
                .amount(balanceTransaction.getAmount().doubleValue())
                .createdAt(balanceTransaction.getCreatedAt())
                .build();
    }
}
