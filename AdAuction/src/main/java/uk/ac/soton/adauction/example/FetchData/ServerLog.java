package uk.ac.soton.adauction.example.FetchData;

import uk.ac.soton.adauction.example.App;
import uk.ac.soton.adauction.example.FetchData.Queriers.LocalQuerier;
import uk.ac.soton.adauction.example.FetchData.Queriers.RemoteQuerier;
import uk.ac.soton.adauction.example.Utils.GraphFilters;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.HashMap;
import java.util.Map;

public class ServerLog extends LocalQuerier {
    private HashMap<String, String> parameters = new HashMap<>();

    /**
     * Overload without offset for conversions.
     */
    public HashMap<String, Integer> fetchConversionCounts(String groupingGranularity, int tickIndex) throws SQLException {
        return fetchConversionCounts(groupingGranularity, tickIndex, 0);
    }

    /**
     * fetch conversion counts
     * @param groupingGranularity
     * @param tickIndex
     * @param offset
     * @return
     * @throws SQLException
     */
    public HashMap<String, Integer> fetchConversionCounts(String groupingGranularity, int tickIndex, int offset) throws SQLException {
        Timestamp latest = getLatestEntryTimestamp();
        if (latest == null) {
            return new HashMap<>();
        }

        LocalDateTime latestLdt  = latest.toLocalDateTime();
        Boundary boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);

