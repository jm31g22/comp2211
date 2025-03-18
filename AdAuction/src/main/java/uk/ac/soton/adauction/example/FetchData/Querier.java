package uk.ac.soton.adauction.example.FetchData;

import uk.ac.soton.adauction.example.Utils.DatabaseConnection;

import java.sql.Connection;

abstract class Querier {

    protected final Connection conn = DatabaseConnection.getLocalLogsConnection();


}
