package uk.ac.soton.adauction.example.FetchData;

import uk.ac.soton.adauction.example.FetchData.Queriers.LocalQuerier;
import uk.ac.soton.adauction.example.FetchData.Queriers.RemoteQuerier;

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

    public HashMap<String, Integer> fetchConversionCounts(String groupingGranularity, int tickIndex, int offset) {
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

    public HashMap<String, Integer> fetchConversionCounts(LocalDateTime lowerDateTime, LocalDateTime upperDateTime, int tickIndex) {
        if (lowerDateTime.isAfter(upperDateTime)) {
            throw new IllegalArgumentException("lowerDateTime must be before upperDateTime");
        }
        int newTick = (tickIndex >= 0) ? tickIndex : resolveTickIndex(lowerDateTime, upperDateTime);
        TickInfo tickInfo = getTickInfo(newTick);
        return fetchConversionCountsInternal(lowerDateTime, upperDateTime, newTick);
    }

    private HashMap<String, Integer> fetchConversionCountsInternal(LocalDateTime startBoundary, LocalDateTime endBoundary, int tickIndex) {
        HashMap<String, Integer> counts   = new HashMap<>();
        TickInfo tickInfo = getTickInfo(tickIndex);

        String sql =
                "SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count " +
                        "FROM server_log " +
                        "WHERE conversion = 'Yes' " +
                        "  AND entry_date BETWEEN ? AND ? " +
                        "GROUP BY tick";

        System.out.println("SQL statement: " + sql);
        System.out.println("Boundaries: " + startBoundary + " to " + endBoundary);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startBoundary));
            stmt.setTimestamp(2, Timestamp.valueOf(endBoundary));

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


    public HashMap<String, Integer> fetchBounceCounts(String groupingGranularity, int tickIndex) {
        return fetchBounceCounts(groupingGranularity, tickIndex, 0);
    }

    public HashMap<String, Integer> fetchBounceCounts(String groupingGranularity, int tickIndex, int offset) {
        Timestamp latest = getLatestEntryTimestamp();
        if (latest == null) return new HashMap<>();

        LocalDateTime latestLdt  = latest.toLocalDateTime();
        Boundary      boundaries = computeBoundaries(latestLdt, groupingGranularity, offset);

        return fetchBounceCountsInternal(boundaries.getStart(),
                boundaries.getEnd(),
                tickIndex);
    }

    public HashMap<String, Integer> fetchBounceCounts(LocalDateTime lowerDateTime, LocalDateTime upperDateTime, int tickIndex) {
        if (lowerDateTime.isAfter(upperDateTime)) {
            throw new IllegalArgumentException("lowerDateTime must be before upperDateTime");
        }
        return fetchBounceCountsInternal(lowerDateTime, upperDateTime, tickIndex);
    }

    private HashMap<String, Integer> fetchBounceCountsInternal(LocalDateTime startBoundary, LocalDateTime endBoundary, int tickIndex) {

        HashMap<String, Integer> counts   = new HashMap<>();
        TickInfo tickInfo = getTickInfo(tickIndex);

        String sql =
                "SELECT " + tickInfo.getTickExpression() + " AS tick, COUNT(*) AS count " +
                        "FROM server_log " +
                        "WHERE ((strftime('%s', exit_date) - strftime('%s', entry_date)) <= 10 " +
                        "       OR pages_viewed = 1) " +
                        "  AND entry_date BETWEEN ? AND ? " +
                        "GROUP BY " + tickInfo.getTickExpression();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startBoundary.truncatedTo(ChronoUnit.SECONDS).format(fmt));
            stmt.setString(2, endBoundary  .truncatedTo(ChronoUnit.SECONDS).format(fmt));

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
