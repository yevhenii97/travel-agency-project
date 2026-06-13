package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VoucherMapperImpl implements VoucherMapper {
    @Override
    public Voucher toVoucher(VoucherDTO dto) {
        return Voucher.builder()
                .id(UUID.fromString(dto.getId()))
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .tourType(TourType.valueOf(dto.getTourType()))
                .transferType(TransferType.valueOf(dto.getTransferType()))
                .hotelType(HotelType.valueOf(dto.getHotelType()))
                .status(VoucherStatus.valueOf(dto.getStatus()))
                .arrivalDate(dto.getArrivalDate())
                .evictionDate(dto.getEvictionDate())
                .user(User.builder()
                        .id(dto.getUserId())
                        .build())
                .isHot(dto.getIsHot())
                .build();
    }

    @Override
    public VoucherDTO toVoucherDTO(Voucher voucher) {
        return VoucherDTO.builder()
                .id(String.valueOf(voucher.getId()))
                .title(voucher.getTitle())
                .description(voucher.getDescription())
                .price(voucher.getPrice())
                .tourType(String.valueOf(voucher.getTourType()))
                .transferType(String.valueOf(voucher.getTransferType()))
                .hotelType(String.valueOf(voucher.getHotelType()))
                .status(String.valueOf(voucher.getStatus()))
                .arrivalDate(voucher.getArrivalDate())
                .evictionDate(voucher.getEvictionDate())
                .userId(voucher.getUser().getId())
                .isHot(voucher.isHot())
                .build();
    }
}
