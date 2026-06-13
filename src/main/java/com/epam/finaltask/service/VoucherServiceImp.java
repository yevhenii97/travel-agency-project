package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.mapper.VoucherMapperImpl;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherServiceImp implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final VoucherMapperImpl voucherMapper;

    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {
        Voucher newVoucher = voucherMapper.toVoucher(voucherDTO);
        return voucherMapper.toVoucherDTO(voucherRepository.save(newVoucher));
    }

    @Override
    public VoucherDTO order(String id, String userId) {
        Voucher voucherEntity = voucherRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found"));
        User userEntity = userRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!userEntity.isActive()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is blocked");

        voucherEntity.setUser(userEntity);
        voucherEntity.setStatus(VoucherStatus.REGISTERED);
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucherEntity));
    }

    @Override
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
        Voucher voucherEntity = voucherRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found"));

        voucherEntity.setTitle(voucherDTO.getTitle());
        voucherEntity.setDescription(voucherDTO.getDescription());
        voucherEntity.setPrice(voucherDTO.getPrice());
        voucherEntity.setTourType(TourType.valueOf(voucherDTO.getTourType()));
        voucherEntity.setTransferType(TransferType.valueOf(voucherDTO.getTransferType()));
        voucherEntity.setHotelType(HotelType.valueOf(voucherDTO.getHotelType()));
        voucherEntity.setArrivalDate(voucherDTO.getArrivalDate());
        voucherEntity.setEvictionDate(voucherDTO.getEvictionDate());
        voucherEntity.setHot(voucherDTO.getIsHot());

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucherEntity));
    }

    @Override
    public void delete(String voucherId) {
        UUID id = UUID.fromString(voucherId);
        if (!voucherRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");

        voucherRepository.deleteById(id);
    }

    @Override
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        Voucher voucherEntity = voucherRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found"));

        voucherEntity.setHot(voucherDTO.getIsHot());
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucherEntity));
    }

    @Override
    public List<VoucherDTO> findAllByUserId(String userId) {
        User userEntity = userRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userEntity.getVouchers().stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAllByTourType(TourType tourType) {
        List<Voucher> voucherEntity = voucherRepository.findAllByTourType(tourType);
        if (voucherEntity.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");

        return voucherEntity.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAllByTransferType(String transferType) {
        List<Voucher> voucherEntity = voucherRepository.findAllByTransferType(TransferType.valueOf(transferType));
        if (voucherEntity.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");

        return voucherEntity.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAllByPrice(Double price) {
        List<Voucher> voucherEntity = voucherRepository.findAllByPrice(price);
        if (voucherEntity.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");

        return voucherEntity.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType) {
        List<Voucher> voucherEntity = voucherRepository.findAllByHotelType(hotelType);
        if (voucherEntity.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");

        return voucherEntity.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAll() {
        return voucherRepository.findAll().stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }
}
