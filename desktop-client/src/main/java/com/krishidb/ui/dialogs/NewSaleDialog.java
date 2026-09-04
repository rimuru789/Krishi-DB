package com.krishidb.ui.dialogs;

import com.krishidb.ui.pages.SalePanel;
import com.krishidb.util.I18n;

import javax.swing.*;
import java.awt.*;

public class NewSaleDialog extends JDialog {

    private final SalePanel salePanel;

    public NewSaleDialog(JFrame parent) {
        super(parent, I18n.get("sale.dialog.newsale_title"), true);

        setSize(1150, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        salePanel = new SalePanel();
        add(salePanel, BorderLayout.CENTER);
    }

    public SalePanel getSalePanel() {
        return salePanel;
    }
}
