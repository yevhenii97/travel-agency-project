package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.model.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public User toUser(UserDTO dto) {
        return User.builder()
                .username(dto.getUsername())
                .phoneNumber(dto.getPhoneNumber())
                .balance(BigDecimal.valueOf(dto.getBalance()))
                .active(dto.isActive()).build();
    }

    @Override
    public UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .balance(user.getBalance().doubleValue())
                .role(user.getRole().name())
                .active(user.isActive())
                .build();
    }
}
