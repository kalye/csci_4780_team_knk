import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;

public class FTPClient {

	private static final String PROMPT_MSG = "mytftp>";

	private Socket myftpSocket;
	private static final String fileSeparator = FileSystems.getDefault().getSeparator();

	public FTPClient(Socket myftpSocket) {
		this.myftpSocket = myftpSocket;
	}

	public void processRequest() {
		try {

			PrintWriter outputToServer = new PrintWriter(myftpSocket.getOutputStream(), true);
			BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(myftpSocket.getInputStream()));
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(System.in));
			String userInput;
			while (true) {
				System.out.print(PROMPT_MSG);
				userInput = stdInput.readLine();
				if (userInput == null || "".equals(userInput)) {
					continue;
				} else if (userInput.equalsIgnoreCase("quit")) {
					myftpSocket.close();
					System.exit(0);
				} else if (userInput.contains("get")) {
					getFileAndSaveCurrentDirectory(userInput, outputToServer, inputFromServer);
				} else if (userInput.contains("put")) {
					putFileInRemoteServer(userInput, outputToServer);
				} else if (userInput.contains("delete")) {
					deleteFileInRemoteServer(userInput, outputToServer, inputFromServer);
				} else if (userInput.contains("ls")) {
					lsFileInRemoteServerDirectory(userInput, outputToServer, inputFromServer);
				} else if (userInput.contains("cd")) {
					cdRemoteServerDirectory(userInput, outputToServer, inputFromServer);
				} else if (userInput.contains("mkdir")) {
					mkdirRemoteServerDirectory(userInput, outputToServer, inputFromServer);
				} else if (userInput.contains("pwd")) {
					pwdRemoteServerDirectory(userInput, outputToServer, inputFromServer);
				}
				System.out.print(PROMPT_MSG);
			}
		} catch (Exception e) {
			System.out.println(PROMPT_MSG + e.getMessage());
		}
	}

	private void pwdRemoteServerDirectory(String userInput, PrintWriter outputToServer, BufferedReader inputFromServer)
			throws IOException {
		userInput = replacePrompt(userInput);
		outputToServer.println(userInput);
		System.out.println(PROMPT_MSG + inputFromServer.readLine());
	}

	private void mkdirRemoteServerDirectory(String userInput, PrintWriter outputToServer,
			BufferedReader inputFromServer) throws IOException {
		userInput = replacePrompt(userInput);
		outputToServer.println(userInput);
		System.out.println(PROMPT_MSG + inputFromServer.readLine());

	}

	private void cdRemoteServerDirectory(String userInput, PrintWriter outputToServer, BufferedReader inputFromServer)
			throws IOException {
		userInput = replacePrompt(userInput);
		outputToServer.println(userInput);
		System.out.println("PROMPT_MSG" + inputFromServer.readLine());
	}

	private void lsFileInRemoteServerDirectory(String userInput, PrintWriter outputToServer,
			BufferedReader inputFromServer) throws IOException {
		userInput = replacePrompt(userInput);
		outputToServer.println(userInput);
		System.out.println(PROMPT_MSG + inputFromServer.readLine());
	}

	private void deleteFileInRemoteServer(String userInput, PrintWriter outputToServer, BufferedReader inputFromServer)
			throws IOException {
		userInput = replacePrompt(userInput);
		outputToServer.println(userInput);
		System.out.println(PROMPT_MSG + inputFromServer.readLine());
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

	private String replacePrompt(String userInput) {
		if (userInput != null && userInput.startsWith(PROMPT_MSG)) {
			userInput = userInput.replace(PROMPT_MSG, "");
		}
		return userInput;
	}

	private void putFileInRemoteServer(String userInput, PrintWriter outputToServer) throws IOException {
		userInput = replacePrompt(userInput);
		outputToServer.print(userInput);
		userInput = cleanString(userInput);
		String fileName = getFileName(userInput);
		FileInputStream fis = new FileInputStream(fileName);
		int fileSize = fis.available();
		outputToServer.println("fileSize:" + fileSize);
		byte[] buffer = new byte[4096];
		DataOutputStream dos = new DataOutputStream(myftpSocket.getOutputStream());
		while (fis.read(buffer) > 0) {
			dos.write(buffer);
		}

		fis.close();
		dos.close();

	}

	private String cleanString(String userInput) {
		if (userInput == null) {
			return "";
		}
		userInput = replacePrompt(userInput);
		if (userInput
				.matches("^(get|put)\\s*([a-zA-Z]:)?(" + fileSeparator + "?[a-zA-Z0-9_.-]+)+" + fileSeparator + "?")) {
			return userInput.replaceAll("get|put", "");
		}
		return userInput;
	}

	private void getFileAndSaveCurrentDirectory(String userInput, PrintWriter outputToServer,
			BufferedReader inputFromServer) throws IOException {
		System.out.println("getFileAndSaveCurrentDirectory()");
		userInput = replacePrompt(userInput);
		outputToServer.println(userInput);
		userInput = cleanString(userInput);
		String fileName = getFileName(userInput);
		String fileSizeString = null;

		int filesize = 0;
		while (true) {
			fileSizeString = inputFromServer.readLine();
			if (fileSizeString != null && fileSizeString.contains("fileSize:")) {
				filesize = getFileSize(fileSizeString);
				break;
			}
		}
		DataInputStream dis = new DataInputStream(myftpSocket.getInputStream());
		FileOutputStream fos = new FileOutputStream(fileName);
		byte[] buffer = new byte[4096];
		int read = 0;
		int totalRead = 0;
		int remaining = filesize;
		while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
			totalRead += read;
			remaining -= read;
			System.out.println("read " + totalRead + " bytes.");
			fos.write(buffer, 0, read);
		}

		fos.close();
		dis.close();
		System.out.println("getFileAndSaveCurrentDirectory() exit");
		String output = null;
		while (true) {
			System.out.println("Still waiting for server reply about request status. Response = " + output);
			output = inputFromServer.readLine();
			if (output != null) {
				break;
			}
		}
		System.out.println(PROMPT_MSG + output);

	}

	private int getFileSize(String fileSizeString) {
		if (fileSizeString != null && fileSizeString.contains("fileSize:")) {
			return Integer.parseInt(fileSizeString.replace("fileSize:", ""));
		}
		return 0;
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		if (args.length != 2) {
			System.err.println("Usage: java myftp <host name> <port number>");
			System.exit(1);
		}
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);
		try {
			Socket myftpSocket = new Socket(hostName, portNumber);
			FTPClient ftpClient = new FTPClient(myftpSocket);
			ftpClient.processRequest();
		} catch (Exception e) {
			System.err.println("Something went wrong while processing your request. Hostname: " + hostName + " error: "
					+ e.getMessage());
			System.exit(1);
		}
	}
}
