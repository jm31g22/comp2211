package uk.ac.soton.adauction.example.FetchData;

import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class KeyMetrics extends DatabaseConnection{

    private final HashMap<String, String> metricValuePairs = new HashMap<>();

    public void calculateMetrics() {

        //Counting number of logs in impression_log
        try {
            String query = "SELECT COUNT(*) FROM impression_log";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            System.out.println(rs);

            if (rs.next()) {
                int count = rs.getInt(1);
                metricValuePairs.put("NumberOfImpressions", Integer.toString(count));

            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
    public HashMap<String, String> getMetricValuePairs() {
        return metricValuePairs;
    }

}
