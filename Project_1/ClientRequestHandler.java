import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientRequestHandler extends Thread {

	private Socket socket = null;
	private String currentWorkingDirectory;
	private static final String fileSeparator = FileSystems.getDefault().getSeparator();

	public ClientRequestHandler(Socket socket) {
		this.socket = socket;
		currentWorkingDirectory = System.getProperty("user.dir");
	}

	public void run() {
		try {
			if (socket.isClosed()) {
				System.exit(0);
			}
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String inputLine, outputLine;
			while (true) {
				inputLine = in.readLine();
				if(inputLine == null || inputLine.isEmpty()){
					continue;
				}
				if (inputLine.contains("get")) {
					sendFile(inputLine, out);
				} else if (inputLine.contains("put")) {
					putFileInRemoteDirectory(inputLine, in);
				} else if (inputLine.contains("delete")) {
					deleteFileFromRemoteDirectory(inputLine, out);
				} else if (inputLine.contains("ls")) {
					lsCurrentDirectory(out);
				} else if (inputLine.contains("cd")) {
					changeDirectory(inputLine);
				} else if (inputLine.contains("mkdir")) {
					makdeDirectory(inputLine, out);
				} else if (inputLine.contains("pwd")) {
					out.println(getCurrentWorkingDirectory());
				} else if (socket.isClosed() || inputLine.equalsIgnoreCase("quit")) {
					System.exit(0);
				}
			}
		} catch (Exception e) {

		}
	}

	private void putFileInRemoteDirectory(String inputLine, BufferedReader in) throws IOException {
		inputLine = cleanString(inputLine).trim();
		// String fileName = getFileName(inputLine);
		byte[] buffer = new byte[4096];
		int read = 0;
		int totalRead = 0;
		String fileSizeString = in.readLine();
		int filesize = getFileSize(fileSizeString);
		int remaining = filesize;
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		FileOutputStream fos = new FileOutputStream(inputLine);
		while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
			totalRead += read;
			remaining -= read;
			System.out.println("read " + totalRead + " bytes.");
			fos.write(buffer, 0, read);
		}

		fos.close();
		dis.close();
	}

	private int getFileSize(String fileSizeString) {
		if (fileSizeString != null && fileSizeString.contains("fileSize:")) {
			return Integer.parseInt(fileSizeString.replace("fileSize:", ""));
		}
		return 0;
	}

	private void deleteFileFromRemoteDirectory(String inputLine, PrintWriter out) {
		inputLine = cleanString(inputLine).trim();
		File f = null;
	      boolean bool = false;
	      
	      try{
	         f = new File(inputLine);
	         bool = f.delete();
	         if(bool){
	        	 out.println("File deleted: "+bool);
	         } else {
	        	 out.println("Deleting file : " + inputLine + " failed.");
	         }
	            
	      }catch(Exception e){
	    	  out.println("Deleting file : " + inputLine + " failed. Exeption" + e.getMessage());
	      }
		
	}

	private void lsCurrentDirectory(PrintWriter out) {
		File curDir = new File(currentWorkingDirectory);
		File[] filesList = curDir.listFiles();
		StringBuilder builder = new StringBuilder();
		for (File f : filesList) {
			if (f.isDirectory())
				appendSubDirectory(f, builder);
			if (f.isFile()) {
				builder.append("\n").append(f.getName());
			}
		}
		out.println(builder.toString());

	}

	public void appendSubDirectory(File curDir, StringBuilder builder) {
		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isDirectory()) {
				builder.append("\n").append(f.getName());
			}
		}
	}

	private void makdeDirectory(String inputLine, PrintWriter out) {
		try {
			inputLine = cleanString(inputLine).trim();
			Path pathToFile = Paths.get(inputLine);
			Files.createDirectories(pathToFile);
			out.println("directory " + inputLine + " is created.");
		} catch (Exception e) {
			out.println("error creating directory " + inputLine + ". Error is: " + e.getMessage());
		}

	}

	private void changeDirectory(String inputLine) {
		inputLine = cleanString(inputLine).trim();
		Path newPath = Paths.get(inputLine);
		if (Files.exists(newPath)) {
			currentWorkingDirectory = newPath.toAbsolutePath().toString();
		}

	}

	private String getFileName(String userInput) {
		if (userInput == null || userInput.endsWith(fileSeparator)) {
			return "";
		}
		if (userInput.contains(fileSeparator)) {
			int lastIndex = userInput.lastIndexOf(fileSeparator);
			return userInput.substring(lastIndex + 1, userInput.length());
		}
		return userInput;
	}

	private String cleanString(String userInput) {
		if (userInput == null) {
			return "";
		}
		if (userInput.matches("^(get|put|mkdir|cd|delete)\\s*([a-zA-Z]:)?(" + fileSeparator + "?[a-zA-Z0-9_.-]+)+"
				+ fileSeparator + "?")) {
			return userInput.replaceAll("get|put|mkdir|cd|delete", "");
		}
		return userInput;
	}

	private void sendFile(String inputLine, PrintWriter out) throws IOException {
		inputLine = cleanString(inputLine).trim();
		String fileName = getFileName(inputLine);
		fileName = currentWorkingDirectory + fileSeparator + fileName;
		FileInputStream fis = new FileInputStream(fileName);
		int fileSize = fis.available();
		out.println("fileSize:" + fileSize);
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		byte[] buffer = new byte[4096];

		while (fis.read(buffer) > 0) {
			dos.write(buffer);
		}

		fis.close();
		dos.close();

	}

	public String getCurrentWorkingDirectory() {
		return currentWorkingDirectory;
	}

	public static void main(String[] args) {
		ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null);
		clientRequestHandler.lsCurrentDirectory(null);
		System.out.println();
	}
}
