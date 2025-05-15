package max.iv.usersubscriptionservice.service;

import max.iv.usersubscriptionservice.dto.SubscriptionCreateRequestDto;
import max.iv.usersubscriptionservice.dto.SubscriptionResponseDto;
import max.iv.usersubscriptionservice.dto.TopSubscriptionDto;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {

    SubscriptionResponseDto addSubscriptionToUser(UUID userId, SubscriptionCreateRequestDto subscriptionCreateDto);
    List<SubscriptionResponseDto> getUserSubscriptions(UUID userId);
    void removeSubscriptionFromUser(UUID userId, UUID subscriptionId);
    List<TopSubscriptionDto> getTop3PopularSubscriptions();
}
