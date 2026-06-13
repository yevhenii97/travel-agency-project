package com.epam.finaltask.service;

import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDTO updateUser(String username, UserDTO userDTO) {
        User userEntity = userRepository.findUserByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        userEntity.setPhoneNumber(userDTO.getPhoneNumber());
        userEntity.setPassword(userDTO.getPassword());
        return userMapper.toUserDTO(userRepository.save(userEntity));
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User userEntity = userRepository.findUserByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.toUserDTO(userEntity);
    }

    @Override
    public UserDTO changeAccountStatus(UserDTO userDTO) {
        User existingUser = userRepository.findById(UUID.fromString(userDTO.getId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        User user = userMapper.toUser(userDTO);

        user.setId(existingUser.getId());
        user.setUsername(existingUser.getUsername());
        user.setPassword(existingUser.getPassword());
        user.setRole(existingUser.getRole());
        user.setPhoneNumber(existingUser.getPhoneNumber());
        user.setBalance(existingUser.getBalance());
        user.setActive(userDTO.isActive());

        User saved = userRepository.save(user);

        return userMapper.toUserDTO(saved);
    }

    @Override
    public UserDTO getUserById(UUID id) {
        User userEntity = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.toUserDTO(userEntity);
    }

}
