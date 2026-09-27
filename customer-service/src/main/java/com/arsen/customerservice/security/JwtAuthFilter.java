package com.arsen.customerservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_CLAIM = "userId";
    private static final String ROLES_CLAIM = "roles";
    private static final String AUTHORITY_KEY = "authority";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            try {
                Claims claims = jwtService.getClaims(header.substring(BEARER_PREFIX.length()));
                String userId = claims.get(USER_ID_CLAIM, String.class);
                if (StringUtils.hasText(userId)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            new AuthenticatedUser(userId, claims.getSubject()), null, extractAuthorities(claims));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("Invalid JWT: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private List<GrantedAuthority> extractAuthorities(Claims claims) {
        if (!(claims.get(ROLES_CLAIM) instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .map(role -> role instanceof Map<?, ?> map ? map.get(AUTHORITY_KEY) : role)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();
    }
}
