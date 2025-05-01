package uk.ac.soton.adauction.example.FetchData;

import uk.ac.soton.adauction.example.FetchData.Queriers.LocalQuerier;
import uk.ac.soton.adauction.example.FetchData.Queriers.RemoteQuerier;
import uk.ac.soton.adauction.example.Utils.GraphFilters;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClickLog extends LocalQuerier {
    private HashMap<String, String> parameters = new HashMap<>();
    public HashMap<String, Integer> fetchClickCounts(String groupingGranularity, int tickIndex) throws SQLException {
        return fetchClickCounts(groupingGranularity, tickIndex, 0);
    }

    /**
     * fetch click counts in specified time window
     * @param groupingGranularity
     * @param tickIndex
     * @param offset
     * @return
     */
    public HashMap<String, Integer> fetchClickCounts(String groupingGranularity, int tickIndex, int offset) throws SQLException {
        Timestamp latestTimestamp = getLatestClickTimestamp();
        if (latestTimestamp == null) return new HashMap<>();
        LocalDateTime latestLdt = latestTimestamp.toLocalDateTime();
        Boundary      boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);

        return fetchClickCountsInternal(boundaries.getStart(),
                boundaries.getEnd(),
                tickIndex);
    }

    public HashMap<String, Integer> fetchClickCounts(LocalDateTime lowerDateTime, LocalDateTime upperDateTime, int tickIndex) throws SQLException {
        if (lowerDateTime.isAfter(upperDateTime)) {
            throw new IllegalArgumentException("lowerDateTime must be before upperDateTime");
        }
        return fetchClickCountsInternal(lowerDateTime, upperDateTime, tickIndex);
    }

    private HashMap<String, Integer> fetchClickCountsInternal(LocalDateTime startBoundary, LocalDateTime endBoundary, int tickIndex) throws SQLException {

        HashMap<String, Integer> counts   = new HashMap<>();
        TickInfo                 tickInfo = getTickInfo(tickIndex);

        StringBuilder sql = new StringBuilder("SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count " +
                "FROM click_log c JOIN unique_users i ON c.id = i.id WHERE 1=1");
        String query = addFilters(sql);
        System.out.println("SQL statement: " + query);

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
            e.printStackTrace();            // (or use a logger)
        }

        // pad out missing buckets with zeros so the chart has a continuous series
        return generateFullSeries(counts,
                startBoundary,
                endBoundary,
                tickIndex,
                tickInfo.getTickPattern());
    }

    /**
     *
     * @param groupingGranularity
     * @param tickIndex
     * @return
     */
    public HashMap<String, Integer> fetchUniqueCounts(String groupingGranularity, int tickIndex) throws SQLException {
        return fetchUniqueCounts(groupingGranularity, tickIndex, 0);
    }

    /**
     * get unique clicks during a period of time
     * @param groupingGranularity
     * @param tickIndex
     * @param offset
     * @return
     */
    public HashMap<String, Integer> fetchUniqueCounts(String groupingGranularity, int tickIndex, int offset) throws SQLException {
        // compute latest and boundaries as before
        Timestamp latestTimestamp = getLatestClickTimestamp();
        if (latestTimestamp == null) {
            return new HashMap<>();
        }
        LocalDateTime latestLdt = latestTimestamp.toLocalDateTime();
        Boundary boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);

        // delegate to the new overload
        return fetchUniqueCounts(boundaries.getStart(), boundaries.getEnd(), tickIndex);
    }

    public HashMap<String, Integer> fetchUniqueCounts(LocalDateTime lowerBound, LocalDateTime upperBound, int tickIndex) throws SQLException {
        HashMap<String, Integer> counts = new HashMap<>();
        int newTick = (tickIndex >= 0) ? tickIndex : resolveTickIndex(lowerBound, upperBound);
        TickInfo tickInfo = getTickInfo(newTick);

        StringBuilder sql = new StringBuilder("WITH first_date AS (" +
                "   SELECT id, MIN(click_date) AS click_date " +
                "   FROM click_log " +
                "   GROUP BY id" +
                ") " +
                "SELECT " +
                tickInfo.getTickExpression() +
                " AS tick, COUNT(*) AS count " +
                "FROM first_date c JOIN unique_users i ON c.id = i.id WHERE 1=1");
        String query = addFilters(sql);
        System.out.println("SQL statement: " + query);
        System.out.println("Boundaries (unique): " + lowerBound + " to " + upperBound);

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            //stmt.setString(1, lowerBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));
            //stmt.setString(2, upperBound.truncatedTo(ChronoUnit.SECONDS).format(fmt));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String tick = rs.getString("tick");
                int countVal = rs.getInt("count");
                counts.put(tick, countVal);
                System.out.println("tick: " + tick + " unique count: " + countVal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return generateFullSeries(counts, lowerBound, upperBound, tickIndex, tickInfo.getTickPattern());
    }


    public double[] getHistogramData(){
        String query = "SELECT click_cost AS cost FROM click_log";
        ArrayList<Double> values = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                double cost = rs.getInt("cost");
                values.add(cost);
                System.out.println("cost: " + cost);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values.stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * Get the timestamp of the latest click in log
     * @return
     */
    private Timestamp getLatestClickTimestamp() {
        String sql = "SELECT MAX(click_date) AS maxDate FROM click_log";
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
     * Compute date boundaries for SQL query of click log
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
                // Assume a Monday-based week
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

    private int resolveTickIndex(LocalDateTime start, LocalDateTime end) {
        int maxPoints = 50;
        long hours = ChronoUnit.HOURS.between(start, end) + 1;
        if (hours <= maxPoints) return 0;
        if (hours / 24 <= maxPoints) return 1;
        if (hours / (24 * 7) <= maxPoints) return 2;
        return 3;
    }

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

    private String addFilters(StringBuilder query) throws SQLException {
        String firstColumnName = getFirstColumnName("click_log");
        if (!parameters.containsValue("null")){
            query.append(" AND DATE(click_date) BETWEEN DATE(").append(firstColumnName).append(") AND " +
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

    /**
     * return the SQL date-format expression and the java date pattern given an index
     * 0=hour, 1=day, 2=week, 3=month
     * @param tickIndex
     * @return
     */
    private TickInfo getTickInfo(int tickIndex) {
        switch (tickIndex) {
            case 0: // hour
                return new TickInfo("strftime('%Y-%m-%d %H:00:00', click_date)", "yyyy-MM-dd HH:00:00");
            case 1: // day
                return new TickInfo("strftime('%Y-%m-%d', click_date)", "yyyy-MM-dd");
            case 2: // week
                return new TickInfo("strftime('%Y-W%W', click_date)", "yyyy-'W'ww");
            case 3: // month
                return new TickInfo("strftime('%Y-%m', click_date)", "yyyy-MM");
            default:
                throw new IllegalArgumentException("Invalid tick index: " + tickIndex);
        }
    }


    /**
     * ensure continuous x axis
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
            // Hourly
            LocalDateTime current = startBoundary;
            while (!current.isAfter(endBoundary)) {
                String tick = current.format(formatter);
                fullCounts.put(tick, counts.getOrDefault(tick, 0));
                current = current.plusHours(1);
            }
        } else if (tickIndex == 1) {
            // Daily
            LocalDate startDate = startBoundary.toLocalDate();
            LocalDate endDate = endBoundary.toLocalDate();
            for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                String tick = d.format(formatter);
                fullCounts.put(tick, counts.getOrDefault(tick, 0));
            }
        } else if (tickIndex == 2) {
            // Weekly (we assume a single “bucket” for that week)
            LocalDate startDate = startBoundary.toLocalDate();
            String tick = startDate.format(formatter); // e.g. "2025-W09"
            fullCounts.put(tick, counts.getOrDefault(tick, 0));
        } else if (tickIndex == 3) {
            // Monthly (similarly a single bucket)
            LocalDate startDate = startBoundary.toLocalDate();
            String tick = startDate.format(formatter); // e.g. "2025-03"
            fullCounts.put(tick, counts.getOrDefault(tick, 0));
        }

        return fullCounts;
    }

    //helper boundary class
    private static class Boundary {
        private final LocalDateTime start;
        private final LocalDateTime end;

        public Boundary(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }
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
