package com.krishidb.ui;

import com.krishidb.ui.components.Sidebar;
import com.krishidb.ui.pages.CustomerPanel;
import com.krishidb.ui.pages.InventoryPanel;
import com.krishidb.ui.pages.SalePanel;
import com.krishidb.ui.pages.SettingsPanel;
import com.krishidb.ui.pages.SupplierPanel;
import com.krishidb.ui.pages.SyncPanel;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame implements I18n.LocaleChangeListener {

    private CardLayout cardLayout;
    private JPanel pageContainer;
    private SalePanel salePanel;
    private InventoryPanel inventoryPanel;
    private CustomerPanel customerPanel;
    private SupplierPanel supplierPanel;
    private SyncPanel syncPanel;
    private SettingsPanel settingsPanel;
    private Sidebar sidebar;

    private final List<PlaceholderPanel> placeholderPanels = new ArrayList<>();

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
        salePanel = new SalePanel();
        inventoryPanel = new InventoryPanel();
        customerPanel = new CustomerPanel();
        supplierPanel = new SupplierPanel();
        syncPanel = new SyncPanel(this);
        settingsPanel = new SettingsPanel(this);

        // Registering Required Routes
        // 1. DASHBOARD
        pageContainer.add(createPlaceholderPage("nav.dashboard", "Dashboard", "⌂"), "DASHBOARD");

        // 2. NEW_SALE & NEW_ENTRY
        pageContainer.add(salePanel, "NEW_SALE");
        pageContainer.add(salePanel, "NEW_ENTRY");

        // 3. INVENTORY (Active)
        pageContainer.add(inventoryPanel, "INVENTORY");

        // 4. CUSTOMERS (Active)
        pageContainer.add(customerPanel, "CUSTOMERS");

        // 5. SUPPLIERS (Active)
        pageContainer.add(supplierPanel, "SUPPLIERS");

        // 6. PURCHASES
        pageContainer.add(createPlaceholderPage("nav.purchases", "Purchases", "📦"), "PURCHASES");

        // 7. EXPENSES
        pageContainer.add(createPlaceholderPage("nav.expenses", "Expenses", "₹"), "EXPENSES");

        // 8. TRANSACTIONS
        pageContainer.add(createPlaceholderPage("nav.transactions", "Transactions", "≡"), "TRANSACTIONS");

        // 9. REPORTS
        pageContainer.add(createPlaceholderPage("nav.reports", "Reports", "▥"), "REPORTS");

        // 10. MARKET_PRICES
        pageContainer.add(createPlaceholderPage("nav.market_prices", "Market Prices", "↗"), "MARKET_PRICES");

        // 11. SYNC (Active)
        pageContainer.add(syncPanel, "SYNC");

        // 12. SETTINGS (Active)
        pageContainer.add(settingsPanel, "SETTINGS");

        // ---------------- ADD TO WINDOW ----------------
        add(sidebar, BorderLayout.WEST);
        add(pageContainer, BorderLayout.CENTER);

        I18n.addListener(this);
    }

    private static class PlaceholderPanel extends JPanel implements I18n.LocaleChangeListener {
        private final String i18nKey;
        private final String defaultTitle;
        private final String icon;
        private JLabel iconLabel;
        private JLabel titleLabel;
        private JLabel subtitleLabel;

        public PlaceholderPanel(String i18nKey, String defaultTitle, String icon) {
            this.i18nKey = i18nKey;
            this.defaultTitle = defaultTitle;
            this.icon = icon;

            setLayout(new GridBagLayout());
            setBackground(new Color(248, 250, 252));

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(new EmptyBorder(40, 50, 40, 50));

            iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            titleLabel = new JLabel(I18n.get(i18nKey));
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
            titleLabel.setForeground(new Color(15, 23, 42));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            subtitleLabel = new JLabel(I18n.get("placeholder.under_construction", I18n.get(i18nKey)));
            subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            subtitleLabel.setForeground(new Color(100, 116, 139));
            subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel noteLabel = new JLabel(I18n.get("placeholder.coming_soon"));
            noteLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            noteLabel.setForeground(new Color(148, 163, 184));
            noteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(iconLabel);
            card.add(Box.createVerticalStrut(15));
            card.add(titleLabel);
            card.add(Box.createVerticalStrut(8));
            card.add(subtitleLabel);
            card.add(Box.createVerticalStrut(6));
            card.add(noteLabel);

            add(card);
            I18n.addListener(this);
        }

        @Override
        public void onLocaleChange() {
            titleLabel.setText(I18n.get(i18nKey));
            subtitleLabel.setText(I18n.get("placeholder.under_construction", I18n.get(i18nKey)));
            revalidate();
            repaint();
        }
    }

    private JPanel createPlaceholderPage(String i18nKey, String defaultTitle, String icon) {
        PlaceholderPanel panel = new PlaceholderPanel(i18nKey, defaultTitle, icon);
        placeholderPanels.add(panel);
        return panel;
    }

    public void refreshInventory() {
        if (inventoryPanel != null) {
            inventoryPanel.refreshInventory();
        }
        if (salePanel != null) {
            salePanel.refreshAll();
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
    }

    public void refreshSales() {
        if (salePanel != null) {
            salePanel.refreshAll();
        }
    }

    public void showPage(String pageName) {
        cardLayout.show(pageContainer, pageName);
    }

    public void updateSidebarStatus(boolean online, int pending) {
        sidebar.updateSyncStatus(online, pending);
    }

    @Override
    public void onLocaleChange() {
        setTitle(I18n.get("app.title") + " - " + I18n.get("app.subtitle"));
    }
}