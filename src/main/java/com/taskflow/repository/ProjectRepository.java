package com.taskflow.repository;

import com.taskflow.dto.UpdateProjectRequest;
import com.taskflow.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ProjectRepository {

    private final JdbcTemplate jdbcTemplate;
    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private Project mapProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getLong("id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        project.setOwnerId(toLong(rs.getObject("owner_id")));
        project.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        project.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return project;
    }

    public Long createProject(Project project) {
        String sql = """
            INSERT INTO projects(name, description, owner_id)
            VALUES (?, ?, ?)
            RETURNING id
            """;

        return jdbcTemplate.queryForObject(sql, Long.class, project.getName(), project.getDescription(), project.getOwnerId());
    }

    public List<Project> getProjectsByUserId(Long userId) {
        String sql = """
            SELECT p.*
            FROM projects p
            JOIN project_members pm ON pm.project_id = p.id
            WHERE pm.user_id = ?
            ORDER BY p.updated_at DESC NULLS LAST, p.id DESC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapProject(rs), userId);
    }

    public long countProjectsByUserId(Long userId) {
        String sql = """
            SELECT COUNT(*)
            FROM project_members
            WHERE user_id = ?
            """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId);
        return count != null ? count : 0L;
    }

    public Project getProjectById(Long id) {
        String sql = "SELECT * FROM projects WHERE id = ?";
        List<Project> projects = jdbcTemplate.query(sql, (rs, rowNum) -> mapProject(rs), id);
        return projects.isEmpty() ? null : projects.get(0);
    }

    public int updateProject(Long id, UpdateProjectRequest request) {
        String sql = """
                UPDATE projects
                SET name = ?,
                    description = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, request.getName(), request.getDescription(), id);
    }

    public int deleteProject(Long id) {
        String sql = "DELETE FROM projects WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
