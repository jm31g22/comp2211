package uk.ac.soton.adauction.example.FetchData;

import java.sql.Connection;

public class Log {

    protected final Connection conn;
    public Log() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        conn = databaseConnection.getConnection();
    }
}
