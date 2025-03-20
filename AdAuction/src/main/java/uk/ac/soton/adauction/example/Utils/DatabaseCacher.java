package uk.ac.soton.adauction.example.Utils;

import java.sql.*;
import java.io.File;

public class DatabaseCacher {
    private static Connection localConnection = DatabaseConnection.getLocalLogsConnection();
    private static Connection remoteConnection;
    private static final String DB_FILE_PATH = "logs.db"; // Path to the SQLite database file
    private static final String CREATE_CLICK_LOG_TABLE =
            "CREATE TABLE click_log ("
                    + "  click_date DATETIME, "
                    + "  id BIGINT, "
                    + "  click_cost DECIMAL(10,5), "
                    + "  PRIMARY KEY (click_date, id) "
                    + ")";

    private static final String CREATE_IMPRESSION_LOG_TABLE =
            "CREATE TABLE impression_log ("
                    + "  impression_date DATETIME, "
                    + "  id BIGINT, "
                    + "  gender VARCHAR(10), "
                    + "  age VARCHAR(10), "
                    + "  income VARCHAR(10), "
                    + "  context VARCHAR(50), "
                    + "  impression_cost DECIMAL(10,5), "
                    + "  PRIMARY KEY (impression_date, id) "
                    + ")";

    private static final String CREATE_SERVER_LOG_TABLE =
            "CREATE TABLE server_log ("
                    + "  entry_date DATETIME, "
                    + "  id BIGINT, "
                    + "  exit_date DATETIME, "
                    + "  pages_viewed INT, "
                    + "  conversion VARCHAR(10), "
                    + "  PRIMARY KEY (entry_date, id) "
                    + ")";
    private static final String CREATE_UNIQUE_USERS_TABLE =
            "CREATE TABLE unique_users ("
                    + "  id BIGINT PRIMARY KEY, "
                    + "  gender VARCHAR(10), "
                    + "  age VARCHAR(10), "
                    + "  income VARCHAR(10), "
                    + "  context VARCHAR(20) "
                    + ")";

    public static void main(String[] args) {
        try {
            cacheDatabase(DatabaseConnection.getRemoteLogsConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createSQLiteTable(String createTableSQL, String tableName) throws SQLException {
        System.out.println("Creating table " + tableName);
        try {
            PreparedStatement stmt = localConnection.prepareStatement("DROP TABLE IF EXISTS " + tableName);
            stmt.executeUpdate();

            stmt = localConnection.prepareStatement(createTableSQL);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void cacheDatabase(Connection mysqlconn) throws SQLException {
        File dbFile = new File(DB_FILE_PATH);
        if (dbFile.exists()) {
            System.out.println("logs.db already exists. Skipping database caching.");
            return;
        }

        try (Statement pragmaStmt = localConnection.createStatement()) {
            pragmaStmt.execute("PRAGMA synchronous = OFF;");  // Faster inserts, less disk I/O
            pragmaStmt.execute("PRAGMA journal_mode = OFF;"); // Disables rollback logging for speed
        }


        remoteConnection = mysqlconn;
        try {


            createSQLiteTable(CREATE_CLICK_LOG_TABLE, "click_log");
            createSQLiteTable(CREATE_IMPRESSION_LOG_TABLE, "impression_log");
            createSQLiteTable(CREATE_SERVER_LOG_TABLE, "server_log");
            createSQLiteTable(CREATE_UNIQUE_USERS_TABLE, "unique_users");

            migrateTable(mysqlconn, "SELECT * FROM click_log", "click_log");
            migrateTable(mysqlconn, "SELECT * FROM impression_log", "impression_log");
            migrateTable(mysqlconn, "SELECT * FROM server_log", "server_log");
            migrateTable(mysqlconn, "SELECT DISTINCT id, gender, age, income, context FROM impression_log", "unique_users");


            indexTables();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




    private static void migrateTable(Connection mysqlConn, String selectSQL, String tableName) throws SQLException {


        String insertSQL = getInsertSQL(tableName);

        try (
                PreparedStatement mysqlStmt = mysqlConn.prepareStatement(selectSQL);
                PreparedStatement sqliteStmt = localConnection.prepareStatement(insertSQL);
                ResultSet rs = mysqlStmt.executeQuery()
        ) {

            mysqlStmt.setFetchSize(Integer.MIN_VALUE);
            localConnection.setAutoCommit(false);  // Batch insert optimization
            int batchSize = 0;
            final int BATCH_LIMIT = 10_000;

            while (rs.next()) {
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    sqliteStmt.setObject(i, rs.getObject(i));
                }
                sqliteStmt.addBatch();
                batchSize++;

                if (batchSize == BATCH_LIMIT) {
                    sqliteStmt.executeBatch();
                    localConnection.commit();
                    batchSize = 0;
                }
            }

            sqliteStmt.executeBatch();
            localConnection.commit();
            System.out.println("Migrated table: " + tableName);
        }
    }

    // Generates an INSERT statement dynamically
    private static String getInsertSQL(String tableName) {
        switch (tableName) {
            case "click_log":
                return "INSERT INTO click_log (click_date, id, click_cost) VALUES (?, ?, ?)";
            case "impression_log":
                return "INSERT INTO impression_log (impression_date, id, gender, age, income, context, impression_cost) VALUES (?, ?, ?, ?, ?, ?, ?)";
            case "server_log":
                return "INSERT INTO server_log (entry_date, id, exit_date, pages_viewed, conversion) VALUES (?, ?, ?, ?, ?)";
            case "unique_users":
                return "INSERT INTO unique_users (id, gender, age, income, context) VALUES (?, ?, ?, ?, ?)";
            default:
                throw new IllegalArgumentException("Unknown table: " + tableName);
        }
    }

    private static void indexTables() {

        try {
            String indexingQuery = "CREATE INDEX IF NOT EXISTS idx_timestamp ON click_log (id);";
            Statement indexingstmt = localConnection.createStatement();
            indexingstmt.execute(indexingQuery);

            indexingQuery = "CREATE INDEX IF NOT EXISTS idx_timestamp ON impression_log (id);";
            indexingstmt.execute(indexingQuery);

            indexingQuery = "CREATE INDEX IF NOT EXISTS idx_timestamp ON server_log (id);";
            indexingstmt.execute(indexingQuery);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
