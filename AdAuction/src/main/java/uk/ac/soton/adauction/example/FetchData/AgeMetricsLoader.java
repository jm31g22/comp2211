package uk.ac.soton.adauction.example.FetchData;

import javafx.beans.property.SimpleStringProperty;
import uk.ac.soton.adauction.example.SettingsState;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import static uk.ac.soton.adauction.example.FetchData.MetricsLoader.getCost;

public class AgeMetricsLoader extends DatabaseConnection{
    private final HashMap<String, SimpleStringProperty> metricValuePairs = new HashMap<>();
    private String query;
    private ResultSet rs;

    private ResultSet executeQuery(String query, String age) {

        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, age);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //load metrics when audience segment(age) is set
    public HashMap<String, SimpleStringProperty> loadAgeMetrics(String age) throws SQLException {
        loadSimpleAgeMetrics(age);
        loadTotalAgeCost(age);
        loadCostMetrics();
        loadAgeBounceMetrics(age);
        return metricValuePairs;
    }

    private void loadSimpleAgeMetrics(String age){
        try {
            //Query number of impressions
            query = "SELECT COUNT(*) FROM impression_log WHERE age = ?" ;
            rs = executeQuery(query, age);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.computeIfAbsent("NumberOfImpressions", key -> new SimpleStringProperty())
                        .set(Integer.toString(count));
            }
            //Query number of clicks
            query = "SELECT COUNT(*) FROM click_log c INNER JOIN user_log u ON c.id = u.id WHERE u.age = ?";
            rs = executeQuery(query,age);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfClicks", new SimpleStringProperty(Integer.toString(count)));
            }

            //Query number of conversions
            query = "SELECT COUNT(*)  FROM server_log s INNER JOIN user_log u ON s.id = u.id "+
                    "WHERE s.conversion = 'Yes' and u.age = ?" ;
            rs = executeQuery(query, age);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfConversions", new SimpleStringProperty(Integer.toString(count)));
            }

            //Query number of uniques
            query = "SELECT COUNT(DISTINCT c.id) FROM click_log c INNER JOIN user_log u ON c.id = u.id WHERE u.age = ?";
            rs = executeQuery(query, age);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfUniques", new SimpleStringProperty(Integer.toString(count)));
            }

        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTotalAgeCost(String age) throws SQLException {

        query = "SELECT SUM(click_cost) FROM click_log c INNER JOIN user_log u ON c.id = u.id WHERE u.age = ?";
        rs = executeQuery(query, age);

        double cost = 0.0;
        if (rs.next()) {
            cost += rs.getDouble(1);
        }
        query = "SELECT SUM(impression_cost) FROM impression_log WHERE gender = ?";
        rs = executeQuery(query, age);

        if (rs.next()) {
            cost += rs.getDouble(1);
        }
        metricValuePairs.put("TotalCost", new SimpleStringProperty(Double.toString(cost/100)));
    }

    private void loadCostMetrics() {
        getCost(metricValuePairs);
    }

    public void loadAgeBounceMetrics(String age) throws SQLException {

        String definition = SettingsState.getBounceDefinitionBinding().get();
        int value = SettingsState.getBounceDefinitionNumberBinding().get();

        //Change the query depending on the definition of bounce
        if (definition.equals("Pages")) {
            query ="SELECT COUNT(*) FROM server_log s LEFT JOIN user_log u ON s.id = u.id "+
                    "WHERE s.pages_viewed <= ? and u.age = ?";


        } else {
            query = "SELECT COUNT(*) FROM server_log s LEFT JOIN impression_log i ON s.id = i.id" +
                    "WHERE TIMESTAMPDIFF(SECOND, s.entry_date, s.exit_date) <= ? and u.age = ?";
        }

        retrieveBoundRate(value, age);
    }


    private void retrieveBoundRate(int value, String age) throws SQLException {
        rs = executeQuery(query, value, age);
        if (rs.next()) {
            metricValuePairs.computeIfAbsent("NumberOfBounces", key -> new SimpleStringProperty())
                    .set(Integer.toString(rs.getInt(1)));           }

        double bounceRate = Double.parseDouble(metricValuePairs.get("NumberOfBounces").get())/Double.parseDouble(metricValuePairs.get("NumberOfClicks").get());
        metricValuePairs.computeIfAbsent("BounceRate", key -> new SimpleStringProperty())
                .set(Double.toString(bounceRate));
    }




    private ResultSet executeQuery(String query, int x, String gender) {

        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, x);
            pstmt.setString(2, gender);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
