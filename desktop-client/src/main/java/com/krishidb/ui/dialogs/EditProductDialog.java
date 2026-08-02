package com.krishidb.ui.dialogs;


import javax.swing.*;
import java.awt.*;

import com.krishidb.dao.ProductDAO;
import com.krishidb.model.Product;


public class EditProductDialog extends JDialog {


    private JTextField nameField;
    private JTextField categoryField;
    private JTextField unitField;
    private JTextField priceField;
    private JTextField stockField;
    private JTextField lowStockField;


    private Product product;


    public EditProductDialog(JFrame parent, Product product) {

    super(parent, "Edit Product", true);

    this.product = product;


    setSize(550,520);

    setLocationRelativeTo(parent);

    setLayout(new BorderLayout(20,20));



    // ---------------- FORM ----------------

    JPanel form = new JPanel(
            new GridBagLayout()
    );


    form.setBorder(
            BorderFactory.createEmptyBorder(
                    30,40,20,40
            )
    );


    nameField =
            new JTextField(product.getName());


    categoryField =
            new JTextField(product.getCategory());


    unitField =
            new JTextField(product.getUnit());


    priceField =
            new JTextField(
                    String.valueOf(
                            product.getSellingPrice()
                    )
            );


    stockField =
            new JTextField(
                    String.valueOf(
                            product.getStockQuantity()
                    )
            );


    lowStockField =
            new JTextField(
                    String.valueOf(
                            product.getLowStockLevel()
                    )
            );



    addField(form,"Product Name",nameField,0);

    addField(form,"Category",categoryField,1);

    addField(form,"Unit",unitField,2);

    addField(form,"Selling Price",priceField,3);

    addField(form,"Stock Quantity",stockField,4);

    addField(form,"Low Stock Level",lowStockField,5);



    add(
            form,
            BorderLayout.CENTER
    );



    // ---------------- BUTTONS ----------------


    JButton cancelButton =
            new JButton("Cancel");


    JButton saveButton =
            new JButton("Save Changes");



    // FlatLaf rectangular buttons

    cancelButton.putClientProperty(
            "JButton.buttonType",
            "square"
    );


    saveButton.putClientProperty(
            "JButton.buttonType",
            "square"
    );



    cancelButton.setPreferredSize(
            new Dimension(100,40)
    );


    saveButton.setPreferredSize(
            new Dimension(140,40)
    );



    cancelButton.addActionListener(
            e -> dispose()
    );


    saveButton.addActionListener(
            e -> saveProduct()
    );



    JPanel buttonPanel =
        new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        15,
                        10
                )
        );


buttonPanel.setBorder(
        BorderFactory.createEmptyBorder(
                0,
                20,
                10,
                20
        )
);


    buttonPanel.add(cancelButton);

    buttonPanel.add(saveButton);



    add(
            buttonPanel,
            BorderLayout.SOUTH
    );



    setVisible(true);
}

    private void addField(
        JPanel panel,
        String label,
        JTextField field,
        int row
){

    GridBagConstraints gbc = new GridBagConstraints();

    gbc.insets = new Insets(
            10,10,10,10
    );

    gbc.gridy = row;


    gbc.gridx = 0;

    gbc.anchor = GridBagConstraints.WEST;

    panel.add(
            new JLabel(label),
            gbc
    );


    gbc.gridx = 1;

    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.weightx = 1;


    field.setPreferredSize(
            new Dimension(250,40)
    );


    panel.add(
            field,
            gbc
    );
}



    private void saveProduct(){

    try {

        product.setName(
                nameField.getText()
        );


        product.setCategory(
                categoryField.getText()
        );


        product.setUnit(
                unitField.getText()
        );


        product.setSellingPrice(
                Double.parseDouble(
                        priceField.getText()
                )
        );


        product.setStockQuantity(
                Double.parseDouble(
                        stockField.getText()
                )
        );


        product.setLowStockLevel(
                Double.parseDouble(
                        lowStockField.getText()
                )
        );


        ProductDAO dao = new ProductDAO();


        boolean updated =
                dao.updateProduct(product);



        if(updated){

            JOptionPane.showMessageDialog(
                    this,
                    "Product updated successfully"
            );

            dispose();

        }
        else{

            JOptionPane.showMessageDialog(
                    this,
                    "Update failed"
            );
        }


    } catch(NumberFormatException e){

        JOptionPane.showMessageDialog(
                this,
                "Please enter valid numbers"
        );

    }

}

}