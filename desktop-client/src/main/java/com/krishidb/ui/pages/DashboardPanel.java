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
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class DashboardPanel extends JPanel implements I18n.LocaleChangeListener {

    private final DashboardDAO dashboardDAO;
    private final MainFrame mainFrame;

    // Header Components
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton refreshButton;
    private JButton quickSaleBtn;
    private JButton quickPurchaseBtn;

    // Financial KPI Row 1
    private JLabel todaySalesTitle;
    private JLabel todaySalesVal;
    private JLabel todaySalesSub;

    private JLabel inventoryTitle;
    private JLabel inventoryVal;
    private JLabel inventorySub;

    private JLabel purchasesTitle;
    private JLabel purchasesVal;
    private JLabel purchasesSub;

    private JLabel expensesTitle;
    private JLabel expensesVal;
    private JLabel expensesSub;

    // Operational KPI Row 2
    private JLabel netTitle;
    private JLabel netVal;
    private JLabel netSub;

    private JLabel lowStockTitle;
    private JLabel lowStockVal;
    private JLabel lowStockSub;

    private JLabel customerKhataTitle;
    private JLabel customerKhataVal;
    private JLabel customerKhataSub;

    private JLabel supplierPayableTitle;
    private JLabel supplierPayableVal;
    private JLabel supplierPayableSub;

    // Visual Summary: Today's Money Flow
    private JLabel flowTitle;
    private JLabel flowSubtitle;
    private JLabel flowInflowVal;
    private JLabel flowInflowLabel;
    private JLabel flowInwardVal;
    private JLabel flowInwardLabel;
    private JLabel flowOutflowVal;
    private JLabel flowOutflowLabel;
    private JLabel flowNetVal;
    private JLabel flowNetLabel;
    private FlowDistributionBar flowDistributionBar;

    // Recent Transactions
    private JLabel recentTxHeading;
    private JLabel recentTxSub;
    private DefaultTableModel txTableModel;
    private JTable txTable;
    private JPanel txCard;
    private JPanel txEmptyState;
    private JScrollPane txScroll;

    // Business Snapshot Panel Labels
    private JLabel snapshotHeading;
    private JLabel snapInvSection;
    private JLabel snapTotalProductsVal;
    private JLabel snapTotalProductsLabel;
    private JLabel snapInStockVal;
    private JLabel snapInStockLabel;
    private JLabel snapLowStockVal;
    private JLabel snapLowStockLabel;
    private JLabel snapStockValVal;
    private JLabel snapStockValLabel;

    private JLabel snapCreditSection;
    private JLabel snapCustKhataVal;
    private JLabel snapCustKhataLabel;
    private JLabel snapSuppPayableVal;
    private JLabel snapSuppPayableLabel;
    private JLabel snapNetCreditVal;
    private JLabel snapNetCreditLabel;

    private JLabel snapOpsSection;
    private JLabel snapTodaySalesVal;
    private JLabel snapTodaySalesLabel;
    private JLabel snapTodayPurchasesVal;
    private JLabel snapTodayPurchasesLabel;
    private JLabel snapTodayExpensesVal;
    private JLabel snapTodayExpensesLabel;

    // Low Stock Alerts Section
    private JLabel lowStockHeading;
    private JLabel lowStockCountBadge;
    private JPanel lowStockCard;
    private JPanel lowStockEmptyState;
    private JLabel lowStockEmptyTitle;
    private JLabel lowStockEmptyDesc;
    private JScrollPane lowStockScroll;
    private DefaultTableModel lowStockTableModel;
    private JTable lowStockTable;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.dashboardDAO = new DashboardDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));

        // Scrollable content wrapper for flawless responsive resizing
        JPanel rootContainer = new JPanel(new BorderLayout(0, 20));
        rootContainer.setOpaque(false);
        rootContainer.setBorder(new EmptyBorder(22, 28, 25, 28));

        rootContainer.add(createHeader(), BorderLayout.NORTH);
        rootContainer.add(createMainContent(), BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(rootContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        I18n.addListener(this);
        refreshDashboard();
    }

    // -------------------------------------------------------------
    // TOP HEADER
    // -------------------------------------------------------------
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel titlesPanel = new JPanel();
        titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));
        titlesPanel.setOpaque(false);

        titleLabel = new JLabel(I18n.get("dashboard.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("dashboard.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titlesPanel.add(titleLabel);
        titlesPanel.add(Box.createVerticalStrut(4));
        titlesPanel.add(subtitleLabel);

        // Action buttons with crisp borders and modern styling
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        quickSaleBtn = new JButton("＋  " + I18n.get("nav.new_sale"));
        quickSaleBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        quickSaleBtn.setBackground(new Color(22, 101, 52));
        quickSaleBtn.setForeground(Color.WHITE);
        quickSaleBtn.setFocusPainted(false);
        quickSaleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quickSaleBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(21, 128, 61), 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        quickSaleBtn.addActionListener(e -> {
            if (mainFrame != null) mainFrame.showPage("NEW_SALE");
        });

        quickPurchaseBtn = new JButton("＋  " + I18n.get("nav.new_entry"));
        quickPurchaseBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        quickPurchaseBtn.setBackground(new Color(30, 58, 138));
        quickPurchaseBtn.setForeground(Color.WHITE);
        quickPurchaseBtn.setFocusPainted(false);
        quickPurchaseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quickPurchaseBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(29, 78, 216), 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        quickPurchaseBtn.addActionListener(e -> {
            if (mainFrame != null) mainFrame.showPage("PURCHASES");
        });

        refreshButton = new JButton("↻  " + I18n.get("dashboard.btn.refresh"));
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(new Color(15, 23, 42));
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(8, 16, 8, 16)
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

    // -------------------------------------------------------------
    // MAIN CONTENT
    // -------------------------------------------------------------
    private JPanel createMainContent() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        container.add(createKpiSection());
        container.add(Box.createVerticalStrut(18));
        container.add(createSplitBody());

        return container;
    }

    // -------------------------------------------------------------
    // KPI GRID (8 Cards, 2 Rows of 4)
    // -------------------------------------------------------------
    private JPanel createKpiSection() {
        JPanel grid = new JPanel(new GridLayout(2, 4, 14, 14));
        grid.setOpaque(false);

        // Row 1: Core Financials
        // 1. Today's Sales
        JPanel card1 = createRichCardBase(new Color(22, 101, 52), "[ + ]");
        todaySalesTitle = createCardTitle(I18n.get("dashboard.card.today_sales"));
        todaySalesVal = createCardValue("\u20B90.00", new Color(22, 101, 52));
        todaySalesSub = createCardSub("0 " + I18n.get("dashboard.sub.invoices_today"));
        card1.add(todaySalesTitle);
        card1.add(Box.createVerticalStrut(4));
        card1.add(todaySalesVal);
        card1.add(Box.createVerticalStrut(3));
        card1.add(todaySalesSub);

        // 2. Inventory Valuation
        JPanel card2 = createRichCardBase(new Color(51, 65, 85), "[ # ]");
        inventoryTitle = createCardTitle(I18n.get("dashboard.card.inventory_val"));
        inventoryVal = createCardValue("\u20B90.00", new Color(15, 23, 42));
        inventorySub = createCardSub("0 " + I18n.get("dashboard.sub.skus_registered"));
        card2.add(inventoryTitle);
        card2.add(Box.createVerticalStrut(4));
        card2.add(inventoryVal);
        card2.add(Box.createVerticalStrut(3));
        card2.add(inventorySub);

        // 3. Total Purchases
        JPanel card3 = createRichCardBase(new Color(30, 58, 138), "[ = ]");
        purchasesTitle = createCardTitle(I18n.get("dashboard.card.total_purchases"));
        purchasesVal = createCardValue("\u20B90.00", new Color(30, 58, 138));
        purchasesSub = createCardSub("0 " + I18n.get("dashboard.sub.inward_today"));
        card3.add(purchasesTitle);
        card3.add(Box.createVerticalStrut(4));
        card3.add(purchasesVal);
        card3.add(Box.createVerticalStrut(3));
        card3.add(purchasesSub);

        // 4. Total Expenses
        JPanel card4 = createRichCardBase(new Color(185, 28, 28), "[ - ]");
        expensesTitle = createCardTitle(I18n.get("dashboard.card.total_expenses"));
        expensesVal = createCardValue("\u20B90.00", new Color(185, 28, 28));
        expensesSub = createCardSub("0 " + I18n.get("dashboard.sub.expenses_today"));
        card4.add(expensesTitle);
        card4.add(Box.createVerticalStrut(4));
        card4.add(expensesVal);
        card4.add(Box.createVerticalStrut(3));
        card4.add(expensesSub);

        // Row 2: Operational Health
        // 5. Net Profit / Balance
        JPanel card5 = createRichCardBase(new Color(22, 101, 52), "[ \u2248 ]");
        netTitle = createCardTitle(I18n.get("dashboard.card.net_balance"));
        netVal = createCardValue("\u20B90.00", new Color(22, 101, 52));
        netSub = createCardSub(I18n.get("dashboard.card.net_balance_desc"));
        card5.add(netTitle);
        card5.add(Box.createVerticalStrut(4));
        card5.add(netVal);
        card5.add(Box.createVerticalStrut(3));
        card5.add(netSub);

        // 6. Low Stock Alerts
        JPanel card6 = createRichCardBase(new Color(180, 83, 9), "[ ! ]");
        lowStockTitle = createCardTitle(I18n.get("dashboard.card.low_stock"));
        lowStockVal = createCardValue("0", new Color(180, 83, 9));
        lowStockSub = createCardSub(I18n.get("dashboard.card.low_stock_desc"));
        card6.add(lowStockTitle);
        card6.add(Box.createVerticalStrut(4));
        card6.add(lowStockVal);
        card6.add(Box.createVerticalStrut(3));
        card6.add(lowStockSub);

        // 7. Customer Khata Due
        JPanel card7 = createRichCardBase(new Color(67, 56, 202), "[ > ]");
        customerKhataTitle = createCardTitle(I18n.get("dashboard.card.customer_khata"));
        customerKhataVal = createCardValue("\u20B90.00", new Color(15, 23, 42));
        customerKhataSub = createCardSub(I18n.get("dashboard.sub.farmer_dues"));
        card7.add(customerKhataTitle);
        card7.add(Box.createVerticalStrut(4));
        card7.add(customerKhataVal);
        card7.add(Box.createVerticalStrut(3));
        card7.add(customerKhataSub);

        // 8. Supplier Payable
        JPanel card8 = createRichCardBase(new Color(220, 38, 38), "[ < ]");
        supplierPayableTitle = createCardTitle(I18n.get("dashboard.card.supplier_payable"));
        supplierPayableVal = createCardValue("\u20B90.00", new Color(185, 28, 28));
        supplierPayableSub = createCardSub(I18n.get("dashboard.sub.vendor_dues"));
        card8.add(supplierPayableTitle);
        card8.add(Box.createVerticalStrut(4));
        card8.add(supplierPayableVal);
        card8.add(Box.createVerticalStrut(3));
        card8.add(supplierPayableSub);

        grid.add(card1);
        grid.add(card2);
        grid.add(card3);
        grid.add(card4);
        grid.add(card5);
        grid.add(card6);
        grid.add(card7);
        grid.add(card8);

        return grid;
    }

    // -------------------------------------------------------------
    // RESPONSIVE SPLIT BODY (62% Left, 38% Right using GridBagLayout)
    // -------------------------------------------------------------
    private JPanel createSplitBody() {
        JPanel splitPanel = new JPanel(new GridBagLayout());
        splitPanel.setOpaque(false);

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.weightx = 0.62;
        gbcLeft.weighty = 1.0;
        gbcLeft.fill = GridBagConstraints.BOTH;
        gbcLeft.insets = new Insets(0, 0, 0, 9);

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 1;
        gbcRight.gridy = 0;
        gbcRight.weightx = 0.38;
        gbcRight.weighty = 1.0;
        gbcRight.fill = GridBagConstraints.BOTH;
        gbcRight.insets = new Insets(0, 9, 0, 0);

        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setOpaque(false);

        leftColumn.add(createMoneyFlowPanel());
        leftColumn.add(Box.createVerticalStrut(16));
        leftColumn.add(createRecentTxPanel());

        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setOpaque(false);

        rightColumn.add(createBusinessSnapshotPanel());
        rightColumn.add(Box.createVerticalStrut(16));
        rightColumn.add(createLowStockPanel());

        splitPanel.add(leftColumn, gbcLeft);
        splitPanel.add(rightColumn, gbcRight);

        return splitPanel;
    }

    // -------------------------------------------------------------
    // LEFT COLUMN 1: TODAY'S MONEY FLOW (Native Swing Visual Summary)
    // -------------------------------------------------------------
    private JPanel createMoneyFlowPanel() {
        JPanel panel = createStandardCard();
        panel.setLayout(new BorderLayout(0, 12));

        // Header row
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        flowTitle = new JLabel(I18n.get("dashboard.flow.title"));
        flowTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        flowTitle.setForeground(new Color(15, 23, 42));

        flowSubtitle = new JLabel(I18n.get("dashboard.flow.subtitle"));
        flowSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        flowSubtitle.setForeground(new Color(100, 116, 139));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(flowTitle);
        titles.add(Box.createVerticalStrut(2));
        titles.add(flowSubtitle);

        header.add(titles, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Metrics Row
        JPanel metricsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        metricsRow.setOpaque(false);

        // 1. Sales Inflow
        JPanel b1 = createSummaryBlock(new Color(220, 252, 231), new Color(22, 101, 52));
        flowInflowLabel = createBlockLabel(I18n.get("dashboard.flow.inflow"));
        flowInflowVal = createBlockVal("\u20B90.00", new Color(22, 101, 52));
        b1.add(flowInflowLabel);
        b1.add(Box.createVerticalStrut(3));
        b1.add(flowInflowVal);

        // 2. Stock Purchases
        JPanel b2 = createSummaryBlock(new Color(219, 234, 254), new Color(30, 58, 138));
        flowInwardLabel = createBlockLabel(I18n.get("dashboard.flow.inward"));
        flowInwardVal = createBlockVal("\u20B90.00", new Color(30, 58, 138));
        b2.add(flowInwardLabel);
        b2.add(Box.createVerticalStrut(3));
        b2.add(flowInwardVal);

        // 3. Operating Expenses
        JPanel b3 = createSummaryBlock(new Color(254, 226, 226), new Color(185, 28, 28));
        flowOutflowLabel = createBlockLabel(I18n.get("dashboard.flow.outflow"));
        flowOutflowVal = createBlockVal("\u20B90.00", new Color(185, 28, 28));
        b3.add(flowOutflowLabel);
        b3.add(Box.createVerticalStrut(3));
        b3.add(flowOutflowVal);

        // 4. Net Daily Flow
        JPanel b4 = createSummaryBlock(new Color(241, 245, 249), new Color(15, 23, 42));
        flowNetLabel = createBlockLabel(I18n.get("dashboard.flow.net"));
        flowNetVal = createBlockVal("\u20B90.00", new Color(22, 101, 52));
        b4.add(flowNetLabel);
        b4.add(Box.createVerticalStrut(3));
        b4.add(flowNetVal);

        metricsRow.add(b1);
        metricsRow.add(b2);
        metricsRow.add(b3);
        metricsRow.add(b4);

        // Flow distribution bar (native painted Swing component)
        flowDistributionBar = new FlowDistributionBar();

        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.add(metricsRow);
        centerContainer.add(Box.createVerticalStrut(12));
        centerContainer.add(flowDistributionBar);

        panel.add(centerContainer, BorderLayout.CENTER);
        return panel;
    }

    // -------------------------------------------------------------
    // LEFT COLUMN 2: RECENT FINANCIAL TRANSACTIONS TABLE
    // -------------------------------------------------------------
    private JPanel createRecentTxPanel() {
        txCard = createStandardCard();
        txCard.setLayout(new BorderLayout(0, 10));

        // Header
        JPanel headPanel = new JPanel(new BorderLayout());
        headPanel.setOpaque(false);

        recentTxHeading = new JLabel(I18n.get("dashboard.section.recent_tx"));
        recentTxHeading.setFont(new Font("SansSerif", Font.BOLD, 15));
        recentTxHeading.setForeground(new Color(15, 23, 42));

        recentTxSub = new JLabel("Latest recorded cash & khata entries");
        recentTxSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        recentTxSub.setForeground(new Color(100, 116, 139));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(recentTxHeading);
        titles.add(Box.createVerticalStrut(2));
        titles.add(recentTxSub);

        headPanel.add(titles, BorderLayout.WEST);
        txCard.add(headPanel, BorderLayout.NORTH);

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
        txTable = new JTable(txTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return c;
            }
        };
        txTable.setRowHeight(32);
        txTable.setShowVerticalLines(false);
        txTable.setGridColor(new Color(241, 245, 249));
        txTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        txTable.getTableHeader().setBackground(new Color(241, 245, 249));
        txTable.getTableHeader().setForeground(new Color(71, 85, 105));
        txTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        // Column Renderers
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer();
        dateRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        dateRenderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        txTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        txTable.getColumnModel().getColumn(0).setCellRenderer(dateRenderer);

        txTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        txTable.getColumnModel().getColumn(1).setCellRenderer(new BadgeCellRenderer());

        DefaultTableCellRenderer descRenderer = new DefaultTableCellRenderer();
        descRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        descRenderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        txTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        txTable.getColumnModel().getColumn(2).setCellRenderer(descRenderer);

        txTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        txTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.RIGHT);
                c.setBorder(new EmptyBorder(0, 8, 0, 10));
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

        txScroll = new JScrollPane(txTable);
        txScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        txScroll.setPreferredSize(new Dimension(0, 240));

        // Empty state container
        txEmptyState = createEmptyPlaceholder(
                I18n.get("dashboard.empty.tx_title"),
                I18n.get("dashboard.empty.tx_desc")
        );

        txCard.add(txScroll, BorderLayout.CENTER);
        return txCard;
    }

    // -------------------------------------------------------------
    // RIGHT COLUMN 1: BUSINESS SNAPSHOT PANEL
    // -------------------------------------------------------------
    private JPanel createBusinessSnapshotPanel() {
        JPanel card = createStandardCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        snapshotHeading = new JLabel(I18n.get("dashboard.snapshot.title"));
        snapshotHeading.setFont(new Font("SansSerif", Font.BOLD, 15));
        snapshotHeading.setForeground(new Color(15, 23, 42));
        snapshotHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(snapshotHeading);
        card.add(Box.createVerticalStrut(14));

        // 1. Inventory Snapshot
        snapInvSection = createSectionHeading(I18n.get("dashboard.snapshot.inventory"), new Color(30, 58, 138));
        card.add(snapInvSection);
        card.add(Box.createVerticalStrut(6));

        snapTotalProductsLabel = createSnapshotRowLabel(I18n.get("dashboard.snapshot.total_products"));
        snapTotalProductsVal = createSnapshotRowVal("0");
        card.add(createSnapshotRow(snapTotalProductsLabel, snapTotalProductsVal));

        snapInStockLabel = createSnapshotRowLabel(I18n.get("dashboard.snapshot.in_stock"));
        snapInStockVal = createSnapshotRowVal("0");
        card.add(createSnapshotRow(snapInStockLabel, snapInStockVal));

        snapLowStockLabel = createSnapshotRowLabel(I18n.get("dashboard.snapshot.low_stock"));
        snapLowStockVal = createSnapshotRowVal("0");
        card.add(createSnapshotRow(snapLowStockLabel, snapLowStockVal));

        snapStockValLabel = createSnapshotRowLabel(I18n.get("dashboard.snapshot.stock_val"));
        snapStockValVal = createSnapshotRowVal("\u20B90.00");
        card.add(createSnapshotRow(snapStockValLabel, snapStockValVal));

        card.add(Box.createVerticalStrut(10));
        card.add(createRowSeparator());
        card.add(Box.createVerticalStrut(10));

        // 2. Credit Snapshot
        snapCreditSection = createSectionHeading(I18n.get("dashboard.snapshot.credit"), new Color(180, 83, 9));
        card.add(snapCreditSection);
        card.add(Box.createVerticalStrut(6));

        snapCustKhataLabel = createSnapshotRowLabel(I18n.get("dashboard.snapshot.cust_due"));
        snapCustKhataVal = createSnapshotRowVal("\u20B90.00");
        card.add(createSnapshotRow(snapCustKhataLabel, snapCustKhataVal));

        snapSuppPayableLabel = createSnapshotRowLabel(I18n.get("dashboard.snapshot.supp_due"));
        snapSuppPayableVal = createSnapshotRowVal("\u20B90.00");
        card.add(createSnapshotRow(snapSuppPayableLabel, snapSuppPayableVal));

        snapNetCreditLabel = createSnapshotRowLabel(I18n.get("dashboard.snapshot.net_credit"));
        snapNetCreditVal = createSnapshotRowVal("\u20B90.00");
        card.add(createSnapshotRow(snapNetCreditLabel, snapNetCreditVal));

        card.add(Box.createVerticalStrut(10));
        card.add(createRowSeparator());
        card.add(Box.createVerticalStrut(10));

        // 3. Operations Snapshot
        snapOpsSection = createSectionHeading(I18n.get("dashboard.snapshot.operations"), new Color(22, 101, 52));
        card.add(snapOpsSection);
        card.add(Box.createVerticalStrut(6));

        snapTodaySalesLabel = createSnapshotRowLabel(I18n.get("dashboard.snapshot.today_sales"));
        snapTodaySalesVal = createSnapshotRowVal("0");
        card.add(createSnapshotRow(snapTodaySalesLabel, snapTodaySalesVal));

        snapTodayPurchasesLabel = createSnapshotRowLabel(I18n.get("dashboard.snapshot.today_purchases"));
        snapTodayPurchasesVal = createSnapshotRowVal("0");
        card.add(createSnapshotRow(snapTodayPurchasesLabel, snapTodayPurchasesVal));

        snapTodayExpensesLabel = createSnapshotRowLabel(I18n.get("dashboard.snapshot.today_expenses"));
        snapTodayExpensesVal = createSnapshotRowVal("0");
        card.add(createSnapshotRow(snapTodayExpensesLabel, snapTodayExpensesVal));

        return card;
    }

    // -------------------------------------------------------------
    // RIGHT COLUMN 2: LOW STOCK INVENTORY ALERTS
    // -------------------------------------------------------------
    private JPanel createLowStockPanel() {
        lowStockCard = createStandardCard();
        lowStockCard.setLayout(new BorderLayout(0, 10));

        JPanel headPanel = new JPanel(new BorderLayout());
        headPanel.setOpaque(false);

        lowStockHeading = new JLabel(I18n.get("dashboard.section.low_stock_alerts"));
        lowStockHeading.setFont(new Font("SansSerif", Font.BOLD, 15));
        lowStockHeading.setForeground(new Color(15, 23, 42));

        lowStockCountBadge = new JLabel(" 0 ");
        lowStockCountBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        lowStockCountBadge.setForeground(new Color(22, 101, 52));
        lowStockCountBadge.setBackground(new Color(220, 252, 231));
        lowStockCountBadge.setOpaque(true);
        lowStockCountBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        headPanel.add(lowStockHeading, BorderLayout.WEST);
        headPanel.add(lowStockCountBadge, BorderLayout.EAST);
        lowStockCard.add(headPanel, BorderLayout.NORTH);

        // Healthy Empty State Panel
        lowStockEmptyState = new JPanel(new BorderLayout());
        lowStockEmptyState.setBackground(new Color(240, 253, 244));
        lowStockEmptyState.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(187, 247, 208), 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JPanel textWrap = new JPanel();
        textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));
        textWrap.setOpaque(false);

        lowStockEmptyTitle = new JLabel("\u2713  " + I18n.get("dashboard.empty.healthy_title"));
        lowStockEmptyTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        lowStockEmptyTitle.setForeground(new Color(22, 101, 52));

        lowStockEmptyDesc = new JLabel("<html>" + I18n.get("dashboard.empty.healthy_desc") + "</html>");
        lowStockEmptyDesc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lowStockEmptyDesc.setForeground(new Color(21, 128, 61));

        textWrap.add(lowStockEmptyTitle);
        textWrap.add(Box.createVerticalStrut(4));
        textWrap.add(lowStockEmptyDesc);
        lowStockEmptyState.add(textWrap, BorderLayout.CENTER);

        // Table for low stock items
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
        lowStockTable = new JTable(lowStockTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(254, 242, 242));
                }
                return c;
            }
        };
        lowStockTable.setRowHeight(30);
        lowStockTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        lowStockTable.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

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

        lowStockScroll = new JScrollPane(lowStockTable);
        lowStockScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        lowStockScroll.setPreferredSize(new Dimension(0, 160));

        lowStockCard.add(lowStockEmptyState, BorderLayout.CENTER);
        return lowStockCard;
    }

    // -------------------------------------------------------------
    // DATA REFRESH LOGIC (Non-blocking SwingWorker)
    // -------------------------------------------------------------
    public void refreshDashboard() {
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
        // Financial KPI Cards
        todaySalesVal.setText("\u20B9" + String.format("%.2f", m.todaySales));
        todaySalesSub.setText(m.todayInvoices + " " + I18n.get("dashboard.sub.invoices_today"));

        inventoryVal.setText("\u20B9" + String.format("%.2f", m.inventoryValuation));
        inventorySub.setText(m.productCount + " " + I18n.get("dashboard.sub.skus_registered"));

        purchasesVal.setText("\u20B9" + String.format("%.2f", m.totalPurchases));
        purchasesSub.setText(m.todayPurchasesCount + " " + I18n.get("dashboard.sub.inward_today"));

        expensesVal.setText("\u20B9" + String.format("%.2f", m.totalExpenses));
        expensesSub.setText(m.todayExpensesCount + " " + I18n.get("dashboard.sub.expenses_today"));

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

        // Money Flow Summary Panel
        flowInflowVal.setText("\u20B9" + String.format("%.2f", m.todaySales));
        flowInwardVal.setText("\u20B9" + String.format("%.2f", m.todayPurchases));
        flowOutflowVal.setText("\u20B9" + String.format("%.2f", m.todayExpenses));

        double netDaily = m.todaySales - m.todayPurchases - m.todayExpenses;
        flowNetVal.setText((netDaily >= 0 ? "+" : "") + "\u20B9" + String.format("%.2f", netDaily));
        flowNetVal.setForeground(netDaily >= 0 ? new Color(22, 101, 52) : new Color(185, 28, 28));

        flowDistributionBar.setValues(m.todaySales, m.todayPurchases, m.todayExpenses);

        // Business Snapshot Panel
        snapTotalProductsVal.setText(String.valueOf(m.productCount));
        snapInStockVal.setText(String.valueOf(m.inStockCount));
        snapLowStockVal.setText(String.valueOf(m.lowStockCount));
        if (m.lowStockCount > 0) {
            snapLowStockVal.setForeground(new Color(185, 28, 28));
        } else {
            snapLowStockVal.setForeground(new Color(22, 101, 52));
        }
        snapStockValVal.setText("\u20B9" + String.format("%.2f", m.inventoryValuation));

        snapCustKhataVal.setText("\u20B9" + String.format("%.2f", m.customerOutstanding));
        snapSuppPayableVal.setText("\u20B9" + String.format("%.2f", m.supplierOutstanding));
        double netCredit = m.customerOutstanding - m.supplierOutstanding;
        snapNetCreditVal.setText((netCredit >= 0 ? "+" : "") + "\u20B9" + String.format("%.2f", netCredit));
        snapNetCreditVal.setForeground(netCredit >= 0 ? new Color(22, 101, 52) : new Color(185, 28, 28));

        snapTodaySalesVal.setText(m.todayInvoices + " (" + "\u20B9" + String.format("%.2f", m.todaySales) + ")");
        snapTodayPurchasesVal.setText(m.todayPurchasesCount + " (" + "\u20B9" + String.format("%.2f", m.todayPurchases) + ")");
        snapTodayExpensesVal.setText(m.todayExpensesCount + " (" + "\u20B9" + String.format("%.2f", m.todayExpenses) + ")");

        // Update Recent Transactions Table & Empty State
        txTableModel.setRowCount(0);
        if (m.recentTransactions != null && !m.recentTransactions.isEmpty()) {
            for (TransactionRecord tx : m.recentTransactions) {
                String prefix = tx.isInflow() ? "+" : "-";
                txTableModel.addRow(new Object[]{
                        tx.getTransactionDate() != null ? tx.getTransactionDate() : "-",
                        tx.getTransactionType(),
                        tx.getDescription() != null ? tx.getDescription() : "-",
                        prefix + " \u20B9" + String.format("%.2f", tx.getAmount())
                });
            }
            txCard.remove(txEmptyState);
            txCard.add(txScroll, BorderLayout.CENTER);
        } else {
            txCard.remove(txScroll);
            txCard.add(txEmptyState, BorderLayout.CENTER);
        }
        txCard.revalidate();
        txCard.repaint();

        // Update Low Stock Table & Empty State
        lowStockTableModel.setRowCount(0);
        if (m.lowStockProducts != null && !m.lowStockProducts.isEmpty()) {
            for (Product p : m.lowStockProducts) {
                lowStockTableModel.addRow(new Object[]{
                        p.getName(),
                        String.format("%.1f", p.getStockQuantity()),
                        p.getUnit(),
                        String.format("%.1f", p.getLowStockLevel())
                });
            }
            lowStockCountBadge.setText(" " + m.lowStockProducts.size() + " ");
            lowStockCountBadge.setForeground(new Color(185, 28, 28));
            lowStockCountBadge.setBackground(new Color(254, 226, 226));

            lowStockCard.remove(lowStockEmptyState);
            lowStockCard.add(lowStockScroll, BorderLayout.CENTER);
        } else {
            lowStockCountBadge.setText(" 0 ");
            lowStockCountBadge.setForeground(new Color(22, 101, 52));
            lowStockCountBadge.setBackground(new Color(220, 252, 231));

            lowStockCard.remove(lowStockScroll);
            lowStockCard.add(lowStockEmptyState, BorderLayout.CENTER);
        }
        lowStockCard.revalidate();
        lowStockCard.repaint();

        if (mainFrame != null) {
            mainFrame.updateSidebarStatus(false, m.pendingSyncCount);
        }
    }

    // -------------------------------------------------------------
    // UI BUILDER HELPERS
    // -------------------------------------------------------------
    private JPanel createRichCardBase(Color accentColor, String glyph) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(accentColor);
                // 3px left indicator bar
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));
        return card;
    }

    private JPanel createStandardCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 18, 16, 18)
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
        l.setFont(new Font("SansSerif", Font.BOLD, 20));
        l.setForeground(color);
        return l;
    }

    private JLabel createCardSub(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(new Color(148, 163, 184));
        return l;
    }

    private JPanel createSummaryBlock(Color bg, Color border) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBackground(bg);
        block.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return block;
    }

    private JLabel createBlockLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(new Color(71, 85, 105));
        return l;
    }

    private JLabel createBlockVal(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 15));
        l.setForeground(color);
        return l;
    }

    private JLabel createSectionHeading(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel createSnapshotRow(JLabel label, JLabel val) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.add(label, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private JLabel createSnapshotRowLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(71, 85, 105));
        return l;
    }

    private JLabel createSnapshotRowVal(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(new Color(15, 23, 42));
        return l;
    }

    private JSeparator createRowSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(241, 245, 249));
        sep.setBackground(new Color(241, 245, 249));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private JPanel createEmptyPlaceholder(String title, String desc) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(248, 250, 252));
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(30, 20, 30, 20)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 14));
        t.setForeground(new Color(71, 85, 105));
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel d = new JLabel(desc);
        d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        d.setForeground(new Color(148, 163, 184));
        d.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(t);
        p.add(Box.createVerticalStrut(6));
        p.add(d);
        return p;
    }

    // -------------------------------------------------------------
    // NATIVE SWING DISTRIBUTION BAR
    // -------------------------------------------------------------
    public static class FlowDistributionBar extends JPanel {
        private double sales = 0;
        private double purchases = 0;
        private double expenses = 0;

        public FlowDistributionBar() {
            setPreferredSize(new Dimension(0, 12));
            setMinimumSize(new Dimension(0, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
            setOpaque(false);
        }

        public void setValues(double sales, double purchases, double expenses) {
            this.sales = Math.max(0, sales);
            this.purchases = Math.max(0, purchases);
            this.expenses = Math.max(0, expenses);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 6;

            double total = sales + purchases + expenses;
            if (total <= 0 || w <= 0) {
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                g2.dispose();
                return;
            }

            Shape originalClip = g2.getClip();
            RoundRectangle2D roundedBar = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);
            g2.clip(roundedBar);

            int salesW = (int) Math.round((sales / total) * w);
            int purchasesW = (int) Math.round((purchases / total) * w);
            int expensesW = w - salesW - purchasesW;

            int currentX = 0;
            if (salesW > 0) {
                g2.setColor(new Color(34, 197, 94)); // Emerald green for sales
                g2.fillRect(currentX, 0, salesW, h);
                currentX += salesW;
            }
            if (purchasesW > 0) {
                g2.setColor(new Color(59, 130, 246)); // Blue for purchases
                g2.fillRect(currentX, 0, purchasesW, h);
                currentX += purchasesW;
            }
            if (expensesW > 0) {
                g2.setColor(new Color(239, 68, 68)); // Red for expenses
                g2.fillRect(currentX, 0, expensesW, h);
            }

            g2.setClip(originalClip);
            g2.setColor(new Color(203, 213, 225));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.dispose();
        }
    }

    // -------------------------------------------------------------
    // TABLE BADGE CELL RENDERER
    // -------------------------------------------------------------
    public static class BadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setHorizontalAlignment(SwingConstants.CENTER);

            String text = value != null ? value.toString() : "";
            Color bg;
            Color fg;

            switch (text.toUpperCase()) {
                case "SALE" -> {
                    bg = new Color(220, 252, 231);
                    fg = new Color(21, 128, 61);
                }
                case "PURCHASE" -> {
                    bg = new Color(219, 234, 254);
                    fg = new Color(30, 64, 175);
                }
                case "EXPENSE" -> {
                    bg = new Color(254, 226, 226);
                    fg = new Color(185, 28, 28);
                }
                case "CUSTOMER_PAYMENT" -> {
                    bg = new Color(204, 251, 241);
                    fg = new Color(15, 118, 110);
                }
                case "SUPPLIER_PAYMENT" -> {
                    bg = new Color(254, 243, 199);
                    fg = new Color(180, 83, 9);
                }
                default -> {
                    bg = new Color(241, 245, 249);
                    fg = new Color(71, 85, 105);
                }
            }

            Color finalBg = bg;
            JPanel pillPanel = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(finalBg);
                    g2.fillRoundRect(6, 4, getWidth() - 12, getHeight() - 8, 10, 10);
                    g2.dispose();
                }
            };
            pillPanel.setOpaque(false);
            label.setForeground(fg);
            label.setFont(new Font("SansSerif", Font.BOLD, 10));
            label.setOpaque(false);
            pillPanel.add(label);

            if (isSelected) {
                pillPanel.setBackground(table.getSelectionBackground());
                pillPanel.setOpaque(true);
            }
            return pillPanel;
        }
    }

    // -------------------------------------------------------------
    // LOCALIZATION LISTENER
    // -------------------------------------------------------------
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

        flowTitle.setText(I18n.get("dashboard.flow.title"));
        flowSubtitle.setText(I18n.get("dashboard.flow.subtitle"));
        flowInflowLabel.setText(I18n.get("dashboard.flow.inflow"));
        flowInwardLabel.setText(I18n.get("dashboard.flow.inward"));
        flowOutflowLabel.setText(I18n.get("dashboard.flow.outflow"));
        flowNetLabel.setText(I18n.get("dashboard.flow.net"));

        recentTxHeading.setText(I18n.get("dashboard.section.recent_tx"));
        snapshotHeading.setText(I18n.get("dashboard.snapshot.title"));
        snapInvSection.setText(I18n.get("dashboard.snapshot.inventory"));
        snapTotalProductsLabel.setText(I18n.get("dashboard.snapshot.total_products"));
        snapInStockLabel.setText(I18n.get("dashboard.snapshot.in_stock"));
        snapLowStockLabel.setText(I18n.get("dashboard.snapshot.low_stock"));
        snapStockValLabel.setText(I18n.get("dashboard.snapshot.stock_val"));

        snapCreditSection.setText(I18n.get("dashboard.snapshot.credit"));
        snapCustKhataLabel.setText(I18n.get("dashboard.snapshot.cust_due"));
        snapSuppPayableLabel.setText(I18n.get("dashboard.snapshot.supp_due"));
        snapNetCreditLabel.setText(I18n.get("dashboard.snapshot.net_credit"));

        snapOpsSection.setText(I18n.get("dashboard.snapshot.operations"));
        snapTodaySalesLabel.setText(I18n.get("dashboard.snapshot.today_sales"));
        snapTodayPurchasesLabel.setText(I18n.get("dashboard.snapshot.today_purchases"));
        snapTodayExpensesLabel.setText(I18n.get("dashboard.snapshot.today_expenses"));

        lowStockHeading.setText(I18n.get("dashboard.section.low_stock_alerts"));
        lowStockEmptyTitle.setText("\u2713  " + I18n.get("dashboard.empty.healthy_title"));
        lowStockEmptyDesc.setText("<html>" + I18n.get("dashboard.empty.healthy_desc") + "</html>");

        String[] txCols = {
                I18n.get("transaction.col.date"),
                I18n.get("transaction.col.type"),
                I18n.get("transaction.col.description"),
                I18n.get("transaction.col.amount")
        };
        for (int i = 0; i < txCols.length; i++) {
            txTable.getColumnModel().getColumn(i).setHeaderValue(txCols[i]);
        }
        txTable.getTableHeader().repaint();

        String[] stockCols = {
                I18n.get("inventory.col.product"),
                I18n.get("inventory.col.stock"),
                I18n.get("inventory.col.unit"),
                I18n.get("inventory.col.low_stock_level")
        };
        for (int i = 0; i < stockCols.length; i++) {
            lowStockTable.getColumnModel().getColumn(i).setHeaderValue(stockCols[i]);
        }
        lowStockTable.getTableHeader().repaint();

        refreshDashboard();
    }
}
