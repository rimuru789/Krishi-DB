package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.MarketPrice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MarketPriceDAO {

    public List<MarketPrice> getAllPrices() {
        List<MarketPrice> list = new ArrayList<>();
        String sql = """
            SELECT id, commodity, market, district, price, unit, recorded_at
            FROM market_prices
            ORDER BY commodity ASC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToMarketPrice(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching market prices: " + e.getMessage());
        }
        return list;
    }

    public List<MarketPrice> searchPrices(String query, String marketFilter) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, commodity, market, district, price, unit, recorded_at
            FROM market_prices
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (marketFilter != null && !marketFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(marketFilter)) {
            sql.append(" AND market = ?");
            params.add(marketFilter.trim());
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (commodity LIKE ? OR market LIKE ? OR district LIKE ?)");
            String pattern = "%" + query.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY commodity ASC");

        List<MarketPrice> list = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMarketPrice(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching market prices: " + e.getMessage());
        }
        return list;
    }

    public List<String> getDistinctMarkets() {
        List<String> markets = new ArrayList<>();
        String sql = "SELECT DISTINCT market FROM market_prices WHERE market IS NOT NULL AND TRIM(market) != '' ORDER BY market";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                markets.add(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching distinct markets: " + e.getMessage());
        }
        return markets;
    }

    public int getCommoditiesCount() {
        String sql = "SELECT COUNT(DISTINCT commodity) FROM market_prices";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting commodities: " + e.getMessage());
        }
        return 0;
    }

    private MarketPrice mapResultSetToMarketPrice(ResultSet rs) throws SQLException {
        return new MarketPrice(
                rs.getInt("id"),
                rs.getString("commodity"),
                rs.getString("market"),
                rs.getString("district"),
                rs.getDouble("price"),
                rs.getString("unit"),
                rs.getString("recorded_at")
        );
    }
}
