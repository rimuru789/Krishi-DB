package com.krishidb.ui.pages;

import com.krishidb.dao.ReportDAO;
import com.krishidb.dao.ReportDAO.ReportResult;
import com.krishidb.util.CsvExporter;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class ReportsPanel extends JPanel implements I18n.LocaleChangeListener {

    private final ReportDAO reportDAO;

    // Header
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton refreshButton;
    private JButton exportCsvBtn;

    // Controls
    private JLabel reportTypeLabel;
    private JComboBox<ReportTypeOption> reportTypeBox;
    private JLabel periodLabel;
    private JComboBox<PeriodOption> periodBox;
    private JPanel customDatePanel;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JButton generateBtn;

    // Summary Strip
    private JLabel summaryCountLabel;
    private JLabel summaryValueLabel;

    // Table
    private DefaultTableModel tableModel;
    private JTable table;

    public ReportsPanel() {
        this.reportDAO = new ReportDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        I18n.addListener(this);
        generateReport();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel titlesPanel = new JPanel();
        titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));
        titlesPanel.setOpaque(false);

        titleLabel = new JLabel(I18n.get("reports.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("reports.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titlesPanel.add(titleLabel);
        titlesPanel.add(Box.createVerticalStrut(4));
        titlesPanel.add(subtitleLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        exportCsvBtn = new JButton("📥  " + I18n.get("reports.btn.export_csv"));
        exportCsvBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        exportCsvBtn.setBackground(new Color(22, 101, 52));
        exportCsvBtn.setForeground(Color.WHITE);
        exportCsvBtn.setFocusPainted(false);
        exportCsvBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportCsvBtn.addActionListener(e -> handleExportCsv());

        refreshButton = new JButton("↻  " + I18n.get("reports.btn.refresh"));
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(new Color(15, 23, 42));
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 14, 8, 14)
        ));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> generateReport());

        actions.add(exportCsvBtn);
        actions.add(refreshButton);

        headerPanel.add(titlesPanel, BorderLayout.WEST);
        headerPanel.add(actions, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainContent() {
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setOpaque(false);

        container.add(createFilterAndSummaryBar(), BorderLayout.NORTH);
        container.add(createTableArea(), BorderLayout.CENTER);

        return container;
    }

    private JPanel createFilterAndSummaryBar() {
        JPanel bar = new JPanel(new BorderLayout(15, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Filters Left
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);

        reportTypeLabel = new JLabel(I18n.get("reports.label.report_type") + ":");
        reportTypeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        reportTypeBox = new JComboBox<>();
        reportTypeBox.setPreferredSize(new Dimension(170, 32));
        loadReportTypes();
        reportTypeBox.addActionListener(e -> {
            updatePeriodVisibility();
            generateReport();
        });

        periodLabel = new JLabel(I18n.get("reports.label.period") + ":");
        periodLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        periodBox = new JComboBox<>();
        periodBox.setPreferredSize(new Dimension(130, 32));
        loadPeriods();
        periodBox.addActionListener(e -> {
            updatePeriodVisibility();
            generateReport();
        });

        customDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        customDatePanel.setOpaque(false);
        fromDateField = new JTextField(LocalDate.now().minusMonths(1).toString(), 8);
        fromDateField.setPreferredSize(new Dimension(85, 32));
        toDateField = new JTextField(LocalDate.now().toString(), 8);
        toDateField.setPreferredSize(new Dimension(85, 32));
        customDatePanel.add(new JLabel("From:"));
        customDatePanel.add(fromDateField);
        customDatePanel.add(new JLabel("To:"));
        customDatePanel.add(toDateField);
        customDatePanel.setVisible(false);

        generateBtn = new JButton(I18n.get("reports.btn.generate"));
        generateBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        generateBtn.setBackground(new Color(30, 58, 138));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setFocusPainted(false);
        generateBtn.addActionListener(e -> generateReport());

        filters.add(reportTypeLabel);
        filters.add(reportTypeBox);
        filters.add(periodLabel);
        filters.add(periodBox);
        filters.add(customDatePanel);
        filters.add(generateBtn);

        // Summary Right
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        summary.setOpaque(false);

        summaryCountLabel = new JLabel(I18n.get("reports.summary.records", 0));
        summaryCountLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        summaryCountLabel.setForeground(new Color(71, 85, 105));

        summaryValueLabel = new JLabel(I18n.get("reports.summary.value", "0.00"));
        summaryValueLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryValueLabel.setForeground(new Color(22, 101, 52));

        summary.add(summaryCountLabel);
        summary.add(summaryValueLabel);

        bar.add(filters, BorderLayout.WEST);
        bar.add(summary, BorderLayout.EAST);

        return bar;
    }

    private JPanel createTableArea() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        tableModel = new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadReportTypes() {
        reportTypeBox.removeAllItems();
        reportTypeBox.addItem(new ReportTypeOption("SALES", I18n.get("reports.type.sales")));
        reportTypeBox.addItem(new ReportTypeOption("PURCHASES", I18n.get("reports.type.purchases")));
        reportTypeBox.addItem(new ReportTypeOption("EXPENSES", I18n.get("reports.type.expenses")));
        reportTypeBox.addItem(new ReportTypeOption("INVENTORY", I18n.get("reports.type.inventory")));
        reportTypeBox.addItem(new ReportTypeOption("TRANSACTIONS", I18n.get("reports.type.transactions")));
    }

    private void loadPeriods() {
        periodBox.removeAllItems();
        periodBox.addItem(new PeriodOption("ALL_TIME", I18n.get("reports.period.all_time")));
        periodBox.addItem(new PeriodOption("TODAY", I18n.get("reports.period.today")));
        periodBox.addItem(new PeriodOption("YESTERDAY", I18n.get("reports.period.yesterday")));
        periodBox.addItem(new PeriodOption("THIS_WEEK", I18n.get("reports.period.this_week")));
        periodBox.addItem(new PeriodOption("THIS_MONTH", I18n.get("reports.period.this_month")));
        periodBox.addItem(new PeriodOption("CUSTOM", I18n.get("reports.period.custom")));
    }

    private void updatePeriodVisibility() {
        ReportTypeOption rOpt = (ReportTypeOption) reportTypeBox.getSelectedItem();
        boolean isInventory = rOpt != null && "INVENTORY".equals(rOpt.code);

        periodLabel.setVisible(!isInventory);
        periodBox.setVisible(!isInventory);

        PeriodOption pOpt = (PeriodOption) periodBox.getSelectedItem();
        boolean isCustom = !isInventory && pOpt != null && "CUSTOM".equals(pOpt.code);
        customDatePanel.setVisible(isCustom);

        revalidate();
        repaint();
    }

    public void generateReport() {
        ReportTypeOption rOpt = (ReportTypeOption) reportTypeBox.getSelectedItem();
        if (rOpt == null) return;

        PeriodOption pOpt = (PeriodOption) periodBox.getSelectedItem();
        String period = pOpt != null ? pOpt.code : "ALL_TIME";
        String from = fromDateField.getText().trim();
        String to = toDateField.getText().trim();

        SwingWorker<ReportResult, Void> worker = new SwingWorker<>() {
            @Override
            protected ReportResult doInBackground() {
                return switch (rOpt.code) {
                    case "PURCHASES" -> reportDAO.getPurchaseReport(period, from, to);
                    case "EXPENSES" -> reportDAO.getExpenseReport(period, from, to);
                    case "INVENTORY" -> reportDAO.getInventoryReport();
                    case "TRANSACTIONS" -> reportDAO.getTransactionReport(period, from, to);
                    default -> reportDAO.getSalesReport(period, from, to);
                };
            }

            @Override
            protected void done() {
                try {
                    ReportResult res = get();
                    displayReportResult(res);
                } catch (Exception e) {
                    System.err.println("Report generation error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void displayReportResult(ReportResult res) {
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(res.headers);

        for (Object[] row : res.rows) {
            tableModel.addRow(row);
        }

        // Align right for numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        if (table.getColumnCount() > 0) {
            table.getColumnModel().getColumn(table.getColumnCount() - 1).setCellRenderer(rightRenderer);
        }

        summaryCountLabel.setText(I18n.get("reports.summary.records", res.recordCount));
        summaryValueLabel.setText(I18n.get("reports.summary.value", String.format("%.2f", res.totalValue)));
    }

    private void handleExportCsv() {
        ReportTypeOption rOpt = (ReportTypeOption) reportTypeBox.getSelectedItem();
        String typeName = (rOpt != null ? rOpt.code.toLowerCase() : "report");
        String suggestedName = "krishi_" + typeName + "_report_" + LocalDate.now() + ".csv";

        CsvExporter.exportTableToCsv(this, tableModel, suggestedName);
    }

    @Override
    public void onLocaleChange() {
        titleLabel.setText(I18n.get("reports.title"));
        subtitleLabel.setText(I18n.get("reports.subtitle"));
        refreshButton.setText("↻  " + I18n.get("reports.btn.refresh"));
        exportCsvBtn.setText("📥  " + I18n.get("reports.btn.export_csv"));

        reportTypeLabel.setText(I18n.get("reports.label.report_type") + ":");
        periodLabel.setText(I18n.get("reports.label.period") + ":");
        generateBtn.setText(I18n.get("reports.btn.generate"));

        loadReportTypes();
        loadPeriods();
        generateReport();
    }

    public static class ReportTypeOption {
        public final String code;
        public final String label;

        public ReportTypeOption(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static class PeriodOption {
        public final String code;
        public final String label;

        public PeriodOption(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
