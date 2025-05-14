package max.iv.usersubscriptionservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import max.iv.usersubscriptionservice.dto.UserCreateRequestDto;
import max.iv.usersubscriptionservice.dto.UserResponseDto;
import max.iv.usersubscriptionservice.dto.UserUpdateRequestDto;
import max.iv.usersubscriptionservice.dto.UserWithSubscriptionNamesDto;
import max.iv.usersubscriptionservice.exception.ResourceNotFoundException;
import max.iv.usersubscriptionservice.models.User;
import max.iv.usersubscriptionservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateRequestDto userCreateDto) {
        log.info("Received request to create user with username: {}", userCreateDto.username());
        UserResponseDto createdUser = userService.createUser(userCreateDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.id())
                .toUri();

        log.info("User created successfully with ID: {}, returning 201 Created", createdUser.id());
        return ResponseEntity.created(location).body(createdUser); // Возвращаем 201 Created
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        log.info("Received request to get user by ID: {}", id);
        UserResponseDto user = userService.getUserById(id);
        log.info("User found with ID: {}, returning 200 OK", id);
        return ResponseEntity.ok(user); // Возвращаем 200 OK
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID id,
                                                      @Valid @RequestBody UserUpdateRequestDto userUpdateDto) {
        log.info("Received request to update user with ID: {}", id);
        UserResponseDto updatedUser = userService.updateUser(id, userUpdateDto);
        log.info("User updated successfully with ID: {}, returning 200 OK", id);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        log.info("Received request to delete user with ID: {}", id);
        userService.deleteUser(id);
        log.info("User deleted successfully with ID: {}, returning 204 No Content", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<UserWithSubscriptionNamesDto>> getAllUsersWithSubscriptions(
            @PageableDefault(size = 10, sort = "username") Pageable pageable) {
        log.info("Received request to get all users with their subscriptions. Pageable: {}", pageable);
        Page<UserWithSubscriptionNamesDto> usersPage = userService.getAllUsersWithSubscriptions(pageable);
        log.info("Returning page {} of {} users with their subscriptions, status 200 OK",
                usersPage.getNumber(), usersPage.getNumberOfElements());
        return ResponseEntity.ok(usersPage);
    }
     @GetMapping("/{id}/with-subscriptions")
     public ResponseEntity<UserWithSubscriptionNamesDto> getUserByIdWithSubscriptions(@PathVariable UUID id) {
         log.info("Received request to get user by ID with subscriptions: {}", id);
         UserWithSubscriptionNamesDto user = userService.getUserByIdWithSubscriptions(id);
         log.info("User found with ID: {}, returning 200 OK", id);
         return ResponseEntity.ok(user);
     }


}
