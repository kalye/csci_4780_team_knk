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
    public static void main(String args[])
    {
        
        if (args.length != 2) {
            System.err.println("Usage: java myftp <host name> <port number>");
            System.exit(1);
        } 
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        
        try (
            Socket myftpSocket = new Socket(hostName, portNumber);
            PrintWriter outputToServer =
                new PrintWriter(myftpSocket.getOutputStream(), true);
            BufferedReader inputFromServer =
                new BufferedReader(
                    new InputStreamReader(myftpSocket.getInputStream()));
            BufferedReader stdInput =
                new BufferedReader(
                    new InputStreamReader(System.in))
        ) {
            String userInput;
            while ((userInput = stdInput.readLine()) != null) {
                outputToServer.println(userInput);
                System.out.println("echo: " + inputFromServer.readLine());
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
}
