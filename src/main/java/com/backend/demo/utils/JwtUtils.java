package com.backend.demo.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.demo.errors.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class JwtUtils {
    public static int token_minutes_duration = 60;
    public static int refresh_token_minutes_duration = 60 * 2;
    public static int verification_token_minutes_duration = 60;

    public static Map<String, String> generateAccessAndRefreshTokens(HttpServletRequest request,
                                                                     UserDetails userDetails,
                                                                     Algorithm algorithm) {
        String accessToken =
                JWT.create()
                        .withSubject(userDetails.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + (long) token_minutes_duration * 60 * 1000))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("roles",
                                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                        .sign(algorithm);
        String refreshToken =
                JWT.create()
                        .withSubject(userDetails.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + (long) refresh_token_minutes_duration * 60 * 1000))
                        .withIssuer(request.getRequestURL().toString())
                        .sign(algorithm);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        return tokens;
    }

    public static Map<String, String> generateNewAccessToken(HttpServletRequest request,
                                                             UserDetails userDetails,
                                                             Algorithm algorithm,
                                                             String existingRefreshToken) {
        String accessToken =
                JWT.create()
                        .withSubject(userDetails.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + (long) token_minutes_duration * 60 * 1000))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("roles",
                                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                        .sign(algorithm);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", existingRefreshToken);
        return tokens;
    }

    public static String generateUserVerificationToken(HttpServletRequest request,
                                                       String username,
                                                       Algorithm algorithm) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + (long) verification_token_minutes_duration * 60 * 1000))
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);

    }

    public static String generatePasswordRecoveryToken(HttpServletRequest request,
                                                       String username,
                                                       Algorithm algorithm) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + (long) verification_token_minutes_duration * 60 * 1000))
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);
    }

    public static String getUsernameFromJWT(Algorithm algorithm, String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        return decodedJWT.getSubject();
    }

    public static void catchJWTError(HttpServletResponse response,
                                     JWTVerificationException exception) throws IOException {
        response.setHeader("error", exception.getMessage());
        response.setStatus(UNAUTHORIZED.value());
        response.setContentType(APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = new ErrorResponse(UNAUTHORIZED, exception.getMessage());
        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }

    public static void catchVerificationTokenError(HttpServletResponse response,
                                                   JWTVerificationException exception) throws IOException {
        response.setHeader("error", exception.getMessage());
        response.setStatus(BAD_REQUEST.value());
        response.setContentType(APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST, exception.getMessage());
        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }

    public static void catchOrganizationIdError(HttpServletResponse response,
                                                HttpClientErrorException exception) throws IOException {
        response.setHeader("error", exception.getMessage());
        response.setStatus(UNAUTHORIZED.value());
        response.setContentType(APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = new ErrorResponse(UNAUTHORIZED, exception.getMessage());
        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}
