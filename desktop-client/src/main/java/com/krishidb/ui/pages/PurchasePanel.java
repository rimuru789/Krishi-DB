package com.krishidb.ui.pages;

import com.krishidb.dao.ProductDAO;
import com.krishidb.dao.PurchaseDAO;
import com.krishidb.dao.SupplierDAO;
import com.krishidb.model.Product;
import com.krishidb.model.Purchase;
import com.krishidb.model.PurchaseItem;
import com.krishidb.model.Supplier;
import com.krishidb.ui.dialogs.AddSupplierDialog;
import com.krishidb.ui.dialogs.PurchaseDetailsDialog;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PurchasePanel extends JPanel implements I18n.LocaleChangeListener {

    private final PurchaseDAO purchaseDAO;
    private final ProductDAO productDAO;
    private final SupplierDAO supplierDAO;

    // Header Components
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton refreshButton;

    // Stat Cards
    private JLabel todayPurchasesHeading;
    private JLabel todayPurchasesValue;
    private JLabel todayPurchasesDesc;
    private JLabel totalPurchasesHeading;
    private JLabel totalPurchasesValue;
    private JLabel totalPurchasesDesc;
    private JLabel totalSpentHeading;
    private JLabel totalSpentValue;
    private JLabel totalSpentDesc;

    // Inward Form Components
    private JLabel inwardSectionHeading;
    private JLabel supplierLabel;
    private JComboBox<SupplierOption> supplierComboBox;
    private JButton quickAddSupplierBtn;

    private JLabel productLabel;
    private JComboBox<ProductOption> productComboBox;
    private JLabel stockBadgeLabel;

    private JLabel rateLabel;
    private JSpinner rateSpinner;

    private JLabel quantityLabel;
    private JSpinner quantitySpinner;
    private JButton addToCartBtn;

    // Cart Components
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private final List<PurchaseItem> cartItems = new ArrayList<>();
    private JButton removeCartItemBtn;
    private JButton clearCartBtn;

    // Purchase Finalization Components
    private JLabel grandTotalTitleLabel;
    private JLabel grandTotalAmountLabel;
    private JLabel paymentMethodLabel;
    private JComboBox<PaymentOption> paymentMethodComboBox;
    private JLabel invoiceNumberLabel;
    private JTextField invoiceNumberField;
    private JLabel notesLabel;
    private JTextField notesField;
    private JButton completePurchaseBtn;

    // Recent Purchases Components
    private JLabel recentPurchasesHeading;
    private JTextField searchField;
    private DefaultTableModel purchasesTableModel;
    private JTable purchasesTable;
    private TableRowSorter<DefaultTableModel> purchasesSorter;
    private JButton viewReceiptBtn;
    private JButton cancelPurchaseBtn;

    public PurchasePanel() {
        this.purchaseDAO = new PurchaseDAO();
        this.productDAO = new ProductDAO();
        this.supplierDAO = new SupplierDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        I18n.addListener(this);
        refreshAll();
    }

    // ---------------- HEADER ----------------
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel titlesPanel = new JPanel();
        titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));
        titlesPanel.setOpaque(false);

        titleLabel = new JLabel(I18n.get("purchase.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("purchase.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titlesPanel.add(titleLabel);
        titlesPanel.add(Box.createVerticalStrut(4));
        titlesPanel.add(subtitleLabel);

        refreshButton = new JButton("↻  " + I18n.get("purchase.btn.refresh"));
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(new Color(15, 23, 42));
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 16, 8, 16)
        ));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> refreshAll());

        headerPanel.add(titlesPanel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        return headerPanel;
    }

    // ---------------- MAIN CONTENT ----------------
    private JPanel createMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout(0, 15));
        mainContent.setOpaque(false);

        mainContent.add(createStatCards(), BorderLayout.NORTH);

        // Split Left (Inward Register & Cart) and Right (History Table)
        JPanel workArea = new JPanel(new GridLayout(1, 2, 20, 0));
        workArea.setOpaque(false);

        workArea.add(createInwardFormAndCartPanel());
        workArea.add(createRecentPurchasesPanel());

        mainContent.add(workArea, BorderLayout.CENTER);
        return mainContent;
    }

    // ---------------- STAT CARDS ----------------
    private JPanel createStatCards() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 95));

        // Card 1: Today's Purchases
        JPanel card1 = new JPanel();
        card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
        card1.setBackground(Color.WHITE);
        card1.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 18, 12, 18)
        ));
        todayPurchasesHeading = new JLabel(I18n.get("purchase.card.today_purchases"));
        todayPurchasesHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        todayPurchasesHeading.setForeground(new Color(100, 116, 139));
        todayPurchasesValue = new JLabel("\u20B90.00");
        todayPurchasesValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        todayPurchasesValue.setForeground(new Color(30, 58, 138));
        todayPurchasesDesc = new JLabel(I18n.get("purchase.card.today_purchases_desc"));
        todayPurchasesDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        todayPurchasesDesc.setForeground(new Color(148, 163, 184));
        card1.add(todayPurchasesHeading);
        card1.add(Box.createVerticalStrut(4));
        card1.add(todayPurchasesValue);
        card1.add(Box.createVerticalStrut(2));
        card1.add(todayPurchasesDesc);

        // Card 2: Total Transactions
        JPanel card2 = new JPanel();
        card2.setLayout(new BoxLayout(card2, BoxLayout.Y_AXIS));
        card2.setBackground(Color.WHITE);
        card2.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 18, 12, 18)
        ));
        totalPurchasesHeading = new JLabel(I18n.get("purchase.card.total_purchases"));
        totalPurchasesHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        totalPurchasesHeading.setForeground(new Color(100, 116, 139));
        totalPurchasesValue = new JLabel("0");
        totalPurchasesValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        totalPurchasesValue.setForeground(new Color(15, 23, 42));
        totalPurchasesDesc = new JLabel(I18n.get("purchase.card.total_purchases_desc"));
        totalPurchasesDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        totalPurchasesDesc.setForeground(new Color(148, 163, 184));
        card2.add(totalPurchasesHeading);
        card2.add(Box.createVerticalStrut(4));
        card2.add(totalPurchasesValue);
        card2.add(Box.createVerticalStrut(2));
        card2.add(totalPurchasesDesc);

        // Card 3: Total Spent / Inward Volume
        JPanel card3 = new JPanel();
        card3.setLayout(new BoxLayout(card3, BoxLayout.Y_AXIS));
        card3.setBackground(Color.WHITE);
        card3.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 18, 12, 18)
        ));
        totalSpentHeading = new JLabel(I18n.get("purchase.card.total_spent"));
        totalSpentHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        totalSpentHeading.setForeground(new Color(100, 116, 139));
        totalSpentValue = new JLabel("\u20B90.00");
        totalSpentValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        totalSpentValue.setForeground(new Color(180, 83, 9));
        totalSpentDesc = new JLabel(I18n.get("purchase.card.total_spent_desc"));
        totalSpentDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        totalSpentDesc.setForeground(new Color(148, 163, 184));
        card3.add(totalSpentHeading);
        card3.add(Box.createVerticalStrut(4));
        card3.add(totalSpentValue);
        card3.add(Box.createVerticalStrut(2));
        card3.add(totalSpentDesc);

        cardsPanel.add(card1);
        cardsPanel.add(card2);
        cardsPanel.add(card3);

        return cardsPanel;
    }

    // ---------------- INWARD FORM & CART (LEFT PANEL) ----------------
    private JPanel createInwardFormAndCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 18, 15, 18)
        ));

        // Section Title
        inwardSectionHeading = new JLabel(I18n.get("purchase.section.inward"));
        inwardSectionHeading.setFont(new Font("SansSerif", Font.BOLD, 16));
        inwardSectionHeading.setForeground(new Color(15, 23, 42));

        // Form fields grid
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);

        // 1. Supplier Row
        supplierLabel = new JLabel(I18n.get("purchase.label.supplier"));
        supplierLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        supplierLabel.setForeground(new Color(71, 85, 105));

        JPanel supplierRow = new JPanel(new BorderLayout(8, 0));
        supplierRow.setOpaque(false);
        supplierComboBox = new JComboBox<>();
        supplierComboBox.setPreferredSize(new Dimension(0, 32));

        quickAddSupplierBtn = new JButton("+");
        quickAddSupplierBtn.setToolTipText(I18n.get("purchase.label.new_supplier_btn"));
        quickAddSupplierBtn.setPreferredSize(new Dimension(42, 32));
        quickAddSupplierBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        quickAddSupplierBtn.setFocusPainted(false);
        quickAddSupplierBtn.setBackground(new Color(241, 245, 249));
        quickAddSupplierBtn.addActionListener(e -> {
            AddSupplierDialog dialog = new AddSupplierDialog();
            dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            loadSuppliers();
        });

        supplierRow.add(supplierComboBox, BorderLayout.CENTER);
        supplierRow.add(quickAddSupplierBtn, BorderLayout.EAST);

        // 2. Product Row
        productLabel = new JLabel(I18n.get("purchase.label.product"));
        productLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        productLabel.setForeground(new Color(71, 85, 105));

        productComboBox = new JComboBox<>();
        productComboBox.setPreferredSize(new Dimension(0, 32));
        productComboBox.addActionListener(e -> updateProductStockBadge());

        stockBadgeLabel = new JLabel();
        stockBadgeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        stockBadgeLabel.setForeground(new Color(100, 116, 139));

        // 3. Rate & Quantity Row
        JPanel rateQtyPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        rateQtyPanel.setOpaque(false);

        JPanel rateSub = new JPanel(new BorderLayout(0, 4));
        rateSub.setOpaque(false);
        rateLabel = new JLabel(I18n.get("purchase.label.rate"));
        rateLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        rateLabel.setForeground(new Color(71, 85, 105));
        rateSpinner = new JSpinner(new SpinnerNumberModel(100.0, 0.0, 1000000.0, 5.0));
        rateSpinner.setPreferredSize(new Dimension(0, 32));
        rateSub.add(rateLabel, BorderLayout.NORTH);
        rateSub.add(rateSpinner, BorderLayout.CENTER);

        JPanel qtySub = new JPanel(new BorderLayout(0, 4));
        qtySub.setOpaque(false);
        quantityLabel = new JLabel(I18n.get("purchase.label.quantity"));
        quantityLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        quantityLabel.setForeground(new Color(71, 85, 105));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 10000.0, 1.0));
        quantitySpinner.setPreferredSize(new Dimension(0, 32));
        qtySub.add(quantityLabel, BorderLayout.NORTH);
        qtySub.add(quantitySpinner, BorderLayout.CENTER);

        rateQtyPanel.add(rateSub);
        rateQtyPanel.add(qtySub);

        // 4. Add to Cart Button
        addToCartBtn = new JButton("＋  " + I18n.get("purchase.btn.add_to_cart"));
        addToCartBtn.setPreferredSize(new Dimension(0, 34));
        addToCartBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addToCartBtn.setBackground(new Color(30, 58, 138));
        addToCartBtn.setForeground(Color.WHITE);
        addToCartBtn.setFocusPainted(false);
        addToCartBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addToCartBtn.addActionListener(e -> handleAddToCart());

        formPanel.add(supplierLabel);
        formPanel.add(Box.createVerticalStrut(3));
        formPanel.add(supplierRow);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(productLabel);
        formPanel.add(Box.createVerticalStrut(3));
        formPanel.add(productComboBox);
        formPanel.add(Box.createVerticalStrut(2));
        formPanel.add(stockBadgeLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(rateQtyPanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(addToCartBtn);

        // Middle: Cart Table
        String[] cartColumns = {
                I18n.get("purchase.cart.col.product"),
                I18n.get("purchase.cart.col.rate"),
                I18n.get("purchase.cart.col.quantity"),
                I18n.get("purchase.cart.col.subtotal")
        };
        cartTableModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(26);
        cartTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        cartTable.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        cartTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setPreferredSize(new Dimension(0, 110));
        cartScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        // Cart Actions Strip
        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        cartActions.setOpaque(false);
        removeCartItemBtn = new JButton(I18n.get("purchase.btn.remove_item"));
        removeCartItemBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        removeCartItemBtn.addActionListener(e -> handleRemoveCartItem());

        clearCartBtn = new JButton(I18n.get("purchase.btn.clear_cart"));
        clearCartBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        clearCartBtn.addActionListener(e -> handleClearCart());

        cartActions.add(removeCartItemBtn);
        cartActions.add(clearCartBtn);

        // Bottom: Checkout fields & Grand Total
        JPanel checkoutPanel = new JPanel();
        checkoutPanel.setLayout(new BoxLayout(checkoutPanel, BoxLayout.Y_AXIS));
        checkoutPanel.setOpaque(false);
        checkoutPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        grandTotalTitleLabel = new JLabel(I18n.get("purchase.label.grand_total"));
        grandTotalTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        grandTotalTitleLabel.setForeground(new Color(15, 23, 42));

        grandTotalAmountLabel = new JLabel("\u20B90.00");
        grandTotalAmountLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        grandTotalAmountLabel.setForeground(new Color(22, 101, 52));
        totalRow.add(grandTotalTitleLabel, BorderLayout.WEST);
        totalRow.add(grandTotalAmountLabel, BorderLayout.EAST);

        // Payment Mode and Invoice Row
        JPanel payInvRow = new JPanel(new GridLayout(1, 2, 10, 0));
        payInvRow.setOpaque(false);

        JPanel paySub = new JPanel(new BorderLayout(0, 3));
        paySub.setOpaque(false);
        paymentMethodLabel = new JLabel(I18n.get("purchase.label.payment_method"));
        paymentMethodLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        paymentMethodComboBox = new JComboBox<>();
        paymentMethodComboBox.setPreferredSize(new Dimension(0, 30));
        paySub.add(paymentMethodLabel, BorderLayout.NORTH);
        paySub.add(paymentMethodComboBox, BorderLayout.CENTER);

        JPanel invSub = new JPanel(new BorderLayout(0, 3));
        invSub.setOpaque(false);
        invoiceNumberLabel = new JLabel(I18n.get("purchase.label.invoice_no"));
        invoiceNumberLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        invoiceNumberField = new JTextField();
        invoiceNumberField.setPreferredSize(new Dimension(0, 30));
        invSub.add(invoiceNumberLabel, BorderLayout.NORTH);
        invSub.add(invoiceNumberField, BorderLayout.CENTER);

        payInvRow.add(paySub);
        payInvRow.add(invSub);

        // Notes row
        notesLabel = new JLabel(I18n.get("purchase.label.notes"));
        notesLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        notesField = new JTextField();
        notesField.setPreferredSize(new Dimension(0, 30));

        // Complete Purchase Button
        completePurchaseBtn = new JButton("✔  " + I18n.get("purchase.btn.complete_purchase"));
        completePurchaseBtn.setPreferredSize(new Dimension(0, 38));
        completePurchaseBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        completePurchaseBtn.setBackground(new Color(22, 101, 52));
        completePurchaseBtn.setForeground(Color.WHITE);
        completePurchaseBtn.setFocusPainted(false);
        completePurchaseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        completePurchaseBtn.addActionListener(e -> handleCompletePurchase());

        checkoutPanel.add(totalRow);
        checkoutPanel.add(Box.createVerticalStrut(8));
        checkoutPanel.add(payInvRow);
        checkoutPanel.add(Box.createVerticalStrut(6));
        checkoutPanel.add(notesLabel);
        checkoutPanel.add(Box.createVerticalStrut(2));
        checkoutPanel.add(notesField);
        checkoutPanel.add(Box.createVerticalStrut(10));
        checkoutPanel.add(completePurchaseBtn);

        // Center assembly
        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.setOpaque(false);
        centerArea.add(cartScroll, BorderLayout.CENTER);
        centerArea.add(cartActions, BorderLayout.SOUTH);

        JPanel upperArea = new JPanel(new BorderLayout(0, 8));
        upperArea.setOpaque(false);
        upperArea.add(inwardSectionHeading, BorderLayout.NORTH);
        upperArea.add(formPanel, BorderLayout.CENTER);

        panel.add(upperArea, BorderLayout.NORTH);
        panel.add(centerArea, BorderLayout.CENTER);
        panel.add(checkoutPanel, BorderLayout.SOUTH);

        loadPaymentOptions();
        return panel;
    }

    // ---------------- RECENT PURCHASES (RIGHT PANEL) ----------------
    private JPanel createRecentPurchasesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 18, 15, 18)
        ));

        // Top Heading & Search Bar
        JPanel topStrip = new JPanel(new BorderLayout(10, 0));
        topStrip.setOpaque(false);

        recentPurchasesHeading = new JLabel(I18n.get("purchase.section.recent_purchases"));
        recentPurchasesHeading.setFont(new Font("SansSerif", Font.BOLD, 16));
        recentPurchasesHeading.setForeground(new Color(15, 23, 42));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(170, 30));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("purchase.search_placeholder"));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterPurchases(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterPurchases(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterPurchases(); }
        });

        topStrip.add(recentPurchasesHeading, BorderLayout.WEST);
        topStrip.add(searchField, BorderLayout.EAST);

        // Table
        String[] cols = {
                I18n.get("purchase.table.col.id"),
                I18n.get("purchase.table.col.date"),
                I18n.get("purchase.table.col.supplier"),
                I18n.get("purchase.table.col.invoice"),
                I18n.get("purchase.table.col.payment"),
                I18n.get("purchase.table.col.total"),
                I18n.get("purchase.table.col.sync")
        };

        purchasesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        purchasesTable = new JTable(purchasesTableModel);
        purchasesTable.setRowHeight(30);
        purchasesTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        purchasesTable.getTableHeader().setBackground(new Color(241, 245, 249));

        purchasesSorter = new TableRowSorter<>(purchasesTableModel);
        purchasesTable.setRowSorter(purchasesSorter);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        purchasesTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        purchasesTable.getColumnModel().getColumn(0).setMaxWidth(50);
        purchasesTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        purchasesTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        purchasesTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(purchasesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        // Bottom Action Buttons
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomActions.setOpaque(false);

        viewReceiptBtn = new JButton("📄  " + I18n.get("purchase.btn.view_receipt"));
        viewReceiptBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        viewReceiptBtn.addActionListener(e -> handleViewReceipt());

        cancelPurchaseBtn = new JButton("✖  " + I18n.get("purchase.btn.cancel_purchase"));
        cancelPurchaseBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cancelPurchaseBtn.setForeground(new Color(185, 28, 28));
        cancelPurchaseBtn.addActionListener(e -> handleCancelPurchase());

        bottomActions.add(viewReceiptBtn);
        bottomActions.add(cancelPurchaseBtn);

        panel.add(topStrip, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomActions, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------- LOGIC & HANDLERS ----------------
    public void refreshAll() {
        loadSuppliers();
        loadProducts();
        loadPurchases();
        updateStatCards();
    }

    private void updateStatCards() {
        double todayTotal = purchaseDAO.getTodayPurchasesTotal();
        int totalCount = purchaseDAO.getTotalPurchasesCount();
        double totalSpent = purchaseDAO.getTotalPurchasesAmount();

        todayPurchasesValue.setText("\u20B9" + String.format("%.2f", todayTotal));
        totalPurchasesValue.setText(String.valueOf(totalCount));
        totalSpentValue.setText("\u20B9" + String.format("%.2f", totalSpent));
    }

    private void loadSuppliers() {
        supplierComboBox.removeAllItems();
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        for (Supplier s : suppliers) {
            supplierComboBox.addItem(new SupplierOption(s.getId(), s.getName()));
        }
    }

    private void loadProducts() {
        productComboBox.removeAllItems();
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            productComboBox.addItem(new ProductOption(p.getId(), p.getName(), p.getUnit(), p.getStockQuantity(), p.getSellingPrice()));
        }
        updateProductStockBadge();
    }

    private void updateProductStockBadge() {
        ProductOption opt = (ProductOption) productComboBox.getSelectedItem();
        if (opt != null) {
            stockBadgeLabel.setText(I18n.get("purchase.label.available_stock", String.format("%.1f", opt.currentStock), opt.unit));
            if (rateSpinner != null && ((Double) rateSpinner.getValue()) == 100.0) {
                rateSpinner.setValue(opt.sellingPrice);
            }
        } else {
            stockBadgeLabel.setText("");
        }
    }

    private void loadPaymentOptions() {
        paymentMethodComboBox.removeAllItems();
        paymentMethodComboBox.addItem(new PaymentOption("CASH", I18n.get("purchase.pay.cash")));
        paymentMethodComboBox.addItem(new PaymentOption("UPI", I18n.get("purchase.pay.upi")));
        paymentMethodComboBox.addItem(new PaymentOption("CREDIT", I18n.get("purchase.pay.credit")));
    }

    private void handleAddToCart() {
        ProductOption opt = (ProductOption) productComboBox.getSelectedItem();
        if (opt == null) {
            JOptionPane.showMessageDialog(this, I18n.get("purchase.msg.select_product"), I18n.get("purchase.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        double qty = ((Number) quantitySpinner.getValue()).doubleValue();
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, I18n.get("purchase.msg.invalid_quantity"), I18n.get("purchase.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        double rate = ((Number) rateSpinner.getValue()).doubleValue();
        if (rate < 0) {
            JOptionPane.showMessageDialog(this, I18n.get("purchase.msg.invalid_rate"), I18n.get("purchase.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if product is already in cart, if so update quantity
        for (PurchaseItem item : cartItems) {
            if (item.getProductId() == opt.id) {
                item.setQuantity(item.getQuantity() + qty);
                item.setPricePerUnit(rate);
                refreshCartTable();
                return;
            }
        }

        PurchaseItem newItem = new PurchaseItem(opt.id, opt.name, opt.unit, qty, rate);
        cartItems.add(newItem);
        refreshCartTable();
    }

    private void handleRemoveCartItem() {
        int selected = cartTable.getSelectedRow();
        if (selected >= 0 && selected < cartItems.size()) {
            cartItems.remove(selected);
            refreshCartTable();
        }
    }

    private void handleClearCart() {
        cartItems.clear();
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        double total = 0.0;
        for (PurchaseItem item : cartItems) {
            cartTableModel.addRow(new Object[]{
                    item.getProductName() + (item.getUnit() != null ? " (" + item.getUnit() + ")" : ""),
                    String.format("%.2f", item.getPricePerUnit()),
                    String.format("%.2f", item.getQuantity()),
                    String.format("%.2f", item.getSubtotal())
            });
            total += item.getSubtotal();
        }
        total = Math.round(total * 100.0) / 100.0;
        grandTotalAmountLabel.setText("\u20B9" + String.format("%.2f", total));
    }

    private void handleCompletePurchase() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, I18n.get("purchase.msg.cart_empty"), I18n.get("purchase.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        SupplierOption supOpt = (SupplierOption) supplierComboBox.getSelectedItem();
        PaymentOption payOpt = (PaymentOption) paymentMethodComboBox.getSelectedItem();
        String paymentMethod = payOpt != null ? payOpt.code : "CASH";

        if ("CREDIT".equalsIgnoreCase(paymentMethod) && (supOpt == null || supOpt.id <= 0)) {
            JOptionPane.showMessageDialog(this, I18n.get("purchase.msg.credit_supplier_required"), I18n.get("purchase.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        double grandTotal = 0.0;
        for (PurchaseItem item : cartItems) {
            grandTotal += item.getSubtotal();
        }
        grandTotal = Math.round(grandTotal * 100.0) / 100.0;

        Purchase purchase = new Purchase();
        if (supOpt != null && supOpt.id > 0) {
            purchase.setSupplierId(supOpt.id);
            purchase.setSupplierName(supOpt.name);
        }
        purchase.setInvoiceNumber(invoiceNumberField.getText().trim());
        purchase.setPaymentMethod(paymentMethod);
        purchase.setNotes(notesField.getText().trim());
        purchase.setTotalAmount(grandTotal);

        try {
            Purchase savedPurchase = purchaseDAO.createPurchase(purchase, cartItems);
            JOptionPane.showMessageDialog(
                    this,
                    I18n.get("purchase.msg.complete_success", savedPurchase.getId(), String.format("%.2f", savedPurchase.getTotalAmount())),
                    I18n.get("purchase.title"),
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Reset cart and fields
            cartItems.clear();
            refreshCartTable();
            invoiceNumberField.setText("");
            notesField.setText("");

            // Refresh UI
            refreshAll();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    I18n.get("purchase.msg.complete_failed", ex.getMessage()),
                    I18n.get("purchase.title"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadPurchases() {
        purchasesTableModel.setRowCount(0);
        List<Purchase> list = purchaseDAO.getAllPurchases();
        for (Purchase p : list) {
            purchasesTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getPurchaseDate() != null ? p.getPurchaseDate() : "-",
                    p.getSupplierName() != null ? p.getSupplierName() : "-",
                    p.getInvoiceNumber() != null && !p.getInvoiceNumber().isEmpty() ? p.getInvoiceNumber() : "-",
                    p.getPaymentMethod(),
                    String.format("%.2f", p.getTotalAmount()),
                    p.getSyncStatus()
            });
        }
    }

    private void filterPurchases() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            purchasesSorter.setRowFilter(null);
        } else {
            purchasesSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void handleViewReceipt() {
        int selected = purchasesTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, I18n.get("purchase.msg.select_purchase_first"), I18n.get("purchase.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = purchasesTable.convertRowIndexToModel(selected);
        int purchaseId = (int) purchasesTableModel.getValueAt(modelRow, 0);

        Purchase purchase = purchaseDAO.getPurchaseById(purchaseId);
        if (purchase != null) {
            PurchaseDetailsDialog dialog = new PurchaseDetailsDialog(SwingUtilities.getWindowAncestor(this), purchase);
            dialog.setVisible(true);
        }
    }

    private void handleCancelPurchase() {
        int selected = purchasesTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, I18n.get("purchase.msg.select_purchase_first"), I18n.get("purchase.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = purchasesTable.convertRowIndexToModel(selected);
        int purchaseId = (int) purchasesTableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                I18n.get("purchase.msg.cancel_confirm", purchaseId),
                I18n.get("purchase.msg.cancel_title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = purchaseDAO.cancelPurchase(purchaseId);
                if (success) {
                    JOptionPane.showMessageDialog(this, I18n.get("purchase.msg.cancel_success", purchaseId), I18n.get("purchase.title"), JOptionPane.INFORMATION_MESSAGE);
                    refreshAll();
                } else {
                    JOptionPane.showMessageDialog(this, I18n.get("purchase.msg.cancel_failed", "Not found"), I18n.get("purchase.title"), JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, I18n.get("purchase.msg.cancel_failed", ex.getMessage()), I18n.get("purchase.title"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onLocaleChange() {
        titleLabel.setText(I18n.get("purchase.title"));
        subtitleLabel.setText(I18n.get("purchase.subtitle"));
        refreshButton.setText("↻  " + I18n.get("purchase.btn.refresh"));

        todayPurchasesHeading.setText(I18n.get("purchase.card.today_purchases"));
        todayPurchasesDesc.setText(I18n.get("purchase.card.today_purchases_desc"));
        totalPurchasesHeading.setText(I18n.get("purchase.card.total_purchases"));
        totalPurchasesDesc.setText(I18n.get("purchase.card.total_purchases_desc"));
        totalSpentHeading.setText(I18n.get("purchase.card.total_spent"));
        totalSpentDesc.setText(I18n.get("purchase.card.total_spent_desc"));

        inwardSectionHeading.setText(I18n.get("purchase.section.inward"));
        supplierLabel.setText(I18n.get("purchase.label.supplier"));
        productLabel.setText(I18n.get("purchase.label.product"));
        rateLabel.setText(I18n.get("purchase.label.rate"));
        quantityLabel.setText(I18n.get("purchase.label.quantity"));
        addToCartBtn.setText("＋  " + I18n.get("purchase.btn.add_to_cart"));

        removeCartItemBtn.setText(I18n.get("purchase.btn.remove_item"));
        clearCartBtn.setText(I18n.get("purchase.btn.clear_cart"));

        grandTotalTitleLabel.setText(I18n.get("purchase.label.grand_total"));
        paymentMethodLabel.setText(I18n.get("purchase.label.payment_method"));
        invoiceNumberLabel.setText(I18n.get("purchase.label.invoice_no"));
        notesLabel.setText(I18n.get("purchase.label.notes"));
        completePurchaseBtn.setText("✔  " + I18n.get("purchase.btn.complete_purchase"));

        recentPurchasesHeading.setText(I18n.get("purchase.section.recent_purchases"));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("purchase.search_placeholder"));
        viewReceiptBtn.setText("📄  " + I18n.get("purchase.btn.view_receipt"));
        cancelPurchaseBtn.setText("✖  " + I18n.get("purchase.btn.cancel_purchase"));

        loadPaymentOptions();
        updateProductStockBadge();
        refreshCartTable();
    }

    // Helper classes for ComboBoxes
    public static class SupplierOption {
        public final int id;
        public final String name;

        public SupplierOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class ProductOption {
        public final int id;
        public final String name;
        public final String unit;
        public final double currentStock;
        public final double sellingPrice;

        public ProductOption(int id, String name, String unit, double currentStock, double sellingPrice) {
            this.id = id;
            this.name = name;
            this.unit = unit;
            this.currentStock = currentStock;
            this.sellingPrice = sellingPrice;
        }

        @Override
        public String toString() {
            return name + " (" + unit + ")";
        }
    }

    public static class PaymentOption {
        public final String code;
        public final String label;

        public PaymentOption(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
