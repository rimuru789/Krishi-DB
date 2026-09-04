package com.krishidb.ui.pages;

import com.krishidb.dao.CustomerDAO;
import com.krishidb.dao.ProductDAO;
import com.krishidb.dao.SaleDAO;
import com.krishidb.exception.InsufficientStockException;
import com.krishidb.model.Customer;
import com.krishidb.model.Product;
import com.krishidb.model.Sale;
import com.krishidb.model.SaleItem;
import com.krishidb.ui.dialogs.AddCustomerDialog;
import com.krishidb.ui.dialogs.SaleDetailsDialog;
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

public class SalePanel extends JPanel implements I18n.LocaleChangeListener {

    private final SaleDAO saleDAO;
    private final ProductDAO productDAO;
    private final CustomerDAO customerDAO;

    // Header Components
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton refreshButton;

    // Stat Card Components
    private JLabel todaySalesHeading;
    private JLabel todaySalesValue;
    private JLabel todaySalesDesc;
    private JLabel totalSalesHeading;
    private JLabel totalSalesValue;
    private JLabel totalSalesDesc;
    private JLabel pendingSyncHeading;
    private JLabel pendingSyncValue;
    private JLabel pendingSyncDesc;

    // POS Register Form Components
    private JLabel posSectionHeading;
    private JLabel customerLabel;
    private JComboBox<CustomerOption> customerComboBox;
    private JButton quickAddCustomerBtn;

    private JLabel productLabel;
    private JComboBox<ProductOption> productComboBox;
    private JLabel stockBadgeLabel;
    private JLabel rateBadgeLabel;

    private JLabel quantityLabel;
    private JSpinner quantitySpinner;
    private JButton addToCartBtn;

    // Cart Components
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private final List<SaleItem> cartItems = new ArrayList<>();
    private JButton removeCartItemBtn;
    private JButton clearCartBtn;

    // Checkout Components
    private JLabel grandTotalTitleLabel;
    private JLabel grandTotalAmountLabel;
    private JLabel paymentMethodLabel;
    private JComboBox<PaymentOption> paymentMethodComboBox;
    private JLabel notesLabel;
    private JTextField notesField;
    private JButton completeSaleBtn;

    // Recent Sales Components
    private JLabel recentSalesHeading;
    private JTextField searchField;
    private DefaultTableModel salesTableModel;
    private JTable salesTable;
    private TableRowSorter<DefaultTableModel> salesSorter;
    private JButton viewReceiptBtn;
    private JButton cancelSaleBtn;

    public SalePanel() {
        this.saleDAO = new SaleDAO();
        this.productDAO = new ProductDAO();
        this.customerDAO = new CustomerDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        refreshAll();
        I18n.addListener(this);
    }

