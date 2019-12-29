

/**
 *
 * @author mahmed27 & muma10837
 */
import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {

        String serverHostname = new String ("127.0.0.1");
	int serverPortNumber = 10007; 

        if (args.length > 0) {
            //pass the hsotname through cmd argument
            serverHostname = args[0];
            serverPortNumber = Integer.parseInt(args[1]);
        }
        System.out.println ("Attemping to connect to host " + serverHostname + " on port 10007.");

        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            //Connect to server and open IO stream
            echoSocket = new Socket(serverHostname, serverPortNumber);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                                        echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for " + "the connection to: " + serverHostname);
            System.exit(1);
        }

	BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in));
	String userInput;

        System.out.print ("input-comand: ");
	while ((userInput = stdIn.readLine()) != null) {
            //send to server using socket
	    out.println(userInput);
            //read reply from server
            String replyFromServer = in.readLine();
	    System.out.println("Answer: " + replyFromServer);
            if(replyFromServer.equals(".bye") || replyFromServer.equals("connection closed")){
                break;
            }
            System.out.print ("input-command: ");
	}

	out.close();
	in.close();
	stdIn.close();
	echoSocket.close();
    }
}
