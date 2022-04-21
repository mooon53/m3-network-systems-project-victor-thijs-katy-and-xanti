package control;

import java.util.Random;

import static control.MyProtocol.DEBUGGING_MODE;

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
        if (DEBUGGING_MODE) System.out.println("packet retransmitter started");
    }

    /**
     * Resends if not everyone in range has acknowledged the packet.
     */
    public void run() {
        Random random = new Random();
        // TODO: check if everyone in range has received the packet this transmitter was made for
        boolean received = false;
        try {
            while (!received) {
                if (DEBUGGING_MODE) System.out.println("Not everyone received, starting sleep");
                int randomTime = 1000 * random.nextInt(5);
                Thread.sleep(5000 + randomTime); // Wait a random time 5-10  seconds
                if (!packetStorage.checkReceivers(nodeID, seqNum, fragNum) && attempts < 5) {
                    if (DEBUGGING_MODE) System.out.println("sleep over, resending");
                    packetStorage.resendPacket(nodeID, seqNum, fragNum);
                    attempts++;
                } else received = true;
            }
            if (DEBUGGING_MODE) System.out.println("everyone has received this packet");
//            Thread.sleep(60000);
//            packetStorage.removePacket(nodeID, seqNum, fragNum);
        } catch (InterruptedException e) { }
    }
}
