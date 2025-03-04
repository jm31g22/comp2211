package uk.ac.soton.adauction.example.FetchData;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class to obtain data from the impression log
 */
public class ImpressionLog extends Log{
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
