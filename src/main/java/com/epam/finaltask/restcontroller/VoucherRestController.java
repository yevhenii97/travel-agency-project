package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherRestController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllVouchers() {
        return ResponseEntity.ok(
                Map.of("results", voucherService.findAll())
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getVoucherByUserId(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                Map.of("results", voucherService.findAllByUserId(userId))
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> createVoucher(
            @Valid @RequestBody VoucherDTO voucherDTO) {

        voucherService.create(voucherDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "statusCode", "OK",
                        "statusMessage", "Voucher is successfully created"
                ));
    }

    @PatchMapping("/{voucherId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateVoucher(
            @PathVariable String voucherId,
            @RequestBody VoucherDTO voucherDTO) {

        voucherService.update(voucherId, voucherDTO);

        return ResponseEntity.ok(
                Map.of(
                        "statusCode", "OK",
                        "statusMessage", "Voucher is successfully updated"
                )
        );
    }

    @DeleteMapping("/{voucherId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteVoucherByVoucherId(
            @PathVariable String voucherId) {

        voucherService.delete(voucherId);

        return ResponseEntity.ok(
                Map.of(
                        "statusCode", "OK",
                        "statusMessage", String.format(
                                "Voucher with Id %s has been deleted",
                                voucherId
                        )
                )
        );
    }

    @PatchMapping("/{voucherId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> changeStatus(
            @PathVariable String voucherId,
            @RequestBody VoucherDTO voucherDTO) {

        voucherService.changeHotStatus(voucherId, voucherDTO);

        return ResponseEntity.ok(
                Map.of(
                        "statusCode", "OK",
                        "statusMessage", "Voucher status is successfully changed"
                )
        );
    }







//    @GetMapping
//    public ResponseEntity<List<VoucherDTO>> getAllVouchers() {
//        return ResponseEntity.ok(voucherService.findAll());
//    }
//
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<VoucherDTO>> getVoucherByUserId(@PathVariable String userId) {
//        return ResponseEntity.ok(voucherService.findAllByUserId(userId));
//    }
//
//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<VoucherDTO> createVoucher(@Valid @RequestBody VoucherDTO voucherDTO) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(voucherService.create(voucherDTO));
//    }
//
//    @PatchMapping("/{voucherId}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<VoucherDTO> updateVoucher(
//            @PathVariable String voucherId,
//            @Valid @RequestBody VoucherDTO voucherDTO
//    ) {
//        return ResponseEntity.ok(voucherService.update(voucherId, voucherDTO));
//    }
//
//    @DeleteMapping("/{voucherId}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public void deleteVoucherByVoucherId(@PathVariable String voucherId) {
//        voucherService.delete(voucherId);
//    }
//
//    @PatchMapping("/{voucherId}/status")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
//    public ResponseEntity<VoucherDTO> changeStatus(
//            @PathVariable String voucherId,
//            @Valid @RequestBody VoucherDTO voucherDTO
//    ) {
//        return ResponseEntity.ok(voucherService.changeHotStatus(voucherId, voucherDTO));
//    }
}
