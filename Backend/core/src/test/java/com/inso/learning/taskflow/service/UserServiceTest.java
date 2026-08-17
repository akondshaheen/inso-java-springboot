package com.inso.learning.taskflow.service;

import com.inso.learning.taskflow.domain.Role;
import com.inso.learning.taskflow.domain.User;
import com.inso.learning.taskflow.exception.DuplicateResourceException;
import com.inso.learning.taskflow.exception.ResourceNotFoundException;
import com.inso.learning.taskflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * =============================================================================
 * A UNIT TEST FOR THE SERVICE LAYER, USING MOCKITO
 * =============================================================================
 *
 * WHAT IS THE DIFFERENCE BETWEEN A MOCK AND A REAL DEPENDENCY?
 * -------------------------------------------------------------------------
 * A real UserRepositoryImpl would need a live Hibernate session and a real
 * (or in-memory) database. A MOCK is a fake stand-in object that we
 * program to return exactly the values we want, so we can test
 * UserService's business logic completely on its own, without needing any
 * database at all. @Mock (from Mockito) creates that fake UserRepository,
 * and @InjectMocks creates a real UserService and automatically injects
 * the mock into its constructor.
 *
 * WHAT SHOULD BE UNIT TESTED VS INTEGRATION TESTED?
 * -------------------------------------------------------------------------
 * Business logic that lives in plain Java (like the duplicate-email check
 * below) is a perfect fit for a fast, isolated unit test - we do not need
 * a real database just to prove an "if" statement works correctly. The
 * actual behaviour of our JPA mappings and queries, on the other hand, can
 * only be honestly verified against a real database engine - that is what
 * TaskJpaRepositoryTest (an integration test using @DataJpaTest) is for.
 *
 * WHY CAN EXCESSIVE MOCKING BE A PROBLEM?
 * -------------------------------------------------------------------------
 * If we mock too much - for example, mocking so many collaborators that a
 * test just checks "did my code call these methods in this order?" - the
 * test ends up testing our mock setup instead of real behaviour, and it
 * becomes brittle: any harmless refactor of the internal implementation
 * can break the test even though the actual behaviour never changed. Here
 * we mock only the one real dependency this class has (the repository)
 * and let the actual UserService business logic run for real.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUserThrowsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser("New Person", "taken@example.com", "password123"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("taken@example.com");

        // We also verify the repository's create(...) method was NEVER
        // called - proving the business rule stopped the operation before
        // any data would have been written.
        verify(userRepository, never()).create(any());
    }

    @Test
    void registerUserSavesNewUserWithHashedPasswordWhenEmailIsFree() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash-value");
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> {
            User passedIn = invocation.getArgument(0);
            passedIn.setId(1L);
            return passedIn;
        });

        User result = userService.registerUser("New Person", "new@example.com", "password123");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        // The raw password must never be stored directly - only its hash,
        // produced here by the (mocked) PasswordEncoder.
        assertThat(result.getPasswordHash()).isEqualTo("bcrypt-hash-value");
        assertThat(result.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void getUserByIdThrowsResourceNotFoundWhenMissing() {
        when(userRepository.getById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getUserByIdReturnsUserWhenPresent() {
        User existing = new User(5L, "Carla", "carla@example.com", "hash", Role.USER, LocalDateTime.now());
        when(userRepository.getById(5L)).thenReturn(Optional.of(existing));

        User result = userService.getUserById(5L);

        assertThat(result).isEqualTo(existing);
    }
}
