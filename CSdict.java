
// You can use this file as a starting point for your dictionary client
// The file contains the code for command line parsing and it also
// illustrates how to read and partially parse the input typed by the user.
// Although your main class has to be in this file, there is no requirement that you
// use this template or hav all or your classes in this file.

import java.lang.System;
import java.io.IOException;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.Socket;

//
// This is an implementation of a simplified version of a command
// line dictionary client. The only argument the program takes is
// -d which turns on debugging output.
//


public class CSdict {
    static final int MAX_LEN = 255;
    static Boolean debugOn = false;

    private static final int PERMITTED_ARGUMENT_COUNT = 1;
    private static String command;
    private static String[] arguments;
    private static int SET_TIMEOUT_LIMIT = 30000;

    private static Socket socket;

    public static void main(String [] args) {
        
        int len;
        // Verify command line arguments

        if (args.length == PERMITTED_ARGUMENT_COUNT) {
            debugOn = args[0].equals("-d");
            if (debugOn) {
                System.out.println("Debugging output enabled");
            } else {
                System.out.println("997 Invalid command line option - Only -d is allowed");
                return;
            }
        } else if (args.length > PERMITTED_ARGUMENT_COUNT) {
            System.out.println("996 Too many command line options - Only -d is allowed");
            return;
        }

        // Infinite while loop to keep program running

        String userInput;
        while (true) {


            // Example code to read command line input and extract arguments.

            try {
                byte cmdString[] = new byte[MAX_LEN];
                System.out.print("csdict> ");
                System.in.read(cmdString);
                // Convert the command string to ASII
                String inputString = new String(cmdString, "ASCII");

                // Split the string into words
                String[] inputs = inputString.trim().split("( |\t)+");
                // Set the command
                command = inputs[0].toLowerCase().trim();
                // Remainder of the inputs is the arguments.
                arguments = Arrays.copyOfRange(inputs, 1, inputs.length);
                switch(command) {
                    case "open":
                        String hostname = "dict.org";
                        Integer portNumber = 2628;
                        // if (arguments == null || arguments.length != 2) {
                        //     System.err.println("901 Incorrect number of arguments.");
                        //     return;
                        // }
                        // openSocketConnection(arguments[0], Integer.parseInt(arguments[1]));

                        openSocketConnection(hostname, portNumber); // Hardcoded for easier testing
                        break;
                    case "dict":
                        dictCommand();
                        break;
                    case "set":
                        setCommand();
                        break;
                    case "define":
                        defineCommand();
                        break;
                    case "match":
                        matchCommand();
                        break;
                    case "prefixmatch":
                        prefixmatchCommand();
                        break;
                    case "close":
                        closeCommand();
                        break;
                    case "quit":
                        quitCommand();
                        break;
                    default:
                        System.err.println("900 Invalid command.");
                }

                // TODO: Delete this
                // System.out.println("The command is: " + command);
                // len = arguments.length;
                // System.out.println("The arguments are: ");
                // for (int i = 0; i < len; i++) {
                //     System.out.println("    " + arguments[i]);
                // }
                // System.out.println("Done.");

            } catch (IOException exception) {
                System.err.println("998 Input error while reading commands, terminating.");
                System.exit(-1);
            }
        }
    }

/*
 *	TODO: Opens a new TCP/IP connection to an dictionary server.
 *	The server's name and the port number the server is listening
 *	on are specified by the command's parameters. The server name
 *	can be either a domain name or an IP address in dotted form.
 *	Both the SERVER and PORT values must be provided. This command
 *	is considered an unexpected command if it is given when a control
 *	connection is already open.
*/

// LITERALLY THIS IS NOT REQUIRED
//private static void openCommand() {
////    System.out.println("open command selected!");
//	String hostname = "dict.org";
//	Integer portNumber = 2628;
//  // Socket socket = new Socket(hostName, portNumber);
//	if (socket.isConnected()) {
//		System.err.println("903 Supplied command not expected at this time.");
//	} else {
//		openSocketConnection(hostname, portNumber);
//	}
//}


    /*
     *	TODO: Retrieve and print the list of all the dictionaries
     *	the server supports. Each line will consist of a single
     *	word that is the the name of a dictionary followed by
     *	some information about the dictionary. You simply have
     *	to print each of these lines as returned by the server.
    */
    private static void dictCommand() {
        System.out.println("dictCommand() is called.");
    }

    /*
     * TODO: Check the link man. I give up.
    */
    private static void setCommand() {
        System.out.println("setCommand() is called.");
    }

    /*
     * TODO: Check the link man. I give up.
    */
    private static void defineCommand() {
        System.out.println("openCommand() is called.");
    }

    /*
     * TODO: Check the link man. I give up.
    */
    private static void matchCommand() {
        System.out.println("matchCommand() is called.");
    }

    /*
     * TODO: Check the link man. I give up.
    */
    private static void prefixmatchCommand() {
        System.out.println("prefixmatchCommand() is called.");
    }

    /*
     * TODO: closeCommand closes the connection and is in a state waiting to open or quit
    */
    private static void closeCommand() {
//	 try {
//	 	if (socket.isClosed() || socket == null) {
//	 		return;
//	 	}
//
////		 out.close();
////		 in.close();
//		 socket.close();
//	 } catch (IOException exception) {
//	 	System.err.println("999 Processing error. You done fucked up.");
//	 }
    }

    /*
     * TODO: What's the difference from closeCommand()
     * need to do close before it can quit
    */
    private static void quitCommand() {
        System.out.println("Good bye!");
	    System.exit(0);
    }

    private static void openSocketConnection(String hostName, int portNumber) {
        if (socket != null && socket.isConnected()) {
            System.err.println("903 Supplied command not expected at this time. ");
        }
        try {
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn =
                    new BufferedReader(
                            new InputStreamReader(System.in));
            System.out.println(in.readLine());
        }
        catch (IOException exception) {
            System.err.println("998 Input error while reading commands, terminating.");
            System.exit(-1);
        }
    }
}
