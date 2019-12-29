
/**
 *
 * @author muma10837
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread {

    private Socket socket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private int id = 0;
    StringTokenizer stok = null;
    Server2 server = null;

    private boolean bExit = true;

    public ServerThread(Server2 inServer, Socket inSocket) {
        socket = inSocket;
        id = socket.getPort();
        server = inServer;
    }

    public void open() {
        try {
            //open IO streams
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {

        String inputLine;

        //wait and read input from client. Exit when input from socket is Bye
        while (bExit) {
            try {
                System.out.println("Waiting for input ...");
                inputLine = in.readLine();
                System.out.println("Server: " + inputLine);
                server.requestLookup(inputLine, id);
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void send(String lookupAnswer) {
        out.println(lookupAnswer);
    }

    public int getID() {
        return id;
    }

    public void close() {
        try {
            bExit = false;
            socket.close();
            in.close();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
