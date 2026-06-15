package com.epam.finaltask.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

import com.epam.finaltask.dto.user.*;
import com.epam.finaltask.model.entities.BalanceTransaction;
import com.epam.finaltask.model.entities.User;
import com.epam.finaltask.model.enums.Role;
import com.epam.finaltask.model.enums.TransactionType;
import com.epam.finaltask.repository.BalanceTransactionRepository;
import com.epam.finaltask.service.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.epam.finaltask.mapper.interfaces.UserMapper;
import com.epam.finaltask.repository.UserRepository;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BalanceTransactionRepository balanceTransactionRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Get all users when users exist - returns UserPage")
    void test1() {
        User user = createUser(Role.USER, true, BigDecimal.valueOf(100));

        UserResponseDTO dto = createUserResponseDTO(user);

        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> usersPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAllByRole(Role.USER, pageable)).thenReturn(usersPage);
        when(userMapper.toUserResponseDTO(user)).thenReturn(dto);

        Page<UserResponseDTO> result = userService.getAllUsers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(user.getUsername(), result.getContent().get(0).getUsername());

        verify(userRepository).findAllByRole(Role.USER, pageable);
        verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    @DisplayName("Get user by username when user exists - returns user")
    void test2() {
        User user = createUser(Role.USER, true, BigDecimal.valueOf(100));
        UserResponseDTO dto = createUserResponseDTO(user);

        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(dto);

        UserResponseDTO result = userService.getUserByUsername(user.getUsername());

        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(Role.USER.name(), result.getRole());

        verify(userRepository).findUserByUsername(user.getUsername());
        verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    @DisplayName("Get user by username when user not found - throws not found")
    void test3() {
        String userName = "missingUser";
        when(userRepository.findUserByUsername(userName)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.getUserByUsername(userName)
        );

        assertEquals(NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());

        verify(userRepository).findUserByUsername(userName);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Update user when user exists - updates phone number")
    void test4() {
        User user = createUser(Role.USER, true, BigDecimal.valueOf(100));

        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setPhoneNumber("+380661111111");

        UserResponseDTO response = createUserResponseDTO(user);
        response.setPhoneNumber(request.getPhoneNumber());


        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.updateUser(user.getUsername(), request);

        assertEquals("+380661111111", user.getPhoneNumber());
        assertEquals("+380661111111", result.getPhoneNumber());

        verify(userRepository).findUserByUsername(user.getUsername());
        verify(userRepository).save(user);
        verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    @DisplayName("change account status by id when user exists - changes status")
    void test5() {
        User user = createUser(Role.USER, true, BigDecimal.valueOf(100));

        ChangeUserStatusDTO request = new ChangeUserStatusDTO();
        request.setActive(false);

        UserResponseDTO response = new UserResponseDTO();
        response.setActive(request.isActive());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.changeAccountStatusById(String.valueOf(user.getId()), request);

        assertFalse(user.isActive());
        assertFalse(result.getActive());

        verify(userRepository).findById(user.getId());
        verify(userRepository).save(user);
        verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    @DisplayName("Change account status by username when user exists - changes status")
    void test6() {
        User user = createUser(Role.USER, false, BigDecimal.valueOf(100));

        ChangeUserStatusDTO request = new ChangeUserStatusDTO();
        request.setActive(true);

        UserResponseDTO response = new UserResponseDTO();
        response.setActive(request.isActive());

        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.changeAccountStatusByUsername(user.getUsername(), request);

        assertTrue(user.isActive());
        assertTrue(result.getActive());

        verify(userRepository).findUserByUsername(user.getUsername());
        verify(userRepository).save(user);
        verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    @DisplayName("Get UserById_WhenUserExists_ReturnsUser")
    void test7() {
        User user = createUser(Role.USER, true, BigDecimal.valueOf(100));
        UserResponseDTO response = createUserResponseDTO(user);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.getUserById(user.getId());

        assertEquals(user.getId().toString(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());

        verify(userRepository).findById(user.getId());
        verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    @DisplayName("Set role manager for user when user role USER - promotes to manager")
    void test8() {
        User user = createUser(Role.USER, true, BigDecimal.valueOf(100));
        UserResponseDTO response = createUserResponseDTO(user);
        response.setRole(Role.MANAGER.name());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.setRoleManagerForUser(String.valueOf(user.getId()));

        assertEquals(Role.MANAGER, user.getRole());
        assertEquals(Role.MANAGER.name(), result.getRole());

        verify(userRepository).findById(user.getId());
        verify(userRepository).save(user);
        verify(userMapper).toUserResponseDTO(user);

    }

    @Test
    @DisplayName("Set role manager for user when user is admin does not change role and does not save")
    void test9() {
        User admin = createUser(Role.ADMIN, true, BigDecimal.valueOf(100));
        UserResponseDTO response = createUserResponseDTO(admin);

        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userMapper.toUserResponseDTO(admin)).thenReturn(response);

        UserResponseDTO result = userService.setRoleManagerForUser(admin.getId().toString());

        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals(Role.ADMIN.name(), result.getRole());

        verify(userRepository).findById(admin.getId());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper).toUserResponseDTO(admin);
    }

    @Test
    @DisplayName("Change password when old password correct - changes password")
    void test10() {
        User user = createUser(Role.USER, true, BigDecimal.valueOf(100));
        user.setPassword("encodedOldPassword");

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setOldPassword("OldPassword123");
        request.setNewPassword("NewPassword123");

        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

        userService.changePassword(user.getUsername(), request);

        assertEquals("encodedNewPassword", user.getPassword());

        verify(userRepository).findUserByUsername(user.getUsername());
        verify(passwordEncoder).matches(request.getOldPassword(), "encodedOldPassword");
        verify(passwordEncoder).encode(request.getNewPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Change password when old password incorrect - throws bad request")
    void test11() {
        User user = createUser(Role.USER, true, BigDecimal.valueOf(100));
        user.setPassword("encodedOldPassword");

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setOldPassword("WrongPassword");
        request.setNewPassword("NewPassword123");

        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), "encodedOldPassword")).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.changePassword(user.getUsername(), request)
        );

        assertEquals(BAD_REQUEST, ex.getStatusCode());
        assertEquals("Old password is incorrect", ex.getReason());

        verify(userRepository).findUserByUsername(user.getUsername());
        verify(passwordEncoder).matches(request.getOldPassword(), "encodedOldPassword");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deposit own balance when user active and amount valid - deposits balance and creates transaction")
    void test12() {
        User user = createUser(Role.USER, true, BigDecimal.valueOf(100));

        DepositRequestDTO request = new DepositRequestDTO();
        request.setAmount(BigDecimal.valueOf(50));

        UserResponseDTO responseDTO = createUserResponseDTO(user);
        responseDTO.setBalance(150.0);

        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.depositOwnBalance(user.getUsername(), request);

        assertEquals(BigDecimal.valueOf(150), user.getBalance());
        assertEquals(150.0, result.getBalance().doubleValue());

        ArgumentCaptor<BalanceTransaction> transactionCaptor =
                ArgumentCaptor.forClass(BalanceTransaction.class);

        verify(balanceTransactionRepository).save(transactionCaptor.capture());

        BalanceTransaction transaction = transactionCaptor.getValue();

        assertEquals(user, transaction.getUser());
        assertEquals(BigDecimal.valueOf(50), transaction.getAmount());
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertNotNull(transaction.getCreatedAt());

        verify(userRepository).findUserByUsername(user.getUsername());
        verify(userRepository).save(user);
        verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    @DisplayName("Deposit own balance when user blocked - throws Forbidden")
    void test13() {
        User user = createUser(Role.USER, false, BigDecimal.valueOf(100));

        DepositRequestDTO request = new DepositRequestDTO();
        request.setAmount(BigDecimal.valueOf(50));

        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.depositOwnBalance(user.getUsername(), request)
        );

        assertEquals(FORBIDDEN, ex.getStatusCode());
        assertEquals("User is blocked", ex.getReason());

        verify(userRepository).findUserByUsername(user.getUsername());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(balanceTransactionRepository);
    }

    @Test
    @DisplayName("Deposit own balance when amount is zero - throws BadRequest")
    void test14() {
        User user = createUser(Role.USER, true, BigDecimal.valueOf(100));

        DepositRequestDTO request = new DepositRequestDTO();
        request.setAmount(BigDecimal.ZERO);

        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.depositOwnBalance(user.getUsername(), request)
        );

        assertEquals(BAD_REQUEST, ex.getStatusCode());
        assertEquals("Deposit amount must be greater than zero", ex.getReason());

        verify(userRepository).findUserByUsername(user.getUsername());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(balanceTransactionRepository);
    }

    @Test
    @DisplayName("Deposit user balance when user exists and amount valid - deposits balance")
    void test15() {
        User user = createUser(Role.USER, true, BigDecimal.valueOf(200));

        DepositRequestDTO request = new DepositRequestDTO();
        request.setAmount(BigDecimal.valueOf(100));

        UserResponseDTO responseDTO = createUserResponseDTO(user);
        responseDTO.setBalance(300.0);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.depositUserBalance(user.getId().toString(), request);

        assertEquals(BigDecimal.valueOf(300), user.getBalance());
        assertEquals(300.0, result.getBalance().doubleValue());

        verify(userRepository).findById(user.getId());
        verify(userRepository).save(user);
        verify(balanceTransactionRepository).save(any(BalanceTransaction.class));
        verify(userMapper).toUserResponseDTO(user);
    }

    private User createUser(Role role, boolean active, BigDecimal balance) {
        return User.builder()
                .id(UUID.randomUUID())
                .username("user01")
                .password("encodedPassword")
                .phoneNumber("+380661234567")
                .role(role)
                .active(active)
                .balance(balance)
                .build();

    }

    private UserResponseDTO createUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .active(user.isActive())
                .balance(user.getBalance().doubleValue())
                .build();
    }

}
