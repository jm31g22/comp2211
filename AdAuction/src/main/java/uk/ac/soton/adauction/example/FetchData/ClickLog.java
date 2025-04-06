package uk.ac.soton.adauction.example.FetchData;

import uk.ac.soton.adauction.example.FetchData.Queriers.LocalQuerier;
import uk.ac.soton.adauction.example.FetchData.Queriers.RemoteQuerier;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;

public class ClickLog extends LocalQuerier {

    public HashMap<String, Integer> fetchClickCounts(String groupingGranularity, int tickIndex) {
        return fetchClickCounts(groupingGranularity, tickIndex, 0);
    }

    /**
     * fetch click counts in specified time window
     */
    public HashMap<String, Integer> fetchClickCounts(String groupingGranularity, int tickIndex, int offset) {
        HashMap<String, Integer> counts = new HashMap<>();

        // get the latest click timestamp.
        Timestamp latestTimestamp = getLatestClickTimestamp();
        if (latestTimestamp == null) {
            return counts;
        }
        LocalDateTime latestLdt = latestTimestamp.toLocalDateTime();

        // calculate boundaries based on groupingGranularity + offset.
        Boundary boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);
        LocalDateTime startBoundary = boundaries.getStart();
        LocalDateTime endBoundary = boundaries.getEnd();
        TickInfo tickInfo = getTickInfo(tickIndex);

        // run query
        String sql = "SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count " +
                "FROM click_log " +
                "WHERE click_date BETWEEN ? AND ? " +
                "GROUP BY tick";


        System.out.println("SQL statement: " + sql);
        System.out.println("Boundaries: " + startBoundary + " to " + endBoundary);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            stmt.setString(1, startBoundary.truncatedTo(ChronoUnit.SECONDS).format(formatter));
            stmt.setString(2, endBoundary.truncatedTo(ChronoUnit.SECONDS).format(formatter));
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

        // fill in anu missing data with 0
        return generateFullSeries(counts, startBoundary, endBoundary, tickIndex, tickInfo.getTickPattern());
    }


    public HashMap<String, Integer> fetchUniqueCounts(String groupingGranularity, int tickIndex) {
        return fetchUniqueCounts(groupingGranularity, tickIndex, 0);
    }

    /**
     * get unique clicks during a period of time
     */
    public HashMap<String, Integer> fetchUniqueCounts(String groupingGranularity, int tickIndex, int offset) {
        HashMap<String, Integer> counts = new HashMap<>();

        // latest click date
        Timestamp latestTimestamp = getLatestClickTimestamp();
        if (latestTimestamp == null) {
            return counts;
        }
        LocalDateTime latestLdt = latestTimestamp.toLocalDateTime();

        // calculate boundaries
        Boundary boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);
        LocalDateTime startBoundary = boundaries.getStart();
        LocalDateTime endBoundary = boundaries.getEnd();
        TickInfo tickInfo = getTickInfo(tickIndex);

        //sql
        String query = "WITH first_date AS (" +
                "   SELECT id, MIN(click_date) AS earliest_click " +
                "   FROM click_log " +
                "   GROUP BY id" +
                ") " +
                "SELECT " + tickInfo.getTickExpression().replace("click_date", "earliest_click") + " AS tick, COUNT(*) AS count " +
                "FROM first_date " +
                "WHERE earliest_click BETWEEN ? AND ? " +
                "GROUP BY tick";


        System.out.println("SQL statement: " + query);
        System.out.println("Boundaries (unique): " + startBoundary + " to " + endBoundary);

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            stmt.setString(1, startBoundary.truncatedTo(ChronoUnit.SECONDS).format(formatter));
            stmt.setString(2, endBoundary.truncatedTo(ChronoUnit.SECONDS).format(formatter));

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

        return generateFullSeries(counts, startBoundary, endBoundary, tickIndex, tickInfo.getTickPattern());
    }

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

    /**
     * return the SQL date-format expression and the java date pattern given an index, 0=hour, 1=day, 2=week, 3=month
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
