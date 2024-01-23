package com.vladpochuev.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladpochuev.model.Bin;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

public class RestAuthenticatedFilter extends OncePerRequestFilter {
    private RequestMatcher requestMatcher = new AntPathRequestMatcher("/api/bin", HttpMethod.POST.name());
    private HttpServletRequest request;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Duration cookieTtl = Duration.ofDays(1);
    private final String cookieName = "Bin-to-create";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.requestMatcher.matches(request) && (authentication == null || !authentication.isAuthenticated())) {
            returnErrorMessage(request, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void returnErrorMessage(HttpServletRequest request, HttpServletResponse response)
            throws JsonProcessingException {
        if (Arrays.stream(request.getCookies()).anyMatch(cookie -> cookie.getName().equals(this.cookieName))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            Bin bin = composeBin(request);
            String encodedBin = URLEncoder.encode(objectMapper.writeValueAsString(bin), StandardCharsets.UTF_8);
            Cookie cookie = new Cookie(this.cookieName, encodedBin);
            setCookieProperties(cookie);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.addCookie(cookie);
        }
    }

    private Bin composeBin(HttpServletRequest request) {
        this.request = request;
        return new Bin(
                getPar("title"),
                getPar("message"),
                Integer.parseInt(getPar("x")),
                Integer.parseInt(getPar("y")),
                getPar("color"),
                getPar("amountOfTime")
        );
    }

    private String getPar(String name) {
        return request.getParameter(name);
    }

    private void setCookieProperties(Cookie cookie) {
        cookie.setPath("/");
        cookie.setDomain(null);
        cookie.setSecure(true);
        cookie.setHttpOnly(false);
        cookie.setMaxAge((int) this.cookieTtl.toSeconds());
    }

    public void setRequestMatcher(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
    }
}
