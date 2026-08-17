package com.inso.learning.taskflow.security;

import com.inso.learning.taskflow.domain.User;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A SMALL HELPER FOR READING "WHO IS MAKING THIS REQUEST?"
 * -------------------------------------------------------------------------
 * Once JwtAuthenticationFilter has verified a request's token, it stores
 * the authenticated User as the "principal" inside Spring Security's
 * SecurityContext (see JwtAuthenticationFilter for how that is set).
 * SecurityContextHolder keeps this information on a ThreadLocal - a value
 * that is only visible to the current thread - which works well here
 * because Spring MVC handles each HTTP request on its own thread.
 *
 * Controllers and services call this helper instead of reading
 * SecurityContextHolder directly, so the rest of the codebase does not
 * need to know any Spring Security details at all - if we ever changed how
 * authentication works, only this one class would need to change.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
