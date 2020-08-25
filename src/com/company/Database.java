package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class Database {

    String user; // user variable
    String password; // password variable
    String database; // database variable
    Connection connect = null; // connection variable
    Statement statement = null; // statement variable

    Properties prop = new Properties(); // properties object
    MyIdentity identity  = new MyIdentity(); // identity object to help with identity and accessing the db

    //Starts connection to the database
    public void start(){

        identity.setIdentity(prop);
        user = prop.getProperty("user");
        password = prop.getProperty("password");
        database = prop.getProperty("database");

        //// This will load the MySQL driver, each DB has its own driver
        try{Class.forName("com.mysql.cj.jdbc.Driver");

            connect = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", user, password); // connecting to db
            statement = connect.createStatement();

            // Statements allow to issue SQL queries to the database.  Create an instance
            // that we will use to ultimately send queries to the database.
            statement.executeQuery("use " + database + ";");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }


    //Method to authenticate the user, it takes in the username and password
    public ArrayList<String> auth(String name, String password){

        ResultSet r;// Resultset variable
        ArrayList<String> tempCredentials = new ArrayList<>();//Arraylist to hold returned values
        this.start();
        if(connect != null) {
            try {
                r = statement.executeQuery("select employees.LastName, employees.BirthDate from employees where LastName = " + "'"+name+"'" + " AND BirthDate = " + "'"+password+"'" + ";");
                while(r.next()){
                    tempCredentials.add(r.getString("LastName"));
                    tempCredentials.add(String.valueOf(r.getDate("BirthDate")));
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return tempCredentials;
    }


    //Method to list items from the database
    public ArrayList<String> list(String target) {
        ArrayList<String> temp = new ArrayList<>();
        ResultSet r ;
        String t = target;
        if (target.equals("customer")) {
            try {
                r = statement.executeQuery("select customers.CustomerID, customers.CompanyName from customers order by customers.CustomerID;");
                while(r.next()){
                    String w = r.getString("CustomerID") + "\t" + r.getString("CompanyName")+"\r\n";
                    temp.add(w);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            try {
                r = statement.executeQuery("select products.ProductID, products.ProductName from products order by products.ProductID;");
                while(r.next()){
                    String w = r.getInt("ProductID") + "\t" + r.getString("ProductName")+"\r\n";
                    temp.add(w);
                }
            } catch (Exception e) {
                System.out.println(e);
            }

        }
        return temp;
    }


    //MEthod to check if customer exists it uses a boolean to validate if customer exists or not
    public boolean checkCustomer(String t){
        String x = t;
        String w = "";
        boolean m = false;
        ResultSet r;
        try{
            r = statement.executeQuery("select customers.CustomerID from customers where customers.CustomerID = "+"'"+t+"'"+";");
            while (r.next()){
                w = r.getString("CustomerID");
            }
            if(w.equals(x)){
               m = true;
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return m;
    }


    //MEthod to get product it gets the product and returns the id in order to determine if the product hs discontinued items in it
    public int getProduct(String t){
        int m = Integer.valueOf(t);
        ResultSet r;
        int we = 0;

        try{
            r = statement.executeQuery("select products.ProductID from products where products.ProductID = " + "'"+m+"'" + " AND products.Discontinued = "+ "'"+0+"'" + ";");
            while(r.next()){
                we = r.getInt("ProductID");
            }
        }catch(Exception e){
            System.out.println();
        }

        return we;
    }


    //Method to make order
    public int makeOrder(HashMap<String,String> order,HashMap<Integer,Integer> Products){
        int result;
        try {

                String Address = "";
                String City = "";
                String Region = "";
                String PostalCode = "";
                String Country = "";

                //Booleans to check if order hashmap is filled
                boolean isAddress = (order.get("Address")!=null);
                boolean isCity = (order.get("City")!=null);
                boolean isRegion = (order.get("Region")!=null);
                boolean isPostalCode = (order.get("PostalCode")!=null);
                boolean isCountry = (order.get("Country")!=null);
                if (isAddress && isCity && isRegion && isPostalCode && isCountry) {
                    ResultSet Customer = statement.executeQuery("select Address,City,Region,PostalCode,Country from customers where CustomerID ='" + order.get("CustomerID") + "';");
                    if (Customer.next()) {
                        Address = Customer.getString("Address");
                        City = Customer.getString("City");
                        Region = Customer.getString("Region");
                        PostalCode = Customer.getString("PostalCode");
                        Country = Customer.getString("Country");
                    }

                } else {
                    Address = order.get("Address");
                    City = order.get("City");
                    Region = order.get("Region");
                    PostalCode = order.get("PostalCode");
                    Country = order.get("Country");

                }

                boolean insertOrders = statement.execute("insert into orders (CustomerID,OrderDate,ShipAddress,ShipCity,ShipRegion,ShipPostalCode,ShipCountry) values('" + order.get("CustomerID") + "',current_date() ,'" + Address + "','" + City + "','" + Region + "','" + PostalCode + "','" + Country + "');");
                int order_id = 0;
                ResultSet OrderID = statement.executeQuery("select * from orders where CustomerID ='" + order.get("CustomerID") + "' and OrderDate =current_date() and ShipAddress ='" + Address + "' and ShipCity ='" + City + "' and ShipRegion ='" + Region + "' and ShipPostalCode ='" + PostalCode + "' and ShipCountry ='" + Country + "';");
                if (OrderID.next()) {
                    order_id = OrderID.getInt("OrderID");
                } else {
                    result = 0;
                }
                for (int Key : Products.keySet()) {
                    float UnitPrice = 0;
                    int UnitsInStock = 0;
                    ResultSet Product = statement.executeQuery("select * from products where ProductID = " + Key + ";");
                    if (Product.next()) {
                        UnitPrice = Product.getFloat("UnitPrice");
                        UnitsInStock = Product.getInt("UnitsInStock");
                    }
                    boolean insertOrdersDetails = statement.execute("insert into orderdetails (OrderID,ProductID,UnitPrice,Quantity) values ('" + order_id + "','" + Key + "','" + UnitPrice + "'," + Products.get(Key) + ");");

                    boolean updateProducts = statement.execute("update products set UnitsInStock = '" + (UnitsInStock - Products.get(Key)) + "' where ProductID = " + Key + ";");

                }
                result = order_id;


        }catch (Exception e) {
            result = 0;
            System.out.println(e.getMessage());
        }
        return result;
    }


    //Method to close connectiion
    public void closeConnection(){
        try{
            if (statement != null){
                statement.close();//Close statement
            }
            if(connect != null){
                connect.close();//Close connection
            }
        }catch(Exception e){
            System.out.println(e);//Message to be returned if closing fails
        }
    }



    public Connection getConnect() {
        return connect;
    }

}
