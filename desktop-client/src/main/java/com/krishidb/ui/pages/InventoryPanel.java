package com.krishidb.ui.pages;

import com.krishidb.dao.ProductDAO;
import com.krishidb.model.Product;
import com.krishidb.ui.dialogs.AddProductDialog;
import com.krishidb.ui.dialogs.EditProductDialog;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel implements I18n.LocaleChangeListener {

    private final ProductDAO productDAO;

    private DefaultTableModel tableModel;
    private JTable productTable;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;

    private JLabel totalProductsHeading;
    private JLabel totalProductsDesc;
    private JLabel lowStockHeading;
    private JLabel lowStockDesc;
    private JLabel inventoryValueHeading;
    private JLabel inventoryValueDesc;

    private JLabel totalProductsValue;
    private JLabel lowStockValue;
    private JLabel inventoryValue;

    private JLabel tableTitleLabel;
    private JTextField searchField;

    public InventoryPanel() {
        productDAO = new ProductDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(35, 40, 35, 40));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        refreshInventory();
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

        titleLabel = new JLabel(I18n.get("inventory.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("inventory.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titleArea.add(titleLabel);
        titleArea.add(Box.createVerticalStrut(6));
        titleArea.add(subtitleLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        buttonPanel.setOpaque(false);

        addButton = new JButton(I18n.get("inventory.btn.add"));
        editButton = new JButton(I18n.get("inventory.btn.edit"));
        deleteButton = new JButton(I18n.get("inventory.btn.delete"));
        refreshButton = new JButton(I18n.get("inventory.btn.refresh"));

        addButton.setPreferredSize(new Dimension(160, 42));
        editButton.setPreferredSize(new Dimension(90, 42));
        deleteButton.setPreferredSize(new Dimension(90, 42));
        refreshButton.setPreferredSize(new Dimension(180, 42));

        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addButton.putClientProperty("JButton.buttonType", "roundRect");
        editButton.putClientProperty("JButton.buttonType", "roundRect");
        deleteButton.putClientProperty("JButton.buttonType", "roundRect");
        refreshButton.putClientProperty("JButton.buttonType", "roundRect");

        addButton.addActionListener(e -> {
            AddProductDialog dialog = new AddProductDialog();
            dialog.setVisible(true);
            refreshInventory();
        });

        editButton.addActionListener(e -> editSelectedProduct());
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        refreshButton.addActionListener(e -> refreshInventory());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        header.add(titleArea, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);

        return header;
    }

    // -------------------------------------------------
    // MAIN CONTENT
    // -------------------------------------------------
    private JPanel createContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BorderLayout(0, 25));

        // SUMMARY CARDS
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setOpaque(false);

        totalProductsValue = new JLabel("0");
        lowStockValue = new JLabel("0");
        inventoryValue = new JLabel("₹0.00");

        totalProductsHeading = createCardHeader(I18n.get("inventory.card.total_products"));
        totalProductsDesc = createCardDesc(I18n.get("inventory.card.total_products_desc"));

        lowStockHeading = createCardHeader(I18n.get("inventory.card.low_stock"));
        lowStockDesc = createCardDesc(I18n.get("inventory.card.low_stock_desc"));

        inventoryValueHeading = createCardHeader(I18n.get("inventory.card.inventory_value"));
        inventoryValueDesc = createCardDesc(I18n.get("inventory.card.inventory_value_desc"));

        summaryPanel.add(buildSummaryCard(totalProductsHeading, totalProductsValue, totalProductsDesc));
        summaryPanel.add(buildSummaryCard(lowStockHeading, lowStockValue, lowStockDesc));
        summaryPanel.add(buildSummaryCard(inventoryValueHeading, inventoryValue, inventoryValueDesc));

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
                I18n.get("inventory.col.id"),
                I18n.get("inventory.col.product"),
                I18n.get("inventory.col.category"),
                I18n.get("inventory.col.unit"),
                I18n.get("inventory.col.selling_price"),
                I18n.get("inventory.col.stock"),
                I18n.get("inventory.col.low_stock_level"),
                I18n.get("inventory.col.sync")
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

        tableTitleLabel = new JLabel(I18n.get("inventory.table.title"));
        tableTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("inventory.search_placeholder"));
        searchField.setPreferredSize(new Dimension(260, 38));

        tableHeader.add(tableTitleLabel, BorderLayout.WEST);
        tableHeader.add(searchField, BorderLayout.EAST);

        String[] columns = getColumnNames();
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        productTable.setRowSorter(sorter);
        productTable.setRowHeight(42);
        productTable.setShowVerticalLines(false);
        productTable.setFillsViewportHeight(true);
        productTable.getTableHeader().setReorderingAllowed(false);

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

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        section.add(tableHeader, BorderLayout.NORTH);
        section.add(scrollPane, BorderLayout.CENTER);

        return section;
    }

    // -------------------------------------------------
    // LOAD DATA FROM SQLITE
    // -------------------------------------------------
    public void refreshInventory() {
        List<Product> products = productDAO.getAllProducts();
        tableModel.setRowCount(0);

        int lowStockCount = 0;
        double totalInventoryValue = 0;

        for (Product product : products) {
            if (product.getStockQuantity() <= product.getLowStockLevel()) {
                lowStockCount++;
            }
            totalInventoryValue += product.getStockQuantity() * product.getSellingPrice();

            tableModel.addRow(new Object[]{
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getUnit(),
                    String.format("₹%.2f", product.getSellingPrice()),
                    product.getStockQuantity(),
                    product.getLowStockLevel(),
                    product.getSyncStatus()
            });
        }

        totalProductsValue.setText(String.valueOf(products.size()));
        lowStockValue.setText(String.valueOf(lowStockCount));
        inventoryValue.setText(String.format("₹%,.2f", totalInventoryValue));
    }

    private void deleteSelectedProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, I18n.get("inventory.msg.select_product"));
            return;
        }

        int modelRow = productTable.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        int choice = JOptionPane.showConfirmDialog(
                this,
                I18n.get("inventory.msg.delete_confirm"),
                I18n.get("inventory.msg.delete_title"),
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = productDAO.deleteProduct(id);
        if (deleted) {
            JOptionPane.showMessageDialog(this, I18n.get("inventory.msg.delete_success"));
            refreshInventory();
        } else {
            JOptionPane.showMessageDialog(this, I18n.get("inventory.msg.delete_failed"));
        }
    }

    private void editSelectedProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, I18n.get("inventory.msg.select_product"));
            return;
        }

        int modelRow = productTable.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        List<Product> products = productDAO.getAllProducts();
        Product selectedProduct = null;
        for (Product product : products) {
            if (product.getId() == id) {
                selectedProduct = product;
                break;
            }
        }

        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, I18n.get("inventory.msg.product_not_found"));
            return;
        }

        new EditProductDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                selectedProduct
        );

        refreshInventory();
    }

    @Override
    public void onLocaleChange() {
        titleLabel.setText(I18n.get("inventory.title"));
        subtitleLabel.setText(I18n.get("inventory.subtitle"));
        addButton.setText(I18n.get("inventory.btn.add"));
        editButton.setText(I18n.get("inventory.btn.edit"));
        deleteButton.setText(I18n.get("inventory.btn.delete"));
        refreshButton.setText(I18n.get("inventory.btn.refresh"));

        totalProductsHeading.setText(I18n.get("inventory.card.total_products"));
        totalProductsDesc.setText(I18n.get("inventory.card.total_products_desc"));
        lowStockHeading.setText(I18n.get("inventory.card.low_stock"));
        lowStockDesc.setText(I18n.get("inventory.card.low_stock_desc"));
        inventoryValueHeading.setText(I18n.get("inventory.card.inventory_value"));
        inventoryValueDesc.setText(I18n.get("inventory.card.inventory_value_desc"));

        tableTitleLabel.setText(I18n.get("inventory.table.title"));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("inventory.search_placeholder"));

        String[] columns = getColumnNames();
        for (int i = 0; i < columns.length; i++) {
            productTable.getColumnModel().getColumn(i).setHeaderValue(columns[i]);
        }
        productTable.getTableHeader().repaint();

        revalidate();
        repaint();
    }
}