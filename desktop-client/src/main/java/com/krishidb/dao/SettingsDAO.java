package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.AppSetting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsDAO {

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM app_settings WHERE setting_key = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String val = resultSet.getString("setting_value");
                    return (val != null) ? val : defaultValue;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading setting for key '" + key + "': " + e.getMessage());
        }

        return defaultValue;
    }

    public boolean saveSetting(String key, String value) {
        String sql = """
            INSERT INTO app_settings (setting_key, setting_value)
            VALUES (?, ?)
            ON CONFLICT(setting_key) DO UPDATE SET setting_value = excluded.setting_value
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, key);
            statement.setString(2, value);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving setting for key '" + key + "': " + e.getMessage());
            return false;
        }
    }

    public Map<String, String> getAllSettings() {
        Map<String, String> settings = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM app_settings";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                settings.put(
                    resultSet.getString("setting_key"),
                    resultSet.getString("setting_value")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all settings: " + e.getMessage());
        }

        return settings;
    }

    public List<AppSetting> getAllSettingsList() {
        List<AppSetting> list = new ArrayList<>();
        Map<String, String> map = getAllSettings();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            list.add(new AppSetting(entry.getKey(), entry.getValue()));
        }
        return list;
    }
}