        return fetchConversionCountsInternal(boundaries.getStart(),
                boundaries.getEnd(),
                tickIndex);
    }

    /**
     * fetch conversion counts with custom date range
     * @param lowerDateTime
     * @param upperDateTime
     * @param tickIndex
     * @return
     * @throws SQLException
     */
    public HashMap<String, Integer> fetchConversionCounts(LocalDateTime lowerDateTime, LocalDateTime upperDateTime, int tickIndex) throws SQLException {
        if (lowerDateTime.isAfter(upperDateTime)) {
            throw new IllegalArgumentException("lowerDateTime must be before upperDateTime");
        }
        int newTick = (tickIndex >= 0) ? tickIndex : resolveTickIndex(lowerDateTime, upperDateTime);
        TickInfo tickInfo = getTickInfo(newTick);
        return fetchConversionCountsInternal(lowerDateTime, upperDateTime, newTick);
    }

    /**
     * internal helper to fetch counts (regardless if custom date range)
     * @param startBoundary
     * @param endBoundary
     * @param tickIndex
     * @return
     * @throws SQLException
     */
    private HashMap<String, Integer> fetchConversionCountsInternal(LocalDateTime startBoundary, LocalDateTime endBoundary, int tickIndex) throws SQLException {
        HashMap<String, Integer> counts   = new HashMap<>();
        TickInfo tickInfo = getTickInfo(tickIndex);

        StringBuilder sql = new StringBuilder("SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count " +
                "FROM server_log s JOIN unique_users i ON s.id = i.id WHERE conversion = 'Yes' ");
        String query = addFilters(sql);

        System.out.println("SQL statement: " + sql);
        System.out.println("Boundaries: " + startBoundary + " to " + endBoundary);

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //stmt.setTimestamp(1, Timestamp.valueOf(startBoundary));
            //stmt.setTimestamp(2, Timestamp.valueOf(endBoundary));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("tick"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ensure continuous x-axis
        return generateFullSeries(counts,
                startBoundary,
                endBoundary,
                tickIndex,
                tickInfo.getTickPattern());
    }


    public HashMap<String, Integer> fetchBounceCounts(String groupingGranularity, int tickIndex) throws SQLException {
        return fetchBounceCounts(groupingGranularity, tickIndex, 0);
    }

    /**
     * fetch bounce count
     * @param groupingGranularity
     * @param tickIndex
     * @param offset
     * @return
     * @throws SQLException
     */
    public HashMap<String, Integer> fetchBounceCounts(String groupingGranularity, int tickIndex, int offset) throws SQLException {
        Timestamp latest = getLatestEntryTimestamp();
        if (latest == null) return new HashMap<>();

        LocalDateTime latestLdt  = latest.toLocalDateTime();
        Boundary      boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);

        return fetchBounceCountsInternal(boundaries.getStart(),
                boundaries.getEnd(),
                tickIndex);
    }

    /**
     * fetch bounce counts with custom date range
     * @param lowerDateTime
     * @param upperDateTime
     * @param tickIndex
     * @return
     * @throws SQLException
     */
    public HashMap<String, Integer> fetchBounceCounts(LocalDateTime lowerDateTime, LocalDateTime upperDateTime, int tickIndex) throws SQLException {
        if (lowerDateTime.isAfter(upperDateTime)) {
            throw new IllegalArgumentException("lowerDateTime must be before upperDateTime");
        }
        return fetchBounceCountsInternal(lowerDateTime, upperDateTime, tickIndex);
    }

    /**
     * internal helper to fetch bounce counts regardless of custom date
     * @param startBoundary
     * @param endBoundary
     * @param tickIndex
     * @return
     * @throws SQLException
     */
    private HashMap<String, Integer> fetchBounceCountsInternal(LocalDateTime startBoundary, LocalDateTime endBoundary, int tickIndex) throws SQLException {
        HashMap<String, Integer> counts   = new HashMap<>();
        TickInfo tickInfo = getTickInfo(tickIndex);
        String query;
        String definition = App.getAppState().getBounceDefinitionBinding().get();
        int value = App.getAppState().getBounceDefinitionNumberBinding().get();
        if (definition.equalsIgnoreCase("Pages")){
            StringBuilder sql = new StringBuilder("SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count " +
            "FROM server_log s JOIN unique_users i ON s.id = i.id WHERE pages_viewed = " + value);
            query = addFilters(sql);
        }else{
            StringBuilder sql = new StringBuilder("SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count " +
                    "FROM server_log s JOIN unique_users i ON s.id = i.id WHERE " +
                    "((strftime('%s', exit_date) - strftime('%s', entry_date)) <= " + value);
            query = addFilters(sql);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //stmt.setString(1, startBoundary.truncatedTo(ChronoUnit.SECONDS).format(fmt));
            //stmt.setString(2, endBoundary  .truncatedTo(ChronoUnit.SECONDS).format(fmt));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("tick"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // pad missing buckets so the line chart is continuous
        return generateFullSeries(counts,
                startBoundary,
                endBoundary,
                tickIndex,
                tickInfo.getTickPattern());
    }

    /**
     * Get latest entry date from server log
     * @return
     */
    private Timestamp getLatestEntryTimestamp() {
        String sql = "SELECT MAX(entry_date) AS maxDate FROM server_log";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
                LocalDate baseStartOfWeek = latestDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .plusWeeks(offset);
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
     * calculate sensible tick index based on custom dates
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
     * get tick info based in index
     * @param tickIndex
     * @return
     */
    private TickInfo getTickInfo(int tickIndex) {
        switch (tickIndex) {
            case 0: // hourly
                return new TickInfo("strftime('%Y-%m-%d %H:00:00', entry_date)", "yyyy-MM-dd HH:00:00");
            case 1: // daily
                return new TickInfo("strftime('%Y-%m-%d', entry_date)", "yyyy-MM-dd");
            case 2: // weekly
                return new TickInfo("strftime('%Y-W%W', entry_date)", "yyyy-'W'ww");
            case 3: // monthly
                return new TickInfo("strftime('%Y-%m', entry_date)", "yyyy-MM");
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

        if (tickIndex == 0) {
            // hourly
            LocalDateTime current = startBoundary;
            while (!current.isAfter(endBoundary)) {
                String tick = current.format(formatter);
                fullCounts.put(tick, counts.getOrDefault(tick, 0));
                current = current.plusHours(1);
            }
        } else if (tickIndex == 1) {
            // daily
            LocalDate startDate = startBoundary.toLocalDate();
            LocalDate endDate = endBoundary.toLocalDate();
            for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                String tick = d.format(formatter);
                fullCounts.put(tick, counts.getOrDefault(tick, 0));
            }
        } else if (tickIndex == 2) {
            // weekly
            String tick = startBoundary.toLocalDate().format(formatter);
            fullCounts.put(tick, counts.getOrDefault(tick, 0));
        } else if (tickIndex == 3) {
            // monthly
            String tick = startBoundary.toLocalDate().format(formatter);
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
                return rs.getString("name"); // The "name" column contains the column name
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
        String firstColumnName = getFirstColumnName("server_log");
        if (!parameters.containsValue("null")){
            query.append(" AND DATE(entry_date) BETWEEN DATE(").append(firstColumnName).append(") AND " +
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

    // helper classes
    private static class Boundary {
        private final LocalDateTime start;
        private final LocalDateTime end;
        public Boundary(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd()   { return end;   }
    }

    private static class TickInfo {
        private final String tickExpression;
        private final String tickPattern;
        public TickInfo(String tickExpression, String tickPattern) {
            this.tickExpression = tickExpression;
            this.tickPattern = tickPattern;
        }
        public String getTickExpression() { return tickExpression; }
        public String getTickPattern()    { return tickPattern;    }
    }
}
