package com.strange.safety;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationHack {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationHack(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        System.out.println("==================================================");
        System.out.println("🛠️ Running DB Migration Hack to drop old columns...");
        try {
            jdbcTemplate.execute("ALTER TABLE cameras DROP COLUMN source_type;");
            System.out.println("✅ Dropped source_type column successfully.");
        } catch (Exception e) {
            System.out.println("⚠️ Could not drop source_type (maybe already dropped): " + e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE cameras DROP COLUMN assigned_video_path;");
            System.out.println("✅ Dropped assigned_video_path column successfully.");
        } catch (Exception e) {
            System.out.println("⚠️ Could not drop assigned_video_path (maybe already dropped): " + e.getMessage());
        }
        System.out.println("==================================================");
    }
}
