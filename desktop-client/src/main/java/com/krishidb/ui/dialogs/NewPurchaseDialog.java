package com.krishidb.ui.dialogs;

import com.krishidb.ui.pages.PurchasePanel;
import com.krishidb.util.I18n;

import javax.swing.*;
import java.awt.*;

public class NewPurchaseDialog extends JDialog {

    private final PurchasePanel purchasePanel;

    public NewPurchaseDialog(JFrame parent) {
        super(parent, I18n.get("purchase.dialog.newpurchase_title"), true);

        setSize(1150, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        purchasePanel = new PurchasePanel();
        add(purchasePanel, BorderLayout.CENTER);
    }

    public PurchasePanel getPurchasePanel() {
        return purchasePanel;
    }
}
