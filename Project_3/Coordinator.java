import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
public class Coordinator{
	private static int port;
	private static int time;
	private String inputFromClientS;
	private String commands[];
	//private Hashtable ;
	private static void readConfig(String fileName){
		String configCommands[] = new String[2];
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName))))
		{
			String linef = br.readLine();
			int i = 0;
			while(linef !=null){
				configCommands[i] = linef;
				//System.out.println(linef);
				linef = br.readLine();
				i++;
			}
			
		}
		catch (FileNotFoundException ex){
			System.out.println(ex + "File wasn't found");
		}
		catch(IOException ex){
			System.out.println(ex + "IO Exception reading file");
		}
		port = Integer.parseInt(configCommands[0]);
		time = Integer.parseInt(configCommands[1]);
		//return configCommands;
	}

private void createServerSocket(int portNumber){
    	while(true)
    	{
		try(
			//Open Socket
			ServerSocket serverSocket = new ServerSocket(portNumber);
			//Wait for connection 
			Socket clientSocket = serverSocket.accept();
			//Get input from client
			BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			DataInputStream inputDClient = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream outputDClient = new DataOutputStream(clientSocket.getOutputStream());
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					clientSocket.getOutputStream()));
			)
		{
			
			while(true)
			{
				inputFromClientS = inputDClient.readUTF();
				System.out.println(inputFromClientS);
				if(inputFromClientS == null || inputFromClient.equals("")){
					continue;
				}
				commands = inputFromClientS.split(" ");
				if(commands[0].equals("get"))
				{
					System.err.println("DEBUG: get command received ");
					
					System.out.println("File: " + commands[1] + " transfer complete");
				}
				else if(commands[0].equals("register"))
				{
					String id = inputDClient.readUTF();
					String portnum = inputDClient.readUTF();
					String hostname = inputDClient.readUTF(); 
					//System.out.println(one + " " + two + " " + three);
				}
				else if(commands[0].equals("delete"))
				{
					
				}
				else if(commands[0].equals( "ls"))
				{
					
				}
				else if(commands[0].equals( "cd"))
				{
					
					
				}
				else if(commands[0].equals( "mkdir"))
				{
					
				}
				else if(commands[0].equals( "pwd"))
				{
					
					
				}
				else if(commands[0].equals( "quit"))
				{
					System.err.println("DEBUG: quit command received ");
					clientSocket.close();
					break;

				}
				
			}
			
			
		}
		catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
		
	}
}

	public static void main(String[] args)
	{
		readConfig(args[0]);
		Coordinator c = new Coordinator();
		c.createServerSocket(port);

	}
}

