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
			//execute/start server that listen to terminate command using annonymous/lambda thread so that it won't block execusion of
			//the normal server
			new Thread(() -> {
				ServerSocket tserverSocket;
				try {
					//create server socket
					tserverSocket = new ServerSocket(tport);
					//loop and wait until we get client connection request
					while (serverRunning) {
						//wait on client to connect to this server and once they do create ClientRequestHandler that interact with the client
						//once the creation of ClientRequestHandler is done start the thread and wait for another connection
						//note serverSocket.accept() method wait until client connect to this server
						new ClientRequestHandler(tserverSocket.accept()).start();
					}
				} catch (IOException e) {
					System.out.println("Exception caught when trying to listen on port " + tport + " or listening for a connection");
		            System.out.println(e.getMessage());
				}

			}).start();
			//start the normal server socket using annonymous/lambda thread. Actually this can be started normally without annonymous thread as it doesn't
			//block other serversocket like the terminal server
			new Thread(() -> {
				ServerSocket serverSocket;
				try {
					//create server socket
					serverSocket = new ServerSocket(nport);
					//loop and wait until we get client connection request
					while (serverRunning) {
						//wait on client to connect to this server and once they do create ClientRequestHandler that interact with the client
						//once the creation of ClientRequestHandler is done start the thread and wait for another connection
						//note serverSocket.accept() method wait until client connect to this server
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
