package com.krishidb.ui.pages;

import com.krishidb.dao.TransactionDAO;
import com.krishidb.model.TransactionRecord;
import com.krishidb.ui.dialogs.RecordPaymentDialog;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class TransactionPanel extends JPanel implements I18n.LocaleChangeListener {

    private final TransactionDAO transactionDAO;

    // Header
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton refreshButton;

    // Stat Cards
    private JLabel inflowHeading;
    private JLabel inflowValue;
    private JLabel inflowDesc;

    private JLabel outflowHeading;
    private JLabel outflowValue;
    private JLabel outflowDesc;

    private JLabel netHeading;
    private JLabel netValue;
    private JLabel netDesc;

    private JLabel countHeading;
    private JLabel countValue;
    private JLabel countDesc;

    // Controls
    private JButton recordPaymentBtn;
    private JComboBox<String> typeFilterBox;
    private JComboBox<String> paymentFilterBox;
    private JTextField searchField;

    // Table
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    public TransactionPanel() {
        this.transactionDAO = new TransactionDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        I18n.addListener(this);
        refreshTransactions();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel titlesPanel = new JPanel();
        titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));
        titlesPanel.setOpaque(false);

        titleLabel = new JLabel(I18n.get("transaction.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("transaction.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titlesPanel.add(titleLabel);
        titlesPanel.add(Box.createVerticalStrut(4));
        titlesPanel.add(subtitleLabel);

        refreshButton = new JButton("↻  " + I18n.get("transaction.btn.refresh"));
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(new Color(15, 23, 42));
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 16, 8, 16)
        ));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> refreshTransactions());

        headerPanel.add(titlesPanel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainContent() {
        JPanel container = new JPanel(new BorderLayout(0, 15));
        container.setOpaque(false);

        container.add(createStatCards(), BorderLayout.NORTH);
        container.add(createTableArea(), BorderLayout.CENTER);

        return container;
    }

    private JPanel createStatCards() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 95));

        // Card 1: Total Inflow (Sales + Customer Payments)
        JPanel card1 = new JPanel();
        card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
        card1.setBackground(Color.WHITE);
        card1.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 12, 16)
        ));
        inflowHeading = new JLabel(I18n.get("transaction.card.inflow"));
        inflowHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        inflowHeading.setForeground(new Color(100, 116, 139));
        inflowValue = new JLabel("\u20B90.00");
        inflowValue.setFont(new Font("SansSerif", Font.BOLD, 20));
        inflowValue.setForeground(new Color(22, 101, 52));
        inflowDesc = new JLabel(I18n.get("transaction.card.inflow_desc"));
        inflowDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        inflowDesc.setForeground(new Color(148, 163, 184));
        card1.add(inflowHeading);
        card1.add(Box.createVerticalStrut(4));
        card1.add(inflowValue);
        card1.add(Box.createVerticalStrut(2));
        card1.add(inflowDesc);

        // Card 2: Total Outflow (Purchases + Expenses + Supplier Payments)
        JPanel card2 = new JPanel();
        card2.setLayout(new BoxLayout(card2, BoxLayout.Y_AXIS));
        card2.setBackground(Color.WHITE);
        card2.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 12, 16)
        ));
        outflowHeading = new JLabel(I18n.get("transaction.card.outflow"));
        outflowHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        outflowHeading.setForeground(new Color(100, 116, 139));
        outflowValue = new JLabel("\u20B90.00");
        outflowValue.setFont(new Font("SansSerif", Font.BOLD, 20));
        outflowValue.setForeground(new Color(185, 28, 28));
        outflowDesc = new JLabel(I18n.get("transaction.card.outflow_desc"));
        outflowDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        outflowDesc.setForeground(new Color(148, 163, 184));
        card2.add(outflowHeading);
        card2.add(Box.createVerticalStrut(4));
        card2.add(outflowValue);
        card2.add(Box.createVerticalStrut(2));
        card2.add(outflowDesc);

        // Card 3: Net Cash Movement
        JPanel card3 = new JPanel();
        card3.setLayout(new BoxLayout(card3, BoxLayout.Y_AXIS));
        card3.setBackground(Color.WHITE);
        card3.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 12, 16)
        ));
        netHeading = new JLabel(I18n.get("transaction.card.net"));
        netHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        netHeading.setForeground(new Color(100, 116, 139));
        netValue = new JLabel("\u20B90.00");
        netValue.setFont(new Font("SansSerif", Font.BOLD, 20));
        netValue.setForeground(new Color(30, 58, 138));
        netDesc = new JLabel(I18n.get("transaction.card.net_desc"));
        netDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        netDesc.setForeground(new Color(148, 163, 184));
        card3.add(netHeading);
        card3.add(Box.createVerticalStrut(4));
        card3.add(netValue);
        card3.add(Box.createVerticalStrut(2));
        card3.add(netDesc);

        // Card 4: Total Records Count
        JPanel card4 = new JPanel();
        card4.setLayout(new BoxLayout(card4, BoxLayout.Y_AXIS));
        card4.setBackground(Color.WHITE);
        card4.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 12, 16)
        ));
        countHeading = new JLabel(I18n.get("transaction.card.count"));
        countHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        countHeading.setForeground(new Color(100, 116, 139));
        countValue = new JLabel("0");
        countValue.setFont(new Font("SansSerif", Font.BOLD, 20));
        countValue.setForeground(new Color(15, 23, 42));
        countDesc = new JLabel(I18n.get("transaction.card.count_desc"));
        countDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        countDesc.setForeground(new Color(148, 163, 184));
        card4.add(countHeading);
        card4.add(Box.createVerticalStrut(4));
        card4.add(countValue);
        card4.add(Box.createVerticalStrut(2));
        card4.add(countDesc);

        cardsPanel.add(card1);
        cardsPanel.add(card2);
        cardsPanel.add(card3);
        cardsPanel.add(card4);

        return cardsPanel;
    }

    private JPanel createTableArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setOpaque(false);

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTools.setOpaque(false);

        recordPaymentBtn = new JButton("＋  " + I18n.get("transaction.btn.record_payment"));
        recordPaymentBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        recordPaymentBtn.setBackground(new Color(30, 58, 138));
        recordPaymentBtn.setForeground(Color.WHITE);
        recordPaymentBtn.setFocusPainted(false);
        recordPaymentBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        recordPaymentBtn.addActionListener(e -> handleRecordPayment());

        leftTools.add(recordPaymentBtn);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTools.setOpaque(false);

        String[] types = {"ALL", "SALE", "PURCHASE", "EXPENSE", "CUSTOMER_PAYMENT", "SUPPLIER_PAYMENT"};
        typeFilterBox = new JComboBox<>(types);
        typeFilterBox.setPreferredSize(new Dimension(140, 32));
        typeFilterBox.addActionListener(e -> applyFilter());

        String[] payments = {"ALL", "CASH", "UPI", "CREDIT", "BANK"};
        paymentFilterBox = new JComboBox<>(payments);
        paymentFilterBox.setPreferredSize(new Dimension(100, 32));
        paymentFilterBox.addActionListener(e -> applyFilter());

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(180, 32));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("transaction.search_placeholder"));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        rightTools.add(new JLabel(I18n.get("transaction.filter.type") + ":"));
        rightTools.add(typeFilterBox);
        rightTools.add(new JLabel(I18n.get("transaction.filter.payment") + ":"));
        rightTools.add(paymentFilterBox);
        rightTools.add(searchField);

        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);

        // Table
        String[] cols = {
                I18n.get("transaction.col.id"),
                I18n.get("transaction.col.date"),
                I18n.get("transaction.col.type"),
                I18n.get("transaction.col.ref_id"),
                I18n.get("transaction.col.description"),
                I18n.get("transaction.col.payment_method"),
                I18n.get("transaction.col.amount"),
                I18n.get("transaction.col.sync")
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setMaxWidth(70);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        // Custom renderer for Amount with color (Green for +, Red for -)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.RIGHT);
                String str = (val != null) ? val.toString() : "";
                if (str.startsWith("+")) {
                    c.setForeground(new Color(22, 101, 52));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (str.startsWith("-")) {
                    c.setForeground(new Color(185, 28, 28));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(new Color(15, 23, 42));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void refreshTransactions() {
        tableModel.setRowCount(0);
        List<TransactionRecord> records = transactionDAO.getAllTransactions();
        for (TransactionRecord tx : records) {
            String prefix = tx.isInflow() ? "+" : "-";
            String amountDisplay = prefix + " \u20B9" + String.format("%.2f", tx.getAmount());

            tableModel.addRow(new Object[]{
                    tx.getId(),
                    tx.getTransactionDate() != null ? tx.getTransactionDate() : "-",
                    tx.getTransactionType(),
                    tx.getReferenceId() != null ? tx.getReferenceId() : "-",
                    tx.getDescription() != null ? tx.getDescription() : "",
                    tx.getPaymentMethod(),
                    amountDisplay,
                    tx.getSyncStatus()
            });
        }

        double totalInflow = transactionDAO.getTotalInflow();
        double totalOutflow = transactionDAO.getTotalOutflow();
        double net = transactionDAO.getNetCashFlow();
        int count = transactionDAO.getTotalCount();

        inflowValue.setText("\u20B9" + String.format("%.2f", totalInflow));
        outflowValue.setText("\u20B9" + String.format("%.2f", totalOutflow));
        netValue.setText((net >= 0 ? "+" : "") + "\u20B9" + String.format("%.2f", net));
        netValue.setForeground(net >= 0 ? new Color(22, 101, 52) : new Color(185, 28, 28));
        countValue.setText(String.valueOf(count));
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        String selectedType = (String) typeFilterBox.getSelectedItem();
        String selectedPayment = (String) paymentFilterBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();

        if (text != null && !text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + text));
        }

        if (selectedType != null && !selectedType.isEmpty() && !"ALL".equalsIgnoreCase(selectedType)) {
            filters.add(RowFilter.regexFilter("(?i)^" + selectedType + "$", 2));
        }

        if (selectedPayment != null && !selectedPayment.isEmpty() && !"ALL".equalsIgnoreCase(selectedPayment)) {
            filters.add(RowFilter.regexFilter("(?i)^" + selectedPayment + "$", 5));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void handleRecordPayment() {
        RecordPaymentDialog dialog = new RecordPaymentDialog();
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshTransactions();
        }
    }

    @Override
    public void onLocaleChange() {
        titleLabel.setText(I18n.get("transaction.title"));
        subtitleLabel.setText(I18n.get("transaction.subtitle"));
        refreshButton.setText("↻  " + I18n.get("transaction.btn.refresh"));

        inflowHeading.setText(I18n.get("transaction.card.inflow"));
        inflowDesc.setText(I18n.get("transaction.card.inflow_desc"));
        outflowHeading.setText(I18n.get("transaction.card.outflow"));
        outflowDesc.setText(I18n.get("transaction.card.outflow_desc"));
        netHeading.setText(I18n.get("transaction.card.net"));
        netDesc.setText(I18n.get("transaction.card.net_desc"));
        countHeading.setText(I18n.get("transaction.card.count"));
        countDesc.setText(I18n.get("transaction.card.count_desc"));

        recordPaymentBtn.setText("＋  " + I18n.get("transaction.btn.record_payment"));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("transaction.search_placeholder"));

        refreshTransactions();
    }
}
