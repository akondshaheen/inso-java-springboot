package com.inso.learning.taskflow.controller;

import tools.jackson.databind.ObjectMapper;
import com.inso.learning.taskflow.domain.Role;
import com.inso.learning.taskflow.domain.User;
import com.inso.learning.taskflow.dto.request.UserRegistrationRequest;
import com.inso.learning.taskflow.exception.ResourceNotFoundException;
import com.inso.learning.taskflow.repository.UserRepository;
import com.inso.learning.taskflow.security.JwtService;
import com.inso.learning.taskflow.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * =============================================================================
 * @WebMvcTest: AN INTEGRATION TEST FOR ONLY THE WEB (HTTP) LAYER
 * =============================================================================
 *
 * WHAT DOES @WebMvcTest DO?
 * -------------------------------------------------------------------------
 * @WebMvcTest boots only the Spring MVC infrastructure needed to serve
 * HTTP requests (DispatcherServlet, argument resolvers, our
 * @RestControllerAdvice, JSON serialization) for ONE controller
 * (UserController), instead of starting the whole application (no real
 * database, no service beans). Every other bean the controller needs
 * (here, UserService) must be supplied as a @MockBean - a Mockito mock
 * that Spring registers into the application context in place of the real
 * bean.
 *
 * WHY USE MockMvc INSTEAD OF STARTING A REAL EMBEDDED SERVER?
 * -------------------------------------------------------------------------
 * MockMvc simulates an HTTP request being dispatched through Spring MVC's
 * machinery WITHOUT opening a real network socket or real Tomcat server.
 * This makes the test much faster than a full end-to-end test, while still
 * genuinely exercising request parsing, validation, controller method
 * dispatch, and JSON serialization - the exact things we want this test
 * to prove work correctly.
 *
 * THIS TEST ALSO PROVES THE GLOBAL EXCEPTION HANDLER WORKS
 * -------------------------------------------------------------------------
 * The "returns404WhenUserServiceThrowsNotFound" test below does not test
 * GlobalExceptionHandler directly - it proves that when a controller
 * method lets a ResourceNotFoundException escape, the whole chain (our
 * @RestControllerAdvice catching it and building an ApiErrorResponse)
 * produces exactly the HTTP response a real client would receive.
 *
 * WHY DISABLE SECURITY FILTERS HERE WITH addFilters = false?
 * -------------------------------------------------------------------------
 * Now that Spring Security is on the classpath, every request would
 * normally need a valid JWT to pass through JwtAuthenticationFilter and
 * the authorization rules in SecurityConfig. This test's job is to verify
 * UserController's OWN behaviour (request parsing, validation, response
 * shape) in isolation - re-testing authentication here as well would mix
 * two different concerns into one test and make failures harder to
 * diagnose. We still supply UserRepository and JwtService as mocks
 * because JwtAuthenticationFilter (a Spring bean) is still constructed
 * even when its filtering logic is skipped.
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    // RequestMetricsFilter (a Filter bean, added in the concurrency stage)
    // is auto-detected by @WebMvcTest for the same reason
    // JwtAuthenticationFilter is - Filter-type beans are explicitly
    // included by this test slice. Its constructor needs a RequestMetrics
    // bean, so we supply a mock here even though this test never checks
    // metrics behaviour.
    @MockitoBean
    private com.inso.learning.taskflow.concurrency.RequestMetrics requestMetrics;

    @Test
    void registerReturns201WithLocationHeaderAndBody() throws Exception {
        User created = new User(1L, "Dana", "dana@example.com", "hashedValue", Role.USER, LocalDateTime.now());
        when(userService.registerUser(any(), any(), any())).thenReturn(created);

        UserRegistrationRequest request = new UserRegistrationRequest("Dana", "dana@example.com", "password123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/1"))
                .andExpect(jsonPath("$.name").value("Dana"))
                .andExpect(jsonPath("$.email").value("dana@example.com"));
    }

    /**
     * BEAN VALIDATION FAILING BEFORE THE CONTROLLER METHOD EVEN RUNS
     * -------------------------------------------------------------------
     * Sending a blank "name" should never reach our mocked UserService at
     * all - @Valid should reject the request first, and our
     * GlobalExceptionHandler should turn that into a 400 response listing
     * which field failed.
     */
    @Test
    void registerReturns400WhenNameIsBlank() throws Exception {
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest("", "dana@example.com", "password123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns404WhenUserServiceThrowsNotFound() throws Exception {
        when(userService.getUserById(anyLong())).thenThrow(new ResourceNotFoundException("User not found with id 42"));

        mockMvc.perform(get("/api/users/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id 42"));
    }
}
