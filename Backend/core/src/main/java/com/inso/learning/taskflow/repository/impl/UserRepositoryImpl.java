package com.inso.learning.taskflow.repository.impl;

import com.inso.learning.taskflow.domain.User;
import com.inso.learning.taskflow.entity.UserEntity;
import com.inso.learning.taskflow.exception.ResourceNotFoundException;
import com.inso.learning.taskflow.mapper.UserMapper;
import com.inso.learning.taskflow.repository.UserRepository;
import com.inso.learning.taskflow.repository.jpa.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * STEP 9 OF OUR BUILD SEQUENCE: THE @Repository IMPLEMENTATION (THE ADAPTER)
 * =============================================================================
 *
 * WHAT DOES @Repository ACTUALLY DO?
 * -------------------------------------------------------------------------
 * @Repository is a specialised form of @Component - it tells Spring's
 * component scan "create one instance of this class and manage it as a
 * bean, available for Dependency Injection". Spring also uses @Repository
 * as a marker for a small extra feature: it automatically translates
 * low-level database exceptions (like SQL-specific errors) into Spring's
 * own consistent DataAccessException hierarchy, so code that calls a
 * repository does not need to know which particular database driver is
 * being used underneath.
 *
 * WHY DOES THIS CLASS EXIST, WHEN UserJpaRepository ALREADY DOES ALL THE
 * DATABASE WORK?
 * -------------------------------------------------------------------------
 * This class is the "adapter" that connects two worlds: our own
 * UserRepository interface (the "port" the service layer depends on,
 * speaking only in terms of domain.User) and Spring Data JPA's
 * UserJpaRepository (speaking only in terms of entity.UserEntity). Every
 * method here follows the same shape: convert incoming domain objects to
 * entities (if needed) with UserMapper, delegate the real database work to
 * UserJpaRepository, then convert the entity result back to a domain
 * object before returning it. The service layer never sees a UserEntity.
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    /**
     * CONSTRUCTOR INJECTION (the preferred way to wire dependencies in
     * Spring): this class simply declares what it needs as constructor
     * parameters. Spring sees this constructor when building the
     * UserRepositoryImpl bean, finds a matching UserJpaRepository bean (the
     * dynamic proxy Spring Data generated) and a matching UserMapper bean,
     * and passes them in automatically. We never call "new
     * UserJpaRepository()" or "new UserMapper()" ourselves.
     *
     * Constructor injection is preferred over field injection
     * (@Autowired on a field) because: (1) fields can be made "final",
     * making it impossible to accidentally leave a dependency unset; (2) a
     * class's dependencies are visible directly in its constructor
     * signature, making tests easy to write by simply calling "new
     * UserRepositoryImpl(fakeJpaRepo, fakeMapper)" without needing a
     * Spring context at all; (3) if a class ends up needing too many
     * dependencies, a long constructor signature is an obvious warning
     * sign that the class may be doing too much.
     */
    public UserRepositoryImpl(UserJpaRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User create(User user) {
        UserEntity saved = userJpaRepository.save(userMapper.toEntity(user));
        return userMapper.toDomain(saved);
    }

    @Override
    public List<User> getAll() {
        // The Stream API's map() converts every UserEntity coming back
        // from the database into a domain User; toList() (Java 16+)
        // collects the results into a new, unmodifiable List - a compact,
        // readable way to transform one collection into another.
        return userJpaRepository.findAll().stream()
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<User> getById(Long id) {
        return userJpaRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public User update(User user) {
        UserEntity existing = userJpaRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + user.getId()));
        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        existing.setPasswordHash(user.getPasswordHash());
        existing.setRole(user.getRole());
        return userMapper.toDomain(userJpaRepository.save(existing));
    }

    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }
}
