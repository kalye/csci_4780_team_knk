import java.io.*;
import java.net.*;


public class myftpserver {
	private String inputFromClientS;
	private String commands[];

    //default constructor
    myftpserver(){
    };
    private boolean deleteFileFromServer(String fileName){
    	boolean success = false;
    	try{
    		File fe = new File(fileName);
    		if(fe.delete())
			success = true;
		else
			success = false;
    	}
    	catch(SecurityException e )
    	{
    		System.out.println("You can not delete file: " +fileName);
    		System.out.println(e.getMessage());
    	}

    	return success;
		
    }
    private byte[] convertFileToByteArray(String fileName){
    	FileInputStream fis = null;
    	BufferedInputStream bis = null;
    	byte[] fileArray = null;
    	try
    	{

    	File fe = new File(fileName);
		if(fe.canRead()){
			fis = new FileInputStream(fe);
			bis = new BufferedInputStream(fis);
			fileArray = new byte[(int)fe.length()];
			bis.read(fileArray);
		}
		else
			System.err.println("File: " + fe.getName() + " cannot be read");

		fis.close();
		bis.close();
	}
	catch(IOException e)
	{
		System.out.println("Exception caught when trying to read file: " + fileName);
		System.out.println(e.getMessage());
	}
	catch(NullPointerException e)
	{
		System.out.println("Exception caught when trying to read file: " + fileName);
		System.out.println(e.getMessage());
	}
	
		return fileArray;

    }

    private void byteArrayToFile(String fileName, byte[] fileArray){

    	
    	FileOutputStream fos = null;
    	BufferedOutputStream bos = null;
    	try {
    		File fe = new File(fileName);
    		fe.createNewFile();
    		fos = new FileOutputStream(fe);
    		bos = new BufferedOutputStream(fos);
    		bos.write(fileArray);
    		fos.close();
    		bos.close();
    	}
    	catch(IOException e)
	{
		System.out.println("Exception caught when trying to read file: " + fileName);
		System.out.println(e.getMessage());
	}
	catch(NullPointerException e)
	{
		System.out.println("Exception caught when trying to read file: " + fileName);
		System.out.println(e.getMessage());
	}

    }
    private String pwd(){
    	String workingDir = System.getProperty("user.dir");
    	return workingDir;
    }
    private void createServerSocket(int portNumber){
    	while(true)
    	{
		try(
			//Open Socket
			ServerSocket serverSocket = new ServerSocket(portNumber);
			//Wait for connection 
			Socket clientSocket = serverSocket.accept();
			//Get output from client
			PrintWriter outputToClient = new PrintWriter (clientSocket.getOutputStream(), true);
			//Get input from client
			BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			//Output to Click with a buffer
			BufferedOutputStream osToClient = new BufferedOutputStream(clientSocket.getOutputStream());
			//Input from client as BufferedInputer 
			BufferedInputStream inBuffFromClient= new BufferedInputStream(clientSocket.getInputStream());
			)
		{
			
			while(true)
			{
				inputFromClientS = inputFromClient.readLine();
				commands = inputFromClientS.split(" ");
				if(commands[0].equals("get"))
				{
					System.err.println("DEBUG: get command received ");
					osToClient.write(convertFileToByteArray(commands[1]));
					osToClient.flush();
					System.out.println("File: " + commands[1] + " transfer complete");
				}
				else if(commands[0].equals("put"))
				{
					System.err.println("DEBUG: put command received ");
					int maxFileSize = 1000*1024;
					byte[] fileArray = new byte[maxFileSize];
					inBuffFromClient.read(fileArray);
					byteArrayToFile(commands[1],fileArray);
					System.out.println("File: " + commands[1] + " saved to server successfully");
				}
				else if(commands[0].equals("delete"))
				{
					System.err.println("DEBUG: delete command received ");
					boolean success = deleteFileFromServer(commands[1]);
					if(success)
						outputToClient.println("File was deleted");
					else
						outputToClient.println("File wasn't deleted");
				}
				else if(commands[0].equals( "ls"))
				{
					System.err.println("DEBUG: ls command received ");
				}
				else if(commands[0].equals( "cd"))
				{
					System.err.println("DEBUG: cd command received ");
				}
				else if(commands[0].equals( "mkdir"))
				{
					System.err.println("DEBUG: mkdir command received ");
				}
				else if(commands[0].equals( "pwd"))
				{
					System.err.println("DEBUG: pwd command received ");
					outputToClient.println("Current directory : " + pwd());
					
				}
				else if(commands[0].equals( "quit"))
				{
					System.err.println("DEBUG: quit command received ");

				}
				else 
				{
					System.err.println("DEBUG: " + commands[0] + " is not a valid command");
				}
			}
			
			
		}
		catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
		
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
			myftpserver mfs = new myftpserver();
			mfs.createServerSocket(portNumber);
		}
}

}

