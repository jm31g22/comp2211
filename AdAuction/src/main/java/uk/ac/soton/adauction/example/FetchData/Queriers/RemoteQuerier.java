package uk.ac.soton.adauction.example.FetchData.Queriers;

import uk.ac.soton.adauction.example.Utils.DatabaseConnection;

import java.sql.Connection;

public abstract class RemoteQuerier {
    protected final Connection conn = DatabaseConnection.getRemoteLogsConnection();
}
