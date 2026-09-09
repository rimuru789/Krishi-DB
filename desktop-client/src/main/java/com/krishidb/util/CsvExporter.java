package com.krishidb.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class CsvExporter {

    public static boolean exportTableToCsv(Component parent, TableModel model, String suggestedFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(I18n.get("reports.export.dialog_title"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fileChooser.setSelectedFile(new File(suggestedFileName.endsWith(".csv") ? suggestedFileName : (suggestedFileName + ".csv")));

        int userSelection = fileChooser.showSaveDialog(parent);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
        }

        // Confirmation if file already exists
        if (fileToSave.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    parent,
                    I18n.get("reports.export.overwrite_prompt", fileToSave.getName()),
                    I18n.get("reports.export.overwrite_title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        try {
            int colCount = model.getColumnCount();
            int rowCount = model.getRowCount();

            String[] headers = new String[colCount];
            for (int c = 0; c < colCount; c++) {
                headers[c] = model.getColumnName(c);
            }

            java.util.List<String[]> rows = new java.util.ArrayList<>();
            for (int r = 0; r < rowCount; r++) {
                String[] rowData = new String[colCount];
                for (int c = 0; c < colCount; c++) {
                    Object val = model.getValueAt(r, c);
                    rowData[c] = val != null ? val.toString() : "";
                }
                rows.add(rowData);
            }

            exportToFile(fileToSave, headers, rows);

            JOptionPane.showMessageDialog(
                    parent,
                    I18n.get("reports.export.success", fileToSave.getAbsolutePath()),
                    I18n.get("reports.title"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return true;
        } catch (Exception ex) {
            System.err.println("CSV Export failed: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    parent,
                    I18n.get("reports.export.failed", ex.getMessage()),
                    I18n.get("reports.title"),
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    public static void exportToFile(File fileToSave, String[] headers, java.util.List<String[]> rows) throws java.io.IOException {
        try (FileOutputStream fos = new FileOutputStream(fileToSave);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            // Write UTF-8 BOM for Microsoft Excel on Windows (preserves Devanagari Marathi & Hindi)
            writer.write('\uFEFF');

            if (headers != null && headers.length > 0) {
                for (int col = 0; col < headers.length; col++) {
                    writer.write(escapeCsv(headers[col]));
                    if (col < headers.length - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }

            if (rows != null) {
                for (String[] row : rows) {
                    for (int col = 0; col < row.length; col++) {
                        writer.write(escapeCsv(row[col] != null ? row[col] : ""));
                        if (col < row.length - 1) {
                            writer.write(",");
                        }
                    }
                    writer.newLine();
                }
            }

            writer.flush();
        }
    }

    public static String escapeCsv(String input) {
        if (input == null) {
            return "";
        }
        boolean containsSpecial = input.contains(",") || input.contains("\"") || input.contains("\n") || input.contains("\r");
        if (containsSpecial) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }
}
