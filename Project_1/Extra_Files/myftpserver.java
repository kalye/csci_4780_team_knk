import java.io.IOException;
import java.net.ServerSocket;

public class myftpserver {

	// default constructor
	myftpserver() {
	};

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: java myftpserver normal port <nport> and terminate port <tport> .");
			System.exit(1);
		} else {
			int nport = Integer.parseInt(args[0]);
			int tport = Integer.parseInt(args[1]);
			boolean serverRunning = true;
			new Thread(() -> {
				ServerSocket tserverSocket;
				try {
					tserverSocket = new ServerSocket(tport);
					while (serverRunning) {
						new ClientRequestHandler(tserverSocket.accept()).start();
					}
				} catch (IOException e) {
					System.out.println("Exception caught when trying to listen on port " + tport + " or listening for a connection");
		            System.out.println(e.getMessage());
				}

			}).start();
			new Thread(() -> {
				ServerSocket serverSocket;
				try {
					serverSocket = new ServerSocket(nport);
					while (serverRunning) {
						new ClientRequestHandler(serverSocket.accept()).start();
					}
				} catch (IOException e) {
					System.out.println("Exception caught when trying to listen on port " + nport + " or listening for a connection");
		            System.out.println(e.getMessage());
				}

			}).start();

		}
	}

}
