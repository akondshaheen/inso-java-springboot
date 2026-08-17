package com.inso.learning.taskflow.repository;

import com.inso.learning.taskflow.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * STEP 7 OF OUR BUILD SEQUENCE: OUR OWN REPOSITORY INTERFACE
 * =============================================================================
 *
 * WHY DEFINE OUR OWN REPOSITORY INTERFACE, WHEN SPRING DATA JPA ALREADY
 * GIVES US ONE (JpaRepository)?
 * -------------------------------------------------------------------------
 * This interface is a "port": it describes WHAT the rest of the
 * application (the service layer) needs from persistence, using our
 * DOMAIN objects (User), without mentioning JPA, Hibernate, or entities
 * anywhere. The service layer will depend only on this interface.
 *
 * The actual JPA-based implementation of this interface
 * (repository.impl.UserRepositoryImpl, in the next step) is an "adapter":
 * it fulfils this contract using Spring Data JPA's UserJpaRepository
 * underneath, converting between UserEntity and User with the UserMapper.
 *
 * This extra layer of indirection means our service layer's tests can
 * depend on THIS interface and a simple test double, without needing a
 * real database or even Hibernate on the classpath at all. It also means
 * that, if we ever swapped our persistence technology, only the "impl"
 * classes and mappers would need to change - the service layer would be
 * completely unaffected because it never saw an entity in the first
 * place.
 */
public interface UserRepository {

    User create(User user);

    List<User> getAll();

    Optional<User> getById(Long id);

    Optional<User> getByEmail(String email);

    boolean existsByEmail(String email);

    User update(User user);

    void deleteById(Long id);
}
