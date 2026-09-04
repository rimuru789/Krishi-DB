package com.krishidb.ui.dialogs;

import com.krishidb.dao.ProductDAO;
import com.krishidb.model.Product;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddProductDialog extends JDialog {

    private JTextField nameField;
    private JTextField categoryField;
    private JTextField unitField;

    private JTextField priceField;
    private JTextField stockField;
    private JTextField lowStockField;

    private final ProductDAO productDAO;

    public AddProductDialog() {
        setTitle(I18n.get("product.dialog.add_title"));
        setSize(500, 650);
        setLocationRelativeTo(null);
        setModal(true);

        productDAO = new ProductDAO();

        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 250, 252));

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 15, 30));

        JLabel title = new JLabel(I18n.get("product.dialog.add_header"));
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel(I18n.get("product.dialog.add_subheader"));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 116, 139));

        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);

        return panel;
    }

    private JPanel createForm() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(248, 250, 252));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        nameField = new JTextField();
        categoryField = new JTextField();
        unitField = new JTextField();
        priceField = new JTextField();
        stockField = new JTextField();
        lowStockField = new JTextField();

        int row = 0;
        addField(form, gbc, row++, I18n.get("product.dialog.name"), nameField);
        addField(form, gbc, row++, I18n.get("product.dialog.category"), categoryField);
        addField(form, gbc, row++, I18n.get("product.dialog.unit"), unitField);
        addField(form, gbc, row++, I18n.get("product.dialog.selling_price"), priceField);
        addField(form, gbc, row++, I18n.get("product.dialog.stock_quantity"), stockField);
        addField(form, gbc, row++, I18n.get("product.dialog.low_stock_level"), lowStockField);

        container.add(form, BorderLayout.CENTER);
        return container;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.35;

        JLabel text = new JLabel(label);
        text.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(text, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        field.setPreferredSize(new Dimension(250, 38));
        panel.add(field, gbc);
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton cancel = new JButton(I18n.get("product.btn.cancel"));
        JButton save = new JButton(I18n.get("product.btn.save"));

        cancel.putClientProperty("JButton.buttonType", "square");
        save.putClientProperty("JButton.buttonType", "square");

        cancel.setPreferredSize(new Dimension(100, 40));
        save.setPreferredSize(new Dimension(150, 40));

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> saveProduct());

        panel.add(cancel);
        panel.add(save);

        return panel;
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

            Product product = new Product(
                    nameField.getText().trim(),
                    categoryField.getText().trim(),
                    unitField.getText().trim(),
                    price,
                    stock,
                    lowStock
            );

            if (productDAO.addProduct(product)) {
                JOptionPane.showMessageDialog(this, I18n.get("product.msg.add_success"));
                dispose();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, I18n.get("product.msg.valid_numbers"));
        }
    }
}