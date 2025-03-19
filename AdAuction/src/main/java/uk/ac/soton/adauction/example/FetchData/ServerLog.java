package uk.ac.soton.adauction.example.FetchData;

import uk.ac.soton.adauction.example.FetchData.Queriers.LocalQuerier;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.HashMap;

public class ServerLog extends LocalQuerier {


    /**
     * Overload without offset for conversions.
     */
    public HashMap<String, Integer> fetchConversionCounts(String groupingGranularity, int tickIndex) {
        return fetchConversionCounts(groupingGranularity, tickIndex, 0);
    }

    /**
     * Unified method for “conversion” data grouped by hour/day/week/month within a chosen offset window.
     */
    public HashMap<String, Integer> fetchConversionCounts(String groupingGranularity, int tickIndex, int offset) {
        HashMap<String, Integer> counts = new HashMap<>();

        // latest entry date
        Timestamp latest = getLatestEntryTimestamp();
        if (latest == null) {
            return counts;
        }
        LocalDateTime latestLdt = latest.toLocalDateTime();

        // calculate boundaries
        Boundary boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);
        LocalDateTime startBoundary = boundaries.getStart();
        LocalDateTime endBoundary = boundaries.getEnd();
        TickInfo tickInfo = getTickInfo(tickIndex);

        String sql = "SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count "
                + "FROM server_log "
                + "WHERE conversion = 'Yes' "
                + "  AND entry_date BETWEEN ? AND ? "
                + "GROUP BY " + tickInfo.getTickExpression();

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
                System.out.println("Conversion tick: " + tick + " => " + countVal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // continuous x axis
        return generateFullSeries(counts, startBoundary, endBoundary, tickIndex, tickInfo.getTickPattern());
    }

    public HashMap<String, Integer> fetchBounceCounts(String groupingGranularity, int tickIndex) {
        return fetchBounceCounts(groupingGranularity, tickIndex, 0);
    }

    //fetch bounce data in time window
    public HashMap<String, Integer> fetchBounceCounts(String groupingGranularity, int tickIndex, int offset) {
        HashMap<String, Integer> counts = new HashMap<>();

        // latest entry date
        Timestamp latest = getLatestEntryTimestamp();
        if (latest == null) {
            return counts;
        }
        LocalDateTime latestLdt = latest.toLocalDateTime();

        // boundaries
        Boundary boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);
        LocalDateTime startBoundary = boundaries.getStart();
        LocalDateTime endBoundary = boundaries.getEnd();
        TickInfo tickInfo = getTickInfo(tickIndex);

        // bounces - needs updating
        String sql = "SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count "
                + "FROM server_log "
                + "WHERE (TIMEDIFF(exit_date, entry_date) <= TIME('00:00:10') OR pages_viewed = 1) "
                + "  AND entry_date BETWEEN ? AND ? "
                + "GROUP BY " + tickInfo.getTickExpression();

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
                System.out.println("Bounce tick: " + tick + " => " + countVal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // fill missing intervals
        return generateFullSeries(counts, startBoundary, endBoundary, tickIndex, tickInfo.getTickPattern());
    }

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

    private TickInfo getTickInfo(int tickIndex) {
        switch (tickIndex) {
            case 0: // hour
                return new TickInfo("DATE_FORMAT(entry_date, '%Y-%m-%d %H:00:00')", "yyyy-MM-dd HH:00:00");
            case 1: // day
                return new TickInfo("DATE_FORMAT(entry_date, '%Y-%m-%d')", "yyyy-MM-dd");
            case 2: // week
                return new TickInfo(
                        "CONCAT(YEAR(entry_date), '-W', LPAD(WEEK(entry_date,1), 2, '0'))",
                        "yyyy-'W'ww"
                );
            case 3: // month
                return new TickInfo("DATE_FORMAT(entry_date, '%Y-%m')", "yyyy-MM");
            default:
                throw new IllegalArgumentException("Invalid tick index: " + tickIndex);
        }
    }

    //ensure continuous x axis
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
