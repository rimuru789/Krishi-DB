package com.krishidb.ui.pages;


import javax.swing.*;
import java.awt.*;

import com.krishidb.dao.SyncDAO;
import com.krishidb.ui.MainFrame;



public class SyncPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel statusLabel;


    public SyncPanel(MainFrame mainFrame){

    this.mainFrame = mainFrame;


        setLayout(
                new BorderLayout()
        );


        JLabel title =
                new JLabel("Sync Center");


        title.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        32
                )
        );


        title.setBorder(
                BorderFactory.createEmptyBorder(
                        30,30,20,30
                )
        );


        add(
                title,
                BorderLayout.NORTH
        );



        JPanel center =
                new JPanel(
                        new GridBagLayout()
                );


        JButton syncButton =
                new JButton("Sync Now");


        syncButton.setPreferredSize(
                new Dimension(150,45)
        );



        statusLabel =
                new JLabel(
                        "Ready to sync"
                );


        syncButton.addActionListener(e -> {

    SyncDAO dao = new SyncDAO();

    boolean result = dao.syncProductsToServer();


    

if(result){

    statusLabel.setText("Sync completed");
    mainFrame.updateSidebarStatus(true, 0);

    JOptionPane.showMessageDialog(
        this,
        "Products synced successfully!"
    );
    mainFrame.updateSidebarStatus(true,0);
    mainFrame.refreshInventory();

}
else{

    statusLabel.setText("Sync failed");

    JOptionPane.showMessageDialog(
        this,
        "Sync failed!"
    );

}

});



        JPanel box =
                new JPanel();


        box.add(syncButton);

        box.add(statusLabel);



        center.add(box);



        add(
                center,
                BorderLayout.CENTER
        );

    }

}