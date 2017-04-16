import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Collection;
public class Coordinator{
	private static int port;
	private static int time;
	private String inputFromClientS;
	private String commands[];
	private static Hashtable<Integer, CoordinatorThreadA> Participants = new Hashtable<>();
	
	public static void main(String[] args)
	{
		readConfig(args[0]);
		try(ServerSocket serverSocket = new ServerSocket(port);) //Listening
		{
			while (true) { //Continue listening with threads
        	Socket sock = serverSocket.accept();
        	new Thread(new CoordinatorThreadB(sock)).start();
      		} //End of WHile 
		} 
		catch(IOException e)
		{
			System.out.println(e + "Error caught when starting connection with client");
		}
		
		

	}

	private static void readConfig(String fileName){
		String configCommands[] = new String[2];
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName))))
		{
			String linef = br.readLine();
			int i = 0;
			while(linef !=null){
				configCommands[i] = linef;
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
	}

final static class CoordinatorThreadA implements Runnable
{
	String userInput, commands[], chostName;
	int cport;
	public DataOutputStream tempDOS;
	public CoordinatorThreadA(String hostName, String port)
	{
		chostName = hostName;
		cport = Integer.parseInt(port);
	}
	public void run(){
		try(Socket aSocket = new Socket(chostName,cport);
			//BufferedWriter writer4file = new BufferedWriter(new FileWriter(Participant.log, true));
			DataOutputStream outputToServer = new DataOutputStream(aSocket.getOutputStream());
			BufferedReader inputFromServer =
                new BufferedReader(
                    new InputStreamReader(aSocket.getInputStream()));
            
			 ) 
		{
			tempDOS = outputToServer;
			while(true)
			{
				//userInput = stdInput.readLine();
				if (userInput == null || "".equals(userInput)) {
					continue;
				} 
				outputToServer.writeUTF(userInput);
                outputToServer.flush();
                commands = userInput.split(" ");
				
			}
		}

		catch(IOException e)
		{
			System.out.println(e + "Exception caught when creating socket");
		}
		
	}//End of Run 
	public void send(String message, DataOutputStream dos)
	{
		try{
			dos.writeUTF("Ack: [" + message + "] " + "Received");
			dos.flush();
		}
		catch(IOException e)
		{
			System.out.println(e + "Error caught trying to send message");
		}
		
	}
} //End of Thread A

final static class CoordinatorThreadB implements Runnable
{
	Socket clientSocket;
	String inputFromClientS, commands[];
	int bport = 0;
	public CoordinatorThreadB(Socket sock)
	{
		clientSocket = sock;
	}

	public void run(){

		 try(
			DataInputStream inputDClient = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream outputDClient = new DataOutputStream(clientSocket.getOutputStream());
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					clientSocket.getOutputStream()));
			){

		 	while(true) 
		 	{
		 		inputFromClientS = inputDClient.readUTF();
				if(inputFromClientS == null ){
					continue;
				}
				commands = inputFromClientS.split(" ");
				if(commands[0].equals("register"))
				{
					String id = inputDClient.readUTF();
					String port = inputDClient.readUTF();
					String hostName = inputDClient.readUTF();
					CoordinatorThreadA temp = new CoordinatorThreadA(hostName,port);
					Thread thread = new Thread(temp);
					Participants.put(Integer.parseInt(id),temp);
					thread.start();
				}
				else if(commands[0].equals("deregister"))
				{
					
				}
				else if(commands[0].equals("disconnect"))
				{
					
				}
				else if(commands[0].equals("reconnect"))
				{
					
				}
				else if(commands[0].equals("msend"))
				{
					System.out.println("["+commands[1]+"] " + " Send to all Participants");
					Collection<CoordinatorThreadA> cta = Participants.values();
					for(CoordinatorThreadA temp: cta)
					{
						temp.send(commands[1],temp.tempDOS);
					}
				}
		 	}
		 }
		 catch(IOException e)
		 {
		 	System.out.println(e + "Exception caught when listeing for clientsocket");
		 }
		

		 }
		 
		
}

	
} //End of Program 

