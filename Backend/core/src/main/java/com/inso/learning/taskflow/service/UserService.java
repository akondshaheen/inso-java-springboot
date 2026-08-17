package com.inso.learning.taskflow.service;

import com.inso.learning.taskflow.domain.Role;
import com.inso.learning.taskflow.domain.User;
import com.inso.learning.taskflow.exception.DuplicateResourceException;
import com.inso.learning.taskflow.exception.ResourceNotFoundException;
import com.inso.learning.taskflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * =============================================================================
 * STEP 10 OF OUR BUILD SEQUENCE: THE @Service LAYER
 * =============================================================================
 *
 * WHY DOES THIS LAYER EXIST, SEPARATE FROM THE CONTROLLER AND REPOSITORY?
 * -------------------------------------------------------------------------
 * The Controller-Service-Repository split gives each layer exactly one
 * responsibility:
 *   - The CONTROLLER's job is to understand HTTP: reading the request,
 *     validating its shape, and choosing the right status code for the
 *     response. It should not decide business rules.
 *   - The SERVICE's job is to hold BUSINESS LOGIC: rules like "an email
 *     address must be unique" or "only a project's owner may edit it".
 *     This is where decisions are made.
 *   - The REPOSITORY's job is only to fetch and store data.
 *
 * If business logic like the duplicate-email check below lived directly in
 * the controller instead, two problems would appear quickly: (1) the same
 * rule would need to be copied into every controller method that touches
 * users, and duplicated again for any other entry point (a CLI tool, a
 * scheduled job) that also creates users; (2) testing the rule would
 * require spinning up a full HTTP layer (MockMvc) just to check a simple
 * "if" statement, instead of calling a plain Java method directly.
 *
 * WHAT DOES @Service ACTUALLY DO, AND WHAT HAPPENS WHEN SPRING SEES IT?
 * -------------------------------------------------------------------------
 * @Service is a specialised @Component - it exists mainly to communicate
 * INTENT to other developers reading the code (this class holds business
 * logic), though Spring treats it exactly like @Component for bean
 * creation purposes. At startup, Spring's component scan finds this class,
 * creates one instance of it (a bean), sees that its constructor needs a
 * UserRepository, finds the matching UserRepositoryImpl bean already
 * created, and injects it here automatically. This class then becomes
 * available to be injected into whatever needs it next (our
 * UserController).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a brand new user. This method demonstrates a business rule
     * (email uniqueness) that must be enforced BEFORE we touch the
     * database, plus password hashing via Spring Security's
     * BCryptPasswordEncoder (see SecurityConfig for why bcrypt was chosen
     * over a simpler hash like SHA-256).
     */
    public User registerUser(String name, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("A user with email '" + email + "' already exists");
        }
        String passwordHash = passwordEncoder.encode(rawPassword);
        User newUser = new User(name, email, passwordHash, Role.USER);
        return userRepository.create(newUser);
    }

    public List<User> getAllUsers() {
        return userRepository.getAll();
    }

    /**
     * WHY DOES THE REPOSITORY RETURN Optional<User>, BUT THIS METHOD
     * RETURNS A PLAIN User?
     * -------------------------------------------------------------------
     * Optional exists to force the CALLER of a method that might not find
     * anything to explicitly decide what "nothing found" means for them.
     * Here, in the service layer, we decide: for this specific use case
     * ("give me the user with this id, I need it now"), a missing user is
     * an error condition, so we convert the empty Optional into a thrown
     * ResourceNotFoundException with orElseThrow(...). A different calling
     * context might instead choose to keep the Optional and do something
     * else entirely (see how ProjectService checks "does this project
     * exist" without necessarily throwing).
     */
    public User getUserById(Long id) {
        return userRepository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    public User updateUser(Long id, String name, String email) {
        User existing = getUserById(id);
        existing.setName(name);
        existing.setEmail(email);
        return userRepository.update(existing);
    }

    public void deleteUser(Long id) {
        // We call getUserById(...) first purely so a delete of a
        // non-existent user reports a clear 404 error instead of silently
        // doing nothing (which is what a bare "deleteById" would do).
        getUserById(id);
        userRepository.deleteById(id);
    }
}
