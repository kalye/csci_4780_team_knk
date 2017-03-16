import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
//This is the class that handle client communication/request per each connection
//This class run as multiple thread-each client connection is handled by object of this class as different thread
public class ClientRequestHandler extends Thread {

	private String inputFromClientS;
	private String commands[];
	//used for holding the current directory-or used to simulate the current working directory of the server 
	//each client may have different directory
	private String currentWorkingDirectory;
	private Socket socket;
	//used for terminating the current put transfer of file
	private boolean putTerminated = false;
	//used for terminating the current get transfer of file
	private boolean getTerminated = false;
	//used for file separator character for different platform
	private static final String fileSeparator = FileSystems.getDefault().getSeparator();
	//shared map used between different thread to hold get and put command id for later used to 
	//distinguish which thread handled the get or put command
	//put command begin with p_{threadId} while get g_{threadId}
	public static final Map<String, ClientRequestHandler> TERMINATE_COMMAND_ID_CLIENT_THREAD = new HashMap<>();

	public ClientRequestHandler(Socket socket) {
		this.socket = socket;
		//save the current working directory of server as soon as the clientHandler created
		currentWorkingDirectory = System.getProperty("user.dir");
	}

	public void run() {
		try {
			//if the socket is alread closed exist
			if (socket.isClosed()) {
				System.exit(0);
			}
			BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			DataInputStream inputDClient = new DataInputStream(socket.getInputStream());
			DataOutputStream outputDClient = new DataOutputStream(socket.getOutputStream());
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			while (true) {
				inputFromClientS = inputDClient.readUTF();
				if (inputFromClientS == null || inputFromClient.equals("")) {
					continue;
				}
				commands = inputFromClientS.split(" ");
				if (commands[0].equals("get")) {
					System.err.println("DEBUG: get command received ");
					//create get command id
					String commandId = "g_" + this.getId();
					//send get command id to the client
					writer.write(commandId);
					writer.newLine();
					writer.flush();
					//save get command id so that if later the client send terminate command we can use to tell which thread handled the get command
					//and use it to set getTerminated. GetTerminated will be used to terminate get transfer
					TERMINATE_COMMAND_ID_CLIENT_THREAD.put(commandId, this);
					// sendFile(commands[1], writer);
					convertFileToByteArray(commands[1], outputDClient);
					System.out.println("File: " + commands[1] + " transfer complete");
				} else if (commands[0].equals("put")) {
					//create put command id
					String commandId = "p_" + this.getId();
					writer.write(commandId);
					writer.newLine();
					writer.flush();
					//save put command id so that if later the client send terminate command we can use to tell which thread handled the put command
					//and use it to set putTerminated. PutTerminated will be used to terminate put transfer
					TERMINATE_COMMAND_ID_CLIENT_THREAD.put(commandId, this);
					byteArrayToFile(commands[1], inputDClient);
					// readFileFromClient(commands[1], inputFromClient, writer);
					System.out.println("File: " + commands[1] + " saved to server successfully");
				} else if (commands[0].equals("delete")) {
					boolean success = deleteFileFromServer(commands[1]);
					if (success) {
						System.out.println("File: " + commands[1] + " deleted successfully");
						writer.write("File: " + commands[1] + " deleted successfully");
						writer.newLine();
						writer.flush();
					} else {
						System.out.println("File: " + commands[1] + " wasn't deleted successfully");
						writer.write("File: " + commands[1] + " wasn't deleted successfully");
						writer.newLine();
						writer.flush();
					}
				} else if (commands[0].equals("ls")) {
					lsCurrentDirectory(writer);
				} else if (commands[0].equals("cd")) {
					changeDirectory(commands[1], socket);

				} else if (commands[0].equals("mkdir")) {
					makdeDirectory(commands[1], socket);
				} else if (commands[0].equals("pwd")) {
					writer.write("Current directory : " + pwd());
					writer.newLine();
					writer.flush();

				} else if (commands[0].equals("quit")) {
					System.err.println("DEBUG: quit command received ");
					socket.close();
					break;

				} else if(commands[0].equals("terminate")){
					//get the thread/ClientRequestHandler that first created from common map
					ClientRequestHandler handler = TERMINATE_COMMAND_ID_CLIENT_THREAD.get(commands[1]);
					//if there is already handler set the flag/getTerminated or putTerminated
					//so that the ClientRequestHandler will terminate the get/put transfer
					if(handler != null){
						handler.setTerminatedFlag(commands[1]);
					}
				} else {
					System.err.println("DEBUG: " + commands[0] + " is not a valid command");
				}
			}
		} catch (Exception e) {
			System.out.println("Exception caught when trying to listen on port or listening for a connection");
			System.out.println(e.getMessage());
		}
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
    private String pwd(){
    	return currentWorkingDirectory;
    }
    //The method to set the putTerminated/getTerminated to true so that the clientRequestHandler will terminate the transfer
    public void setTerminatedFlag(String commandId){
    	if(commandId.startsWith("p")){
    		this.putTerminated = true;
    	} else {
    		this.getTerminated = true;
    	}
    }
	private void changeDirectory(String inputLine, Socket socket) throws IOException {
		if (inputLine.trim().equals("..")) {
			int lastindex = currentWorkingDirectory.lastIndexOf(fileSeparator);
			if (lastindex > 0) {
				inputLine = currentWorkingDirectory.replace(currentWorkingDirectory.substring(lastindex), "");
			}
		}
		Path newPath = Paths.get(inputLine);
		if (!newPath.isAbsolute()) {
			newPath = Paths.get(currentWorkingDirectory, inputLine);
		}
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
		try {
			for (File f : filesList) {
				if (f.isDirectory()) {
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
		} catch (Exception e) {
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

	private void convertFileToByteArray(String fileName, DataOutputStream sStream) {
		BufferedInputStream bis = null;
		byte[] fileArray = null;
		try {

			File fe = new File(fileName);
			if (fe.canRead()) {
				bis = new BufferedInputStream(new FileInputStream(fe));
				fileArray = new byte[(int) fe.length()];
				bis.read(fileArray);
			} else
				System.err.println("File: " + fe.getName() + " cannot be read");
			bis.close();
			sStream.writeLong(fe.length()); // Size of file
			sStream.write(fileArray); // The file as a byte array
			sStream.flush();
		} catch (IOException e) {
			System.out.println("Exception caught when trying to read file: " + fileName);
			System.out.println(e.getMessage());
		} catch (NullPointerException e) {
			System.out.println("Exception caught when trying to read file: " + fileName);
			System.out.println(e.getMessage());
		}

	}

	private void byteArrayToFile(String fileName, DataInputStream cStream) {

		try {
			BufferedOutputStream bos = null;
			byte[] fileArray = new byte[8 * 1024];
			File fe = new File(fileName);
			fe.createNewFile();
			long size = cStream.readLong();
			bos = new BufferedOutputStream(new FileOutputStream(fe));
			int bytesRead = 0;
			while (size > 0) {
				bytesRead = cStream.read(fileArray, 0, fileArray.length);
				bos.write(fileArray);
				size -= bytesRead;
			}

			bos.close();
		} catch (IOException e) {
			System.out.println("Exception caught when trying to read file: " + fileName);
			System.out.println(e.getMessage());
		} catch (NullPointerException e) {
			System.out.println("Exception caught when trying to read file: " + fileName);
			System.out.println(e.getMessage());
		}

	}

	private boolean deleteFileFromServer(String fileName) {
		Path pathToFile = Paths.get(fileName);
		if (!pathToFile.isAbsolute()) {
			fileName = currentWorkingDirectory + fileSeparator + fileName;
		}
		boolean success = false;
		try {
			File fe = new File(fileName);
			success = fe.delete();
		} catch (SecurityException e) {
			System.out.println("You can not delete file: " + fileName);
			System.out.println(e.getMessage());
		}

		return success;

	}

}
