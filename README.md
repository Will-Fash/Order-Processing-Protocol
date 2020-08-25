## Program Overview:

The program is a server program that runs on its own protocol to communicate with clients, essentially
it receives inputs, validates them and sends the appropriate response in the right format and order to
the client. The server program sits in the middle tier between the client and the database.

## Project Structure:

The program has 6 classes:
• Main: This is where the server is run, an instance of the server is created and instantiated here.
• Server: This is the server class, it initializes a server socket, binds to a port and waits to accep
input from a client.
• Protocol: This is the protocol class that handles the communication between the client and the
server. It describes the format and order and the specific actions that can be taken when
communication is underway between the client and the server.
• Orders: This class keep the orders temporarily until ordered or dropped.
• MyIdentity: This is a helper class that helps to hold all the database credentials needed to
connect with the database.
• Database: This class connects to the database using the mysql connector and helps fulfill client
requests.

## Key Algorithms And DataStructures:

Most of the datastructures used in the program are HashMaps, Sets, Arraylists and Arrays
When input is received the it is processed and used to initialize some global variables which are then
used to process requests by action methods and then send the appropriate response to the client.
For better understanding internal documentations is provided

## Class Interactions:
The main instantiates an object of the server class, which then instantiates an object of the protocol
class to process input, fulfill requests and respond accordingly. The protocol class instantiates an
object of the database class and uses it when necessary to fulfill requests that involve the database.
The order class is used sparingly as it just stores orders before they are either dropped or ordered.

## Assumptions:
The client uses the required protocol.
Only one connection at a time.
A client is in perpetual connection until logout is read.
An order is open until it is either dropped or ordered.