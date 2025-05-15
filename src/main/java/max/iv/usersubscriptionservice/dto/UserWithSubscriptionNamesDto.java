package max.iv.usersubscriptionservice.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserWithSubscriptionNamesDto (

        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> subscriptionNames

) {
}
