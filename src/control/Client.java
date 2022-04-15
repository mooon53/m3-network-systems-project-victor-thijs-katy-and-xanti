package control;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import static utils.HelpFunc.*;

import model.*;

/**
 * Client object responsible for listening and sending.
 */
public class Client {
    private SocketChannel sock;
    private Sender sender;
    private Listener listener;

    private BlockingQueue<Packet> receivedQueue;
    private BlockingQueue<Packet> sendingQueue;

    private static int nodeID = 3;

    private static HashMap<Integer, FragHandler> receivedMessages = new HashMap();

    /**
     * Gets a FragmentHandler by its sequence number.
     *
     * @param seqNum sequence number of wanted FragmentHandler
     * @return FragmentHandler corresponding to the sequence number
     */
    public static FragHandler getFragHandler(int seqNum) {
        return receivedMessages.get(seqNum);
    }

    /**
     * Checks if a FragmentHandler for a certain sequence number already exists.
     *
     * @param seqNum sequence number for which the existence needs to be checked
     * @return true if the FragHandler corresponding to the given sequence number exists
     */
    public static boolean fragHandlerExists(int seqNum) {
        return receivedMessages.containsKey(seqNum);
    }

    /**
     * Adds a FragmentHandler to the stored HashMap.
     *
     * @param seqNum      sequence number corresponding to the FragHandler which needs to be added
     * @param fragHandler FragHandler which needs to be added to the receivedMessages HashMap
     */
    public static void addFragHandler(int seqNum, FragHandler fragHandler) {
        receivedMessages.put(seqNum, fragHandler);
        // TODO: store the FragmentHandlers differently,
        //  since we only store by sequence number and this is not unique
    }

    /**
     * Resets the HashMap of all receivedMessages.
     */
    public static void resetReceivedMessages() {
        receivedMessages = new HashMap<>();
    }

    /**
     * Gets the ID of the node of this client.
     *
     * @return ID of the node of this client
     */
    public int getNodeID() {
        return nodeID;
    }

    /**
     * Sets the ID of the node of the client.
     *
     * @param nodeID the ID to which the node ID of this client should be set
     */
    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

//	public void printByteBuffer(ByteBuffer bytes, int bytesLength) {
//		System.out.print("DATA: ");
//		for (int i = 0; i < bytesLength; i++) {
//			System.out.print(bytes.get(i) + " ");
//		}
//		System.out.println();
//	}

    /**
     * Creates a header byte array with all the information correctly set.
     *
     * @param dest    destination node ID
     * @param syn     synchronization flag
     * @param ack     acknowledgement flag
     * @param frag    fragmentation flag
     * @param dm      direct message flag
     * @param seq     sequence number for the packet
     * @param dataLen length of the actual data (so excluding the header)
     * @param nxtHop  node ID from the node who actually sent the packet
     * @param fragNum fragmentation number to indicate which fragment this is
     * @return a byte array with all bits set to the corresponding input
     */
    public static byte[] createHeader(int dest, boolean syn, boolean ack, boolean frag, boolean dm,
                                      int seq, int dataLen, int nxtHop, int fragNum) {
        byte[] output = new byte[3];
        String firstByte = createFirstHeaderByte(dest, syn, ack, frag, dm);
        output[0] = stringToByte(firstByte);
        String secondByte = createSecondHeaderByte(seq, dataLen);
        output[1] = stringToByte(secondByte);
        String thirdByte = createThirdHeaderByte(dataLen, nxtHop, fragNum);
        output[2] = stringToByte(thirdByte);
        return output;
    }

    /**
     * Creates a string of bits for the first byte of the header.
     *
     * @param dest destination node ID
     * @param syn  synchronization flag
     * @param ack  acknowledgement flag
     * @param frag fragmentation flag
     * @param dm   direct message flag
     * @return a string of bits for the first byte of the header
     */
    public static String createFirstHeaderByte(int dest, boolean syn, boolean ack, boolean frag,
                                               boolean dm) {
        StringBuilder firstByte = new StringBuilder();
        boolean[] flags = {syn, ack, frag, dm};
        firstByte.append(padString(Integer.toBinaryString(nodeID), 2));
        firstByte.append(padString(Integer.toBinaryString(dest), 2));
        for (boolean flag : flags) {
            firstByte.append(flag ? "1" : "0");
        }
        return firstByte.toString();
    }

