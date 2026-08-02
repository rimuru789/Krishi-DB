package com.krishidb.ui.pages;

import com.krishidb.dao.ProductDAO;
import com.krishidb.model.Product;
import com.krishidb.ui.dialogs.AddProductDialog;
import com.krishidb.ui.dialogs.EditProductDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel {

    private final ProductDAO productDAO;

    private DefaultTableModel tableModel;
    private JTable productTable;
    private TableRowSorter<DefaultTableModel> sorter;
   
    private JLabel totalProductsValue;
    private JLabel lowStockValue;
    private JLabel inventoryValue;

    public InventoryPanel() {

        productDAO = new ProductDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));

        setBorder(
                new EmptyBorder(35, 40, 35, 40)
        );

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        refreshInventory();
    }


    // -------------------------------------------------
    // HEADER
    // -------------------------------------------------

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setOpaque(false);

        header.setBorder(
                new EmptyBorder(0, 0, 25, 0)
        );


        JPanel titleArea = new JPanel();

        titleArea.setOpaque(false);

        titleArea.setLayout(
                new BoxLayout(titleArea, BoxLayout.Y_AXIS)
        );


        JLabel title = new JLabel("Inventory");

        title.setFont(
                new Font("SansSerif", Font.BOLD, 30)
        );

        title.setForeground(
                new Color(15, 23, 42)
        );


        JLabel subtitle = new JLabel(
                "Manage products, stock levels and selling prices."
        );

        subtitle.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        subtitle.setForeground(
                new Color(100, 116, 139)
        );


        titleArea.add(title);

        titleArea.add(
                Box.createVerticalStrut(6)
        );

        titleArea.add(subtitle);


  JPanel buttonPanel = new JPanel(
        new FlowLayout(
                FlowLayout.RIGHT,
                12,
                10
        )
);


JButton addButton =
        new JButton("+ Add Product");

JButton editButton =
        new JButton("Edit");

JButton deleteButton =
        new JButton("Delete");

JButton refreshButton =
        new JButton("Refresh Inventory");



addButton.setPreferredSize(
        new Dimension(150,42)
);

editButton.setPreferredSize(
        new Dimension(90,42)
);

deleteButton.setPreferredSize(
        new Dimension(90,42)
);

refreshButton.setPreferredSize(
        new Dimension(170,42)
);


editButton.addActionListener(
        e -> editSelectedProduct()
);


deleteButton.addActionListener(
        e -> deleteSelectedProduct()
);

addButton.putClientProperty(
        "JButton.buttonType",
        "roundRect"
);

editButton.putClientProperty(
        "JButton.buttonType",
        "roundRect"
);

deleteButton.putClientProperty(
        "JButton.buttonType",
        "roundRect"
);

refreshButton.putClientProperty(
        "JButton.buttonType",
        "roundRect"
);


// Button sizing
addButton.setPreferredSize(
        new Dimension(140, 40)
);

refreshButton.setPreferredSize(
        new Dimension(170, 40)
);


// Cursor
addButton.setCursor(
        Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR
        )
);

refreshButton.setCursor(
        Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR
        )
);


// Open Add Product Dialog
addButton.addActionListener(e -> {

    AddProductDialog dialog =
            new AddProductDialog();

    dialog.setVisible(true);

    refreshInventory();

});


// Refresh button
refreshButton.addActionListener(
        e -> refreshInventory()
);


// Add buttons to panel
buttonPanel.add(addButton);
buttonPanel.add(editButton);
buttonPanel.add(deleteButton);
buttonPanel.add(refreshButton);


        header.add(
                titleArea,
                BorderLayout.WEST
        );

        header.add(
        buttonPanel,
        BorderLayout.EAST
        );

        return header;
    }


    // -------------------------------------------------
    // MAIN CONTENT
    // -------------------------------------------------

    private JPanel createContent() {

        JPanel content = new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BorderLayout(0, 25)
        );


        // SUMMARY CARDS

        JPanel summaryPanel =
                new JPanel(new GridLayout(1, 3, 20, 0));

        summaryPanel.setOpaque(false);


        totalProductsValue =
                new JLabel("0");

        lowStockValue =
                new JLabel("0");

        inventoryValue =
                new JLabel("₹0.00");


        summaryPanel.add(
                createSummaryCard(
                        "TOTAL PRODUCTS",
                        totalProductsValue,
                        "Products currently registered"
                )
        );


        summaryPanel.add(
                createSummaryCard(
                        "LOW STOCK",
                        lowStockValue,
                        "Products requiring attention"
                )
        );


        summaryPanel.add(
                createSummaryCard(
                        "INVENTORY VALUE",
                        inventoryValue,
                        "Estimated current stock value"
                )
        );


        // TABLE AREA

        JPanel tableSection =
                createTableSection();


        content.add(
                summaryPanel,
                BorderLayout.NORTH
        );

        content.add(
                tableSection,
                BorderLayout.CENTER
        );

        return content;
    }


    // -------------------------------------------------
    // SUMMARY CARD
    // -------------------------------------------------

    private JPanel createSummaryCard(
            String heading,
            JLabel valueLabel,
            String description) {

        JPanel card = new JPanel();

        card.setBackground(Color.WHITE);

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBorder(
                new EmptyBorder(22, 22, 22, 22)
        );


        JLabel headingLabel =
                new JLabel(heading);

        headingLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        11
                )
        );

        headingLabel.setForeground(
                new Color(100, 116, 139)
        );


        valueLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        28
                )
        );

        valueLabel.setForeground(
                new Color(15, 23, 42)
        );


        JLabel descriptionLabel =
                new JLabel(description);

        descriptionLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        descriptionLabel.setForeground(
                new Color(148, 163, 184)
        );


        card.add(headingLabel);

        card.add(
                Box.createVerticalStrut(12)
        );

        card.add(valueLabel);

        card.add(
                Box.createVerticalStrut(5)
        );

        card.add(descriptionLabel);

        return card;
    }


    // -------------------------------------------------
    // TABLE
    // -------------------------------------------------

    private JPanel createTableSection() {

        JPanel section =
                new JPanel(new BorderLayout());

        section.setBackground(Color.WHITE);

        section.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );


        JPanel tableHeader =
                new JPanel(new BorderLayout());

        tableHeader.setOpaque(false);

        tableHeader.setBorder(
                new EmptyBorder(0, 0, 15, 0)
        );


        JLabel title =
                new JLabel("Product Inventory");

        title.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        18
                )
        );


        JTextField searchField =
        new JTextField();


