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
import java.util.concurrent.locks.*;

public class myftp {
    private static final String PROMPT_MSG = "mytftp>";
    private static final String fileSeparator = FileSystems.getDefault().getSeparator();
    private int terminatePort = 0;
    private String hostname = "";
    private int nport;
    public static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public myftp() {
    }
    
    synchronized private void sendFile(String fileName, DataOutputStream sStream) {
        BufferedInputStream bis = null;
        byte[] fileArray = null;
        lock.writeLock().lock();
        try {
            
            File fe = new File(fileName);
            if (fe.canRead()) {
                bis = new BufferedInputStream(new FileInputStream(fe));
                fileArray = new byte[(int) fe.length()];
                bis.read(fileArray);
            } else
            {
                System.err.println("File: " + fe.getName() + " cannot be read");
                bis.close();
                return;
            }
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
        finally{
            lock.writeLock().unlock();
        }
        
    }
    
    private void receiveFile(String fileName, DataInputStream cStream) {
        lock.readLock().lock();
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
        finally{
            lock.readLock().unlock();
        }
        
    }
    
    public void createSocket(String hostName, int nport, int tport) {
        
        this.terminatePort = tport;
        this.hostname = hostName;
        this.nport = nport;
        // variables
        String userInput, serverInput;
        File fe;
        
        // Resourse Statements closes all of these objects after the program
        // closes
        try (Socket myftpSocket = new Socket(hostName, nport);
             DataInputStream inputDServer = new DataInputStream(myftpSocket.getInputStream());
             DataOutputStream outputToServer = new DataOutputStream(myftpSocket.getOutputStream());
             BufferedReader inputFromServer = new BufferedReader(
                                                                 new InputStreamReader(myftpSocket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(myftpSocket.getOutputStream()));
             BufferedReader stdInput = new BufferedReader(new InputStreamReader(System.in));) {
            System.out.print(PROMPT_MSG);
            while (true) {
                userInput = stdInput.readLine();
                if (userInput == null || "".equals(userInput)) {
                    continue;
                }
                userInput = replacePrompt(userInput);
                boolean multipleCommand = false;
                if (userInput.contains(" & ")) {
                    multipleCommand = true;
                }
                if (multipleCommand) {
                    executeMultipleCommand(userInput);
                } else {
                    executeSingleCommand(userInput, inputDServer, outputToServer, inputFromServer, writer);
                }
                
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
    
    private void executeMultipleCommand(String userInput) {
        String[] commands = userInput.split("&");
        for (String cmd : commands) {
            new Thread(() -> {
                try {
                    Socket myftpSocket = new Socket(this.hostname, this.nport);
                    DataInputStream inputDServer = new DataInputStream(myftpSocket.getInputStream());
                    DataOutputStream outputToServer = new DataOutputStream(myftpSocket.getOutputStream());
                    BufferedReader inputFromServer = new BufferedReader(
                                                                        new InputStreamReader(myftpSocket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(myftpSocket.getOutputStream()));
                    executeSingleCommand(cmd, inputDServer, outputToServer, inputFromServer, writer);
                } catch (IOException e) {
                    System.out.println("Exception caught while executing multiple command in executeMultipleCommand()");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }).start();
            ;
        }
        
    }
    
    private void executeSingleCommand(String userInput, DataInputStream inputDServer, DataOutputStream outputToServer,
                                      BufferedReader inputFromServer, BufferedWriter writer) throws IOException {
        outputToServer.writeUTF(userInput);
        outputToServer.flush();
        String[] commands;
        commands = userInput.split(" ");

        if (commands[0].equals("get")) {
            System.out.println(inputFromServer.readLine());
            receiveFile(commands[1], inputDServer);
            System.out.print(PROMPT_MSG);
        } else if (commands[0].equals("put")) {
            
            System.out.println(inputFromServer.readLine());
            sendFile(commands[1], outputToServer);
            System.out.print(PROMPT_MSG);
            
        } else if (commands[0].equals("quit")) {
            System.err.println("Socket Closed");
            System.exit(0);
        } else if (userInput.contains("ls")) {
            lsFileInRemoteServerDirectory(userInput, writer, inputFromServer);
        } else if (userInput.contains("cd")) {
            cdRemoteServerDirectory(userInput, writer, inputFromServer);
        } else if (userInput.contains("mkdir")) {
            mkdirRemoteServerDirectory(userInput, writer, inputFromServer);
        } else if (userInput.equals("pwd")) {
            System.out.println(inputFromServer.readLine());
            System.out.print(PROMPT_MSG);
        } else if (commands[0].equals("delete")) {
            System.out.println(inputFromServer.readLine());
            System.out.print(PROMPT_MSG);
        } else if (commands[0].equals("terminate")) {
            executeTerminateCommand(commands[1]);
            System.out.println(inputFromServer.readLine());
            System.out.print(PROMPT_MSG);
        }
    }
    
    private void executeTerminateCommand(String command_ID) {
        Socket myftpSocket;
        try {
            myftpSocket = new Socket(this.hostname, this.terminatePort);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(myftpSocket.getOutputStream()));
            writer.write(command_ID);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error executing terminate command " + command_ID + " .Error-" + e.getMessage());
        }
        
    }
    
    private void lsFileInRemoteServerDirectory(String userInput, BufferedWriter writer, BufferedReader inputFromServer)
    throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        try {
            userInput = inputFromServer.readLine();
            while (userInput != null) {
                System.out.println(userInput);
                if (inputFromServer.ready()) {
                    userInput = inputFromServer.readLine();
                } else {
                    break;
                }
                
            }
        } catch (Exception e) {
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
    
    private void mkdirRemoteServerDirectory(String userInput, BufferedWriter writer, BufferedReader inputFromServer)
    throws IOException {
        waitForServerResponse(inputFromServer);
        System.out.print(PROMPT_MSG);
        
    }
    
    private String replacePrompt(String userInput) {
        if (userInput != null && userInput.startsWith(PROMPT_MSG)) {
            userInput = userInput.replace(PROMPT_MSG, "");
        }
        return userInput;
    }
    
    public static void main(String args[]) {
        // Making sure correct input is given by the user
        if (args.length != 3) {
            System.err.println("Usage: java myftp <host name> normal port<nport>  and terminate port<tport>");
            System.exit(1);
        }
        
        myftp mfp = new myftp();
        mfp.createSocket(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }
}
