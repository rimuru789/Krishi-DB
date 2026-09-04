package com.krishidb.ui.dialogs;

import com.krishidb.dao.SupplierDAO;
import com.krishidb.model.Supplier;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditSupplierDialog extends JDialog {

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField villageField;

    private final Supplier supplier;
    private final SupplierDAO supplierDAO;

    public EditSupplierDialog(JFrame parent, Supplier supplier) {
        super(parent, I18n.get("supplier.dialog.edit_title"), true);
        this.supplier = supplier;
        this.supplierDAO = new SupplierDAO();

        setSize(480, 460);
        setLocationRelativeTo(parent);

        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 250, 252));

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(22, 28, 16, 28));

        JLabel title = new JLabel(I18n.get("supplier.dialog.edit_header"));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel(I18n.get("supplier.col.id") + ": #" + supplier.getId() + " | " + I18n.get("supplier.col.sync") + ": " + supplier.getSyncStatus());
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 116, 139));

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);

        return panel;
    }

    private JPanel createForm() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(248, 250, 252));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 28, 20, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(supplier.getName());
        phoneField = new JTextField(supplier.getPhone());
        villageField = new JTextField(supplier.getVillage() != null ? supplier.getVillage() : "");

        nameField.setPreferredSize(new Dimension(240, 38));
        phoneField.setPreferredSize(new Dimension(240, 38));
        villageField.setPreferredSize(new Dimension(240, 38));

        int row = 0;
        addField(form, gbc, row++, I18n.get("supplier.dialog.name") + " *", nameField);
        addField(form, gbc, row++, I18n.get("supplier.dialog.phone") + " *", phoneField);
        addField(form, gbc, row++, I18n.get("supplier.dialog.village"), villageField);

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
        panel.add(field, gbc);
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(8, 20, 16, 20));

        JButton cancel = new JButton(I18n.get("product.btn.cancel"));
        JButton save = new JButton(I18n.get("supplier.btn.save_changes"));

        cancel.putClientProperty("JButton.buttonType", "square");
        save.putClientProperty("JButton.buttonType", "square");

        cancel.setPreferredSize(new Dimension(100, 38));
        save.setPreferredSize(new Dimension(160, 38));
        save.setBackground(new Color(22, 101, 52));
        save.setForeground(Color.WHITE);

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> saveSupplierChanges());

        panel.add(cancel);
        panel.add(save);

        return panel;
    }

    private void saveSupplierChanges() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String village = villageField.getText().trim();

        if (name.length() < 2) {
            JOptionPane.showMessageDialog(this, I18n.get("supplier.msg.val_name_required"), I18n.get("supplier.dialog.edit_title"), JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (!phone.matches("^[6-9]\\d{9}$")) {
            JOptionPane.showMessageDialog(this, I18n.get("supplier.msg.val_phone_invalid"), I18n.get("supplier.dialog.edit_title"), JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }

        supplier.setName(name);
        supplier.setPhone(phone);
        supplier.setVillage(village);

        boolean success = supplierDAO.updateSupplier(supplier);
        if (success) {
            JOptionPane.showMessageDialog(this, I18n.get("supplier.msg.update_success"), I18n.get("supplier.dialog.edit_title"), JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, I18n.get("supplier.msg.update_failed"), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
