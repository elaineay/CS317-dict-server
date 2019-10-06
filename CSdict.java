
// You can use this file as a starting point for your dictionary client
// The file contains the code for command line parsing and it also
// illustrates how to read and partially parse the input typed by the user.
// Although your main class has to be in this file, there is no requirement that you
// use this template or hav all or your classes in this file.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

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
    private static PrintWriter out;
    private static BufferedReader in;
    private static BufferedReader stdIn;
    private static String myDict = "*";

    public static void main(String [] args) {
        
        int len;
        // Verify command line arguments
        if (args.length == PERMITTED_ARGUMENT_COUNT) {
            debugOn = args[0].equals("[-d]");
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



            } catch (IOException exception) {
                System.err.println("998 Input error while reading commands, terminating.");
                System.exit(-1);
            }
            try{
                switch(command) {
                    case "open":
                        try {
                            if (arguments.length != 2) {
                                System.err.println("901 Incorrect number of arguments.");
                                break;
                            }
                            openCommand(arguments[0], Integer.parseInt(arguments[1]));
                            //1 or more arguments invalid ie. second arg non-numeric value
                        } catch (NumberFormatException exception) {
                            System.err.println("902 Invalid argument.");
                        }
                        break;
                    case "dict":
                        dictCommand();
                        break;
                    case "set":
                        if (arguments.length != 1) {
                            System.err.println("901 Incorrect number of arguments.");
                            break;
                        }
                        if (socket == null) {
                            System.err.println("903 Supplied command not expected at this time.");
                            break;
                        }
                        myDict = arguments[0];
                        System.out.println("Database set to: " + myDict);
                        break;
                    case "define":
                        if (arguments.length != 1) {
                            System.err.println("901 Incorrect number of arguments.");
                            break;
                        }
                        defineCommand(arguments[0], myDict);
                        break;
                    case "match":
                        if (arguments.length != 1) {
                            System.err.println("901 Incorrect number of arguments.");
                            break;
                        }
                        matchCommand(arguments[0], myDict, "exact");
                        break;
                    case "prefixmatch":
                        if (arguments.length != 1) {
                            System.err.println("901 Incorrect number of arguments.");
                            break;
                        }
                        matchCommand(arguments[0], myDict, "prefix");
                        break;
                    case "close":
                        closeCommand();
                        break;
                    case "quit":
                        quitCommand();
                        break;
                    case "":
                        break;
                    default:
                        System.err.println("900 Invalid command.");
                }

            } catch (Exception exception) {
                System.err.println("925 Control connection I/O error, closing control connection.");
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


    /*
     *	TODO: Retrieve and print the list of all the dictionaries
     *	the server supports. Each line will consist of a single
     *	word that is the the name of a dictionary followed by
     *	some information about the dictionary. You simply have
     *	to print each of these lines as returned by the server.
    */
    private static void dictCommand() {
        if(socket == null || socket.isClosed()) {
            System.err.println("903 Supplied command not expected at this time.");
            return;
        }
        try {
            out.println("Show DB");
            if (debugOn) {
                System.out.println("> DICT");
            }
            String dictList;
            while(true) {
                dictList = in.readLine();
                if (debugOn & containsStatusMessage(dictList)) {
                    System.out.println("<-- " + dictList);
                }
                if (dictList.contains("250 ok")) break;
                if (dictList.contains("530")) {
                    System.err.println("999 Processing error. Access Denied.");
                    break;
                }
                if (!debugOn & !containsStatusMessage(dictList)) {
                    System.out.println(dictList);
                }
            }
        } catch (Exception exception){
            System.err.println("999 Processing error. \"Dict\" failed to be called");
            System.exit(-1);
        }
    }

    /*
     * TODO: Check the link man. I give up.
    */
    private static void defineCommand(String word, String dictName) {
        Integer STATUS_LENGTH = 6;
        if (socket == null || socket.isClosed()) {
            System.err.println("903 Supplied command not expected at this time.");
            return;
        }
        try {
            out.println("DEFINE " + dictName + " " + word);
            if (debugOn) {
                System.out.println("> DEFINE " + dictName + " " + word);
            }
            String defList;
            while (true) {
                defList = in.readLine();
                // Print <-- on server response with debug on
                if (debugOn & containsStatusMessage(defList)) {
                    System.out.println("<-- " + defList);
                }
                // break early if no match found
                if (defList.contains("552 no match")) {
                    System.out.println("***No definition found***");
                    matchCommand(word, dictName, ".");
                    break;
                } else if (defList.contains("550 invalid database")) {
                    System.out.println("999 Processing error. Invalid database.");
                    myDict = "*";
                    System.out.println("Database reset to: " + myDict);
                    break;
                } else if (defList.contains("530")) {
                    System.err.println("999 Processing error. Access Denied.");
                    break;
                // break if 250 ok
                } else if (defList.contains("250 ok")) {
                    break;
                }
                // Print @ if 151 is found
                if (!debugOn & defList.toLowerCase().contains("151 " + "\"" + word.toLowerCase() + "\"")) {
                    System.out.println("@" + defList.substring(STATUS_LENGTH + word.length()));
                } else if (!containsStatusMessage(defList)){
                    System.out.println(defList);
                }
            }
        } catch (Exception exception) {
            System.err.println(exception);
            // System.err.println("999 Processing error.\"Define\" failed to be called");
            System.exit(-1);
          }
      }

    /*
     * TODO: Check the link man. I give up.
    */
    private static void matchCommand(String word, String dictName, String strategy) {
        if(socket == null || socket.isClosed()) {
            System.err.println("903 Supplied command not expected at this time.");
            return;
        }
        try {
            out.println("MATCH " + dictName + " " + strategy + " " + word);
            if (debugOn) {
                System.out.println("> MATCH " + dictName + " " + strategy + " " + word);
            }
            String matchList;
            while (true) {
                matchList = in.readLine();
                if (debugOn & containsStatusMessage(matchList)) {
                    System.out.println("<-- " + matchList);
                }
                if (matchList.contains("552 no match") && strategy.equals(".")) {
                    System.out.println("****No matches found****");
                    break;
                } else if (matchList.contains("552 no match") && strategy.equals("exact")) {
                    System.out.println("*****No matching word(s) found*****");
                    break;
                } else if (matchList.contains("552 no match") && strategy.equals("prefix")) {
                    System.out.println("***No matching word(s) found****");
                    break;
                } else if (matchList.contains("550 invalid database")) {
                    System.out.println("999 Processing error. Invalid database.");
                    myDict = "*";
                    System.out.println("Database reset to: " + myDict);
                    break;
                } else if (matchList.contains("530")) {
                    System.err.println("999 Processing error. Access Denied.");
                    break;
                } else if (matchList.contains("250 ok")) {
                    break;
                }
                if (!containsStatusMessage(matchList)) {
                    System.out.println(matchList);
                }
                if (matchList.contains("250 ok")) break;
            }
        } catch (Exception exception) {
            System.err.println("999 Processing error. \"Match\" failed to be called");
            System.exit(-1);
        }
    }

    /*
     * TODO: closeCommand closes the connection and is in a state waiting to open or quit
    */
    private static void closeCommand() {
	 try {
	 	if (socket.isClosed() || socket == null) {
            System.err.println("903 Supplied command not expected at this time");
	 		return;
	 	}
        out.println("QUIT");
         if (debugOn) {
             System.out.println("> CLOSE " + socket);
             System.out.println("<-- " + in.readLine());
         }
         socket.close();
	 } catch (IOException exception) {
         System.out.println("999 Processing error. This shouldn't have happened.");
     }
    }

    /*
     * need to do close before it can quit
    */
    private static void quitCommand() {
            System.exit(0);
    }

    private static void openCommand(String hostName, int portNumber) {
        if (socket != null && socket.isConnected()) {
            System.err.println("903 Supplied command not expected at this time.");
        }
        try {
            if (debugOn) {
                System.out.println("> OPEN " + hostName + " " + portNumber);
            }
            socket = new Socket(hostName, portNumber);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
            stdIn = new BufferedReader(
                            new InputStreamReader(System.in));
            if (debugOn) {
                System.out.println("<-- " + in.readLine());
            }

        }
        catch (IOException exception) {
            System.err.println("920 Control connection to " + hostName + " on port " + portNumber + " failed to open.");
        }
    }

    /*
     * Given a line from the server, returns true if it is a status message
     * false otherwise.
    */
    private static boolean containsStatusMessage(String line) {
        try {
            return line.length() > 3 & line.substring(0,2).matches("[0-9]+");
        } catch (StringIndexOutOfBoundsException exception) {
            return false;
        }
        
    }
}
