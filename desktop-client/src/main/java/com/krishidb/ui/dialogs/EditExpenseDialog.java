package com.krishidb.ui.dialogs;

import com.krishidb.dao.ExpenseDAO;
import com.krishidb.model.Expense;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditExpenseDialog extends JDialog {

    private final Expense expense;
    private final ExpenseDAO expenseDAO;

    private JComboBox<String> categoryBox;
    private JTextField descriptionField;
    private JTextField amountField;
    private JComboBox<String> paymentMethodBox;
    private boolean saved = false;

    public EditExpenseDialog(Expense expense) {
        this.expense = expense;
        this.expenseDAO = new ExpenseDAO();

        setTitle(I18n.get("expense.dialog.edit_title"));
        setSize(480, 520);
        setLocationRelativeTo(null);
        setModal(true);

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

        JLabel title = new JLabel(I18n.get("expense.dialog.edit_header"));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Expense #" + expense.getId() + " - " + (expense.getExpenseDate() != null ? expense.getExpenseDate() : ""));
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

        String[] categories = {
                "Transport", "Labor", "Electricity", "Rent",
                "Packaging", "Maintenance", "Fertilizer/Seed Handling", "Other"
        };
        categoryBox = new JComboBox<>(categories);
        categoryBox.setEditable(true);
        categoryBox.setSelectedItem(expense.getCategory());

        descriptionField = new JTextField(expense.getDescription() != null ? expense.getDescription() : "");
        amountField = new JTextField(String.format("%.2f", expense.getAmount()));

        String[] payments = {"CASH", "UPI", "BANK"};
        paymentMethodBox = new JComboBox<>(payments);
        paymentMethodBox.setSelectedItem(expense.getPaymentMethod());

        int row = 0;
        addField(form, gbc, row++, I18n.get("expense.dialog.category"), categoryBox);
        addField(form, gbc, row++, I18n.get("expense.dialog.description"), descriptionField);
        addField(form, gbc, row++, I18n.get("expense.dialog.amount"), amountField);
        addField(form, gbc, row++, I18n.get("expense.dialog.payment_method"), paymentMethodBox);

        return form;
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(51, 65, 85));
        form.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        field.setPreferredSize(new Dimension(0, 32));
        form.add(field, gbc);
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 20, 15, 20));

        JButton cancelBtn = new JButton(I18n.get("expense.btn.cancel"));
        cancelBtn.setPreferredSize(new Dimension(95, 36));
        cancelBtn.setBackground(new Color(241, 245, 249));
        cancelBtn.setForeground(new Color(71, 85, 105));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(I18n.get("expense.btn.save_changes"));
        saveBtn.setPreferredSize(new Dimension(130, 36));
        saveBtn.setBackground(new Color(30, 58, 138));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> handleUpdate());

        panel.add(cancelBtn);
        panel.add(saveBtn);
        return panel;
    }

    private void handleUpdate() {
        String cat = (String) categoryBox.getSelectedItem();
        if (cat == null || cat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, I18n.get("expense.msg.category_required"), I18n.get("expense.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        String amtText = amountField.getText().trim();
        double amt;
        try {
            amt = Double.parseDouble(amtText);
            if (amt <= 0) {
                JOptionPane.showMessageDialog(this, I18n.get("expense.msg.invalid_amount"), I18n.get("expense.title"), JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, I18n.get("expense.msg.invalid_amount"), I18n.get("expense.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        expense.setCategory(cat.trim());
        expense.setDescription(descriptionField.getText().trim());
        expense.setAmount(amt);
        expense.setPaymentMethod((String) paymentMethodBox.getSelectedItem());

        try {
            boolean success = expenseDAO.updateExpense(expense);
            if (success) {
                saved = true;
                JOptionPane.showMessageDialog(this, I18n.get("expense.msg.update_success"), I18n.get("expense.title"), JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, I18n.get("expense.msg.update_failed"), I18n.get("expense.title"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.get("expense.msg.update_failed") + ": " + ex.getMessage(), I18n.get("expense.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
