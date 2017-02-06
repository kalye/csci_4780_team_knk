import java.io.*;
import java.net.*;


public class myftpserver {


    //default constructor
    myftpserver(){
    };
    
    private static void createServerSocket(int portNumber){
		try(
			//Open Socket
			ServerSocket serverSocket = new ServerSocket(portNumber);
			//Wait for connection 
			Socket clientSocket = serverSocket.accept(); 
			//Get output from client
			PrintWriter outputToClient = new PrintWriter (clientSocket.getOutputStream(), true);
			//Get input from client
			BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			)
		{
			String inputFromClientS;
			while( (inputFromClientS = inputFromClient.readLine()) !=null )
			{
				/*
					For the commands I was thinking we could make individual methods for all of the commands so it would look cleaner
				*/
				outputToClient.println(inputFromClientS);
				if(inputFromClientS.equals("get"))
				{
					System.err.println("DEBUG: get command received ");
				}
				else if(inputFromClientS.equals( "put"))
				{
					System.err.println("DEBUG: put command received ");
				}
				else if(inputFromClientS.equals("delete"))
				{
					System.err.println("DEBUG: delete command received ");
				}
				else if(inputFromClientS.equals( "ls"))
				{
					System.err.println("DEBUG: ls command received ");
				}
				else if(inputFromClientS.equals( "cd"))
				{
					System.err.println("DEBUG: cd command received ");
				}
				else if(inputFromClientS.equals( "mkdir"))
				{
					System.err.println("DEBUG: mkdir command received ");
				}
				else if(inputFromClientS.equals( "pwd"))
				{
					System.err.println("DEBUG: pwd command received ");
				}
				else if(inputFromClientS.equals( "quit"))
				{
					System.err.println("DEBUG: quit command received ");
				}
			}
			
			
		}
		catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
		
	}
	public static void main(String[] args) {
		if (args.length != 1)
       {
           System.err.println("Usage: java myftpserver <port number>");
           System.exit(1);
       }
       else
       {
			int portNumber = Integer.parseInt(args[0]);
			createServerSocket(portNumber);
		}
}

}

