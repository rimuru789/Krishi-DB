package com.krishidb.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {

            // Enable foreign-key checking in SQLite
            statement.execute("PRAGMA foreign_keys = ON");

            // ---------------- USERS ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'USER',
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // ---------------- CUSTOMERS ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT,
                    village TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    sync_status TEXT DEFAULT 'PENDING'
                )
            """);

            // ---------------- SUPPLIERS ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS suppliers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT,
                    village TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    sync_status TEXT DEFAULT 'PENDING'
                )
            """);

            // ---------------- PRODUCTS ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT,
                    unit TEXT NOT NULL,
                    selling_price REAL NOT NULL DEFAULT 0,
                    stock_quantity REAL NOT NULL DEFAULT 0,
                    low_stock_level REAL DEFAULT 5,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    sync_status TEXT DEFAULT 'PENDING'
                )
            """);

            // ---------------- SALES ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER,
                    total_amount REAL NOT NULL,
                    payment_method TEXT,
                    sale_date TEXT DEFAULT CURRENT_TIMESTAMP,
                    notes TEXT,
                    sync_status TEXT DEFAULT 'PENDING',

                    FOREIGN KEY (customer_id)
                    REFERENCES customers(id)
                )
            """);

            // ---------------- SALE ITEMS ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS sale_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER NOT NULL,
                    product_id INTEGER NOT NULL,
                    quantity REAL NOT NULL,
                    price_per_unit REAL NOT NULL,
                    subtotal REAL NOT NULL,

                    FOREIGN KEY (sale_id)
                    REFERENCES sales(id),

                    FOREIGN KEY (product_id)
                    REFERENCES products(id)
                )
            """);

            // ---------------- PURCHASES ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS purchases (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    supplier_id INTEGER,
                    total_amount REAL NOT NULL,
                    purchase_date TEXT DEFAULT CURRENT_TIMESTAMP,
                    notes TEXT,
                    sync_status TEXT DEFAULT 'PENDING',

                    FOREIGN KEY (supplier_id)
                    REFERENCES suppliers(id)
                )
            """);

            // ---------------- PURCHASE ITEMS ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS purchase_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    purchase_id INTEGER NOT NULL,
                    product_id INTEGER NOT NULL,
                    quantity REAL NOT NULL,
                    price_per_unit REAL NOT NULL,
                    subtotal REAL NOT NULL,

                    FOREIGN KEY (purchase_id)
                    REFERENCES purchases(id),

                    FOREIGN KEY (product_id)
                    REFERENCES products(id)
                )
            """);

            // ---------------- EXPENSES ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category TEXT NOT NULL,
                    description TEXT,
                    amount REAL NOT NULL,
                    expense_date TEXT DEFAULT CURRENT_TIMESTAMP,
                    sync_status TEXT DEFAULT 'PENDING'
                )
            """);

            // ---------------- TRANSACTIONS ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaction_type TEXT NOT NULL,
                    reference_id INTEGER,
                    amount REAL NOT NULL,
                    payment_method TEXT,
                    transaction_date TEXT DEFAULT CURRENT_TIMESTAMP,
                    sync_status TEXT DEFAULT 'PENDING'
                )
            """);

            // ---------------- MARKET PRICES ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS market_prices (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    commodity TEXT NOT NULL,
                    market TEXT,
                    district TEXT,
                    price REAL NOT NULL,
                    unit TEXT NOT NULL,
                    recorded_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // ---------------- SYNC QUEUE ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS sync_queue (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    table_name TEXT NOT NULL,
                    record_id INTEGER NOT NULL,
                    operation TEXT NOT NULL,
                    status TEXT DEFAULT 'PENDING',
                    attempts INTEGER DEFAULT 0,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_attempt_at TEXT
                )
            """);

            // ---------------- SETTINGS ----------------
            statement.execute("""
                CREATE TABLE IF NOT EXISTS app_settings (
                    setting_key TEXT PRIMARY KEY,
                    setting_value TEXT
                )
            """);

            System.out.println("Krishi-DB database initialized successfully.");

        } catch (SQLException e) {

            System.out.println("Database initialization failed.");
            e.printStackTrace();
        }
    }
}