package com.krishidb.ui.pages;

import com.krishidb.dao.SettingsDAO;
import com.krishidb.database.DatabaseManager;
import com.krishidb.ui.MainFrame;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * SettingsPanel - Modern Tactile Agri-SaaS Settings and Configuration View.
 * Fully responsive across 1280x720, 1366x768, 1536x864, and 1920x1080.
 * Eliminates horizontal clipping via Scrollable viewport-width tracking and dynamic column reflow.
 */
public class SettingsPanel extends JPanel implements I18n.LocaleChangeListener {

    private final MainFrame mainFrame;
    private final SettingsDAO settingsDAO;
    private boolean isUpdatingLocale = false;
    private boolean isStackedLayout = false;

    // Layout containers for responsive reflow
    private ScrollableContentPanel rootContainer;
    private JPanel splitGrid;
    private JPanel leftCol;
    private JPanel rightCol;

    // Form input components
    private JComboBox<LanguageItem> languageCombo;
    private JTextField businessNameField;
    private JTextField ownerNameField;
    private JTextField phoneField;
    private JTextField villageField;
    private JTextField serverUrlField;

    // Action buttons
    private JButton backupButton;
    private JButton saveButton;

    // Dynamic Labels for runtime i18n
    // Header
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel badgeOfflineFirst;
    private JLabel badgeOfflineDesc;

    // Card 1: Application Preferences
    private JLabel langSectionLabel;
    private JLabel langSectionDescLabel;
    private JLabel langSelectLabel;
    private JLabel langSelectHelperLabel;
    private JLabel langNoteLabel;

    // Card 2: Business Profile
    private JLabel businessSectionLabel;
    private JLabel businessSectionDescLabel;
    private JLabel businessIdentityBadgeLabel;
    private JLabel businessNameLabel;
    private JLabel businessNameHelperLabel;
    private JLabel ownerNameLabel;
    private JLabel ownerNameHelperLabel;
    private JLabel phoneLabel;
    private JLabel phoneHelperLabel;
    private JLabel villageLabel;
    private JLabel villageHelperLabel;

    // Card 3: Server & Synchronization
    private JLabel serverSectionLabel;
    private JLabel serverSectionDescLabel;
    private JLabel serverUrlLabel;
    private JLabel serverUrlHelperLabel;
    private JLabel serverModeTitleLabel;
    private JLabel serverModeDescLabel;

    // Card 4: Local Data Management
    private JLabel backupSectionLabel;
    private JLabel backupSectionDescLabel;
    private JLabel backupTitleLabel;
    private JLabel backupDescLabel;
    private JLabel backupNoteLabel;

    // Card 5: About Krishi-DB
    private JLabel aboutSectionLabel;
    private JLabel aboutSectionDescLabel;
    private JLabel aboutProductNameLabel;
    private JLabel versionBadgeLabel;
    private JLabel modeBadgeLabel;
    private JLabel aboutDescLabel;
    private JLabel aboutFeature1Label;
    private JLabel aboutFeature2Label;
    private JLabel aboutFeature3Label;

    // Save Footer
    private JLabel saveIndicatorLabel;
    private JLabel saveHelperLabel;

