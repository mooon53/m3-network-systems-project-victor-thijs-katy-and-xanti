package control;

public class Fragment {
	int sourceID;
	int fragID;
	String messagePart;

	public Fragment(int sourceID, int fragID, String message) {
		this.sourceID = sourceID;
		this.fragID = fragID;
		this.messagePart = message;
	}

	public int getSourceID() {
		return sourceID;
	}

	public int getFragID() {
		return fragID;
	}

	public String getMessagePart() {
		return messagePart;
	}
}
