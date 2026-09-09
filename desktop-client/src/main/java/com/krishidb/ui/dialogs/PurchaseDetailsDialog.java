package com.krishidb.ui.dialogs;

import com.krishidb.model.Purchase;
import com.krishidb.model.PurchaseItem;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PurchaseDetailsDialog extends JDialog {

    public PurchaseDetailsDialog(Window parent, Purchase purchase) {
        super(parent, I18n.get("purchase.dialog.details_title"), ModalityType.APPLICATION_MODAL);

        setSize(580, 640);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 250, 252));

        // ---------------- HEADER ----------------
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(22, 25, 18, 25));

        JLabel title = new JLabel(I18n.get("purchase.dialog.receipt_header"));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        String supplierDisplay = purchase.getSupplierName() != null && !purchase.getSupplierName().isEmpty()
                ? purchase.getSupplierName()
                : "-";

        String invoiceDisplay = purchase.getInvoiceNumber() != null && !purchase.getInvoiceNumber().isEmpty()
                ? purchase.getInvoiceNumber()
                : ("PUR-" + purchase.getId());

        JLabel sub = new JLabel(I18n.get("purchase.dialog.receipt_no", invoiceDisplay) + "  |  " +
                I18n.get("purchase.dialog.receipt_date", purchase.getPurchaseDate() != null ? purchase.getPurchaseDate() : "-"));
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(new Color(100, 116, 139));

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(sub);
        add(header, BorderLayout.NORTH);

        // ---------------- CENTER (DETAILS & ITEMS) ----------------
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        // Top info grid
        JPanel infoGrid = new JPanel(new GridLayout(2, 2, 15, 8));
        infoGrid.setBackground(Color.WHITE);
        infoGrid.setBorder(new EmptyBorder(12, 16, 12, 16));

        addMetaLabel(infoGrid, I18n.get("purchase.dialog.supplier", supplierDisplay));
        addMetaLabel(infoGrid, I18n.get("purchase.dialog.payment_mode", purchase.getPaymentMethod()));
        addMetaLabel(infoGrid, I18n.get("purchase.col.sync") + ": " + purchase.getSyncStatus());
        addMetaLabel(infoGrid, I18n.get("purchase.dialog.notes",
                (purchase.getNotes() != null && !purchase.getNotes().isEmpty() ? purchase.getNotes() : "-")));

        contentPanel.add(infoGrid, BorderLayout.NORTH);

        // Items Table
        String[] columns = {
                "#",
                I18n.get("purchase.cart.col.product"),
                I18n.get("purchase.cart.col.rate"),
                I18n.get("purchase.cart.col.quantity"),
                I18n.get("purchase.cart.col.subtotal")
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        if (purchase.getItems() != null) {
            int idx = 1;
            for (PurchaseItem item : purchase.getItems()) {
                model.addRow(new Object[]{
                        idx++,
                        item.getProductName() + (item.getUnit() != null ? " (" + item.getUnit() + ")" : ""),
                        String.format("%.2f", item.getPricePerUnit()),
                        String.format("%.2f", item.getQuantity()),
                        String.format("%.2f", item.getSubtotal())
                });
            }
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(45);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Total strip
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.setBorder(new EmptyBorder(8, 15, 8, 15));

        JLabel totalLabel = new JLabel(I18n.get("purchase.label.grand_total") + "  \u20B9" + String.format("%.2f", purchase.getTotalAmount()));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        totalLabel.setForeground(new Color(22, 101, 52));
        totalPanel.add(totalLabel);

        contentPanel.add(totalPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        // ---------------- FOOTER BUTTONS ----------------
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(10, 20, 15, 20));

        JButton closeBtn = new JButton(I18n.get("purchase.btn.close"));
        closeBtn.setPreferredSize(new Dimension(100, 36));
        closeBtn.setBackground(new Color(241, 245, 249));
        closeBtn.setForeground(new Color(71, 85, 105));
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        add(footer, BorderLayout.SOUTH);
    }

    private void addMetaLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(51, 65, 85));
        panel.add(label);
    }
}
