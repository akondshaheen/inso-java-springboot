package com.inso.learning.taskflow.controller;

import com.inso.learning.taskflow.domain.User;
import com.inso.learning.taskflow.dto.request.UserRegistrationRequest;
import com.inso.learning.taskflow.dto.request.UserUpdateRequest;
import com.inso.learning.taskflow.dto.response.UserResponse;
import com.inso.learning.taskflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * =============================================================================
 * STEP 12 OF OUR BUILD SEQUENCE: THE @RestController LAYER
 * =============================================================================
 *
 * WHAT IS THE DIFFERENCE BETWEEN @Controller AND @RestController?
 * -------------------------------------------------------------------------
 * @Controller is the original Spring MVC annotation: by default, whatever
 * a @Controller method returns is treated as the NAME of a server-side
 * view (an HTML template) to render, useful for building traditional
 * websites that return full HTML pages. @RestController is a shortcut for
 * @Controller plus @ResponseBody on every method - it tells Spring "do not
 * treat return values as view names; instead, serialize them directly
 * into the HTTP response body" (as JSON, by default, using Jackson). This
 * is exactly what we want for a REST API, where every endpoint returns
 * data, not a rendered page.
 *
 * WHAT HAPPENS, STEP BY STEP, WHEN A REQUEST HITS AN ENDPOINT LIKE
 * "POST /api/users"?
 * -------------------------------------------------------------------------
 *   1. The embedded Tomcat server receives the raw HTTP request.
 *   2. Spring MVC's DispatcherServlet looks at the request's method (POST)
 *      and path ("/api/users") and matches it to the @PostMapping method
 *      below, based on the @RequestMapping("/api/users") on this class.
 *   3. Spring MVC reads the request body (JSON text) and uses Jackson to
 *      DESERIALIZE it into a UserRegistrationRequest object - this is
 *      exactly the reverse of what happens when we return a UserResponse.
 *   4. Because the parameter is annotated @Valid, Spring runs Bean
 *      Validation against every constraint on UserRegistrationRequest
 *      BEFORE our method body executes. If validation fails, a
 *      MethodArgumentNotValidException is thrown and our method never
 *      runs at all - the global exception handler (later stage) turns
 *      that into a 400 Bad Request.
 *   5. Our controller method calls the SERVICE layer to perform the real
 *      business logic (registerUser). The controller itself never talks
 *      to a repository or entity directly - that would blur the layers'
 *      responsibilities and make the business logic harder to test and
 *      reuse.
 *   6. We wrap the result in a ResponseEntity, choosing the HTTP status
 *      code explicitly, and Spring serializes the body back to JSON.
 *
 * WHY NOT PUT THE BUSINESS LOGIC (LIKE THE DUPLICATE-EMAIL CHECK) DIRECTLY
 * IN THIS CONTROLLER METHOD?
 * -------------------------------------------------------------------------
 * See UserService's Javadoc for the full explanation - in short, business
 * rules belong in the service layer so they can be reused by any other
 * caller and unit-tested without needing HTTP at all.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 201 CREATED VS 200 OK
     * -------------------------------------------------------------------
     * 200 OK is a generic "the request succeeded" response. 201 Created is
     * more specific: it tells the client "a new resource was created as a
     * result of this request", and, by REST convention, the response
     * should include a "Location" header pointing at the new resource's
     * URL - which is exactly what ResponseEntity.created(uri) does for us.
     */
    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        User created = userService.registerUser(request.name(), request.email(), request.password());
        URI location = URI.create("/api/users/" + created.getId());
        return ResponseEntity.created(location).body(UserResponse.from(created));
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAllUsers().stream().map(UserResponse::from).toList();
    }

    /**
     * "{id}" IN THE PATH IS A PATH VARIABLE: part of the URL itself
     * identifies exactly which resource is being requested (unlike a query
     * parameter, which is optional extra information appended after "?").
     * @PathVariable tells Spring MVC to extract that segment of the URL
     * and pass it in as this method's "id" parameter, converting the text
     * to a Long automatically.
     */
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return UserResponse.from(userService.getUserById(id));
    }

    /**
     * PUT vs PATCH: this is a PUT endpoint because UserUpdateRequest
     * requires every editable field (name and email) to be supplied - the
     * client is REPLACING the editable part of the resource, not making a
     * small partial change.
     */
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return UserResponse.from(userService.updateUser(id, request.name(), request.email()));
    }

    /**
     * 204 NO CONTENT is the conventional response for a successful DELETE:
     * the operation succeeded, and there is deliberately no response body
     * to return, because there is nothing left to describe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
