package max.iv.usersubscriptionservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Email(message = "Email should be valid")
        @Size(max = 100)
        String email,

        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        @Size(max = 50)
        String firstName,

        @Size(max = 50)
        String lastName
) {
}
