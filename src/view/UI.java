package view;

import control.Fragment;
import control.Message;
import control.MyProtocol;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * User interface class.
 */
public class UI {
    /**
     * Prints a message.
     *
     * @param message Message object which needs to be printed
     */
    public static void printMessage(Message message) {
        if (MyProtocol.DEBUGGING_MODE) {
            System.out.println();
        }
        System.out.println("[" + message.getSourceID() + "]: " + message);
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
        System.out.print("> ");
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