package max.iv.usersubscriptionservice.repository;

import max.iv.usersubscriptionservice.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM User u")
    Page<UUID> findUserIds(Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.subscriptions s WHERE u.id IN :userIds")
    List<User> findUsersWithSubscriptionsByIds(@Param("userIds") List<UUID> userIds);

   // @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.subscriptions",
   //               countQuery = "SELECT count(DISTINCT u.id) FROM User u")
   // Page<User> findAllWithSubscriptions(Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.subscriptions WHERE u.id = :userId")
    Optional<User> findByIdWithSubscriptions(UUID userId);
}
