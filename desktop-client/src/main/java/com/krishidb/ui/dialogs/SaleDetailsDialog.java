package com.krishidb.ui.dialogs;

import com.krishidb.model.Sale;
import com.krishidb.model.SaleItem;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SaleDetailsDialog extends JDialog {

    public SaleDetailsDialog(Window parent, Sale sale) {
        super(parent, I18n.get("sale.dialog.receipt_title"), ModalityType.APPLICATION_MODAL);

        setSize(560, 620);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 250, 252));

        // ---------------- HEADER ----------------
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(22, 25, 18, 25));

        JLabel title = new JLabel(I18n.get("sale.dialog.receipt_header"));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        String customerDisplay = sale.getCustomerName() != null && !sale.getCustomerName().isEmpty()
                ? sale.getCustomerName()
                : I18n.get("sale.label.walkin_customer");

        JLabel sub = new JLabel(I18n.get("sale.dialog.receipt_no", sale.getId()) + "  |  " +
                I18n.get("sale.dialog.receipt_date", sale.getSaleDate() != null ? sale.getSaleDate() : "-"));
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

        addMetaLabel(infoGrid, I18n.get("sale.dialog.receipt_customer", customerDisplay));
        addMetaLabel(infoGrid, I18n.get("sale.dialog.receipt_payment", sale.getPaymentMethod()));
        addMetaLabel(infoGrid, I18n.get("sale.table.col.sync") + ": " + sale.getSyncStatus());
        addMetaLabel(infoGrid, I18n.get("sale.dialog.receipt_notes",
                (sale.getNotes() != null && !sale.getNotes().isEmpty() ? sale.getNotes() : "-")));

        contentPanel.add(infoGrid, BorderLayout.NORTH);

        // Items Table
        String[] columns = {
                I18n.get("sale.cart.col.num"),
                I18n.get("sale.cart.col.product"),
                I18n.get("sale.cart.col.rate"),
                I18n.get("sale.cart.col.quantity"),
                I18n.get("sale.cart.col.subtotal")
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        if (sale.getItems() != null) {
            int idx = 1;
            for (SaleItem item : sale.getItems()) {
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
        table.getColumnModel().getColumn(0).setPreferredWidth(40);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Total Footer Card
        JPanel totalCard = new JPanel(new BorderLayout());
        totalCard.setBackground(Color.WHITE);
        totalCard.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel totalText = new JLabel(I18n.get("sale.dialog.receipt_total", String.format("%.2f", sale.getTotalAmount())));
        totalText.setFont(new Font("SansSerif", Font.BOLD, 18));
        totalText.setForeground(new Color(15, 23, 42));
        totalCard.add(totalText, BorderLayout.EAST);

        contentPanel.add(totalCard, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        // ---------------- DIALOG FOOTER ----------------
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(5, 20, 15, 20));

        JButton closeBtn = new JButton(I18n.get("sale.dialog.receipt_close"));
        closeBtn.setPreferredSize(new Dimension(100, 38));
        closeBtn.addActionListener(e -> dispose());

        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void addMetaLabel(JPanel parent, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(51, 65, 85));
        parent.add(label);
    }
}
