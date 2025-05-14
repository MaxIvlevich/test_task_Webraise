package max.iv.usersubscriptionservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import max.iv.usersubscriptionservice.dto.SubscriptionCreateRequestDto;
import max.iv.usersubscriptionservice.dto.SubscriptionResponseDto;
import max.iv.usersubscriptionservice.dto.TopSubscriptionDto;
import max.iv.usersubscriptionservice.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping("/users/{userId}/subscriptions")
    public ResponseEntity<SubscriptionResponseDto> addSubscription(
            @PathVariable UUID userId,
            @Valid @RequestBody SubscriptionCreateRequestDto subscriptionCreateDto) {
        log.info("Received request to add subscription for user ID: {}", userId);
        SubscriptionResponseDto createdSubscription = subscriptionService.addSubscriptionToUser(userId, subscriptionCreateDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{subscriptionId}")
                .buildAndExpand(createdSubscription.id())
                .toUri();

        log.info("Subscription added successfully with ID: {} for user ID: {}, returning 201 Created",
                createdSubscription.id(), userId);
        return ResponseEntity.created(location).body(createdSubscription);
    }

    @GetMapping("/users/{userId}/subscriptions")
    public ResponseEntity<List<SubscriptionResponseDto>> getUserSubscriptions(@PathVariable UUID userId) {
        log.info("Received request to get subscriptions for user ID: {}", userId);
        List<SubscriptionResponseDto> subscriptions = subscriptionService.getUserSubscriptions(userId);
        log.info("Found {} subscriptions for user ID: {}, returning 200 OK", subscriptions.size(), userId);
        return ResponseEntity.ok(subscriptions);
    }

    @DeleteMapping("/users/{userId}/subscriptions/{subscriptionId}")
    public ResponseEntity<Void> removeSubscription(
            @PathVariable UUID userId,
            @PathVariable UUID subscriptionId) {
        log.info("Received request to remove subscription ID: {} for user ID: {}", subscriptionId, userId);
        subscriptionService.removeSubscriptionFromUser(userId, subscriptionId);
        log.info("Subscription ID: {} removed successfully for user ID: {}, returning 204 No Content", subscriptionId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscriptions/top")
    public ResponseEntity<List<TopSubscriptionDto>> getTopSubscriptions() {
        log.info("Received request to get top 3 popular subscriptions");
        List<TopSubscriptionDto> topSubscriptions = subscriptionService.getTop3PopularSubscriptions();
        log.info("Returning {} top subscriptions, status 200 OK", topSubscriptions.size());
        return ResponseEntity.ok(topSubscriptions);
    }
}
