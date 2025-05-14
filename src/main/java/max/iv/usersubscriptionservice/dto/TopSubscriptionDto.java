package max.iv.usersubscriptionservice.dto;

import max.iv.usersubscriptionservice.models.enums.ServiceName;

public record TopSubscriptionDto (
        ServiceName serviceName,
        String serviceDisplayName,
        long subscriptionCount
) {

}
