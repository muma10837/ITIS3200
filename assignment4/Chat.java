import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

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
    private String user = null;
    private Integer port = null;
    private BufferedWriter outStreamWriteToSocket = null;
    private BufferedReader inStreamReadFromSocket = null;
    private ArrayList<Key> listOfKeys = null;
    private ArrayList<String> listOfKeyUsers = null;
    private Scanner in = new Scanner(System.in);
    
    public Chat(String h, String p) {
        port = Integer.parseInt(p);
        hostName = h;
    }
    
    public void runChatClient() throws IOException {
        int index = -1;
        System.out.println("Chat Client is statrting ......");
        socket = new Socket(hostName, port); //(1) create a socket and connect to server 
        outStreamWriteToSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        inStreamReadFromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.print("Please give your username(keep in mind username needs to be on key list): ");
        
        while(user == null) {    
            user = in.nextLine();
            index = listOfKeyUsers.indexOf(user);
            if(index < 0) {
                user = null;
                System.out.print("This user is not in the database please try again : ");
            }
        }
        
        int userIndex = index;
        
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
                        if(readFromSocket != null) {
                        Key key = listOfKeys.get(listOfKeyUsers.indexOf(readFromSocket.substring(readFromSocket.indexOf("Id: ") + 4, readFromSocket.indexOf(",Message:"))));
                        readFromSocket = new String(encryptOrDecryptText(key, Cipher.DECRYPT_MODE, 
                                DatatypeConverter.parseHexBinary(readFromSocket.substring(readFromSocket.indexOf("Message: ") + 9))));
                        System.out.println(listOfKeyUsers.get(listOfKeys.indexOf(key)) + ": "+ readFromSocket);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchPaddingException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidKeyException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalBlockSizeException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BadPaddingException ex) {
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
                    if(userInput.equals("exit")) {
                        close();
                    }
                    outStreamWriteToSocket.write("Id: " + user + ",Message: " + 
                            DatatypeConverter.printHexBinary(encryptOrDecryptText(listOfKeys.get(userIndex), 
                                    Cipher.ENCRYPT_MODE, userInput.getBytes())));
                    outStreamWriteToSocket.newLine();
                    outStreamWriteToSocket.flush();
                }
                } catch (IOException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchPaddingException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidKeyException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalBlockSizeException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BadPaddingException ex) {
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
    
    public void populateKeys(String location) throws FileNotFoundException {
        String input= "";
        File file = new File("./" + location);
        Scanner in = new Scanner(file);
        listOfKeys = new ArrayList<>();
        listOfKeyUsers = new ArrayList<>();
                
        while(in.hasNextLine()) {
            input = in.nextLine();
            listOfKeyUsers.add(input.substring(0, input.indexOf(": ")));
            String key = input.substring(input.indexOf(": ") + 2);
            Key secretkey = generateSymmetricKey(key, "AES");
            listOfKeys.add(secretkey);
        }
    }
    
    public static Key generateSymmetricKey(String secretKey, String keyType) {
        byte[] keyInByte = secretKey.getBytes();
        return new SecretKeySpec(keyInByte, keyType);
    }
    
    public static byte[] encryptOrDecryptText(Key key, int encryptOrDecryptMode, byte[] textOrCipher) throws NoSuchAlgorithmException
            , NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        
        Cipher cipher = Cipher.getInstance(key.getAlgorithm());
        cipher.init(encryptOrDecryptMode, key);
        return cipher.doFinal(textOrCipher);
    }

     // Chat client starting point
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length < 3) {
            System.out.println("Hostname and port are expected from command line");
            System.exit(1);
        }
        Chat chat = null;
        try{
            chat = new Chat(args[0], args[1]);
            chat.populateKeys(args[2]);
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
