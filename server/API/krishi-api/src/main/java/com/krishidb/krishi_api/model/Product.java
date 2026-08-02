package com.krishidb.krishi_api.model;


import jakarta.persistence.*;


@Entity
@Table(name="products")
public class Product {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String name;

    private String category;

    private String unit;

    private double sellingPrice;

    private double stockQuantity;

    private double lowStockLevel;

    public void setId(Long id){
    this.id = id;
}


    public Product(){

    }


    public Long getId(){
        return id;
    }


    public String getName(){
        return name;
    }


    public void setName(String name){
        this.name=name;
    }


    public String getCategory(){
        return category;
    }


    public void setCategory(String category){
        this.category=category;
    }


    public String getUnit(){
        return unit;
    }


    public void setUnit(String unit){
        this.unit=unit;
    }


    public double getSellingPrice(){
        return sellingPrice;
    }


    public void setSellingPrice(double sellingPrice){
        this.sellingPrice=sellingPrice;
    }


    public double getStockQuantity(){
        return stockQuantity;
    }


    public void setStockQuantity(double stockQuantity){
        this.stockQuantity=stockQuantity;
    }


    public double getLowStockLevel(){
        return lowStockLevel;
    }


    public void setLowStockLevel(double lowStockLevel){
        this.lowStockLevel=lowStockLevel;
    }

}