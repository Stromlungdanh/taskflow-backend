package com.taskflow.repository;

import com.taskflow.dto.UserOptionResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProjectMemberRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProjectMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UserOptionResponse> getMembersByProjectId(Long projectId) {
        String sql = """
            SELECT u.id, u.username
            FROM project_members pm
            JOIN users u ON pm.user_id = u.id
            WHERE pm.project_id = ?
            ORDER BY u.username ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new UserOptionResponse(
                        rs.getLong("id"),
                        rs.getString("username")
                ), projectId);
    }

    public boolean existsByProjectIdAndUserId(Long projectId, Long userId) {
        String sql = """
            SELECT COUNT(*)
            FROM project_members
            WHERE project_id = ? AND user_id = ?
        """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, projectId, userId);
        return count != null && count > 0;
    }

    public void addMember(Long projectId, Long userId, String role) {
        String sql = """
            INSERT INTO project_members(project_id, user_id, role)
            VALUES (?, ?, ?)
        """;
        jdbcTemplate.update(sql, projectId, userId, role);
    }

    public int removeMember(Long projectId, Long userId) {
        String sql = """
            DELETE FROM project_members
            WHERE project_id = ? AND user_id = ?
        """;

        return jdbcTemplate.update(sql, projectId, userId);
    }

    public boolean isMember(Long projectId, Long userId) {
        return existsByProjectIdAndUserId(projectId, userId);
    }

    public String getRole(Long projectId, Long userId) {
        String sql = """
            SELECT role
            FROM project_members
            WHERE project_id = ? AND user_id = ?
        """;

        List<String> roles = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("role"),
                projectId,
                userId
        );

        return roles.isEmpty() ? null : roles.get(0);
    }

    public int updateRole(Long projectId, Long userId, String role) {
        String sql = """
            UPDATE project_members
            SET role = ?
            WHERE project_id = ? AND user_id = ?
        """;

        return jdbcTemplate.update(sql, role, projectId, userId);
    }
}