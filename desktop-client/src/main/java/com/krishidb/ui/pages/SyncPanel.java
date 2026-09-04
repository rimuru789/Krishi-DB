package com.krishidb.ui.pages;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import com.krishidb.dao.SyncDAO;
import com.krishidb.ui.MainFrame;
import com.krishidb.dao.SyncQueueDAO;
import com.krishidb.model.SyncRecord;
import com.krishidb.util.I18n;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class SyncPanel extends JPanel implements I18n.LocaleChangeListener {

    private final MainFrame mainFrame;

    private JLabel titleLabel;
    private JButton syncButton;
    private JLabel statusLabel;

    private JTable historyTable;
    private DefaultTableModel historyModel;
    private JScrollPane scrollPane;
    private TitledBorder historyBorder;

    public SyncPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));

        titleLabel = new JLabel(I18n.get("sync.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

        add(titleLabel, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        syncButton = new JButton(I18n.get("sync.btn.sync_now"));
        syncButton.setPreferredSize(new Dimension(160, 45));
        syncButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        syncButton.putClientProperty("JButton.buttonType", "roundRect");

        statusLabel = new JLabel(I18n.get("sync.status.ready"));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        syncButton.addActionListener(e -> {
            statusLabel.setText(I18n.get("sync.status.in_progress"));
            syncButton.setEnabled(false);

            // Run in background worker so UI doesn't freeze
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    SyncDAO dao = new SyncDAO();
                    return dao.syncProductsToServer();
                }

                @Override
                protected void done() {
                    syncButton.setEnabled(true);
                    try {
                        boolean result = get();
                        if (result) {
                            statusLabel.setText(I18n.get("sync.status.completed"));
                            mainFrame.updateSidebarStatus(true, 0);
                            JOptionPane.showMessageDialog(
                                    SyncPanel.this,
                                    I18n.get("sync.msg.success"),
                                    I18n.get("sync.title"),
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            mainFrame.refreshInventory();
                            loadSyncHistory();
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
                }
            };
            worker.execute();
        });

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        actionPanel.setOpaque(false);
        actionPanel.add(syncButton);
        actionPanel.add(statusLabel);

        center.add(actionPanel, BorderLayout.CENTER);

        String[] columns = getColumnNames();
        historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(36);

        scrollPane = new JScrollPane(historyTable);
        historyBorder = BorderFactory.createTitledBorder(I18n.get("sync.history.title"));
        scrollPane.setBorder(historyBorder);
        scrollPane.setPreferredSize(new Dimension(900, 340));

        center.add(scrollPane, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        loadSyncHistory();
        I18n.addListener(this);
    }

    private String[] getColumnNames() {
        return new String[]{
                I18n.get("sync.col.table"),
                I18n.get("sync.col.operation"),
                I18n.get("sync.col.status"),
                I18n.get("sync.col.time")
        };
    }

    private void loadSyncHistory() {
        historyModel.setRowCount(0);
        SyncQueueDAO dao = new SyncQueueDAO();
        List<SyncRecord> records = dao.getSyncHistory();

        for (SyncRecord record : records) {
            historyModel.addRow(new Object[]{
                    record.getTableName(),
                    record.getOperation(),
                    record.getStatus(),
                    record.getCreatedAt()
            });
        }
    }

    @Override
    public void onLocaleChange() {
        titleLabel.setText(I18n.get("sync.title"));
        syncButton.setText(I18n.get("sync.btn.sync_now"));
        statusLabel.setText(I18n.get("sync.status.ready"));

        historyBorder.setTitle(I18n.get("sync.history.title"));
        scrollPane.repaint();

        String[] columns = getColumnNames();
        for (int i = 0; i < columns.length; i++) {
            historyTable.getColumnModel().getColumn(i).setHeaderValue(columns[i]);
        }
        historyTable.getTableHeader().repaint();

        revalidate();
        repaint();
    }
}