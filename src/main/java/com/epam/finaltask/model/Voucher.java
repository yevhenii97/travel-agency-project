package com.epam.finaltask.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
@Table(name = "\"voucher\"")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;
    private String description;
    private Double price;
    @Enumerated(EnumType.STRING)
    private TourType tourType;
    @Enumerated(EnumType.STRING)
    private TransferType transferType;
    @Enumerated(EnumType.STRING)
    private HotelType hotelType;
    @Enumerated(EnumType.STRING)
    private VoucherStatus status;
    private LocalDate arrivalDate;
    private LocalDate evictionDate;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private boolean isHot;
}
