package view;

import control.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UI {
	public static void printMessage(Message message) {
		System.out.println(message);
	}

	public String getInput(String prompt) throws IOException { //Javadoc: add \n to the prompt if you want a newline
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print(prompt);
		System.out.print("> ");
		return br.readLine();
	}

	public String getInput() throws IOException {
		return getInput("");
	}
}