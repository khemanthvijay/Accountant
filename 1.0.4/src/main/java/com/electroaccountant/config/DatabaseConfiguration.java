package com.electroaccountant.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseConfiguration {
    @Bean
    DataSource dataSource(@Value("${electro-accountant.data-dir}") String dataDirectory) throws Exception {
        Path directory = Path.of(dataDirectory).toAbsolutePath().normalize();
        Files.createDirectories(directory);
        Path database = directory.resolve("electro-accountant.db");

        // SQLite URI parameters apply safety settings whenever a connection is opened.
        String url = "jdbc:sqlite:file:" + database.toString().replace('\\', '/')
                + "?foreign_keys=on&journal_mode=WAL&synchronous=FULL&busy_timeout=5000";
        repairLegacyPaymentColumns(url);
        return new DriverManagerDataSource(url);
    }

    /**
     * Some early local SQLite files have Flyway history for payment migrations
     * but do not contain the physical columns. Repair this before Flyway and
     * repository SQL can read those tables.
     */
    private void repairLegacyPaymentColumns(String url) throws Exception {
        try (Connection connection = DriverManager.getConnection(url)) {
            addColumnIfMissing(connection, "sales_entry", "payment_type", "TEXT NOT NULL DEFAULT 'CREDIT'");
            addColumnIfMissing(connection, "purchase_entry", "payment_type", "TEXT NOT NULL DEFAULT 'CREDIT'");
        }
    }

    private void addColumnIfMissing(Connection connection, String table, String column, String definition) throws Exception {
        if (!tableExists(connection, table) || hasColumn(connection, table, column)) return;
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private boolean tableExists(Connection connection, String table) throws Exception {
        try (var statement = connection.prepareStatement("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            statement.setString(1, table);
            try (ResultSet result = statement.executeQuery()) { return result.next(); }
        }
    }

    private boolean hasColumn(Connection connection, String table, String column) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet columns = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (columns.next()) if (column.equalsIgnoreCase(columns.getString("name"))) return true;
            return false;
        }
    }
}
