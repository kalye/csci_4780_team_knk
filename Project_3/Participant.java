import java.net.ServerSocket;
import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;
import java.util.*;
public class Participant {
	int uniqueid;
	
	public Participant() {
    }
	    private static final String fileSeparator = FileSystems.getDefault().getSeparator();
	    private static String tFileName = "";
	    private int terminatePort = 0;
	    private String hostname = "";
	    private static int aport;
	    public static String hostName = null;
	    public static int bPort = 0;
	    //public static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	    
	    private static String[] readConfig(String fileName){
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
		hostName = configCommands[2]; //Save them to the static variable hostname 
		aport = Integer.parseInt(configCommands[3]); //Save them to the static variable port 
		return configCommands;
	}
	    
	    // public void createSocket(String hostName, int aport, int bport) {
	        
	    //     this.terminatePort = aport;
	    //     this.hostname = hostName;
	    //     this.aport = aport;
	    //     // variables
	    //     String userInput, serverInput;
	    //     File fe;
	        
	    //     // Resourse Statements closes all of these objects after the program
	    //     // closes
	    //     try (Socket myftpSocket = new Socket(hostName, aport);
	    //          DataInputStream inputDServer = new DataInputStream(myftpSocket.getInputStream());
	    //          DataOutputStream outputToServer = new DataOutputStream(myftpSocket.getOutputStream());
	    //          BufferedReader inputFromServer = new BufferedReader(
	    //                                                              new InputStreamReader(myftpSocket.getInputStream()));
	    //          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(myftpSocket.getOutputStream()));
	    //          BufferedReader stdInput = new BufferedReader(new InputStreamReader(System.in));) {
	    //         System.out.print("Participant");
     //            userInput = stdInput.readLine();

	    //     } catch (UnknownHostException e) {
	    //         System.err.println("Don't know about host " + hostName);
	    //         System.exit(1);
	    //     } catch (IOException e) {
	    //         System.err.println("Couldn't get I/O for the connection to " + hostName);
	    //         System.exit(1);
	    //     }
	    // }

	    

	    

	    
	
	
	
	public static void main(String[] args) {

		String[] config = readConfig(args[0]);
		
		Thread threadA = new Thread(new ParticipantThreadA());
		//Thread threadB = new Thread(new ParticipantThreadB());
		threadA.start();
		// threadB.start();
	}

final static class ParticipantThreadA implements Runnable
{
	String userInput, commands[];
	public void run(){
		try(Socket aSocket = new Socket(Participant.hostName,Participant.aport);
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
				}
				else if(commands[0].equals("deregister"))
				{
					//deregister();
				}
				else if(commands[0].equals("disconnect"))
				{
					//disconnect();
				}
				else if(commands[0].equals("reconnect"))
				{
					//reconnect();
				}
				else if(commands[0].equals("mSend"))
				{
					//mSend();
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
	public void register(int portNumber){
		
	}
	
	public void deregister(){
		
	}
	
	public void disconnect(){
		
	}
	
	public void reconnect(int portNumber){
		
	}
	
	public void mSend(String message){
		
	}
}

final static class ParticipantThreadB implements Runnable
{
	int bport = 0;
	public ParticipantThreadB(int port)
	{
		bport = port;
	}

	public void run(){

		 try(ServerSocket bServer = new ServerSocket(bport)){}
		 
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




}