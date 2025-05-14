package max.iv.usersubscriptionservice.service.impliment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import max.iv.usersubscriptionservice.dto.UserCreateRequestDto;
import max.iv.usersubscriptionservice.dto.UserResponseDto;
import max.iv.usersubscriptionservice.dto.UserUpdateRequestDto;
import max.iv.usersubscriptionservice.dto.UserWithSubscriptionNamesDto;
import max.iv.usersubscriptionservice.exception.DuplicateResourceException;
import max.iv.usersubscriptionservice.exception.ResourceNotFoundException;
import max.iv.usersubscriptionservice.mapper.UserMapper;
import max.iv.usersubscriptionservice.models.User;
import max.iv.usersubscriptionservice.repository.UserRepository;
import max.iv.usersubscriptionservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateRequestDto userCreateDto) {
        log.info("Creating user with username: {}", userCreateDto.username());
        if (userRepository.existsByUsername(userCreateDto.username())) {
            throw new DuplicateResourceException("User with username " + userCreateDto.username() + " already exists.");
        }
        if (userRepository.existsByEmail(userCreateDto.email())) {
            throw new DuplicateResourceException("User with email " + userCreateDto.email() + " already exists.");
        }

        User user = userMapper.toUser(userCreateDto);
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID userId) {
        log.info("Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });
        return userMapper.toUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(UUID userId, UserUpdateRequestDto userUpdateDto) {
        log.info("Updating user with ID: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for update with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });
        if (userUpdateDto.username() != null && !userUpdateDto.username().isBlank() &&
                !existingUser.getUsername().equals(userUpdateDto.username()) &&
                userRepository.existsByUsername(userUpdateDto.username())) {
            throw new DuplicateResourceException("Username " + userUpdateDto.username() + " is already taken.");
        }
        if (userUpdateDto.email() != null && !userUpdateDto.email().isBlank() &&
                !existingUser.getEmail().equals(userUpdateDto.email()) &&
                userRepository.existsByEmail(userUpdateDto.email())) {
            throw new DuplicateResourceException("Email " + userUpdateDto.email() + " is already taken.");
        }

        userMapper.updateUserFromDto(userUpdateDto, existingUser);
        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully with ID: {}", updatedUser.getId());
        return userMapper.toUserResponseDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.warn("User not found for deletion with ID: {}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User deleted successfully with ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserWithSubscriptionNamesDto getUserByIdWithSubscriptions(UUID userId) {
        log.info("Fetching user by ID with subscriptions: {}", userId);
        User user = userRepository.findByIdWithSubscriptions(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });
        return userMapper.toUserWithSubscriptionNamesDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public  Page<UserWithSubscriptionNamesDto> getAllUsersWithSubscriptions(Pageable pageable) {
        log.info("Fetching all users with their subscriptions, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<UUID> userIdPage = userRepository.findUserIds(pageable);
        List<UUID> userIdsOnPage = userIdPage.getContent();

        if (userIdsOnPage.isEmpty()) {
            return Page.empty(pageable);
        }
        List<User> usersWithSubscriptions = userRepository.findUsersWithSubscriptionsByIds(userIdsOnPage);

        List<UserWithSubscriptionNamesDto> dtoList = usersWithSubscriptions.stream()
                .map(userMapper::toUserWithSubscriptionNamesDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, userIdPage.getTotalElements());
    }
}
