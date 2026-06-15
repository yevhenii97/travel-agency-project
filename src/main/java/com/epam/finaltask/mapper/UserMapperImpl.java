package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.user.UserDTO;
import com.epam.finaltask.dto.user.UserResponseDTO;
import com.epam.finaltask.mapper.interfaces.UserMapper;
import com.epam.finaltask.model.entities.User;
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
                .active(dto.getIsActive()).build();
    }

    @Override
    public UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .balance(user.getBalance().doubleValue())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .build();
    }

    @Override
    public UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(String.valueOf(user.getId()))
                .username(user.getUsername())
                .role(String.valueOf(user.getRole()))
                .phoneNumber(user.getPhoneNumber())
                .balance(user.getBalance().doubleValue())
                .active(user.isActive())
                .build();
    }
}
