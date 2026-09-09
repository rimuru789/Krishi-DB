package com.krishidb.ui.pages;

import com.krishidb.dao.ExpenseDAO;
import com.krishidb.model.Expense;
import com.krishidb.ui.dialogs.AddExpenseDialog;
import com.krishidb.ui.dialogs.EditExpenseDialog;
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

public class ExpensePanel extends JPanel implements I18n.LocaleChangeListener {

    private final ExpenseDAO expenseDAO;

    // Header
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton refreshButton;

    // Stat Cards
    private JLabel todayExpensesHeading;
    private JLabel todayExpensesValue;
    private JLabel todayExpensesDesc;
    private JLabel totalExpensesHeading;
    private JLabel totalExpensesValue;
    private JLabel totalExpensesDesc;
    private JLabel totalCountHeading;
    private JLabel totalCountValue;
    private JLabel totalCountDesc;

    // Controls
    private JButton addExpenseBtn;
    private JButton editExpenseBtn;
    private JButton deleteExpenseBtn;
    private JComboBox<String> categoryFilterBox;
    private JTextField searchField;

    // Table
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    public ExpensePanel() {
        this.expenseDAO = new ExpenseDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        I18n.addListener(this);
        refreshExpenses();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel titlesPanel = new JPanel();
        titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));
        titlesPanel.setOpaque(false);

        titleLabel = new JLabel(I18n.get("expense.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("expense.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titlesPanel.add(titleLabel);
        titlesPanel.add(Box.createVerticalStrut(4));
        titlesPanel.add(subtitleLabel);

        refreshButton = new JButton("↻  " + I18n.get("expense.btn.refresh"));
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(new Color(15, 23, 42));
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 16, 8, 16)
        ));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> refreshExpenses());

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
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 95));

        // Card 1: Today's Expenses
        JPanel card1 = new JPanel();
        card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
        card1.setBackground(Color.WHITE);
        card1.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 18, 12, 18)
        ));
        todayExpensesHeading = new JLabel(I18n.get("expense.card.today"));
        todayExpensesHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        todayExpensesHeading.setForeground(new Color(100, 116, 139));
        todayExpensesValue = new JLabel("\u20B90.00");
        todayExpensesValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        todayExpensesValue.setForeground(new Color(185, 28, 28));
        todayExpensesDesc = new JLabel(I18n.get("expense.card.today_desc"));
        todayExpensesDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        todayExpensesDesc.setForeground(new Color(148, 163, 184));
        card1.add(todayExpensesHeading);
        card1.add(Box.createVerticalStrut(4));
        card1.add(todayExpensesValue);
        card1.add(Box.createVerticalStrut(2));
        card1.add(todayExpensesDesc);

        // Card 2: Total Expenses
        JPanel card2 = new JPanel();
        card2.setLayout(new BoxLayout(card2, BoxLayout.Y_AXIS));
        card2.setBackground(Color.WHITE);
        card2.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 18, 12, 18)
        ));
        totalExpensesHeading = new JLabel(I18n.get("expense.card.total"));
        totalExpensesHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        totalExpensesHeading.setForeground(new Color(100, 116, 139));
        totalExpensesValue = new JLabel("\u20B90.00");
        totalExpensesValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        totalExpensesValue.setForeground(new Color(15, 23, 42));
        totalExpensesDesc = new JLabel(I18n.get("expense.card.total_desc"));
        totalExpensesDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        totalExpensesDesc.setForeground(new Color(148, 163, 184));
        card2.add(totalExpensesHeading);
        card2.add(Box.createVerticalStrut(4));
        card2.add(totalExpensesValue);
        card2.add(Box.createVerticalStrut(2));
        card2.add(totalExpensesDesc);

        // Card 3: Expense Entries Count
        JPanel card3 = new JPanel();
        card3.setLayout(new BoxLayout(card3, BoxLayout.Y_AXIS));
        card3.setBackground(Color.WHITE);
        card3.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 18, 12, 18)
        ));
        totalCountHeading = new JLabel(I18n.get("expense.card.count"));
        totalCountHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        totalCountHeading.setForeground(new Color(100, 116, 139));
        totalCountValue = new JLabel("0");
        totalCountValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        totalCountValue.setForeground(new Color(180, 83, 9));
        totalCountDesc = new JLabel(I18n.get("expense.card.count_desc"));
        totalCountDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        totalCountDesc.setForeground(new Color(148, 163, 184));
        card3.add(totalCountHeading);
        card3.add(Box.createVerticalStrut(4));
        card3.add(totalCountValue);
        card3.add(Box.createVerticalStrut(2));
        card3.add(totalCountDesc);

        cardsPanel.add(card1);
        cardsPanel.add(card2);
        cardsPanel.add(card3);

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

        addExpenseBtn = new JButton("＋  " + I18n.get("expense.btn.add"));
        addExpenseBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addExpenseBtn.setBackground(new Color(22, 101, 52));
        addExpenseBtn.setForeground(Color.WHITE);
        addExpenseBtn.setFocusPainted(false);
        addExpenseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addExpenseBtn.addActionListener(e -> handleAddExpense());

        editExpenseBtn = new JButton(I18n.get("expense.btn.edit"));
        editExpenseBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        editExpenseBtn.addActionListener(e -> handleEditExpense());

        deleteExpenseBtn = new JButton(I18n.get("expense.btn.delete"));
        deleteExpenseBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        deleteExpenseBtn.setForeground(new Color(185, 28, 28));
        deleteExpenseBtn.addActionListener(e -> handleDeleteExpense());

        leftTools.add(addExpenseBtn);
        leftTools.add(editExpenseBtn);
        leftTools.add(deleteExpenseBtn);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTools.setOpaque(false);

        String[] cats = {"ALL", "Transport", "Labor", "Electricity", "Rent", "Packaging", "Maintenance", "Other"};
        categoryFilterBox = new JComboBox<>(cats);
        categoryFilterBox.setPreferredSize(new Dimension(140, 32));
        categoryFilterBox.addActionListener(e -> applyFilter());

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 32));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("expense.search_placeholder"));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        rightTools.add(new JLabel(I18n.get("expense.filter.category") + ":"));
        rightTools.add(categoryFilterBox);
        rightTools.add(searchField);

        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);

        // Table
        String[] cols = {
                I18n.get("expense.col.id"),
                I18n.get("expense.col.date"),
                I18n.get("expense.col.category"),
                I18n.get("expense.col.description"),
                I18n.get("expense.col.amount"),
                I18n.get("expense.col.payment_method"),
                I18n.get("expense.col.sync")
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
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void refreshExpenses() {
        tableModel.setRowCount(0);
        List<Expense> expenses = expenseDAO.getAllExpenses();
        for (Expense exp : expenses) {
            tableModel.addRow(new Object[]{
                    exp.getId(),
                    exp.getExpenseDate() != null ? exp.getExpenseDate() : "-",
                    exp.getCategory(),
                    exp.getDescription() != null ? exp.getDescription() : "",
                    String.format("%.2f", exp.getAmount()),
                    exp.getPaymentMethod(),
                    exp.getSyncStatus()
            });
        }

        double todayTotal = expenseDAO.getTodayExpensesAmount();
        double totalAmt = expenseDAO.getTotalExpensesAmount();
        int totalCnt = expenseDAO.getTotalExpensesCount();

        todayExpensesValue.setText("\u20B9" + String.format("%.2f", todayTotal));
        totalExpensesValue.setText("\u20B9" + String.format("%.2f", totalAmt));
        totalCountValue.setText(String.valueOf(totalCnt));
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        String selectedCategory = (String) categoryFilterBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();

        if (text != null && !text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + text));
        }

        if (selectedCategory != null && !selectedCategory.isEmpty() && !"ALL".equalsIgnoreCase(selectedCategory)) {
            filters.add(RowFilter.regexFilter("(?i)^" + selectedCategory + "$", 2));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void handleAddExpense() {
        AddExpenseDialog dialog = new AddExpenseDialog();
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshExpenses();
        }
    }

    private void handleEditExpense() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, I18n.get("expense.msg.select_first"), I18n.get("expense.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selected);
        int expenseId = (int) tableModel.getValueAt(modelRow, 0);

        Expense exp = expenseDAO.getExpenseById(expenseId);
        if (exp != null) {
            EditExpenseDialog dialog = new EditExpenseDialog(exp);
            dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshExpenses();
            }
        }
    }

    private void handleDeleteExpense() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, I18n.get("expense.msg.select_first"), I18n.get("expense.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selected);
        int expenseId = (int) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                I18n.get("expense.msg.delete_confirm", expenseId),
                I18n.get("expense.msg.delete_title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = expenseDAO.deleteExpense(expenseId);
                if (success) {
                    JOptionPane.showMessageDialog(this, I18n.get("expense.msg.delete_success"), I18n.get("expense.title"), JOptionPane.INFORMATION_MESSAGE);
                    refreshExpenses();
                } else {
                    JOptionPane.showMessageDialog(this, I18n.get("expense.msg.delete_failed"), I18n.get("expense.title"), JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, I18n.get("expense.msg.delete_failed") + ": " + ex.getMessage(), I18n.get("expense.title"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onLocaleChange() {
        titleLabel.setText(I18n.get("expense.title"));
        subtitleLabel.setText(I18n.get("expense.subtitle"));
        refreshButton.setText("↻  " + I18n.get("expense.btn.refresh"));

        todayExpensesHeading.setText(I18n.get("expense.card.today"));
        todayExpensesDesc.setText(I18n.get("expense.card.today_desc"));
        totalExpensesHeading.setText(I18n.get("expense.card.total"));
        totalExpensesDesc.setText(I18n.get("expense.card.total_desc"));
        totalCountHeading.setText(I18n.get("expense.card.count"));
        totalCountDesc.setText(I18n.get("expense.card.count_desc"));

        addExpenseBtn.setText("＋  " + I18n.get("expense.btn.add"));
        editExpenseBtn.setText(I18n.get("expense.btn.edit"));
        deleteExpenseBtn.setText(I18n.get("expense.btn.delete"));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("expense.search_placeholder"));

        refreshExpenses();
    }
}
