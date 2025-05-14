package max.iv.usersubscriptionservice.dto;

import max.iv.usersubscriptionservice.models.enums.ServiceName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponseDto(
        UUID id,
        ServiceName serviceName,
        String serviceDisplayName,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID userId
) {
}
