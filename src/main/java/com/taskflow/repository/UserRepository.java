package com.taskflow.repository;

import com.taskflow.dto.UserOptionResponse;
import com.taskflow.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(User user) {

        String sql = "INSERT INTO users(username,password,email,role) VALUES(?,?,?,?)";

        jdbcTemplate.update(
                sql,
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRole()
        );
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setEmail(rs.getString("email"));
            user.setRole(rs.getString("role"));
            return user;
        }, username);

        return users.isEmpty() ? null : users.get(0);
    }

    public List<UserOptionResponse> getAllUserOptions() {
        String sql = """
        SELECT id, username
        FROM users
        ORDER BY username ASC
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new UserOptionResponse(
                        rs.getLong("id"),
                        rs.getString("username")
                )
        );
    }
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setRole(rs.getString("role"));
            return user;
        }, id);

        return users.isEmpty() ? null : users.get(0);
    }




}