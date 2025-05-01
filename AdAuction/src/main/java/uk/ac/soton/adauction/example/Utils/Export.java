package uk.ac.soton.adauction.example.Utils;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.beans.property.SimpleStringProperty;

import com.itextpdf.text.Document;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Export {

    private static HashMap<String, SimpleStringProperty> myMetricValuePairs;
    private static String filepath;
    private static ObservableList<Map<String, String>> users;

    public static void exportMetrics(HashMap<String, SimpleStringProperty> metricValuePairs, String type, String location) throws IOException, DocumentException {
        myMetricValuePairs = metricValuePairs;
        filepath = Paths.get(System.getProperty("user.home"), location).toString();

        if (type.equals("pdf")) {
            exportMetricsPDF();
        }

        if (type.equals("csv")) {
            exportMetricsCSV();
        }

    }


    private static void exportMetricsCSV() throws IOException {
        filepath = Paths.get(filepath, "metrics.csv").toString();

        FileWriter writer = new FileWriter(filepath);

        writer.write(String.join(",", myMetricValuePairs.keySet()) + "\n");

        // Extract and write values
        StringBuilder valueLine = new StringBuilder();
        for (String key : myMetricValuePairs.keySet()) {
            String value = myMetricValuePairs.get(key).get(); // Extract actual string
            valueLine.append(value).append(",");
        }
        // Remove trailing comma
        if (!valueLine.isEmpty()) {
            valueLine.setLength(valueLine.length() - 1);
        }
        writer.write(valueLine + "\n");

        System.out.println("CSV created at: " + filepath);
        writer.close();

    }

    private static void exportMetricsPDF() throws FileNotFoundException, DocumentException {

        filepath = Paths.get(filepath, "metrics.pdf").toString();

        // Create document and write to PDF
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filepath));
        System.out.println("PDF created at: " + filepath);

        document.open();
        document.newPage();

        for (Map.Entry<String, SimpleStringProperty> entry : myMetricValuePairs.entrySet())  {
            String line = entry.getKey() + ": " + entry.getValue().get();
            document.add(new Paragraph(line));
        }
        document.close();


    }

    public static void exportUsers(ObservableList<Map<String, String>> usersMap, String type, String location) throws IOException, DocumentException {
        users = usersMap;
        filepath = Paths.get(System.getProperty("user.home"), location).toString();

        if (type.equals("pdf")) {
            exportUsersPDF();
        }

        if (type.equals("csv")) {
            exportUsersCSV();
        }
    }

    private static void exportUsersCSV() {
        filepath = Paths.get(filepath, "users.csv").toString();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            // Write header
            writer.write("Username,Password,Email,Role");
            writer.newLine();

            // Write user rows
            for (Map<String, String> user : users) {
                String line = String.join(",",
                        escapeCSV(user.get("username")),
                        escapeCSV(user.get("password")),
                        escapeCSV(user.get("email")),
                        escapeCSV(user.get("role"))
                );
                writer.write(line);
                writer.newLine();
            }

            System.out.println("CSV created at: " + filepath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to escape commas and quotes in CSV
    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    private static void exportUsersPDF() {
        filepath = Paths.get(filepath, "users.pdf").toString();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(filepath));
            document.open();

            document.add(new Paragraph("User List\n\n"));

            PdfPTable table = new PdfPTable(4); // 4 columns: username, password, email, role

            // Add table headers
            table.addCell("Username");
            table.addCell("Password");
            table.addCell("Email");
            table.addCell("Role");

            // Add rows
            for (Map<String, String> user : users) {
                table.addCell(user.get("username"));
                table.addCell(user.get("password"));
                table.addCell(user.get("email"));
                table.addCell(user.get("role"));
            }

            document.add(table);
            document.close();

            System.out.println("PDF created at: " + filepath);
        } catch (DocumentException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
