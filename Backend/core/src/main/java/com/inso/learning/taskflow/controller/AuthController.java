package com.inso.learning.taskflow.controller;

import com.inso.learning.taskflow.domain.User;
import com.inso.learning.taskflow.dto.request.LoginRequest;
import com.inso.learning.taskflow.dto.response.LoginResponse;
import com.inso.learning.taskflow.repository.UserRepository;
import com.inso.learning.taskflow.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * =============================================================================
 * AUTHENTICATION ENDPOINT: TURNING A PASSWORD INTO A JWT
 * =============================================================================
 *
 * WHAT HAPPENS, STEP BY STEP, WHEN A CLIENT CALLS POST /api/auth/login?
 * -------------------------------------------------------------------------
 *   1. SecurityConfig explicitly "permitAll"s this endpoint, since a caller
 *      obviously cannot present a JWT before they have ever logged in.
 *   2. We look the user up by email. If no such user exists, we throw the
 *      SAME BadCredentialsException as a wrong password would cause -
 *      revealing "no account exists with that email" instead would let an
 *      attacker discover which email addresses are registered at all,
 *      simply by trying logins and watching which error message comes
 *      back. Using one generic message for both cases is a small but real
 *      security habit worth knowing for an interview.
 *   3. We compare the submitted password against the stored hash using
 *      passwordEncoder.matches(...) - never by comparing raw strings,
 *      since we only ever stored a bcrypt hash, never the real password.
 *   4. If the password matches, JwtService issues a signed token
 *      containing the user's id, which the client must now attach to
 *      every future request's Authorization header.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.getByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.getId(), user.getName(), user.getRole().name());
    }
}
