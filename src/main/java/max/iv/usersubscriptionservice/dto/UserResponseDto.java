package max.iv.usersubscriptionservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto (

        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){

}
