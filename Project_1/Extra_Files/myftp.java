/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package project1_ds_team3;

/**
 *
 * @author kosiuzodinma
 */
import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;

public class myftp {
	private static final String PROMPT_MSG = "mytftp>";
	private static final String fileSeparator = FileSystems.getDefault().getSeparator();
	private int terminatePort=0;
	private String hostname = "";
    public myftp(){}
    
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
    public void createSocket(String hostName, int nport, int tport)
    {

    	this.terminatePort = tport;
    	this.hostname = hostName;
        //variables 
        String userInput, serverInput;
        File fe;
        
        //Resourse Statements closes all of these objects after the program closes 
        try (
            Socket myftpSocket = new Socket(hostName, nport);
            DataInputStream inputDServer = new DataInputStream(myftpSocket.getInputStream());
            DataOutputStream outputToServer = new DataOutputStream(myftpSocket.getOutputStream());
            BufferedReader inputFromServer =
                new BufferedReader(
                    new InputStreamReader(myftpSocket.getInputStream()));
        		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
        				myftpSocket.getOutputStream()));
            BufferedReader stdInput =
                new BufferedReader(
                    new InputStreamReader(System.in));
        ) {
        	System.out.print(PROMPT_MSG);
            while (true) {
                userInput = stdInput.readLine();
                if (userInput == null || "".equals(userInput)) {
					continue;
				} 
                userInput = replacePrompt(userInput);
                boolean multipleCommand = false;
                if(userInput.contains(" & ")){
                	multipleCommand = true;
                }
                if(multipleCommand){
                	executeMultipleCommand(userInput, inputDServer, outputToServer, inputFromServer, writer);
                } else {
                    executeSingleCommand(userInput, inputDServer, outputToServer, inputFromServer, writer);
                }
                
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
    }

	private void executeMultipleCommand(String userInput, DataInputStream inputDServer, DataOutputStream outputToServer,
			BufferedReader inputFromServer, BufferedWriter writer) {
		String[] commands = userInput.split("&");
		for(String cmd: commands){
			new Thread(() ->{
				try {
					executeSingleCommand(cmd, inputDServer, outputToServer, inputFromServer, writer);
				} catch (IOException e) {
					System.out.println("Exception caught while executing multiple command in executeMultipleCommand()");
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}).start();;
		}
		
	}

	private void executeSingleCommand(String userInput, DataInputStream inputDServer,
			DataOutputStream outputToServer, BufferedReader inputFromServer, BufferedWriter writer) throws IOException {
		outputToServer.writeUTF(userInput);
        outputToServer.flush();
		String[] commands;
		commands = userInput.split(" ");
           
		if(commands[0].equals("quit"))
		{
		    System.err.println("Socket Closed");
		    System.exit(0);
		} 
		if(commands[0].equals("get"))
		{
			System.out.print(inputFromServer.readLine());
			byteArrayToFile(commands[1], inputDServer);
			System.out.print(PROMPT_MSG);
		    //getFileFromServer(commands[1], inputFromServer);
		}
		else if(commands[0].equals("put"))
		{
			System.out.print(inputFromServer.readLine());
			convertFileToByteArray(commands[1], outputToServer);
			System.out.print(PROMPT_MSG);
			//sendFileToRemoteServer(commands[1], writer, inputFromServer);

		}
		else if(commands[0].equals("quit"))
		{
		    System.err.println("Socket Closed");
		    System.exit(0);
		} else if (userInput.contains("ls")) {
			lsFileInRemoteServerDirectory(userInput, writer, inputFromServer);
		} else if (userInput.contains("cd")) {
			cdRemoteServerDirectory(userInput, writer, inputFromServer);
		} else if (userInput.contains("mkdir")) {
			mkdirRemoteServerDirectory(userInput, writer, inputFromServer);
		} else if(userInput.equals("pwd")){
			System.out.println(inputFromServer.readLine());
			System.out.print(PROMPT_MSG);
		} else if(commands[0].equals("delete")){
			System.out.println(inputFromServer.readLine());
			System.out.print(PROMPT_MSG);
		} else if(commands[0].equals("terminate")){
			executeTerminateCommand(commands[1]);
		}
	}

	private void executeTerminateCommand(String command_ID) {
		 Socket myftpSocket;
		try {
			myftpSocket = new Socket(this.hostname, this.terminatePort);
	         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
     				myftpSocket.getOutputStream()));
	            writer.write(command_ID);
				writer.newLine();
				writer.flush();
		} catch (IOException e) {
			 System.err.println("Error executing terminate command " + command_ID + " .Error-" + e.getMessage());
		}
         
	}

	private void sendFileToRemoteServer(String filename, BufferedWriter writer, BufferedReader inputFromServer) throws IOException {
		try {
			String line = null;
			FileReader fileReader = new FileReader(filename);
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
			while((line = bufferedReader.readLine()) != null) {
				writer.write(line.replaceAll("\\s", ""));
				writer.newLine();
				writer.flush();
            } 
			bufferedReader.close();
			writer.write("Sending file " + filename + " is successful.");
			writer.newLine();
			writer.flush();
			System.out.println(inputFromServer.readLine());
		} catch (IOException | NullPointerException e ) {
			writer.write("Error reading file " + filename + ". " + e.getMessage());
			System.out.println("Exception caught when trying to read file: " + writer);
			System.out.println(e.getMessage());
		} 
		System.out.print(PROMPT_MSG);
	}

	private void getFileFromServer(String filename, BufferedReader inputFromServer) {
		String orginalFileName = filename;
		int lastindex = filename.lastIndexOf(fileSeparator);
		if(lastindex > -1){
			filename = filename.substring(lastindex + 1);
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
			while((line = inputFromServer.readLine()) != null){
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
			}
			bos.flush();
			bos.close();
			fe = new File(filename);
			if(fe.exists() && !success){
				fe.delete();
			}
			System.out.print(PROMPT_MSG);
		}
		catch(IOException e)
		{
		    System.err.println("There was an I/O error");
		    System.err.println(e);
		}
	}
	private void lsFileInRemoteServerDirectory(String userInput, BufferedWriter writer,
			BufferedReader inputFromServer) throws IOException {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try{
			userInput = inputFromServer.readLine();
			while (userInput != null) {
					System.out.println(userInput);
					if(inputFromServer.ready()){
						userInput = inputFromServer.readLine();
					} else {
						break;
					}

			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.print(PROMPT_MSG);
	}
	private void waitForServerResponse(BufferedReader inputFromServer) throws IOException {
		String response = null;
		while (true) {
			response = inputFromServer.readLine();
			if (response == null || "".equals(response)) {
				continue;
			} else {
				break;
			}
		}
		System.out.println(response);
	}
	private void cdRemoteServerDirectory(String userInput, BufferedWriter writer, BufferedReader inputFromServer)
			throws IOException {
		waitForServerResponse(inputFromServer);
		System.out.print(PROMPT_MSG);
	}
	private void mkdirRemoteServerDirectory(String userInput, BufferedWriter writer,
			BufferedReader inputFromServer) throws IOException {
		waitForServerResponse(inputFromServer);
		System.out.print(PROMPT_MSG);

	}
	private String replacePrompt(String userInput) {
		if (userInput != null && userInput.startsWith(PROMPT_MSG)) {
			userInput = userInput.replace(PROMPT_MSG, "");
		}
		return userInput;
	}
    public static void main(String args[])
    {
        //Making sure correct input is given by the user 
        if (args.length != 3) {
            System.err.println("Usage: java myftp <host name> normal port<nport>  and terminate port<tport>");
            System.exit(1);
        }

        myftp mfp = new myftp();
        mfp.createSocket(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }
}
