package com.inso.learning.taskflow.dto.response;

/**
 * The response body for a successful login: the JWT the client must now
 * attach to the "Authorization: Bearer <token>" header of every future
 * request, plus a little basic profile information so the client does not
 * need a second request just to know who just logged in.
 */
public record LoginResponse(String token, Long userId, String name, String role) {
}
