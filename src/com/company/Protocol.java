package com.company;

import java.util.*;

public class Protocol {

    private static final String version = "Order3901/1.0"; // Constant variable to store protocol version
    private static final String body_header = "Content-Length"; // Constant variable to store content-length
    private static final int DEFAULT = 0; // Constant variable to store o
    protected Set<String> allowed_operations; // Set of allowed operations
    private ArrayList<String> body; // ArrayList to store body content
    protected String cookie = null; // Variable to store cookie
    protected HashMap<String,String> headers; // HashMap to store the headers
    private String operation = null; // Store operation used by
    private String target = ""; // Variable to store target
    private String usedVersion = null; // Version used by client
    private String contentBody = null; // Variable to store the body to the message
    private String returnMessage; // Variable where return message would be stored
    private int bodyLength = DEFAULT; // Body length to store byte length of data
    private Database db; // Database object
    private Orders order; // Order object


    //Protocol class's constructor
    public Protocol(){
        this.headers = new HashMap<>(); //Instantiate header HashMap
        this.allowed_operations = new HashSet<>(); //Instantiate allowed operations set
        this.allowed_operations.add("AUTH"); // }
        this.allowed_operations.add("LOGOUT");
        this.allowed_operations.add("LIST");
        this.allowed_operations.add("NEW");     //      Add allowed operations to the allowed operations set
        this.allowed_operations.add("ADD");
        this.allowed_operations.add("ORDER");
        this.allowed_operations.add("DROP"); // }
        db = new Database(); //Instantiate db object
        order = new Orders(); //Instantiate order object
    }


    // Choice Method to decide what action method to calls
    public String choice(){
        String choice = "";
        if(this.operation.equals("AUTH")){
            choice = auth();
        }else if(this.operation.equals("LOGOUT")){
            choice = logout();
        }else if(this.operation.equals("LIST")){
            choice = list();
        }else if(this.operation.equals("NEW")){
            choice = New();
        }else if(this.operation.equals("ADD")){
            choice = add();
        }else if(this.operation.equals("ORDER")){
            choice = order();
        }else{
            choice = drop();
        }
        return choice;
    }


    // Method to process input
    public void preprocessInput(String inputLine){
        String[] lineArray = inputLine.split("\n"); //Breaks input string into lines and inserts them into a linearray
        String spaceArray[]; //Declaring a space array to take in variables

        spaceArray = lineArray[0].split(" ", 3); //Space array takes in the first value of the line array when it's splitted

        //Assigns the firstline of the client message to the appropriate variables
        for(String i : allowed_operations){
            if(spaceArray[0].equals(i)){
                this.operation = spaceArray[0];
                this.target = spaceArray[1];
                this.usedVersion = spaceArray[2];
            }
        }

        //Adds headers as required to the header hashmap
        for(int i = 1; i < lineArray.length; i++){
            if(lineArray[i].length() < 4){ // Sets the content body
                this.contentBody = lineArray[i];
            }else if(lineArray[i].length() != 0){
                spaceArray = lineArray[i].split(":", 2);
                this.headers.put(spaceArray[0], spaceArray[1].trim());
            }
        }

    }


    //Action method to authenticate a user
    public String auth(){
        ArrayList<String> authentication = null;

        //Condition to handle some scenarios
        if(((this.target.equals("")) && (!this.usedVersion.equals(version))) && ((!headers.containsKey("Password")) && (this.headers.get("Password").length() == 0))){
                this.returnMessage = version+" "+429+" "+"invalid information or wrong protocol, please make sure to type password with a capital 'P'.\r\n";
        }else{
            try{
                String password = this.headers.get("Password"); //Stores the value of the key 'Password' in the HashMap
                authentication = db.auth(this.target,password); //Arraylist to store the arraylist returned by the Database class' auth method

                if((authentication.get(0).equalsIgnoreCase(this.target)) || (authentication.get(1).equals(password))){ //Condition to authenticate the user
                    this.cookie = "foob";
                    this.returnMessage = version+" "+200+" "+"ok\n" + "Set-Cookie: "+this.cookie + "\r\n"; //Return message if authentication passes
                }else{
                    this.returnMessage = version+" "+450+" "+"wrong credentials\r\n"; //Return message if authentication fails
                }
            }catch(Exception e){
                this.returnMessage = version+" "+435+" "+ e.getMessage()+"\r\n";
            }
        }
        return this.returnMessage;
    }


