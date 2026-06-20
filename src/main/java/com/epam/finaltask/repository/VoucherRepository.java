package com.epam.finaltask.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.epam.finaltask.model.enums.HotelType;
import com.epam.finaltask.model.enums.TourType;
import com.epam.finaltask.model.enums.TransferType;
import com.epam.finaltask.model.entities.Voucher;
import org.springframework.data.jpa.repository.Query;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {
    List<Voucher> findAllByUserId(UUID userId);
    Page<Voucher> findAll(Pageable pageable);
    List<Voucher> findByIsHotTrueOrderByPriceAsc();
    Page<Voucher> findAllByOrderByIsHotDescPriceAsc(Pageable pageable);
    Page<Voucher> findAllByUserIsNullOrderByIsHotDescPriceAsc(Pageable pageable);

    @Query("""
            select v from Voucher v
                    where (:tourType is null or v.tourType = :tourType)
                    and (:hotelType is null or v.hotelType = :hotelType)
                    and (:transferType is null or v.transferType = :transferType)
                    and (:maxPrice is null or v.price <= :maxPrice)
                    and (:availableOnly = false or v.user is null)
                    order by v.isHot desc, v.price asc
            """)
    Page<Voucher> search(
            TourType tourType,
            TransferType transferType,
            HotelType hotelType,
            Double maxPrice,
            boolean availableOnly,
            Pageable pageable
    );

}
