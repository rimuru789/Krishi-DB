package com.krishidb.ui;

import javax.swing.*;
import java.awt.*;
import com.krishidb.ui.components.Sidebar;
import com.krishidb.ui.pages.InventoryPanel;
import com.krishidb.ui.pages.SyncPanel;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel pageContainer;
    private InventoryPanel inventoryPanel;
    private Sidebar sidebar;

    public MainFrame() {

        // ---------------- WINDOW ----------------

        setTitle("Krishi-DB");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setMinimumSize(new Dimension(1100, 700));

        setSize(1400, 850);

        setLocationRelativeTo(null);


        // ---------------- MAIN LAYOUT ----------------

        setLayout(new BorderLayout());


     sidebar = new Sidebar(this);


        // ---------------- PAGE SYSTEM ----------------

        cardLayout = new CardLayout();

        pageContainer = new JPanel(cardLayout);

        JPanel dashboardPlaceholder = createPlaceholderPage(
                "Dashboard"
        );

        inventoryPanel = new InventoryPanel();

        pageContainer.add(
                dashboardPlaceholder,
                "DASHBOARD"
        );

        pageContainer.add(
        inventoryPanel,
        "INVENTORY"
);

pageContainer.add(
        new SyncPanel(this),
        "SYNC"
);


        // ---------------- ADD TO WINDOW ----------------

        add(sidebar, BorderLayout.WEST);

        add(pageContainer, BorderLayout.CENTER);
    }


    private JPanel createPlaceholderPage(String title) {

        JPanel panel = new JPanel(
                new GridBagLayout()
        );

        JLabel label = new JLabel(title);

        label.setFont(
                new Font("SansSerif", Font.BOLD, 32)
        );

        panel.add(label);

        return panel;
    }

    public void refreshInventory(){

    inventoryPanel.refreshInventory();

}


    public void showPage(String pageName) {

        cardLayout.show(
                pageContainer,
                pageName
        );
    }

    public void updateSidebarStatus(boolean online, int pending)
{
    sidebar.updateSyncStatus(
            online,
            pending
    );
}
}