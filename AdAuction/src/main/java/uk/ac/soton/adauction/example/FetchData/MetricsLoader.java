package uk.ac.soton.adauction.example.FetchData;

import javafx.beans.property.SimpleStringProperty;
import uk.ac.soton.adauction.example.AppState;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsLoader extends Querier {

    private final HashMap<String, SimpleStringProperty> metricValuePairs = new HashMap<>();
    private String query;
    private StringBuilder queryBuilder = new StringBuilder();
    private ResultSet rs;
    private HashMap<String, String> parameters = new HashMap<>();
    List<Object> values = new ArrayList<>();

    public HashMap<String, SimpleStringProperty> loadAllMetrics(String age, String gender, String income, String context) throws SQLException {
        parseParameters(age, gender, income, context);

        loadSimpleMetrics();
        loadTotalCost();
        loadCostMetrics();
        loadBounceMetrics();

        System.out.println("Metrics loaded");
        return metricValuePairs;
    }

    private void parseParameters(String age, String gender, String income, String context) throws SQLException {
        parameters.clear();

        if (!age.equals("All")) parameters.put("age", age);
        if (!gender.equals("All")) parameters.put("gender", gender);
        if (!income.equals("All")) parameters.put("income", income);
        if (!context.equals("All")) parameters.put("context", context);
    }

    private void loadSimpleMetrics() {
        values.clear();
        parameters.values().forEach(values::add);

        try {
            // NumberOfImpressions
            StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) FROM impression_log i WHERE 1=1");
            addFilters(queryBuilder);
            metricValuePairs.computeIfAbsent("NumberOfImpressions", key -> new SimpleStringProperty())
                    .set(Integer.toString((int) executeFilteredQuery(queryBuilder.toString(), "int")));

            // NumberOfClicks
            queryBuilder = new StringBuilder(
                    "SELECT COUNT(*) FROM click_log c JOIN unique_users i ON c.id = i.id WHERE c.click_cost >= 0"
            );
            addFilters(queryBuilder);
            metricValuePairs.computeIfAbsent("NumberOfClicks", key -> new SimpleStringProperty())
                    .set(Integer.toString((int) executeFilteredQuery(queryBuilder.toString(), "int")));

            // NumberOfConversions
            queryBuilder = new StringBuilder(
                    "SELECT COUNT(*) FROM server_log c JOIN unique_users i ON c.id = i.id WHERE conversion = 'Yes'"
            );
            addFilters(queryBuilder);
            metricValuePairs.computeIfAbsent("NumberOfConversions", key -> new SimpleStringProperty())
                    .set(Integer.toString((int) executeFilteredQuery(queryBuilder.toString(), "int")));

            // Number of Uniques
            queryBuilder = new StringBuilder(
                    "SELECT COUNT(DISTINCT c.id) FROM click_log c JOIN unique_users i ON c.id = i.id WHERE 1=1"
            );
            addFilters(queryBuilder);
            metricValuePairs.computeIfAbsent("NumberOfUniques", key -> new SimpleStringProperty())
                    .set(Integer.toString((int) executeFilteredQuery(queryBuilder.toString(), "int")));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addFilters(StringBuilder query) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            query.append(" AND i.").append(entry.getKey()).append(" = ?");
        }
    }

    private void loadTotalCost() throws SQLException {
        double cost = 0.0;

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT SUM(click_cost) FROM click_log c JOIN unique_users i ON c.id = i.id WHERE 1=1"
        );
        addFilters(queryBuilder);
        cost += (double) executeFilteredQuery(queryBuilder.toString(), "double");

        queryBuilder = new StringBuilder(
                "SELECT SUM(impression_cost) FROM impression_log i WHERE 1=1"
        );
        addFilters(queryBuilder);
        cost += (double) executeFilteredQuery(queryBuilder.toString(), "double");

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
        String definition = AppState.getBounceDefinitionBinding().get();
        int value = AppState.getBounceDefinitionNumberBinding().get();

        // Change the query depending on the definition of bounce
        if (definition.equals("Pages")) {
            queryBuilder = new StringBuilder(
                    "SELECT COUNT(*) FROM server_log c JOIN unique_users i ON c.id = i.id WHERE 1=1"
            );
            addFilters(queryBuilder);
            queryBuilder.append(" AND pages_viewed >= ").append(value);
        } else {
            queryBuilder = new StringBuilder(
                    "SELECT COUNT(*) FROM server_log c JOIN unique_users i ON c.id = i.id WHERE 1=1"
            );
            addFilters(queryBuilder);
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

    private Number executeFilteredQuery(String query, String returnType) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Set values dynamically
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            System.out.println("Executed SQL: " + stmt.toString());

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

}
