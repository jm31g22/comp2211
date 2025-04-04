package uk.ac.soton.adauction.example.FetchData;

import javafx.beans.property.SimpleStringProperty;
import uk.ac.soton.adauction.example.App;
import uk.ac.soton.adauction.example.AppState;
import uk.ac.soton.adauction.example.FetchData.Queriers.LocalQuerier;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsLoader extends LocalQuerier {

    private final HashMap<String, SimpleStringProperty> metricValuePairs = new HashMap<>();
    private StringBuilder queryBuilder = new StringBuilder();
    private HashMap<String, String> parameters = new HashMap<>();
    List<Object> values = new ArrayList<>();
    private double cost;

    public HashMap<String, SimpleStringProperty> loadAllMetrics(String age, String gender, String income, String context, String startDate, String endDate) throws SQLException {
        System.out.println("Updating metrics");
        addParameters(age, gender, income, context, startDate, endDate);

        loadSimpleMetrics();
        loadTotalCost();
        loadCostMetrics();
        loadBounceMetrics();

        System.out.println("Metrics loaded");
        return metricValuePairs;
    }

    private void addParameters(String age, String gender, String income, String context, String startDate, String endDate) throws SQLException {
        parameters.clear();

        if (!age.equals("All")) parameters.put("age", age);
        if (!gender.equals("All")) parameters.put("gender", gender);
        if (!income.equals("All")) parameters.put("income", income);
        if (!context.equals("All")) parameters.put("context", context);
        if (!startDate.equals("Start")) parameters.put("startDate", startDate);
        if (!endDate.equals("End")) parameters.put("endDate", endDate);

    }

    private String getFirstColumnName(String tableName) throws SQLException {
        String query = "PRAGMA table_info(" + tableName + ")";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getString("name"); // The "name" column contains the column name
            }
        }
        return null; // Return null if no column is found
    }

    private void loadSimpleMetrics() {
        values.clear();
        values.addAll(parameters.values());

        try {
            // NumberOfImpressions
            StringBuilder queryBuilder = new StringBuilder("SELECT SUM(i.impression_cost) AS total_cost, COUNT(*) " +
                    "AS total_count FROM impression_log i WHERE 1=1");
            addFilters(queryBuilder, "impression_log");


            Object[] result = executeFilteredDoubleQuery(queryBuilder.toString());
            cost = (double) result[0];
            int count = (int) result[1];

            metricValuePairs.computeIfAbsent("NumberOfImpressions", key -> new SimpleStringProperty())
                    .set(Integer.toString(count));

            // NumberOfClicks
            queryBuilder = new StringBuilder(
                    "SELECT COUNT(*) FROM click_log c JOIN unique_users i ON c.id = i.id WHERE c.click_cost >= 0"
            );
            addFilters(queryBuilder, "click_log");
            metricValuePairs.computeIfAbsent("NumberOfClicks", key -> new SimpleStringProperty())
                    .set(Integer.toString((int) executeFilteredQuery(queryBuilder.toString(), "int")));

            // NumberOfConversions
            queryBuilder = new StringBuilder(
                    "SELECT COUNT(*) FROM server_log c JOIN unique_users i ON c.id = i.id WHERE conversion = 'Yes'"
            );
            addFilters(queryBuilder, "server_log");
            metricValuePairs.computeIfAbsent("NumberOfConversions", key -> new SimpleStringProperty())
                    .set(Integer.toString((int) executeFilteredQuery(queryBuilder.toString(), "int")));

            // Number of Uniques
            queryBuilder = new StringBuilder(
                    "SELECT COUNT(DISTINCT c.id) FROM click_log c JOIN unique_users i ON c.id = i.id WHERE 1=1"
            );
            addFilters(queryBuilder, "click_log");
            metricValuePairs.computeIfAbsent("NumberOfUniques", key -> new SimpleStringProperty())
                    .set(Integer.toString((int) executeFilteredQuery(queryBuilder.toString(), "int")));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void loadTotalCost() throws SQLException {

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT SUM(click_cost) FROM click_log c JOIN unique_users i ON c.id = i.id WHERE 1=1"
        );
        addFilters(queryBuilder, "click_log");
        cost += (double) executeFilteredQuery(queryBuilder.toString(), "double");

//        queryBuilder = new StringBuilder(
//                "SELECT SUM(impression_cost) FROM impression_log i WHERE 1=1"
//        );
//        addFilters(queryBuilder, "impression_log");
//        cost += (double) executeFilteredQuery(queryBuilder.toString(), "double");

        metricValuePairs.computeIfAbsent("TotalCost", key -> new SimpleStringProperty())
                .set(Double.toString(cost/100));
    }

    private void loadCostMetrics() {
        double CTR = Double.parseDouble(metricValuePairs.get("NumberOfClicks").get()) /
                Double.parseDouble(metricValuePairs.get("NumberOfImpressions").get());
        metricValuePairs.computeIfAbsent("CTR", key -> new SimpleStringProperty())
                .set(Double.toString(CTR));

        double CPA = Double.parseDouble(metricValuePairs.get("TotalCost").get()) /
                Double.parseDouble(metricValuePairs.get("NumberOfConversions").get());
        metricValuePairs.computeIfAbsent("CPA", key -> new SimpleStringProperty())
                .set(Double.toString(CPA));

        double CPC = Double.parseDouble(metricValuePairs.get("TotalCost").get()) /
                Double.parseDouble(metricValuePairs.get("NumberOfClicks").get());
        metricValuePairs.computeIfAbsent("CPC", key -> new SimpleStringProperty())
                .set(Double.toString(CPC));

        double CPM = Double.parseDouble(metricValuePairs.get("TotalCost").get()) /
                (Double.parseDouble(metricValuePairs.get("NumberOfImpressions").get()) / 1000);
        metricValuePairs.computeIfAbsent("CPM", key -> new SimpleStringProperty())
                .set(Double.toString(CPM));
    }

    public HashMap<String, SimpleStringProperty> loadBounceMetrics() throws SQLException {
        String definition = App.getAppState().getBounceDefinitionBinding().get();
        int value = App.getAppState().getBounceDefinitionNumberBinding().get();

        // Change the query depending on the definition of bounce
        if (definition.equals("Pages")) {
            queryBuilder = new StringBuilder(
                    "SELECT COUNT(*) FROM server_log c JOIN unique_users i ON c.id = i.id WHERE 1=1"
            );
            addFilters(queryBuilder, "server_log");
            queryBuilder.append(" AND pages_viewed >= ").append(value);
        } else {
            queryBuilder = new StringBuilder(
                    "SELECT COUNT(*) FROM server_log c JOIN unique_users i ON c.id = i.id WHERE 1=1"
            );
            addFilters(queryBuilder, "server_log");
            queryBuilder.append(" AND TIMESTAMPDIFF(SECOND, entry_date, exit_date) <= ").append(value);
        }

        metricValuePairs.computeIfAbsent("NumberOfBounces", key -> new SimpleStringProperty())
                .set(Integer.toString((int) executeFilteredQuery(queryBuilder.toString(), "int")));

        double bounceRate = Double.parseDouble(metricValuePairs.get("NumberOfBounces").get()) /
                Double.parseDouble(metricValuePairs.get("NumberOfClicks").get());

        metricValuePairs.computeIfAbsent("BounceRate", key -> new SimpleStringProperty())
                .set(Double.toString(bounceRate));

        return metricValuePairs;
    }

    private void addFilters(StringBuilder query, String tableName) throws SQLException {
        String firstColumnName = getFirstColumnName(tableName);

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();

            if ("startDate".equalsIgnoreCase(key)) {
                query.append(" AND DATE(").append(firstColumnName).append(") >= ?");
            } else if ("endDate".equalsIgnoreCase(key)) {
                query.append(" AND DATE(").append(firstColumnName).append(") <= ?");
            } else {
                query.append(" AND i.").append(key).append(" = ?");
            }
        }
    }

    private Number executeFilteredQuery(String query, String returnType) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Set values dynamically
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            System.out.println(stmt.toString());

            // Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (returnType.equals("int")) {
                        return rs.getObject(1) != null ? rs.getInt(1) : 0; // Return 0 if NULL
                    } else {
                        return rs.getObject(1) != null ? rs.getDouble(1) : 0.0; // Return 0.0 if NULL
                    }
                }
            }
        }
        return returnType.equals("int") ? 0 : 0.0; // Return default value (no -1 to avoid errors)
    }

    private Object[] executeFilteredDoubleQuery(String query) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Set values dynamically
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            System.out.println(stmt.toString());

            // Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Handling SUM(impression_cost) and COUNT(*)
                    Double totalCost = rs.getObject(1) != null ? rs.getDouble(1) : 0.0;
                    Integer totalCount = rs.getObject(2) != null ? rs.getInt(2) : 0;
                    return new Object[]{totalCost, totalCount};
                }
            }
        }
        return new Object[]{0.0, 0}; // Default return values if no data is found
    }
}
