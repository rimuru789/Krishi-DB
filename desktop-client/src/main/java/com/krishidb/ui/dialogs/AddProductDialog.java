package com.krishidb.ui.dialogs;

import com.krishidb.dao.ProductDAO;
import com.krishidb.model.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddProductDialog extends JDialog {


    private JTextField nameField;
    private JTextField categoryField;
    private JTextField unitField;

    private JTextField priceField;
    private JTextField stockField;
    private JTextField lowStockField;


    private ProductDAO productDAO;



    public AddProductDialog() {


        setTitle("Add Product");

        setSize(500, 650);

        setLocationRelativeTo(null);

        setModal(true);


        productDAO = new ProductDAO();


        setLayout(new BorderLayout());

        getContentPane()
                .setBackground(new Color(248,250,252));



        add(createHeader(), BorderLayout.NORTH);

        add(createForm(), BorderLayout.CENTER);

        add(createButtons(), BorderLayout.SOUTH);


    }



    private JPanel createHeader() {


        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(25,30,5,30)
        );


        JLabel title =
                new JLabel("Add New Product");


        title.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        26
                )
        );


        title.setForeground(
                new Color(15,23,42)
        );



        JLabel subtitle =
                new JLabel(
                        "Create a new inventory item"
                );


        subtitle.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );


        subtitle.setForeground(
                new Color(100,116,139)
        );


        panel.add(title);

        panel.add(
                Box.createVerticalStrut(5)
        );

        panel.add(subtitle);



        return panel;
    }





    private JPanel createForm() {


        JPanel container =
                new JPanel(
                        new BorderLayout()
                );


        container.setBackground(
                new Color(248,250,252)
        );


        JPanel form =
                new JPanel(
                        new GridBagLayout()
                );


        form.setBackground(Color.WHITE);

        form.setBorder(
                new EmptyBorder(25,30,25,30)
        );



        GridBagConstraints gbc =
                new GridBagConstraints();


        gbc.insets =
                new Insets(8,8,8,8);


        gbc.fill =
                GridBagConstraints.HORIZONTAL;


        gbc.weightx = 1;



        nameField = new JTextField();
        categoryField = new JTextField();
        unitField = new JTextField();

        priceField = new JTextField();
        stockField = new JTextField();
        lowStockField = new JTextField();



        int row = 0;



        addField(
                form,
                gbc,
                row++,
                "Product Name",
                nameField
        );


        addField(
                form,
                gbc,
                row++,
                "Category",
                categoryField
        );


        addField(
                form,
                gbc,
                row++,
                "Unit",
                unitField
        );


        addField(
                form,
                gbc,
                row++,
                "Selling Price",
                priceField
        );


        addField(
                form,
                gbc,
                row++,
                "Stock Quantity",
                stockField
        );


        addField(
                form,
                gbc,
                row++,
                "Low Stock Level",
                lowStockField
        );



        container.add(
                form,
                BorderLayout.CENTER
        );


        return container;

    }





    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            JTextField field
    ){


        gbc.gridy = row;


        gbc.gridx = 0;

        gbc.weightx = 0.3;


        JLabel text =
                new JLabel(label);


        text.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );


        panel.add(
                text,
                gbc
        );



        gbc.gridx = 1;

        gbc.weightx = 0.7;


        field.setPreferredSize(
                new Dimension(250,38)
        );


        panel.add(
                field,
                gbc
        );


    }






    private JPanel createButtons(){

    JPanel panel =
            new JPanel(
                    new FlowLayout(
                            FlowLayout.RIGHT,
                            15,
                            15
                    )
            );


    panel.setBackground(Color.WHITE);


    panel.setBorder(
            new EmptyBorder(
                    10,
                    20,
                    20,
                    20
            )
    );


    JButton cancel =
            new JButton("Cancel");


    JButton save =
            new JButton("Save Product");


    // FlatLaf rectangular buttons
    cancel.putClientProperty(
            "JButton.buttonType",
            "square"
    );


    save.putClientProperty(
            "JButton.buttonType",
            "square"
    );


    cancel.setPreferredSize(
            new Dimension(
                    100,
                    40
            )
    );


    save.setPreferredSize(
            new Dimension(
                    140,
                    40
            )
    );


    cancel.addActionListener(
            e -> dispose()
    );


    save.addActionListener(
            e -> saveProduct()
    );


    panel.add(cancel);

    panel.add(save);


    return panel;

}

   private void saveProduct(){


        try {


            if(nameField.getText().isBlank()
                    || categoryField.getText().isBlank()
                    || unitField.getText().isBlank()) {


                JOptionPane.showMessageDialog(
                        this,
                        "Please fill all fields"
                );

                return;
            }




            double price =
                    Double.parseDouble(
                            priceField.getText()
                    );


            double stock =
                    Double.parseDouble(
                            stockField.getText()
                    );


            double lowStock =
                    Double.parseDouble(
                            lowStockField.getText()
                    );



            if(price < 0 || stock < 0 || lowStock < 0){

                JOptionPane.showMessageDialog(
                        this,
                        "Values cannot be negative"
                );

                return;
            }





            Product product =
                    new Product(
                            nameField.getText(),
                            categoryField.getText(),
                            unitField.getText(),
                            price,
                            stock,
                            lowStock
                    );



            if(productDAO.addProduct(product)){


                JOptionPane.showMessageDialog(
                        this,
                        "Product added successfully!"
                );


                dispose();


            }


        }
        catch(Exception e){


            JOptionPane.showMessageDialog(
                    this,
                    "Enter valid numeric values"
            );

        }


    }


}