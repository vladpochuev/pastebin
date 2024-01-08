package com.vladpochuev.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.time.Instant;
import java.util.List;

public class TokenAuthenticationUserDetailsService
        implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    private final JdbcTemplate jdbcTemplate;

    public TokenAuthenticationUserDetailsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken) throws UsernameNotFoundException {
        if (authenticationToken.getPrincipal() instanceof Token token) {
            List<SimpleGrantedAuthority> authorities = token.authorities().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            return new TokenUser(token.subject(), "nopassword", true, true,
                    Boolean.FALSE.equals(this.jdbcTemplate.queryForObject("""
                            SELECT EXISTS(SELECT id FROM deactivated_tokens WHERE id = ?)
                            """, Boolean.class, token.id())) &&
                            token.expiresAt().isAfter(Instant.now()), true, authorities, token);
        }

        throw new UsernameNotFoundException("Principal must be of type Token");
    }
}