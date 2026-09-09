package com.krishidb.ui.dialogs;

import com.krishidb.dao.CustomerDAO;
import com.krishidb.dao.SupplierDAO;
import com.krishidb.dao.TransactionDAO;
import com.krishidb.model.Customer;
import com.krishidb.model.Supplier;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class RecordPaymentDialog extends JDialog {

    private JComboBox<String> paymentTypeBox; // "CUSTOMER", "SUPPLIER"
    private JComboBox<EntityOption> entityBox;
    private JLabel balanceInfoLabel;
    private JTextField amountField;
    private JComboBox<String> paymentMethodBox;
    private JTextField notesField;

    private final CustomerDAO customerDAO;
    private final SupplierDAO supplierDAO;
    private final TransactionDAO transactionDAO;
    private boolean saved = false;

    public RecordPaymentDialog() {
        setTitle(I18n.get("transaction.dialog.payment_title"));
        setSize(500, 560);
        setLocationRelativeTo(null);
        setModal(true);

        this.customerDAO = new CustomerDAO();
        this.supplierDAO = new SupplierDAO();
        this.transactionDAO = new TransactionDAO();

        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 250, 252));

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);

        loadEntities();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 15, 30));

        JLabel title = new JLabel(I18n.get("transaction.dialog.payment_header"));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel(I18n.get("transaction.dialog.payment_subheader"));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 116, 139));

        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);

        return panel;
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        String[] types = {
                I18n.get("transaction.dialog.type_customer"),
                I18n.get("transaction.dialog.type_supplier")
        };
        paymentTypeBox = new JComboBox<>(types);
        paymentTypeBox.addActionListener(e -> loadEntities());

        entityBox = new JComboBox<>();
        entityBox.addActionListener(e -> updateBalanceInfo());

        balanceInfoLabel = new JLabel(" ");
        balanceInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        balanceInfoLabel.setForeground(new Color(180, 83, 9));

        amountField = new JTextField();

        String[] paymentModes = {"CASH", "UPI", "BANK"};
        paymentMethodBox = new JComboBox<>(paymentModes);

        notesField = new JTextField();

        int row = 0;
        addField(form, gbc, row++, I18n.get("transaction.dialog.party_type"), paymentTypeBox);
        addField(form, gbc, row++, I18n.get("transaction.dialog.select_party"), entityBox);
        addField(form, gbc, row++, I18n.get("transaction.dialog.current_balance"), balanceInfoLabel);
        addField(form, gbc, row++, I18n.get("transaction.dialog.amount"), amountField);
        addField(form, gbc, row++, I18n.get("transaction.dialog.payment_method"), paymentMethodBox);
        addField(form, gbc, row++, I18n.get("transaction.dialog.notes"), notesField);

        return form;
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(51, 65, 85));
        form.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        field.setPreferredSize(new Dimension(0, 32));
        form.add(field, gbc);
    }

    private void loadEntities() {
        entityBox.removeAllItems();
        boolean isCustomer = paymentTypeBox.getSelectedIndex() == 0;

        if (isCustomer) {
            List<Customer> customers = customerDAO.getAllCustomers();
            for (Customer c : customers) {
                entityBox.addItem(new EntityOption(c.getId(), c.getName(), c.getOutstandingBalance()));
            }
        } else {
            List<Supplier> suppliers = supplierDAO.getAllSuppliers();
            for (Supplier s : suppliers) {
                entityBox.addItem(new EntityOption(s.getId(), s.getName(), s.getOutstandingBalance()));
            }
        }
        updateBalanceInfo();
    }

    private void updateBalanceInfo() {
        EntityOption opt = (EntityOption) entityBox.getSelectedItem();
        if (opt != null) {
            balanceInfoLabel.setText("\u20B9" + String.format("%.2f", opt.balance));
        } else {
            balanceInfoLabel.setText("\u20B90.00");
        }
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 20, 15, 20));

        JButton cancelBtn = new JButton(I18n.get("transaction.btn.cancel"));
        cancelBtn.setPreferredSize(new Dimension(95, 36));
        cancelBtn.setBackground(new Color(241, 245, 249));
        cancelBtn.setForeground(new Color(71, 85, 105));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(I18n.get("transaction.btn.record_payment"));
        saveBtn.setPreferredSize(new Dimension(150, 36));
        saveBtn.setBackground(new Color(22, 101, 52));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> handleRecord());

        panel.add(cancelBtn);
        panel.add(saveBtn);
        return panel;
    }

    private void handleRecord() {
        EntityOption opt = (EntityOption) entityBox.getSelectedItem();
        if (opt == null || opt.id <= 0) {
            JOptionPane.showMessageDialog(this, I18n.get("transaction.msg.select_party_first"), I18n.get("transaction.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        String amtText = amountField.getText().trim();
        double amt;
        try {
            amt = Double.parseDouble(amtText);
            if (amt <= 0) {
                JOptionPane.showMessageDialog(this, I18n.get("transaction.msg.invalid_amount"), I18n.get("transaction.title"), JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, I18n.get("transaction.msg.invalid_amount"), I18n.get("transaction.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        String payMethod = (String) paymentMethodBox.getSelectedItem();
        String notes = notesField.getText().trim();
        boolean isCustomer = paymentTypeBox.getSelectedIndex() == 0;

        try {
            boolean success;
            if (isCustomer) {
                success = transactionDAO.recordCustomerPayment(opt.id, amt, payMethod, notes);
            } else {
                success = transactionDAO.recordSupplierPayment(opt.id, amt, payMethod, notes);
            }

            if (success) {
                saved = true;
                JOptionPane.showMessageDialog(this, I18n.get("transaction.msg.payment_recorded"), I18n.get("transaction.title"), JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, I18n.get("transaction.msg.payment_failed"), I18n.get("transaction.title"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.get("transaction.msg.payment_failed") + ": " + ex.getMessage(), I18n.get("transaction.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public static class EntityOption {
        public final int id;
        public final String name;
        public final double balance;

        public EntityOption(int id, String name, double balance) {
            this.id = id;
            this.name = name;
            this.balance = balance;
        }

        @Override
        public String toString() {
            return name + " (Bal: \u20B9" + String.format("%.2f", balance) + ")";
        }
    }
}
