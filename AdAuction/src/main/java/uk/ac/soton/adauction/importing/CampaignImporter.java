package uk.ac.soton.adauction.importing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CampaignImporter {

    // Connection details - rewriteBatchedStatements groups insert statements leading to huge optimisation
    // MAKE SURE TO LEAVE REWRITEBATCHEDSTATEMENTS IN THE URL IF YOU REPLACE IT WITH YOUR SQL SERVER
    private static final String JDBC_URL      = "jdbc:mysql://185.247.116.78:3306/2month_campaign?user=root&password=Group34Southampton!&rewriteBatchedStatements=true";
    private static final String JDBC_USER     = "root";
    private static final String JDBC_PASSWORD = "Group34Southampton!";

    private static final String CREATE_CLICK_LOG_TABLE =
            "CREATE TABLE IF NOT EXISTS click_log ("
                    + "  click_date DATETIME, "
                    + "  id BIGINT, "
                    + "  click_cost DECIMAL(10,5), "
                    + "  PRIMARY KEY (click_date, id) "
                    + ")";

    private static final String CREATE_IMPRESSION_LOG_TABLE =
            "CREATE TABLE IF NOT EXISTS impression_log ("
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
            "CREATE TABLE IF NOT EXISTS server_log ("
                    + "  entry_date DATETIME, "
                    + "  id BIGINT, "
                    + "  exit_date DATETIME, "
                    + "  pages_viewed INT, "
                    + "  conversion VARCHAR(10), "
                    + "  PRIMARY KEY (entry_date, id) "
                    + ")";

    private static final String INSERT_CLICK_LOG =
            "INSERT INTO click_log (click_date, id, click_cost) "
                    + "VALUES (?, ?, ?)";

    private static final String INSERT_IMPRESSION_LOG =
            "INSERT IGNORE INTO impression_log (impression_date, id, gender, age, income, context, impression_cost) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_SERVER_LOG =
            "INSERT INTO server_log (entry_date, id, exit_date, pages_viewed, conversion) "
                    + "VALUES (?, ?, ?, ?, ?)";

    /**
     * Expects three arguments: <br>
     * 1) 2_week_click_log.csv <br>
     * 2) 2_week_impression_log.csv <br>
     * 3) 2_week_server_log.csv
     *
     * @param args 2_week_click_log.csv, 2_week_impression_log.csv, 2_week_server_log.csv
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java CampaignImporter <click_log.csv> <impression_log.csv> <server_log.csv>");
            System.exit(1);
        }

        String clickLogPath      = args[0];
        String impressionLogPath = args[1];
        String serverLogPath     = args[2];

        // Connect to database
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            createTablesIfNotExist(conn);

            // Import each file
            importClickLog(conn, clickLogPath);
            importImpressionLog(conn, impressionLogPath);
            importServerLog(conn, serverLogPath);

            System.out.println("Import completed successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates tables if required.
     *
     * @param conn the database connection
     * @throws SQLException if a database access error occurs
     */
    private static void createTablesIfNotExist(Connection conn) throws SQLException {
        try (
                PreparedStatement psClickLog      = conn.prepareStatement(CREATE_CLICK_LOG_TABLE);
                PreparedStatement psImpressionLog = conn.prepareStatement(CREATE_IMPRESSION_LOG_TABLE);
                PreparedStatement psServerLog     = conn.prepareStatement(CREATE_SERVER_LOG_TABLE)
        ) {
            psClickLog.executeUpdate();
            psImpressionLog.executeUpdate();
            psServerLog.executeUpdate();
        }
    }

    /**
     * CSV fields: Date, ID, Click Cost <br>
     * Uses manual commits to try to optimize large inserts.
     *
     * @param conn         the database connection
     * @param clickLogPath the path to the click log CSV file
     */
    private static void importClickLog(Connection conn, String clickLogPath) {
        long startTime = System.currentTimeMillis();
        try (BufferedReader br = new BufferedReader(new FileReader(clickLogPath));
             PreparedStatement ps = conn.prepareStatement(INSERT_CLICK_LOG)) {

            conn.setAutoCommit(false);
            // Skip header line
            String line = br.readLine();

            int batchSize = 0;
            final int BATCH_LIMIT = 1000;
            int batches = 0;
            final int  MAX_BATCHES = 10;

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 3) {
                    // Handle malformed line (more handling maybe here ? )
                    continue;
                }

                String dateStr     = fields[0].trim();
                String idStr       = fields[1].trim();
                String clickCostStr= fields[2].trim();

                ps.setString(1, dateStr);
                ps.setLong(2, Long.parseLong(idStr));
                ps.setBigDecimal(3, new java.math.BigDecimal(clickCostStr));

                ps.addBatch();

                if (++batchSize == BATCH_LIMIT) {
                    ps.executeBatch();
                    batchSize = 0;
                    if (++batches == MAX_BATCHES) {
                        conn.commit();
                        batches = 0;
                    }
                }
            }
            // Execute any remaining batched statements
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);

            System.out.println("Click log imported from " + clickLogPath);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds");
    }

    /**
     * CSV fields: Date, ID, Gender, Age, Income, Context, Impression Cost
     *
     * @param conn              the database connection
     * @param impressionLogPath the path to the impression log CSV file
     */
    private static void importImpressionLog(Connection conn, String impressionLogPath) {
        long startTime = System.currentTimeMillis();
        try (BufferedReader br = new BufferedReader(new FileReader(impressionLogPath));
             PreparedStatement ps = conn.prepareStatement(INSERT_IMPRESSION_LOG)) {

            conn.setAutoCommit(false);
            // Skip header
            String line = br.readLine();

            int batchSize = 0;
            final int BATCH_LIMIT = 10000;
            int batches = 0;
            final int  MAX_BATCHES = 10;

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 7) {
                    continue;
                }

                String dateStr   = fields[0].trim();
                String idStr     = fields[1].trim();
                String gender    = fields[2].trim();
                String age       = fields[3].trim();
                String income    = fields[4].trim();
                String context   = fields[5].trim();
                String costStr   = fields[6].trim();

                ps.setString(1, dateStr);
                ps.setLong(2, Long.parseLong(idStr));
                ps.setString(3, gender);
                ps.setString(4, age);
                ps.setString(5, income);
                ps.setString(6, context);
                ps.setBigDecimal(7, new java.math.BigDecimal(costStr));

                ps.addBatch();

                if (++batchSize == BATCH_LIMIT) {
                    ps.executeBatch();
                    batchSize = 0;
                    if (++batches == MAX_BATCHES) {
                        conn.commit();
                        batches = 0;
                    }
                }
            }
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Impression log imported from " + impressionLogPath);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds");
    }

    /**
     * CSV fields: Entry Date, ID, Exit Date, Pages Viewed, Conversion
     *
     * @param conn          the database connection
     * @param serverLogPath the path to the server log CSV file
     */
    private static void importServerLog(Connection conn, String serverLogPath) {
        long startTime = System.currentTimeMillis();
        try (BufferedReader br = new BufferedReader(new FileReader(serverLogPath));
             PreparedStatement ps = conn.prepareStatement(INSERT_SERVER_LOG)) {

            conn.setAutoCommit(false);
            String line = br.readLine();

            int batchSize = 0;
            final int BATCH_LIMIT = 1000;
            int batches = 0;
            final int  MAX_BATCHES = 10;

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 5) {
                    continue;
                }

                String entryDateStr   = fields[0].trim();
                String idStr          = fields[1].trim();
                String exitDateStr    = fields[2].trim();
                String pagesViewedStr = fields[3].trim();
                String conversion     = fields[4].trim();
                //handle n/a for dates
                if (entryDateStr.equals("n/a")) {
                    ps.setNull(1, java.sql.Types.DATE);
                }else{
                    ps.setString(1, entryDateStr);
                }

                ps.setLong(2, Long.parseLong(idStr));
                ps.setString(3, "n/a".equals(exitDateStr) ? "NULL" : exitDateStr);

                if (exitDateStr.equals("n/a")) {
                    ps.setNull(3, java.sql.Types.DATE);
                }else{
                    ps.setString(3, exitDateStr);
                }
                
                ps.setInt(4, Integer.parseInt(pagesViewedStr));
                ps.setString(5, conversion);

                ps.addBatch();

                if (++batchSize == BATCH_LIMIT) {
                    ps.executeBatch();
                    batchSize = 0;
                    if (++batches == MAX_BATCHES) {
                        conn.commit();
                        batches = 0;
                    }
                }
            }
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Server log imported from " + serverLogPath);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds");
    }
}
