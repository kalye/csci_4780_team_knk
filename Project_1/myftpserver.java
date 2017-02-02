package myftpserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class MyFtpServer {
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private DataInputStream inputStream;
	private PrintStream outputToClient;
	private byte[] output;
	private SocketAddress socketAddress;
    InetAddress ipAddress;

    //default constructor
    MyFtpServer(){};
    
    public void createServerSocket(int portNumber){
		try {
			ipAddress = InetAddress.getLocalHost();
			socketAddress = new InetSocketAddress(ipAddress, portNumber);
		} catch (UnknownHostException e1) {
			System.out.println("Could not get local host");
		}
		try{
			//Open Socket
			serverSocket = new ServerSocket(portNumber);
			clientSocket = serverSocket.accept(); 
			clientSocket.bind(socketAddress);
			//receive inputStream from client
			inputStream = new DataInputStream(clientSocket.getInputStream());
			//output to client
			outputToClient = new PrintStream(clientSocket.getOutputStream());
			outputToClient.write(output);
			
			}
		catch(Exception e){
			System.out.println(e);
		}
		//close sockets
		try {
			outputToClient.close();
			inputStream.close();
			serverSocket.close();
			clientSocket.close();
		} catch (IOException e) {
			System.out.print(e);
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int portNumber = 80;
		MyFtpServer server = new MyFtpServer();
		while(true){
			server.createServerSocket(portNumber);
		}
	}
}

