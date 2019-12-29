import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
public class Chatd implements Runnable {

    public ServerSocket chatdServerSocket = null;
    public ArrayList<Socket> connectedChatClient = new ArrayList<>();
    private BufferedWriter outStreamWriteToClient = null;
    
    public Chatd(int portNumber) throws IOException {
        chatdServerSocket = new ServerSocket(portNumber); // (1) create a socket and bind to a port
        System.out.println("Server Started and listening in port: " + portNumber);
    }
    
    public void runServer() throws IOException {
        
        while (true) {
            try {
        	Socket s = chatdServerSocket.accept(); // (2) Wait to accept connections from clinets
                connectedChatClient.add(s); // (3) add the accetped socket to the list
                Thread thread = new Thread(this); 
                thread.start();

            } catch (IOException ex) {
                chatdServerSocket.close();
                Logger.getLogger(Chatd.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void close() throws IOException {
        chatdServerSocket.close();
    }

    @Override
    public void run() {
        String input = null;
        BufferedReader inStreamReadFromSocket = null;
        Socket currentConnectedSocket = connectedChatClient.get(connectedChatClient.size() - 1); 
       
        try {
            // (3) Create a Buffered Reader uing current connetecd socket to read from the socket  --- OUT#1
            inStreamReadFromSocket = new BufferedReader(new InputStreamReader(currentConnectedSocket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(Chatd.class.getName()).log(Level.SEVERE, null, ex);
            try {
                close();
            } catch (IOException ex1) {
                Logger.getLogger(Chatd.class.getName()).log(Level.SEVERE, null, ex1);
            }
            System.out.println("Could not able to write to client socket");
            System.exit(1);
        }

        try {
            // (4) Reading from the client socket --- OUT#2
            /*************************************write your code as condition for while loop********************************/
            while ((input = inStreamReadFromSocket.readLine()) != null) {
                System.out.println("Message from client " + currentConnectedSocket + " : " + input);

                for (Socket socket : connectedChatClient) {

                    if (socket.getPort() != currentConnectedSocket.getPort()) {
			// (5) Forward received message to the other clients in the same chat room  --- OUT#3 
			sendMessage(input, new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                    }
                }
            }
        } catch (IOException ex) {
            try {
                close();
                inStreamReadFromSocket.close();
            } catch (IOException ex1) {
                Logger.getLogger(Chatd.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(Chatd.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public void sendMessage(String message, BufferedWriter streamOut) throws IOException {
        streamOut.write(message);
        streamOut.newLine();
        streamOut.flush();
    }

    //Chat server starting point
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if(args.length < 1) {
            System.out.println("Chatd expects port number as command line argument");
            System.exit(1);
            
        }
        Chatd server = null;
        try {
            server = new Chatd(Integer.parseInt(args[0]));
            server.runServer();
        } catch (IOException e) {
            if (server.chatdServerSocket != null && !server.chatdServerSocket.isClosed()) {
                try {
                    server.chatdServerSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace(System.err);
                }
            }
        }
    }
    
}
