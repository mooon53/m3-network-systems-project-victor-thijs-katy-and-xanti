package view;

import model.objects.Fragment;
import model.objects.Message;
import control.MyProtocol;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * User interface class.
 */
public class UI {
    public static final String ANSI_PURPLE = "\033[0;35m";
    public static final String ANSI_GREEN = "\033[0;32m";
    public static final String ANSI_BLUE = "\033[0;34m";
    public static final String ANSI_YELLOW = "\033[0;33m";
    public static final String ANSI_RESET = "\u001B[0m";
    static String[] colours = new String[]{ANSI_PURPLE, ANSI_GREEN, ANSI_BLUE, ANSI_YELLOW};

    /**
     * Prints a message.
     *
     * @param message Message object which needs to be printed
     */
    public static void printMessage(Message message) {
        if (MyProtocol.DEBUGGING_MODE) {
            System.out.println();
        }
        String colour = colours[message.getSourceID()];
        System.out.println(colour + "[" + message.getSourceID() + "]: " + message + ANSI_RESET);
    }

    public static void printFragment(Fragment fragment) {
        Message message = new Message(fragment);
        printMessage(message);
    }

    /**
     * Asks for an input from the user.
     *
     * @param prompt prompt which is displayed to the user
     * @return the input from the user
     * @throws IOException throws IOException if the BufferedReader throws an IOException
     */
    //add \n to the prompt if you want a newline
    public String getInput(BufferedReader br, String prompt) throws IOException {
        if (!prompt.equals("")) {
            System.out.println(prompt);
        }
        return br.readLine();
    }

    /**
     * Asks for an input from the user without a prompt.
     *
     * @return the input from the user
     * @throws IOException throws IOException if the BufferedReader throws an IOException
     */
    public String getInput(BufferedReader br) throws IOException {
        return getInput(br, "");
    }
}