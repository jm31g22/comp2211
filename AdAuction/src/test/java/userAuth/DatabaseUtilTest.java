package userAuth;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class DatabaseUtilTest {

    @Test
    public void testGetConnection() {
        try {
            Connection connection = DatabaseUtil.getConnection();
            assertNotNull(connection, "Database connection should not be empty");
            connection.close();
        } catch (SQLException e) {
            fail("An error occurred while obtaining a database connection:" + e.getMessage());
        }
    }
}