package max.iv.usersubscriptionservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import max.iv.usersubscriptionservice.models.enums.ServiceName;

import java.time.LocalDate;

public record SubscriptionCreateRequestDto(
        @NotNull(message = "Service name cannot be null")
        ServiceName serviceName,

        @NotNull(message = "Start date cannot be null")
        LocalDate startDate,

        @FutureOrPresent(message = "End date must be in the present or future")
        LocalDate endDate
) {
}
