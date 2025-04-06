package uk.ac.soton.adauction.example.Utils;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.beans.property.SimpleStringProperty;

import com.itextpdf.text.Document;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Export {

    private static HashMap<String, SimpleStringProperty> myMetricValuePairs;
    private static String filepath;

    public static void export(HashMap<String, SimpleStringProperty> metricValuePairs, String type, String location) throws IOException, DocumentException {
        myMetricValuePairs = metricValuePairs;
        filepath = Paths.get(System.getProperty("user.home"), location).toString();

        if (type.equals("pdf")) {
            exportPDF();
        }

        if (type.equals("csv")) {
            exportCSV();
        }

    }

    private static void exportCSV() throws IOException {
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

    private static void exportPDF() throws FileNotFoundException, DocumentException {

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
}
