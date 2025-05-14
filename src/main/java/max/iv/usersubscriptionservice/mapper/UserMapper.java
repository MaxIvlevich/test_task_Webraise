package max.iv.usersubscriptionservice.mapper;

import max.iv.usersubscriptionservice.dto.UserCreateRequestDto;
import max.iv.usersubscriptionservice.dto.UserResponseDto;
import max.iv.usersubscriptionservice.dto.UserUpdateRequestDto;
import max.iv.usersubscriptionservice.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toUserResponseDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public User toUser(UserCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        return user;
    }

    public void updateUserFromDto(UserUpdateRequestDto dto, User user) {
        if (dto.username() != null && !dto.username().isBlank()) {
            user.setUsername(dto.username());
        }
        if (dto.email() != null && !dto.email().isBlank()) {
            user.setEmail(dto.email());
        }
        if (dto.firstName() != null) {
            user.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            user.setLastName(dto.lastName());
        }
    }
}
