package com.backend.demo.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.demo.config.ConfigProperties;
import com.backend.demo.model.InvalidToken;
import com.backend.demo.repository.InvalidTokenRepository;
import com.backend.demo.repository.UserRepository;
import com.backend.demo.service.CustomUserDetailsService;
import com.backend.demo.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class CustomAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    UserRepository userRepository;

    @Autowired
    InvalidTokenRepository invalidTokenRepository;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm =
                        Algorithm.HMAC256(configProperties.getAuthenticationSecret().getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(token);
                boolean tokenIsInvalid = isTokenInvalidated(token);
                if (tokenIsInvalid) {
                    throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
                }
                String username = decodedJWT.getSubject();
                String[] permissions = decodedJWT.getClaim("permissions").asArray(String.class);
                Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                stream(permissions).forEach(permission -> {
                    authorities.add(new SimpleGrantedAuthority(permission));
                });
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request, response);
            } catch (JWTVerificationException exception) {
                JwtUtils.catchJWTError(response, exception);
            } catch (HttpClientErrorException exception) {
                JwtUtils.catchOrganizationIdError(response, exception);
            } catch (Exception exception) {
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isTokenInvalidated(String token) {
        InvalidToken invalidToken = invalidTokenRepository.findByToken(token);
        return invalidToken != null;
    }
}
