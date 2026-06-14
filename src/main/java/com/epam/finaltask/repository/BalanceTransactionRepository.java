package com.epam.finaltask.repository;

import com.epam.finaltask.model.entities.BalanceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, UUID> {

    List<BalanceTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);

}
