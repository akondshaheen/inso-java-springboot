package com.inso.learning.taskflow.security;

import com.inso.learning.taskflow.domain.User;
import com.inso.learning.taskflow.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * THE JWT AUTHENTICATION FILTER: WHERE A TOKEN BECOMES "WHO IS LOGGED IN"
 * =============================================================================
 *
 * WHAT IS A SERVLET FILTER, AND WHERE DOES IT SIT IN THE REQUEST FLOW?
 * -------------------------------------------------------------------------
 * A filter runs BEFORE a request reaches Spring MVC's DispatcherServlet
 * (and therefore before it reaches any of our @RestController methods).
 * Spring Security is itself built almost entirely out of filters, chained
 * together into a "filter chain" - this class is one extra filter we add
 * into that chain (see SecurityConfig, which places it before Spring
 * Security's built-in username/password filter).
 *
 * OncePerRequestFilter guarantees this filter's logic runs exactly once
 * per request, even in environments where a request could otherwise be
 * forwarded internally and re-enter the filter chain.
 *
 * WHAT DOES THIS FILTER ACTUALLY DO, STEP BY STEP?
 * -------------------------------------------------------------------------
 *   1. Look for an "Authorization: Bearer <token>" header on the request.
 *   2. If there is no such header, do nothing and let the request continue
 *      - it will reach Spring Security's normal authorisation check
 *      unauthenticated, and be rejected later if the endpoint requires
 *      authentication.
 *   3. If a token is present, ask JwtService to verify its signature and
 *      expiration and extract the user id.
 *   4. Look that user up in the database (we still need the user's
 *      current role and details, and this also naturally rejects tokens
 *      for since-deleted users).
 *   5. Build a Spring Security Authentication object, wrapping our own
 *      domain.User as "principal" so SecurityUtils can retrieve it later,
 *      and store it in the SecurityContext - from this point on, for the
 *      rest of this one request, Spring Security considers this request
 *      authenticated.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring("Bearer ".length());
            try {
                Long userId = jwtService.extractUserId(token);
                Optional<User> user = userRepository.getById(userId);
                user.ifPresent(this::authenticateInSecurityContext);
            } catch (JwtException | IllegalArgumentException invalidToken) {
                // An invalid, expired, or tampered token simply means "this
                // request is not authenticated" - we deliberately do not
                // throw here, because a broken/expired token should not
                // crash the request; it should just fall through to Spring
                // Security's normal "you are not authenticated" handling
                // for endpoints that require a login.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateInSecurityContext(User user) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
