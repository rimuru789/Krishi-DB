package com.krishidb.ui.dialogs;

import com.krishidb.dao.SupplierDAO;
import com.krishidb.model.Supplier;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SupplierDetailsDialog extends JDialog {

    public SupplierDetailsDialog(JFrame parent, Supplier supplier) {
        super(parent, I18n.get("supplier.dialog.details_title"), true);

        SupplierDAO supplierDAO = new SupplierDAO();
        int linkedPurchases = supplierDAO.getLinkedPurchasesCount(supplier.getId());

        setSize(460, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 250, 252));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(20, 25, 15, 25));

        JLabel title = new JLabel(supplier.getName());
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel sub = new JLabel(I18n.get("supplier.col.id") + ": #" + supplier.getId() + " | " + supplier.getVillage());
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

        addDetailRow(body, I18n.get("supplier.dialog.phone"), supplier.getPhone());
        addDetailRow(body, I18n.get("supplier.dialog.village"), supplier.getVillage() != null ? supplier.getVillage() : "-");
        addDetailRow(body, I18n.get("supplier.col.created_at"), supplier.getCreatedAt());
        addDetailRow(body, "Updated", supplier.getUpdatedAt() != null ? supplier.getUpdatedAt() : "-");
        addDetailRow(body, I18n.get("supplier.col.sync"), supplier.getSyncStatus());
        addDetailRow(body, "Linked Purchases", String.valueOf(linkedPurchases));

        add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(6, 20, 14, 20));

        JButton closeBtn = new JButton(I18n.get("supplier.btn.close"));
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
