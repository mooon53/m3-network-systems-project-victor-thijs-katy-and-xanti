package control;

public class PacketRetransmitter implements Runnable {
	private PacketStorage packetStorage;
	private int nodeID;
	private int seqNum;
	private int fragNum;
	private int attempts;
	private boolean succes;

	public PacketRetransmitter(int ID, int sequence, int fragment, PacketStorage packetStore) {
		this.packetStorage = packetStore;
		this.nodeID = ID;
		this.seqNum = sequence;
		this.fragNum = fragment;
		this.attempts = 0;
		this.succes = false;
		System.out.println("packet retransmitter started");
	}

	public void run() {
		while (!checkReceivers() && attempts < 10) { // TODO: check if everyone in range has received the packet this transmitter was made for
			try {
				Thread.sleep(5000); // Wait 5 seconds
			} catch (InterruptedException e) {

			}
			System.out.println("sleep over, resending");
			packetStorage.resendPacket(nodeID, seqNum, fragNum);
			attempts++;
		}
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {

		}
		packetStorage.removePacket(nodeID, seqNum, fragNum);
	}

	public boolean checkReceivers() {
		int inRangeClients = 0;
		int receivedClients = 0;
		for (int i = 0; i < 4; i++) {
			if (Client.inRange[i]) {
				inRangeClients++;
				if (packetStorage.hasPacket(nodeID, seqNum, fragNum)) {
					if (packetStorage.hasReceived(i, nodeID, seqNum, fragNum)) receivedClients++;
				}
			}
		}
		return inRangeClients == receivedClients;
	}
}
