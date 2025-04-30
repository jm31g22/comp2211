package uk.ac.soton.adauction.example.FetchData;

import uk.ac.soton.adauction.example.FetchData.Queriers.LocalQuerier;
import uk.ac.soton.adauction.example.FetchData.Queriers.RemoteQuerier;


import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class to obtain data from the impression log
 */
public class ImpressionLog extends LocalQuerier {

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
    // ---------------------------------------------
// GENDER
// ---------------------------------------------
    public HashMap<String, Integer> fetchImpressionGenderCount() {
        // default to “everything up to now” – you can change this if you prefer a different default window
        return fetchImpressionGenderCount(LocalDateTime.MIN, LocalDateTime.now());
    }

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


    // ---------------------------------------------
// AGE
// ---------------------------------------------
    public HashMap<String, Integer> fetchImpressionAgeCount() {
        return fetchImpressionAgeCount(LocalDateTime.MIN, LocalDateTime.now());
    }

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


    // ---------------------------------------------
// INCOME
// ---------------------------------------------
    public HashMap<String, Integer> fetchImpressionIncomeCount() {
        return fetchImpressionIncomeCount(LocalDateTime.MIN, LocalDateTime.now());
    }

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


    public HashMap<String, Integer> fetchImpressionCounts(String groupingGranularity, int tickIndex) {
        return fetchImpressionCounts(groupingGranularity, tickIndex, 0);
    }


    public HashMap<String, Integer> fetchImpressionCounts(String groupingGranularity, int tickIndex, int offset) {
        Timestamp latestTimestamp = getLatestImpressionTimestamp();
        if (latestTimestamp == null) {
            return new HashMap<>();
        }

        LocalDateTime latestLdt = latestTimestamp.toLocalDateTime();
        Boundary boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);
        return fetchImpressionCounts(boundaries.getStart(), boundaries.getEnd(), tickIndex);
    }

    public HashMap<String, Integer> fetchImpressionCounts(LocalDateTime lowerBound, LocalDateTime upperBound, int tickIndex) {
        HashMap<String, Integer> counts = new HashMap<>();
        int newTick = (tickIndex >= 0) ? tickIndex : resolveTickIndex(lowerBound, upperBound);
        TickInfo tickInfo = getTickInfo(newTick);

        String sql = "SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count " +
                "FROM impression_log " +
                "WHERE impression_date BETWEEN ? AND ? " +
                "GROUP BY tick";
        System.out.println("SQL statement: " + sql);
        System.out.println("Boundaries: " + lowerBound + " to " + upperBound);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            stmt.setString(1, lowerBound.truncatedTo(ChronoUnit.SECONDS).format(formatter));
            stmt.setString(2, upperBound.truncatedTo(ChronoUnit.SECONDS).format(formatter));

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

    //compute boundaries
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

    private int resolveTickIndex(LocalDateTime start, LocalDateTime end) {
        int maxPoints = 50;
        long hours = ChronoUnit.HOURS.between(start, end) + 1;
        if (hours <= maxPoints) return 0;
        if (hours / 24 <= maxPoints) return 1;
        if (hours / (24 * 7) <= maxPoints) return 2;
        return 3;
    }

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


    // ensure continuous x axis
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
