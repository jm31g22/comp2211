package userAuth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//connect to database
public class DatabaseUtil {
    //change to your own database
//    private static final String URL = "jdbc:mysql://localhost:3306/your_database";
//    private static final String USER = "your_username";
//    private static final String PASSWORD = "your_password";
    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String USER = "";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}