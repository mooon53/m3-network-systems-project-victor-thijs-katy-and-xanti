package view;

import control.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        System.out.println("[" + message.getSourceID() + "]: " + message);
    }

    /**
     * Asks for an input from the user.
     *
     * @param prompt prompt which is displayed to the user
     * @return the input from the user
     * @throws IOException throws IOException if the BufferedReader throws an IOException
     */
    //add \n to the prompt if you want a newline
    public String getInput(String prompt) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(prompt);
        System.out.print("> ");
        return br.readLine();
    }

    /**
     * Asks for an input from the user without a prompt.
     *
     * @return the input from the user
     * @throws IOException throws IOException if the BufferedReader throws an IOException
     */
    public String getInput() throws IOException {
        return getInput("");
    }
}