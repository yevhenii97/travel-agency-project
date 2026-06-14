package com.epam.finaltask.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherServiceImp implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final BalanceTransactionRepository balanceTransactionRepository;
    private final VoucherMapperImpl voucherMapper;

    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {
        log.info("Creating voucher: title={}, tourType={}, price={}",
                voucherDTO.getTitle(), voucherDTO.getTourType(), voucherDTO.getPrice());

        Voucher voucher = voucherMapper.toVoucher(voucherDTO);

        if (voucherDTO.getUserId() != null) {
            log.debug("Assigning voucher to user: userId={}", voucherDTO.getUserId());

            User user = userRepository.findById(voucherDTO.getUserId())
                    .orElseThrow(() -> {
                        log.warn("Cannot create voucher. User not found: userId={}", voucherDTO.getUserId());
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                    });

            voucher.setUser(user);
        }

        Voucher saved = voucherRepository.save(voucher);

        log.info("Voucher created successfully: voucherId={}, title={}",
                saved.getId(), saved.getTitle());

        return voucherMapper.toVoucherDTO(saved);
    }

    @Override
    @Transactional
    public VoucherDTO order(String voucherId, String userName) {
        log.info("Ordering voucher: voucherId={}, username={}", voucherId, userName);

        Voucher voucherEntity = voucherRepository.findById(UUID.fromString(voucherId))
                .orElseThrow(() -> {
                    log.warn("Cannot order voucher. Voucher not found: voucherId={}", voucherId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");
                });

        User userEntity = userRepository.findUserByUsername(userName)
                .orElseThrow(() -> {
                    log.warn("Cannot order voucher. User not found: username={}", userName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        if (!userEntity.isActive()) {
            log.warn("Blocked user tried to order voucher: userId={}, username={}",
                    userEntity.getId(), userEntity.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is blocked");
        }

        if (voucherEntity.getUser() != null) {
            log.warn("Voucher already ordered: voucherId={}, currentUserId={}",
                    voucherEntity.getId(), voucherEntity.getUser().getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher is already ordered");
        }

        BigDecimal userBalance = userEntity.getBalance();
        BigDecimal voucherPrice = BigDecimal.valueOf(voucherEntity.getPrice());

        if(userBalance.compareTo(voucherPrice) < 0){
            log.warn("Not enough balance: userId={}, balance={}, voucherPrice={}",
                    userEntity.getId(), userBalance, voucherPrice);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough balance");
        }

        log.debug("Voucher before order: voucherId={}, status={}, currentUserId={}",
                voucherEntity.getId(),
                voucherEntity.getStatus(),
                voucherEntity.getUser() != null ? voucherEntity.getUser().getId() : null);

        userEntity.setBalance(userBalance.subtract(voucherPrice));
        voucherEntity.setUser(userEntity);
        voucherEntity.setStatus(VoucherStatus.REGISTERED);

        log.debug("Voucher ordered: voucherId={}, userId={}, newBalance={}",
                voucherEntity.getId(), userEntity.getId(), userEntity.getBalance());

        Voucher savedVoucher = voucherRepository.save(voucherEntity);
        userRepository.save(userEntity);

        log.info("Voucher ordered successfully: voucherId={}, userId={}, newBalance={}",
                savedVoucher.getId(), userEntity.getId(), userEntity.getBalance());

        BalanceTransaction transaction =
                BalanceTransaction.builder()
                        .user(userEntity)
                        .amount(voucherPrice)
                        .type(TransactionType.PURCHASE)
                        .createdAt(LocalDateTime.now())
                        .build();

        balanceTransactionRepository.save(transaction);

        return voucherMapper.toVoucherDTO(savedVoucher);
    }

    @Override
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
        log.info("Updating voucher: voucherId={}", id);
        log.debug("Update voucher payload: title={}, tourType={}, transferType={}, hotelType={}, price={}",
                voucherDTO.getTitle(),
                voucherDTO.getTourType(),
                voucherDTO.getTransferType(),
                voucherDTO.getHotelType(),
                voucherDTO.getPrice());

        Voucher voucherEntity = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> {
                    log.warn("Cannot update voucher. Voucher not found: voucherId={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");
                });

        voucherEntity.setTitle(voucherDTO.getTitle());
        voucherEntity.setDescription(voucherDTO.getDescription());
        voucherEntity.setPrice(voucherDTO.getPrice());
        voucherEntity.setTourType(TourType.valueOf(voucherDTO.getTourType()));
        voucherEntity.setTransferType(TransferType.valueOf(voucherDTO.getTransferType()));
        voucherEntity.setHotelType(HotelType.valueOf(voucherDTO.getHotelType()));
        voucherEntity.setArrivalDate(voucherDTO.getArrivalDate());
        voucherEntity.setEvictionDate(voucherDTO.getEvictionDate());
        voucherEntity.setHot(voucherDTO.getIsHot());

        Voucher saved = voucherRepository.save(voucherEntity);

        log.info("Voucher updated successfully: voucherId={}", saved.getId());

        return voucherMapper.toVoucherDTO(saved);
    }

    @Override
    public void delete(String voucherId) {
        log.info("Deleting voucher: voucherId={}", voucherId);

        UUID id = UUID.fromString(voucherId);

        if (!voucherRepository.existsById(id)) {
            log.warn("Cannot delete voucher. Voucher not found: voucherId={}", voucherId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");
        }

        voucherRepository.deleteById(id);

        log.info("Voucher deleted successfully: voucherId={}", voucherId);
    }

    @Override
    public VoucherDTO findById(String id) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Voucher not found"
                ));

        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public VoucherDTO changeStatus(String id, ChangeVoucherStatusRequestDTO voucherDTO) {
        log.info("Changing voucher status: voucherId={}, requestedStatus={}", id, voucherDTO.getStatus());

        Voucher voucherEntity = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> {
                    log.warn("Cannot change voucher status. Voucher not found: voucherId={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");
                });

        VoucherStatus newStatus = VoucherStatus.valueOf(voucherDTO.getStatus().toUpperCase());

        if (newStatus != VoucherStatus.PAID && newStatus != VoucherStatus.CANCELED) {
            log.warn("Invalid voucher status change requested: voucherId={}, requestedStatus={}",
                    id, newStatus);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PAID or CANCELED allowed");
        }

        if (voucherEntity.getStatus() != VoucherStatus.REGISTERED) {
            log.warn("Invalid voucher status transition: voucherId={}, currentStatus={}, requestedStatus={}",
                    id, voucherEntity.getStatus(), newStatus);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only REGISTERED voucher can be changed");
        }

        voucherEntity.setStatus(newStatus);

        Voucher saved = voucherRepository.save(voucherEntity);

        log.info("Voucher status changed successfully: voucherId={}, newStatus={}",
                saved.getId(), saved.getStatus());

        return voucherMapper.toVoucherDTO(saved);
    }

    @Override
    @Transactional
    public VoucherDTO changeHotStatus(String id, ChangeHotStatusRequestDTO request) {
        log.info("Changing voucher hot status: voucherId={}, hot={}", id, request.isHot());

        Voucher voucherEntity = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> {
                    log.warn("Cannot change hot status. Voucher not found: voucherId={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");
                });

        voucherEntity.setHot(request.isHot());

        Voucher saved = voucherRepository.save(voucherEntity);

        log.info("Voucher hot status changed successfully: voucherId={}, hot={}",
                saved.getId(), saved.isHot());

        return voucherMapper.toVoucherDTO(saved);
    }

    @Override
    public List<VoucherDTO> findAvailable(Pageable pageable) {
        return voucherRepository.findAllByUserIsNullOrderByIsHotDescPriceAsc(pageable)
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }

    @Override
    public List<VoucherDTO> findAllByUserId(String userId) {
        log.info("Finding vouchers by userId={}", userId);

        User userEntity = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    log.warn("Cannot find vouchers. User not found: userId={}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        List<VoucherDTO> vouchers = userEntity.getVouchers()
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();

        log.debug("Found {} vouchers for userId={}", vouchers.size(), userId);

        return vouchers;
    }

    @Override
    public List<VoucherDTO> search(String tourType, String transferType, String hotelType, Double maxPrice) {
        log.info("Searching vouchers");
        log.debug("Search params: tourType={}, transferType={}, hotelType={}, maxPrice={}",
                tourType, transferType, hotelType, maxPrice);

        TourType tourTypeEnum = tourType != null ? TourType.valueOf(tourType.toUpperCase()) : null;
        TransferType transferTypeEnum = transferType != null ? TransferType.valueOf(transferType.toUpperCase()) : null;
        HotelType hotelTypeEnum = hotelType != null ? HotelType.valueOf(hotelType.toUpperCase()) : null;

        List<VoucherDTO> vouchers = voucherRepository.search(
                        tourTypeEnum,
                        transferTypeEnum,
                        hotelTypeEnum,
                        maxPrice
                )
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();

        log.debug("Search completed. Found {} vouchers", vouchers.size());

        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAll(Pageable pageable) {
        log.info("Finding all vouchers: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        List<VoucherDTO> vouchers = voucherRepository.findAllByOrderByIsHotDescPriceAsc(pageable)
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();

        log.debug("Found {} vouchers on page={}", vouchers.size(), pageable.getPageNumber());

        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAllHotVouchers(Pageable pageable) {
        log.info("Finding hot vouchers: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        List<VoucherDTO> vouchers = voucherRepository.findByIsHotTrueOrderByPriceAsc()
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();

        log.debug("Found {} hot vouchers", vouchers.size());

        return vouchers;
    }

    @Override
    public List<VoucherDTO> getCurrentUserVouchers(String username, Pageable pageable) {
        log.info("Finding current user vouchers: username={}, page={}, size={}",
                username, pageable.getPageNumber(), pageable.getPageSize());

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Cannot find current user vouchers. User not found: username={}", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        List<VoucherDTO> vouchers = voucherRepository.findAllByUserId(user.getId())
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();

        log.debug("Found {} vouchers for current user: username={}", vouchers.size(), username);

        return vouchers;
    }

//    @Override
//    public VoucherDTO create(VoucherDTO voucherDTO) {
//        log.info("Creating voucher: title={}", voucherDTO.getTitle());
//        Voucher voucher = voucherMapper.toVoucher(voucherDTO);
//
//        if (voucherDTO.getUserId() != null) {
//            User user = userRepository.findById(voucherDTO.getUserId())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
//
//            voucher.setUser(user);
//        }
//        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
//    }
//
//    @Override
//    public VoucherDTO order(String voucherId, String userName) {
//        Voucher voucherEntity = voucherRepository.findById(UUID.fromString(voucherId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found"));
//        User userEntity = userRepository.findUserByUsername(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
//
//        if (!userEntity.isActive()) {
//            log.warn("Blocked user tried to order voucher: userId={}", userEntity.getId());
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is blocked");
//        }
//
//        log.debug("Voucher before order: id={}, status={}, currentUser={}",
//                voucherEntity.getId(), voucherEntity.getStatus(),
//                voucherEntity.getUser() != null ? voucherEntity.getUser().getId() : null);
//
//        voucherEntity.setUser(userEntity);
//        voucherEntity.setStatus(VoucherStatus.REGISTERED);
//        return voucherMapper.toVoucherDTO(voucherRepository.save(voucherEntity));
//    }
//
//    @Override
//    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
//        Voucher voucherEntity = voucherRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found"));
//
//        voucherEntity.setTitle(voucherDTO.getTitle());
//        voucherEntity.setDescription(voucherDTO.getDescription());
//        voucherEntity.setPrice(voucherDTO.getPrice());
//        voucherEntity.setTourType(TourType.valueOf(voucherDTO.getTourType()));
//        voucherEntity.setTransferType(TransferType.valueOf(voucherDTO.getTransferType()));
//        voucherEntity.setHotelType(HotelType.valueOf(voucherDTO.getHotelType()));
//        voucherEntity.setArrivalDate(voucherDTO.getArrivalDate());
//        voucherEntity.setEvictionDate(voucherDTO.getEvictionDate());
//        voucherEntity.setHot(voucherDTO.getIsHot());
//
//        return voucherMapper.toVoucherDTO(voucherRepository.save(voucherEntity));
//    }
//
//    @Override
//    public void delete(String voucherId) {
//        UUID id = UUID.fromString(voucherId);
//        if (!voucherRepository.existsById(id))
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");
//
//        voucherRepository.deleteById(id);
//    }
//
//    @Override
//    public VoucherDTO changeStatus(String id, ChangeVoucherStatusRequestDTO voucherDTO) {
//        Voucher voucherEntity = voucherRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found"));
//
//        VoucherStatus newStatus = VoucherStatus.valueOf(voucherDTO.getStatus().toUpperCase());
//
//        if (newStatus != VoucherStatus.PAID && newStatus != VoucherStatus.CANCELED) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PAID or CANCELED allowed");
//        }
//
//        if (voucherEntity.getStatus() != VoucherStatus.REGISTERED) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only REGISTERED voucher can be changed");
//        }
//
//        voucherEntity.setStatus(VoucherStatus.valueOf(voucherDTO.getStatus()));
//        return voucherMapper.toVoucherDTO(voucherRepository.save(voucherEntity));
//    }
//
//    @Override
//    public VoucherDTO changeHotStatus(String id, ChangeHotStatusRequestDTO changeHotStatusRequestDTO) {
//        Voucher voucherEntity = voucherRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found"));
//
//        voucherEntity.setHot(changeHotStatusRequestDTO.isHot());
//        return voucherMapper.toVoucherDTO(voucherRepository.save(voucherEntity));
//    }
//
//    @Override
//    public List<VoucherDTO> findAllByUserId(String userId) {
//        User userEntity = userRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
//        return userEntity.getVouchers().stream().map(voucherMapper::toVoucherDTO).toList();
//    }
//
//    @Override
//    public List<VoucherDTO> findAllByTourType(TourType tourType) {
//        List<Voucher> voucherEntity = voucherRepository.findAllByTourType(tourType);
//        if (voucherEntity.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");
//
//        return voucherEntity.stream().map(voucherMapper::toVoucherDTO).toList();
//    }
//
//    @Override
//    public List<VoucherDTO> findAllByTransferType(String transferType) {
//        List<Voucher> voucherEntity = voucherRepository.findAllByTransferType(TransferType.valueOf(transferType));
//        if (voucherEntity.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");
//
//        return voucherEntity.stream().map(voucherMapper::toVoucherDTO).toList();
//    }
//
//    @Override
//    public List<VoucherDTO> findAllByPrice(Double price) {
//        List<Voucher> voucherEntity = voucherRepository.findAllByPrice(price);
//        if (voucherEntity.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");
//
//        return voucherEntity.stream().map(voucherMapper::toVoucherDTO).toList();
//    }
//
//    @Override
//    public List<VoucherDTO> findAllByHotelType(HotelType hotelType) {
//        List<Voucher> voucherEntity = voucherRepository.findAllByHotelType(hotelType);
//        if (voucherEntity.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");
//
//        return voucherEntity.stream().map(voucherMapper::toVoucherDTO).toList();
//    }
//
//    @Override
//    public List<VoucherDTO> search(String tourType, String transferType, String hotelType, Double maxPrice) {
//        TourType tourTypeEnum = tourType != null
//                ? TourType.valueOf(tourType.toUpperCase())
//                : null;
//
//        TransferType transferTypeEnum = transferType != null
//                ? TransferType.valueOf(transferType.toUpperCase())
//                : null;
//
//        HotelType hotelTypeEnum = hotelType != null
//                ? HotelType.valueOf(hotelType.toUpperCase())
//                : null;
//
//        List<Voucher> vouchers = voucherRepository.search(
//                tourTypeEnum,
//                transferTypeEnum,
//                hotelTypeEnum,
//                maxPrice
//        );
//
//        return vouchers.stream()
//                .map(voucherMapper::toVoucherDTO)
//                .toList();
//    }
//
//    @Override
//    public List<VoucherDTO> findAll(Pageable pageable) {
//        return voucherRepository.findAll(pageable).stream()
//                .map(voucherMapper::toVoucherDTO)
//                .toList();
//    }
//
//    @Override
//    public List<VoucherDTO> findAllHotVouchers(Pageable pageable) {
//        return voucherRepository.findByIsHotTrueOrderByPriceAsc().stream()
//                .map(voucherMapper::toVoucherDTO)
//                .toList();
//    }
//
//    @Override
//    public List<VoucherDTO> getCurrentUserVouchers(String username, Pageable pageable) {
//        User user = userRepository.findUserByUsername(username)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
//
//        return voucherRepository.findAllByUserId(user.getId())
//                .stream()
//                .map(voucherMapper::toVoucherDTO)
//                .toList();
//    }
}
