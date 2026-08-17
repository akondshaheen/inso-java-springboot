package com.inso.learning.taskflow.security;

import com.inso.learning.taskflow.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * =============================================================================
 * THE JWT CONCEPT, EXPLAINED, AND OUR SMALL IMPLEMENTATION OF IT
 * =============================================================================
 *
 * WHAT PROBLEM DOES A JWT SOLVE?
 * -------------------------------------------------------------------------
 * HTTP is STATELESS: the server does not automatically remember who you
 * are between requests. A traditional website solves this with a SESSION -
 * the server stores "user 5 is logged in" in memory and gives the browser
 * a session id cookie to prove it on every later request. That approach
 * needs the server to keep session state in memory (or a shared cache) for
 * every logged-in user, which becomes awkward once you have multiple
 * server instances behind a load balancer.
 *
 * A JWT (JSON Web Token) takes the opposite approach: instead of the
 * SERVER remembering who is logged in, the CLIENT holds a small signed
 * token that already contains who they are (their user id, for example).
 * The server does not need to store anything about active logins at all -
 * it only needs to verify the token's signature on every request. This is
 * why JWTs fit naturally with REST's "stateless" principle.
 *
 * WHAT IS ACTUALLY INSIDE A JWT?
 * -------------------------------------------------------------------------
 * A JWT is three Base64-encoded parts joined by dots: "header.payload.signature".
 *   - The HEADER says which algorithm was used to sign the token.
 *   - The PAYLOAD ("claims") is the actual data - here, the user's id, a
 *     "subject", an issued-at time, and an expiration time.
 *   - The SIGNATURE is created by hashing the header and payload together
 *     with a SECRET KEY that only the server knows. Anyone can read a
 *     JWT's payload (it is only Base64-encoded, not encrypted!), but only
 *     someone holding the secret key can produce a signature that will
 *     pass verification - so a client cannot forge or silently modify a
 *     token's contents without the server noticing during verification.
 *
 * COMMON MISTAKE: because a JWT's payload is only encoded, not encrypted,
 * you must NEVER put secret information (a password, for example) inside
 * it - anyone who intercepts the token can decode and read the payload
 * with nothing more than a text editor.
 */
@Component
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(@Value("${taskflow.jwt.secret}") String secret,
                       @Value("${taskflow.jwt.expiration-minutes:60}") long expirationMinutes) {
        // Keys.hmacShaKeyFor turns our plain-text secret into the kind of
        // key object the signing algorithm needs. The secret must be long
        // enough (at least 256 bits for HS256) - see application.yml.
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMinutes * 60 * 1000;
    }

    /**
     * Builds a signed token for a user who has just logged in successfully.
     * We store the user's id as the "subject" claim (the standard JWT claim
     * for "who is this token about"), plus their role, so later requests
     * can check permissions without a database lookup for the role alone.
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Verifies the token's signature and expiration, then extracts the
     * user id. If the token was tampered with, expired, or malformed,
     * jjwt throws an unchecked JwtException, which our JwtAuthenticationFilter
     * catches and treats as "not authenticated" rather than crashing the
     * request.
     */
    public Long extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }
}