    /**
     * Custom Scrollable Panel that strictly tracks viewport width.
     * Prevents children from forcing the JScrollPane wider than the window.
     */
    private static class ScrollableContentPanel extends JPanel implements Scrollable {
        public ScrollableContentPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // Strictly lock to viewport width
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false; // Enable vertical scrolling naturally
        }
    }

    public static class LanguageItem {
        private final String code;
        private final String displayName;

        public LanguageItem(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.settingsDAO = new SettingsDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));

        // Scrollable root container with responsive padding
        rootContainer = new ScrollableContentPanel(new BorderLayout(0, 18));
        rootContainer.setBorder(new EmptyBorder(18, 22, 22, 22));

        rootContainer.add(createHeaderPanel(), BorderLayout.NORTH);
        rootContainer.add(createMainContentPanel(), BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(rootContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        // Listen for window resize to dynamically adapt two-column vs stacked layout
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateResponsiveLayout();
            }
        });

        loadSettingsData();
        I18n.addListener(this);
    }

    private void updateResponsiveLayout() {
        int width = getWidth();
        if (width <= 0) return;

        // On viewport width < 960px, reflow to stacked single-column to prevent cramping
        boolean shouldStack = width < 960;
        if (shouldStack != isStackedLayout) {
            isStackedLayout = shouldStack;
            applyColumnLayout();
        }
    }

    // -------------------------------------------------------------
    // TOP HEADER WITH RESPONSIVE OFFLINE-FIRST BADGE
    // -------------------------------------------------------------
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(14, 10));
        headerPanel.setOpaque(false);

        // Titles
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        titleLabel = new JLabel(I18n.get("settings.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel("<html>" + I18n.get("settings.subtitle") + "</html>");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(subtitleLabel);

        // Right-side Offline Configuration Badge
        JPanel badgeContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeContainer.setOpaque(false);

        JPanel badgePill = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        badgePill.setOpaque(false);
        badgePill.setBorder(new EmptyBorder(6, 12, 6, 12));

        JComponent statusDot = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(34, 197, 94)); // Emerald green dot
                g2.fillOval(2, 5, 8, 8);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(12, 18);
            }
        };

        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);

        badgeOfflineFirst = new JLabel(I18n.get("settings.badge.offline_first"));
        badgeOfflineFirst.setFont(new Font("SansSerif", Font.BOLD, 11));
        badgeOfflineFirst.setForeground(new Color(15, 23, 42));

        badgeOfflineDesc = new JLabel(I18n.get("settings.badge.offline_desc"));
        badgeOfflineDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        badgeOfflineDesc.setForeground(new Color(100, 116, 139));

        textBlock.add(badgeOfflineFirst);
        textBlock.add(badgeOfflineDesc);

        badgePill.add(statusDot, BorderLayout.WEST);
        badgePill.add(textBlock, BorderLayout.CENTER);

        badgeContainer.add(badgePill);

        headerPanel.add(titleBlock, BorderLayout.CENTER);
        headerPanel.add(badgeContainer, BorderLayout.EAST);

        return headerPanel;
    }

    // -------------------------------------------------------------
    // MAIN CONTENT (RESPONSIVE SPLIT + STICKY SAVE FOOTER)
    // -------------------------------------------------------------
    private JPanel createMainContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Left Column: Business Profile + Server & Sync
        leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        leftCol.add(createBusinessProfileCard());
        leftCol.add(Box.createVerticalStrut(16));
        leftCol.add(createServerSyncCard());

        // Right Column: Preferences + Data Management + About
        rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        rightCol.add(createPreferencesCard());
        rightCol.add(Box.createVerticalStrut(16));
        rightCol.add(createDataManagementCard());
        rightCol.add(Box.createVerticalStrut(16));
        rightCol.add(createAboutCard());

        splitGrid = new JPanel();
        splitGrid.setOpaque(false);
        applyColumnLayout();

        contentPanel.add(splitGrid);
        contentPanel.add(Box.createVerticalStrut(18));
        contentPanel.add(createSaveFooterBar());

        return contentPanel;
    }

    private void applyColumnLayout() {
        splitGrid.removeAll();
        if (isStackedLayout) {
            splitGrid.setLayout(new BoxLayout(splitGrid, BoxLayout.Y_AXIS));
            splitGrid.add(leftCol);
            splitGrid.add(Box.createVerticalStrut(16));
            splitGrid.add(rightCol);
        } else {
            splitGrid.setLayout(new GridBagLayout());
            GridBagConstraints gbcLeft = new GridBagConstraints();
            gbcLeft.gridx = 0;
            gbcLeft.gridy = 0;
            gbcLeft.weightx = 0.52;
            gbcLeft.weighty = 1.0;
            gbcLeft.fill = GridBagConstraints.BOTH;
            gbcLeft.insets = new Insets(0, 0, 0, 8);

            GridBagConstraints gbcRight = new GridBagConstraints();
            gbcRight.gridx = 1;
            gbcRight.gridy = 0;
            gbcRight.weightx = 0.48;
            gbcRight.weighty = 1.0;
            gbcRight.fill = GridBagConstraints.BOTH;
            gbcRight.insets = new Insets(0, 8, 0, 0);

            splitGrid.add(leftCol, gbcLeft);
            splitGrid.add(rightCol, gbcRight);
        }
        splitGrid.revalidate();
        splitGrid.repaint();
    }

    // -------------------------------------------------------------
    // CARD 1: BUSINESS PROFILE (LEFT)
    // -------------------------------------------------------------
    private JPanel createBusinessProfileCard() {
        JPanel card = createStandardCard(new Color(22, 101, 52)); // Forest green accent
        card.setLayout(new BorderLayout(0, 12));

        // Header Row
        JPanel headerRow = new JPanel(new BorderLayout(10, 0));
        headerRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        businessSectionLabel = new JLabel(I18n.get("settings.section.business"));
        businessSectionLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        businessSectionLabel.setForeground(new Color(15, 23, 42));

        businessSectionDescLabel = new JLabel("<html>" + I18n.get("settings.section.business_desc") + "</html>");
        businessSectionDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        businessSectionDescLabel.setForeground(new Color(100, 116, 139));

        titleBox.add(businessSectionLabel);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(businessSectionDescLabel);

        JPanel identityBadge = createPillBadge(
                I18n.get("settings.badge.identity"),
                new Color(240, 253, 244),
                new Color(187, 247, 208),
                new Color(21, 128, 61)
        );
        businessIdentityBadgeLabel = (JLabel) identityBadge.getComponent(0);

        headerRow.add(titleBox, BorderLayout.CENTER);
        headerRow.add(identityBadge, BorderLayout.EAST);
        card.add(headerRow, BorderLayout.NORTH);

        // Form Fields (Adaptive GridBagLayout)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        businessNameField = createStyledTextField();
        ownerNameField = createStyledTextField();
        phoneField = createStyledTextField();
        villageField = createStyledTextField();

        businessNameLabel = new JLabel(I18n.get("settings.label.business_name"));
        businessNameHelperLabel = new JLabel("<html>" + I18n.get("settings.label.business_name_helper") + "</html>");
        addFormRow(formPanel, 0, businessNameLabel, businessNameHelperLabel, businessNameField);

        ownerNameLabel = new JLabel(I18n.get("settings.label.owner_name"));
        ownerNameHelperLabel = new JLabel("<html>" + I18n.get("settings.label.owner_name_helper") + "</html>");
        addFormRow(formPanel, 1, ownerNameLabel, ownerNameHelperLabel, ownerNameField);

        phoneLabel = new JLabel(I18n.get("settings.label.phone"));
        phoneHelperLabel = new JLabel("<html>" + I18n.get("settings.label.phone_helper") + "</html>");
        addFormRow(formPanel, 2, phoneLabel, phoneHelperLabel, phoneField);

        villageLabel = new JLabel(I18n.get("settings.label.village"));
        villageHelperLabel = new JLabel("<html>" + I18n.get("settings.label.village_helper") + "</html>");
        addFormRow(formPanel, 3, villageLabel, villageHelperLabel, villageField);

        card.add(formPanel, BorderLayout.CENTER);
        return card;
    }

    // -------------------------------------------------------------
    // CARD 2: SERVER & SYNCHRONIZATION (LEFT)
    // -------------------------------------------------------------
    private JPanel createServerSyncCard() {
        JPanel card = createStandardCard(new Color(37, 99, 235)); // Royal blue accent
        card.setLayout(new BorderLayout(0, 12));

        // Header
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        serverSectionLabel = new JLabel(I18n.get("settings.section.server"));
        serverSectionLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        serverSectionLabel.setForeground(new Color(15, 23, 42));

        serverSectionDescLabel = new JLabel("<html>" + I18n.get("settings.section.server_desc") + "</html>");
        serverSectionDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        serverSectionDescLabel.setForeground(new Color(100, 116, 139));

        titleBox.add(serverSectionLabel);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(serverSectionDescLabel);
        card.add(titleBox, BorderLayout.NORTH);

        // Body
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setOpaque(false);

        // Server URL input row
        JPanel formRow = new JPanel(new GridBagLayout());
        formRow.setOpaque(false);

        serverUrlField = createStyledTextField();
        serverUrlLabel = new JLabel(I18n.get("settings.label.server_url"));
        serverUrlHelperLabel = new JLabel("<html>" + I18n.get("settings.label.server_url_helper") + "</html>");
        addFormRow(formRow, 0, serverUrlLabel, serverUrlHelperLabel, serverUrlField);

        bodyPanel.add(formRow);
        bodyPanel.add(Box.createVerticalStrut(12));

        // Informational status container
        JPanel infoBox = new JPanel(new BorderLayout(10, 0));
        infoBox.setBackground(new Color(248, 250, 252));
        infoBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JComponent syncDot = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37, 99, 235));
                g2.fillOval(2, 4, 8, 8);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(14, 16);
            }
        };

        JPanel infoText = new JPanel();
        infoText.setLayout(new BoxLayout(infoText, BoxLayout.Y_AXIS));
        infoText.setOpaque(false);

        serverModeTitleLabel = new JLabel(I18n.get("settings.server.mode_title"));
        serverModeTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        serverModeTitleLabel.setForeground(new Color(30, 58, 138));

        serverModeDescLabel = new JLabel("<html>" + I18n.get("settings.server.mode_desc") + "</html>");
        serverModeDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        serverModeDescLabel.setForeground(new Color(71, 85, 105));

        infoText.add(serverModeTitleLabel);
        infoText.add(Box.createVerticalStrut(3));
        infoText.add(serverModeDescLabel);

        infoBox.add(syncDot, BorderLayout.WEST);
        infoBox.add(infoText, BorderLayout.CENTER);

        bodyPanel.add(infoBox);
        card.add(bodyPanel, BorderLayout.CENTER);

        return card;
    }

    // -------------------------------------------------------------
    // CARD 3: APPLICATION PREFERENCES (RIGHT)
    // -------------------------------------------------------------
    private JPanel createPreferencesCard() {
        JPanel card = createStandardCard(new Color(79, 70, 229)); // Indigo accent
        card.setLayout(new BorderLayout(0, 12));

        // Header
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        langSectionLabel = new JLabel(I18n.get("settings.section.language"));
        langSectionLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        langSectionLabel.setForeground(new Color(15, 23, 42));

        langSectionDescLabel = new JLabel("<html>" + I18n.get("settings.section.language_desc") + "</html>");
        langSectionDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        langSectionDescLabel.setForeground(new Color(100, 116, 139));

        titleBox.add(langSectionLabel);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(langSectionDescLabel);
        card.add(titleBox, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);

        languageCombo = new JComboBox<>(new LanguageItem[]{
                new LanguageItem("en", "English"),
                new LanguageItem("mr", "मराठी (Marathi)"),
                new LanguageItem("hi", "हिंदी (Hindi)")
        });
        languageCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        languageCombo.setPreferredSize(new Dimension(140, 36));
        languageCombo.setMinimumSize(new Dimension(100, 36));
        languageCombo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Immediate runtime language change listener
        languageCombo.addActionListener(e -> {
            if (isUpdatingLocale) return;
            LanguageItem item = (LanguageItem) languageCombo.getSelectedItem();
            if (item != null && !item.getCode().equalsIgnoreCase(I18n.getLanguageCode())) {
                settingsDAO.saveSetting("app_language", item.getCode());
                I18n.setLanguage(item.getCode());
            }
        });

        langSelectLabel = new JLabel(I18n.get("settings.label.language"));
        langSelectHelperLabel = new JLabel("<html>" + I18n.get("settings.label.language_helper") + "</html>");
        addFormRow(row, 0, langSelectLabel, langSelectHelperLabel, languageCombo);

        body.add(row);
        body.add(Box.createVerticalStrut(8));

        langNoteLabel = new JLabel("<html>ⓘ  " + I18n.get("settings.label.language_note") + "</html>");
        langNoteLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        langNoteLabel.setForeground(new Color(100, 116, 139));
        body.add(langNoteLabel);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // -------------------------------------------------------------
    // CARD 4: LOCAL DATA MANAGEMENT (RIGHT)
    // -------------------------------------------------------------
    private JPanel createDataManagementCard() {
        JPanel card = createStandardCard(new Color(217, 119, 6)); // Amber accent
        card.setLayout(new BorderLayout(0, 12));

        // Header
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        backupSectionLabel = new JLabel(I18n.get("settings.section.backup"));
        backupSectionLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        backupSectionLabel.setForeground(new Color(15, 23, 42));

        backupSectionDescLabel = new JLabel("<html>" + I18n.get("settings.section.backup_desc") + "</html>");
        backupSectionDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        backupSectionDescLabel.setForeground(new Color(100, 116, 139));

        titleBox.add(backupSectionLabel);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(backupSectionDescLabel);
        card.add(titleBox, BorderLayout.NORTH);

        // Action Block with responsive vertical-safe flow
        JPanel actionBox = new JPanel(new BorderLayout(12, 10));
        actionBox.setBackground(new Color(254, 252, 232)); // Warm amber-50
        actionBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(254, 240, 138), 1),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Top Row: Custom painted database disk icon + Text description
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);

        JComponent dbIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int iconW = 26;
                int iconH = 28;
                int x = (w - iconW) / 2;
                int y = (h - iconH) / 2;

                Color baseColor = new Color(180, 83, 9);
                Color fillColor = new Color(254, 243, 199);
                for (int i = 0; i < 3; i++) {
                    int py = y + (i * 9);
                    g2.setColor(fillColor);
                    g2.fillRoundRect(x, py, iconW, 9, 6, 6);
                    g2.setColor(baseColor);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(x, py, iconW, 9, 6, 6);
                }
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 34);
            }
        };

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        backupTitleLabel = new JLabel(I18n.get("settings.backup.title"));
        backupTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        backupTitleLabel.setForeground(new Color(120, 53, 15));

        backupDescLabel = new JLabel("<html>" + I18n.get("settings.backup.desc") + "</html>");
        backupDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        backupDescLabel.setForeground(new Color(146, 64, 14));

        backupNoteLabel = new JLabel("<html>•  " + I18n.get("settings.backup.note") + "</html>");
        backupNoteLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        backupNoteLabel.setForeground(new Color(180, 83, 9));

        textCol.add(backupTitleLabel);
        textCol.add(Box.createVerticalStrut(3));
        textCol.add(backupDescLabel);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(backupNoteLabel);

        topRow.add(dbIcon, BorderLayout.WEST);
        topRow.add(textCol, BorderLayout.CENTER);

        // Bottom Row: Action Button aligned cleanly to the right
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);

        backupButton = new JButton(I18n.get("settings.btn.backup"));
        backupButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        backupButton.setFocusPainted(false);
        backupButton.setBackground(Color.WHITE);
        backupButton.setForeground(new Color(15, 23, 42));
        backupButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backupButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(7, 14, 7, 14)
        ));
        backupButton.addActionListener(e -> performDatabaseBackup());
        btnRow.add(backupButton);

        actionBox.add(topRow, BorderLayout.CENTER);
        actionBox.add(btnRow, BorderLayout.SOUTH);

        card.add(actionBox, BorderLayout.CENTER);
        return card;
    }

    // -------------------------------------------------------------
    // CARD 5: ABOUT KRISHI-DB (RIGHT)
    // -------------------------------------------------------------
    private JPanel createAboutCard() {
        JPanel card = createStandardCard(new Color(100, 116, 139)); // Slate accent
        card.setLayout(new BorderLayout(0, 12));

        // Header
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        aboutSectionLabel = new JLabel(I18n.get("settings.section.about"));
        aboutSectionLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        aboutSectionLabel.setForeground(new Color(15, 23, 42));

        aboutSectionDescLabel = new JLabel("<html>" + I18n.get("settings.section.about_desc") + "</html>");
        aboutSectionDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        aboutSectionDescLabel.setForeground(new Color(100, 116, 139));

        titleBox.add(aboutSectionLabel);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(aboutSectionDescLabel);
        card.add(titleBox, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Product Identity Row (FlowLayout allows wrapping on narrow cards)
        JPanel idRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        idRow.setOpaque(false);

        aboutProductNameLabel = new JLabel("Krishi-DB");
        aboutProductNameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        aboutProductNameLabel.setForeground(new Color(15, 23, 42));

        JPanel versionPill = createPillBadge(
                I18n.get("settings.about.version"),
                new Color(239, 246, 255),
                new Color(191, 219, 254),
                new Color(29, 78, 216)
        );
        versionBadgeLabel = (JLabel) versionPill.getComponent(0);

        JPanel modePill = createPillBadge(
                I18n.get("settings.about.mode"),
                new Color(241, 245, 249),
                new Color(203, 213, 225),
                new Color(71, 85, 105)
        );
        modeBadgeLabel = (JLabel) modePill.getComponent(0);

        idRow.add(aboutProductNameLabel);
        idRow.add(versionPill);
        idRow.add(modePill);
        body.add(idRow);
        body.add(Box.createVerticalStrut(6));

        // Description with HTML auto-wrap
        aboutDescLabel = new JLabel("<html>" + I18n.get("settings.about.desc") + "</html>");
        aboutDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        aboutDescLabel.setForeground(new Color(71, 85, 105));
        body.add(aboutDescLabel);
        body.add(Box.createVerticalStrut(10));

        // Stacked feature chips (guaranteed to fit inside any card width)
        JPanel featuresBox = new JPanel();
        featuresBox.setLayout(new BoxLayout(featuresBox, BoxLayout.Y_AXIS));
        featuresBox.setOpaque(false);

        aboutFeature1Label = createFeatureChip(I18n.get("settings.about.feature1"));
        aboutFeature2Label = createFeatureChip(I18n.get("settings.about.feature2"));
        aboutFeature3Label = createFeatureChip(I18n.get("settings.about.feature3"));

        featuresBox.add(aboutFeature1Label);
        featuresBox.add(Box.createVerticalStrut(4));
        featuresBox.add(aboutFeature2Label);
        featuresBox.add(Box.createVerticalStrut(4));
        featuresBox.add(aboutFeature3Label);

        body.add(featuresBox);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    // -------------------------------------------------------------
    // BOTTOM SAVE SETTINGS ACTION BAR
    // -------------------------------------------------------------
    private JPanel createSaveFooterBar() {
        JPanel bar = new JPanel(new BorderLayout(14, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(12, 18, 12, 18));

        // Left note with HTML wrapping
        JPanel leftNote = new JPanel();
        leftNote.setLayout(new BoxLayout(leftNote, BoxLayout.Y_AXIS));
        leftNote.setOpaque(false);

        saveIndicatorLabel = new JLabel("●  " + I18n.get("settings.save_indicator"));
        saveIndicatorLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveIndicatorLabel.setForeground(new Color(22, 101, 52));

        saveHelperLabel = new JLabel("<html>" + I18n.get("settings.save_helper") + "</html>");
        saveHelperLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        saveHelperLabel.setForeground(new Color(100, 116, 139));

        leftNote.add(saveIndicatorLabel);
        leftNote.add(Box.createVerticalStrut(2));
        leftNote.add(saveHelperLabel);

        // Right Save Button (constrained, guaranteed to remain on screen)
        saveButton = new JButton("✓  " + I18n.get("settings.btn.save"));
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveButton.setBackground(new Color(22, 101, 52)); // Forest green
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.setPreferredSize(new Dimension(160, 40));
        saveButton.setMinimumSize(new Dimension(130, 38));
        saveButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(21, 128, 61), 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        saveButton.addActionListener(e -> saveSettingsData());

        bar.add(leftNote, BorderLayout.CENTER);
        bar.add(saveButton, BorderLayout.EAST);

        return bar;
    }

    // -------------------------------------------------------------
    // UI HELPER BUILDERS
    // -------------------------------------------------------------
    private JPanel createStandardCard(Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                if (accentColor != null) {
                    g2.setColor(accentColor);
                    g2.fillRoundRect(0, 0, 4, getHeight() - 1, 4, 4);
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        return card;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(120, 36));
        field.setMinimumSize(new Dimension(80, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    private void addFormRow(JPanel container, int row, JLabel label, JLabel helper, JComponent control) {
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(15, 23, 42));

        helper.setFont(new Font("SansSerif", Font.PLAIN, 11));
        helper.setForeground(new Color(100, 116, 139));

        textCol.add(label);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(helper);

        // Left Label Column (weight 0.40)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.40;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 10);
        container.add(textCol, gbc);

        // Right Input Column (weight 0.60, expands dynamically)
        gbc.gridx = 1;
        gbc.weightx = 0.60;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        container.add(control, gbc);
    }

    private JPanel createPillBadge(String text, Color bg, Color border, Color fg) {
        JPanel pill = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        pill.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(fg);
        pill.add(label);
        return pill;
    }

    private JLabel createFeatureChip(String text) {
        JLabel chip = new JLabel("✓  " + text);
        chip.setFont(new Font("SansSerif", Font.PLAIN, 11));
        chip.setForeground(new Color(71, 85, 105));
        chip.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return chip;
    }

    // -------------------------------------------------------------
    // DATA PERSISTENCE & ACTIONS (PRESERVED FUNCTIONALITY)
    // -------------------------------------------------------------
    private void loadSettingsData() {
        String savedLang = settingsDAO.getSetting("app_language", "en");
        for (int i = 0; i < languageCombo.getItemCount(); i++) {
            if (languageCombo.getItemAt(i).getCode().equalsIgnoreCase(savedLang)) {
                languageCombo.setSelectedIndex(i);
                break;
            }
        }

        businessNameField.setText(settingsDAO.getSetting("business_name", ""));
        ownerNameField.setText(settingsDAO.getSetting("owner_name", ""));
        phoneField.setText(settingsDAO.getSetting("contact_phone", ""));
        villageField.setText(settingsDAO.getSetting("village_name", ""));
        serverUrlField.setText(settingsDAO.getSetting("server_url", "http://localhost:8080"));
    }

    private void saveSettingsData() {
        LanguageItem selectedLang = (LanguageItem) languageCombo.getSelectedItem();
        String langCode = selectedLang != null ? selectedLang.getCode() : "en";

        settingsDAO.saveSetting("app_language", langCode);
        settingsDAO.saveSetting("business_name", businessNameField.getText().trim());
        settingsDAO.saveSetting("owner_name", ownerNameField.getText().trim());
        settingsDAO.saveSetting("contact_phone", phoneField.getText().trim());
        settingsDAO.saveSetting("village_name", villageField.getText().trim());
        settingsDAO.saveSetting("server_url", serverUrlField.getText().trim());

        // Apply language immediately
        I18n.setLanguage(langCode);

        JOptionPane.showMessageDialog(
                this,
                I18n.get("settings.msg.saved"),
                I18n.get("settings.title"),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void performDatabaseBackup() {
        try {
            Path dbPath = DatabaseManager.resolveDatabasePath();
            if (!Files.exists(dbPath)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Database file does not exist yet.",
                        "Backup Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(I18n.get("settings.btn.backup"));
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            fileChooser.setSelectedFile(new File("krishidb_backup_" + timeStamp + ".db"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File targetFile = fileChooser.getSelectedFile();
                Files.copy(dbPath, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(
                        this,
                        I18n.get("settings.msg.backup_success", targetFile.getAbsolutePath()),
                        I18n.get("settings.title"),
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    I18n.get("settings.msg.backup_failed", ex.getMessage()),
                    "Backup Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @Override
    public void onLocaleChange() {
        isUpdatingLocale = true;
        try {
            // Header
            titleLabel.setText(I18n.get("settings.title"));
            subtitleLabel.setText("<html>" + I18n.get("settings.subtitle") + "</html>");
            badgeOfflineFirst.setText(I18n.get("settings.badge.offline_first"));
            badgeOfflineDesc.setText(I18n.get("settings.badge.offline_desc"));

            // Preferences
            langSectionLabel.setText(I18n.get("settings.section.language"));
            langSectionDescLabel.setText("<html>" + I18n.get("settings.section.language_desc") + "</html>");
            langSelectLabel.setText(I18n.get("settings.label.language"));
            langSelectHelperLabel.setText("<html>" + I18n.get("settings.label.language_helper") + "</html>");
            langNoteLabel.setText("<html>ⓘ  " + I18n.get("settings.label.language_note") + "</html>");

            // Business Profile
            businessSectionLabel.setText(I18n.get("settings.section.business"));
            businessSectionDescLabel.setText("<html>" + I18n.get("settings.section.business_desc") + "</html>");
            businessIdentityBadgeLabel.setText(I18n.get("settings.badge.identity"));
            businessNameLabel.setText(I18n.get("settings.label.business_name"));
            businessNameHelperLabel.setText("<html>" + I18n.get("settings.label.business_name_helper") + "</html>");
            ownerNameLabel.setText(I18n.get("settings.label.owner_name"));
            ownerNameHelperLabel.setText("<html>" + I18n.get("settings.label.owner_name_helper") + "</html>");
            phoneLabel.setText(I18n.get("settings.label.phone"));
            phoneHelperLabel.setText("<html>" + I18n.get("settings.label.phone_helper") + "</html>");
            villageLabel.setText(I18n.get("settings.label.village"));
            villageHelperLabel.setText("<html>" + I18n.get("settings.label.village_helper") + "</html>");

            // Server & Sync
            serverSectionLabel.setText(I18n.get("settings.section.server"));
            serverSectionDescLabel.setText("<html>" + I18n.get("settings.section.server_desc") + "</html>");
            serverUrlLabel.setText(I18n.get("settings.label.server_url"));
            serverUrlHelperLabel.setText("<html>" + I18n.get("settings.label.server_url_helper") + "</html>");
            serverModeTitleLabel.setText(I18n.get("settings.server.mode_title"));
            serverModeDescLabel.setText("<html>" + I18n.get("settings.server.mode_desc") + "</html>");

            // Local Data Management
            backupSectionLabel.setText(I18n.get("settings.section.backup"));
            backupSectionDescLabel.setText("<html>" + I18n.get("settings.section.backup_desc") + "</html>");
            backupTitleLabel.setText(I18n.get("settings.backup.title"));
            backupDescLabel.setText("<html>" + I18n.get("settings.backup.desc") + "</html>");
            backupNoteLabel.setText("<html>•  " + I18n.get("settings.backup.note") + "</html>");
            backupButton.setText(I18n.get("settings.btn.backup"));

            // About
            aboutSectionLabel.setText(I18n.get("settings.section.about"));
            aboutSectionDescLabel.setText("<html>" + I18n.get("settings.section.about_desc") + "</html>");
            versionBadgeLabel.setText(I18n.get("settings.about.version"));
            modeBadgeLabel.setText(I18n.get("settings.about.mode"));
            aboutDescLabel.setText("<html>" + I18n.get("settings.about.desc") + "</html>");
            aboutFeature1Label.setText("✓  " + I18n.get("settings.about.feature1"));
            aboutFeature2Label.setText("✓  " + I18n.get("settings.about.feature2"));
            aboutFeature3Label.setText("✓  " + I18n.get("settings.about.feature3"));

            // Save Footer
            saveIndicatorLabel.setText("●  " + I18n.get("settings.save_indicator"));
            saveHelperLabel.setText("<html>" + I18n.get("settings.save_helper") + "</html>");
            saveButton.setText("✓  " + I18n.get("settings.btn.save"));

            // Sync combo selection with current active language
            String currentCode = I18n.getLanguageCode();
            for (int i = 0; i < languageCombo.getItemCount(); i++) {
                if (languageCombo.getItemAt(i).getCode().equalsIgnoreCase(currentCode)) {
                    languageCombo.setSelectedIndex(i);
                    break;
                }
            }
        } finally {
            isUpdatingLocale = false;
        }

        revalidate();
        repaint();
    }
}
