package control;

public class Fragment {
	int sourceID;
	int seqNum;
	int fragID;
	String messagePart;

	public Fragment(int sourceID, int seqNum, int fragID, String message) {
		this.sourceID = sourceID;
		this.seqNum = seqNum;
		this.fragID = fragID;
		this.messagePart = message;
	}

	public int getSourceID() {
		return sourceID;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public int getFragID() {
		return fragID;
	}

	public String getMessagePart() {
		return messagePart;
	}
}
