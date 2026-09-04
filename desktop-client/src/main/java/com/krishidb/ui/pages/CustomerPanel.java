package com.krishidb.ui.pages;

import com.krishidb.dao.CustomerDAO;
import com.krishidb.model.Customer;
import com.krishidb.ui.dialogs.AddCustomerDialog;
import com.krishidb.ui.dialogs.CustomerDetailsDialog;
import com.krishidb.ui.dialogs.EditCustomerDialog;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class CustomerPanel extends JPanel implements I18n.LocaleChangeListener {

    private final CustomerDAO customerDAO;

    private DefaultTableModel tableModel;
    private JTable customerTable;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton viewButton;
    private JButton refreshButton;

    private JLabel totalCustomersHeading;
    private JLabel totalCustomersDesc;
    private JLabel pendingSyncHeading;
    private JLabel pendingSyncDesc;
    private JLabel activeVillagesHeading;
    private JLabel activeVillagesDesc;

    private JLabel totalCustomersValue;
    private JLabel pendingSyncValue;
    private JLabel activeVillagesValue;

    private JLabel tableTitleLabel;
    private JTextField searchField;

    public CustomerPanel() {
        customerDAO = new CustomerDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(35, 40, 35, 40));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        refreshCustomers();
        I18n.addListener(this);
    }

    // -------------------------------------------------
    // HEADER
    // -------------------------------------------------
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 25, 0));

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));

        titleLabel = new JLabel(I18n.get("customer.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("customer.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titleArea.add(titleLabel);
        titleArea.add(Box.createVerticalStrut(6));
        titleArea.add(subtitleLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        addButton = new JButton(I18n.get("customer.btn.add"));
        editButton = new JButton(I18n.get("customer.btn.edit"));
        deleteButton = new JButton(I18n.get("customer.btn.delete"));
        viewButton = new JButton(I18n.get("customer.btn.view"));
        refreshButton = new JButton("↻");

        addButton.setPreferredSize(new Dimension(160, 42));
        editButton.setPreferredSize(new Dimension(90, 42));
        deleteButton.setPreferredSize(new Dimension(90, 42));
        viewButton.setPreferredSize(new Dimension(120, 42));
        refreshButton.setPreferredSize(new Dimension(50, 42));

        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addButton.putClientProperty("JButton.buttonType", "roundRect");
        editButton.putClientProperty("JButton.buttonType", "roundRect");
        deleteButton.putClientProperty("JButton.buttonType", "roundRect");
        viewButton.putClientProperty("JButton.buttonType", "roundRect");
        refreshButton.putClientProperty("JButton.buttonType", "roundRect");

        addButton.addActionListener(e -> {
            AddCustomerDialog dialog = new AddCustomerDialog();
            dialog.setVisible(true);
            refreshCustomers();
        });

        editButton.addActionListener(e -> editSelectedCustomer());
        deleteButton.addActionListener(e -> deleteSelectedCustomer());
        viewButton.addActionListener(e -> viewSelectedCustomerDetails());
        refreshButton.addActionListener(e -> refreshCustomers());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(refreshButton);

        header.add(titleArea, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);

        return header;
    }

    // -------------------------------------------------
    // MAIN CONTENT
    // -------------------------------------------------
    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 25));
        content.setOpaque(false);

        // SUMMARY CARDS
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setOpaque(false);

        totalCustomersValue = new JLabel("0");
        pendingSyncValue = new JLabel("0");
        activeVillagesValue = new JLabel("0");

        totalCustomersHeading = createCardHeader(I18n.get("customer.card.total"));
        totalCustomersDesc = createCardDesc(I18n.get("customer.card.total_desc"));

        pendingSyncHeading = createCardHeader(I18n.get("customer.card.pending_sync"));
        pendingSyncDesc = createCardDesc(I18n.get("customer.card.pending_sync_desc"));

        activeVillagesHeading = createCardHeader(I18n.get("customer.card.active_villages"));
        activeVillagesDesc = createCardDesc(I18n.get("customer.card.active_villages_desc"));

        summaryPanel.add(buildSummaryCard(totalCustomersHeading, totalCustomersValue, totalCustomersDesc));
        summaryPanel.add(buildSummaryCard(pendingSyncHeading, pendingSyncValue, pendingSyncDesc));
        summaryPanel.add(buildSummaryCard(activeVillagesHeading, activeVillagesValue, activeVillagesDesc));

        content.add(summaryPanel, BorderLayout.NORTH);
        content.add(createTableSection(), BorderLayout.CENTER);

        return content;
    }

    private JLabel createCardHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(new Color(100, 116, 139));
        return label;
    }

    private JLabel createCardDesc(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(new Color(148, 163, 184));
        return label;
    }

    private JPanel buildSummaryCard(JLabel heading, JLabel value, JLabel desc) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        value.setFont(new Font("SansSerif", Font.BOLD, 28));
        value.setForeground(new Color(15, 23, 42));

        card.add(heading);
        card.add(Box.createVerticalStrut(12));
        card.add(value);
        card.add(Box.createVerticalStrut(5));
        card.add(desc);

        return card;
    }

    private String[] getColumnNames() {
        return new String[]{
                I18n.get("customer.col.id"),
                I18n.get("customer.col.name"),
                I18n.get("customer.col.phone"),
                I18n.get("customer.col.village"),
                I18n.get("customer.col.created_at"),
                I18n.get("customer.col.sync")
        };
    }

    // -------------------------------------------------
    // TABLE
    // -------------------------------------------------
    private JPanel createTableSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(0, 0, 15, 0));

        tableTitleLabel = new JLabel(I18n.get("customer.table.title"));
        tableTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("customer.search_placeholder"));
        searchField.setPreferredSize(new Dimension(300, 38));

        tableHeader.add(tableTitleLabel, BorderLayout.WEST);
        tableHeader.add(searchField, BorderLayout.EAST);

        String[] columns = getColumnNames();
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        customerTable.setRowSorter(sorter);
        customerTable.setRowHeight(42);
        customerTable.setShowVerticalLines(false);
        customerTable.setFillsViewportHeight(true);
        customerTable.getTableHeader().setReorderingAllowed(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String text = searchField.getText();
                if (text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        section.add(tableHeader, BorderLayout.NORTH);
        section.add(scrollPane, BorderLayout.CENTER);

        return section;
    }

    // -------------------------------------------------
    // LOAD DATA FROM SQLITE
    // -------------------------------------------------
    public void refreshCustomers() {
        List<Customer> customers = customerDAO.getAllCustomers();
        tableModel.setRowCount(0);

        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getName(),
                    c.getPhone(),
                    c.getVillage() != null ? c.getVillage() : "-",
                    c.getCreatedAt(),
                    c.getSyncStatus()
            });
        }

        totalCustomersValue.setText(String.valueOf(customers.size()));
        pendingSyncValue.setText(String.valueOf(customerDAO.getPendingCount()));
        activeVillagesValue.setText(String.valueOf(customerDAO.getDistinctVillageCount()));
    }

    private void deleteSelectedCustomer() {
        int row = customerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, I18n.get("customer.msg.select_first"));
            return;
        }

        int modelRow = customerTable.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        // Check foreign key constraint for sales
        int linkedSales = customerDAO.getLinkedSalesCount(id);
        if (linkedSales > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    I18n.get("customer.msg.delete_blocked_sales", name, linkedSales),
                    I18n.get("customer.msg.delete_title"),
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                I18n.get("customer.msg.delete_confirm", name),
                I18n.get("customer.msg.delete_title"),
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = customerDAO.deleteCustomer(id);
        if (deleted) {
            JOptionPane.showMessageDialog(this, I18n.get("customer.msg.delete_success"));
            refreshCustomers();
        } else {
            JOptionPane.showMessageDialog(this, I18n.get("customer.msg.delete_failed"));
        }
    }

    private void editSelectedCustomer() {
        int row = customerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, I18n.get("customer.msg.select_first"));
            return;
        }

        int modelRow = customerTable.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        Customer selected = customerDAO.getCustomerById(id);
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Customer not found");
            return;
        }

        new EditCustomerDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected);
        refreshCustomers();
    }

    private void viewSelectedCustomerDetails() {
        int row = customerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, I18n.get("customer.msg.select_first"));
            return;
        }

        int modelRow = customerTable.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        Customer selected = customerDAO.getCustomerById(id);
        if (selected != null) {
            new CustomerDetailsDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected);
        }
    }

    @Override
    public void onLocaleChange() {
        titleLabel.setText(I18n.get("customer.title"));
        subtitleLabel.setText(I18n.get("customer.subtitle"));
        addButton.setText(I18n.get("customer.btn.add"));
        editButton.setText(I18n.get("customer.btn.edit"));
        deleteButton.setText(I18n.get("customer.btn.delete"));
        viewButton.setText(I18n.get("customer.btn.view"));

        totalCustomersHeading.setText(I18n.get("customer.card.total"));
        totalCustomersDesc.setText(I18n.get("customer.card.total_desc"));
        pendingSyncHeading.setText(I18n.get("customer.card.pending_sync"));
        pendingSyncDesc.setText(I18n.get("customer.card.pending_sync_desc"));
        activeVillagesHeading.setText(I18n.get("customer.card.active_villages"));
        activeVillagesDesc.setText(I18n.get("customer.card.active_villages_desc"));

        tableTitleLabel.setText(I18n.get("customer.table.title"));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("customer.search_placeholder"));

        String[] columns = getColumnNames();
        for (int i = 0; i < columns.length; i++) {
            customerTable.getColumnModel().getColumn(i).setHeaderValue(columns[i]);
        }
        customerTable.getTableHeader().repaint();

        revalidate();
        repaint();
    }
}
