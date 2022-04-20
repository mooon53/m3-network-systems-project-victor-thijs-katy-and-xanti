package control;

import model.FragHandler;
import view.UI;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

import static control.Client.*;
import static control.PacketType.DATA;
import static utils.HelpFunc.*;
import static control.MyProtocol.*;

/**
 * Class that stores all the packets we have received,
 * removing them once everyone in our range has seen them.
 * Also decides whether to retransmit etc.
 */
public class PacketStorage {
    // A map that maps the Packet to a combination of the sequence number and fragmentation number
    // For example, sequence 15 and fragmentation 4 would be 01111 00100 = 484
    // An array so we can store the packet from each sender in their own map
    private HashMap[] packets;
    private HashMap[] fragments;
    private HashMap[] received;

    private Client client;

    /**
     * Constructor for the packet storage.
     *
     * @param client client for whom the packet storage is
     */
    public PacketStorage(Client client) {
        this.client = client;
        packets = new HashMap[4];
        fragments = new HashMap[4];
        received = new HashMap[4];
        for (int i = 0; i < 4; i++) {
            packets[i] = new HashMap<Long, Packet>();
            fragments[i] = new HashMap<Long, Fragment>();
            received[i] = new HashMap<Long, boolean[]>();
        }
    }

    /**
     * Adds a packet to the storage.
     *
     * @param fragment fragment which needs to be stored
     * @param packet   packet which needs to be stored
     */
    public void addPacket(Fragment fragment, Packet packet) {
        boolean dm = fragment.getHeader().getDm();
        Header header = fragment.getHeader();
        System.out.println("Packet added to packet storage seq "
                + header.getSeqNum() + " and frag " + header.getFragNum());
        long id = id(header.getSeqNum(), header.getFragNum());
        int hashMapID = header.getSource();
        HashMap<Long, Fragment> fragmentsMap = fragments[hashMapID];
        HashMap<Long, Packet> packetMap = packets[hashMapID];
        if (!fragmentsMap.containsKey(id)) {
            packetMap.put(id, packet);
            fragmentsMap.put(id, fragment);
            boolean[] seen = new boolean[4];
            seen[MyProtocol.getNodeID()] = true;
            seen[header.getSource()] = true;
            seen[header.getNxtHop()] = true;
            received[header.getSource()].put(id, seen);
            if (!dm || dm && header.getDest() == MyProtocol.getNodeID()) {
                if (header.getFrag()) {
                    FragHandler fragHandler;
                    if (fragHandlerExists(header.getSeqNum())) {
                        fragHandler = getFragHandler(header.getSeqNum());
                        System.out.println(fragHandler);
                        fragHandler.addFragment(fragment);
                    } else {
                        fragHandler = new FragHandler(fragment);
                        System.out.println(fragHandler);
                        addFragHandler(header.getSeqNum(), fragHandler);
                        Thread fragHandlerThread = new Thread(fragHandler, "Fragmentation handler "
                                + header.getSeqNum());
                        fragHandlerThread.start();
                    }
                } else if (header.getSource() != client.getNodeID()) {
                    UI.printFragment(fragment);
                }
            }
            if (header.getSource() != client.getNodeID()) {
                header.setAck();
            }
            header.setNxtHop(client.getNodeID());
            byte[] headerBytes = header.toByteArray();
            byte[] dataBytes = packet.getData().array();
            byte[] newDataBytes = new byte[dataBytes.length - Header.HEADER_LENGTH];
            System.arraycopy(dataBytes, 3, newDataBytes, 0, header.getDataLen());
            ByteBuffer byteBuffer = ByteBuffer.allocate(headerBytes.length + dataBytes.length)
                    .put(headerBytes).put(newDataBytes);
            Packet newPacket = new Packet(DATA, byteBuffer);
            sendMessage(newPacket);
        } else {
            HashMap map = received[header.getSource()];
            boolean[] bools = (boolean[]) map.get(id(header.getSeqNum(), header.getFragNum()));
            bools[header.getNxtHop()] = true;
            received[header.getSource()].put(id, bools);
            System.out.println(Arrays.toString(bools));
            if (!checkReceivers(header.getSource(), header.getSeqNum(), header.getFragNum())) resendPacket(header.getSource(), header.getSeqNum(), header.getFragNum());
        }


        PacketRetransmitter remover = new PacketRetransmitter(header.getSource(),
                header.getSeqNum(), header.getFragNum(), this);
        Thread packetRemoveThread = new Thread(remover, "PacketRemover " + header.getSource() + " "
                + header.getSeqNum() + " " + header.getFragNum());
        packetRemoveThread.start();
    }

