package max.iv.usersubscriptionservice.service;

import max.iv.usersubscriptionservice.dto.UserCreateRequestDto;
import max.iv.usersubscriptionservice.dto.UserResponseDto;
import max.iv.usersubscriptionservice.dto.UserUpdateRequestDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDto createUser(UserCreateRequestDto userCreateDto);
    UserResponseDto getUserById(UUID userId);
    UserResponseDto updateUser(UUID userId, UserUpdateRequestDto userUpdateDto);
    void deleteUser(UUID userId);
    List<UserResponseDto> getAllUsers();
}
