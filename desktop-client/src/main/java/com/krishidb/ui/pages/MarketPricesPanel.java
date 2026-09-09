package com.krishidb.ui.pages;

import com.krishidb.dao.MarketPriceDAO;
import com.krishidb.model.MarketPrice;
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
import java.util.List;

public class MarketPricesPanel extends JPanel implements I18n.LocaleChangeListener {

    private final MarketPriceDAO marketPriceDAO;

    // Header
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton refreshButton;

    // Notice banner
    private JLabel bannerLabel;

    // Stat Cards
    private JLabel commoditiesHeading;
    private JLabel commoditiesValue;
    private JLabel commoditiesDesc;

    private JLabel marketsHeading;
    private JLabel marketsValue;
    private JLabel marketsDesc;

    private JLabel statusHeading;
    private JLabel statusValue;
    private JLabel statusDesc;

    // Filter controls
    private JComboBox<String> marketFilterBox;
    private JTextField searchField;

    // Table
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    public MarketPricesPanel() {
        this.marketPriceDAO = new MarketPriceDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        I18n.addListener(this);
        refreshMarketPrices();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel titlesPanel = new JPanel();
        titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));
        titlesPanel.setOpaque(false);

        titleLabel = new JLabel(I18n.get("market.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("market.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titlesPanel.add(titleLabel);
        titlesPanel.add(Box.createVerticalStrut(4));
        titlesPanel.add(subtitleLabel);

        refreshButton = new JButton("↻  " + I18n.get("market.btn.refresh"));
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(new Color(15, 23, 42));
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 16, 8, 16)
        ));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> refreshMarketPrices());

        headerPanel.add(titlesPanel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainContent() {
        JPanel container = new JPanel(new BorderLayout(0, 15));
        container.setOpaque(false);

        JPanel topGroup = new JPanel();
        topGroup.setLayout(new BoxLayout(topGroup, BoxLayout.Y_AXIS));
        topGroup.setOpaque(false);

        topGroup.add(createBanner());
        topGroup.add(Box.createVerticalStrut(12));
        topGroup.add(createStatCards());

        container.add(topGroup, BorderLayout.NORTH);
        container.add(createTableArea(), BorderLayout.CENTER);

        return container;
    }

    private JPanel createBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(238, 242, 255));
        banner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(199, 210, 254)),
                new EmptyBorder(10, 16, 10, 16)
        ));

        bannerLabel = new JLabel("ℹ  " + I18n.get("market.banner.offline_notice"));
        bannerLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        bannerLabel.setForeground(new Color(49, 46, 129));

        banner.add(bannerLabel, BorderLayout.CENTER);
        return banner;
    }

    private JPanel createStatCards() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 95));

        // Card 1: Commodities Count
        JPanel card1 = new JPanel();
        card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
        card1.setBackground(Color.WHITE);
        card1.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 18, 12, 18)
        ));
        commoditiesHeading = new JLabel(I18n.get("market.card.commodities"));
        commoditiesHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        commoditiesHeading.setForeground(new Color(100, 116, 139));
        commoditiesValue = new JLabel("0");
        commoditiesValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        commoditiesValue.setForeground(new Color(22, 101, 52));
        commoditiesDesc = new JLabel(I18n.get("market.card.commodities_desc"));
        commoditiesDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        commoditiesDesc.setForeground(new Color(148, 163, 184));
        card1.add(commoditiesHeading);
        card1.add(Box.createVerticalStrut(4));
        card1.add(commoditiesValue);
        card1.add(Box.createVerticalStrut(2));
        card1.add(commoditiesDesc);

        // Card 2: APMC Mandis
        JPanel card2 = new JPanel();
        card2.setLayout(new BoxLayout(card2, BoxLayout.Y_AXIS));
        card2.setBackground(Color.WHITE);
        card2.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 18, 12, 18)
        ));
        marketsHeading = new JLabel(I18n.get("market.card.markets"));
        marketsHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        marketsHeading.setForeground(new Color(100, 116, 139));
        marketsValue = new JLabel("0");
        marketsValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        marketsValue.setForeground(new Color(30, 58, 138));
        marketsDesc = new JLabel(I18n.get("market.card.markets_desc"));
        marketsDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        marketsDesc.setForeground(new Color(148, 163, 184));
        card2.add(marketsHeading);
        card2.add(Box.createVerticalStrut(4));
        card2.add(marketsValue);
        card2.add(Box.createVerticalStrut(2));
        card2.add(marketsDesc);

        // Card 3: Storage Engine
        JPanel card3 = new JPanel();
        card3.setLayout(new BoxLayout(card3, BoxLayout.Y_AXIS));
        card3.setBackground(Color.WHITE);
        card3.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 18, 12, 18)
        ));
        statusHeading = new JLabel(I18n.get("market.card.source"));
        statusHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusHeading.setForeground(new Color(100, 116, 139));
        statusValue = new JLabel("Offline SQLite");
        statusValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        statusValue.setForeground(new Color(15, 23, 42));
        statusDesc = new JLabel(I18n.get("market.card.source_desc"));
        statusDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusDesc.setForeground(new Color(148, 163, 184));
        card3.add(statusHeading);
        card3.add(Box.createVerticalStrut(4));
        card3.add(statusValue);
        card3.add(Box.createVerticalStrut(2));
        card3.add(statusDesc);

        cardsPanel.add(card1);
        cardsPanel.add(card2);
        cardsPanel.add(card3);

        return cardsPanel;
    }

    private JPanel createTableArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setOpaque(false);

        JLabel tableTitle = new JLabel(I18n.get("market.table.title"));
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        tableTitle.setForeground(new Color(15, 23, 42));

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTools.setOpaque(false);

        marketFilterBox = new JComboBox<>();
        marketFilterBox.setPreferredSize(new Dimension(160, 32));
        marketFilterBox.addActionListener(e -> applyFilter());

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(220, 32));
        searchField.putClientProperty("JTextField.placeholderText", I18n.get("market.search_placeholder"));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        rightTools.add(new JLabel(I18n.get("market.filter.market") + ":"));
        rightTools.add(marketFilterBox);
        rightTools.add(searchField);

        toolbar.add(tableTitle, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);

        // Table
        String[] cols = {
                "#",
                I18n.get("market.col.commodity"),
                I18n.get("market.col.market"),
                I18n.get("market.col.district"),
                I18n.get("market.col.price"),
                I18n.get("market.col.unit"),
                I18n.get("market.col.date")
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.RIGHT);
                c.setForeground(new Color(22, 101, 52));
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void refreshMarketPrices() {
        // Load markets into combo
        String currentSelection = (String) marketFilterBox.getSelectedItem();
        marketFilterBox.removeAllItems();
        marketFilterBox.addItem("ALL");
        List<String> markets = marketPriceDAO.getDistinctMarkets();
        for (String m : markets) {
            marketFilterBox.addItem(m);
        }
        if (currentSelection != null) {
            marketFilterBox.setSelectedItem(currentSelection);
        }

        tableModel.setRowCount(0);
        List<MarketPrice> list = marketPriceDAO.getAllPrices();
        int idx = 1;
        for (MarketPrice mp : list) {
            tableModel.addRow(new Object[]{
                    idx++,
                    mp.getCommodity(),
                    mp.getMarket() != null ? mp.getMarket() : "-",
                    mp.getDistrict() != null ? mp.getDistrict() : "-",
                    "\u20B9" + String.format("%.2f", mp.getPrice()),
                    mp.getUnit(),
                    mp.getRecordedAt() != null ? mp.getRecordedAt() : "-"
            });
        }

        commoditiesValue.setText(String.valueOf(marketPriceDAO.getCommoditiesCount()));
        marketsValue.setText(String.valueOf(markets.size()));
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        String selectedMarket = (String) marketFilterBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();

        if (text != null && !text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + text));
        }

        if (selectedMarket != null && !selectedMarket.isEmpty() && !"ALL".equalsIgnoreCase(selectedMarket)) {
            filters.add(RowFilter.regexFilter("(?i)^" + selectedMarket + "$", 2));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    @Override
    public void onLocaleChange() {
        titleLabel.setText(I18n.get("market.title"));
        subtitleLabel.setText(I18n.get("market.subtitle"));
        refreshButton.setText("↻  " + I18n.get("market.btn.refresh"));
        bannerLabel.setText("ℹ  " + I18n.get("market.banner.offline_notice"));

        commoditiesHeading.setText(I18n.get("market.card.commodities"));
        commoditiesDesc.setText(I18n.get("market.card.commodities_desc"));
        marketsHeading.setText(I18n.get("market.card.markets"));
        marketsDesc.setText(I18n.get("market.card.markets_desc"));
        statusHeading.setText(I18n.get("market.card.source"));
        statusDesc.setText(I18n.get("market.card.source_desc"));

        searchField.putClientProperty("JTextField.placeholderText", I18n.get("market.search_placeholder"));

        refreshMarketPrices();
    }
}
