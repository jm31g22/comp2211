package uk.ac.soton.adauction.example.FetchData;

import uk.ac.soton.adauction.example.Utils.DatabaseConnection;

import java.sql.*;
import java.util.HashMap;

public class ClickLog{
    private final Connection conn = DatabaseConnection.getLogsConnection();


    public HashMap<String, Integer> fetchClickDateCount(){
        HashMap<String, Integer> counts = new HashMap<>();

        Statement stmt;
        try {
            stmt = conn.createStatement();

            String strSelect = "select date(click_log.click_date) as date, count(*) as count from click_log group by date";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String date = rs.getDate("date").toString();
                int count = rs.getInt("count");
                counts.put(date,count);
                System.out.println("Click date: " + date + " Count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }

    public HashMap<String, Integer> fetchClickHourCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement()
        ) {
            String strSelect = "select date_format(click_date, '%Y-%m-%d %H:00:00') as click_time, count(*) as count from click_log group by click_time";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = rs.getTimestamp("click_time").toString();
                int count = rs.getInt("count");
                counts.put(time,count);
                System.out.println("Click time: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchClickWeekCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement()
        ) {
            String strSelect = "select extract(week from click_date) as week, count(*) as count from click_log group by week";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = String.valueOf(rs.getLong("week"));
                Integer count = rs.getInt("count");
                time = "Week " + time;
                counts.put(time, count);
                System.out.println("click week: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchClickMonthCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement()
        ) {
            String strSelect = "select extract(month from click_date) as month, count(*) as count from click_log group by month";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = String.valueOf(rs.getLong("month"));
                Integer count = rs.getInt("count");
                counts.put(time, count);
                System.out.println("click month: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchUniqueDateCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement()
        ) {
            String strSelect = "with first_date as (select id, min(date(click_date)) as date from click_log group by id) select first_date.date as unique_date, count(*) as count from first_date group by first_date.date";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String date = rs.getDate("unique_date").toString();
                int count = rs.getInt("count");
                counts.put(date,count);
                System.out.println("unique date: " + date + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchUniqueHourCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement()
        ) {
            String strSelect = "with first_date as (select id, min(date_format( click_date, '%Y-%m-%d %H:00:00' )) as date from click_log group by id) select date_format(first_date.date, '%Y-%m-%d %H:00:00' ) as time, count(*) as count from first_date group by time";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = rs.getTimestamp("time").toString();
                int count = rs.getInt("count");
                counts.put(time,count);
                System.out.println("Click time: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchUniqueWeekCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement()
        ) {
            String strSelect = "with first_date as (select id, min(week(click_date)) as date from click_log group by id) select first_date.date as week, count(*) as count from first_date group by first_date.date";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = String.valueOf(rs.getLong("week"));
                Integer count = rs.getInt("count");
                time = "Week " + time;
                counts.put(time, count);
                System.out.println("Unique week: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchUniqueMonthCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement()
        ) {
            String strSelect = "with first_date as (select id, min(month(click_date)) as date from click_log group by id) select first_date.date as month, count(*) as count from first_date group by first_date.date";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = String.valueOf(rs.getLong("month"));
                Integer count = rs.getInt("count");
                counts.put(time, count);
                System.out.println("Unique month: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }
}
