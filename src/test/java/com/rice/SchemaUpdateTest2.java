package com.rice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
public class SchemaUpdateTest2 {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void updateConstraint() {
        try {
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
            jdbcTemplate.execute("ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('USER','ADMIN','EMPLOYEE'))");
            System.out.println("SCHEMA CONSTRAINT UPDATE SUCCESSFUL");
        } catch (Exception e) {
            System.out.println("SCHEMA CONSTRAINT UPDATE FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
