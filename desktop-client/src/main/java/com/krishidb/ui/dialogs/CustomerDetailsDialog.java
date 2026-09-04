package com.krishidb.ui.dialogs;

import com.krishidb.dao.CustomerDAO;
import com.krishidb.model.Customer;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomerDetailsDialog extends JDialog {

    public CustomerDetailsDialog(JFrame parent, Customer customer) {
        super(parent, I18n.get("customer.dialog.details_title"), true);

        CustomerDAO customerDAO = new CustomerDAO();
        int linkedSales = customerDAO.getLinkedSalesCount(customer.getId());

        setSize(460, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 250, 252));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(20, 25, 15, 25));

        JLabel title = new JLabel(customer.getName());
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel sub = new JLabel(I18n.get("customer.col.id") + ": #" + customer.getId() + " | " + customer.getVillage());
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(new Color(100, 116, 139));

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        add(header, BorderLayout.NORTH);

        // Content
        JPanel body = new JPanel(new GridLayout(6, 2, 10, 10));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 25, 20, 25));

        addDetailRow(body, I18n.get("customer.dialog.phone"), customer.getPhone());
        addDetailRow(body, I18n.get("customer.dialog.village"), customer.getVillage() != null ? customer.getVillage() : "-");
        addDetailRow(body, I18n.get("customer.col.created_at"), customer.getCreatedAt());
        addDetailRow(body, "Updated", customer.getUpdatedAt() != null ? customer.getUpdatedAt() : "-");
        addDetailRow(body, I18n.get("customer.col.sync"), customer.getSyncStatus());
        addDetailRow(body, "Linked Sales", String.valueOf(linkedSales));

        add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(6, 20, 14, 20));

        JButton closeBtn = new JButton(I18n.get("customer.btn.close"));
        closeBtn.setPreferredSize(new Dimension(100, 36));
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));

        JLabel val = new JLabel(value != null ? value : "-");
        val.setFont(new Font("SansSerif", Font.PLAIN, 13));
        val.setForeground(new Color(15, 23, 42));

        panel.add(lbl);
        panel.add(val);
    }
}
