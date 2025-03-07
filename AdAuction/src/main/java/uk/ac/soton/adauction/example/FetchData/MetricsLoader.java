package uk.ac.soton.adauction.example.FetchData;

import javafx.beans.property.SimpleStringProperty;
import uk.ac.soton.adauction.example.SettingsState;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class MetricsLoader extends DatabaseConnection{

    private final HashMap<String, SimpleStringProperty> metricValuePairs = new HashMap<>();
    private String query;
    private ResultSet rs;



    public HashMap<String, SimpleStringProperty> loadMetrics() throws SQLException {

        loadSimpleMetrics();
        loadTotalCost();
        laodCostMetrics();
        loadBounceMetrics();
        return metricValuePairs;
    }

    private void loadSimpleMetrics() {
        try {

            //Query number of impressions
            query = "SELECT COUNT(*) FROM impression_log";
            rs = executeQuery(query);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfImpressions", new SimpleStringProperty(Integer.toString(count)));
            }

            //Query number of clicks
            query = "SELECT COUNT(*) FROM click_log";
            rs = executeQuery(query);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfClicks", new SimpleStringProperty(Integer.toString(count)));
            }

            //Query number of conversions
            query = "SELECT COUNT(*) FROM server_log WHERE conversion = 'Yes'";
            rs = executeQuery(query);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfConversions", new SimpleStringProperty(Integer.toString(count)));
            }

            //Query number of uniques
            query = "SELECT COUNT(DISTINCT id) FROM click_log;";
            rs = executeQuery(query);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfUniques", new SimpleStringProperty(Integer.toString(count)));
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void loadTotalCost() throws SQLException {

        query = "SELECT SUM(click_cost) FROM click_log";
        rs = executeQuery(query);

        double cost = 0.0;
        if (rs.next()) {
            cost += rs.getDouble(1);
        }
        query = "SELECT SUM(impression_cost) FROM impression_log";
        rs = executeQuery(query);

        if (rs.next()) {
            cost += rs.getDouble(1);
        }
        metricValuePairs.put("TotalCost", new SimpleStringProperty(Double.toString(cost/100)));
    }

    private void laodCostMetrics() {
        double CTR = Double.parseDouble(metricValuePairs.get("NumberOfClicks").get())/Double.parseDouble(metricValuePairs.get("NumberOfImpressions").get());
        metricValuePairs.put("CTR", new SimpleStringProperty(Double.toString(CTR)));

        double CPA = Double.parseDouble(metricValuePairs.get("TotalCost").get())/Double.parseDouble(metricValuePairs.get("NumberOfConversions").get());
        metricValuePairs.put("CPA", new SimpleStringProperty(Double.toString(CPA)));

        double CPC = Double.parseDouble(metricValuePairs.get("TotalCost").get())/Double.parseDouble(metricValuePairs.get("NumberOfClicks").get());
        metricValuePairs.put("CPC", new SimpleStringProperty(Double.toString(CPC)));

        double CPM = Double.parseDouble(metricValuePairs.get("TotalCost").get())/(Double.parseDouble(metricValuePairs.get("NumberOfImpressions").get())/1000);
        metricValuePairs.put("CPM", new SimpleStringProperty(Double.toString(CPM)));
    }

    private void loadBounceMetrics() throws SQLException {
        String definition = SettingsState.getBounceDefinitionBinding().get();
        int value = SettingsState.getBounceDefinitionNumberBinding().get();
        if (definition.equals("Pages")) {
           query ="SELECT COUNT(*) FROM server_log WHERE pages_viewed <= ?";
           rs = executeQuery(query, value);

           if (rs.next()) {
               metricValuePairs.put("NumberOfBounces", new SimpleStringProperty(Integer.toString(rs.getInt(1))));
           }
        } else {
        }
    }


    private ResultSet executeQuery(String query) {

        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ResultSet executeQuery(String query, int x) {

        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, x);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
