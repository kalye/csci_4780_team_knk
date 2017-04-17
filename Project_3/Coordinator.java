import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Set;
public class Coordinator{
	private static int port;
	private static long time;
	private String inputFromClientS;
	private String commands[];
	private static Hashtable<Integer, CoordinatorThreadA> Participants = new Hashtable<>();
	private static Hashtable<Long,String> AllMessages = new Hashtable<>();
	private static Hashtable<Integer,Long> TimeOfDisconnect = new Hashtable<>();
	
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
		time = Long.parseLong(configCommands[1]) *1000;
	}

final static class CoordinatorThreadA implements Runnable
{
	String userInput, commands[], chostName;
	int cport;
	public DataOutputStream tempDOS;
	volatile boolean shutdown = false;
	public CoordinatorThreadA(String hostName, String port)
	{
		chostName = hostName;
		cport = Integer.parseInt(port);
	}
	public void run(){
		try(Socket aSocket = new Socket(chostName,cport);
			DataOutputStream outputToServer = new DataOutputStream(aSocket.getOutputStream());
			BufferedReader inputFromServer =
                new BufferedReader(
                    new InputStreamReader(aSocket.getInputStream()));
            
			 ) 
		{
			tempDOS = outputToServer;
			while(!shutdown)
			{
				
			}
		}

		catch(IOException e)
		{
			System.out.println(e + "Exception caught when creating socket");
		}
		finally
		{
			try{
			tempDOS.close();}
			catch(IOException e)
			{
				System.out.println(e + "Exception caught when closing stream");
			}
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
	public void shutdown(){
		shutdown = true;
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
					int id = Integer.parseInt(inputDClient.readUTF());
					Participants.remove(id);

				}
				else if(commands[0].equals("disconnect"))
				{
					int id = Integer.parseInt(inputDClient.readUTF());
					long timeOfDisconnect = Long.parseLong(inputDClient.readUTF());
					TimeOfDisconnect.put(id,timeOfDisconnect);
					Participants.remove(id);
				}
				else if(commands[0].equals("reconnect"))
				{
					String id = inputDClient.readUTF();
					String port = inputDClient.readUTF();
					String hostName = inputDClient.readUTF();
					long timeOfReconnect = Long.parseLong(inputDClient.readUTF());
					CoordinatorThreadA temp = new CoordinatorThreadA(hostName,port);
					Thread thread = new Thread(temp);
					Participants.put(Integer.parseInt(id),temp);
					thread.start();
					thread.sleep(1);
					reconnect(timeOfReconnect,Integer.parseInt(id),temp);

				}
				else if(commands[0].equals("msend"))
				{
					AllMessages.put(System.currentTimeMillis(),commands[1]);
					System.out.println("["+commands[1]+"] " + " Send to all Participants");
					Collection<CoordinatorThreadA> cta = Participants.values();
					for(CoordinatorThreadA temp: cta)
					{
						temp.send(commands[1],temp.tempDOS);
					}
				}
				else if(commands[0].equals("print"))
				{
					//All messages
					Set set = AllMessages.entrySet();
					// check set values
   					System.out.println("Set values: " + set);
					
				}
		 	}
		 }
		 catch(IOException e)
		 {
		 	System.out.println(e + "Exception caught when listeing for clientsocket");
		 }
		 catch(InterruptedException e)
		 {
		 	System.out.println(e + "Error caught while thread sleeping");
		 }
		

		 } //End of Run 

	public void reconnect(long tOReconnect, int id, CoordinatorThreadA tempThread){
		Set<Long> times = AllMessages.keySet();
		long tOfDisconnect = TimeOfDisconnect.get(id);
		//CoordinatorThreadA tempThread = Participants.get(id);
		long timeFrame = tOfDisconnect + Coordinator.time;
		if(tOReconnect < timeFrame)
		{
		System.out.println("Statement1");
		System.out.println("Time Frame:" + timeFrame);
		System.out.println("Reconnect Time: " + tOReconnect);

		for(long temp: times)
		{
			String message = AllMessages.get(temp);
			if(temp > tOfDisconnect)
			{
				tempThread.send(message,tempThread.tempDOS);
			}
		}//End of For
		}//End of IF
		else
		{
			long adjustedTimeFrame = tOfDisconnect + (tOReconnect-timeFrame);
			System.out.println("Statement2");
			System.out.println("Time Frame:" + adjustedTimeFrame);
			System.out.println("Reconnect Time: " + tOReconnect);

			for(long temp: times)
			{
			String message = AllMessages.get(temp);
			if(temp > adjustedTimeFrame)
			{
				tempThread.send(message,tempThread.tempDOS);
			}
			}//End of For
		}
	}
		 
		
}

	
} //End of Program 

