package uk.ac.soton.adauction.example.Utils;

import java.sql.*;

public class DatabaseEmptier {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getRemoteLogsConnection();

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            try (Statement stmt = conn.createStatement()) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("🗑 Deleting table: " + tableName);
                    stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName);
                }
                System.out.println("✅ All tables deleted successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
