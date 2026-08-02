package com.krishidb.ui.components;

import com.krishidb.ui.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Sidebar extends JPanel {

    private final MainFrame mainFrame;
    private JLabel connectionStatus;
    private JLabel syncStatus;

    private final Color sidebarColor = new Color(15, 23, 42);
    private final Color buttonColor = new Color(15, 23, 42);
    private final Color hoverColor = new Color(30, 41, 59);
    private final Color selectedColor = new Color(22, 101, 52);
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
    }


    // -------------------------------------------------
    // HEADER
    // -------------------------------------------------

    private JPanel createHeader() {

        JPanel header = new JPanel();
        header.setOpaque(false);

        header.setLayout(
                new BoxLayout(header, BoxLayout.Y_AXIS)
        );

        header.setBorder(
                new EmptyBorder(28, 24, 25, 20)
        );

        JLabel logo = new JLabel("KRISHI-DB");

        logo.setForeground(Color.WHITE);
        logo.setFont(
                new Font("SansSerif", Font.BOLD, 24)
        );

        JLabel subtitle =
                new JLabel("Rural Business Ledger");

        subtitle.setForeground(mutedTextColor);
        subtitle.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

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

        navigation.setLayout(
                new BoxLayout(navigation, BoxLayout.Y_AXIS)
        );

        navigation.setBorder(
                new EmptyBorder(5, 14, 10, 14)
        );


        // DASHBOARD

        navigation.add(
                createNavigationButton(
                        "⌂   Dashboard",
                        "DASHBOARD"
                )
        );


        navigation.add(Box.createVerticalStrut(25));

        navigation.add(
                createSectionLabel("BUSINESS")
        );

        navigation.add(Box.createVerticalStrut(8));


        navigation.add(
                createNavigationButton(
                        "＋   New Entry",
                        "NEW_ENTRY"
                )
        );

        navigation.add(
                createNavigationButton(
                        "▣   New Sale",
                        "NEW_SALE"
                )
        );

        navigation.add(
                createNavigationButton(
                        "□   Inventory",
                        "INVENTORY"
                )
        );

        navigation.add(
                createNavigationButton(
                        "♙   Customers",
                        "CUSTOMERS"
                )
        );

        navigation.add(
                createNavigationButton(
                        "₹   Expenses",
                        "EXPENSES"
                )
        );

        navigation.add(
                createNavigationButton(
                        "≡   Transactions",
                        "TRANSACTIONS"
                )
        );


        navigation.add(Box.createVerticalStrut(25));

        navigation.add(
                createSectionLabel("INSIGHTS")
        );

        navigation.add(Box.createVerticalStrut(8));


        navigation.add(
                createNavigationButton(
                        "▥   Reports",
                        "REPORTS"
                )
        );

        navigation.add(
                createNavigationButton(
                        "↗   Market Prices",
                        "MARKET_PRICES"
                )
        );


        navigation.add(Box.createVerticalStrut(25));

        navigation.add(
                createSectionLabel("SYSTEM")
        );

        navigation.add(Box.createVerticalStrut(8));


        navigation.add(
                createNavigationButton(
                        "↻   Sync Center",
                        "SYNC"
                )
        );

        navigation.add(
                createNavigationButton(
                        "⚙   Settings",
                        "SETTINGS"
                )
        );


        return navigation;
    }


    // -------------------------------------------------
    // NAVIGATION BUTTON
    // -------------------------------------------------

    private JButton createNavigationButton(
            String text,
            String pageName) {

        JButton button = new JButton(text);

        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 44)
        );

        button.setPreferredSize(
                new Dimension(220, 44)
        );

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setForeground(textColor);
        button.setBackground(buttonColor);

        button.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        button.setBorder(
                new EmptyBorder(0, 14, 0, 10)
        );

        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.addActionListener(
                e -> mainFrame.showPage(pageName)
        );

        return button;
    }


    // -------------------------------------------------
    // SECTION LABEL
    // -------------------------------------------------

    private JLabel createSectionLabel(String text) {

        JLabel label = new JLabel(text);

        label.setForeground(mutedTextColor);

        label.setFont(
                new Font("SansSerif", Font.BOLD, 11)
        );

        label.setBorder(
                new EmptyBorder(0, 12, 0, 0)
        );

        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }


    // -------------------------------------------------
    // CONNECTION / SYNC STATUS
    // -------------------------------------------------

    private JPanel createStatusArea() {

        JPanel container = new JPanel();

        container.setOpaque(false);

        container.setLayout(
                new BoxLayout(container, BoxLayout.Y_AXIS)
        );

        container.setBorder(
                new EmptyBorder(15, 22, 25, 22)
        );


         connectionStatus =
                new JLabel("●  OFFLINE");

        connectionStatus.setForeground(
                new Color(251, 191, 36)
        );

        connectionStatus.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        12
                )
        );


                syncStatus =
                new JLabel("1 change saved locally");

        syncStatus.setForeground(mutedTextColor);

        syncStatus.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        11
                )
        );


        connectionStatus.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        syncStatus.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        container.add(connectionStatus);

        container.add(
                Box.createVerticalStrut(5)
        );

        container.add(syncStatus);

        return container;
    }

    public void updateSyncStatus(boolean online, int pending)
{

    if(online)
    {
        connectionStatus.setText("● ONLINE");

        connectionStatus.setForeground(
                new Color(34,197,94)
        );

        syncStatus.setText(
                pending + " pending changes"
        );

    }
    else
    {
        connectionStatus.setText("● OFFLINE");

        connectionStatus.setForeground(
                new Color(251,191,36)
        );


        syncStatus.setText(
                pending + " changes saved locally"
        );
    }


    repaint();
}

}