    //Action method to handle the logout request
    public String logout(){

        //Condition to handle some scenarios
        if((!this.target.equals("LOGOUT")) && (!this.usedVersion.equals(version))){
            this.returnMessage = version+" "+403+" "+"wrong protocol or wrong action\r\n"; //return message if condition fails
        }else{

            //Condition to check if user's logged in
            if((!headers.containsKey("Cookie")) && (this.headers.get("Cookie").equals(this.cookie))){
                this.returnMessage = version+" "+411+" User not logged in\r\n"; // return message if condition fails
            }else {
                if(db.getConnect() != null){ //Condition to check if the Database connection still exists
                    try{
                        db.closeConnection(); //Close connection
                        this.cookie = null; //Delete cookie
                        this.returnMessage = version+" "+200+" "+"ok\r\n"; //Return message to be sent if the prior conditions passed
                    }catch (Exception e){
                        this.returnMessage = version+" "+416+" "+e.getMessage() + "\r\n";
                    }
                }else{
                    this.cookie = null; //If db isn't connected to, just delete the cookie
                    this.returnMessage = version+" "+460+" "+"user not logged in\r\n";
                }
            }

        }
        return this.returnMessage;
    }


    //Action method to create new order
    public String New(){

        boolean s = db.checkCustomer(this.target); //Checks to see if a customer exists before creating an order for the customer

        //Check if the address is incomplete
        if(this.headers.containsKey("Address") || this.headers.containsKey("City") || this.headers.containsKey("Region")
        || this.headers.containsKey("PostalCode") || this.headers.containsKey("Country")){
            if(!this.headers.containsKey("Address") && !this.headers.containsKey("City") && !this.headers.containsKey("Region")
            && !this.headers.containsKey("PostalCode") && !this.headers.containsKey("Country")){

                this.returnMessage = version+" "+405+" Incomplete Address\r\n"; //message to return if check passes
                return this.returnMessage; //return and exit the method

            }
        }

        //Some conditions to check before the creating a new order
        if((!this.target.equals("")) &&  (this.headers.containsKey("Cookie")) && (this.headers.get("Cookie").equals(this.cookie))){
            if(s == true){
                order.getOrders().put("CustomerID",this.target); //Add customer id to the orders HashMap
                for(String i : this.headers.keySet()){
                    if(i.equals("Cookie")){
                        continue;
                    }
                    order.getOrders().put(i,this.headers.get(i)); //Add user details to the orders HashMap from the  headers HashMap
                }
                this.returnMessage = version+" "+200+" "+"ok\r\n"; //Return Message to return
            }else{
                this.returnMessage = version+" "+406+" Customer doesn't exist\r\n"; //Message to be returned if condition fails
            }

        }else{
            this.returnMessage = version+" "+402+" wrong state\r\n"; //Message to be returned if condition fails
        }

        return this.returnMessage;
    }


    //Action method to add to order
    public String add(){

        int we;

        //Conditions to determine if right format has been used and if the user has been authenticated
        if((!this.target.equals("")) && (this.usedVersion.equals(version)) && (this.headers.containsKey("Cookie")) && (this.headers.get("Cookie").equals(this.cookie))){

            //Condition to check if a pending order exist
            if(order.getOrders() != null){
                we = db.getProduct(this.target); //Stores the product number to determine if the product has been discontinued or not

                //Condition to add to order if product hasn't been discontinued
                if(we != 0){
                    order.getProducts().put(we,Integer.valueOf(this.contentBody)); //Add products to the products to be ordered HashMap
                    this.returnMessage = version+" "+200+" ok\r\n"; //return message if action is successful
                }else{
                    this.returnMessage = version+" "+407+" Discontinued item\r\n"; //return message if action contains discontinued item
                }
            }else {
                this.returnMessage = version+" "+408+" Bad order\r\n"; //return message if pending order doesn't exist
            }

        }else{
            this.returnMessage = version+" "+409+" User not authenticate or wrong version of protocol used\r\n"; // return message if first condition fails
        }

        return this.returnMessage;
    }


    //Action method to order products
    public String order(){

        int r;
        String m = "";
        String w = "";

        //Condition to check to if right protocol version is used
        if((!this.usedVersion.equals(version))){
            this.returnMessage = version+" "+400+" Server error\r\n"; //return message if condition fails
        }else{

            //Condition to check if user is authenticated
            if((!this.headers.keySet().contains("Cookie")) && (!this.headers.get("Cookie").equals(this.cookie))){
                this.returnMessage = version+" "+ 405 +" User not Authenticated\r\n"; //return message if authentication fails
            }else{

                //Condition to check if pending order exists
                if(order.getOrders()!= null){
                    r = db.makeOrder(order.getOrders(),order.getProducts()); // return the order id of the newly placed order

                    //Condition to check if the order was placed successfully
                    if(r != 0){
                        w = String.valueOf(r); //store int order id in a string
                        w += "\r\n"; //add CRLF to the end of the orderID
                        this.body = new ArrayList<>();
                        this.body.add(w); //add string order id to body array
                        this.bodyLength += this.body.get(0).getBytes().length; //store the byte count of the order id in a body length variable
                        this.headers.put(body_header,String.valueOf(this.bodyLength)); //Create header content length with the byte count of the body
                        m = version+" "+200+" "+"ok\n" +
                                body_header + ":" + headers.get(body_header) + "\n\n";// part of the return message
                        this.returnMessage  = m + this.body.get(0);// return message to be sent if all conditions pass
                    }else{
                        this.returnMessage = version + " "+406+" Database error\r\n"; //return message if database couldn't be place
                    }
                }else {
                    this.returnMessage = version+" "+402+" Bad State\r\n"; //return message if no pending order exists
                }
            }
        }
        return this.returnMessage;
    }



