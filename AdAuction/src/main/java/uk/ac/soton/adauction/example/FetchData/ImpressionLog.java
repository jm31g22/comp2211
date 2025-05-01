package uk.ac.soton.adauction.example.FetchData;

import uk.ac.soton.adauction.example.FetchData.Queriers.LocalQuerier;
import uk.ac.soton.adauction.example.Utils.GraphFilters;


import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to obtain data from the impression log
 */
public class ImpressionLog extends LocalQuerier {
    private HashMap<String, String> parameters = new HashMap<>();

    /**
     * Function to fetch all impression data
     *
     * @return Array List of impressions
     */
    public ArrayList<Impression> fetchAll() {
        ArrayList<Impression> impressions = new ArrayList<>();
        try (
                Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select * from impression_log";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                Timestamp dateAndTime = rs.getTimestamp("impression_date");
                Long id = rs.getLong("id");
                String gender = rs.getString("gender");
                String age = rs.getString("age");
                String income = rs.getString("income");
                String context = rs.getString("context");
                Double impressionCost = rs.getDouble("impression_cost");
                System.out.println("Impression date: " + dateAndTime + ", id: " + id + ", gender: " + gender + ", age: " + age + ", income: " + income + ", context: " + context + ", impression cost: " + impressionCost);
                Impression impression = new Impression(dateAndTime, id, gender, age, income, context, impressionCost);
                impressions.add(impression);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return impressions;
    }

    /**
     * Function for fetching the gender count of the impression
     *
     * @return HashMap of gender count
     */
    public HashMap<String, Integer> fetchImpressionGenderCount() {
        return fetchImpressionGenderCount(LocalDateTime.MIN, LocalDateTime.now());
    }

    /**
     * fetch impression count by gender with custom date range
     * @param lowerBound
     * @param upperBound
     * @return
     */
    public HashMap<String, Integer> fetchImpressionGenderCount(LocalDateTime lowerBound,
                                                               LocalDateTime upperBound) {
        HashMap<String, Integer> counts = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String sqlByGender = ""
                + "SELECT gender, COUNT(*) AS cnt "
                + "FROM impression_log "
                + "WHERE gender = ? "
                + "  AND impression_date BETWEEN ? AND ? "
                + "GROUP BY gender";

        try (PreparedStatement stmt = conn.prepareStatement(sqlByGender)) {
            for (String gender : new String[]{"Male", "Female"}) {
                stmt.setString(1, gender);
                stmt.setString(2, lowerBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                stmt.setString(3, upperBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                System.out.println("SQL statement: " + stmt);
                ResultSet rs = stmt.executeQuery();
                counts.put(gender.toLowerCase(), rs.next() ? rs.getInt("cnt") : 0);
                System.out.println(gender + " = " + counts.get(gender.toLowerCase()));
            }

            // total count
            String sqlTotal = ""
                    + "SELECT COUNT(*) AS cnt "
                    + "FROM impression_log "
                    + "WHERE impression_date BETWEEN ? AND ?";
            try (PreparedStatement totalStmt = conn.prepareStatement(sqlTotal)) {
                totalStmt.setString(1, lowerBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                totalStmt.setString(2, upperBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                System.out.println("SQL statement: " + totalStmt);
                ResultSet rsTotal = totalStmt.executeQuery();
                counts.put("total", rsTotal.next() ? rsTotal.getInt("cnt") : 0);
                System.out.println("Total = " + counts.get("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    /**
     * Fetch impression count by age
     * @return
     */
    public HashMap<String, Integer> fetchImpressionAgeCount() {
        return fetchImpressionAgeCount(LocalDateTime.MIN, LocalDateTime.now());
    }

    /**
     * Fetch impression count by age with custom date range
     * @param lowerBound
     * @param upperBound
     * @return
     */
    public HashMap<String, Integer> fetchImpressionAgeCount(LocalDateTime lowerBound,
                                                            LocalDateTime upperBound) {
        HashMap<String, Integer> counts = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String sqlByAge = ""
                + "SELECT age, COUNT(*) AS cnt "
                + "FROM impression_log "
                + "WHERE age = ? "
                + "  AND impression_date BETWEEN ? AND ? "
                + "GROUP BY age";

        String[] ageBuckets = {"<25","25-34","35-44","45-54",">54"};
        try (PreparedStatement stmt = conn.prepareStatement(sqlByAge)) {
            for (String bucket : ageBuckets) {
                stmt.setString(1, bucket);
                stmt.setString(2, lowerBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                stmt.setString(3, upperBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                System.out.println("SQL statement: " + stmt);
                ResultSet rs = stmt.executeQuery();
                counts.put(bucket, rs.next() ? rs.getInt("cnt") : 0);
                System.out.println(bucket + " = " + counts.get(bucket));
            }

            // total
            String sqlTotal = ""
                    + "SELECT COUNT(*) AS cnt "
                    + "FROM impression_log "
                    + "WHERE impression_date BETWEEN ? AND ?";
            try (PreparedStatement totalStmt = conn.prepareStatement(sqlTotal)) {
                totalStmt.setString(1, lowerBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                totalStmt.setString(2, upperBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                System.out.println("SQL statement: " + totalStmt);
                ResultSet rsTotal = totalStmt.executeQuery();
                counts.put("total", rsTotal.next() ? rsTotal.getInt("cnt") : 0);
                System.out.println("Total = " + counts.get("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    /**
     * fetch impression count by income
     * @return
     */
    public HashMap<String, Integer> fetchImpressionIncomeCount() {
        return fetchImpressionIncomeCount(LocalDateTime.MIN, LocalDateTime.now());
    }

    /**
     * fetch impression count by income with custom date range
     * @param lowerBound
     * @param upperBound
     * @return
     */
    public HashMap<String, Integer> fetchImpressionIncomeCount(LocalDateTime lowerBound,
                                                               LocalDateTime upperBound) {
        HashMap<String, Integer> counts = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String sqlByIncome = ""
                + "SELECT income, COUNT(*) AS cnt "
                + "FROM impression_log "
                + "WHERE income = ? "
                + "  AND impression_date BETWEEN ? AND ? "
                + "GROUP BY income";

        String[] incomeLevels = {"Low","Medium","High"};
        try (PreparedStatement stmt = conn.prepareStatement(sqlByIncome)) {
            for (String level : incomeLevels) {
                stmt.setString(1, level);
                stmt.setString(2, lowerBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                stmt.setString(3, upperBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                System.out.println("SQL statement: " + stmt);
                ResultSet rs = stmt.executeQuery();
                counts.put(level.toLowerCase(), rs.next() ? rs.getInt("cnt") : 0);
                System.out.println(level + " = " + counts.get(level.toLowerCase()));
            }

            // total
            String sqlTotal = ""
                    + "SELECT COUNT(*) AS cnt "
                    + "FROM impression_log "
                    + "WHERE impression_date BETWEEN ? AND ?";
            try (PreparedStatement totalStmt = conn.prepareStatement(sqlTotal)) {
                totalStmt.setString(1, lowerBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                totalStmt.setString(2, upperBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
                System.out.println("SQL statement: " + totalStmt);
                ResultSet rsTotal = totalStmt.executeQuery();
                counts.put("total", rsTotal.next() ? rsTotal.getInt("cnt") : 0);
                System.out.println("Total = " + counts.get("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    /**
     * fetch impression count (no specified timeframe)
     * @param groupingGranularity
     * @param tickIndex
     * @return
     * @throws SQLException
     */
    public HashMap<String, Integer> fetchImpressionCounts(String groupingGranularity, int tickIndex) throws SQLException {
        return fetchImpressionCounts(groupingGranularity, tickIndex, 0);
    }

    /**
     * fetch impression counts
     * @param groupingGranularity
     * @param tickIndex
     * @param offset
     * @return
     * @throws SQLException
     */
    public HashMap<String, Integer> fetchImpressionCounts(String groupingGranularity, int tickIndex, int offset) throws SQLException {
        Timestamp latestTimestamp = getLatestImpressionTimestamp();
        if (latestTimestamp == null) {
            return new HashMap<>();
        }

        LocalDateTime latestLdt = latestTimestamp.toLocalDateTime();
        Boundary boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);
        return fetchImpressionCounts(boundaries.getStart(), boundaries.getEnd(), tickIndex);
    }

    /**
     * fetch impression count with custom date range
     * @param lowerBound
     * @param upperBound
     * @param tickIndex
     * @return
     * @throws SQLException
     */
    public HashMap<String, Integer> fetchImpressionCounts(LocalDateTime lowerBound, LocalDateTime upperBound, int tickIndex) throws SQLException {
        HashMap<String, Integer> counts = new HashMap<>();
        int newTick = (tickIndex >= 0) ? tickIndex : resolveTickIndex(lowerBound, upperBound);
        TickInfo tickInfo = getTickInfo(newTick);

        StringBuilder sql = new StringBuilder("SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count " +
                "FROM impression_log i WHERE 1=1");
        String query = addFilters(sql);
        System.out.println("SQL statement: " + query);
        System.out.println("Boundaries: " + lowerBound + " to " + upperBound);

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            //stmt.setString(1, lowerBound.truncatedTo(ChronoUnit.SECONDS).format(formatter));
            //stmt.setString(2, upperBound.truncatedTo(ChronoUnit.SECONDS).format(formatter));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String tick = rs.getString("tick");
                int countVal = rs.getInt("count");
                counts.put(tick, countVal);
                System.out.println("tick: " + tick + " count: " + countVal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return generateFullSeries(counts, lowerBound, upperBound, newTick, tickInfo.getTickPattern());
    }

    /**
     * get timestamp of the latest impression in impression_log
     * @return
     */
    private Timestamp getLatestImpressionTimestamp() {
        String maxDateSql = "SELECT MAX(impression_date) AS maxDate FROM impression_log";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(maxDateSql)) {
            if (rs.next()) {
                return rs.getTimestamp("maxDate");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * compute date boundaries
     * @param latestLdt
     * @param groupingGranularity
     * @param offset
     * @return
     */
    private Boundary computeBoundaries(LocalDateTime latestLdt, String groupingGranularity, int offset) {
        LocalDateTime startBoundary;
        LocalDateTime endBoundary;
        switch (groupingGranularity.toLowerCase()) {
            case "hour":
            case "hourly": {
                LocalDateTime base = latestLdt.truncatedTo(ChronoUnit.HOURS).plusHours(offset);
                startBoundary = base;
                endBoundary = base.plusHours(1).minusNanos(1);
                break;
            }
            case "day":
            case "daily": {
                LocalDate baseDate = latestLdt.toLocalDate().plusDays(offset);
                startBoundary = baseDate.atStartOfDay();
                endBoundary = startBoundary.plusDays(1).minusNanos(1);
                break;
            }
            case "week":
            case "weekly": {
                LocalDate latestDate = latestLdt.toLocalDate();
                // Assume week starts on Monday.
                LocalDate baseStartOfWeek = latestDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(offset);
                startBoundary = baseStartOfWeek.atStartOfDay();
                LocalDate endOfWeek = baseStartOfWeek.plusDays(6);
                endBoundary = endOfWeek.atTime(LocalTime.MAX);
                break;
            }
            case "month":
            case "monthly": {
                LocalDate latestDate = latestLdt.toLocalDate();
                YearMonth baseYm = YearMonth.from(latestDate).plusMonths(offset);
                startBoundary = baseYm.atDay(1).atStartOfDay();
                endBoundary = baseYm.atEndOfMonth().atTime(LocalTime.MAX);
                break;
            }
            default:
                throw new IllegalArgumentException("Invalid grouping granularity: " + groupingGranularity);
        }
        return new Boundary(startBoundary, endBoundary);
    }

    /**
     * get appropriate tick pattern for custom date range
     * @param start
     * @param end
     * @return
     */
    private int resolveTickIndex(LocalDateTime start, LocalDateTime end) {
        int maxPoints = 50;
        long hours = ChronoUnit.HOURS.between(start, end) + 1;
        if (hours <= maxPoints) return 0;
        if (hours / 24 <= maxPoints) return 1;
        if (hours / (24 * 7) <= maxPoints) return 2;
        return 3;
    }

    /**
     * get tick info based on index
     * @param tickIndex
     * @return
     */
    private TickInfo getTickInfo(int tickIndex) {
        switch (tickIndex) {
            case 0: // hourly
                return new TickInfo("strftime('%Y-%m-%d %H:00:00', impression_date)", "yyyy-MM-dd HH:00:00");
            case 1: // daily
                return new TickInfo("strftime('%Y-%m-%d', impression_date)", "yyyy-MM-dd");
            case 2: // weekly
                return new TickInfo("strftime('%Y-W%W', impression_date)", "yyyy-'W'ww");
            case 3: // monthly
                return new TickInfo("strftime('%Y-%m', impression_date)", "yyyy-MM");
            default:
                throw new IllegalArgumentException("Invalid tick index: " + tickIndex);
        }
    }


    /**
     * Pad out dataset with 0s to ensure continuous dataset
     * @param counts
     * @param startBoundary
     * @param endBoundary
     * @param tickIndex
     * @param tickPattern
     * @return
     */
    private HashMap<String, Integer> generateFullSeries(HashMap<String, Integer> counts,
                                                        LocalDateTime startBoundary,
                                                        LocalDateTime endBoundary,
                                                        int tickIndex,
                                                        String tickPattern) {
        HashMap<String, Integer> fullCounts = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(tickPattern);
        if (tickIndex == 0) { // hourly
            LocalDateTime current = startBoundary;
            while (!current.isAfter(endBoundary)) {
                String tick = current.format(formatter);
                fullCounts.put(tick, counts.getOrDefault(tick, 0));
                current = current.plusHours(1);
            }
        } else if (tickIndex == 1) { // daily
            LocalDate startDate = startBoundary.toLocalDate();
            LocalDate endDate = endBoundary.toLocalDate();
            for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                String tick = d.format(formatter);
                fullCounts.put(tick, counts.getOrDefault(tick, 0));
            }
        } else if (tickIndex == 2) { // weekly
            LocalDate startDate = startBoundary.toLocalDate();
            DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("yyyy-'W'ww");
            String tick = startDate.format(weekFormatter);
            fullCounts.put(tick, counts.getOrDefault(tick, 0));
        } else if (tickIndex == 3) { // monthly
            LocalDate startDate = startBoundary.toLocalDate();
            String tick = startDate.format(formatter);
            fullCounts.put(tick, counts.getOrDefault(tick, 0));
        }
        return fullCounts;
    }

    /**
     * Add parameters from GraphFilters object
     * @param graphFilters
     * @throws SQLException
     */
    public void addParameters(GraphFilters graphFilters) throws SQLException {
        parameters.clear();
        System.out.println("Add Parameters");
        if (!graphFilters.getAge().equals("All")) parameters.put("age", graphFilters.getAge());
        if (!graphFilters.getGender().equals("All")) parameters.put("gender", graphFilters.getGender());
        if (!graphFilters.getIncome().equals("All")) parameters.put("income", graphFilters.getIncome());
        if (!graphFilters.getContext().equals("All")) parameters.put("context", graphFilters.getContext());
        if (!graphFilters.getEndDate().equals("End")) parameters.put("endDate", graphFilters.getEndDate());
        if (!graphFilters.getStartDate().equals("Start")) parameters.put("startDate", graphFilters.getStartDate());
    }

    /**
     * Get first column name from given table
     * @param tableName
     * @return
     * @throws SQLException
     */
    private String getFirstColumnName(String tableName) throws SQLException {
        String query = "PRAGMA table_info(" + tableName + ")";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getString("name");
            }
        }
        return null; // Return null if no column is found
    }

    /**
     * Add filters to query
     * @param query
     * @return
     * @throws SQLException
     */
    private String addFilters(StringBuilder query) throws SQLException {
        String firstColumnName = getFirstColumnName("impression_log");
        if (!parameters.containsValue("null")){
            query.append(" AND DATE(impression_date) BETWEEN DATE(").append(firstColumnName).append(") AND " +
                    "DATE(").append(firstColumnName).append(")");
        }
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            if (!"startDate".equalsIgnoreCase(key) && !"endDate".equalsIgnoreCase(key)) {
                query.append(" AND i.").append(key).append(" = '").append(parameters.get(key)).append("'");
            }

        }
        query.append(" GROUP BY tick");
        System.out.println(query);
        return query.toString();
    }

    private static class Boundary {
        private final LocalDateTime start;
        private final LocalDateTime end;

        public Boundary(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }
    }

    private static class TickInfo {
        private final String tickExpression;
        private final String tickPattern;

        public TickInfo(String tickExpression, String tickPattern) {
            this.tickExpression = tickExpression;
            this.tickPattern = tickPattern;
        }

        public String getTickExpression() {
            return tickExpression;
        }

        public String getTickPattern() {
            return tickPattern;
        }
    }


}
