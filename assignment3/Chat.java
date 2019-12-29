import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mahmed27 & muma10837
 */
public class Chat {
    
    private Socket socket = null;
    private String hostName = null;
    private Integer port = null;
    private BufferedWriter outStreamWriteToSocket = null;
    private BufferedReader inStreamReadFromSocket = null;
    
    public Chat(String h, String p) {
        port = Integer.parseInt(p);
        hostName = h;
    }
    
    public void runChatClient() throws IOException {
        System.out.println("Chat Client is statrting ......");
        socket = new Socket(hostName, port); //(1) create a socket and connect to server 
        outStreamWriteToSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        inStreamReadFromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        //Start a new thread to read from the socket to receive messages from other clients
        // (2) create a thread that reads messages from server and prints to the screen 
        Thread toRead = new Thread(new Runnable(){ // reading from the server socket 
            @Override
            public void run() {
                
                String readFromSocket;
                try {
                    // (3)always read from the the server socket and print to the screen --- OUT#1
                    while(true) {
                        readFromSocket = inStreamReadFromSocket.readLine();
                        System.out.println(readFromSocket);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
        toRead.start();
        
        //start a new thread to write to socket to send message to other clients
        Thread toWrite = new Thread(new Runnable(){ // writting to the server socket
            @Override
            public void run() {
                String userInput;
                BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in));
                System.out.println("Message to send: ");
                try {
		 // (4)always read from the standard output and write to the server socket --- OUT#2
                while(true) {
                    userInput = stdIn.readLine();
                    outStreamWriteToSocket.write(userInput);
                    outStreamWriteToSocket.newLine();
                    outStreamWriteToSocket.flush();
                }
                } catch (IOException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        toWrite.start();
    }
       
    public void close() throws IOException {
        outStreamWriteToSocket.close();
        inStreamReadFromSocket.close();
        socket.close();
    }

     // Chat client starting point
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length < 2) {
            System.out.println("Hostname and port are expected from command line");
            System.exit(1);
        }
        Chat chat = null;
        try{
            chat = new Chat(args[0], args[1]);
            chat.runChatClient();
        } catch(Exception e) {
            try {
                chat.close();
            } catch (IOException ex) {
                Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
    
}
