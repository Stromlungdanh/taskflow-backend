package com.taskflow.repository;

import com.taskflow.dto.PageResponse;
import com.taskflow.dto.TaskResponse;
import com.taskflow.model.Task;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;
    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private Task mapTask(java.sql.ResultSet rs) throws java.sql.SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(rs.getString("status"));
        task.setPriority(rs.getString("priority"));
        task.setProjectId(toLong(rs.getObject("project_id")));
        task.setCreatedBy(toLong(rs.getObject("created_by")));
        task.setAssigneeId(toLong(rs.getObject("assignee_id")));
        task.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        task.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        task.setOrderIndex((Integer) rs.getObject("order_index"));
        return task;
    }

    public void createTask(Task task) {
        String sql = """
                INSERT INTO tasks (title, description, status, priority, project_id, created_by, assignee_id, order_index)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, task.getTitle(), task.getDescription(), task.getStatus(), task.getPriority(), task.getProjectId(), task.getCreatedBy(), task.getAssigneeId(), task.getOrderIndex());
    }

    public Task getTaskById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        List<Task> tasks = jdbcTemplate.query(sql, (rs, rowNum) -> mapTask(rs), id);
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    public int updateTask(Long id, com.taskflow.dto.UpdateTaskRequest request) {
        String sql = """
                UPDATE tasks
                SET title = ?, description = ?, status = ?, priority = ?, assignee_id = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql, request.getTitle(), request.getDescription(), request.getStatus(), request.getPriority(), request.getAssigneeId(), id);
    }

    public int updateTaskStatus(Long id, String status) {
        String sql = """
            UPDATE tasks SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?
            """;
        return jdbcTemplate.update(sql, status, id);
    }

    public int deleteTask(Long id) {
        return jdbcTemplate.update("DELETE FROM tasks WHERE id = ?", id);
    }

    public List<TaskResponse> findTasksByProjectIdWithFilter(Long projectId, String status, String priority, Long assigneeId, int limit, int offset) {
        StringBuilder sql = new StringBuilder("""
        SELECT id, title, description, status, priority, project_id, assignee_id
        FROM tasks
        WHERE project_id = ?
        """);
        List<Object> params = new ArrayList<>();
        params.add(projectId);
        if (status != null && !status.isBlank()) { sql.append(" AND status = ?"); params.add(status); }
        if (priority != null && !priority.isBlank()) { sql.append(" AND priority = ?"); params.add(priority); }
        if (assigneeId != null) { sql.append(" AND assignee_id = ?"); params.add(assigneeId); }
        sql.append(" ORDER BY order_index ASC NULLS LAST, id DESC LIMIT ? OFFSET ?");
        params.add(limit); params.add(offset);
        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            TaskResponse task = new TaskResponse();
            task.setId(rs.getLong("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setStatus(rs.getString("status"));
            task.setPriority(rs.getString("priority"));
            task.setProjectId(rs.getLong("project_id"));
            Object assigneeObj = rs.getObject("assignee_id");
            task.setAssigneeId(assigneeObj != null ? ((Number) assigneeObj).longValue() : null);
            return task;
        });
    }

    public long countTasksByProjectIdWithFilter(Long projectId, String status, String priority, Long assigneeId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM tasks WHERE project_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(projectId);
        if (status != null && !status.isBlank()) { sql.append(" AND status = ?"); params.add(status); }
        if (priority != null && !priority.isBlank()) { sql.append(" AND priority = ?"); params.add(priority); }
        if (assigneeId != null) { sql.append(" AND assignee_id = ?"); params.add(assigneeId); }
        Long count = jdbcTemplate.queryForObject(sql.toString(), params.toArray(), Long.class);
        return count != null ? count : 0L;
    }

    public int updateTaskPosition(Long id, String status, Integer orderIndex) {
        String sql = """
            UPDATE tasks SET status = ?, order_index = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?
            """;
        return jdbcTemplate.update(sql, status, orderIndex, id);
    }

    public PageResponse<TaskResponse> searchTasks(Long projectId, String title, String priority, String status, Long assigneeId, int page, int size) {
        StringBuilder sql = new StringBuilder("""
        SELECT t.id, t.title, t.description, t.status, t.priority, t.project_id, t.assignee_id
        FROM tasks t
        WHERE t.project_id = ?
        """);
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM tasks t WHERE t.project_id = ?");
        List<Object> params = new ArrayList<>();
        List<Object> countParams = new ArrayList<>();
        params.add(projectId); countParams.add(projectId);
        if (title != null && !title.trim().isEmpty()) {
            String keyword = "%" + title.trim() + "%";
            sql.append(" AND LOWER(t.title) LIKE LOWER(?)");
            countSql.append(" AND LOWER(t.title) LIKE LOWER(?)");
            params.add(keyword); countParams.add(keyword);
        }
        if (priority != null && !priority.trim().isEmpty()) {
            sql.append(" AND t.priority = ?"); countSql.append(" AND t.priority = ?");
            params.add(priority.trim()); countParams.add(priority.trim());
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND t.status = ?"); countSql.append(" AND t.status = ?");
            params.add(status.trim()); countParams.add(status.trim());
        }
        if (assigneeId != null) {
            sql.append(" AND t.assignee_id = ?"); countSql.append(" AND t.assignee_id = ?");
            params.add(assigneeId); countParams.add(assigneeId);
        }
        sql.append(" ORDER BY t.order_index ASC NULLS LAST, t.id ASC LIMIT ? OFFSET ?");
        params.add(size); params.add(page * size);
        List<TaskResponse> content = jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            TaskResponse task = new TaskResponse();
            task.setId(rs.getLong("id")); task.setTitle(rs.getString("title")); task.setDescription(rs.getString("description"));
            task.setStatus(rs.getString("status")); task.setPriority(rs.getString("priority")); task.setProjectId(rs.getLong("project_id"));
            Object assigneeObj = rs.getObject("assignee_id");
            task.setAssigneeId(assigneeObj != null ? ((Number) assigneeObj).longValue() : null);
            return task;
        });
        Long totalElements = jdbcTemplate.queryForObject(countSql.toString(), countParams.toArray(), Long.class);
        int totalPages = (int) Math.ceil((double) (totalElements != null ? totalElements : 0L) / size);
        return new PageResponse<>(content, page, size, totalElements != null ? totalElements : 0L, totalPages);
    }

    public boolean existsByProjectIdAndAssigneeId(Long projectId, Long assigneeId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tasks WHERE project_id = ? AND assignee_id = ?", Integer.class, projectId, assigneeId);
        return count != null && count > 0;
    }

    public long countTasksInProjectsByUserId(Long userId) {
        String sql = """
            SELECT COUNT(*)
            FROM tasks t
            JOIN project_members pm ON pm.project_id = t.project_id
            WHERE pm.user_id = ?
            """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId);
        return count != null ? count : 0L;
    }

    public long countTasksInProjectsByUserIdAndStatus(Long userId, String status) {
        String sql = """
            SELECT COUNT(*)
            FROM tasks t
            JOIN project_members pm ON pm.project_id = t.project_id
            WHERE pm.user_id = ? AND t.status = ?
            """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId, status);
        return count != null ? count : 0L;
    }
}
