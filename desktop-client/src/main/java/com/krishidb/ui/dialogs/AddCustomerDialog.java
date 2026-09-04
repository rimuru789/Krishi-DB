package com.krishidb.ui.dialogs;

import com.krishidb.dao.CustomerDAO;
import com.krishidb.model.Customer;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddCustomerDialog extends JDialog {

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField villageField;

    private final CustomerDAO customerDAO;

    public AddCustomerDialog() {
        setTitle(I18n.get("customer.dialog.add_title"));
        setSize(480, 460);
        setLocationRelativeTo(null);
        setModal(true);

        customerDAO = new CustomerDAO();

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
        panel.setBorder(new EmptyBorder(22, 28, 16, 28));

        JLabel title = new JLabel(I18n.get("customer.dialog.add_header"));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel(I18n.get("customer.dialog.add_subheader"));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
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

        nameField = new JTextField();
        phoneField = new JTextField();
        villageField = new JTextField();

        nameField.setPreferredSize(new Dimension(240, 38));
        phoneField.setPreferredSize(new Dimension(240, 38));
        villageField.setPreferredSize(new Dimension(240, 38));

        int row = 0;
        addField(form, gbc, row++, I18n.get("customer.dialog.name") + " *", nameField);
        addField(form, gbc, row++, I18n.get("customer.dialog.phone") + " *", phoneField);
        addField(form, gbc, row++, I18n.get("customer.dialog.village"), villageField);

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
        JButton save = new JButton(I18n.get("customer.btn.save"));

        cancel.putClientProperty("JButton.buttonType", "square");
        save.putClientProperty("JButton.buttonType", "square");

        cancel.setPreferredSize(new Dimension(100, 38));
        save.setPreferredSize(new Dimension(150, 38));
        save.setBackground(new Color(22, 101, 52));
        save.setForeground(Color.WHITE);

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> saveCustomer());

        panel.add(cancel);
        panel.add(save);

        return panel;
    }

    private void saveCustomer() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String village = villageField.getText().trim();

        // 1. Validate Name
        if (name.length() < 2) {
            JOptionPane.showMessageDialog(this, I18n.get("customer.msg.val_name_required"), I18n.get("customer.dialog.add_title"), JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        // 2. Validate Indian Phone Number
        if (!phone.matches("^[6-9]\\d{9}$")) {
            JOptionPane.showMessageDialog(this, I18n.get("customer.msg.val_phone_invalid"), I18n.get("customer.dialog.add_title"), JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }

        Customer customer = new Customer(name, phone, village);
        boolean success = customerDAO.addCustomer(customer);

        if (success) {
            JOptionPane.showMessageDialog(this, I18n.get("customer.msg.add_success"), I18n.get("customer.dialog.add_title"), JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, I18n.get("customer.msg.update_failed"), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
