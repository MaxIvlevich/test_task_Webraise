package max.iv.usersubscriptionservice;
import max.iv.usersubscriptionservice.dto.UserCreateRequestDto;
import max.iv.usersubscriptionservice.dto.UserResponseDto;
import max.iv.usersubscriptionservice.dto.UserUpdateRequestDto;
import max.iv.usersubscriptionservice.models.User;
import max.iv.usersubscriptionservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test-user-subscription-db")
            .withUsername("testuser")
            .withPassword("testpass");


    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");
    }

    private String baseUrl;

    @BeforeAll
    static void beforeAll() {

    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/users";
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUserSuccessfully() {
        UserCreateRequestDto createDto = new UserCreateRequestDto(
                "testuser", "test@example.com", "password123", "Test", "User"
        );

        ResponseEntity<UserResponseDto> response = restTemplate.postForEntity(baseUrl, createDto, UserResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        UserResponseDto createdUser = response.getBody();
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.id()).isNotNull();
        assertThat(createdUser.username()).isEqualTo("testuser");
        assertThat(createdUser.email()).isEqualTo("test@example.com");

        // Дополнительная проверка в БД
        User userInDb = userRepository.findById(createdUser.id()).orElse(null);
        assertThat(userInDb).isNotNull();
        assertThat(userInDb.getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldFailToCreateUserWithDuplicateUsername() {
        // Сначала создаем пользователя
        UserCreateRequestDto createDto1 = new UserCreateRequestDto(
                "duplicateuser", "user1@example.com", "password123", "First", "User"
        );
        restTemplate.postForEntity(baseUrl, createDto1, UserResponseDto.class);

        // Пытаемся создать второго с тем же username
        UserCreateRequestDto createDto2 = new UserCreateRequestDto(
                "duplicateuser", "user2@example.com", "password123", "Second", "User"
        );
        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, createDto2, Map.class); // Ожидаем ошибку

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        Map<String, Object> errorBody = response.getBody();
        assertThat(errorBody).isNotNull();
        assertThat(errorBody.get("message")).asString().contains("User with username duplicateuser already exists");
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        // 1. Создаем пользователя
        UserCreateRequestDto createDto = new UserCreateRequestDto("getuser", "get@example.com", "pass", "Get", "Me");
        UserResponseDto createdUser = restTemplate.postForObject(baseUrl, createDto, UserResponseDto.class);
        assertThat(createdUser).isNotNull();
        UUID userId = createdUser.id();

        // 2. Получаем пользователя по ID
        ResponseEntity<UserResponseDto> response = restTemplate.getForEntity(baseUrl + "/" + userId, UserResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponseDto fetchedUser = response.getBody();
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.id()).isEqualTo(userId);
        assertThat(fetchedUser.username()).isEqualTo("getuser");
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() {
        UUID nonExistentId = UUID.randomUUID();
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/" + nonExistentId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, Object> errorBody = response.getBody();
        assertThat(errorBody).isNotNull();
        assertThat(errorBody.get("message")).asString().contains("User not found with ID: " + nonExistentId);
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        // 1. Создаем пользователя
        UserCreateRequestDto createDto = new UserCreateRequestDto(
                "updateuser", "update@example.com", "pass", "Initial", "Name");
        UserResponseDto createdUser = restTemplate.postForObject(baseUrl, createDto, UserResponseDto.class);
        assertThat(createdUser).isNotNull();
        UUID userId = createdUser.id();

        // 2. Обновляем пользователя
        UserUpdateRequestDto updateDto = new UserUpdateRequestDto(
                "updateduser", "updated@example.com", "Updated", "UpdatedName","UpdatedLastName");

        // Используем HttpEntity для PUT с телом запроса
        HttpEntity<UserUpdateRequestDto> requestUpdate = new HttpEntity<>(updateDto);
        ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                baseUrl + "/" + userId, HttpMethod.PUT, requestUpdate, UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponseDto updatedUser = response.getBody();
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.username()).isEqualTo("updateduser");
        assertThat(updatedUser.email()).isEqualTo("updated@example.com");
        assertThat(updatedUser.firstName()).isEqualTo("UpdatedName");
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // 1. Создаем пользователя
        UserCreateRequestDto createDto = new UserCreateRequestDto("deleteuser", "delete@example.com", "pass", "To", "Delete");
        UserResponseDto createdUser = restTemplate.postForObject(baseUrl, createDto, UserResponseDto.class);
        assertThat(createdUser).isNotNull();
        UUID userId = createdUser.id();

        // 2. Удаляем пользователя
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + userId, HttpMethod.DELETE, null, Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 3. Проверяем, что пользователь действительно удален (попытка получить его должна вернуть 404)
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(baseUrl + "/" + userId, Map.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // 4. Проверка в БД
        assertThat(userRepository.findById(userId)).isEmpty();
    }
}
