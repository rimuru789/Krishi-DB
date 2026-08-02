package com.krishidb.database;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String DATABASE_NAME = "krishidb.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on the classpath", e);
        }

        Path databasePath = resolveDatabasePath();
        String databaseUrl = "jdbc:sqlite:" + databasePath.toAbsolutePath().normalize();
        return DriverManager.getConnection(databaseUrl);
    }

    private static Path resolveDatabasePath() {
        Path currentDir = Paths.get("").toAbsolutePath().normalize();
        Path[] candidates = {
            currentDir.resolve("data"),
            currentDir.resolve("desktop-client").resolve("data"),
            currentDir.getParent() != null ? currentDir.getParent().resolve("desktop-client").resolve("data") : null
        };

        Path selectedDirectory = null;
        for (Path candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            if (Files.exists(candidate)) {
                selectedDirectory = candidate;
                break;
            }
        }

        if (selectedDirectory == null) {
            selectedDirectory = candidates[0];
        }

        try {
            Files.createDirectories(selectedDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to create database directory: " + selectedDirectory, e);
        }

        return selectedDirectory.resolve(DATABASE_NAME);
    }
}