import java.io.IOException;
import java.net.ServerSocket;

public class FTPServer {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java myftpserver <port number>");
			System.exit(1);
		} else {
			int portNumber = Integer.parseInt(args[0]);
			boolean serverRunning = true;
			try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
	            while (serverRunning) {
		            new ClientRequestHandler(serverSocket.accept()).start();
		        }
		    } catch (IOException e) {
	            System.err.println("Could not listen on port " + portNumber);
	            System.exit(-1);
	        }
		}
	}
}