    public String list(){

        String t = "";
        String r = "";
        String w = "";


        if(this.operation.equals("LIST") && this.usedVersion.equals(version) && this.headers.containsKey("Cookie") && (this.headers.get("Cookie").equals(this.cookie))){
            if(this.target.equals("order")){//Check to see if order was passed into the target variable
                if(order.getOrders().containsKey("CustomerID")){//Check to see if an order is open
                    this.body = new ArrayList<>();
                for(int i : order.getProducts().keySet()){//Add the contents of the products HasHMap to the body of the message to be sent to the client
                    t = i + "\t" + order.getProducts().get(i) + "\r\n";
                    this.body.add(t);
                }

                for(String i : body){//Get the content length of the body
                    this.bodyLength += i.getBytes().length;
                }

                this.headers.put(body_header,String.valueOf(this.bodyLength));//Create an header for content length

                r = version+" "+200+" "+"ok\n" +
                        body_header + ": " + headers.get(body_header) + "\n\n";

                for(int i = 0; i < body.size(); i++){//Turn body of message to be sent to a string
                    w += body.get(i);
                }

                this.returnMessage = r + w + "\r\n";
                }else{
                    this.returnMessage = version+" "+402+" wrong state\r\n";//if condition to check if an order exists should fail
                }
            }else if(this.target.equals("customer")){//Condition to see if customer is the tsrget
                 this.body = new ArrayList<>();
                 this.body = db.list(this.target);//Get customer from database and add to the body of the message t be sent

                for(String i : body){
                    this.bodyLength += i.getBytes().length;//Get content length of the body
                }

                this.headers.put(body_header,String.valueOf(this.bodyLength));//Create header with content length

                r = version+" "+200+" "+"ok\n" +
                        body_header + ": " + headers.get(body_header) + "\n\n";

                for(int i = 0; i < body.size(); i++){//Turn body of message to be sent to a string
                    w += body.get(i);
                }
                this.returnMessage = r + w;

            }else if(this.target.equals("product")){//TO check if product was entered
                this.body = new ArrayList<>();
                this.body = db.list(this.target);//Get products from body and add to the body of the message
                for(String i : body){
                    this.bodyLength += i.getBytes().length;//Get content length of the body
                }

                this.headers.put(body_header,String.valueOf(this.bodyLength));//Created header with content length

                r = version+" "+200+" "+"ok\n" +
                        body_header + ": " + headers.get(body_header) + "\n\n";

                for(int i = 0; i < body.size(); i++){//Turn body of message to be sent to a string
                    w += body.get(i);
                }
                this.returnMessage = r + w;
            }else{
                this.returnMessage = version+" "+403+" Bad Target\r\n";//Return message if neither customer nor order nor product is the target
            }
        }else {
            this.returnMessage = version+" "+470+" Bad Information\r\n";//Return message if first condition fails
        }

        return this.returnMessage;
    }


    //Action Method to handle drop cases
    public String drop(){

        //Condition to check for right protocol and right action.
        if((!this.target.equals("DROP")) && (!this.usedVersion.equals(version))){
            this.returnMessage = version + " "+ 460 + " Bad Input\r\n";// return message if condition fails
        }else{

             //Condition to check if user is authenticated, might be redundant.
             if(!this.headers.keySet().contains("Cookie") && this.headers.get("Cookie").equals(this.cookie)){
                this.returnMessage = version+" "+471+" User not authenticated"; // return message if conditions fail
            }else{

                 //Condition to check if order exists
                 if(order.getOrders().keySet().contains("CustomerID")){
                     order = new Orders();//set order object to a new instance
                     this.returnMessage = version+" "+200+" ok\r\n";// return message if condition passes
                 }else{
                     this.returnMessage = version+" "+402+" Bad state\r\n"; //return message of no order exists
                 }
             }
        }
        return  this.returnMessage;
    }


}
