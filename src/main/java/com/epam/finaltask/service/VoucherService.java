package com.epam.finaltask.service;

import java.util.List;

import com.epam.finaltask.dto.user.ChangeVoucherStatusRequestDTO;
import com.epam.finaltask.dto.voucher.ChangeHotStatusRequestDTO;
import com.epam.finaltask.dto.voucher.VoucherDTO;
import org.springframework.data.domain.Pageable;

public interface VoucherService {
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO order(String id, String userId);
    VoucherDTO update(String id, VoucherDTO voucherDTO);
    void delete(String voucherId);
    VoucherDTO changeStatus(String id, ChangeVoucherStatusRequestDTO voucherDTO);
    VoucherDTO changeHotStatus(String id, ChangeHotStatusRequestDTO changeHotStatusRequestDTO);

    List<VoucherDTO> findAllByUserId(String userId);

    List<VoucherDTO> search(
            String tourType,
            String transferType,
            String hotelType,
            Double maxPrice
    );

    List<VoucherDTO> findAll(Pageable pageable);
    List<VoucherDTO> findAllHotVouchers(Pageable pageable);
    List<VoucherDTO> getCurrentUserVouchers(String username, Pageable pageable);
}
