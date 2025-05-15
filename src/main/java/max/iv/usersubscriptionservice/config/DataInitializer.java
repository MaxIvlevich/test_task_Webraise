package max.iv.usersubscriptionservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import max.iv.usersubscriptionservice.models.Subscription;
import max.iv.usersubscriptionservice.models.User;
import max.iv.usersubscriptionservice.models.enums.ServiceName;
import max.iv.usersubscriptionservice.repository.SubscriptionRepository;
import max.iv.usersubscriptionservice.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting data initialization...");

        if (userRepository.count() == 0) {
            log.info("No users found, proceeding with data initialization.");

            User user1 = User.builder()
                    .username("max.iv")
                    .email("max.iv@example.com")
                    .password("password123")
                    .firstName("Max")
                    .lastName("Iv")
                    .build();

            User user2 = User.builder()
                    .username("Sveta.Iv")
                    .email("Sveta.Iv@example.com")
                    .password("securePass!")
                    .firstName("Sveta")
                    .lastName("Iv")
                    .build();

            User user3 = User.builder()
                    .username("alisia.milano")
                    .email("alisia.milano@example.com")
                    .password("alisiasPass")
                    .firstName("Alisia")
                    .lastName("Milano")
                    .build();

            userRepository.saveAll(List.of(user1, user2, user3));
            log.info("Saved initial users.");

            Subscription sub1_1 = Subscription.builder()
                    .user(user1)
                    .serviceName(ServiceName.YOUTUBE_PREMIUM)
                    .startDate(LocalDate.now().minusMonths(1))
                    .endDate(LocalDate.now().plusMonths(11))
                    .build();
            Subscription sub1_2 = Subscription.builder()
                    .user(user1)
                    .serviceName(ServiceName.NETFLIX_STANDARD)
                    .startDate(LocalDate.now().minusDays(10))
                    .endDate(LocalDate.now().plusDays(20))
                    .build();

            Subscription sub2_1 = Subscription.builder()
                    .user(user2)
                    .serviceName(ServiceName.YANDEX_PLUS)
                    .startDate(LocalDate.now())
                    .build();
            Subscription sub2_2 = Subscription.builder()
                    .user(user2)
                    .serviceName(ServiceName.VK_MUSIC)
                    .startDate(LocalDate.now().minusWeeks(2))
                    .endDate(LocalDate.now().plusMonths(3))
                    .build();
            Subscription sub2_3 = Subscription.builder()
                    .user(user2)
                    .serviceName(ServiceName.YOUTUBE_PREMIUM)
                    .startDate(LocalDate.now().minusDays(5))
                    .endDate(LocalDate.now().plusMonths(1))
                    .build();

            Subscription sub3_1 = Subscription.builder()
                    .user(user3)
                    .serviceName(ServiceName.SPOTIFY_PREMIUM)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .build();

            subscriptionRepository.saveAll(List.of(sub1_1, sub1_2, sub2_1, sub2_2, sub2_3, sub3_1));
            log.info("Saved initial subscriptions.");

        } else {
            log.info("Database already contains data. Skipping initialization.");
        }

        log.info("Data initialization finished.");
    }
}
