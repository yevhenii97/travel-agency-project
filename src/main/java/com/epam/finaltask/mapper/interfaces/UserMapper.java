package com.epam.finaltask.mapper.interfaces;

import com.epam.finaltask.dto.user.UserDTO;
import com.epam.finaltask.dto.user.UserResponseDTO;
import com.epam.finaltask.model.entities.User;

public interface UserMapper {
    User toUser(UserDTO userDTO);
    UserDTO toUserDTO(User user);
    UserResponseDTO toUserUserResponseDTO(User user);
}