    // -------------------------------------------------
    // HEADER
    // -------------------------------------------------
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));

        titleLabel = new JLabel(I18n.get("sale.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("sale.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titleArea.add(titleLabel);
        titleArea.add(Box.createVerticalStrut(4));
        titleArea.add(subtitleLabel);

        JPanel actionArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionArea.setOpaque(false);

        refreshButton = new JButton("↻");
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        refreshButton.setPreferredSize(new Dimension(50, 40));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.putClientProperty("JButton.buttonType", "roundRect");
        refreshButton.addActionListener(e -> refreshAll());

        actionArea.add(refreshButton);

        header.add(titleArea, BorderLayout.WEST);
        header.add(actionArea, BorderLayout.EAST);

        return header;
    }

    // -------------------------------------------------
    // MAIN CONTENT
    // -------------------------------------------------
    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 18));
        mainPanel.setOpaque(false);

        // TOP STAT CARDS
        mainPanel.add(createSummaryCards(), BorderLayout.NORTH);

        // SPLIT POS WORKSPACE: Left = POS Terminal / Cart, Right = Recent Sales History
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerLocation(680);
        splitPane.setResizeWeight(0.55);

        splitPane.setLeftComponent(createPosTerminalSection());
        splitPane.setRightComponent(createRecentSalesSection());

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    // -------------------------------------------------
    // STAT SUMMARY CARDS
    // -------------------------------------------------
    private JPanel createSummaryCards() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        summaryPanel.setOpaque(false);

        todaySalesValue = new JLabel("₹0.00");
        totalSalesValue = new JLabel("0");
        pendingSyncValue = new JLabel("0");

        todaySalesHeading = createCardHeader(I18n.get("sale.card.today_sales"));
        todaySalesDesc = createCardDesc(I18n.get("sale.card.today_sales_desc"));

        totalSalesHeading = createCardHeader(I18n.get("sale.card.total_sales"));
        totalSalesDesc = createCardDesc(I18n.get("sale.card.total_sales_desc"));

        pendingSyncHeading = createCardHeader(I18n.get("sale.card.pending_sync"));
        pendingSyncDesc = createCardDesc(I18n.get("sale.card.pending_sync_desc"));

        summaryPanel.add(buildSummaryCard(todaySalesHeading, todaySalesValue, todaySalesDesc));
        summaryPanel.add(buildSummaryCard(totalSalesHeading, totalSalesValue, totalSalesDesc));
        summaryPanel.add(buildSummaryCard(pendingSyncHeading, pendingSyncValue, pendingSyncDesc));

        return summaryPanel;
    }

    private JLabel createCardHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(new Color(100, 116, 139));
        return label;
    }

    private JLabel createCardDesc(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(new Color(148, 163, 184));
        return label;
    }

    private JPanel buildSummaryCard(JLabel heading, JLabel value, JLabel desc) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        value.setFont(new Font("SansSerif", Font.BOLD, 24));
        value.setForeground(new Color(15, 23, 42));

        card.add(heading);
        card.add(Box.createVerticalStrut(8));
        card.add(value);
        card.add(Box.createVerticalStrut(4));
        card.add(desc);

        return card;
    }

    // -------------------------------------------------
    // POS TERMINAL & CART SECTION (LEFT)
    // -------------------------------------------------
    private JPanel createPosTerminalSection() {
        JPanel terminal = new JPanel(new BorderLayout(0, 12));
        terminal.setBackground(Color.WHITE);
        terminal.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Section Title
        JPanel titleBox = new JPanel(new BorderLayout());
        titleBox.setOpaque(false);
        posSectionHeading = new JLabel(I18n.get("sale.section.pos"));
        posSectionHeading.setFont(new Font("SansSerif", Font.BOLD, 16));
        posSectionHeading.setForeground(new Color(15, 23, 42));
        titleBox.add(posSectionHeading, BorderLayout.WEST);
        terminal.add(titleBox, BorderLayout.NORTH);

        // Center: Entry controls and Cart Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // Inputs Panel
        JPanel inputGrid = new JPanel(new GridBagLayout());
        inputGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Row 1: Customer selector & Quick Add button
        customerLabel = new JLabel(I18n.get("sale.label.customer"));
        customerLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        inputGrid.add(customerLabel, gbc);

        customerComboBox = new JComboBox<>();
        customerComboBox.setPreferredSize(new Dimension(280, 36));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        inputGrid.add(customerComboBox, gbc);

        quickAddCustomerBtn = new JButton(I18n.get("sale.label.new_customer_btn"));
        quickAddCustomerBtn.setPreferredSize(new Dimension(95, 36));
        quickAddCustomerBtn.addActionListener(e -> {
            AddCustomerDialog dialog = new AddCustomerDialog();
            dialog.setVisible(true);
            reloadCustomers();
        });
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        inputGrid.add(quickAddCustomerBtn, gbc);

        // Row 2: Product selector
        productLabel = new JLabel(I18n.get("sale.label.product"));
        productLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        inputGrid.add(productLabel, gbc);

        productComboBox = new JComboBox<>();
        productComboBox.setPreferredSize(new Dimension(280, 36));
        productComboBox.addActionListener(e -> updateProductBadges());
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        inputGrid.add(productComboBox, gbc);
        gbc.gridwidth = 1;

        // Row 3: Product badges (Rate and Stock)
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        badgePanel.setOpaque(false);
        rateBadgeLabel = new JLabel("Rate: ₹0.00");
        rateBadgeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        rateBadgeLabel.setForeground(new Color(16, 185, 129));

        stockBadgeLabel = new JLabel("Available: 0");
        stockBadgeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        stockBadgeLabel.setForeground(new Color(59, 130, 246));

        badgePanel.add(rateBadgeLabel);
        badgePanel.add(stockBadgeLabel);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        inputGrid.add(badgePanel, gbc);
        gbc.gridwidth = 1;

        // Row 4: Quantity input and Add to Cart button
        quantityLabel = new JLabel(I18n.get("sale.label.quantity"));
        quantityLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        inputGrid.add(quantityLabel, gbc);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1.0, 0.01, 99999.0, 1.0);
        quantitySpinner = new JSpinner(spinnerModel);
        quantitySpinner.setPreferredSize(new Dimension(120, 36));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.4;
        inputGrid.add(quantitySpinner, gbc);

        addToCartBtn = new JButton(I18n.get("sale.btn.add_to_cart"));
        addToCartBtn.setBackground(new Color(15, 23, 42));
        addToCartBtn.setForeground(Color.WHITE);
        addToCartBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addToCartBtn.setPreferredSize(new Dimension(130, 36));
        addToCartBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addToCartBtn.addActionListener(e -> handleAddToCart());
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 0.6;
        inputGrid.add(addToCartBtn, gbc);

        centerPanel.add(inputGrid, BorderLayout.NORTH);

        // CART TABLE
        cartTableModel = new DefaultTableModel(getCartColumns(), 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(28);
        cartTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        cartTable.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        cartTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        cartTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(new LineBorder(new Color(226, 232, 240)));
        cartScroll.setPreferredSize(new Dimension(0, 160));

        JPanel cartBox = new JPanel(new BorderLayout(0, 6));
        cartBox.setOpaque(false);
        cartBox.add(cartScroll, BorderLayout.CENTER);

        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cartActions.setOpaque(false);
        removeCartItemBtn = new JButton(I18n.get("sale.btn.remove_item"));
        removeCartItemBtn.addActionListener(e -> handleRemoveCartItem());

        clearCartBtn = new JButton(I18n.get("sale.btn.clear_cart"));
        clearCartBtn.addActionListener(e -> handleClearCart());

        cartActions.add(removeCartItemBtn);
        cartActions.add(clearCartBtn);
        cartBox.add(cartActions, BorderLayout.SOUTH);

        centerPanel.add(cartBox, BorderLayout.CENTER);
        terminal.add(centerPanel, BorderLayout.CENTER);

        // SOUTH: CHECKOUT & PAYMENT CONTROLS
        JPanel checkoutPanel = new JPanel();
        checkoutPanel.setLayout(new BoxLayout(checkoutPanel, BoxLayout.Y_AXIS));
        checkoutPanel.setOpaque(false);
        checkoutPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Grand Total Bar
        JPanel totalBar = new JPanel(new BorderLayout());
        totalBar.setBackground(new Color(241, 245, 249));
        totalBar.setBorder(new EmptyBorder(10, 16, 10, 16));

        grandTotalTitleLabel = new JLabel(I18n.get("sale.label.grand_total"));
        grandTotalTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        grandTotalTitleLabel.setForeground(new Color(15, 23, 42));

        grandTotalAmountLabel = new JLabel("₹0.00");
        grandTotalAmountLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        grandTotalAmountLabel.setForeground(new Color(16, 185, 129));

        totalBar.add(grandTotalTitleLabel, BorderLayout.WEST);
        totalBar.add(grandTotalAmountLabel, BorderLayout.EAST);
        checkoutPanel.add(totalBar);
        checkoutPanel.add(Box.createVerticalStrut(10));

        // Payment & Notes Row
        JPanel payRow = new JPanel(new GridLayout(2, 2, 10, 6));
        payRow.setOpaque(false);

        paymentMethodLabel = new JLabel(I18n.get("sale.label.payment_method"));
        paymentMethodLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        notesLabel = new JLabel(I18n.get("sale.label.notes"));
        notesLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        paymentMethodComboBox = new JComboBox<>(getPaymentOptions());
        paymentMethodComboBox.setPreferredSize(new Dimension(0, 36));

        notesField = new JTextField();
        notesField.setPreferredSize(new Dimension(0, 36));

        payRow.add(paymentMethodLabel);
        payRow.add(notesLabel);
        payRow.add(paymentMethodComboBox);
        payRow.add(notesField);
        checkoutPanel.add(payRow);
        checkoutPanel.add(Box.createVerticalStrut(12));

        // Complete Sale Button
        completeSaleBtn = new JButton(I18n.get("sale.btn.complete_sale"));
        completeSaleBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        completeSaleBtn.setBackground(new Color(16, 185, 129));
        completeSaleBtn.setForeground(Color.WHITE);
        completeSaleBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        completeSaleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        completeSaleBtn.addActionListener(e -> handleCompleteSale());
        checkoutPanel.add(completeSaleBtn);

        terminal.add(checkoutPanel, BorderLayout.SOUTH);

        return terminal;
    }

    // -------------------------------------------------
    // RECENT SALES HISTORY SECTION (RIGHT)
    // -------------------------------------------------
    private JPanel createRecentSalesSection() {
        JPanel historyPanel = new JPanel(new BorderLayout(0, 12));
        historyPanel.setBackground(Color.WHITE);
        historyPanel.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Header & Search
        JPanel topBox = new JPanel(new BorderLayout(10, 8));
        topBox.setOpaque(false);

        recentSalesHeading = new JLabel(I18n.get("sale.section.recent_sales"));
        recentSalesHeading.setFont(new Font("SansSerif", Font.BOLD, 16));
        recentSalesHeading.setForeground(new Color(15, 23, 42));
        topBox.add(recentSalesHeading, BorderLayout.NORTH);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 36));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("sale.search_placeholder"));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterSales(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterSales(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterSales(); }
        });
        topBox.add(searchField, BorderLayout.CENTER);

        historyPanel.add(topBox, BorderLayout.NORTH);

        // Sales Table
        salesTableModel = new DefaultTableModel(getSalesColumns(), 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        salesTable = new JTable(salesTableModel);
        salesTable.setRowHeight(32);
        salesTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        salesTable.getTableHeader().setBackground(new Color(241, 245, 249));

        salesSorter = new TableRowSorter<>(salesTableModel);
        salesTable.setRowSorter(salesSorter);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        salesTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        salesTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        salesTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        salesTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        salesTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(new LineBorder(new Color(226, 232, 240)));
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);

        viewReceiptBtn = new JButton(I18n.get("sale.btn.view_receipt"));
        viewReceiptBtn.setPreferredSize(new Dimension(130, 38));
        viewReceiptBtn.addActionListener(e -> handleViewReceipt());

        cancelSaleBtn = new JButton(I18n.get("sale.btn.cancel_sale"));
        cancelSaleBtn.setPreferredSize(new Dimension(120, 38));
        cancelSaleBtn.addActionListener(e -> handleCancelSale());

        actionRow.add(viewReceiptBtn);
        actionRow.add(cancelSaleBtn);
        historyPanel.add(actionRow, BorderLayout.SOUTH);

        return historyPanel;
    }

    // -------------------------------------------------
    // EVENT HANDLERS & LOGIC
    // -------------------------------------------------
    private void updateProductBadges() {
        ProductOption opt = (ProductOption) productComboBox.getSelectedItem();
        if (opt != null && opt.product != null) {
            Product p = opt.product;
            rateBadgeLabel.setText(I18n.get("sale.label.rate", String.format("%.2f", p.getSellingPrice()), p.getUnit()));
            stockBadgeLabel.setText(I18n.get("sale.label.available_stock", String.format("%.2f", p.getStockQuantity()), p.getUnit()));

            if (p.getStockQuantity() <= 0) {
                stockBadgeLabel.setForeground(new Color(239, 68, 68)); // Red
            } else if (p.getStockQuantity() <= p.getLowStockLevel()) {
                stockBadgeLabel.setForeground(new Color(245, 158, 11)); // Amber
            } else {
                stockBadgeLabel.setForeground(new Color(16, 185, 129)); // Green
            }
        } else {
            rateBadgeLabel.setText(I18n.get("sale.label.rate", "0.00", "-"));
            stockBadgeLabel.setText(I18n.get("sale.label.available_stock", "0", "-"));
        }
    }

    private void handleAddToCart() {
        ProductOption opt = (ProductOption) productComboBox.getSelectedItem();
        if (opt == null || opt.product == null) {
            JOptionPane.showMessageDialog(this, I18n.get("sale.msg.select_product"),
                    I18n.get("app.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product p = opt.product;
        double qty;
        try {
            qty = ((Number) quantitySpinner.getValue()).doubleValue();
            if (qty <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, I18n.get("sale.msg.invalid_quantity"),
                    I18n.get("app.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check already added in cart + requested
        double inCartQty = 0.0;
        SaleItem existingItem = null;
        for (SaleItem item : cartItems) {
            if (item.getProductId() == p.getId()) {
                inCartQty += item.getQuantity();
                existingItem = item;
            }
        }

        double totalRequested = inCartQty + qty;
        if (totalRequested > p.getStockQuantity()) {
            JOptionPane.showMessageDialog(this,
                    I18n.get("sale.msg.insufficient_stock", p.getName(), String.format("%.2f", totalRequested), String.format("%.2f", p.getStockQuantity())),
                    I18n.get("app.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + qty);
        } else {
            SaleItem newItem = new SaleItem(p.getId(), p.getName(), p.getUnit(), qty, p.getSellingPrice());
            cartItems.add(newItem);
        }

        // Reset spinner
        quantitySpinner.setValue(1.0);
        refreshCartTable();
    }

    private void handleRemoveCartItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < cartItems.size()) {
            cartItems.remove(selectedRow);
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
        int idx = 1;

        for (SaleItem item : cartItems) {
            cartTableModel.addRow(new Object[]{
                    idx++,
                    item.getProductName(),
                    String.format("%.2f", item.getPricePerUnit()),
                    String.format("%.2f", item.getQuantity()),
                    item.getUnit(),
                    String.format("%.2f", item.getSubtotal()),
                    "✖"
            });
            total += item.getSubtotal();
        }

        total = Math.round(total * 100.0) / 100.0;
        grandTotalAmountLabel.setText(String.format("₹%.2f", total));
    }

    private void handleCompleteSale() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, I18n.get("sale.msg.cart_empty"),
                    I18n.get("app.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        CustomerOption custOpt = (CustomerOption) customerComboBox.getSelectedItem();
        Integer customerId = (custOpt != null && custOpt.customer != null) ? custOpt.customer.getId() : null;
        String customerName = (custOpt != null && custOpt.customer != null) ? custOpt.customer.getName() : null;

        PaymentOption payOpt = (PaymentOption) paymentMethodComboBox.getSelectedItem();
        String paymentMethod = payOpt != null ? payOpt.code : "CASH";

        if ("CREDIT".equalsIgnoreCase(paymentMethod) && (customerId == null || customerId <= 0)) {
            JOptionPane.showMessageDialog(this, I18n.get("sale.msg.credit_customer_required"),
                    I18n.get("app.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        Sale sale = new Sale();
        sale.setCustomerId(customerId);
        sale.setCustomerName(customerName);
        sale.setPaymentMethod(paymentMethod);
        sale.setNotes(notesField.getText().trim());

        try {
            Sale completedSale = saleDAO.createSale(sale, cartItems);

            JOptionPane.showMessageDialog(this,
                    I18n.get("sale.msg.complete_success", completedSale.getId(), String.format("%.2f", completedSale.getTotalAmount())),
                    I18n.get("app.title"), JOptionPane.INFORMATION_MESSAGE);

            // Show itemized receipt dialog
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            SaleDetailsDialog receipt = new SaleDetailsDialog(parentWindow, completedSale);
            receipt.setVisible(true);

            // Reset POS form
            handleClearCart();
            notesField.setText("");
            if (customerComboBox.getItemCount() > 0) {
                customerComboBox.setSelectedIndex(0);
            }

            // Refresh products, stats, and sales history
            refreshAll();

        } catch (InsufficientStockException stockEx) {
            JOptionPane.showMessageDialog(this,
                    I18n.get("sale.msg.insufficient_stock", stockEx.getProductName(),
                            String.format("%.2f", stockEx.getRequestedQuantity()),
                            String.format("%.2f", stockEx.getAvailableQuantity())),
                    I18n.get("app.title"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    I18n.get("sale.msg.complete_failed", ex.getMessage()),
                    I18n.get("app.title"), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void handleViewReceipt() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, I18n.get("sale.msg.select_sale_first"),
                    I18n.get("app.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = salesTable.convertRowIndexToModel(selectedRow);
        int saleId = (int) salesTableModel.getValueAt(modelRow, 0);

        Sale sale = saleDAO.getSaleById(saleId);
        if (sale != null) {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            SaleDetailsDialog dialog = new SaleDetailsDialog(parentWindow, sale);
            dialog.setVisible(true);
        }
    }

    private void handleCancelSale() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, I18n.get("sale.msg.select_sale_first"),
                    I18n.get("app.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = salesTable.convertRowIndexToModel(selectedRow);
        int saleId = (int) salesTableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                I18n.get("sale.msg.cancel_confirm", saleId),
                I18n.get("sale.msg.cancel_title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = saleDAO.cancelSale(saleId);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            I18n.get("sale.msg.cancel_success", saleId),
                            I18n.get("app.title"), JOptionPane.INFORMATION_MESSAGE);
                    refreshAll();
                } else {
                    JOptionPane.showMessageDialog(this,
                            I18n.get("sale.msg.cancel_failed", "Record not found"),
                            I18n.get("app.title"), JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        I18n.get("sale.msg.cancel_failed", ex.getMessage()),
                        I18n.get("app.title"), JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void filterSales() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            salesSorter.setRowFilter(null);
        } else {
            salesSorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    public void refreshAll() {
        reloadCustomers();
        reloadProducts();
        reloadSalesTable();
        reloadStats();
    }

    private void reloadCustomers() {
        CustomerOption currentSelection = (CustomerOption) customerComboBox.getSelectedItem();
        customerComboBox.removeAllItems();

        // Add Walk-in option
        customerComboBox.addItem(new CustomerOption(null));

        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer c : customers) {
            CustomerOption opt = new CustomerOption(c);
            customerComboBox.addItem(opt);
            if (currentSelection != null && currentSelection.customer != null
                    && currentSelection.customer.getId() == c.getId()) {
                customerComboBox.setSelectedItem(opt);
            }
        }
    }

    private void reloadProducts() {
        ProductOption currentSelection = (ProductOption) productComboBox.getSelectedItem();
        productComboBox.removeAllItems();

        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            ProductOption opt = new ProductOption(p);
            productComboBox.addItem(opt);
            if (currentSelection != null && currentSelection.product != null
                    && currentSelection.product.getId() == p.getId()) {
                productComboBox.setSelectedItem(opt);
            }
        }

        updateProductBadges();
    }

    private void reloadSalesTable() {
        salesTableModel.setRowCount(0);
        List<Sale> sales = saleDAO.getAllSales();
        for (Sale s : sales) {
            String custDisplay = s.getCustomerName() != null && !s.getCustomerName().isEmpty()
                    ? s.getCustomerName()
                    : I18n.get("sale.label.walkin_customer");

            salesTableModel.addRow(new Object[]{
                    s.getId(),
                    s.getSaleDate(),
                    custDisplay,
                    s.getPaymentMethod(),
                    String.format("%.2f", s.getTotalAmount()),
                    s.getSyncStatus()
            });
        }
    }

    private void reloadStats() {
        double todayTotal = saleDAO.getTodaySalesTotal();
        int totalSales = saleDAO.getTotalSalesCount();
        int pendingSync = saleDAO.getPendingSalesCount();

        todaySalesValue.setText(String.format("₹%.2f", todayTotal));
        totalSalesValue.setText(String.valueOf(totalSales));
        pendingSyncValue.setText(String.valueOf(pendingSync));
    }

    // -------------------------------------------------
    // LOCALIZATION
    // -------------------------------------------------
    @Override
    public void onLocaleChange() {
        titleLabel.setText(I18n.get("sale.title"));
        subtitleLabel.setText(I18n.get("sale.subtitle"));

        todaySalesHeading.setText(I18n.get("sale.card.today_sales"));
        todaySalesDesc.setText(I18n.get("sale.card.today_sales_desc"));

        totalSalesHeading.setText(I18n.get("sale.card.total_sales"));
        totalSalesDesc.setText(I18n.get("sale.card.total_sales_desc"));

        pendingSyncHeading.setText(I18n.get("sale.card.pending_sync"));
        pendingSyncDesc.setText(I18n.get("sale.card.pending_sync_desc"));

        posSectionHeading.setText(I18n.get("sale.section.pos"));
        customerLabel.setText(I18n.get("sale.label.customer"));
        quickAddCustomerBtn.setText(I18n.get("sale.label.new_customer_btn"));
        productLabel.setText(I18n.get("sale.label.product"));
        quantityLabel.setText(I18n.get("sale.label.quantity"));
        addToCartBtn.setText(I18n.get("sale.btn.add_to_cart"));

        removeCartItemBtn.setText(I18n.get("sale.btn.remove_item"));
        clearCartBtn.setText(I18n.get("sale.btn.clear_cart"));

        grandTotalTitleLabel.setText(I18n.get("sale.label.grand_total"));
        paymentMethodLabel.setText(I18n.get("sale.label.payment_method"));
        notesLabel.setText(I18n.get("sale.label.notes"));
        completeSaleBtn.setText(I18n.get("sale.btn.complete_sale"));

        recentSalesHeading.setText(I18n.get("sale.section.recent_sales"));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("sale.search_placeholder"));
        viewReceiptBtn.setText(I18n.get("sale.btn.view_receipt"));
        cancelSaleBtn.setText(I18n.get("sale.btn.cancel_sale"));

        // Refresh table headers
        updateTableColumnNames(cartTable, getCartColumns());
        updateTableColumnNames(salesTable, getSalesColumns());

        // Refresh Payment Method combo box items
        PaymentOption selectedPay = (PaymentOption) paymentMethodComboBox.getSelectedItem();
        paymentMethodComboBox.setModel(new DefaultComboBoxModel<>(getPaymentOptions()));
        if (selectedPay != null) {
            for (int i = 0; i < paymentMethodComboBox.getItemCount(); i++) {
                if (paymentMethodComboBox.getItemAt(i).code.equals(selectedPay.code)) {
                    paymentMethodComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Re-render customer options and product badges
        reloadCustomers();
        updateProductBadges();
        reloadSalesTable();

        revalidate();
        repaint();
    }

    private void updateTableColumnNames(JTable table, String[] columnNames) {
        for (int i = 0; i < columnNames.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderValue(columnNames[i]);
        }
        table.getTableHeader().repaint();
    }

    private String[] getCartColumns() {
        return new String[]{
                I18n.get("sale.cart.col.num"),
                I18n.get("sale.cart.col.product"),
                I18n.get("sale.cart.col.rate"),
                I18n.get("sale.cart.col.quantity"),
                I18n.get("sale.cart.col.unit"),
                I18n.get("sale.cart.col.subtotal"),
                I18n.get("sale.cart.col.action")
        };
    }

    private String[] getSalesColumns() {
        return new String[]{
                I18n.get("sale.table.col.id"),
                I18n.get("sale.table.col.date"),
                I18n.get("sale.table.col.customer"),
                I18n.get("sale.table.col.payment"),
                I18n.get("sale.table.col.total"),
                I18n.get("sale.table.col.sync")
        };
    }

    private PaymentOption[] getPaymentOptions() {
        return new PaymentOption[]{
                new PaymentOption("CASH", I18n.get("sale.pay.cash")),
                new PaymentOption("UPI", I18n.get("sale.pay.upi")),
                new PaymentOption("CREDIT", I18n.get("sale.pay.credit"))
        };
    }

    // -------------------------------------------------
    // HELPER OPTION CLASSES FOR COMBOBOXES
    // -------------------------------------------------
    public static class CustomerOption {
        public final Customer customer;

        public CustomerOption(Customer customer) {
            this.customer = customer;
        }

        @Override
        public String toString() {
            if (customer == null) {
                return I18n.get("sale.label.walkin_customer");
            }
            String village = customer.getVillage() != null && !customer.getVillage().isEmpty() ? " (" + customer.getVillage() + ")" : "";
            return customer.getName() + village + " - " + customer.getPhone();
        }
    }

    public static class ProductOption {
        public final Product product;

        public ProductOption(Product product) {
            this.product = product;
        }

        @Override
        public String toString() {
            if (product == null) {
                return "-";
            }
            return String.format("%s [₹%.2f/%s | Stock: %.1f]",
                    product.getName(), product.getSellingPrice(), product.getUnit(), product.getStockQuantity());
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
