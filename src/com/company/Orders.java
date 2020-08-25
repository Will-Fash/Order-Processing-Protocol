package com.company;

import java.util.HashMap;
/*Class to handle NEW, ADD and orders*/

public class Orders {
    protected HashMap<String,String> orders = null;//HashMap to hold New Order details
    protected HashMap<Integer,Integer> products = null;//HashMap to hold products in order

    public Orders(){
        orders = new HashMap<>();// Initialize order HashMap
        products = new HashMap<>();// Initialize product HashMap
    }

    // Returns order HashMap
    public HashMap<String, String> getOrders() {
        return orders;
    }

    //Sets order HashMap
    public void setOrders(HashMap<String, String> orders) {
        this.orders = orders;
    }

    // Returns products HashMap
    public HashMap<Integer, Integer> getProducts() {
        return products;
    }

    // Sets products HashMap
    public void setProducts(HashMap<Integer, Integer> products) {
        this.products = products;
    }
}

