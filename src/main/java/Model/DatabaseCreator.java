package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseCreator {
    private static final String DATABASE_URL = "jdbc:sqlite:/Users/zachholt/Desktop/ledgerSystem.db";

    public static void createDatabase() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {
            
            statement.execute("""
                CREATE TABLE IF NOT EXISTS Customer (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Name TEXT NOT NULL,
                    DOB TEXT,
                    PhoneNumber TEXT,
                    StreetAddress TEXT,
                    City TEXT,
                    State TEXT,
                    ZipCode TEXT,
                    CreatedDate TEXT
                )
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS Account (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    CustomerID INTEGER NOT NULL,
                    Name TEXT NOT NULL,
                    AccountNumber TEXT UNIQUE NOT NULL,
                    Balance REAL DEFAULT 0.0,
                    FOREIGN KEY (CustomerID) REFERENCES Customer(ID)
                )
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS "Transaction" (
                    ID TEXT PRIMARY KEY,
                    AccountID INTEGER NOT NULL,
                    Amount REAL NOT NULL,
                    Type TEXT NOT NULL,
                    TransactionDateTime TEXT NOT NULL,
                    MerchantName TEXT,
                    MerchantType TEXT,
                    FOREIGN KEY (AccountID) REFERENCES Account(ID)
                )
            """);

            System.out.println("Database tables initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
} 