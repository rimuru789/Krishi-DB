package com.krishidb.ui.components;

import com.krishidb.ui.MainFrame;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Sidebar extends JPanel implements I18n.LocaleChangeListener {

    private final MainFrame mainFrame;
    private JLabel logo;
    private JLabel subtitle;
    private JLabel connectionStatus;
    private JLabel syncStatus;

    private JLabel businessSectionLabel;
    private JLabel insightsSectionLabel;
    private JLabel systemSectionLabel;

    private boolean isOnline = false;
    private int pendingCount = 1;

    private final Map<String, JButton> navButtons = new HashMap<>();

    private final Color sidebarColor = new Color(15, 23, 42);
    private final Color buttonColor = new Color(15, 23, 42);
    private final Color textColor = new Color(226, 232, 240);
    private final Color mutedTextColor = new Color(148, 163, 184);

    public Sidebar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setPreferredSize(new Dimension(260, 0));
        setBackground(sidebarColor);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createNavigation(), BorderLayout.CENTER);
        add(createStatusArea(), BorderLayout.SOUTH);

        I18n.addListener(this);
    }

    // -------------------------------------------------
    // HEADER
    // -------------------------------------------------
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(28, 24, 25, 20));

        logo = new JLabel(I18n.get("app.title"));
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 24));

        subtitle = new JLabel(I18n.get("app.subtitle"));
        subtitle.setForeground(mutedTextColor);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        return header;
    }

    // -------------------------------------------------
    // NAVIGATION
    // -------------------------------------------------
    private JPanel createNavigation() {
        JPanel navigation = new JPanel();
        navigation.setOpaque(false);
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        navigation.setBorder(new EmptyBorder(5, 14, 10, 14));

        // DASHBOARD
        navigation.add(createNavBtn("DASHBOARD", "⌂", "nav.dashboard"));
        navigation.add(Box.createVerticalStrut(20));

        // BUSINESS SECTION
        businessSectionLabel = createSectionLabel(I18n.get("nav.business"));
        navigation.add(businessSectionLabel);
        navigation.add(Box.createVerticalStrut(6));

        navigation.add(createNavBtn("NEW_ENTRY", "＋", "nav.new_entry"));
        navigation.add(createNavBtn("NEW_SALE", "▣", "nav.new_sale"));
        navigation.add(createNavBtn("INVENTORY", "□", "nav.inventory"));
        navigation.add(createNavBtn("CUSTOMERS", "♙", "nav.customers"));
        navigation.add(createNavBtn("SUPPLIERS", "♟", "nav.suppliers"));
        navigation.add(createNavBtn("PURCHASES", "📦", "nav.purchases"));
        navigation.add(createNavBtn("EXPENSES", "₹", "nav.expenses"));
        navigation.add(createNavBtn("TRANSACTIONS", "≡", "nav.transactions"));

        navigation.add(Box.createVerticalStrut(14));

        // INSIGHTS SECTION
        insightsSectionLabel = createSectionLabel(I18n.get("nav.insights"));
        navigation.add(insightsSectionLabel);
        navigation.add(Box.createVerticalStrut(6));

        navigation.add(createNavBtn("REPORTS", "▥", "nav.reports"));
        navigation.add(createNavBtn("MARKET_PRICES", "↗", "nav.market_prices"));

        navigation.add(Box.createVerticalStrut(14));

        // SYSTEM SECTION
        systemSectionLabel = createSectionLabel(I18n.get("nav.system"));
        navigation.add(systemSectionLabel);
        navigation.add(Box.createVerticalStrut(6));

        navigation.add(createNavBtn("SYNC", "↻", "nav.sync"));
        navigation.add(createNavBtn("SETTINGS", "⚙", "nav.settings"));

        return navigation;
    }

    private JButton createNavBtn(String pageKey, String icon, String i18nKey) {
        JButton button = new JButton(icon + "   " + I18n.get(i18nKey));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setPreferredSize(new Dimension(220, 38));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(textColor);
        button.setBackground(buttonColor);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setBorder(new EmptyBorder(0, 14, 0, 10));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.putClientProperty("icon", icon);
        button.putClientProperty("i18nKey", i18nKey);
        button.putClientProperty("pageKey", pageKey);

        button.addActionListener(e -> mainFrame.showPage(pageKey));

        navButtons.put(pageKey, button);
        return button;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(mutedTextColor);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setBorder(new EmptyBorder(0, 12, 0, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // -------------------------------------------------
    // STATUS AREA
    // -------------------------------------------------
    private JPanel createStatusArea() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(new EmptyBorder(15, 22, 25, 22));

        connectionStatus = new JLabel("●  " + I18n.get("status.offline"));
        connectionStatus.setForeground(new Color(251, 191, 36));
        connectionStatus.setFont(new Font("SansSerif", Font.BOLD, 12));

        syncStatus = new JLabel(I18n.get("status.saved_locally", pendingCount));
        syncStatus.setForeground(mutedTextColor);
        syncStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));

        connectionStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        syncStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(connectionStatus);
        container.add(Box.createVerticalStrut(5));
        container.add(syncStatus);

        return container;
    }

    public void updateSyncStatus(boolean online, int pending) {
        this.isOnline = online;
        this.pendingCount = pending;
        renderStatus();
    }

    private void renderStatus() {
        if (isOnline) {
            connectionStatus.setText("●  " + I18n.get("status.online"));
            connectionStatus.setForeground(new Color(34, 197, 94));
            syncStatus.setText(I18n.get("status.pending_changes", pendingCount));
        } else {
            connectionStatus.setText("●  " + I18n.get("status.offline"));
            connectionStatus.setForeground(new Color(251, 191, 36));
            syncStatus.setText(I18n.get("status.saved_locally", pendingCount));
        }
        repaint();
    }

    @Override
    public void onLocaleChange() {
        logo.setText(I18n.get("app.title"));
        subtitle.setText(I18n.get("app.subtitle"));

        businessSectionLabel.setText(I18n.get("nav.business"));
        insightsSectionLabel.setText(I18n.get("nav.insights"));
        systemSectionLabel.setText(I18n.get("nav.system"));

        for (JButton btn : navButtons.values()) {
            String icon = (String) btn.getClientProperty("icon");
            String key = (String) btn.getClientProperty("i18nKey");
            if (icon != null && key != null) {
                btn.setText(icon + "   " + I18n.get(key));
            }
        }

        renderStatus();
    }
}