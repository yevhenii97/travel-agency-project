package com.epam.finaltask.repository;

import java.util.Optional;
import java.util.UUID;

import com.epam.finaltask.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.epam.finaltask.model.entities.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);
    Optional<User> findUserByUsername(String username);
    Page<User> findAll(Pageable pageable);
    Page<User> findAllByRole(Role role, Pageable pageable);
}
