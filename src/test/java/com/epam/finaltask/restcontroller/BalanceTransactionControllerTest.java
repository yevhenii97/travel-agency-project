package com.epam.finaltask.restcontroller;

import com.epam.finaltask.config.security.SecurityConfig;
import com.epam.finaltask.dto.user.TransactionsDTO;
import com.epam.finaltask.model.enums.TransactionType;
import com.epam.finaltask.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BalanceTransactionController.class)
@Import(SecurityConfig.class)
@EnableMethodSecurity
class BalanceTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    @DisplayName("Should get own transactions history for user")
    @WithMockUser(username = "user01", roles = "USER")
    void test1() throws Exception {
        TransactionsDTO transaction = createTransactionDTO();

        when(transactionService.getOwnTransactionsHistory("user01"))
                .thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/transactions/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));

        verify(transactionService).getOwnTransactionsHistory("user01");
    }

    @Test
    @DisplayName("Should deny own transactions history for admin")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void test2() throws Exception {
        mockMvc.perform(get("/api/transactions/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should deny own transactions history for anonymous user")
    void test3() throws Exception {
        mockMvc.perform(get("/api/transactions/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get transactions history by user id for admin")
    @WithMockUser(roles = "ADMIN")
    void test4() throws Exception {
        String userId = UUID.randomUUID().toString();

        TransactionsDTO transaction = createTransactionDTO();

        when(transactionService.getTransactionsHistoryByUserId(userId))
                .thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/transactions/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));

        verify(transactionService).getTransactionsHistoryByUserId(userId);
    }

    @Test
    @DisplayName("Should deny transactions history by user id for regular user")
    @WithMockUser(roles = "USER")
    void test5() throws Exception {
        String userId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/transactions/user/{userId}", userId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should deny transactions history by user id for manager")
    @WithMockUser(roles = "MANAGER")
    void test6() throws Exception {
        String userId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/transactions/user/{userId}", userId))
                .andExpect(status().isForbidden());
    }

    private TransactionsDTO createTransactionDTO() {
        TransactionsDTO dto = new TransactionsDTO();
        dto.setAmount(100.0);
        dto.setType(TransactionType.DEPOSIT.name());
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
}
