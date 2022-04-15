package control;

public class Message {
	private String message;
	private int sourceID;

	public Message(int source, String message) {
		this.sourceID = source;
		this.message = message;
	}

	public String toString() {
		return "["+sourceID+"]: "+message;
	}

	public int getSourceID() {
		return sourceID;
	}

	public String getMessage() {
		return message;
	}
}
