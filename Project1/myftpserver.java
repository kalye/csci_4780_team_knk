import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class myftpserver {
	private String inputFromClientS;
	private String commands[];
	private String currentWorkingDirectory;
	private static final String fileSeparator = FileSystems.getDefault().getSeparator();

    //default constructor
	myftpserver(){
		currentWorkingDirectory = System.getProperty("user.dir");
    };
    private boolean deleteFileFromServer(String fileName){
    	Path pathToFile = Paths.get(fileName);
		if (!pathToFile.isAbsolute()) {
			fileName = currentWorkingDirectory + fileSeparator + fileName;
		}
    	boolean success = false;
    	try{
    		File fe = new File(fileName);
    		success = fe.delete();
    	}
    	catch(SecurityException e )
    	{
    		System.out.println("You can not delete file: " +fileName);
    		System.out.println(e.getMessage());
    	}

    	return success;
		
    }
    private void convertFileToByteArray(String fileName, DataOutputStream sStream){
    	BufferedInputStream bis = null;
    	byte[] fileArray = null;
    	try
    	{

    	File fe = new File(fileName);
		if(fe.canRead()){
			bis = new BufferedInputStream(new FileInputStream(fe));
			fileArray = new byte[(int)fe.length()];
			bis.read(fileArray);
		}
		else
			System.err.println("File: " + fe.getName() + " cannot be read");
		bis.close();
		sStream.writeLong(fe.length()); //Size of file
		sStream.write(fileArray); //The file as a byte array
		sStream.flush();
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

    private void byteArrayToFile(String fileName, DataInputStream cStream){

    	try {
    		BufferedOutputStream bos = null;
    		byte[] fileArray = new byte[8*1024];
    		File fe = new File(fileName);
    		fe.createNewFile();
    		long size = cStream.readLong();
    		bos = new BufferedOutputStream(new FileOutputStream(fe));
    		int bytesRead = 0;
    		while(size >0)
    		{
    			bytesRead = cStream.read(fileArray, 0, fileArray.length);
    			bos.write(fileArray);
    			size -= bytesRead;
    		}
    		
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
    	return currentWorkingDirectory;
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
				if(inputFromClientS == null || inputFromClient.equals("")){
					continue;
				}
				commands = inputFromClientS.split(" ");
				if(commands[0].equals("get"))
				{
					System.err.println("DEBUG: get command received ");
					//sendFile(commands[1], writer);
					convertFileToByteArray(commands[1], outputDClient);
					System.out.println("File: " + commands[1] + " transfer complete");
				}
				else if(commands[0].equals("put"))
				{
					byteArrayToFile(commands[1], inputDClient);
					//readFileFromClient(commands[1], inputFromClient, writer);
					System.out.println("File: " + commands[1] + " saved to server successfully");
				}
				else if(commands[0].equals("delete"))
				{
					boolean success = deleteFileFromServer(commands[1]);
					if(success)
					{
						System.out.println("File: " + commands[1] + " deleted successfully");
						writer.write("File: " + commands[1] + " deleted successfully");
						writer.newLine();
						writer.flush();
					}
					else
					{
						System.out.println("File: " + commands[1] + " wasn't deleted successfully");
						writer.write("File: " + commands[1] + " wasn't deleted successfully");
						writer.newLine();
						writer.flush();	
					}
				}
				else if(commands[0].equals( "ls"))
				{
					lsCurrentDirectory(writer);
				}
				else if(commands[0].equals( "cd"))
				{
					changeDirectory(commands[1], clientSocket);
					
				}
				else if(commands[0].equals( "mkdir"))
				{
					makdeDirectory(commands[1], clientSocket );
				}
				else if(commands[0].equals( "pwd"))
				{
					writer.write("Current directory : " + pwd());
					writer.newLine();
					writer.flush();
					
				}
				else if(commands[0].equals( "quit"))
				{
					System.err.println("DEBUG: quit command received ");
					clientSocket.close();
					break;

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
    private void changeDirectory(String inputLine, Socket socket) throws IOException {
    	if(inputLine.trim().equals("..")){
    		int lastindex = currentWorkingDirectory.lastIndexOf(fileSeparator);
    		if(lastindex > 0){
    			inputLine = currentWorkingDirectory.replace(currentWorkingDirectory.substring(lastindex), "");
    		}
    	}
		Path newPath = Paths.get(inputLine);
		if(!newPath.isAbsolute()){
			newPath = Paths.get(currentWorkingDirectory, inputLine);
		}
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream()));
		if (Files.exists(newPath)) {
			currentWorkingDirectory = newPath.toAbsolutePath().toString();
			writer.write("Directory changed to " + currentWorkingDirectory);
			writer.newLine();
			writer.flush();
			return;
		}
		writer.write("No such file or directory ");
		writer.newLine();
		writer.flush();

	}
    private void lsCurrentDirectory(BufferedWriter writer) throws IOException {
		File curDir = new File(currentWorkingDirectory);
		File[] filesList = curDir.listFiles();
		try{
		for (File f : filesList) {
			if (f.isDirectory()){
				writer.write(f.getName());
				writer.newLine();
				writer.flush();
				File[] dirFilesList = curDir.listFiles();
				for (File dirfile : dirFilesList) {
					if (dirfile.isDirectory()) {
						writer.write(dirfile.getName());
						writer.newLine();
						writer.flush();
					}
				}
			}
			if (f.isFile()) {
				writer.write(f.getName());
				writer.newLine();
				writer.flush();
			}
		}
		} catch(Exception e){
			e.printStackTrace();
			writer.write("Error while listing file in directory " + pwd());
			writer.newLine();
			writer.flush();
			return;
		}
		writer.write("list is done successfully.");
		writer.newLine();
		writer.flush();
		System.out.println("ls command is done");

	}
    private void makdeDirectory(String inputLine, Socket clientSocket) throws IOException {
    	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
    			clientSocket.getOutputStream()));
		try {
			Path pathToFile = Paths.get(inputLine);
			if(!pathToFile.isAbsolute()){
				pathToFile = Paths.get(currentWorkingDirectory, inputLine);
			}
			Files.createDirectories(pathToFile);
			writer.write("directory " + inputLine + " is created.");
			writer.newLine();
			writer.flush();
		} catch (Exception e) {
			writer.write("error creating directory " + inputLine + ". Error is: " + e.getMessage());
			writer.newLine();
			writer.flush();
		}
	}
    private void sendFile(String filename, BufferedWriter writer) throws IOException {
		String originalname = filename;
		try {
			Path pathToFile = Paths.get(filename);
			if (!pathToFile.isAbsolute()) {
				filename = currentWorkingDirectory + fileSeparator + filename;
			}
			String line = null;
			FileReader fileReader = new FileReader(filename);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) {
				writer.write(line.replaceAll("\\s", ""));
				writer.newLine();
				writer.flush();
            } 
			bufferedReader.close();
			writer.write("Sending file " + originalname + " is successful.");
			writer.newLine();
			writer.flush();
		} catch (IOException | NullPointerException e ) {
			writer.write("Error reading file " + originalname + ". " + e.getMessage());
			writer.newLine();
			writer.flush();
			System.out.println("Exception caught when trying to read file: " + writer);
			System.out.println(e.getMessage());
		} 

	}
    private void readFileFromClient(String filename, BufferedReader inputFromClient, BufferedWriter writer) throws IOException {
		String orginalFileName = filename;
		Path pathToFile = Paths.get(filename);
		if (!pathToFile.isAbsolute()) {
			filename = currentWorkingDirectory + fileSeparator + filename;
		}
		try{
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			File fe = new File(filename);
			fe.createNewFile();
			fos = new FileOutputStream(fe);
			bos = new BufferedOutputStream(fos);
			String line = null;
			boolean success = true;
			while((line = inputFromClient.readLine()) != null){
				if(line.contains("Error reading file " + orginalFileName + ".") || line.contains("Sending file " + orginalFileName + " is successful")){
					System.out.println(line);
					if(line.contains("Error reading file " + orginalFileName + ".")){
						success = false;
					}
					break;
				}
				bos.write(line.getBytes());
			}
			if(success){
				System.out.println("File " + filename + " written to current working directory of client.");
				writer.write("File " + orginalFileName + " saved to Server");
				writer.newLine();;
				writer.flush();
			} else {
				writer.write("Something went wrong while saving file " + orginalFileName + " to Server");
				writer.newLine();;
				writer.flush();
			}
			bos.flush();
			bos.close();
			fe = new File(filename);
			if(fe.exists() && !success){
				fe.delete();
			}
		}
		catch(IOException e)
		{
		    System.err.println("There was an I/O error");
		    System.err.println(e);
		    writer.write("File " + orginalFileName + " saved to Server");
			writer.newLine();;
			writer.flush();
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

