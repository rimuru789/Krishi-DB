package com.krishidb.ui.pages;


import javax.swing.*;
import java.awt.*;

import com.krishidb.dao.SyncDAO;
import com.krishidb.ui.MainFrame;

import com.krishidb.dao.SyncQueueDAO;
import com.krishidb.model.SyncRecord;

import javax.swing.table.DefaultTableModel;
import java.util.List;



public class SyncPanel extends JPanel {

    private MainFrame mainFrame;

        private JLabel statusLabel;
        private JLabel pendingLabel;
        private JLabel lastSyncLabel;

        private JTable historyTable;
        private DefaultTableModel historyModel;


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



        

        JButton syncButton =
                new JButton("Sync Now");


        syncButton.setPreferredSize(
                new Dimension(150,45)
        );



        statusLabel =
                new JLabel(
                        "Ready to sync"
                );

                JPanel center =
        new JPanel(
                new BorderLayout()
        );


center.setBorder(
        BorderFactory.createEmptyBorder(
                20,
                30,
                20,
                30
        )
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
    
    mainFrame.refreshInventory();
    loadSyncHistory();

}
else{

    statusLabel.setText("Sync failed");

    JOptionPane.showMessageDialog(
        this,
        "Sync failed!"
    );

}

});

JPanel actionPanel =
        new JPanel(
                new FlowLayout()
        );


actionPanel.add(syncButton);
actionPanel.add(statusLabel);


center.add(
        actionPanel,
        BorderLayout.CENTER
);


        

        String[] columns =
{
    "Table",
    "Operation",
    "Status",
    "Time"
};


historyModel =
        new DefaultTableModel(columns,0);


historyTable =
        new JTable(historyModel);


JScrollPane scrollPane =
        new JScrollPane(historyTable);


scrollPane.setBorder(
        BorderFactory.createTitledBorder(
                "Sync History"
        )
);


scrollPane.setPreferredSize(
        new Dimension(
                900,
                300
        )
);


center.add(
        scrollPane,
        BorderLayout.SOUTH
);



        add(
                center,
                BorderLayout.CENTER
        );

    }


    private void loadSyncHistory(){


    historyModel.setRowCount(0);


    SyncQueueDAO dao =
            new SyncQueueDAO();


    List<SyncRecord> records =
            dao.getSyncHistory();



    for(SyncRecord record : records){


        historyModel.addRow(
                new Object[]{
                        record.getTableName(),
                        record.getOperation(),
                        record.getStatus(),
                        record.getCreatedAt()
                }
        );

    }

}

}