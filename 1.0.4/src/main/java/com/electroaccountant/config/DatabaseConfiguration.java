package com.electroaccountant.config;

import java.nio.file.Files;
import java.nio.file.Path;

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
        return new DriverManagerDataSource(url);
    }
}
