package com.krishidb.ui.pages;

import com.krishidb.dao.SettingsDAO;
import com.krishidb.database.DatabaseManager;
import com.krishidb.ui.MainFrame;
import com.krishidb.util.I18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsPanel extends JPanel implements I18n.LocaleChangeListener {

    private final MainFrame mainFrame;
    private final SettingsDAO settingsDAO;

    // Form components
    private JComboBox<LanguageItem> languageCombo;
    private JTextField businessNameField;
    private JTextField ownerNameField;
    private JTextField phoneField;
    private JTextField villageField;
    private JTextField serverUrlField;

    // Section Titles and Labels for dynamic translation
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel langSectionLabel;
    private JLabel langSelectLabel;
    private JLabel businessSectionLabel;
    private JLabel businessNameLabel;
    private JLabel ownerNameLabel;
    private JLabel phoneLabel;
    private JLabel villageLabel;
    private JLabel serverSectionLabel;
    private JLabel serverUrlLabel;
    private JLabel backupSectionLabel;
    private JButton backupButton;
    private JButton saveButton;
    private JLabel aboutSectionLabel;
    private JLabel versionLabel;
    private JLabel descLabel;

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
        setBorder(new EmptyBorder(35, 40, 35, 40));

        initComponents();
        loadSettingsData();

        I18n.addListener(this);
    }

    private void initComponents() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        titleLabel = new JLabel(I18n.get("settings.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(15, 23, 42));

        subtitleLabel = new JLabel(I18n.get("settings.subtitle"));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(subtitleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Content Scroll Pane
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 1. Language Card
        JPanel langCard = createCardPanel();
        langSectionLabel = createSectionHeader(I18n.get("settings.section.language"));
        langSelectLabel = new JLabel(I18n.get("settings.label.language"));
        langSelectLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        languageCombo = new JComboBox<>(new LanguageItem[]{
                new LanguageItem("en", "English"),
                new LanguageItem("mr", "मराठी (Marathi)"),
                new LanguageItem("hi", "हिंदी (Hindi)")
        });
        languageCombo.setPreferredSize(new Dimension(280, 38));

        langCard.add(langSectionLabel);
        langCard.add(Box.createVerticalStrut(10));
        langCard.add(langSelectLabel);
        langCard.add(Box.createVerticalStrut(6));
        langCard.add(languageCombo);

        contentPanel.add(langCard);
        contentPanel.add(Box.createVerticalStrut(16));

        // 2. Business Profile Card
        JPanel businessCard = createCardPanel();
        businessSectionLabel = createSectionHeader(I18n.get("settings.section.business"));
        businessCard.add(businessSectionLabel);
        businessCard.add(Box.createVerticalStrut(12));

        JPanel formGrid = new JPanel(new GridLayout(4, 2, 20, 10));
        formGrid.setOpaque(false);

        businessNameLabel = new JLabel(I18n.get("settings.label.business_name"));
        businessNameField = new JTextField();
        businessNameField.setPreferredSize(new Dimension(250, 36));

        ownerNameLabel = new JLabel(I18n.get("settings.label.owner_name"));
        ownerNameField = new JTextField();
        ownerNameField.setPreferredSize(new Dimension(250, 36));

        phoneLabel = new JLabel(I18n.get("settings.label.phone"));
        phoneField = new JTextField();
        phoneField.setPreferredSize(new Dimension(250, 36));

        villageLabel = new JLabel(I18n.get("settings.label.village"));
        villageField = new JTextField();
        villageField.setPreferredSize(new Dimension(250, 36));

        formGrid.add(businessNameLabel);
        formGrid.add(businessNameField);
        formGrid.add(ownerNameLabel);
        formGrid.add(ownerNameField);
        formGrid.add(phoneLabel);
        formGrid.add(phoneField);
        formGrid.add(villageLabel);
        formGrid.add(villageField);

        businessCard.add(formGrid);
        contentPanel.add(businessCard);
        contentPanel.add(Box.createVerticalStrut(16));

        // 3. Server & Backup Card
        JPanel techCard = createCardPanel();
        serverSectionLabel = createSectionHeader(I18n.get("settings.section.server"));
        serverUrlLabel = new JLabel(I18n.get("settings.label.server_url"));
        serverUrlField = new JTextField("http://localhost:8080");
        serverUrlField.setPreferredSize(new Dimension(350, 36));

        techCard.add(serverSectionLabel);
        techCard.add(Box.createVerticalStrut(10));
        techCard.add(serverUrlLabel);
        techCard.add(Box.createVerticalStrut(6));
        techCard.add(serverUrlField);

        techCard.add(Box.createVerticalStrut(20));

        backupSectionLabel = createSectionHeader(I18n.get("settings.section.backup"));
        backupButton = new JButton(I18n.get("settings.btn.backup"));
        backupButton.setPreferredSize(new Dimension(220, 38));
        backupButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backupButton.addActionListener(e -> performDatabaseBackup());

        techCard.add(backupSectionLabel);
        techCard.add(Box.createVerticalStrut(10));
        techCard.add(backupButton);

        contentPanel.add(techCard);
        contentPanel.add(Box.createVerticalStrut(16));

        // 4. About Card
        JPanel aboutCard = createCardPanel();
        aboutSectionLabel = createSectionHeader(I18n.get("settings.section.about"));
        versionLabel = new JLabel(I18n.get("settings.about.version"));
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descLabel = new JLabel(I18n.get("settings.about.desc"));
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descLabel.setForeground(new Color(100, 116, 139));

        aboutCard.add(aboutSectionLabel);
        aboutCard.add(Box.createVerticalStrut(8));
        aboutCard.add(versionLabel);
        aboutCard.add(Box.createVerticalStrut(4));
        aboutCard.add(descLabel);

        contentPanel.add(aboutCard);
        contentPanel.add(Box.createVerticalStrut(20));

        // Save Button Footer
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);

        saveButton = new JButton(I18n.get("settings.btn.save"));
        saveButton.setPreferredSize(new Dimension(160, 42));
        saveButton.setBackground(new Color(22, 101, 52));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> saveSettingsData());

        actionPanel.add(saveButton);
        contentPanel.add(actionPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        return card;
    }

    private JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        label.setForeground(new Color(15, 23, 42));
        return label;
    }

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
                JOptionPane.showMessageDialog(this, "Database file does not exist yet.", "Backup Error", JOptionPane.ERROR_MESSAGE);
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
        titleLabel.setText(I18n.get("settings.title"));
        subtitleLabel.setText(I18n.get("settings.subtitle"));
        langSectionLabel.setText(I18n.get("settings.section.language"));
        langSelectLabel.setText(I18n.get("settings.label.language"));
        businessSectionLabel.setText(I18n.get("settings.section.business"));
        businessNameLabel.setText(I18n.get("settings.label.business_name"));
        ownerNameLabel.setText(I18n.get("settings.label.owner_name"));
        phoneLabel.setText(I18n.get("settings.label.phone"));
        villageLabel.setText(I18n.get("settings.label.village"));
        serverSectionLabel.setText(I18n.get("settings.section.server"));
        serverUrlLabel.setText(I18n.get("settings.label.server_url"));
        backupSectionLabel.setText(I18n.get("settings.section.backup"));
        backupButton.setText(I18n.get("settings.btn.backup"));
        saveButton.setText(I18n.get("settings.btn.save"));
        aboutSectionLabel.setText(I18n.get("settings.section.about"));
        versionLabel.setText(I18n.get("settings.about.version"));
        descLabel.setText(I18n.get("settings.about.desc"));
        revalidate();
        repaint();
    }
}
