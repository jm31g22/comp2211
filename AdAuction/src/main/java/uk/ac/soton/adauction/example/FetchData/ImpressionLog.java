package uk.ac.soton.adauction.example.FetchData;

import uk.ac.soton.adauction.example.FetchData.Queriers.LocalQuerier;


import java.sql.*;
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
    public HashMap<String, Integer> fetchImpressionGenderCount() {
        HashMap<String, Integer> counts = new HashMap<>();
        int val = 0;
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "SELECT COUNT(*) FROM impression_log WHERE gender = 'Male'";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("male", val);
                    System.out.println("Male = " + val);
                }
            }
            strSelect = "SELECT COUNT(*) FROM impression_log WHERE gender = 'Female'";
            System.out.println("SQL statement " + strSelect + " called");
            rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("female", val);
                    System.out.println("Female = " + val);
                }
            }
            strSelect = "SELECT COUNT(*) FROM impression_log";
            System.out.println("SQL statement " + strSelect + " called");
            rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("total", val);
                    System.out.println("Total = " + val);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchImpressionAgeCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        int val = 0;
        try (
                Statement stmt = conn.createStatement();
        ) {
            String strSelect = "SELECT COUNT(*) FROM impression_log WHERE age = '<25'";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            if (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("<25", val);
                }
            }
            strSelect = "SELECT COUNT(*) FROM impression_log WHERE age = '25-34'";
            rs = stmt.executeQuery(strSelect);
            if (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("25-34", val);
                }
            }
            strSelect = "SELECT COUNT(*) FROM impression_log WHERE age = '35-44'";
            rs = stmt.executeQuery(strSelect);
            if (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("35-44", val);
                }
            }
            strSelect = "SELECT COUNT(*) FROM impression_log WHERE age = '45-54'";
            rs = stmt.executeQuery(strSelect);
            if (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("45-54", val);
                }
            }
            strSelect = "SELECT COUNT(*) FROM impression_log WHERE age = '>54'";
            rs = stmt.executeQuery(strSelect);
            if (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put(">54", val);
                }
            }
            strSelect = "SELECT COUNT(*) FROM impression_log";
            rs = stmt.executeQuery(strSelect);
            if (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("total", val);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchImpressionIncomeCount() {
        HashMap<String, Integer> counts = new HashMap<>();
        int val = 0;
        try (
                Statement stmt = conn.createStatement();
        ) {
            String strSelect = "SELECT COUNT(*) FROM impression_log WHERE income = 'Low'";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("low", val);
                }
            }
            strSelect = "SELECT COUNT(*) FROM impression_log WHERE income = 'Medium'";
            System.out.println("SQL statement " + strSelect + " called");
            rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("medium", val);
                }
            }
            strSelect = "SELECT COUNT(*) FROM impression_log WHERE income = 'High'";
            System.out.println("SQL statement " + strSelect + " called");
            rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("high", val);
                }
            }
            strSelect = "SELECT COUNT(*) FROM impression_log";
            System.out.println("SQL statement " + strSelect + " called");
            rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                if (rs.getObject(1) != null) {
                    val = rs.getInt(1);
                    counts.put("total", val);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchImpressionDateCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select date(impression_log.impression_date) as date, count(*) as count from impression_log group by date";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String date = rs.getDate("date").toString();
                int count = rs.getInt("count");
                counts.put(date,count);
                System.out.println("impression date: " + date + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchImpressionHourCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select date_format(impression_date, '%Y-%m-%d %H:00:00') as time, count(*) as count from impression_log group by time";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = rs.getTimestamp("time").toString();
                int count = rs.getInt("count");
                counts.put(time,count);
                System.out.println("impression time: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchImpressionWeekCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select extract(week from impression_date) as week, count(*) as count from impression_log group by week";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = String.valueOf(rs.getLong("week"));
                Integer count = rs.getInt("count");
                time = "Week " + time;
                counts.put(time, count);
                System.out.println("impression week: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchImpressionMonthCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select extract(month from impression_date) as month, count(*) as count from impression_log group by month";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            while (rs.next()) {
                String time = String.valueOf(rs.getLong("Month"));
                Integer count = rs.getInt("count");
                counts.put(time, count);
                System.out.println("impression month: " + time + " count: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }


}
