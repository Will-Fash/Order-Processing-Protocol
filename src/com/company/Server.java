package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    // Sever variables
    private Socket socket = null;
    private ServerSocket server = null;
    private Protocol pr;
    private int port;


    //Server class constructor
    public Server(int portNumber){
       port = portNumber; // Port number to bind to
       pr = new Protocol(); // Protocol Instance
       run(); // Run runs in the constructor.
    }


    public void run(){
        try{
            /* Bluenose defaults to trying to use IPv6, which isn't working well right now and blocks the ServerSocket call.  This next line tells Java to us IPv4 like the rest of the world. */

            System.setProperty("java.net.preferIPv4Stack", "true");

            server = new ServerSocket(port,1); //Starts server socket instance with the given port number no backlog i.e waits for at most one connection.

        } catch (IOException e){
            System.out.println("Could not listen on port: " + port); // Error reporting
            System.exit(-1);
        }

        try {
            socket = server.accept(); // Server waits fro connection
        } catch (IOException e) {
            System.out.println("Accept failed: " + port);
            System.exit(-1);
        }


        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //Buffer reader instance to read input from the socket.
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // PrintWriter to send information to the socket's output stream
        ) {


            while(true) {
                String inputLine = null; //Where socket input is stored
                String outputLine; //Where server response is stored before it is sent.
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    if(inputLine == null){
                        inputLine = line;
                    }else {
                        inputLine += "\n" + line;
                    }

                    if(line.contains("Content-Length:")){
                        System.out.println(line);
                        inputLine += "\n"+in.readLine();
                        inputLine += "\n"+in.readLine();
                        break; //Breaks out of the infinite loop
                    }
                    if(line.equals("")){
                        break; //Breaks out of the infinite loop
                    }
               }

                if(inputLine != null){
                    System.out.println("Client says:\n" + inputLine); //Show what client says
                    pr.preprocessInput(inputLine);//Call the process input method to process the input
                    outputLine = pr.choice(); //Assign the result of the choice method call to the outptutline; basically the server's output
                    System.out.println("\nServer says:\n" + outputLine); //Show what client says
                    out.println(outputLine); // Send output to the client
                }
            }


        }catch(Exception e){
            System.out.println("server error "
                    + port + " while listening for a connection");
            System.out.println(e.getMessage());
        }
    }

}
