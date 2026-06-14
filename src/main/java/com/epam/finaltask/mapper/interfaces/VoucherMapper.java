package com.epam.finaltask.mapper.interfaces;

import com.epam.finaltask.dto.voucher.VoucherDTO;
import com.epam.finaltask.model.entities.Voucher;

public interface VoucherMapper {
    Voucher toVoucher(VoucherDTO voucherDTO);
    VoucherDTO toVoucherDTO(Voucher voucher);
}