    /**
     * Creates a string of bits for the second byte of the header.
     *
     * @param seq     sequence number for the packet
     * @param dataLen length of the actual data (so excluding the header)
     * @return a string of bits for the second byte of the header
     */
    public static String createSecondHeaderByte(int seq, int dataLen) {
        String output = "";
        output += padString(Integer.toBinaryString(seq), 5);
        String dataLenString = padString(Integer.toBinaryString(dataLen), 5);
        output += dataLenString.substring(0, 3);
        return output;
    }

    /**
     * Creates a string of bits for the third byte of the header.
     *
     * @param dataLen length of the actual data (so excluding the header)
     * @param nxtHop  node ID from the node who actually sent the packet
     * @param fragNum fragmentation number to indicate which fragment this is
     * @return a string of bits for the third byte of the header
     */
    public static String createThirdHeaderByte(int dataLen, int nxtHop, int fragNum) {
        String output = "";
        String dataLenString = padString(Integer.toBinaryString(dataLen), 5);
        output += dataLenString.substring(3);
        output += padString(Integer.toBinaryString(nxtHop), 2);
        output += padString(Integer.toBinaryString(fragNum), 4);
        return output;
    }


    /**
     * Client constructor which also starts the listener and sender.
     *
     * @param serverIp      server IP on which the socket is connected
     * @param serverPort    server port on which the socket is connected
     * @param frequency     frequency on which the communication takes place
     * @param receivedQueue receiving queue where packets are stored
     * @param sendingQueue  sending queue where packets are stored
     * @param id            the ID of the node corresponding to the client
     */
    public Client(String serverIp, int serverPort, int frequency,
                  BlockingQueue<Packet> receivedQueue, BlockingQueue<Packet> sendingQueue, int id) {
        this.receivedQueue = receivedQueue;
        this.sendingQueue = sendingQueue;
        this.nodeID = id;
        try {
            sock = SocketChannel.open();
            sock.connect(new InetSocketAddress(serverIp, serverPort));
            listener = new Listener(sock, receivedQueue);
            sender = new Sender(sock, sendingQueue);
            //dont worry, be happy (c)Thijs

            sender.sendConnect(frequency);

            listener.start();
            sender.start();
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e);
            System.exit(1);
        }
    }

    /**
     * Checks if the socket is connected.
     *
     * @return true if the socket is connected
     */
    public boolean isConnected() {
        return sock.isConnected();
    }

    private class Sender extends Thread {
        private BlockingQueue<Packet> sendingQueue;
        private SocketChannel sock;

        /**
         * Sender constructor used by the client for sending.
         *
         * @param sock         socket channel to which the sender should connect
         * @param sendingQueue the sending queue storing packets which needs to be sent
         */
        public Sender(SocketChannel sock, BlockingQueue<Packet> sendingQueue) {
            super();
            this.sendingQueue = sendingQueue;
            this.sock = sock;
        }

        /**
         * Loops through the sendingQueue, sending out data to the socket.
         */
        private void senderLoop() {
            while (sock.isConnected()) {
                try {
                    Packet msg = sendingQueue.take();
                    if (msg.getType() == PacketType.DATA
                            || msg.getType() == PacketType.DATA_SHORT) {
                        ByteBuffer data = msg.getData();
                        data.position(0); //reset position just to be sure
                        //assume capacity is also what we want to send here!
                        int length = data.capacity();
                        ByteBuffer toSend = ByteBuffer.allocate(length + 2);
                        if (msg.getType() == PacketType.SETUP) {
                            toSend.put((byte) 10);
                        } else if (msg.getType() == PacketType.DATA) {
                            toSend.put((byte) 3);
                        } else { // must be DATA_SHORT due to check above
                            toSend.put((byte) 6);
                        }
                        toSend.put((byte) length);
                        toSend.put(data);
                        toSend.position(0);
                        // System.out.println("Sending "+Integer.toString(length)+" bytes!");
                        sock.write(toSend);
                    }
                } catch (IOException e) {
                    System.err.println("Alles is stuk!");
                } catch (InterruptedException e) {
                    System.err.println("Failed to take from sendingQueue: " + e);
                }
            }
        }

        /**
         * Sending a connecting command to the socket.
         *
         * @param frequency frequency on which the connecting command should be sent
         */
        public void sendConnect(int frequency) {
            ByteBuffer buff = ByteBuffer.allocate(4);
            buff.put((byte) 9);
            buff.put((byte) ((frequency >> 16) & 0xff));
            buff.put((byte) ((frequency >> 8) & 0xff));
            buff.put((byte) (frequency & 0xff));
            buff.position(0);
            try {
                sock.write(buff);
            } catch (IOException e) {
                System.err.println("Failed to send HELLO");
            }
        }

        /**
         * Runs the thread which starts a senderLoop.
         */
        public void run() {
            senderLoop();
        }

    }

    private class Listener extends Thread {
        private BlockingQueue<Packet> receivedQueue;
        private SocketChannel sock;

        /**
         * Listener constructor used by the client for listening.
         *
         * @param sock          socket channel to which the listener should connect
         * @param receivedQueue the receiving queue storing packets which needs to be processed
         */
        public Listener(SocketChannel sock, BlockingQueue<Packet> receivedQueue) {
            super();
            this.receivedQueue = receivedQueue;
            this.sock = sock;
        }

        private ByteBuffer messageBuffer = ByteBuffer.allocate(1024);
        private int messageLength = -1;
        private boolean messageReceiving = false;
        private boolean shortData = false;
        private boolean setup = false;

        /**
         * Parses a message and putting it to the receivedQueue.
         *
         * @param received      the ByteBuffer which needs to be processed
         * @param bytesReceived amount of bytes received
         */
        private void parseMessage(ByteBuffer received, int bytesReceived) {
            // printByteBuffer(received, bytesReceived);

            try {
                for (int offset = 0; offset < bytesReceived; offset++) {
                    byte d = received.get(offset);

                    if (messageReceiving) {
                        if (messageLength == -1) {
                            messageLength = (int) d;
                            messageBuffer = ByteBuffer.allocate(messageLength);
                        } else {
                            messageBuffer.put(d);
                        }
                        if (messageBuffer.position() == messageLength) {
                            //Return DATA here
                            //printByteBuffer(messageBuffer, messageLength);
                            //System.out.println("pos:"+Integer.toString(messageBuffer.position()));
                            messageBuffer.position(0);
                            ByteBuffer temp = ByteBuffer.allocate(messageLength);
                            temp.put(messageBuffer);
                            temp.rewind();
                            //TODO: put SETUP message
                            if (setup) {
                                receivedQueue.put(new Packet(PacketType.SETUP, temp));
                            } else if (shortData) {
                                receivedQueue.put(new Packet(PacketType.DATA_SHORT, temp));
                            } else {
                                receivedQueue.put(new Packet(PacketType.DATA, temp));
                            }
                            messageReceiving = false;
                        }
                    } else {
                        switch (d) {
                            case 0x09:
                                // System.out.println("CONNECTED");
                                receivedQueue.put(new Packet(PacketType.HELLO));
                                break;
                            case 0x01:
                                // System.out.println("FREE");
                                receivedQueue.put(new Packet(PacketType.FREE));
                                break;
                            case 0x02:
                                // System.out.println("BUSY");
                                receivedQueue.put(new Packet(PacketType.BUSY));
                                break;
                            case 0x03:
                                messageLength = -1;
                                messageReceiving = true;
                                shortData = false;
                                break;
                            case 0x04:
                                // System.out.println("SENDING");
                                receivedQueue.put(new Packet(PacketType.SENDING));
                                break;
                            case 0x05:
                                // System.out.println("DONE_SENDING");
                                receivedQueue.put(new Packet(PacketType.DONE_SENDING));
                                break;
                            case 0x06:
                                messageLength = -1;
                                messageReceiving = true;
                                shortData = true;
                                break;
                            case 0x08:
                                // System.out.println("END");
                                receivedQueue.put(new Packet(PacketType.END));
                                break;
                            case 0x10:
                                messageLength = -1;
                                messageReceiving = true;
                                setup = true;
                                break;
                            default:
                                System.out.println();
                                break;
                        }
                    }
                }

            } catch (InterruptedException e) {
                System.err.println("Failed to put data in receivedQueue: " + e.toString());
            }
        }

        /**
         * Loops through the receivingQueue, parsing the messages in the queue.
         */
        public void receivingLoop() {
            int bytesRead = 0;
            ByteBuffer recv = ByteBuffer.allocate(1024);
            try {
                while (sock.isConnected()) {
                    bytesRead = sock.read(recv);
                    if (bytesRead > 0) {
                        // System.out.println("Received "+Integer.toString(bytesRead)+" bytes!");
                        parseMessage(recv, bytesRead);
                    } else {
                        break;
                    }
                    recv.clear();
                }
            } catch (IOException e) {
                System.err.println("Error on socket: " + e);
            }

        }

        /**
         * Runs the thread which starts a receivingLoop.
         */
        public void run() {
            receivingLoop();
        }

    }
}