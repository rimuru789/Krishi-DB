package com.krishidb.ui.dialogs;

import javax.swing.*;
import java.awt.*;

import com.krishidb.dao.ProductDAO;
import com.krishidb.model.Product;
import com.krishidb.util.I18n;

public class EditProductDialog extends JDialog {

    private JTextField nameField;
    private JTextField categoryField;
    private JTextField unitField;
    private JTextField priceField;
    private JTextField stockField;
    private JTextField lowStockField;

    private final Product product;

    public EditProductDialog(JFrame parent, Product product) {
        super(parent, I18n.get("product.dialog.edit_title"), true);
        this.product = product;

        setSize(550, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(20, 20));

        // ---------------- FORM ----------------
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        nameField = new JTextField(product.getName());
        categoryField = new JTextField(product.getCategory());
        unitField = new JTextField(product.getUnit());
        priceField = new JTextField(String.valueOf(product.getSellingPrice()));
        stockField = new JTextField(String.valueOf(product.getStockQuantity()));
        lowStockField = new JTextField(String.valueOf(product.getLowStockLevel()));

        addField(form, I18n.get("product.dialog.name"), nameField, 0);
        addField(form, I18n.get("product.dialog.category"), categoryField, 1);
        addField(form, I18n.get("product.dialog.unit"), unitField, 2);
        addField(form, I18n.get("product.dialog.selling_price"), priceField, 3);
        addField(form, I18n.get("product.dialog.stock_quantity"), stockField, 4);
        addField(form, I18n.get("product.dialog.low_stock_level"), lowStockField, 5);

        add(form, BorderLayout.CENTER);

        // ---------------- BUTTONS ----------------
        JButton cancelButton = new JButton(I18n.get("product.btn.cancel"));
        JButton saveButton = new JButton(I18n.get("product.btn.save_changes"));

        cancelButton.putClientProperty("JButton.buttonType", "square");
        saveButton.putClientProperty("JButton.buttonType", "square");

        cancelButton.setPreferredSize(new Dimension(100, 40));
        saveButton.setPreferredSize(new Dimension(160, 40));

        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> saveProduct());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addField(JPanel panel, String label, JTextField field, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        field.setPreferredSize(new Dimension(250, 40));
        panel.add(field, gbc);
    }

    private void saveProduct() {
        try {
            if (nameField.getText().isBlank()
                    || categoryField.getText().isBlank()
                    || unitField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, I18n.get("product.msg.fill_all"));
                return;
            }

            double price = Double.parseDouble(priceField.getText().trim());
            double stock = Double.parseDouble(stockField.getText().trim());
            double lowStock = Double.parseDouble(lowStockField.getText().trim());

            if (price < 0 || stock < 0 || lowStock < 0) {
                JOptionPane.showMessageDialog(this, I18n.get("product.msg.non_negative"));
                return;
            }

            product.setName(nameField.getText().trim());
            product.setCategory(categoryField.getText().trim());
            product.setUnit(unitField.getText().trim());
            product.setSellingPrice(price);
            product.setStockQuantity(stock);
            product.setLowStockLevel(lowStock);

            ProductDAO dao = new ProductDAO();
            boolean updated = dao.updateProduct(product);

            if (updated) {
                JOptionPane.showMessageDialog(this, I18n.get("product.msg.update_success"));
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, I18n.get("product.msg.update_failed"));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, I18n.get("product.msg.valid_numbers"));
        }
    }
}