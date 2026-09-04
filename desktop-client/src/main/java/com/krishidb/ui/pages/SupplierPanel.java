package com.krishidb.ui.pages;

import com.krishidb.dao.SupplierDAO;
import com.krishidb.model.Supplier;
import com.krishidb.ui.dialogs.AddSupplierDialog;
import com.krishidb.ui.dialogs.EditSupplierDialog;
import com.krishidb.ui.dialogs.SupplierDetailsDialog;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class SupplierPanel extends JPanel implements I18n.LocaleChangeListener {

    private final SupplierDAO supplierDAO;

    private DefaultTableModel tableModel;
    private JTable supplierTable;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton viewButton;
    private JButton refreshButton;

    private JLabel totalSuppliersHeading;
    private JLabel totalSuppliersDesc;
    private JLabel pendingSyncHeading;
    private JLabel pendingSyncDesc;
    private JLabel activeLocationsHeading;
    private JLabel activeLocationsDesc;

    private JLabel totalSuppliersValue;
    private JLabel pendingSyncValue;
    private JLabel activeLocationsValue;

    private JLabel tableTitleLabel;
    private JTextField searchField;

    public SupplierPanel() {
        supplierDAO = new SupplierDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(35, 40, 35, 40));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        refreshSuppliers();
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

        titleLabel = new JLabel(I18n.get("supplier.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("supplier.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titleArea.add(titleLabel);
        titleArea.add(Box.createVerticalStrut(6));
        titleArea.add(subtitleLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        addButton = new JButton(I18n.get("supplier.btn.add"));
        editButton = new JButton(I18n.get("supplier.btn.edit"));
        deleteButton = new JButton(I18n.get("supplier.btn.delete"));
        viewButton = new JButton(I18n.get("supplier.btn.view"));
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
            AddSupplierDialog dialog = new AddSupplierDialog();
            dialog.setVisible(true);
            refreshSuppliers();
        });

        editButton.addActionListener(e -> editSelectedSupplier());
        deleteButton.addActionListener(e -> deleteSelectedSupplier());
        viewButton.addActionListener(e -> viewSelectedSupplierDetails());
        refreshButton.addActionListener(e -> refreshSuppliers());

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

        totalSuppliersValue = new JLabel("0");
        pendingSyncValue = new JLabel("0");
        activeLocationsValue = new JLabel("0");

        totalSuppliersHeading = createCardHeader(I18n.get("supplier.card.total"));
        totalSuppliersDesc = createCardDesc(I18n.get("supplier.card.total_desc"));

        pendingSyncHeading = createCardHeader(I18n.get("supplier.card.pending_sync"));
        pendingSyncDesc = createCardDesc(I18n.get("supplier.card.pending_sync_desc"));

        activeLocationsHeading = createCardHeader(I18n.get("supplier.card.active_locations"));
        activeLocationsDesc = createCardDesc(I18n.get("supplier.card.active_locations_desc"));

        summaryPanel.add(buildSummaryCard(totalSuppliersHeading, totalSuppliersValue, totalSuppliersDesc));
        summaryPanel.add(buildSummaryCard(pendingSyncHeading, pendingSyncValue, pendingSyncDesc));
        summaryPanel.add(buildSummaryCard(activeLocationsHeading, activeLocationsValue, activeLocationsDesc));

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
                I18n.get("supplier.col.id"),
                I18n.get("supplier.col.name"),
                I18n.get("supplier.col.phone"),
                I18n.get("supplier.col.village"),
                I18n.get("supplier.col.created_at"),
                I18n.get("supplier.col.sync")
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

        tableTitleLabel = new JLabel(I18n.get("supplier.table.title"));
        tableTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("supplier.search_placeholder"));
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

        supplierTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        supplierTable.setRowSorter(sorter);
        supplierTable.setRowHeight(42);
        supplierTable.setShowVerticalLines(false);
        supplierTable.setFillsViewportHeight(true);
        supplierTable.getTableHeader().setReorderingAllowed(false);

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

        JScrollPane scrollPane = new JScrollPane(supplierTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        section.add(tableHeader, BorderLayout.NORTH);
        section.add(scrollPane, BorderLayout.CENTER);

        return section;
    }

    // -------------------------------------------------
    // LOAD DATA FROM SQLITE
    // -------------------------------------------------
    public void refreshSuppliers() {
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        tableModel.setRowCount(0);

        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    s.getPhone(),
                    s.getVillage() != null ? s.getVillage() : "-",
                    s.getCreatedAt(),
                    s.getSyncStatus()
            });
        }

        totalSuppliersValue.setText(String.valueOf(suppliers.size()));
        pendingSyncValue.setText(String.valueOf(supplierDAO.getPendingCount()));
        activeLocationsValue.setText(String.valueOf(supplierDAO.getDistinctVillageCount()));
    }

    private void deleteSelectedSupplier() {
        int row = supplierTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, I18n.get("supplier.msg.select_first"));
            return;
        }

        int modelRow = supplierTable.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        // Check foreign key constraint for purchases
        int linkedPurchases = supplierDAO.getLinkedPurchasesCount(id);
        if (linkedPurchases > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    I18n.get("supplier.msg.delete_blocked_purchases", name, linkedPurchases),
                    I18n.get("supplier.msg.delete_title"),
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                I18n.get("supplier.msg.delete_confirm", name),
                I18n.get("supplier.msg.delete_title"),
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = supplierDAO.deleteSupplier(id);
        if (deleted) {
            JOptionPane.showMessageDialog(this, I18n.get("supplier.msg.delete_success"));
            refreshSuppliers();
        } else {
            JOptionPane.showMessageDialog(this, I18n.get("supplier.msg.delete_failed"));
        }
    }

    private void editSelectedSupplier() {
        int row = supplierTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, I18n.get("supplier.msg.select_first"));
            return;
        }

        int modelRow = supplierTable.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        Supplier selected = supplierDAO.getSupplierById(id);
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Supplier not found");
            return;
        }

        new EditSupplierDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected);
        refreshSuppliers();
    }

    private void viewSelectedSupplierDetails() {
        int row = supplierTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, I18n.get("supplier.msg.select_first"));
            return;
        }

        int modelRow = supplierTable.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        Supplier selected = supplierDAO.getSupplierById(id);
        if (selected != null) {
            new SupplierDetailsDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected);
        }
    }

    @Override
    public void onLocaleChange() {
        titleLabel.setText(I18n.get("supplier.title"));
        subtitleLabel.setText(I18n.get("supplier.subtitle"));
        addButton.setText(I18n.get("supplier.btn.add"));
        editButton.setText(I18n.get("supplier.btn.edit"));
        deleteButton.setText(I18n.get("supplier.btn.delete"));
        viewButton.setText(I18n.get("supplier.btn.view"));

        totalSuppliersHeading.setText(I18n.get("supplier.card.total"));
        totalSuppliersDesc.setText(I18n.get("supplier.card.total_desc"));
        pendingSyncHeading.setText(I18n.get("supplier.card.pending_sync"));
        pendingSyncDesc.setText(I18n.get("supplier.card.pending_sync_desc"));
        activeLocationsHeading.setText(I18n.get("supplier.card.active_locations"));
        activeLocationsDesc.setText(I18n.get("supplier.card.active_locations_desc"));

        tableTitleLabel.setText(I18n.get("supplier.table.title"));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("supplier.search_placeholder"));

        String[] columns = getColumnNames();
        for (int i = 0; i < columns.length; i++) {
            supplierTable.getColumnModel().getColumn(i).setHeaderValue(columns[i]);
        }
        supplierTable.getTableHeader().repaint();

        revalidate();
        repaint();
    }
}
