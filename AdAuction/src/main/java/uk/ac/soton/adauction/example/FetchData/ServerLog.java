package uk.ac.soton.adauction.example.FetchData;

import uk.ac.soton.adauction.example.FetchData.Queriers.LocalQuerier;

import java.sql.*;
import java.util.HashMap;

public class ServerLog extends LocalQuerier {



    public HashMap<String, Integer> fetchConversionDateCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select date(server_log.entry_date) as date, count(*) as count from server_log where conversion = 'Yes' group by date";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String date = rs.getDate("date").toString();
                int count = rs.getInt("count");
                counts.put(date,count);
                System.out.println("Conversion date: " + date + " Count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchConversionHourCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement()
        ) {
            String strSelect = "select strftime('%Y-%m-%d %H:00:00', server_log.entry_date)\n as time, count(*) as count from server_log where conversion = 'Yes' group by time";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = rs.getTimestamp("time").toString();
                int count = rs.getInt("count");
                counts.put(time,count);
                System.out.println("Conversion time: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchConversionWeekCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select extract(week from server_log.entry_date) as week, count(*) as count from server_log where conversion = 'Yes' group by week";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = String.valueOf(rs.getLong("week"));
                Integer count = rs.getInt("count");
                time = "Week " + time;
                counts.put(time, count);
                System.out.println("Conversion week: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchConversionMonthCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select extract(month from server_log.entry_date) as month, count(*) as count from server_log where conversion = 'Yes' group by month";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = String.valueOf(rs.getLong("month"));
                Integer count = rs.getInt("count");
                counts.put(time, count);
                System.out.println("Conversion month: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchBounceDateCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select date(server_log.entry_date) as date, count(*) as count from server_log where timediff(server_log.exit_date, server_log.entry_date) <= time('00:00:10') or pages_viewed = 1 group by date;";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String date = rs.getDate("date").toString();
                int count = rs.getInt("count");
                counts.put(date,count);
                System.out.println("Server date: " + date + " Count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchBounceHourCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select date_format(server_log.entry_date, '%Y-%m-%d %H:00:00') as time, count(*) as count from server_log where timediff(server_log.exit_date, server_log.entry_date) <= time('00:00:10') or pages_viewed = 1 group by time";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = rs.getTimestamp("time").toString();
                int count = rs.getInt("count");
                counts.put(time,count);
                System.out.println("Bounce time: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchBounceWeekCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select extract(week from server_log.entry_date) as week, count(*) as count from server_log where timediff(server_log.exit_date, server_log.entry_date) <= time('00:00:10') or pages_viewed = 1 group by week";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = String.valueOf(rs.getLong("week"));
                Integer count = rs.getInt("count");
                time = "Week " + time;
                counts.put(time, count);
                System.out.println("Bounce week: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchBounceMonthCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select extract(month from server_log.entry_date) as month, count(*) as count from server_log where timediff(server_log.exit_date, server_log.entry_date) <= time('00:00:10') or pages_viewed = 1 group by month";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = String.valueOf(rs.getLong("month"));
                Integer count = rs.getInt("count");
                counts.put(time, count);
                System.out.println("Bounce month: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }


}
