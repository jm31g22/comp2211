package uk.ac.soton.adauction.example.FetchData;

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
public class ImpressionLog extends DatabaseConnection{
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
        HashMap<String, Integer> counts = new HashMap<>();
        Integer male = 0;
        Integer female = 0;
        Integer total = 0;
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "SELECT gender FROM impression_log";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String gender = rs.getString("gender");
                if (gender.equals("Male")) {
                    male++;
                } else {
                    female++;
                }
                total++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        counts.put("male", male);
        System.out.println("Number of male: " + male);
        counts.put("female", female);
        System.out.println("Number of female: " + female);
        counts.put("total", total);
        System.out.println("Total amount of record: " + total);
        return counts;
    }

    public HashMap<String, Integer> fetchImpressionCounts(String groupingGranularity, int tickIndex) {
        return fetchImpressionCounts(groupingGranularity, tickIndex, 0);
    }

    public HashMap<String, Integer> fetchImpressionCounts(String groupingGranularity, int tickIndex, int offset) {
        HashMap<String, Integer> counts = new HashMap<>();

        Timestamp latestTimestamp = getLatestImpressionTimestamp();
        if (latestTimestamp == null) {
            return counts;
        }

        LocalDateTime latestLdt = latestTimestamp.toLocalDateTime();

        Boundary boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);
        LocalDateTime startBoundary = boundaries.getStart();
        LocalDateTime endBoundary = boundaries.getEnd();
        TickInfo tickInfo = getTickInfo(tickIndex);

        // execute query
        String sql = "SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count " +
                "FROM impression_log " +
                "WHERE impression_date BETWEEN ? AND ? " +
                "GROUP BY " + tickInfo.getTickExpression();
        System.out.println("SQL statement: " + sql);
        System.out.println("Boundaries: " + startBoundary + " to " + endBoundary);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startBoundary));
            stmt.setTimestamp(2, Timestamp.valueOf(endBoundary));
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

        // continuous x axis
        return generateFullSeries(counts, startBoundary, endBoundary, tickIndex, tickInfo.getTickPattern());
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

    private TickInfo getTickInfo(int tickIndex) {
        switch (tickIndex) {
            case 0:
                return new TickInfo("DATE_FORMAT(impression_date, '%Y-%m-%d %H:00:00')", "yyyy-MM-dd HH:00:00");
            case 1:
                return new TickInfo("DATE_FORMAT(impression_date, '%Y-%m-%d')", "yyyy-MM-dd");
            case 2:
                return new TickInfo("CONCAT(YEAR(impression_date), '-W', LPAD(WEEK(impression_date, 1), 2, '0'))", "YYYY-'W'ww");
            case 3:
                return new TickInfo("DATE_FORMAT(impression_date, '%Y-%m')", "yyyy-MM");
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
