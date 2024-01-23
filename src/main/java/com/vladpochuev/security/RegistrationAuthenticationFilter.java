package com.vladpochuev.security;

import com.vladpochuev.dao.UserDAO;
import com.vladpochuev.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DuplicateKeyException;
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
    private static final int MIN_USERNAME_LENGTH = 6;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_USERNAME_LENGTH = 30;
    private static final int MAX_PASSWORD_LENGTH = 30;
    private static final String USERNAME_REGEX = String.format("^(?=[a-zA-Z0-9._]{%d,%d}$)(?!.*[_.]{2})[^_.].*[^_.]$",
            MIN_USERNAME_LENGTH, MAX_USERNAME_LENGTH);
    private static final String PASSWORD_REGEX = String.format("^(?=.*?[a-z]).{%d,%d}$",
            MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH);

    public RegistrationAuthenticationFilter(UserDAO userDAO, PasswordEncoder passwordEncoder,
                                            TokenCookieJweStringSerializer tokenCookieJweStringSerializer) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.tokenCookieJweStringSerializer = tokenCookieJweStringSerializer;
    }

    public RegistrationAuthenticationFilter(AuthenticationManager authenticationManager, UserDAO userDAO,
                                            PasswordEncoder passwordEncoder,
                                            TokenCookieJweStringSerializer tokenCookieJweStringSerializer) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.tokenCookieJweStringSerializer = tokenCookieJweStringSerializer;
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String username = this.obtainUsername(request);
        String password = this.obtainPassword(request);

        validateData(username, password);
        createUser(username, password);

        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        this.setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private void validateData(String username, String password) {
        if (!username.matches(USERNAME_REGEX)) {
            throw new UsernameWrongFormatException("Wrong username format");
        }
        if (!password.matches(PASSWORD_REGEX)) {
            throw new PasswordWrongFormatException("Wrong password format");
        }
    }

    private void createUser(String username, String password) {
        try {
            User user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setUsername(username);
            user.setPassword(this.passwordEncoder.encode(password));

            this.userDAO.create(user);
        } catch (DuplicateKeyException e) {
            throw new UserAlreadyExistsException("User already exists");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        TokenCookieSessionAuthenticationStrategy tokenCookieSessionAuthenticationStrategy = new TokenCookieSessionAuthenticationStrategy();
        tokenCookieSessionAuthenticationStrategy.setTokenStringSerializer(this.tokenCookieJweStringSerializer);
        tokenCookieSessionAuthenticationStrategy.onAuthentication(authResult, request, response);
        response.sendRedirect("/");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        String errorMessage = null;
        if (failed instanceof UserAlreadyExistsException) {
            errorMessage = "duplicate";
        } else if (failed instanceof UsernameWrongFormatException) {
            errorMessage = "usernameWrongFormat";
        } else if (failed instanceof PasswordWrongFormatException) {
            errorMessage = "passwordWrongFormat";
        }

        response.sendRedirect("/signup?error" + (errorMessage == null ? "" : "=" + errorMessage));
    }

    @NotNull
    protected String obtainPassword(HttpServletRequest request) {
        String password = request.getParameter(this.passwordParameter);
        return password != null ? password.trim() : "";
    }

    @NotNull
    protected String obtainUsername(HttpServletRequest request) {
        String username = request.getParameter(this.usernameParameter);
        return username != null ? username.trim() : "";
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
