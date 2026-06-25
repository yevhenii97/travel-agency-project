package com.epam.finaltask.restcontroller;

import com.epam.finaltask.config.security.SecurityConfig;
import com.epam.finaltask.dto.user.ChangeVoucherStatusRequestDTO;
import com.epam.finaltask.dto.voucher.ChangeHotStatusRequestDTO;
import com.epam.finaltask.dto.voucher.VoucherDTO;
import com.epam.finaltask.model.enums.HotelType;
import com.epam.finaltask.model.enums.TourType;
import com.epam.finaltask.model.enums.TransferType;
import com.epam.finaltask.model.enums.VoucherStatus;
import com.epam.finaltask.service.VoucherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoucherRestController.class)
@Import(SecurityConfig.class)
@EnableMethodSecurity
class VoucherRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VoucherService voucherService;

    @Test
    @DisplayName("Should get all vouchers for authenticated user")
    @WithMockUser(roles = "USER")
    void test1() throws Exception {
        VoucherDTO voucher = createVoucherDTO();

        when(voucherService.findAll(any()))
                .thenReturn(List.of(voucher));

        mockMvc.perform(get("/api/vouchers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Summer Tour"))
                .andExpect(jsonPath("$[0].price").value(500.0));

        verify(voucherService).findAll(any());
    }


    @Test
    @DisplayName("Should get vouchers by user id for admin")
    @WithMockUser(roles = "ADMIN")
    void test3() throws Exception {
        String userId = UUID.randomUUID().toString();
        VoucherDTO voucher = createVoucherDTO();

        when(voucherService.findAllByUserId(userId))
                .thenReturn(List.of(voucher));

        mockMvc.perform(get("/api/vouchers/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Summer Tour"));

        verify(voucherService).findAllByUserId(userId);
    }

    @Test
    @DisplayName("Should get vouchers by user id for manager")
    @WithMockUser(roles = "MANAGER")
    void test4() throws Exception {
        String userId = UUID.randomUUID().toString();
        VoucherDTO voucher = createVoucherDTO();

        when(voucherService.findAllByUserId(userId))
                .thenReturn(List.of(voucher));

        mockMvc.perform(get("/api/vouchers/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Summer Tour"));

        verify(voucherService).findAllByUserId(userId);
    }

    @Test
    @DisplayName("Should deny getting vouchers by user id for regular user")
    @WithMockUser(roles = "USER")
    void test5() throws Exception {
        String userId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/vouchers/user/{userId}", userId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get all hot vouchers for authenticated user")
    @WithMockUser(roles = "USER")
    void test6() throws Exception {
        VoucherDTO voucher = createVoucherDTO();
        voucher.setIsHot(true);

        when(voucherService.findAllHotVouchers(any()))
                .thenReturn(List.of(voucher));

        mockMvc.perform(get("/api/vouchers/hot")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isHot").value(true));

        verify(voucherService).findAllHotVouchers(any());
    }

    @Test
    @DisplayName("Should get current user vouchers")
    @WithMockUser(username = "user01", roles = "USER")
    void test7() throws Exception {
        VoucherDTO voucher = createVoucherDTO();

        when(voucherService.getCurrentUserVouchers(eq("user01"), any()))
                .thenReturn(List.of(voucher));

        mockMvc.perform(get("/api/vouchers/me")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Summer Tour"));

        verify(voucherService).getCurrentUserVouchers(eq("user01"), any());
    }

    @Test
    @DisplayName("Should deny current user vouchers for admin")
    @WithMockUser(roles = "ADMIN")
    void test8() throws Exception {
        mockMvc.perform(get("/api/vouchers/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should create voucher for admin")
    @WithMockUser(roles = "ADMIN")
    void test9() throws Exception {
        VoucherDTO request = createVoucherDTO();
        VoucherDTO response = createVoucherDTO();

        when(voucherService.create(any(VoucherDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/vouchers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Summer Tour"))
                .andExpect(jsonPath("$.price").value(500.0));

        verify(voucherService).create(any(VoucherDTO.class));
    }

    @Test
    @DisplayName("Should deny create voucher for regular user")
    @WithMockUser(roles = "USER")
    void test10() throws Exception {
        VoucherDTO request = createVoucherDTO();

        mockMvc.perform(post("/api/vouchers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should update voucher for admin")
    @WithMockUser(roles = "ADMIN")
    void test11() throws Exception {
        String voucherId = UUID.randomUUID().toString();

        VoucherDTO request = createVoucherDTO();
        request.setTitle("Updated Tour");

        VoucherDTO response = createVoucherDTO();
        response.setTitle("Updated Tour");

        when(voucherService.update(eq(voucherId), any(VoucherDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/vouchers/{voucherId}", voucherId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Tour"));

        verify(voucherService).update(eq(voucherId), any(VoucherDTO.class));
    }

    @Test
    @DisplayName("Should delete voucher for admin")
    @WithMockUser(roles = "ADMIN")
    void test12() throws Exception {
        String voucherId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/vouchers/{voucherId}", voucherId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(voucherService).delete(voucherId);
    }

    @Test
    @DisplayName("Should deny delete voucher for regular user")
    @WithMockUser(roles = "USER")
    void test13() throws Exception {
        String voucherId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/vouchers/{voucherId}", voucherId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should change voucher status for admin")
    @WithMockUser(roles = "ADMIN")
    void test14() throws Exception {
        String voucherId = UUID.randomUUID().toString();

        ChangeVoucherStatusRequestDTO request = new ChangeVoucherStatusRequestDTO();
        request.setStatus("PAID");

        VoucherDTO response = createVoucherDTO();
        response.setStatus(VoucherStatus.PAID.name());

        when(voucherService.changeStatus(eq(voucherId), any(ChangeVoucherStatusRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/vouchers/{voucherId}/status", voucherId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(voucherService).changeStatus(eq(voucherId), any(ChangeVoucherStatusRequestDTO.class));
    }

    @Test
    @DisplayName("Should change voucher status for manager")
    @WithMockUser(roles = "MANAGER")
    void test15() throws Exception {
        String voucherId = UUID.randomUUID().toString();

        ChangeVoucherStatusRequestDTO request = new ChangeVoucherStatusRequestDTO();
        request.setStatus("CANCELED");

        VoucherDTO response = createVoucherDTO();
        response.setStatus(VoucherStatus.CANCELED.name());

        when(voucherService.changeStatus(eq(voucherId), any(ChangeVoucherStatusRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/vouchers/{voucherId}/status", voucherId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(voucherService).changeStatus(eq(voucherId), any(ChangeVoucherStatusRequestDTO.class));
    }

    @Test
    @DisplayName("Should deny changing voucher status for regular user")
    @WithMockUser(roles = "USER")
    void test16() throws Exception {
        String voucherId = UUID.randomUUID().toString();

        ChangeVoucherStatusRequestDTO request = new ChangeVoucherStatusRequestDTO();
        request.setStatus("PAID");

        mockMvc.perform(patch("/api/vouchers/{voucherId}/status", voucherId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should change voucher hot status for admin")
    @WithMockUser(roles = "ADMIN")
    void test17() throws Exception {
        String voucherId = UUID.randomUUID().toString();

        ChangeHotStatusRequestDTO request = new ChangeHotStatusRequestDTO();
        request.setHot(true);

        VoucherDTO response = createVoucherDTO();
        response.setIsHot(true);

        when(voucherService.changeHotStatus(eq(voucherId), any(ChangeHotStatusRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/vouchers/{voucherId}/status/hot", voucherId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isHot").value(true));

        verify(voucherService).changeHotStatus(eq(voucherId), any(ChangeHotStatusRequestDTO.class));
    }

    @Test
    @DisplayName("Should change voucher hot status for manager")
    @WithMockUser(roles = "MANAGER")
    void test18() throws Exception {
        String voucherId = UUID.randomUUID().toString();

        ChangeHotStatusRequestDTO request = new ChangeHotStatusRequestDTO();
        request.setHot(true);

        VoucherDTO response = createVoucherDTO();
        response.setIsHot(true);

        when(voucherService.changeHotStatus(eq(voucherId), any(ChangeHotStatusRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/vouchers/{voucherId}/status/hot", voucherId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isHot").value(true));

        verify(voucherService).changeHotStatus(eq(voucherId), any(ChangeHotStatusRequestDTO.class));
    }

    @Test
    @DisplayName("Should order voucher for user")
    @WithMockUser(username = "user01", roles = "USER")
    void test19() throws Exception {
        String voucherId = UUID.randomUUID().toString();

        VoucherDTO response = createVoucherDTO();
        response.setStatus(VoucherStatus.REGISTERED.name());

        when(voucherService.order(voucherId, "user01"))
                .thenReturn(response);

        mockMvc.perform(post("/api/vouchers/{voucherId}/order", voucherId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REGISTERED"));

        verify(voucherService).order(voucherId, "user01");
    }

    @Test
    @DisplayName("Should deny order voucher for admin")
    @WithMockUser(roles = "ADMIN")
    void test20() throws Exception {
        String voucherId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/vouchers/{voucherId}/order", voucherId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    private VoucherDTO createVoucherDTO() {
        VoucherDTO dto = new VoucherDTO();
        dto.setId(String.valueOf(UUID.randomUUID()));
        dto.setTitle("Summer Tour");
        dto.setDescription("Summer tour description");
        dto.setPrice(500.0);
        dto.setTourType(TourType.LEISURE.name());
        dto.setTransferType(TransferType.PLANE.name());
        dto.setHotelType(HotelType.FIVE_STARS.name());
        dto.setStatus(VoucherStatus.REGISTERED.name());
        dto.setArrivalDate(LocalDate.now().plusDays(10));
        dto.setEvictionDate(LocalDate.now().plusDays(20));
        dto.setIsHot(false);
        return dto;
    }
}
