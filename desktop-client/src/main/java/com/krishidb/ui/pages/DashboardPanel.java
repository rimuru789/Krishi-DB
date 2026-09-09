package com.krishidb.ui.pages;

import com.krishidb.dao.DashboardDAO;
import com.krishidb.dao.DashboardDAO.DashboardMetrics;
import com.krishidb.model.Product;
import com.krishidb.model.TransactionRecord;
import com.krishidb.ui.MainFrame;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DashboardPanel extends JPanel implements I18n.LocaleChangeListener {

    private final DashboardDAO dashboardDAO;
    private final MainFrame mainFrame;

    // Header
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton refreshButton;
    private JButton quickSaleBtn;
    private JButton quickPurchaseBtn;

    // Financial KPI Row
    private JLabel todaySalesVal;
    private JLabel todaySalesSub;
    private JLabel todaySalesTitle;

    private JLabel inventoryVal;
    private JLabel inventorySub;
    private JLabel inventoryTitle;

    private JLabel purchasesVal;
    private JLabel purchasesSub;
    private JLabel purchasesTitle;

    private JLabel expensesVal;
    private JLabel expensesSub;
    private JLabel expensesTitle;

    // Operational KPI Row
    private JLabel netVal;
    private JLabel netSub;
    private JLabel netTitle;

    private JLabel lowStockVal;
    private JLabel lowStockSub;
    private JLabel lowStockTitle;

    private JLabel customerKhataVal;
    private JLabel customerKhataSub;
    private JLabel customerKhataTitle;

    private JLabel supplierPayableVal;
    private JLabel supplierPayableSub;
    private JLabel supplierPayableTitle;

    // Tables
    private JLabel recentTxHeading;
    private DefaultTableModel txTableModel;
    private JTable txTable;

    private JLabel lowStockHeading;
    private DefaultTableModel lowStockTableModel;
    private JTable lowStockTable;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.dashboardDAO = new DashboardDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        I18n.addListener(this);
        refreshDashboard();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel titlesPanel = new JPanel();
        titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));
        titlesPanel.setOpaque(false);

        titleLabel = new JLabel(I18n.get("dashboard.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("dashboard.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titlesPanel.add(titleLabel);
        titlesPanel.add(Box.createVerticalStrut(4));
        titlesPanel.add(subtitleLabel);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        quickSaleBtn = new JButton("＋  " + I18n.get("nav.new_sale"));
        quickSaleBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        quickSaleBtn.setBackground(new Color(22, 101, 52));
        quickSaleBtn.setForeground(Color.WHITE);
        quickSaleBtn.setFocusPainted(false);
        quickSaleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quickSaleBtn.addActionListener(e -> {
            if (mainFrame != null) mainFrame.showPage("NEW_SALE");
        });

        quickPurchaseBtn = new JButton("＋  " + I18n.get("nav.new_entry"));
        quickPurchaseBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        quickPurchaseBtn.setBackground(new Color(30, 58, 138));
        quickPurchaseBtn.setForeground(Color.WHITE);
        quickPurchaseBtn.setFocusPainted(false);
        quickPurchaseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quickPurchaseBtn.addActionListener(e -> {
            if (mainFrame != null) mainFrame.showPage("PURCHASES");
        });

        refreshButton = new JButton("↻  " + I18n.get("dashboard.btn.refresh"));
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(new Color(15, 23, 42));
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 14, 8, 14)
        ));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> refreshDashboard());

        actionPanel.add(quickSaleBtn);
        actionPanel.add(quickPurchaseBtn);
        actionPanel.add(refreshButton);

        headerPanel.add(titlesPanel, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(createKpiGrid());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createTablesSplitArea());

        return panel;
    }

    private JPanel createKpiGrid() {
        JPanel kpiContainer = new JPanel();
        kpiContainer.setLayout(new BoxLayout(kpiContainer, BoxLayout.Y_AXIS));
        kpiContainer.setOpaque(false);

        // Row 1: Key Financials (4 cards)
        JPanel row1 = new JPanel(new GridLayout(1, 4, 15, 0));
        row1.setOpaque(false);
        row1.setPreferredSize(new Dimension(0, 92));

        // 1. Today's Sales
        JPanel cardTodaySales = createCardBase();
        todaySalesTitle = createCardTitle(I18n.get("dashboard.card.today_sales"));
        todaySalesVal = createCardValue("\u20B90.00", new Color(22, 101, 52));
        todaySalesSub = createCardSub("0 invoices today");
        cardTodaySales.add(todaySalesTitle);
        cardTodaySales.add(Box.createVerticalStrut(3));
        cardTodaySales.add(todaySalesVal);
        cardTodaySales.add(Box.createVerticalStrut(2));
        cardTodaySales.add(todaySalesSub);

        // 2. Inventory Valuation
        JPanel cardInventory = createCardBase();
        inventoryTitle = createCardTitle(I18n.get("dashboard.card.inventory_val"));
        inventoryVal = createCardValue("\u20B90.00", new Color(15, 23, 42));
        inventorySub = createCardSub("0 SKUs in stock");
        cardInventory.add(inventoryTitle);
        cardInventory.add(Box.createVerticalStrut(3));
        cardInventory.add(inventoryVal);
        cardInventory.add(Box.createVerticalStrut(2));
        cardInventory.add(inventorySub);

        // 3. Total Purchases
        JPanel cardPurchases = createCardBase();
        purchasesTitle = createCardTitle(I18n.get("dashboard.card.total_purchases"));
        purchasesVal = createCardValue("\u20B90.00", new Color(30, 58, 138));
        purchasesSub = createCardSub(I18n.get("dashboard.card.purchases_desc"));
        cardPurchases.add(purchasesTitle);
        cardPurchases.add(Box.createVerticalStrut(3));
        cardPurchases.add(purchasesVal);
        cardPurchases.add(Box.createVerticalStrut(2));
        cardPurchases.add(purchasesSub);

        // 4. Total Expenses
        JPanel cardExpenses = createCardBase();
        expensesTitle = createCardTitle(I18n.get("dashboard.card.total_expenses"));
        expensesVal = createCardValue("\u20B90.00", new Color(185, 28, 28));
        expensesSub = createCardSub(I18n.get("dashboard.card.expenses_desc"));
        cardExpenses.add(expensesTitle);
        cardExpenses.add(Box.createVerticalStrut(3));
        cardExpenses.add(expensesVal);
        cardExpenses.add(Box.createVerticalStrut(2));
        cardExpenses.add(expensesSub);

        row1.add(cardTodaySales);
        row1.add(cardInventory);
        row1.add(cardPurchases);
        row1.add(cardExpenses);

        // Row 2: Operational Health (4 cards)
        JPanel row2 = new JPanel(new GridLayout(1, 4, 15, 0));
        row2.setOpaque(false);
        row2.setPreferredSize(new Dimension(0, 92));

        // 5. Net Balance / Profit
        JPanel cardNet = createCardBase();
        netTitle = createCardTitle(I18n.get("dashboard.card.net_balance"));
        netVal = createCardValue("\u20B90.00", new Color(22, 101, 52));
        netSub = createCardSub(I18n.get("dashboard.card.net_balance_desc"));
        cardNet.add(netTitle);
        cardNet.add(Box.createVerticalStrut(3));
        cardNet.add(netVal);
        cardNet.add(Box.createVerticalStrut(2));
        cardNet.add(netSub);

        // 6. Low Stock Products Alert
        JPanel cardLowStock = createCardBase();
        lowStockTitle = createCardTitle(I18n.get("dashboard.card.low_stock"));
        lowStockVal = createCardValue("0", new Color(180, 83, 9));
        lowStockSub = createCardSub(I18n.get("dashboard.card.low_stock_desc"));
        cardLowStock.add(lowStockTitle);
        cardLowStock.add(Box.createVerticalStrut(3));
        cardLowStock.add(lowStockVal);
        cardLowStock.add(Box.createVerticalStrut(2));
        cardLowStock.add(lowStockSub);

        // 7. Customer Khata Outstanding
        JPanel cardCustKhata = createCardBase();
        customerKhataTitle = createCardTitle(I18n.get("dashboard.card.customer_khata"));
        customerKhataVal = createCardValue("\u20B90.00", new Color(15, 23, 42));
        customerKhataSub = createCardSub(I18n.get("dashboard.card.customer_khata_desc"));
        cardCustKhata.add(customerKhataTitle);
        cardCustKhata.add(Box.createVerticalStrut(3));
        cardCustKhata.add(customerKhataVal);
        cardCustKhata.add(Box.createVerticalStrut(2));
        cardCustKhata.add(customerKhataSub);

        // 8. Supplier Payable
        JPanel cardSupPayable = createCardBase();
        supplierPayableTitle = createCardTitle(I18n.get("dashboard.card.supplier_payable"));
        supplierPayableVal = createCardValue("\u20B90.00", new Color(185, 28, 28));
        supplierPayableSub = createCardSub(I18n.get("dashboard.card.supplier_payable_desc"));
        cardSupPayable.add(supplierPayableTitle);
        cardSupPayable.add(Box.createVerticalStrut(3));
        cardSupPayable.add(supplierPayableVal);
        cardSupPayable.add(Box.createVerticalStrut(2));
        cardSupPayable.add(supplierPayableSub);

        row2.add(cardNet);
        row2.add(cardLowStock);
        row2.add(cardCustKhata);
        row2.add(cardSupPayable);

        kpiContainer.add(row1);
        kpiContainer.add(Box.createVerticalStrut(12));
        kpiContainer.add(row2);

        return kpiContainer;
    }

    private JPanel createTablesSplitArea() {
        JPanel split = new JPanel(new GridLayout(1, 2, 20, 0));
        split.setOpaque(false);

        // Left Table: Recent Transactions
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 18, 15, 18)
        ));

        recentTxHeading = new JLabel(I18n.get("dashboard.section.recent_tx"));
        recentTxHeading.setFont(new Font("SansSerif", Font.BOLD, 15));
        recentTxHeading.setForeground(new Color(15, 23, 42));

        String[] txCols = {
                I18n.get("transaction.col.date"),
                I18n.get("transaction.col.type"),
                I18n.get("transaction.col.description"),
                I18n.get("transaction.col.amount")
        };
        txTableModel = new DefaultTableModel(txCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        txTable = new JTable(txTableModel);
        txTable.setRowHeight(28);
        txTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        txTable.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        txTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        txTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.RIGHT);
                String s = val != null ? val.toString() : "";
                if (s.startsWith("+")) {
                    c.setForeground(new Color(22, 101, 52));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (s.startsWith("-")) {
                    c.setForeground(new Color(185, 28, 28));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(new Color(15, 23, 42));
                }
                return c;
            }
        });

        JScrollPane txScroll = new JScrollPane(txTable);
        txScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        leftPanel.add(recentTxHeading, BorderLayout.NORTH);
        leftPanel.add(txScroll, BorderLayout.CENTER);

        // Right Table: Low Stock Inventory Alerts
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 18, 15, 18)
        ));

        lowStockHeading = new JLabel(I18n.get("dashboard.section.low_stock_alerts"));
        lowStockHeading.setFont(new Font("SansSerif", Font.BOLD, 15));
        lowStockHeading.setForeground(new Color(15, 23, 42));

        String[] stockCols = {
                I18n.get("inventory.col.product"),
                I18n.get("inventory.col.stock"),
                I18n.get("inventory.col.unit"),
                I18n.get("inventory.col.low_stock_level")
        };
        lowStockTableModel = new DefaultTableModel(stockCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        lowStockTable = new JTable(lowStockTableModel);
        lowStockTable.setRowHeight(28);
        lowStockTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        lowStockTable.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        lowStockTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.RIGHT);
                c.setForeground(new Color(185, 28, 28));
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }
        });
        lowStockTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        lowStockTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane stockScroll = new JScrollPane(lowStockTable);
        stockScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        rightPanel.add(lowStockHeading, BorderLayout.NORTH);
        rightPanel.add(stockScroll, BorderLayout.CENTER);

        split.add(leftPanel);
        split.add(rightPanel);

        return split;
    }

    public void refreshDashboard() {
        // Safe asynchronous worker to prevent EDT blocking
        SwingWorker<DashboardMetrics, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardMetrics doInBackground() {
                return dashboardDAO.fetchMetrics();
            }

            @Override
            protected void done() {
                try {
                    DashboardMetrics m = get();
                    updateUiWithMetrics(m);
                } catch (Exception e) {
                    System.err.println("Dashboard refresh error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateUiWithMetrics(DashboardMetrics m) {
        todaySalesVal.setText("\u20B9" + String.format("%.2f", m.todaySales));
        todaySalesSub.setText(m.todayInvoices + " " + I18n.get("dashboard.sub.invoices_today"));

        inventoryVal.setText("\u20B9" + String.format("%.2f", m.inventoryValuation));
        inventorySub.setText(m.productCount + " " + I18n.get("dashboard.sub.skus_registered"));

        purchasesVal.setText("\u20B9" + String.format("%.2f", m.totalPurchases));
        expensesVal.setText("\u20B9" + String.format("%.2f", m.totalExpenses));

        netVal.setText((m.netProfit >= 0 ? "+" : "") + "\u20B9" + String.format("%.2f", m.netProfit));
        netVal.setForeground(m.netProfit >= 0 ? new Color(22, 101, 52) : new Color(185, 28, 28));

        lowStockVal.setText(String.valueOf(m.lowStockCount));
        if (m.lowStockCount > 0) {
            lowStockVal.setForeground(new Color(185, 28, 28));
        } else {
            lowStockVal.setForeground(new Color(22, 101, 52));
        }

        customerKhataVal.setText("\u20B9" + String.format("%.2f", m.customerOutstanding));
        supplierPayableVal.setText("\u20B9" + String.format("%.2f", m.supplierOutstanding));

        // Update tables
        txTableModel.setRowCount(0);
        if (m.recentTransactions != null) {
            for (TransactionRecord tx : m.recentTransactions) {
                String prefix = tx.isInflow() ? "+" : "-";
                txTableModel.addRow(new Object[]{
                        tx.getTransactionDate() != null ? tx.getTransactionDate() : "-",
                        tx.getTransactionType(),
                        tx.getDescription() != null ? tx.getDescription() : "-",
                        prefix + " \u20B9" + String.format("%.2f", tx.getAmount())
                });
            }
        }

        lowStockTableModel.setRowCount(0);
        if (m.lowStockProducts != null) {
            for (Product p : m.lowStockProducts) {
                lowStockTableModel.addRow(new Object[]{
                        p.getName(),
                        String.format("%.1f", p.getStockQuantity()),
                        p.getUnit(),
                        String.format("%.1f", p.getLowStockLevel())
                });
            }
        }

        if (mainFrame != null) {
            mainFrame.updateSidebarStatus(false, m.pendingSyncCount);
        }
    }

    private JPanel createCardBase() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 16, 10, 16)
        ));
        return card;
    }

    private JLabel createCardTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(new Color(100, 116, 139));
        return l;
    }

    private JLabel createCardValue(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 19));
        l.setForeground(color);
        return l;
    }

    private JLabel createCardSub(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(new Color(148, 163, 184));
        return l;
    }

    @Override
    public void onLocaleChange() {
        titleLabel.setText(I18n.get("dashboard.title"));
        subtitleLabel.setText(I18n.get("dashboard.subtitle"));
        refreshButton.setText("↻  " + I18n.get("dashboard.btn.refresh"));
        quickSaleBtn.setText("＋  " + I18n.get("nav.new_sale"));
        quickPurchaseBtn.setText("＋  " + I18n.get("nav.new_entry"));

        todaySalesTitle.setText(I18n.get("dashboard.card.today_sales"));
        inventoryTitle.setText(I18n.get("dashboard.card.inventory_val"));
        purchasesTitle.setText(I18n.get("dashboard.card.total_purchases"));
        expensesTitle.setText(I18n.get("dashboard.card.total_expenses"));
        netTitle.setText(I18n.get("dashboard.card.net_balance"));
        lowStockTitle.setText(I18n.get("dashboard.card.low_stock"));
        customerKhataTitle.setText(I18n.get("dashboard.card.customer_khata"));
        supplierPayableTitle.setText(I18n.get("dashboard.card.supplier_payable"));

        recentTxHeading.setText(I18n.get("dashboard.section.recent_tx"));
        lowStockHeading.setText(I18n.get("dashboard.section.low_stock_alerts"));

        refreshDashboard();
    }
}
