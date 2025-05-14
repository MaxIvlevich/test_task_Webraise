package max.iv.usersubscriptionservice.mapper;

import max.iv.usersubscriptionservice.dto.SubscriptionCreateRequestDto;
import max.iv.usersubscriptionservice.dto.SubscriptionResponseDto;
import max.iv.usersubscriptionservice.dto.TopSubscriptionDto;
import max.iv.usersubscriptionservice.models.Subscription;
import max.iv.usersubscriptionservice.models.User;
import max.iv.usersubscriptionservice.models.enums.ServiceName;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {
    public SubscriptionResponseDto toSubscriptionResponseDto(Subscription subscription) {
        if (subscription == null) {
            return null;
        }
        return new SubscriptionResponseDto(
                subscription.getId(),
                subscription.getServiceName(),
                subscription.getServiceName().getDisplayName(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt(),
                subscription.getUser() != null ? subscription.getUser().getId() : null
        );
    }

    public Subscription toSubscription(SubscriptionCreateRequestDto dto, User user) {
        if (dto == null) {
            return null;
        }
        Subscription subscription = new Subscription();
        subscription.setServiceName(dto.serviceName());
        subscription.setStartDate(dto.startDate());
        subscription.setEndDate(dto.endDate());
        subscription.setUser(user);
        return subscription;
    }

    public TopSubscriptionDto toTopSubscriptionDto(ServiceName serviceName, long count) {
        return new TopSubscriptionDto(
                serviceName,
                serviceName.getDisplayName(),
                count
        );
    }
}
