package control;

public class PacketRetransmitter implements Runnable {
    private PacketStorage packetStorage;
    private int nodeID;
    private int seqNum;
    private int fragNum;
    private int attempts;
    private boolean succes;

    /**
     * Constructor for a packet retransmitter.
     *
     * @param id          id of the packet
     * @param sequence    sequence number of the packet
     * @param fragment    fragmentation number of the packet
     * @param packetStore the packet storage in which the packet should be stored
     */
    public PacketRetransmitter(int id, int sequence, int fragment, PacketStorage packetStore) {
        this.packetStorage = packetStore;
        this.nodeID = id;
        this.seqNum = sequence;
        this.fragNum = fragment;
        this.attempts = 0;
        this.succes = false;
        System.out.println("packet retransmitter started");
    }

    /**
     * Resends if not everyone in range has acknowledged the packet.
     */
    public void run() {
        // TODO: check if everyone in range has received the packet this transmitter was made for
        while (!checkReceivers() && attempts < 10) {
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

    /**
     * Checks whether everyone in range has sent an acknowledgement.
     *
     * @return true if everyone in range has sent an acknowledgement
     */
    public boolean checkReceivers() {
        int inRangeClients = 0;
        int receivedClients = 0;
        for (int i = 0; i < 4; i++) {
            if (Client.inRange[i]) {
                inRangeClients++;
                if (packetStorage.hasPacket(nodeID, seqNum, fragNum)) {
                    if (packetStorage.hasReceived(i, nodeID, seqNum, fragNum)) {
                        receivedClients++;
                    }
                }
            }
        }
        return inRangeClients == receivedClients;
    }
}
