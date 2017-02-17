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
    public myftp(){}
    
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
    public void createSocket(String hostName, int portNumber)
    {

        //variables 
        String userInput, serverInput, commands[];
        File fe;
        
        //Resourse Statements closes all of these objects after the program closes 
        try (
            Socket myftpSocket = new Socket(hostName, portNumber);
            BufferedInputStream isFromServer = new BufferedInputStream(myftpSocket.getInputStream());
            BufferedOutputStream osBuffToServer = new BufferedOutputStream(myftpSocket.getOutputStream());
            PrintWriter outputToServer =
                new PrintWriter(myftpSocket.getOutputStream(), true);
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
                outputToServer.println(userInput);
                commands = userInput.split(" ");
                if(commands[0].equals("get"))
                {
                    getFileFromServer(commands[1], inputFromServer);
                }
                else if(commands[0].equals("put"))
                {
                	sendFileToRemoteServer(commands[1], writer, inputFromServer);

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
				}
                else
                    System.out.print(PROMPT_MSG);
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

	private void sendFileToRemoteServer(String filename, BufferedWriter writer, BufferedReader inputFromServer) throws IOException {
		try {
			String line = null;
			FileReader fileReader = new FileReader(filename);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
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
        if (args.length != 2) {
            System.err.println("Usage: java myftp <host name> <port number>");
            System.exit(1);
        }

        myftp mfp = new myftp();
        mfp.createSocket(args[0], Integer.parseInt(args[1]));
    }
}
