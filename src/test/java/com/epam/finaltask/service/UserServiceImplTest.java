package com.epam.finaltask.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.epam.finaltask.mapper.interfaces.UserMapper;
import com.epam.finaltask.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private UserServiceImpl userService;

//  @Test
//  void getUserByUsername_UserExists_Success() {
//    // Given
//    String username = "existingUser";
//    User user = new User();
//    user.setUsername(username);
//
//    UserDTO expectedUserDTO = new UserDTO();
//    expectedUserDTO.setUsername(username);
//
//    when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
//    when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedUserDTO);
//
//    // When
//    UserDTO result = userService.getUserByUsername(username);
//
//    // Then
//    assertNotNull(result, "The returned UserDTO should not be null");
//    assertEquals(expectedUserDTO.getUsername(), result.getUsername(),
//        "The username should match the expected value");
//
//    verify(userRepository, times(1)).findUserByUsername(username);
//    verify(userMapper, times(1)).toUserDTO(any(User.class));
//  }




//  @Test
//  void getUserById_UserExist_Success() {
//    // Given
//    UUID id = UUID.randomUUID();
//    User user = new User();
//    user.setId(id);
//
//    UserDTO expectedUserDTO = new UserDTO();
//    expectedUserDTO.setId(id.toString());
//
//    when(userRepository.findById(id)).thenReturn(Optional.of(user));
//    when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedUserDTO);
//
//    // When
//    UserDTO resultDTO = userService.getUserById(id);
//
//    // Then
//    assertNotNull(resultDTO, "The returned UserDTO should not be null");
//    assertEquals(expectedUserDTO.getId(), resultDTO.getId(),
//        "The user ID should match the expected value");
//
//    verify(userRepository, times(1)).findById(id);
//    verify(userMapper, times(1)).toUserDTO(any(User.class));
//  }

}
