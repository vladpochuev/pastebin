package com.vladpochuev.config;

import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.vladpochuev.dao.UserDAO;
import com.vladpochuev.model.DbProperties;
import com.vladpochuev.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import javax.sql.DataSource;
import java.text.ParseException;

@SpringBootApplication
@ComponentScan("com.vladpochuev")
@PropertySource("classpath:application.yml")
@EnableScheduling
public class PastebinApplication {
    private final DbProperties dbProperties;

    public PastebinApplication(DbProperties dbProperties) {
        this.dbProperties = dbProperties;
    }

    public static void main(String[] args) {
        SpringApplication.run(PastebinApplication.class, args);
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(this.dbProperties.getUrl());
        dataSource.setUsername(this.dbProperties.getUsername());
        dataSource.setPassword(this.dbProperties.getPassword());

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public TokenCookieJweStringSerializer tokenCookieJweStringSerializer(
            @Value("${jwt.cookie-token-key}") String cookieTokenKey)
            throws ParseException, KeyLengthException {
        return new TokenCookieJweStringSerializer(new DirectEncrypter(
                OctetSequenceKey.parse(cookieTokenKey)
        ));
    }

    @Bean
    public TokenCookieAuthenticationConfigurer tokenCookieAuthenticationConfigurer(
            @Value("${jwt.cookie-token-key}") String cookieTokenKey,
            JdbcTemplate jdbcTemplate
    ) throws Exception {
        return new TokenCookieAuthenticationConfigurer()
                .setTokenCookieStringDeserializer(new TokenCookieJweStringDeserializer(
                        new DirectDecrypter(OctetSequenceKey.parse(cookieTokenKey))
                ))
                .setJdbcTemplate(jdbcTemplate);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TokenCookieAuthenticationConfigurer tokenCookieAuthenticationConfigurer,
            TokenCookieJweStringSerializer tokenCookieJweStringSerializer,
            RegistrationAuthenticationFilter registrationAuthenticationFilter) throws Exception {

        TokenCookieSessionAuthenticationStrategy tokenCookieSessionAuthenticationStrategy =
                new TokenCookieSessionAuthenticationStrategy();
        tokenCookieSessionAuthenticationStrategy.setTokenStringSerializer(tokenCookieJweStringSerializer);

        http
                .formLogin(conf -> conf
                        .loginPage("/login"))
                .addFilterAfter(new GetCsrfTokenFilter(), ExceptionTranslationFilter.class)
                .addFilterBefore(registrationAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new RestAuthenticatedFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers(
                                        "/error",
                                        "/",
                                        "/map",
                                        "/css/**",
                                        "/js/**",
                                        "/images/**",
                                        "/api/**",
                                        "/csrf").permitAll()
                                .requestMatchers(
                                        "/login",
                                        "/signup").anonymous()
                                .anyRequest().authenticated())
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionAuthenticationStrategy(tokenCookieSessionAuthenticationStrategy))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(new CookieCsrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .sessionAuthenticationStrategy((authentication, request, response) -> {}));

        http.apply(tokenCookieAuthenticationConfigurer);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailService(dataSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RegistrationAuthenticationFilter registrationAuthenticationFilter(AuthenticationManager authenticationManager,
                                                                             UserDAO userDAO,
                                                                             TokenCookieJweStringSerializer serializer) {
        return new RegistrationAuthenticationFilter(authenticationManager, userDAO, passwordEncoder(), serializer);
    }
}
