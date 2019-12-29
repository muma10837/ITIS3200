
/**
 *
 * @author mahmed27 & muma10837
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server2 implements Runnable {

    private ServerSocket serverSocket = null;
    private Scanner inFile = null;
    private StringTokenizer stok = null;
    private Socket clientSocket = null;
    private Thread thread = null;

    private ArrayList<ServerThread> client = new ArrayList<>();
    private ArrayList<String> domainNames = new ArrayList<>();
    ArrayList<String> ipAddress = new ArrayList<>();

    private volatile boolean continue1 = true;

    public Server2(String[] args) {
        try {
            if (args.length > 0) {
                serverSocket = new ServerSocket(Integer.parseInt(args[0]));
            } else {
                serverSocket = new ServerSocket(10007);
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: 10007.");
            System.exit(1);
        }

        try {
            if (args.length > 1) {
                inFile = new Scanner(new File(args[1]));
            } else {
                inFile = new Scanner(new File("DNS-table.txt"));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File error");
        }

        while (inFile.hasNext()) {
            stok = new StringTokenizer(inFile.nextLine(), ", ");
            domainNames.add(stok.nextToken());
            ipAddress.add(stok.nextToken());
        }

        inFile.close();
    }

    public void threadStart() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (continue1) {
                clientSocket = null;
                System.out.println("Waiting for connection ...");

                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    System.err.println("Accept failed.");
                    System.exit(1);
                }

                System.out.println("Connection successful");
                client.add(new ServerThread(this, clientSocket));
                client.get(client.size() - 1).open();
                client.get(client.size() - 1).start();
            }

            clientSocket.close();
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void requestLookup(String inputLine, int id) {
        String action;
        stok = new StringTokenizer(inputLine, " ");
        action = stok.nextToken();

        if (action.equals(".bye")) {
            client.get(findClient(id)).send(".bye");
            removeClient(id);
        } else if (action.equals("get-ip")) {
            action = stok.nextToken();
            if (domainNames.contains(action)) {
                client.get(findClient(id)).send(ipAddress.get(domainNames.indexOf(action)));
            } else if (action.equals("0")) {
                client.get(findClient(id)).send("connection closed");
                removeClient(id);
            } else {
                client.get(findClient(id)).send("error this ip is not in the registry");
            }
        } else if (action.equals("get-hostname")) {
            action = stok.nextToken();
            if (ipAddress.contains(action)) {
                client.get(findClient(id)).send(domainNames.get(ipAddress.indexOf(action)));
            } else {
                client.get(findClient(id)).send("error this hostname is not in the registry");
            }
        } else if (action.equals(".bye.exit")) {
            for (int x = 0; x < client.size(); x++) {
                client.get(x).send(".bye");
                removeClient(client.get(x).getID());

            }
            continue1 = false;
            thread.interrupt();
        } else {
            client.get(findClient(id)).send("Please use .bye, get-ip, or get-hostname");
        }
    }

    public int findClient(int id) {
        for (int x = 0; x < client.size(); x++) {
            if (client.get(x).getID() == id) {
                return x;
            }
        }
        return -1;
    }

    public void removeClient(int id) {
        ServerThread temp = client.remove(findClient(id));
        temp.close();
    }

    public static void main(String[] args) throws IOException {
        Server2 server = new Server2(args);
        server.threadStart();

    }
}
