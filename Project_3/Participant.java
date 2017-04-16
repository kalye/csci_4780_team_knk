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
					Thread threadB = new Thread(new ParticipantThreadB(Integer.parseInt(commands[1])));
					threadB.start();
					outputToServer.writeUTF(Integer.toString(Participant.ID));
					outputToServer.writeUTF(commands[1]);
					outputToServer.writeUTF(Participant.hostName);
					outputToServer.flush();
				}
				else if(commands[0].equals("deregister"))
				{
					outputToServer.writeUTF(Integer.toString(Participant.ID));
					outputToServer.flush();
				}
				else if(commands[0].equals("disconnect"))
				{
					outputToServer.writeUTF(Integer.toString(Participant.ID));
					outputToServer.flush();
				}
				else if(commands[0].equals("reconnect"))
				{
					outputToServer.writeUTF(Integer.toString(Participant.ID));
					outputToServer.flush();
				}
				else if(commands[0].equals("msend"))
				{
					outputToServer.writeUTF(commands[1]);
					writer4file.write(commands[1]);
					writer4file.newLine();
					writer4file.flush();
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
	}
} //End of Thread A

final static class ParticipantThreadB implements Runnable
{
	String inputFromClientS, commands[];
	int bport = 0;
	public ParticipantThreadB(int port)
	{
		bport = port;
	}

	public void run(){

		 try(
			//Open Socket
			ServerSocket serverSocket = new ServerSocket(bport);
			//Wait for connection 
			Socket clientSocket = serverSocket.accept();
			//Get input from client
			BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			DataInputStream inputDClient = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream outputDClient = new DataOutputStream(clientSocket.getOutputStream());
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					clientSocket.getOutputStream()));
			){
		 	while(true)
		 	{
		 		inputFromClientS = inputDClient.readUTF();
				System.out.println(inputFromClientS);
				if(inputFromClientS == null || inputFromClient.equals("")){
					continue;
				}
				commands = inputFromClientS.split(" ");
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

		 }
		 
		
}
	
} //End of Program




