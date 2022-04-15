package model;

import control.Fragment;
import control.Message;
import view.UI;

import java.util.HashMap;

import static java.util.Collections.max;

public class FragHandler implements Runnable{
	private int sourceID;
	private HashMap<Integer, String> fragments;
	private boolean complete;

	public FragHandler(int sourceID, int fragID, String fragment) {
		this.sourceID = sourceID;
		fragments = new HashMap<>();
		fragments.put(fragID, fragment);
	}

	public FragHandler(Fragment fragment) {
		new FragHandler(fragment.getSourceID(), fragment.getFragID(), fragment.getMessagePart());
	}

	public void run() {
		while (!isComplete()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO: ask a TA what to put here
			}
		}
		StringBuilder message = new StringBuilder();
		for (int i = 0; i < fragments.size(); i++) {
			String fragment = fragments.get(i);
			message.append(fragment);
		}
		Message fullMessage = new Message(sourceID, message.toString());
		UI.printMessage(fullMessage);
	}

	public void addMessage(int fragID, String fragment, boolean last) {
		fragments.put(fragID, fragment);
		if (last) complete = true;
	}

	@SuppressWarnings("InvisibleCharacter")
	public boolean isComplete() {
		int highestID = max(fragments.keySet());
		String lastFragment = fragments.get(highestID);
		return lastFragment.endsWith("") && highestID+1 == fragments.size(); // DON'T REMOVE "", it is a special character we actually use
	}
}
