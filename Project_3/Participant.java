import java.net.ServerSocket;
import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;
import java.util.*;
public class Participant {
	private static int ID;
    private static int aport;
    private static String hostName = null;
    public static int bPort = 0;
    private static File log = null;
    private static ParticipantThreadB currentThreadB = null;

	public static void main(String[] args) {

		readConfig(args[0]);
		Thread threadA = new Thread(new ParticipantThreadA());
		threadA.start();
	}

	    
	    private static void readConfig(String fileName){
		String configCommands[] = new String[4];
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
			String temp[] = configCommands[2].split(":");
			configCommands[2] = temp[0];
			configCommands[3] = temp[1];
		}
		catch (FileNotFoundException ex){
			System.out.println(ex + "File wasn't found");
		}
		catch(IOException ex){
			System.out.println(ex + "IO Exception reading file");
		}
		ID = Integer.parseInt(configCommands[0]);
		log = new File(configCommands[1]);
		try{
		if(!log.exists())
		{
			log.createNewFile();
		}
		}
		catch (IOException e)
		{
			System.out.println(e + "error caught with file");
		}
		hostName = configCommands[2]; //Save them to the static variable hostname 
		aport = Integer.parseInt(configCommands[3]); //Save them to the static variable port 
	}//End of read config
	
	

final static class ParticipantThreadA implements Runnable
{
	String userInput, commands[];
	boolean offline = false;
	public void run(){
		try(Socket aSocket = new Socket(Participant.hostName,Participant.aport);
			BufferedWriter writer4file = new BufferedWriter(new FileWriter(Participant.log, true));
			DataOutputStream outputToServer = new DataOutputStream(aSocket.getOutputStream());
			BufferedReader inputFromServer =
                new BufferedReader(
                    new InputStreamReader(aSocket.getInputStream()));
        		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
        				aSocket.getOutputStream()));
            BufferedReader stdInput =
                new BufferedReader(
                    new InputStreamReader(System.in));
			 ) 
		{
			while(true)
			{
				userInput = stdInput.readLine();
				if (userInput == null || "".equals(userInput)) {
					continue;
				} 
				outputToServer.writeUTF(userInput);
                outputToServer.flush();
                commands = userInput.split(" ");
				if(commands[0].equals("register"))
				{
					ParticipantThreadB tempThread = new ParticipantThreadB(Integer.parseInt(commands[1]));
					currentThreadB = tempThread;
					Thread threadB = new Thread(tempThread);
					threadB.start();
					threadB.sleep(3);
					outputToServer.writeUTF(Integer.toString(Participant.ID));
					outputToServer.writeUTF(commands[1]);
					outputToServer.writeUTF(Participant.hostName);
					outputToServer.flush();
				}
				else if(commands[0].equals("deregister"))
				{
					outputToServer.writeUTF(Integer.toString(Participant.ID));
					outputToServer.flush();
					currentThreadB.shutdown();
				}
				else if(commands[0].equals("disconnect"))
				{
					outputToServer.writeUTF(Integer.toString(Participant.ID));
					outputToServer.writeUTF(Long.toString(System.currentTimeMillis()));
					outputToServer.flush();
					currentThreadB.shutdown();
				}
				else if(commands[0].equals("reconnect"))
				{
					ParticipantThreadB tempThread = new ParticipantThreadB(Integer.parseInt(commands[1]));
					currentThreadB = tempThread;
					Thread threadB = new Thread(tempThread);
					threadB.start();
					outputToServer.writeUTF(Integer.toString(Participant.ID));
					outputToServer.writeUTF(commands[1]);
					outputToServer.writeUTF(Participant.hostName);
					outputToServer.writeUTF(Long.toString(System.currentTimeMillis()));
					outputToServer.flush();
				}
				else if(commands[0].equals("msend"))
				{
					outputToServer.writeUTF(commands[1]);
					outputToServer.flush();
				}
			}
		}

		catch(UnknownHostException e)
		{
			System.out.println(e + "Exception caught when creating socket");
		}
		catch(IOException e)
		{
			System.out.println(e + "Exception caught when creating socket");
		}
		catch(InterruptedException e)
		{
			System.out.println(e + "Exception caught while thread was sleeping" );
		}
	}
} //End of Thread A

final static class ParticipantThreadB implements Runnable
{
	String inputFromClientS, commands[];
	int bport = 0;
	volatile boolean shutdown = false;
	ServerSocket serverSocket;
	public ParticipantThreadB(int port)
	{
		bport = port;
	}

	public void run(){
		
		try
		{
		//Open Connection 
		serverSocket = new ServerSocket(bport);
		}
		catch(IOException e)
		{
			System.out.println(e + "Error caught creating ThreadB socket");
		}

		 try(
			//Wait for connection 
			Socket clientSocket = serverSocket.accept();
			//Get input from client
			BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			//Writer for file
			BufferedWriter writer4file = new BufferedWriter(new FileWriter(Participant.log, true));
			DataInputStream inputDClient = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream outputDClient = new DataOutputStream(clientSocket.getOutputStream());
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					clientSocket.getOutputStream()));
			){
		 	while(!shutdown)
		 	{
		 		inputFromClientS = inputDClient.readUTF();
				System.out.println(inputFromClientS);
				writer4file.write(inputFromClientS);
				writer4file.newLine();
				writer4file.flush();
				if(inputFromClientS == null){
					continue;
				}
				
		 	}
		 }
		 catch(UnknownHostException e)
		{
			System.out.println(e + "Exception caught when creating socket");
		}
		catch(IOException e)
		{
			System.out.println(e + "Exception caught when creating socket");
		}
		finally{
			try{serverSocket.close();}
			catch(IOException e)
			{
				System.out.println(e + "Problem closing serverSocket");
			}
			
		}

		 }//end of run

	public void shutdown()throws IOException{
		shutdown = true;
		serverSocket.close();
	}
		 
		
}
	
} //End of Program




