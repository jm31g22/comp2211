package uk.ac.soton.adauction.example.FetchData;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class to obtain data from the impression log
 */
public class ImpressionLog {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/testcampaign?rewriteBatchedStatements=true";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "root";

    /**
     * Function to fetch all impression data
     *
     * @return Array List of impressions
     */
    public ArrayList<Impression> fetchAll() {
        ArrayList<Impression> impressions = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(
                JDBC_URL, JDBC_USER, JDBC_PASSWORD);
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
        try (Connection conn = DriverManager.getConnection(
                JDBC_URL, JDBC_USER, JDBC_PASSWORD);
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
        try (Connection conn = DriverManager.getConnection(
                JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select impression_date, count(*) as count from impression_log group by impression_date";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            String currentDate = "";
            int currentCount = 0;
            boolean ini = true;
            while (rs.next()) {
                String date = rs.getDate("impression_date").toString();
                if (ini) {
                    currentDate = date;
                    ini = false;
                }
                int count = rs.getInt("count");
                if (!currentDate.equals(date)){
                    counts.put(currentDate,currentCount);
                    System.out.println("impression date: " + currentDate + " count: " + currentCount);
                    currentDate = date;
                    currentCount = count;
                }else{
                    currentCount += count;
                }

            }
            counts.put(currentDate,currentCount);
            System.out.println("impression date: " + currentDate + " count: " + currentCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public HashMap<String, Integer> fetchImpressionHourCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(
                JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             Statement stmt = conn.createStatement();
        ) {
            String strSelect = "select impression_date, count(*) as count from impression_log group by impression_date";
            System.out.println("SQL statement " + strSelect + " called");
            ResultSet rs = stmt.executeQuery(strSelect);
            String currentTime = "";
            int currentCount = 0;
            boolean ini = true;
            while (rs.next()) {
                String time = rs.getTimestamp("impression_date").toString();
                time = time.substring(0,14).concat("00:00");
                if (ini) {
                    currentTime = time;
                    ini = false;
                }
                int count = rs.getInt("count");
                if (!currentTime.equals(time)){
                    counts.put(currentTime,currentCount);
                    System.out.println("impression time: " + currentTime + " count: " + currentCount);
                    currentTime = time;
                    currentCount = count;
                }else{
                    currentCount += count;
                }

            }
            counts.put(currentTime,currentCount);
            System.out.println("impression date: " + currentTime + " count: " + currentCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }


}
