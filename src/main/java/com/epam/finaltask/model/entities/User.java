package com.epam.finaltask.model.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.epam.finaltask.model.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Entity
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "\"user\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "user")
    private List<Voucher> vouchers;
    @Column(name = "phone_number")
    private String phoneNumber;
    private BigDecimal balance;
    @Column(name = "account_status")
    private boolean active;

    @OneToMany(mappedBy = "user")
    private List<BalanceTransaction> transactions = new ArrayList<>();;
}