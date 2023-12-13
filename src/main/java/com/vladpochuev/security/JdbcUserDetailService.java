package com.vladpochuev.security;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Optional;

public class JdbcUserDetailService extends MappingSqlQuery<UserDetails> implements UserDetailsService {

    public JdbcUserDetailService(DataSource ds) {
        super(ds, """
            SELECT
            users.username,
            users.password,
            array_agg(user_authorities.authority) AS authorities
            FROM users
            LEFT JOIN user_authorities ON user_authorities.id_user = users.id
            WHERE users.username = :username
            GROUP BY users.id
        """);
        this.declareParameter(new SqlParameter("username", Types.VARCHAR));
        this.compile();
    }

    @Override
    protected UserDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .authorities((String[])rs.getArray("authorities").getArray())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return Optional.ofNullable(this.findObjectByNamedParam(Map.of("username", username)))
                .orElseThrow(() -> new UsernameNotFoundException("Username %s not found".formatted(username)));
    }
}