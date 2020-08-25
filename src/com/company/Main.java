package com.company;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
	// write your code here

        Scanner in = new Scanner(System.in); //Scanner object to help get input from the console
        int port; //Variable to store port number

        System.out.println("Enter the port number"); // Request for port number to use
        port = in.nextInt(); //Port number stored in variable port

        Server myProtocol = new Server(port); //Server instance

    }
}
