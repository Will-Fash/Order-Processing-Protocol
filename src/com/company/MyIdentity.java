package com.company;

import java.util.Properties;

/*Class to help haandle Database credentials*/
public class MyIdentity {


    public static void setIdentity(Properties prop) {
        prop.setProperty("database", "******"); //Database to use
        prop.setProperty("user", "****"); //Username to use
        prop.setProperty("password", "*****"); //Password to use
    }

}