    /**
     * Resends a packet.
     *
     * @param nodeID  node ID of the sender
     * @param seqNum  sequence number of the packet
     * @param fragNum fragmentation number of the packet
     */
    public void resendPacket(int nodeID, int seqNum, int fragNum) {
        Fragment fragment = (Fragment) fragments[nodeID].get(id(seqNum, fragNum));
        Packet packet = (Packet) packets[nodeID].get(id(seqNum, fragNum));
        byte[] headerBytes = fragment.getHeader().toByteArray();
        byte[] dataBytes = packet.getData().array();
        byte[] newDataBytes = new byte[dataBytes.length - Header.HEADER_LENGTH];
        System.arraycopy(dataBytes, 3, newDataBytes, 0, fragment.getHeader().getDataLen());
        ByteBuffer byteBuffer = ByteBuffer.allocate(headerBytes.length + dataBytes.length)
                .put(headerBytes).put(dataBytes);
        Packet newPacket = new Packet(DATA, byteBuffer);
        sendMessage(newPacket);
    }

    /**
     * Removes a packet from the storage.
     *
     * @param nodeID  node ID of whom the packet is
     * @param seqNum  sequence number of the packet
     * @param fragNum fragmentation number of the packet
     */
    public void removePacket(int nodeID, int seqNum, int fragNum) {
        String sequence = padString(Integer.toBinaryString(seqNum), 5);
        String frag = padString(Integer.toBinaryString(fragNum), 5);
        long id = Long.valueOf(sequence + frag, 2);
        packets[nodeID].remove(id);
    }

    /**
     * Removes a packet with fragmentation number equal to 0.
     *
     * @param nodeID node ID of whom the packet is
     * @param seqNum sequence number of the packet
     */
    public void removePacket(int nodeID, int seqNum) {
        removePacket(nodeID, seqNum, 0);
    }

    /**
     * Checks if a certain packet is contained in the storage.
     *
     * @param nodeID  node ID of whom the packet is
     * @param seqNum  sequence number of the packet
     * @param fragNum fragmentation number of the packet
     * @return true if a certain packet is contained in the storage
     */
    public boolean hasPacket(int nodeID, int seqNum, int fragNum) {
        return fragments[nodeID].containsKey(id(seqNum, fragNum));
    }

    /**
     * Checks if a certain node has received a certain packet.
     *
     * @param receiverID ID of the node for which the receiving needs to be checked
     * @param nodeID     node ID of whom the packet is
     * @param seqNum     sequence number of the packet
     * @param fragNum    fragmentation number of the packet
     * @return true if a certain node has received a certain packet
     */
    public boolean hasReceived(int receiverID, int nodeID, int seqNum, int fragNum) {
        HashMap<Long, boolean[]> receivers = received[nodeID];
        boolean[] receivedByID = receivers.get(id(seqNum, fragNum));
        return receivedByID[receiverID];
    }

    /**
     * Makes a unique ID for a packet from the sequence number and fragmentation number.
     *
     * @param seqNum  sequence number of the packet
     * @param fragNum fragmentation number of the packet
     * @return the unique ID
     */
    public long id(int seqNum, int fragNum) {
        String sequence = padString(Integer.toBinaryString(seqNum), 5);
        String frag = padString(Integer.toBinaryString(fragNum), 5);
        long id = Long.valueOf(sequence + frag, 2);
        return id;
    }

    /**
     * Checks whether everyone in range has sent an acknowledgement.
     * @param sourceID the source of the packet you want to check
     * @param seqNum the sequence number of the packet you want to check
     * @param fragNum the fragment number of the packet you want to check
     * @return true if everyone in range has sent an acknowledgement
     */
    public boolean checkReceivers(int sourceID, int seqNum, int fragNum) {
        for (int i = 0; i < 4; i++) {
            if (Client.inRange[i]) {
                if (!hasReceived(i, sourceID, seqNum, fragNum)) return false;
            }
        }
        return true;
    }
}