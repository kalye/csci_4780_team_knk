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
import java.util.concurrent.locks.*;

public class ClientRequestHandler extends Thread {

	private String inputFromClientS;
	private String commands[];
	private String currentWorkingDirectory;
	private Socket socket;
	private boolean putTerminated = false;
	private boolean getTerminated = false;
	private static String tFileName;
	private static final String fileSeparator = FileSystems.getDefault().getSeparator();
	public static final Map<String, ClientRequestHandler> TERMINATE_COMMAND_ID_CLIENT_THREAD = new HashMap<>();
	public static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public ClientRequestHandler(Socket socket) {
		this.socket = socket;
		currentWorkingDirectory = System.getProperty("user.dir");
	}

	public void run() {
		try {
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
					String commandId = "g_" + this.getId();
					tFileName = commands[1];
					writer.write(commandId);
					writer.newLine();
					writer.flush();
					TERMINATE_COMMAND_ID_CLIENT_THREAD.put(commandId, this);
					sendFile(commands[1], outputDClient);
					if(!getTerminated)
						System.out.println("File: " + commands[1] + " transfer complete");
				} else if (commands[0].equals("put")) {
					String commandId = "p_" + this.getId();
					tFileName = commands[1];
					writer.write(commandId);
					writer.newLine();
					writer.flush();
					TERMINATE_COMMAND_ID_CLIENT_THREAD.put(commandId, this);
					receiveFile(commands[1], inputDClient);
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
					ClientRequestHandler handler = TERMINATE_COMMAND_ID_CLIENT_THREAD.get(commands[1]);
					if(handler != null){
						handler.setTerminatedFlag(commands[1]);
						System.err.println("DEBUG: terminate command received ");
						if(commands[1].startsWith("p"))
						{
							File fe = new File(tFileName);
							fe.delete();
							writer.write("");
							writer.newLine();
							writer.flush();
						}
						else if(commands[1].startsWith("g"))
						{
							writer.write(tFileName);
							writer.newLine();
							writer.flush();
						}
						
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

	private void sendFile(String fileName, DataOutputStream sStream) {
		BufferedInputStream bis = null;
		byte[] fileArray = null;
		lock.writeLock().lock();
		try {
			if (!Paths.get(fileName).isAbsolute())
			{
				fileName = currentWorkingDirectory + fileSeparator + fileName;
			}
			File fe = new File(fileName);
			long size = fe.length();
			sStream.writeLong(fe.length()); // Size of file
			int bytesRead = 0;
			if (fe.canRead()) {
				bis = new BufferedInputStream(new FileInputStream(fe));
				fileArray = new byte[1024];
				while (size >0)
				{
					bytesRead = bis.read(fileArray,0,fileArray.length);
					sStream.write(fileArray);
					sStream.flush();
					size -= bytesRead;
					if(getTerminated)
					{
						size = -1;
					}
				}
			} else
			{
				System.err.println("File: " + fe.getName() + " cannot be read");
				bis.close();
				return;
			}
			bis.close();
			sStream.flush();
		} catch (IOException e) {
			System.out.println("Exception caught when trying to read file: " + fileName);
			System.out.println(e.getMessage());
		} catch (NullPointerException e) {
			System.out.println("Exception caught when trying to read file: " + fileName);
			System.out.println(e.getMessage());
		}
		finally
		{
			lock.writeLock().unlock();
		}

	}

	private void receiveFile(String fileName, DataInputStream cStream) {
		lock.readLock().lock();
		try {
			BufferedOutputStream bos = null;
			byte[] fileArray = new byte[1024];
			File fe = new File(fileName);
			fe.createNewFile();
			long size = cStream.readLong();
			bos = new BufferedOutputStream(new FileOutputStream(fe));
			int bytesRead = 0;
			while (size > 0) {
				if(putTerminated)
				{
					cStream.skipBytes((int)size);
					size = -1;
					//bos.flush();
				}
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
		finally{
			lock.readLock().unlock();
		}

	}

	private boolean deleteFileFromServer(String fileName) {
		lock.writeLock().lock();
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
		finally{
			lock.writeLock().unlock();
		}

		return success;

	}

}
