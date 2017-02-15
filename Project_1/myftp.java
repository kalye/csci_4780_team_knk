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

public class myftp {

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
        
        try (
            Socket myftpSocket = new Socket(hostName, portNumber);
            BufferedInputStream isFromServer = new BufferedInputStream(myftpSocket.getInputStream());
            BufferedOutputStream osBuffToServer = new BufferedOutputStream(myftpSocket.getOutputStream());
            PrintWriter outputToServer =
                new PrintWriter(myftpSocket.getOutputStream(), true);
            BufferedReader inputFromServer =
                new BufferedReader(
                    new InputStreamReader(myftpSocket.getInputStream()));
            BufferedReader stdInput =
                new BufferedReader(
                    new InputStreamReader(System.in))
        ) {
            
            while (true) {
                userInput = stdInput.readLine(); 
                outputToServer.println(userInput);
                commands = userInput.split(" ");
                if(commands[0].equals("get"))
                {
                    int filesize = 1024*1000;
                    byte[] fileArray = new byte[filesize];
                    try{
                        isFromServer.read(fileArray);
                        byteArrayToFile(commands[1],fileArray);
                        System.out.println("File: " + commands[1] + " saved to client successfully");

                    }
                    catch(IOException e)
                    {
                        System.err.println("There was an I/O error");
                        System.err.println(e);
                    }
                    //System.out.println("Success");
                }
                else if(commands[0].equals("put"))
                {
                    osBuffToServer.write(convertFileToByteArray(commands[1]));

                }
                else if(commands[0].equals("quit"))
                {
                    System.err.println("Socket Closed");
                    System.exit(0);
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
