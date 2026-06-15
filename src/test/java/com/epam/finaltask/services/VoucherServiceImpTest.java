package com.epam.finaltask.services;

import com.epam.finaltask.dto.user.ChangeVoucherStatusRequestDTO;
import com.epam.finaltask.dto.voucher.ChangeHotStatusRequestDTO;
import com.epam.finaltask.dto.voucher.VoucherDTO;
import com.epam.finaltask.mapper.VoucherMapperImpl;
import com.epam.finaltask.model.entities.BalanceTransaction;
import com.epam.finaltask.model.entities.User;
import com.epam.finaltask.model.entities.Voucher;
import com.epam.finaltask.model.enums.*;
import com.epam.finaltask.repository.BalanceTransactionRepository;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import com.epam.finaltask.service.VoucherServiceImp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoucherServiceImpTest {

    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BalanceTransactionRepository balanceTransactionRepository;
    @Mock
    private VoucherMapperImpl voucherMapper;

    @InjectMocks
    private VoucherServiceImp voucherService;

    @Test
    @DisplayName("Should successfully order voucher")
    void test1() {
        UUID voucherId = UUID.randomUUID();
        User user = createUser(Role.USER, true, BigDecimal.valueOf(1000));
        Voucher voucher = createVoucher(voucherId, 500.0);

        VoucherDTO voucherDTO = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.findUserByUsername("user01")).thenReturn(Optional.of(user));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.order(voucherId.toString(), "user01");

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500.0), user.getBalance());
        assertEquals(user, voucher.getUser());

        assertEquals(VoucherStatus.REGISTERED, voucher.getStatus());

        verify(voucherRepository).save(voucher);
        verify(userRepository).save(user);
        verify(balanceTransactionRepository).save(any(BalanceTransaction.class));
    }

    @Test
    @DisplayName("Should throw FORBIDDEN when blocked user orders voucher")
    void test2(){
        UUID voucherId = UUID.randomUUID();
        User user = createUser(Role.USER, false, BigDecimal.valueOf(1000));
        Voucher voucher = createVoucher(voucherId, 500.0);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.order(String.valueOf(voucherId), user.getUsername())
                );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("User is blocked", ex.getReason());

        verify(voucherRepository, never()).save(any());
        verify(balanceTransactionRepository, never()).save(any());

    }

    @Test
    @DisplayName("Should throw NOT_FOUND when deleting unknown voucher")
    void test3() {
        UUID voucherId = UUID.randomUUID();
        when(voucherRepository.existsById(voucherId)).thenReturn(false);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> voucherService.delete(voucherId.toString())
                );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Voucher not found", exception.getReason());

        verify(voucherRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when voucher does not exist during order")
    void test4() {
        UUID voucherId = UUID.randomUUID();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.order(voucherId.toString(), "user01")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Voucher not found", ex.getReason());

        verify(userRepository, never()).findUserByUsername(any());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when user does not exist during order")
    void test5() {
        UUID voucherId = UUID.randomUUID();
        Voucher voucher = createVoucher(voucherId, 500.0);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.findUserByUsername("user01")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.order(voucherId.toString(), "user01")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());

        verify(voucherRepository, never()).save(any());
        verify(balanceTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BAD_REQUEST when voucher already ordered")
    void test6() {
        UUID voucherId = UUID.randomUUID();

        User currentUser = createUser(Role.USER, true, BigDecimal.valueOf(1000));
        User anotherUser = createUser(Role.USER, true, BigDecimal.valueOf(1000));

        Voucher voucher = createVoucher(voucherId, 500.0);
        voucher.setUser(anotherUser);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.findUserByUsername(currentUser.getUsername())).thenReturn(Optional.of(currentUser));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.order(voucherId.toString(), currentUser.getUsername())
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Voucher is already ordered", ex.getReason());

        verify(voucherRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(balanceTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BAD_REQUEST when user has insufficient balance")
    void test7() {
        UUID voucherId = UUID.randomUUID();

        User user = createUser(Role.USER, true, BigDecimal.valueOf(100));
        Voucher voucher = createVoucher(voucherId, 500.0);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.order(voucherId.toString(), user.getUsername())
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Not enough balance", ex.getReason());

        verify(voucherRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(balanceTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update voucher successfully")
    void test8() {
        UUID voucherId = UUID.randomUUID();

        Voucher voucher = createVoucher(voucherId, 300.0);

        VoucherDTO request = new VoucherDTO();
        request.setTitle("Updated title");
        request.setDescription("Updated description");
        request.setPrice(700.0);
        request.setTourType(TourType.SAFARI.name());
        request.setTransferType(TransferType.JEEPS.name());
        request.setHotelType(HotelType.FOUR_STARS.name());
        request.setArrivalDate(LocalDate.now().plusDays(10));
        request.setEvictionDate(LocalDate.now().plusDays(15));
        request.setIsHot(true);

        VoucherDTO response = new VoucherDTO();
        response.setTitle("Updated title");

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(response);

        VoucherDTO result = voucherService.update(voucherId.toString(), request);

        assertNotNull(result);
        assertEquals("Updated title", voucher.getTitle());
        assertEquals("Updated description", voucher.getDescription());
        assertEquals(700.0, voucher.getPrice());
        assertEquals(TourType.SAFARI, voucher.getTourType());
        assertEquals(TransferType.JEEPS, voucher.getTransferType());
        assertEquals(HotelType.FOUR_STARS, voucher.getHotelType());
        assertTrue(voucher.isHot());

        verify(voucherRepository).save(voucher);
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when updating unknown voucher")
    void test9() {
        UUID voucherId = UUID.randomUUID();

        VoucherDTO request = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.update(voucherId.toString(), request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Voucher not found", ex.getReason());

        verify(voucherRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete voucher successfully")
    void test10() {
        UUID voucherId = UUID.randomUUID();

        when(voucherRepository.existsById(voucherId)).thenReturn(true);

        voucherService.delete(voucherId.toString());

        verify(voucherRepository).existsById(voucherId);
        verify(voucherRepository).deleteById(voucherId);
    }

    @Test
    @DisplayName("Should find voucher by id")
    void test11() {
        UUID voucherId = UUID.randomUUID();

        Voucher voucher = createVoucher(voucherId, 500.0);
        VoucherDTO dto = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

        VoucherDTO result = voucherService.findById(voucherId.toString());

        assertNotNull(result);

        verify(voucherRepository).findById(voucherId);
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when voucher not found by id")
    void test12() {
        UUID voucherId = UUID.randomUUID();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.findById(voucherId.toString())
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Voucher not found", ex.getReason());

        verify(voucherMapper, never()).toVoucherDTO(any());
    }

    @Test
    @DisplayName("Should change voucher status to PAID")
    void test13() {
        UUID voucherId = UUID.randomUUID();

        Voucher voucher = createVoucher(voucherId, 500.0);
        voucher.setStatus(VoucherStatus.REGISTERED);

        ChangeVoucherStatusRequestDTO request = new ChangeVoucherStatusRequestDTO();
        request.setStatus("PAID");

        VoucherDTO dto = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

        VoucherDTO result = voucherService.changeStatus(voucherId.toString(), request);

        assertNotNull(result);
        assertEquals(VoucherStatus.PAID, voucher.getStatus());

        verify(voucherRepository).save(voucher);
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @DisplayName("Should change voucher status to CANCELED")
    void test14() {
        UUID voucherId = UUID.randomUUID();

        Voucher voucher = createVoucher(voucherId, 500.0);
        voucher.setStatus(VoucherStatus.REGISTERED);

        ChangeVoucherStatusRequestDTO request = new ChangeVoucherStatusRequestDTO();
        request.setStatus("CANCELED");

        VoucherDTO dto = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

        VoucherDTO result = voucherService.changeStatus(voucherId.toString(), request);

        assertNotNull(result);
        assertEquals(VoucherStatus.CANCELED, voucher.getStatus());

        verify(voucherRepository).save(voucher);
    }

    @Test
    @DisplayName("Should throw BAD_REQUEST when status is not PAID or CANCELED")
    void test15() {
        UUID voucherId = UUID.randomUUID();

        Voucher voucher = createVoucher(voucherId, 500.0);
        voucher.setStatus(VoucherStatus.REGISTERED);

        ChangeVoucherStatusRequestDTO request = new ChangeVoucherStatusRequestDTO();
        request.setStatus("REGISTERED");

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.changeStatus(voucherId.toString(), request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Only PAID or CANCELED allowed", ex.getReason());

        verify(voucherRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BAD_REQUEST when voucher is not REGISTERED")
    void test16() {
        UUID voucherId = UUID.randomUUID();

        Voucher voucher = createVoucher(voucherId, 500.0);
        voucher.setStatus(VoucherStatus.PAID);

        ChangeVoucherStatusRequestDTO request = new ChangeVoucherStatusRequestDTO();
        request.setStatus("CANCELED");

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.changeStatus(voucherId.toString(), request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Only REGISTERED voucher can be changed", ex.getReason());

        verify(voucherRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should change hot status successfully")
    void test17() {
        UUID voucherId = UUID.randomUUID();

        Voucher voucher = createVoucher(voucherId, 500.0);
        voucher.setHot(false);

        ChangeHotStatusRequestDTO request = new ChangeHotStatusRequestDTO();
        request.setHot(true);

        VoucherDTO dto = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

        VoucherDTO result = voucherService.changeHotStatus(voucherId.toString(), request);

        assertNotNull(result);
        assertTrue(voucher.isHot());

        verify(voucherRepository).save(voucher);
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when changing hot status of unknown voucher")
    void test18() {
        UUID voucherId = UUID.randomUUID();

        ChangeHotStatusRequestDTO request = new ChangeHotStatusRequestDTO();
        request.setHot(true);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.changeHotStatus(voucherId.toString(), request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Voucher not found", ex.getReason());

        verify(voucherRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return available vouchers")
    void test19() {
        PageRequest pageable = PageRequest.of(0, 10);

        Voucher voucher = createVoucher(UUID.randomUUID(), 500.0);
        VoucherDTO dto = new VoucherDTO();

        Page<Voucher> voucherPage = new PageImpl<>(List.of(voucher), pageable, 1);

        when(voucherRepository.findAllByUserIsNullOrderByIsHotDescPriceAsc(pageable)).thenReturn(voucherPage);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

        List<VoucherDTO> result = voucherService.findAvailable(pageable);

        assertEquals(1, result.size());

        verify(voucherRepository).findAllByUserIsNullOrderByIsHotDescPriceAsc(pageable);
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @DisplayName("Should return vouchers by user id")
    void test20() {
        UUID userId = UUID.randomUUID();

        Voucher voucher = createVoucher(UUID.randomUUID(), 500.0);
        VoucherDTO dto = new VoucherDTO();

        User user = createUser(Role.USER, true, BigDecimal.valueOf(1000));
        user.setId(userId);
        user.setVouchers(List.of(voucher));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

        List<VoucherDTO> result = voucherService.findAllByUserId(userId.toString());

        assertEquals(1, result.size());

        verify(userRepository).findById(userId);
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when searching vouchers by unknown user id")
    void test21() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.findAllByUserId(userId.toString())
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());

        verify(voucherMapper, never()).toVoucherDTO(any());
    }

    @Test
    @DisplayName("Should search vouchers with filters")
    void test22() {
        Voucher voucher = createVoucher(UUID.randomUUID(), 500.0);
        VoucherDTO dto = new VoucherDTO();

        when(voucherRepository.search(
                TourType.LEISURE,
                TransferType.PLANE,
                HotelType.FIVE_STARS,
                1000.0
        )).thenReturn(List.of(voucher));

        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

        List<VoucherDTO> result = voucherService.search(
                "LEISURE",
                "PLANE",
                "FIVE_STARS",
                1000.0
        );

        assertEquals(1, result.size());

        verify(voucherRepository).search(
                TourType.LEISURE,
                TransferType.PLANE,
                HotelType.FIVE_STARS,
                1000.0
        );
    }

    @Test
    @DisplayName("Should search vouchers without filters")
    void test23() {
        Voucher voucher = createVoucher(UUID.randomUUID(), 500.0);
        VoucherDTO dto = new VoucherDTO();

        when(voucherRepository.search(null, null, null, null))
                .thenReturn(List.of(voucher));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

        List<VoucherDTO> result = voucherService.search("", "", "", null);

        assertEquals(1, result.size());

        verify(voucherRepository).search(null, null, null, null);
    }

    @Test
    @DisplayName("Should return all vouchers")
    void test24() {
        PageRequest pageable = PageRequest.of(0, 10);

        Voucher voucher = createVoucher(UUID.randomUUID(), 500.0);
        VoucherDTO dto = new VoucherDTO();

        Page<Voucher> voucherPage = new PageImpl<>(List.of(voucher), pageable, 1);

        when(voucherRepository.findAllByOrderByIsHotDescPriceAsc(pageable))
                .thenReturn(voucherPage);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

        List<VoucherDTO> result = voucherService.findAll(pageable);

        assertEquals(1, result.size());

        verify(voucherRepository).findAllByOrderByIsHotDescPriceAsc(pageable);
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @DisplayName("Should return all hot vouchers")
    void test25() {
        PageRequest pageable = PageRequest.of(0, 10);

        Voucher voucher = createVoucher(UUID.randomUUID(), 500.0);
        voucher.setHot(true);

        VoucherDTO dto = new VoucherDTO();

        when(voucherRepository.findByIsHotTrueOrderByPriceAsc()).thenReturn(List.of(voucher));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

        List<VoucherDTO> result = voucherService.findAllHotVouchers(pageable);

        assertEquals(1, result.size());

        verify(voucherRepository).findByIsHotTrueOrderByPriceAsc();
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @DisplayName("Should return current user vouchers")
    void test26() {
        PageRequest pageable = PageRequest.of(0, 10);

        User user = createUser(Role.USER, true, BigDecimal.valueOf(1000));

        Voucher voucher = createVoucher(UUID.randomUUID(), 500.0);
        VoucherDTO dto = new VoucherDTO();

        when(userRepository.findUserByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(voucherRepository.findAllByUserId(user.getId()))
                .thenReturn(List.of(voucher));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

        List<VoucherDTO> result = voucherService.getCurrentUserVouchers(user.getUsername(), pageable);

        assertEquals(1, result.size());

        verify(userRepository).findUserByUsername(user.getUsername());
        verify(voucherRepository).findAllByUserId(user.getId());
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when current user not found")
    void test27() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(userRepository.findUserByUsername("missing"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> voucherService.getCurrentUserVouchers("missing", pageable)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());

        verify(voucherRepository, never()).findAllByUserId(any());
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

    private Voucher createVoucher(UUID id, double price) {
        return Voucher.builder()
                .id(id)
                .price(price)
                .build();
    }
}
