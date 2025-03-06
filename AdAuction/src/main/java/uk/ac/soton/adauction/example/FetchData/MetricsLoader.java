package uk.ac.soton.adauction.example.FetchData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class MetricsLoader extends DatabaseConnection{

    private final HashMap<String, String> metricValuePairs = new HashMap<>();
    private String query;
    private ResultSet rs;

    public HashMap<String, String> getMetricValuePairs() {
        return metricValuePairs;
    }

    public void loadMetrics() throws SQLException {

        loadSimpleMetrics();
        loadTotalCost();
        laodCostMetrics();
    }

    private void loadSimpleMetrics() {
        try {

            //Query number of impressions
            query = "SELECT COUNT(*) FROM impression_log";
            rs = executeQuery(query);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfImpressions", Integer.toString(count));
            }

            //Query number of clicks
            query = "SELECT COUNT(*) FROM click_log";
            rs = executeQuery(query);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfClicks", Integer.toString(count));
            }

            //Query number of conversions
            query = "SELECT COUNT(*) FROM server_log WHERE conversion = 'Yes'";
            rs = executeQuery(query);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfConversions", Integer.toString(count));
            }

            //Query number of uniques
            query = "SELECT COUNT(DISTINCT id) FROM click_log;";
            rs = executeQuery(query);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfUniques", Integer.toString(count));
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
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
        metricValuePairs.put("TotalCost", Double.toString(cost/100));
    }

    private void laodCostMetrics() {
        Double CTR = Double.parseDouble(metricValuePairs.get("NumberOfClicks"))/Double.parseDouble(metricValuePairs.get("NumberOfImpressions"));
        metricValuePairs.put("CTR", Double.toString(CTR));

        Double CPA = Double.parseDouble(metricValuePairs.get("TotalCost"))/Double.parseDouble(metricValuePairs.get("NumberOfConversions"));
        metricValuePairs.put("CPA", Double.toString(CPA));

        Double CPC = Double.parseDouble(metricValuePairs.get("TotalCost"))/Double.parseDouble(metricValuePairs.get("NumberOfClicks"));
        metricValuePairs.put("CPC", Double.toString(CPC));

        Double CPM = Double.parseDouble(metricValuePairs.get("TotalCost"))/(Double.parseDouble(metricValuePairs.get("NumberOfImpressions"))/1000);
        metricValuePairs.put("CPM", Double.toString(CPM));
    }

}
