package uk.ac.soton.adauction.example;

import java.sql.SQLException;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("junit-jupiter")
@SelectPackages("uk.ac.soton.adauction.example") // Runs all tests in this package

public class AppTests {
}
