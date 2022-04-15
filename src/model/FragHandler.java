package model;

import control.Fragment;
import control.Message;
import view.UI;

import java.util.HashMap;

import static java.util.Collections.max;

public class FragHandler implements Runnable{
	private int sourceID;
	private int seqNum;
	private HashMap<Integer, String> fragments;
	private boolean complete;

	public FragHandler(int sourceID, int seqNum, int fragID, String fragment) {
		this.sourceID = sourceID;
		this.seqNum = seqNum;
		fragments = new HashMap<>();
		fragments.put(fragID, fragment);
	}

	public FragHandler(Fragment fragment) {
		new FragHandler(fragment.getSourceID(), fragment.getSeqNum(), fragment.getFragID(), fragment.getMessagePart());
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

	public void addFragment(int fragID, String fragment) {
		fragments.put(fragID, fragment);
	}

	public void addFragment(Fragment fragment) {
		addFragment(fragment.getFragID(), fragment.getMessagePart());
	}

	@SuppressWarnings("InvisibleCharacter")
	public boolean isComplete() {
		int highestID = max(fragments.keySet());
		String lastFragment = fragments.get(highestID);
		return lastFragment.endsWith("") && highestID+1 == fragments.size(); // DON'T REMOVE "", it is a special character we actually use
	}
}
