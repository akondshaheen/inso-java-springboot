package com.inso.learning.taskflow.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * =============================================================================
 * SECURITY CONFIGURATION: WIRING AUTHENTICATION AND AUTHORIZATION TOGETHER
 * =============================================================================
 *
 * AUTHENTICATION VS AUTHORIZATION - THE MOST IMPORTANT DISTINCTION IN THIS
 * WHOLE STAGE
 * -------------------------------------------------------------------------
 * AUTHENTICATION answers "who are you?" - proving an identity, normally by
 * checking a password (see AuthController) or, on every later request, by
 * verifying a JWT (see JwtAuthenticationFilter). AUTHORIZATION answers
 * "given that I know who you are, are you ALLOWED to do this?" - for
 * example, only an ADMIN may list every user (see the ".hasRole(ADMIN)"
 * rule below). A request can be authenticated (we know exactly who you
 * are) yet still be unauthorized for a specific action - that mismatch is
 * exactly the difference between a 401 and a 403 response.
 *
 * WHY DISABLE CSRF FOR THIS API?
 * -------------------------------------------------------------------------
 * CSRF (Cross-Site Request Forgery) is an attack that relies on the
 * browser AUTOMATICALLY attaching a session cookie to every request to a
 * site, including ones triggered by a malicious page the user did not
 * intend to visit. Spring Security's CSRF protection exists to guard
 * COOKIE-based session authentication. Our API is stateless and uses a
 * JWT that the CLIENT must deliberately attach to the Authorization
 * header on every request (browsers do not do this automatically the way
 * they do with cookies), so the CSRF attack this protection defends
 * against does not apply here. Disabling it is a deliberate, explained
 * decision - not something to copy blindly into an application that DOES
 * use cookie-based sessions.
 *
 * WHY IS THE SESSION POLICY "STATELESS"?
 * -------------------------------------------------------------------------
 * By default, Spring Security creates an HTTP session (backed by a cookie)
 * to remember a login between requests. Because we authenticate every
 * single request independently using its JWT, we tell Spring Security
 * never to create or use a session at all - this keeps our API properly
 * stateless, meaning any server instance can handle any request without
 * needing to share session state with any other instance (this matters a
 * lot once you run more than one instance of a service behind a load
 * balancer).
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                           RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                           RestAccessDeniedHandler restAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

    /**
     * BCryptPasswordEncoder deliberately takes noticeable time to compute
     * a hash and automatically mixes in a random "salt" for every
     * password. Two users with the identical password end up with two
     * completely different stored hashes, which stops an attacker from
     * using a precomputed table of common password hashes ("rainbow
     * tables") against our database. This is why we replaced the earlier
     * plain SHA-256 PasswordHasher utility with this bean.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Registration and login must be reachable by
                        // someone who is NOT authenticated yet - that is
                        // the whole point of these two endpoints.
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        // Spring Boot Actuator's health endpoint is
                        // intentionally public so that infrastructure
                        // (load balancers, container orchestrators) can
                        // check whether the application is alive without
                        // needing credentials.
                        .requestMatchers("/actuator/health").permitAll()
                        // Other actuator endpoints (like /actuator/metrics)
                        // can reveal internal operational details, so we
                        // restrict them to ADMIN just like our own
                        // hand-written /api/metrics endpoint.
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // AUTHORIZATION EXAMPLE: only an ADMIN may list
                        // every user in the system - an ordinary user has
                        // no legitimate reason to see every other user's
                        // account, so we restrict this by ROLE rather than
                        // relying only on the service layer.
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        // Both of these touch operational, cross-user data
                        // (request counts, bulk notification sending) -
                        // exactly the kind of administrative action that
                        // should not be available to an ordinary user, so
                        // we restrict them the same way as listing every
                        // user above.
                        .requestMatchers(HttpMethod.GET, "/api/metrics").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/notifications/**").hasRole("ADMIN")
                        // Everything else just needs a valid, authenticated
                        // caller - finer-grained checks (like "only the
                        // owner or an admin may edit this project") stay in
                        // the service layer, because they depend on WHICH
                        // specific resource is being touched, not just the
                        // caller's role.
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * WHAT IS CORS, AND WHY DO WE NEED TO CONFIGURE IT AT ALL?
     * -------------------------------------------------------------------------
     * A browser blocks JavaScript running on one origin (say,
     * http://localhost:3000, a React app) from calling an API on a
     * different origin (http://localhost:8080, our Spring Boot app) unless
     * the API explicitly says it is allowed to. This is the "Same-Origin
     * Policy" - a core browser security feature, since without it, any
     * website you visit could silently make authenticated requests to your
     * bank's API using cookies already stored in your browser. CORS
     * (Cross-Origin Resource Sharing) is the mechanism by which our server
     * explicitly grants permission for specific origins, methods, and
     * headers. This configuration is only consulted by BROWSERS - it has
     * no effect on server-to-server calls or tools like Postman/curl.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
