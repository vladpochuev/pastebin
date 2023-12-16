package com.vladpochuev.security;

import com.vladpochuev.dao.UserDAO;
import com.vladpochuev.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.UUID;

public class RegistrationAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/signup", "POST");
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final TokenCookieJweStringSerializer tokenCookieJweStringSerializer;
    private String usernameParameter = "username";
    private String passwordParameter = "password";

    public RegistrationAuthenticationFilter(UserDAO userDAO, PasswordEncoder passwordEncoder, TokenCookieJweStringSerializer tokenCookieJweStringSerializer) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.tokenCookieJweStringSerializer = tokenCookieJweStringSerializer;
    }

    public RegistrationAuthenticationFilter(AuthenticationManager authenticationManager, UserDAO userDAO,
                                            PasswordEncoder passwordEncoder, TokenCookieJweStringSerializer tokenCookieJweStringSerializer) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.tokenCookieJweStringSerializer = tokenCookieJweStringSerializer;
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        String username = this.obtainUsername(request);
        username = username != null ? username.trim() : "";
        String password = this.obtainPassword(request);
        password = password != null ? password : "";

        if (!userDAO.existsByUsername(username)) {
            User user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));

            userDAO.create(user);
        }

        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        this.setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        TokenCookieSessionAuthenticationStrategy tokenCookieSessionAuthenticationStrategy = new TokenCookieSessionAuthenticationStrategy();
        tokenCookieSessionAuthenticationStrategy.setTokenStringSerializer(tokenCookieJweStringSerializer);
        tokenCookieSessionAuthenticationStrategy.onAuthentication(authResult, request, response);

        String bin = request.getParameter("binToCreate");
        String url = bin.equals("") ? "/map" : "/map?binToCreate=" + bin;
        response.sendRedirect(url);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.sendRedirect("/signup?error");
    }

    @Nullable
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(this.passwordParameter);
    }

    @Nullable
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(this.usernameParameter);
    }

    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    public void setUsernameParameter(String usernameParameter) {
        Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
        this.usernameParameter = usernameParameter;
    }

    public void setPasswordParameter(String passwordParameter) {
        Assert.hasText(passwordParameter, "Password parameter must not be empty or null");
        this.passwordParameter = passwordParameter;
    }

    public final String getUsernameParameter() {
        return this.usernameParameter;
    }

    public final String getPasswordParameter() {
        return this.passwordParameter;
    }
}
