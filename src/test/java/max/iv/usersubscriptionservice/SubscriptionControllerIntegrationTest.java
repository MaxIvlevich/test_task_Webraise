package max.iv.usersubscriptionservice;

import max.iv.usersubscriptionservice.dto.SubscriptionCreateRequestDto;
import max.iv.usersubscriptionservice.dto.SubscriptionResponseDto;
import max.iv.usersubscriptionservice.dto.TopSubscriptionDto;
import max.iv.usersubscriptionservice.dto.UserCreateRequestDto;
import max.iv.usersubscriptionservice.dto.UserResponseDto;
import max.iv.usersubscriptionservice.models.Subscription;
import max.iv.usersubscriptionservice.models.User;
import max.iv.usersubscriptionservice.models.enums.ServiceName;
import max.iv.usersubscriptionservice.repository.SubscriptionRepository;
import max.iv.usersubscriptionservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscriptionControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test-sub-db")
            .withUsername("testsubuser")
            .withPassword("testsubpass");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");
    }

    private String baseUserUrl;
    private String baseSubscriptionUrl;
    private User testUser;

    @BeforeEach
    void setUp() {
        baseUserUrl = "http://localhost:" + port + "/users";
        baseSubscriptionUrl = "http://localhost:" + port;
        // Очищаем данные перед каждым тестом
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестового пользователя для большинства тестов подписок
        UserCreateRequestDto userCreateDto = new UserCreateRequestDto(
                "subuser", "subuser@example.com", "password", "Sub", "User"
        );
        ResponseEntity<UserResponseDto> userResponse = restTemplate.postForEntity(baseUserUrl, userCreateDto, UserResponseDto.class);
        if (userResponse.getBody() != null) {
            testUser = new User();
            testUser.setId(userResponse.getBody().id());
            testUser.setUsername(userResponse.getBody().username());
        } else {
            throw new IllegalStateException("Failed to create test user for subscription tests");
        }
    }

    @Test
    void shouldAddSubscriptionToUser() {
        SubscriptionCreateRequestDto createDto = new SubscriptionCreateRequestDto(
                ServiceName.YOUTUBE_PREMIUM, LocalDate.now(), LocalDate.now().plusMonths(1)
        );
        String url = baseUserUrl + "/" + testUser.getId() + "/subscriptions";

        ResponseEntity<SubscriptionResponseDto> response = restTemplate.postForEntity(url, createDto, SubscriptionResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        SubscriptionResponseDto createdSubscription = response.getBody();
        assertThat(createdSubscription).isNotNull();
        assertThat(createdSubscription.id()).isNotNull();
        assertThat(createdSubscription.serviceName()).isEqualTo(ServiceName.YOUTUBE_PREMIUM);
        assertThat(createdSubscription.userId()).isEqualTo(testUser.getId());

        // Проверка в БД
        Optional<Subscription> subInDb = subscriptionRepository.findById(createdSubscription.id());
        assertThat(subInDb).isPresent();
        assertThat(subInDb.get().getServiceName()).isEqualTo(ServiceName.YOUTUBE_PREMIUM);
        assertThat(subInDb.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFailToAddDuplicateSubscription() {
        String url = baseUserUrl + "/" + testUser.getId() + "/subscriptions";
        SubscriptionCreateRequestDto createDto = new SubscriptionCreateRequestDto(
                ServiceName.NETFLIX_STANDARD, LocalDate.now(), null
        );

        // 1. Добавляем первую подписку
        restTemplate.postForEntity(url, createDto, SubscriptionResponseDto.class);

        // 2. Пытаемся добавить такую же
        ResponseEntity<Map> response = restTemplate.postForEntity(url, createDto, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message")).asString().contains("User is already subscribed to Netflix Standard");
    }

    @Test
    void shouldGetUserSubscriptions() {
        String url = baseUserUrl + "/" + testUser.getId() + "/subscriptions";
        // 1. Добавляем две подписки
        restTemplate.postForEntity(url, new SubscriptionCreateRequestDto(ServiceName.YANDEX_PLUS, LocalDate.now(), null), SubscriptionResponseDto.class);
        restTemplate.postForEntity(url, new SubscriptionCreateRequestDto(ServiceName.VK_MUSIC, LocalDate.now(), null), SubscriptionResponseDto.class);

        // 2. Получаем список подписок
        ResponseEntity<List<SubscriptionResponseDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SubscriptionResponseDto>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<SubscriptionResponseDto> subscriptions = response.getBody();
        assertThat(subscriptions).isNotNull();
        assertThat(subscriptions).hasSize(2);
        assertThat(subscriptions).extracting(SubscriptionResponseDto::serviceName)
                .containsExactlyInAnyOrder(ServiceName.YANDEX_PLUS, ServiceName.VK_MUSIC);
    }

    @Test
    void shouldReturnEmptyListForUserWithNoSubscriptions() {
        // Создаем нового пользователя без подписок
        UserCreateRequestDto newUserDto = new UserCreateRequestDto(
                "nosubuser", "nosub@example.com", "pass", "No", "Sub");
        UserResponseDto newUser = restTemplate.postForObject(baseUserUrl, newUserDto, UserResponseDto.class);
        assertThat(newUser).isNotNull();

        String url = baseUserUrl + "/" + newUser.id() + "/subscriptions";
        ResponseEntity<List<SubscriptionResponseDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SubscriptionResponseDto>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<SubscriptionResponseDto> subscriptions = response.getBody();
        assertThat(subscriptions).isNotNull();
        assertThat(subscriptions).isEmpty();
    }

    @Test
    void shouldRemoveSubscriptionFromUser() {
        String addUrl = baseUserUrl + "/" + testUser.getId() + "/subscriptions";
        SubscriptionCreateRequestDto createDto = new SubscriptionCreateRequestDto(
                ServiceName.SPOTIFY_PREMIUM, LocalDate.now(), null
        );
        // 1. Добавляем подписку
        SubscriptionResponseDto addedSubscription = restTemplate.postForObject(addUrl, createDto, SubscriptionResponseDto.class);
        assertThat(addedSubscription).isNotNull();
        UUID subscriptionId = addedSubscription.id();

        // 2. Удаляем подписку
        String deleteUrl = baseUserUrl + "/" + testUser.getId() + "/subscriptions/" + subscriptionId;
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                deleteUrl, HttpMethod.DELETE, null, Void.class
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 3. Проверяем, что подписка удалена
        assertThat(subscriptionRepository.findById(subscriptionId)).isEmpty();

        // 4. Проверяем, что при попытке получить подписки пользователя, этой уже нет
        ResponseEntity<List<SubscriptionResponseDto>> getResponse = restTemplate.exchange(
                addUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<SubscriptionResponseDto>>() {}
        );
        assertThat(getResponse.getBody()).noneMatch(sub -> sub.id().equals(subscriptionId));
    }

    @Test
    void shouldFailToRemoveNonExistentSubscription() {
        UUID nonExistentSubId = UUID.randomUUID();
        String deleteUrl = baseUserUrl + "/" + testUser.getId() + "/subscriptions/" + nonExistentSubId;
        ResponseEntity<Map> response = restTemplate.exchange(
                deleteUrl, HttpMethod.DELETE, null, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message")).asString().contains("Subscription not found with ID: " + nonExistentSubId);
    }

    @Test
    void shouldGetTop3PopularSubscriptions() {
        // 1. Создаем пользователей и подписки для статистики
        UserResponseDto user2 = restTemplate.postForObject(baseUserUrl, new UserCreateRequestDto(
                "user2", "u2@ex.com", "p","user2FN","user2LN"), UserResponseDto.class);
        // user3
        UserResponseDto user3 = restTemplate.postForObject(baseUserUrl, new UserCreateRequestDto(
                "user3", "u3@ex.com", "p","user3FN","user3LN"), UserResponseDto.class);

        // Добавляем подписки
        String urlU1 = baseUserUrl + "/" + testUser.getId() + "/subscriptions";
        String urlU2 = baseUserUrl + "/" + user2.id() + "/subscriptions";
        String urlU3 = baseUserUrl + "/" + user3.id() + "/subscriptions";

        // YouTube: 3
        restTemplate.postForEntity(urlU1, new SubscriptionCreateRequestDto(ServiceName.YOUTUBE_PREMIUM, LocalDate.now(),
                null), SubscriptionResponseDto.class);
        restTemplate.postForEntity(urlU2, new SubscriptionCreateRequestDto(ServiceName.YOUTUBE_PREMIUM, LocalDate.now(),
                null), SubscriptionResponseDto.class);
        restTemplate.postForEntity(urlU3, new SubscriptionCreateRequestDto(ServiceName.YOUTUBE_PREMIUM, LocalDate.now(),
                null), SubscriptionResponseDto.class);
        // Netflix: 2
        restTemplate.postForEntity(urlU1, new SubscriptionCreateRequestDto(ServiceName.NETFLIX_STANDARD, LocalDate.now(),
                null), SubscriptionResponseDto.class);
        restTemplate.postForEntity(urlU2, new SubscriptionCreateRequestDto(ServiceName.NETFLIX_STANDARD, LocalDate.now(),
                null), SubscriptionResponseDto.class);
        // Yandex: 1
        restTemplate.postForEntity(urlU3, new SubscriptionCreateRequestDto(ServiceName.YANDEX_PLUS,
                LocalDate.now(), null), SubscriptionResponseDto.class);
        // VK Music: 1 (не войдет в топ-3)
        restTemplate.postForEntity(urlU1, new SubscriptionCreateRequestDto(ServiceName.VK_MUSIC,
                LocalDate.now(), null), SubscriptionResponseDto.class);


        // 2. Получаем топ подписок
        String topUrl = baseSubscriptionUrl + "/subscriptions/top";
        ResponseEntity<List<TopSubscriptionDto>> response = restTemplate.exchange(
                topUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TopSubscriptionDto>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<TopSubscriptionDto> topSubscriptions = response.getBody();
        assertThat(topSubscriptions).isNotNull();
        assertThat(topSubscriptions).hasSize(3);

        // Проверяем порядок и содержимое (порядок важен, т.к. сортировка по убыванию count)
        assertThat(topSubscriptions.get(0).serviceName()).isEqualTo(ServiceName.YOUTUBE_PREMIUM);
        assertThat(topSubscriptions.get(0).subscriptionCount()).isEqualTo(3);
        assertThat(topSubscriptions.get(0).serviceDisplayName()).isEqualTo("YouTube Premium");

        assertThat(topSubscriptions.get(1).serviceName()).isEqualTo(ServiceName.NETFLIX_STANDARD);
        assertThat(topSubscriptions.get(1).subscriptionCount()).isEqualTo(2);

        assertThat(topSubscriptions.get(2).serviceName()).isEqualTo(ServiceName.VK_MUSIC);
        assertThat(topSubscriptions.get(2).subscriptionCount()).isEqualTo(1);
    }
}
