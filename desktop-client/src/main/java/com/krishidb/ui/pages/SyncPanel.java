package com.krishidb.ui.pages;

import com.krishidb.dao.SettingsDAO;
import com.krishidb.dao.SyncDAO;
import com.krishidb.dao.SyncQueueDAO;
import com.krishidb.model.SyncRecord;
import com.krishidb.ui.MainFrame;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class SyncPanel extends JPanel implements I18n.LocaleChangeListener {

    private final MainFrame mainFrame;
    private final SyncDAO syncDAO;
    private final SyncQueueDAO syncQueueDAO;
    private final SettingsDAO settingsDAO;

    // Header & Status Banner
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton syncButton;
    private JButton refreshButton;
    private JLabel statusLabel;

    private JLabel bannerStatusBadge;
    private JLabel bannerModeLabel;
    private JLabel bannerDescLabel;
    private JLabel bannerServerVal;
    private JLabel bannerQueueVal;
    private JLabel bannerLastSyncVal;

    // Summary Cards (4 Metric Cards)
    private JLabel pendingVal;
    private JLabel pendingSub;
    private JLabel pendingTitle;

    private JLabel syncedVal;
    private JLabel syncedSub;
    private JLabel syncedTitle;

    private JLabel failedVal;
    private JLabel failedSub;
    private JLabel failedTitle;

    private JLabel lastSyncCardVal;
    private JLabel lastSyncCardSub;
    private JLabel lastSyncCardTitle;

    // Module Breakdown Panel
    private JLabel moduleTitle;
    private JLabel moduleSubtitle;
    private JPanel moduleItemsContainer;

    // Offline Protection Panel
    private JLabel protectTitle;
    private JLabel protectP1Title;
    private JLabel protectP1Desc;
    private JLabel protectP2Title;
    private JLabel protectP2Desc;
    private JLabel protectP3Title;
    private JLabel protectP3Desc;
    private JLabel protectP4Title;
    private JLabel protectP4Desc;

    // History Table
    private JLabel historyTitle;
    private JLabel historySubtitle;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private JScrollPane historyScroll;
    private JPanel historyCard;
    private JPanel historyEmptyState;

    public SyncPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.syncDAO = new SyncDAO();
        this.syncQueueDAO = new SyncQueueDAO();
        this.settingsDAO = new SettingsDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));

        // Scrollable root container for smooth resizing
        JPanel rootContainer = new JPanel(new BorderLayout(0, 18));
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
        refreshSyncCenter();
    }

    // -------------------------------------------------------------
    // TOP HEADER & STATUS BANNER
    // -------------------------------------------------------------
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        // Titles Row
        JPanel titlesRow = new JPanel(new BorderLayout());
        titlesRow.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        titleLabel = new JLabel(I18n.get("sync.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("sync.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLabel);

        // Action Buttons Row
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        syncButton = new JButton("↻  " + I18n.get("sync.btn.sync_now"));
        syncButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        syncButton.setPreferredSize(new Dimension(160, 38));
        syncButton.setBackground(new Color(22, 101, 52));
        syncButton.setForeground(Color.WHITE);
        syncButton.setFocusPainted(false);
        syncButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        syncButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(21, 128, 61), 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        syncButton.addActionListener(e -> performSync());

        refreshButton = new JButton("↻  " + I18n.get("sync.btn.refresh"));
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(new Color(15, 23, 42));
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> refreshSyncCenter());

        actionPanel.add(syncButton);
        actionPanel.add(refreshButton);

        titlesRow.add(titleBlock, BorderLayout.WEST);
        titlesRow.add(actionPanel, BorderLayout.EAST);

        headerPanel.add(titlesRow);
        headerPanel.add(Box.createVerticalStrut(14));
        headerPanel.add(createOfflineStatusBanner());

        return headerPanel;
    }

    // -------------------------------------------------------------
    // OFFLINE STATUS BANNER
    // -------------------------------------------------------------
    private JPanel createOfflineStatusBanner() {
        JPanel banner = new JPanel(new BorderLayout(16, 12));
        banner.setBackground(new Color(254, 243, 199)); // Soft amber tint
        banner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(251, 191, 36), 1),
                new EmptyBorder(14, 18, 14, 18)
        ));

        // Left Status Block
        JPanel leftBlock = new JPanel();
        leftBlock.setLayout(new BoxLayout(leftBlock, BoxLayout.Y_AXIS));
        leftBlock.setOpaque(false);

        JPanel topPillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topPillRow.setOpaque(false);

        bannerStatusBadge = new JLabel(" \u25CF  " + I18n.get("status.offline") + " ");
        bannerStatusBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        bannerStatusBadge.setForeground(new Color(146, 64, 14));
        bannerStatusBadge.setBackground(new Color(253, 230, 138));
        bannerStatusBadge.setOpaque(true);
        bannerStatusBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        bannerModeLabel = new JLabel(I18n.get("sync.banner.mode") + ": " + I18n.get("sync.banner.mode_active"));
        bannerModeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        bannerModeLabel.setForeground(new Color(146, 64, 14));

        topPillRow.add(bannerStatusBadge);
        topPillRow.add(bannerModeLabel);

        bannerDescLabel = new JLabel(I18n.get("sync.banner.desc"));
        bannerDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bannerDescLabel.setForeground(new Color(120, 53, 15));

        leftBlock.add(topPillRow);
        leftBlock.add(Box.createVerticalStrut(6));
        leftBlock.add(bannerDescLabel);

        // Right Meta Information Block
        JPanel rightBlock = new JPanel();
        rightBlock.setLayout(new BoxLayout(rightBlock, BoxLayout.Y_AXIS));
        rightBlock.setOpaque(false);

        String serverUrl = settingsDAO.getSetting("server_url", "http://localhost:8080");
        bannerServerVal = new JLabel(I18n.get("sync.banner.server") + " " + serverUrl);
        bannerServerVal.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bannerServerVal.setForeground(new Color(120, 53, 15));

        bannerQueueVal = new JLabel(I18n.get("sync.banner.pending_queue") + " 0 items");
        bannerQueueVal.setFont(new Font("SansSerif", Font.BOLD, 12));
        bannerQueueVal.setForeground(new Color(146, 64, 14));

        bannerLastSyncVal = new JLabel(I18n.get("sync.banner.last_sync") + " " + I18n.get("sync.banner.never"));
        bannerLastSyncVal.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bannerLastSyncVal.setForeground(new Color(146, 64, 14));

        statusLabel = new JLabel(I18n.get("sync.status.ready"));
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        statusLabel.setForeground(new Color(120, 53, 15));

        rightBlock.add(bannerServerVal);
        rightBlock.add(Box.createVerticalStrut(2));
        rightBlock.add(bannerQueueVal);
        rightBlock.add(Box.createVerticalStrut(2));
        rightBlock.add(bannerLastSyncVal);
        rightBlock.add(Box.createVerticalStrut(3));
        rightBlock.add(statusLabel);

        banner.add(leftBlock, BorderLayout.CENTER);
        banner.add(rightBlock, BorderLayout.EAST);

        return banner;
    }

    // -------------------------------------------------------------
    // MAIN CONTENT
    // -------------------------------------------------------------
    private JPanel createMainContent() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        container.add(createSummaryCardsGrid());
        container.add(Box.createVerticalStrut(18));
        container.add(createSplitBody());

        return container;
    }

    // -------------------------------------------------------------
    // SYNC SUMMARY CARDS (4 Metric Cards in a Row)
    // -------------------------------------------------------------
    private JPanel createSummaryCardsGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(0, 96));

        // 1. Pending Changes
        JPanel c1 = createRichCardBase(new Color(217, 119, 6));
        pendingTitle = createCardTitle(I18n.get("sync.card.pending"));
        pendingVal = createCardValue("0", new Color(217, 119, 6));
        pendingSub = createCardSub(I18n.get("sync.card.pending_desc"));
        c1.add(pendingTitle);
        c1.add(Box.createVerticalStrut(4));
        c1.add(pendingVal);
        c1.add(Box.createVerticalStrut(3));
        c1.add(pendingSub);

        // 2. Synced Changes
        JPanel c2 = createRichCardBase(new Color(22, 101, 52));
        syncedTitle = createCardTitle(I18n.get("sync.card.synced"));
        syncedVal = createCardValue("0", new Color(22, 101, 52));
        syncedSub = createCardSub(I18n.get("sync.card.synced_desc"));
        c2.add(syncedTitle);
        c2.add(Box.createVerticalStrut(4));
        c2.add(syncedVal);
        c2.add(Box.createVerticalStrut(3));
        c2.add(syncedSub);

        // 3. Failed Attempts
        JPanel c3 = createRichCardBase(new Color(185, 28, 28));
        failedTitle = createCardTitle(I18n.get("sync.card.failed"));
        failedVal = createCardValue("0", new Color(185, 28, 28));
        failedSub = createCardSub(I18n.get("sync.card.failed_desc"));
        c3.add(failedTitle);
        c3.add(Box.createVerticalStrut(4));
        c3.add(failedVal);
        c3.add(Box.createVerticalStrut(3));
        c3.add(failedSub);

        // 4. Last Sync Time
        JPanel c4 = createRichCardBase(new Color(30, 58, 138));
        lastSyncCardTitle = createCardTitle(I18n.get("sync.card.last_sync"));
        lastSyncCardVal = createCardValue(I18n.get("sync.banner.never"), new Color(15, 23, 42));
        lastSyncCardVal.setFont(new Font("SansSerif", Font.BOLD, 15));
        lastSyncCardSub = createCardSub(I18n.get("sync.card.last_sync_desc"));
        c4.add(lastSyncCardTitle);
        c4.add(Box.createVerticalStrut(6));
        c4.add(lastSyncCardVal);
        c4.add(Box.createVerticalStrut(4));
        c4.add(lastSyncCardSub);

        grid.add(c1);
        grid.add(c2);
        grid.add(c3);
        grid.add(c4);

        return grid;
    }

    // -------------------------------------------------------------
    // RESPONSIVE SPLIT BODY (38% Left, 62% Right using GridBagLayout)
    // -------------------------------------------------------------
    private JPanel createSplitBody() {
        JPanel split = new JPanel(new GridBagLayout());
        split.setOpaque(false);

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.weightx = 0.38;
        gbcLeft.weighty = 1.0;
        gbcLeft.fill = GridBagConstraints.BOTH;
        gbcLeft.insets = new Insets(0, 0, 0, 9);

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 1;
        gbcRight.gridy = 0;
        gbcRight.weightx = 0.62;
        gbcRight.weighty = 1.0;
        gbcRight.fill = GridBagConstraints.BOTH;
        gbcRight.insets = new Insets(0, 9, 0, 0);

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        leftCol.add(createModuleBreakdownPanel());
        leftCol.add(Box.createVerticalStrut(16));
        leftCol.add(createProtectionPanel());

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        rightCol.add(createHistoryPanel());

        split.add(leftCol, gbcLeft);
        split.add(rightCol, gbcRight);

        return split;
    }

    // -------------------------------------------------------------
    // LEFT COLUMN 1: PENDING CHANGES BY MODULE
    // -------------------------------------------------------------
    private JPanel createModuleBreakdownPanel() {
        JPanel card = createStandardCard();
        card.setLayout(new BorderLayout(0, 12));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        moduleTitle = new JLabel(I18n.get("sync.module.title"));
        moduleTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        moduleTitle.setForeground(new Color(15, 23, 42));

        moduleSubtitle = new JLabel(I18n.get("sync.module.subtitle"));
        moduleSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        moduleSubtitle.setForeground(new Color(100, 116, 139));

        titles.add(moduleTitle);
        titles.add(Box.createVerticalStrut(2));
        titles.add(moduleSubtitle);
        card.add(titles, BorderLayout.NORTH);

        moduleItemsContainer = new JPanel();
        moduleItemsContainer.setLayout(new BoxLayout(moduleItemsContainer, BoxLayout.Y_AXIS));
        moduleItemsContainer.setOpaque(false);

        card.add(moduleItemsContainer, BorderLayout.CENTER);
        return card;
    }

    // -------------------------------------------------------------
    // LEFT COLUMN 2: OFFLINE-FIRST PROTECTION PANEL
    // -------------------------------------------------------------
    private JPanel createProtectionPanel() {
        JPanel card = createStandardCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        protectTitle = new JLabel("[ \u25A0 ]  " + I18n.get("sync.protect.title"));
        protectTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        protectTitle.setForeground(new Color(30, 58, 138));
        protectTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(protectTitle);
        card.add(Box.createVerticalStrut(12));

        // Point 1
        protectP1Title = createProtectTitle(I18n.get("sync.protect.point1_title"));
        protectP1Desc = createProtectDesc(I18n.get("sync.protect.point1_desc"));
        card.add(protectP1Title);
        card.add(Box.createVerticalStrut(2));
        card.add(protectP1Desc);
        card.add(Box.createVerticalStrut(10));

        // Point 2
        protectP2Title = createProtectTitle(I18n.get("sync.protect.point2_title"));
        protectP2Desc = createProtectDesc(I18n.get("sync.protect.point2_desc"));
        card.add(protectP2Title);
        card.add(Box.createVerticalStrut(2));
        card.add(protectP2Desc);
        card.add(Box.createVerticalStrut(10));

        // Point 3
        protectP3Title = createProtectTitle(I18n.get("sync.protect.point3_title"));
        protectP3Desc = createProtectDesc(I18n.get("sync.protect.point3_desc"));
        card.add(protectP3Title);
        card.add(Box.createVerticalStrut(2));
        card.add(protectP3Desc);
        card.add(Box.createVerticalStrut(10));

        // Point 4
        protectP4Title = createProtectTitle(I18n.get("sync.protect.point4_title"));
        protectP4Desc = createProtectDesc(I18n.get("sync.protect.point4_desc"));
        card.add(protectP4Title);
        card.add(Box.createVerticalStrut(2));
        card.add(protectP4Desc);

        return card;
    }

    // -------------------------------------------------------------
    // RIGHT COLUMN: SYNC EVENT HISTORY TABLE
    // -------------------------------------------------------------
    private JPanel createHistoryPanel() {
        historyCard = createStandardCard();
        historyCard.setLayout(new BorderLayout(0, 10));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        historyTitle = new JLabel(I18n.get("sync.history.title"));
        historyTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        historyTitle.setForeground(new Color(15, 23, 42));

        historySubtitle = new JLabel(I18n.get("sync.history.subtitle"));
        historySubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        historySubtitle.setForeground(new Color(100, 116, 139));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(historyTitle);
        titles.add(Box.createVerticalStrut(2));
        titles.add(historySubtitle);

        head.add(titles, BorderLayout.WEST);
        historyCard.add(head, BorderLayout.NORTH);

        String[] cols = getColumnNames();
        historyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        historyTable = new JTable(historyModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return c;
            }
        };
        historyTable.setRowHeight(32);
        historyTable.setShowVerticalLines(false);
        historyTable.setGridColor(new Color(241, 245, 249));
        historyTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        historyTable.getTableHeader().setBackground(new Color(241, 245, 249));
        historyTable.getTableHeader().setForeground(new Color(71, 85, 105));
        historyTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        // Column widths & renderers
        DefaultTableCellRenderer modRenderer = new DefaultTableCellRenderer();
        modRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        historyTable.getColumnModel().getColumn(0).setCellRenderer(modRenderer);

        historyTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        historyTable.getColumnModel().getColumn(1).setCellRenderer(new OperationBadgeRenderer());

        historyTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(2).setCellRenderer(new StatusBadgeRenderer());

        DefaultTableCellRenderer timeRenderer = new DefaultTableCellRenderer();
        timeRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        historyTable.getColumnModel().getColumn(3).setCellRenderer(timeRenderer);

        historyScroll = new JScrollPane(historyTable);
        historyScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        historyScroll.setPreferredSize(new Dimension(0, 360));

        historyEmptyState = createEmptyHistoryPlaceholder(
                I18n.get("sync.history.empty_title"),
                I18n.get("sync.history.empty_desc")
        );

        historyCard.add(historyScroll, BorderLayout.CENTER);
        return historyCard;
    }

    // -------------------------------------------------------------
    // DATA LOADING & REFRESH
    // -------------------------------------------------------------
    public void refreshSyncCenter() {
        SwingWorker<SyncDataBundle, Void> worker = new SwingWorker<>() {
            @Override
            protected SyncDataBundle doInBackground() {
                SyncDataBundle data = new SyncDataBundle();
                data.pendingCount = syncQueueDAO.getPendingCount();
                data.syncedCount = syncQueueDAO.getCompletedCount();
                data.failedCount = syncQueueDAO.getFailedCount();
                data.lastSyncTime = syncQueueDAO.getLastSyncTime();
                data.moduleCounts = syncQueueDAO.getPendingCountsByModule();
                data.history = syncQueueDAO.getSyncHistory(20);
                data.serverUrl = settingsDAO.getSetting("server_url", "http://localhost:8080");
                return data;
            }

            @Override
            protected void done() {
                try {
                    SyncDataBundle d = get();
                    updateUi(d);
                } catch (Exception e) {
                    System.err.println("Error refreshing sync data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private static class SyncDataBundle {
        int pendingCount;
        int syncedCount;
        int failedCount;
        String lastSyncTime;
        Map<String, Integer> moduleCounts;
        List<SyncRecord> history;
        String serverUrl;
    }

    private void updateUi(SyncDataBundle d) {
        // Banner info
        bannerServerVal.setText(I18n.get("sync.banner.server") + " " + d.serverUrl);
        bannerQueueVal.setText(I18n.get("sync.banner.pending_queue") + " " + d.pendingCount + " items");
        String lastSyncStr = (d.lastSyncTime != null && !d.lastSyncTime.isEmpty())
                ? d.lastSyncTime
                : I18n.get("sync.banner.never");
        bannerLastSyncVal.setText(I18n.get("sync.banner.last_sync") + " " + lastSyncStr);

        // Summary Cards
        pendingVal.setText(String.valueOf(d.pendingCount));
        syncedVal.setText(String.valueOf(d.syncedCount));
        failedVal.setText(String.valueOf(d.failedCount));
        lastSyncCardVal.setText(lastSyncStr);

        // Module Breakdown Rows
        moduleItemsContainer.removeAll();
        if (d.moduleCounts != null && !d.moduleCounts.isEmpty()) {
            int maxVal = 1;
            for (int val : d.moduleCounts.values()) {
                if (val > maxVal) maxVal = val;
            }

            for (Map.Entry<String, Integer> entry : d.moduleCounts.entrySet()) {
                String modKey = entry.getKey();
                int count = entry.getValue();
                moduleItemsContainer.add(createModuleCountRow(modKey, count, maxVal));
                moduleItemsContainer.add(Box.createVerticalStrut(6));
            }
        }
        moduleItemsContainer.revalidate();
        moduleItemsContainer.repaint();

        // History Table & Empty State
        historyModel.setRowCount(0);
        if (d.history != null && !d.history.isEmpty()) {
            for (SyncRecord rec : d.history) {
                historyModel.addRow(new Object[]{
                        rec.getTableName(),
                        rec.getOperation(),
                        rec.getStatus(),
                        rec.getCreatedAt()
                });
            }
            historyCard.remove(historyEmptyState);
            historyCard.add(historyScroll, BorderLayout.CENTER);
        } else {
            historyCard.remove(historyScroll);
            historyCard.add(historyEmptyState, BorderLayout.CENTER);
        }
        historyCard.revalidate();
        historyCard.repaint();

        if (mainFrame != null) {
            mainFrame.updateSidebarStatus(false, d.pendingCount);
        }
    }

    private JPanel createModuleCountRow(String moduleKey, int count, int maxVal) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        String labelText = getModuleDisplayName(moduleKey);
        JLabel nameLabel = new JLabel(labelText);
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        nameLabel.setForeground(new Color(51, 65, 85));

        JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightWrap.setOpaque(false);

        JLabel countBadge = new JLabel(" " + count + " ");
        countBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        if (count > 0) {
            countBadge.setForeground(new Color(180, 83, 9));
            countBadge.setBackground(new Color(254, 243, 199));
        } else {
            countBadge.setForeground(new Color(100, 116, 139));
            countBadge.setBackground(new Color(241, 245, 249));
        }
        countBadge.setOpaque(true);
        countBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        rightWrap.add(countBadge);

        row.add(nameLabel, BorderLayout.WEST);
        row.add(rightWrap, BorderLayout.EAST);
        return row;
    }

    private String getModuleDisplayName(String moduleKey) {
        return switch (moduleKey.toLowerCase()) {
            case "products" -> I18n.get("sync.module.products");
            case "customers" -> I18n.get("sync.module.customers");
            case "suppliers" -> I18n.get("sync.module.suppliers");
            case "purchases" -> I18n.get("sync.module.purchases");
            case "sales" -> I18n.get("sync.module.sales");
            case "expenses" -> I18n.get("sync.module.expenses");
            case "transactions" -> I18n.get("sync.module.transactions");
            default -> moduleKey.substring(0, 1).toUpperCase() + moduleKey.substring(1);
        };
    }

    // -------------------------------------------------------------
    // PERFORM SYNC
    // -------------------------------------------------------------
    private void performSync() {
        statusLabel.setText(I18n.get("sync.status.in_progress"));
        syncButton.setEnabled(false);

        SwingWorker<SyncDAO.SyncResult, Void> worker = new SwingWorker<>() {
            @Override
            protected SyncDAO.SyncResult doInBackground() {
                return syncDAO.syncProductsWithResult();
            }

            @Override
            protected void done() {
                syncButton.setEnabled(true);
                try {
                    SyncDAO.SyncResult result = get();
                    if (result.isServerUnavailable()) {
                        statusLabel.setText(I18n.get("sync.status.failed"));
                        JOptionPane.showMessageDialog(
                                SyncPanel.this,
                                I18n.get("sync.msg.server_unavailable"),
                                I18n.get("sync.title"),
                                JOptionPane.ERROR_MESSAGE
                        );
                    } else if (result.getTotalAttempted() == 0) {
                        statusLabel.setText(I18n.get("sync.status.ready"));
                        JOptionPane.showMessageDialog(
                                SyncPanel.this,
                                I18n.get("sync.msg.no_pending"),
                                I18n.get("sync.title"),
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else if (result.isAllSuccess()) {
                        statusLabel.setText(I18n.get("sync.status.completed"));
                        JOptionPane.showMessageDialog(
                                SyncPanel.this,
                                I18n.get("sync.msg.success"),
                                I18n.get("sync.title"),
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        if (mainFrame != null) {
                            mainFrame.refreshInventory();
                        }
                    } else if (result.isPartialSuccess()) {
                        statusLabel.setText(I18n.get("sync.status.completed"));
                        JOptionPane.showMessageDialog(
                                SyncPanel.this,
                                I18n.get("sync.msg.partial", result.getSuccessCount(), result.getFailureCount()),
                                I18n.get("sync.title"),
                                JOptionPane.WARNING_MESSAGE
                        );
                        if (mainFrame != null) {
                            mainFrame.refreshInventory();
                        }
                    } else {
                        statusLabel.setText(I18n.get("sync.status.failed"));
                        JOptionPane.showMessageDialog(
                                SyncPanel.this,
                                I18n.get("sync.msg.failed"),
                                I18n.get("sync.title"),
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    statusLabel.setText(I18n.get("sync.status.failed"));
                    JOptionPane.showMessageDialog(
                            SyncPanel.this,
                            I18n.get("sync.msg.failed"),
                            I18n.get("sync.title"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
                refreshSyncCenter();
            }
        };
        worker.execute();
    }

    // -------------------------------------------------------------
    // UI BUILDER HELPERS
    // -------------------------------------------------------------
    private JPanel createRichCardBase(Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(accentColor);
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

    private JLabel createProtectTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(new Color(15, 23, 42));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel createProtectDesc(String text) {
        JLabel l = new JLabel("<html>" + text + "</html>");
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(new Color(100, 116, 139));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel createEmptyHistoryPlaceholder(String title, String desc) {
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

    private String[] getColumnNames() {
        return new String[]{
                I18n.get("sync.col.table"),
                I18n.get("sync.col.operation"),
                I18n.get("sync.col.status"),
                I18n.get("sync.col.time")
        };
    }

    // -------------------------------------------------------------
    // BADGE RENDERERS FOR TABLE
    // -------------------------------------------------------------
    public static class OperationBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setHorizontalAlignment(SwingConstants.CENTER);

            String text = value != null ? value.toString() : "";
            Color bg;
            Color fg;

            switch (text.toUpperCase()) {
                case "INSERT" -> {
                    bg = new Color(220, 252, 231);
                    fg = new Color(21, 128, 61);
                }
                case "UPDATE" -> {
                    bg = new Color(219, 234, 254);
                    fg = new Color(30, 64, 175);
                }
                case "DELETE" -> {
                    bg = new Color(254, 226, 226);
                    fg = new Color(185, 28, 28);
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

    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setHorizontalAlignment(SwingConstants.CENTER);

            String text = value != null ? value.toString() : "";
            Color bg;
            Color fg;

            switch (text.toUpperCase()) {
                case "COMPLETED", "SYNCED" -> {
                    bg = new Color(220, 252, 231);
                    fg = new Color(21, 128, 61);
                }
                case "PENDING" -> {
                    bg = new Color(254, 243, 199);
                    fg = new Color(180, 83, 9);
                }
                case "FAILED" -> {
                    bg = new Color(254, 226, 226);
                    fg = new Color(185, 28, 28);
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
        titleLabel.setText(I18n.get("sync.title"));
        subtitleLabel.setText(I18n.get("sync.subtitle"));
        syncButton.setText("↻  " + I18n.get("sync.btn.sync_now"));
        refreshButton.setText("↻  " + I18n.get("sync.btn.refresh"));
        statusLabel.setText(I18n.get("sync.status.ready"));

        bannerStatusBadge.setText(" \u25CF  " + I18n.get("status.offline") + " ");
        bannerModeLabel.setText(I18n.get("sync.banner.mode") + ": " + I18n.get("sync.banner.mode_active"));
        bannerDescLabel.setText(I18n.get("sync.banner.desc"));

        pendingTitle.setText(I18n.get("sync.card.pending"));
        pendingSub.setText(I18n.get("sync.card.pending_desc"));

        syncedTitle.setText(I18n.get("sync.card.synced"));
        syncedSub.setText(I18n.get("sync.card.synced_desc"));

        failedTitle.setText(I18n.get("sync.card.failed"));
        failedSub.setText(I18n.get("sync.card.failed_desc"));

        lastSyncCardTitle.setText(I18n.get("sync.card.last_sync"));
        lastSyncCardSub.setText(I18n.get("sync.card.last_sync_desc"));

        moduleTitle.setText(I18n.get("sync.module.title"));
        moduleSubtitle.setText(I18n.get("sync.module.subtitle"));

        protectTitle.setText("[ \u25A0 ]  " + I18n.get("sync.protect.title"));
        protectP1Title.setText(I18n.get("sync.protect.point1_title"));
        protectP1Desc.setText("<html>" + I18n.get("sync.protect.point1_desc") + "</html>");
        protectP2Title.setText(I18n.get("sync.protect.point2_title"));
        protectP2Desc.setText("<html>" + I18n.get("sync.protect.point2_desc") + "</html>");
        protectP3Title.setText(I18n.get("sync.protect.point3_title"));
        protectP3Desc.setText("<html>" + I18n.get("sync.protect.point3_desc") + "</html>");
        protectP4Title.setText(I18n.get("sync.protect.point4_title"));
        protectP4Desc.setText("<html>" + I18n.get("sync.protect.point4_desc") + "</html>");

        historyTitle.setText(I18n.get("sync.history.title"));
        historySubtitle.setText(I18n.get("sync.history.subtitle"));

        String[] cols = getColumnNames();
        for (int i = 0; i < cols.length; i++) {
            historyTable.getColumnModel().getColumn(i).setHeaderValue(cols[i]);
        }
        historyTable.getTableHeader().repaint();

        refreshSyncCenter();
    }
}