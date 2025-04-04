package uk.ac.soton.adauction.example.Utils;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.io.*;
import java.sql.*;
import java.util.*;

public class CampaignImporter {
    private static final Connection conn = DatabaseConnection.getLocalLogsConnection();

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

    public static void importCampaign(String clickLogCSV, String impressionLogCSV, String serverLogCSV) {
        try {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute( "PRAGMA automatic_index = OFF");
                stmt.execute("PRAGMA synchronous = OFF");
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA temp_store = MEMORY");
                stmt.execute("PRAGMA foreign_keys = OFF");
            }

            dropTables();
            createTables();
            importImpressionsAndPopulateUsers(impressionLogCSV);

            importCSV(clickLogCSV,
                    "INSERT OR IGNORE INTO click_log VALUES (?, ?, ?)",
                    3);

            importCSV(serverLogCSV,
                    "INSERT OR IGNORE INTO server_log VALUES (?, ?, ?, ?, ?)",
                    5);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void dropTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS click_log");
        stmt.execute("DROP TABLE IF EXISTS impression_log");
        stmt.execute("DROP TABLE IF EXISTS server_log");
        stmt.execute("DROP TABLE IF EXISTS unique_users");
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_CLICK_LOG_TABLE);
            stmt.execute(CREATE_IMPRESSION_LOG_TABLE);
            stmt.execute(CREATE_SERVER_LOG_TABLE);
            stmt.execute(CREATE_UNIQUE_USERS_TABLE);
        }
    }

    private static void importImpressionsAndPopulateUsers(String impressionLogCSV) {
        final String insertSQL = "INSERT OR IGNORE INTO impression_log VALUES (?, ?, ?, ?, ?, ?, ?)";
        final String userInsertSQL = "INSERT OR IGNORE INTO unique_users VALUES (?, ?, ?, ?, ?)";
        final int BATCH_SIZE = 5000;

        LongOpenHashSet seenUserIds = new LongOpenHashSet();

        try (
                BufferedReader reader = new BufferedReader(new FileReader(impressionLogCSV), 16 * 1024);
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                PreparedStatement userStmt = conn.prepareStatement(userInsertSQL)
        ) {
            conn.setAutoCommit(false);
            reader.readLine(); // skip header
            String line;
            int batchCount = 0;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] fields = line.split(",", -1);
                if (fields.length < 7) continue;

                String date = fields[0].strip();
                long userId = Long.parseLong(fields[1].strip());
                String gender = fields[2].strip();
                String age = fields[3].strip();
                String income = fields[4].strip();
                String context = fields[5].strip();
                double cost = Double.parseDouble(fields[6].strip());

                pstmt.setString(1, date);
                pstmt.setLong(2, userId);
                pstmt.setString(3, gender);
                pstmt.setString(4, age);
                pstmt.setString(5, income);
                pstmt.setString(6, context);
                pstmt.setDouble(7, cost);
                pstmt.addBatch();

                if (!seenUserIds.contains(userId)) {
                    seenUserIds.add(userId);
                    userStmt.setLong(1, userId);
                    userStmt.setString(2, gender);
                    userStmt.setString(3, age);
                    userStmt.setString(4, income);
                    userStmt.setString(5, context);
                    userStmt.addBatch();
                }

                if (++batchCount % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    userStmt.executeBatch();
                    conn.commit();
                }
            }

            pstmt.executeBatch();
            userStmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);

            System.out.println("Impression log and unique users imported successfully.");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }

    private static void importCSV(String csvFile, String insertSQL, int expectedColumns) {
        try (
                BufferedReader reader = new BufferedReader(new FileReader(csvFile));
                PreparedStatement pstmt = conn.prepareStatement(insertSQL)
        ) {
            conn.setAutoCommit(false);
            reader.readLine(); // skip header
            String line;
            int batchCount = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split(",", -1);
                if (fields.length < expectedColumns) continue;

                for (int i = 0; i < expectedColumns; i++) {
                    pstmt.setString(i + 1, fields[i].trim());
                }

                pstmt.addBatch();

                if (++batchCount % 1000 == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                }
            }

            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Imported " + csvFile + " successfully.");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }
}
