package max.iv.usersubscriptionservice.repository;

import max.iv.usersubscriptionservice.dto.TopSubscriptionDto;
import max.iv.usersubscriptionservice.models.Subscription;
import max.iv.usersubscriptionservice.models.User;
import max.iv.usersubscriptionservice.models.enums.ServiceName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUser(User user);

    List<Subscription> findByUserId(UUID userId);

    Optional<Subscription> findByIdAndUserId(UUID subscriptionId, UUID userId);

    @Query("SELECT s.serviceName, COUNT(s) as subscriptionCount " +
            "FROM Subscription s " +
            "GROUP BY s.serviceName " +
            "ORDER BY subscriptionCount DESC " +
            "LIMIT 3")
    List<Object[]> findTop3RawData();

    Optional<Subscription> findByUserIdAndServiceName(UUID userId, ServiceName serviceName);
}