searchField.putClientProperty(
        "JTextField.placeholderText",
        "Search products..."
);


searchField.setPreferredSize(
        new Dimension(260,38)
);






        tableHeader.add(
                title,
                BorderLayout.WEST
        );

        tableHeader.add(
                searchField,
                BorderLayout.EAST
        );


        String[] columns = {
                "ID",
                "Product",
                "Category",
                "Unit",
                "Selling Price",
                "Stock",
                "Low Stock Level",
                "Sync"
        };


        tableModel =
                new DefaultTableModel(columns, 0) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };


        productTable =
                new JTable(tableModel);

                sorter = new TableRowSorter<>(tableModel);

productTable.setRowSorter(sorter);

        productTable.setRowHeight(42);

        productTable.setShowVerticalLines(false);

        productTable.setFillsViewportHeight(true);

        productTable.getTableHeader()
                .setReorderingAllowed(false);
                // SEARCH FUNCTIONALITY

searchField.getDocument().addDocumentListener(new DocumentListener(){


    public void insertUpdate(DocumentEvent e){
        filter();
    }


    public void removeUpdate(DocumentEvent e){
        filter();
    }


    public void changedUpdate(DocumentEvent e){
        filter();
    }


    private void filter(){

        String text =
                searchField.getText();


        if(text.trim().isEmpty()){

            sorter.setRowFilter(null);

        }
        else{

            sorter.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" + text
                    )
            );

        }
    }

});


        JScrollPane scrollPane =
                new JScrollPane(productTable);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder()
        );


        section.add(
                tableHeader,
                BorderLayout.NORTH
        );

        section.add(
                scrollPane,
                BorderLayout.CENTER
        );

        return section;
    }


    // -------------------------------------------------
    // LOAD DATA FROM SQLITE
    // -------------------------------------------------

    public void refreshInventory() {

        List<Product> products =
                productDAO.getAllProducts();

        tableModel.setRowCount(0);


        int lowStockCount = 0;

        double totalInventoryValue = 0;


        for (Product product : products) {

            if (product.getStockQuantity()
                    <= product.getLowStockLevel()) {

                lowStockCount++;
            }


            totalInventoryValue +=
                    product.getStockQuantity()
                    * product.getSellingPrice();


            tableModel.addRow(
                    new Object[]{
                            product.getId(),
                            product.getName(),
                            product.getCategory(),
                            product.getUnit(),

                            String.format(
                                    "₹%.2f",
                                    product.getSellingPrice()
                            ),

                            product.getStockQuantity(),

                            product.getLowStockLevel(),

                            product.getSyncStatus()
                    }
            );
        }


        totalProductsValue.setText(
                String.valueOf(products.size())
        );

        lowStockValue.setText(
                String.valueOf(lowStockCount)
        );

        inventoryValue.setText(
                String.format(
                        "₹%,.2f",
                        totalInventoryValue
                )
        );
    }
    private void deleteSelectedProduct(){

    int row =
            productTable.getSelectedRow();



    if(row == -1){

        JOptionPane.showMessageDialog(
                this,
                "Please select a product first"
        );

        return;
    }



    int id =
            (int)productTable.getValueAt(row,0);



    int choice =
            JOptionPane.showConfirmDialog(
                    this,
                    "Delete this product?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );



    if(choice != JOptionPane.YES_OPTION){

        return;

    }



    ProductDAO dao =
            new ProductDAO();



    boolean deleted =
            dao.deleteProduct(id);



    if(deleted){

        JOptionPane.showMessageDialog(
                this,
                "Product deleted successfully"
        );


        refreshInventory();

    }
    else{


        JOptionPane.showMessageDialog(
                this,
                "Delete failed"
        );

    }

}
private void editSelectedProduct() {
    System.out.println("EDIT CLICKED");

    int row = productTable.getSelectedRow();


    if(row == -1){

        JOptionPane.showMessageDialog(
                this,
                "Please select a product first"
        );

        return;
    }



    int id = (int) productTable.getValueAt(row, 0);



    ProductDAO dao = new ProductDAO();


    List<Product> products = dao.getAllProducts();



    Product selectedProduct = null;


    for(Product product : products){

        if(product.getId() == id){

            selectedProduct = product;
            break;
        }
    }



    if(selectedProduct == null){

        JOptionPane.showMessageDialog(
                this,
                "Product not found"
        );

        return;
    }



    new EditProductDialog(
        (JFrame) SwingUtilities.getWindowAncestor(this),
        selectedProduct
);

refreshInventory();

}
}