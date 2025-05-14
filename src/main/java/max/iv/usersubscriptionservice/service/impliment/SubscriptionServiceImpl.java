package max.iv.usersubscriptionservice.service.impliment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import max.iv.usersubscriptionservice.dto.SubscriptionCreateRequestDto;
import max.iv.usersubscriptionservice.dto.SubscriptionResponseDto;
import max.iv.usersubscriptionservice.dto.TopSubscriptionDto;
import max.iv.usersubscriptionservice.exception.DuplicateResourceException;
import max.iv.usersubscriptionservice.exception.ResourceNotFoundException;
import max.iv.usersubscriptionservice.mapper.SubscriptionMapper;
import max.iv.usersubscriptionservice.models.Subscription;
import max.iv.usersubscriptionservice.models.User;
import max.iv.usersubscriptionservice.models.enums.ServiceName;
import max.iv.usersubscriptionservice.repository.SubscriptionRepository;
import max.iv.usersubscriptionservice.repository.UserRepository;
import max.iv.usersubscriptionservice.service.SubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    @Transactional
    public SubscriptionResponseDto addSubscriptionToUser(UUID userId, SubscriptionCreateRequestDto subscriptionCreateDto) {
        log.info("Adding subscription {} to user ID: {}", subscriptionCreateDto.serviceName(), userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for adding subscription, ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });
        boolean alreadySubscribed = subscriptionRepository.findByUserIdAndServiceName(userId, subscriptionCreateDto.serviceName())
                .isPresent();
        if (alreadySubscribed) {
            log.warn("User {} already subscribed to {}", userId, subscriptionCreateDto.serviceName());
            throw new DuplicateResourceException("User is already subscribed to " + subscriptionCreateDto.serviceName().getDisplayName());
        }

        Subscription subscription = subscriptionMapper.toSubscription(subscriptionCreateDto, user);
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription {} added successfully to user ID: {}, subscription ID: {}",
                savedSubscription.getServiceName(), userId, savedSubscription.getId());
        return subscriptionMapper.toSubscriptionResponseDto(savedSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDto> getUserSubscriptions(UUID userId) {
        log.info("Fetching subscriptions for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.warn("User not found for fetching subscriptions, ID: {}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        return subscriptions.stream()
                .map(subscriptionMapper::toSubscriptionResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeSubscriptionFromUser(UUID userId, UUID subscriptionId) {
        log.info("Removing subscription ID: {} from user ID: {}", subscriptionId, userId);
        if (!userRepository.existsById(userId)) {
            log.warn("User not found for removing subscription, ID: {}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        Subscription subscription = subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> {
                    log.warn("Subscription not found with ID: {} for user ID: {} or does not belong to the user", subscriptionId, userId);
                    return new ResourceNotFoundException(
                            "Subscription not found with ID: " + subscriptionId + " for user: " + userId);
                });
        subscriptionRepository.delete(subscription);
        log.info("Subscription ID: {} removed successfully from user ID: {}", subscriptionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopSubscriptionDto> getTop3PopularSubscriptions() {
        log.info("Fetching top 3 popular subscriptions");
        List<Object[]> rawData = subscriptionRepository.findTop3RawData();
        return rawData.stream()
                .map(row -> {
                    ServiceName serviceName = (ServiceName) row[0];
                    Number countNumber = (Number) row[1];
                    long count = (countNumber != null) ? countNumber.longValue() : 0L;
                    return subscriptionMapper.toTopSubscriptionDto(serviceName, count);
                })
                .collect(Collectors.toList());
    }
}
