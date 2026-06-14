package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.user.ChangeVoucherStatusRequestDTO;
import com.epam.finaltask.dto.voucher.ChangeHotStatusRequestDTO;
import com.epam.finaltask.dto.voucher.VoucherDTO;
import com.epam.finaltask.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Voucher API", description = "API for managing travel vouchers"
)
public class VoucherRestController {

    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all vouchers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get vouchers successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<VoucherDTO>> getAllVouchers(
            @PageableDefault(size = 10, sort = "price") Pageable pageable
    ) {
        log.info("Request to get all vouchers: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        return ResponseEntity.ok(voucherService.findAll(pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get vouchers by userId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get vouchers successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<VoucherDTO>> getVoucherByUserId(@PathVariable String userId) {
        log.info("Request to get vouchers for {} user id", userId);
        return ResponseEntity.ok(voucherService.findAllByUserId(userId));
    }

    @GetMapping("/hot")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all hot vouchers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get hot vouchers successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<VoucherDTO>> getAllHotVouchers(
            @PageableDefault(size = 10, sort = "price") Pageable pageable
    ) {
        log.info("Request to get all vouchers: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        return ResponseEntity.ok(voucherService.findAllHotVouchers(pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Get current user vouchers",
            description = "Returns all vouchers of currently authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vouchers retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<VoucherDTO>> getCurrentUserVouchers(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "price") Pageable pageable
    ) {
        log.info("Request to get vouchers for {}", authentication.getName());
        return ResponseEntity.ok(voucherService.getCurrentUserVouchers(authentication.getName(), pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create voucher")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Voucher created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<VoucherDTO> createVoucher(@Valid @RequestBody VoucherDTO voucherDTO) {
        log.info("Request to create voucher: title={}, tourType={}", voucherDTO.getTitle(), voucherDTO.getTourType());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(voucherService.create(voucherDTO));
    }

    @PatchMapping("/{voucherId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update voucher")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voucher updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<VoucherDTO> updateVoucher(
            @PathVariable String voucherId,
            @RequestBody VoucherDTO voucherDTO
    ) {
        log.info("Request to update voucher: voucherId={}", voucherId);
        log.debug("Update voucher payload: title={}, tourType={}, price={}",
                voucherDTO.getTitle(), voucherDTO.getTourType(), voucherDTO.getPrice());

        return ResponseEntity.ok(voucherService.update(voucherId, voucherDTO));
    }

    @DeleteMapping("/{voucherId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete voucher by voucherId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voucher deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteVoucherByVoucherId(@PathVariable String voucherId) {
        log.info("Request to delete {} voucher", voucherId);
        voucherService.delete(voucherId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{voucherId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Change status for voucher")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voucher status changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<VoucherDTO> changeStatus(
            @PathVariable String voucherId,
            @RequestBody ChangeVoucherStatusRequestDTO changeStatusRequestDTO
    ) {
        log.info("Request to change status {} for {} voucher",changeStatusRequestDTO.getStatus(), voucherId);
        return ResponseEntity.ok(voucherService.changeStatus(voucherId, changeStatusRequestDTO));
    }

    @PatchMapping("/{voucherId}/status/hot")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Change hot status for voucher")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voucher hot status changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<VoucherDTO> changeHotStatus(
            @PathVariable String voucherId,
            @RequestBody ChangeHotStatusRequestDTO changeHotStatusRequestDTO) {
        log.info("Request to change hot status for {} voucher", voucherId);
        return ResponseEntity.ok(voucherService.changeHotStatus(voucherId, changeHotStatusRequestDTO));
    }

    @PostMapping("/{voucherId}/order")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Order voucher")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voucher ordered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<VoucherDTO> orderVoucher(
            Authentication authentication,
            @PathVariable String voucherId) {
        log.info("Request to order {} voucher for {}", voucherId, authentication.getName());
        return ResponseEntity.ok(voucherService.order(voucherId, authentication.getName()));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search voucher by parameters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get vouchers successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<VoucherDTO>> searchVouchers(
            @RequestParam(required = false) String tourType,
            @RequestParam(required = false) String transferType,
            @RequestParam(required = false) String hotelType,
            @RequestParam(required = false) Double maxPrice
    ) {
        log.info("Request to search voucher for {} tourType, {} transferType, {} hotelType, {} maxPrice",
                tourType, transferType, hotelType, maxPrice);
        return ResponseEntity.ok(voucherService.search(tourType, transferType, hotelType, maxPrice));
    }
}
