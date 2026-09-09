package com.krishidb.ui;

import com.krishidb.ui.components.Sidebar;
import com.krishidb.ui.pages.CustomerPanel;
import com.krishidb.ui.pages.DashboardPanel;
import com.krishidb.ui.pages.ExpensePanel;
import com.krishidb.ui.pages.InventoryPanel;
import com.krishidb.ui.pages.MarketPricesPanel;
import com.krishidb.ui.pages.PurchasePanel;
import com.krishidb.ui.pages.ReportsPanel;
import com.krishidb.ui.pages.SalePanel;
import com.krishidb.ui.pages.SettingsPanel;
import com.krishidb.ui.pages.SupplierPanel;
import com.krishidb.ui.pages.SyncPanel;
import com.krishidb.ui.pages.TransactionPanel;
import com.krishidb.util.I18n;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements I18n.LocaleChangeListener {

    private CardLayout cardLayout;
    private JPanel pageContainer;

    private DashboardPanel dashboardPanel;
    private SalePanel salePanel;
    private InventoryPanel inventoryPanel;
    private CustomerPanel customerPanel;
    private SupplierPanel supplierPanel;
    private PurchasePanel purchasePanel;
    private ExpensePanel expensePanel;
    private TransactionPanel transactionPanel;
    private ReportsPanel reportsPanel;
    private MarketPricesPanel marketPricesPanel;
    private SyncPanel syncPanel;
    private SettingsPanel settingsPanel;
    private Sidebar sidebar;

    public MainFrame() {
        // ---------------- WINDOW ----------------
        setTitle(I18n.get("app.title") + " - " + I18n.get("app.subtitle"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1400, 850);
        setLocationRelativeTo(null);

        // ---------------- MAIN LAYOUT ----------------
        setLayout(new BorderLayout());

        sidebar = new Sidebar(this);

        // ---------------- PAGE SYSTEM (CardLayout) ----------------
        cardLayout = new CardLayout();
        pageContainer = new JPanel(cardLayout);

        // Initializing Real Panels
        dashboardPanel = new DashboardPanel(this);
        salePanel = new SalePanel();
        inventoryPanel = new InventoryPanel();
        customerPanel = new CustomerPanel();
        supplierPanel = new SupplierPanel();
        purchasePanel = new PurchasePanel();
        expensePanel = new ExpensePanel();
        transactionPanel = new TransactionPanel();
        reportsPanel = new ReportsPanel();
        marketPricesPanel = new MarketPricesPanel();
        syncPanel = new SyncPanel(this);
        settingsPanel = new SettingsPanel(this);

        // Registering Required Routes (Each distinct panel registered EXACTLY ONCE)
        pageContainer.add(dashboardPanel, "DASHBOARD");
        pageContainer.add(salePanel, "NEW_SALE");
        pageContainer.add(inventoryPanel, "INVENTORY");
        pageContainer.add(customerPanel, "CUSTOMERS");
        pageContainer.add(supplierPanel, "SUPPLIERS");
        pageContainer.add(purchasePanel, "PURCHASES");
        pageContainer.add(expensePanel, "EXPENSES");
        pageContainer.add(transactionPanel, "TRANSACTIONS");
        pageContainer.add(reportsPanel, "REPORTS");
        pageContainer.add(marketPricesPanel, "MARKET_PRICES");
        pageContainer.add(syncPanel, "SYNC");
        pageContainer.add(settingsPanel, "SETTINGS");

        // ---------------- ADD TO WINDOW ----------------
        add(sidebar, BorderLayout.WEST);
        add(pageContainer, BorderLayout.CENTER);

        I18n.addListener(this);
    }

    public void refreshInventory() {
        if (inventoryPanel != null) {
            inventoryPanel.refreshInventory();
        }
        if (salePanel != null) {
            salePanel.refreshAll();
        }
        if (purchasePanel != null) {
            purchasePanel.refreshAll();
        }
    }

    public void refreshCustomers() {
        if (customerPanel != null) {
            customerPanel.refreshCustomers();
        }
        if (salePanel != null) {
            salePanel.refreshAll();
        }
    }

    public void refreshSuppliers() {
        if (supplierPanel != null) {
            supplierPanel.refreshSuppliers();
        }
        if (purchasePanel != null) {
            purchasePanel.refreshAll();
        }
    }

    public void refreshSales() {
        if (salePanel != null) {
            salePanel.refreshAll();
        }
    }

    public void refreshPurchases() {
        if (purchasePanel != null) {
            purchasePanel.refreshAll();
        }
    }

    public void refreshExpenses() {
        if (expensePanel != null) {
            expensePanel.refreshExpenses();
        }
    }

    public void refreshTransactions() {
        if (transactionPanel != null) {
            transactionPanel.refreshTransactions();
        }
    }

    public void refreshDashboard() {
        if (dashboardPanel != null) {
            dashboardPanel.refreshDashboard();
        }
    }

    public void showPage(String pageName) {
        // "NEW_ENTRY" represents inward stock entry and routes to PURCHASES
        if ("NEW_ENTRY".equals(pageName)) {
            pageName = "PURCHASES";
        }

        cardLayout.show(pageContainer, pageName);

        // Auto-refresh dynamic views upon becoming active
        if ("DASHBOARD".equals(pageName) && dashboardPanel != null) {
            dashboardPanel.refreshDashboard();
        } else if ("PURCHASES".equals(pageName) && purchasePanel != null) {
            purchasePanel.refreshAll();
        } else if ("EXPENSES".equals(pageName) && expensePanel != null) {
            expensePanel.refreshExpenses();
        } else if ("TRANSACTIONS".equals(pageName) && transactionPanel != null) {
            transactionPanel.refreshTransactions();
        } else if ("REPORTS".equals(pageName) && reportsPanel != null) {
            reportsPanel.generateReport();
        } else if ("MARKET_PRICES".equals(pageName) && marketPricesPanel != null) {
            marketPricesPanel.refreshMarketPrices();
        }
    }

    public void updateSidebarStatus(boolean online, int pending) {
        sidebar.updateSyncStatus(online, pending);
    }

    @Override
    public void onLocaleChange() {
        setTitle(I18n.get("app.title") + " - " + I18n.get("app.subtitle"));
    }
}