package control;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import static control.MyProtocol.DEBUGGING_MODE;
import static view.DebugInterface.printPacket;

import model.*;

/**
 * Client object responsible for listening and sending.
 */
public class Client {
    private SocketChannel sock;
    private Sender sender;
    private Listener listener;

    private boolean busy;

    private BlockingQueue<Packet> receivedQueue;
    private BlockingQueue<Packet> sendingQueue;
    private PingSender pingSender;

    private int nodeID = 3;

    public static boolean[] inRange = new boolean[]{false, false, false, false};

    private static HashMap<Integer, FragHandler> fragHandlers = new HashMap();

    public BlockingQueue<Packet> getSendingQueue() {
        return sendingQueue;
    }

    private InRangeChecker[] inRangeCheckers = new InRangeChecker[4];

    /**
     * Returns the InRangeChecker object array.
     * @return array of InRangeChecker objects
     */
    public InRangeChecker[] getInRangeCheckers() {
        return inRangeCheckers;
    }

    /**
     * Resets the inRange boolean array.
     */
    public void resetInRange() {
        inRange = new boolean[]{false, false, false, false};
    }

    /**
     * Sets the value of a certain nodeID in the inRange array to the specified value.
     * @param nodeID the nodeID of which the value should be set
     * @param set the value it should be set to
     */
    public void setInRange(int nodeID, boolean set) {
        inRange[nodeID] = set;
    }

    /**
     * Sets the value of a certain nodeID in the inRange array to true.
     *
     * @param nodeID the nodeID of which the value should be set to true
     */
    public void setInRange(int nodeID) {
        setInRange(nodeID, true);
    }

    /**
     * Returns which nodes are in range.
     *
     * @return nodes that are in range
     */
    public boolean[] getInRange() {
        return inRange;
    }

    /**
     * Gets a FragmentHandler by its sequence number.
     *
     * @param seqNum sequence number of wanted FragmentHandler
     * @return FragmentHandler corresponding to the sequence number
     */
    public static FragHandler getFragHandler(int seqNum) {
        return fragHandlers.get(seqNum);
    }

    /**
     * Checks if a FragmentHandler for a certain sequence number already exists.
     *
     * @param seqNum sequence number for which the existence needs to be checked
     * @return true if the FragHandler corresponding to the given sequence number exists
     */
    public static boolean fragHandlerExists(int seqNum) {
        return fragHandlers.containsKey(seqNum);
    }

    /**
     * Adds a FragmentHandler to the stored HashMap.
     *
     * @param seqNum      sequence number corresponding to the FragHandler which needs to be added
     * @param fragHandler FragHandler which needs to be added to the receivedMessages HashMap
     */
    public static void addFragHandler(int seqNum, FragHandler fragHandler) {
        fragHandlers.put(seqNum, fragHandler);
        // TODO: store the FragmentHandlers differently,
        //  since we only store by sequence number and this is not unique
    }

    /**
     * Resets the HashMap of all receivedMessages.
     */
    public static void resetReceivedMessages() {
        fragHandlers = new HashMap<>();
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
        // for every client, create a receiving and a sending queue
        this.receivedQueue = receivedQueue;
        this.sendingQueue = sendingQueue;

        this.nodeID = id;
        try {
            sock = SocketChannel.open();
            sock.connect(new InetSocketAddress(serverIp, serverPort));

            // create a listener and sender connected to the socket
            listener = new Listener(sock, receivedQueue);
            sender = new Sender(sock, sendingQueue);

            sender.sendConnect(frequency);

            listener.start();
            sender.start();
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e);
            System.exit(1);
        }
    }

    /**
     * Starts a PingSender and a pingSender thread.
     */
    public void ping() {
        pingSender = new PingSender(nodeID, this);
        Thread pingSenderThread = new Thread(pingSender, "ping thread");
        pingSenderThread.start();
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
         * Works as pure ALOHA, also checking if the line is busy.
         */
        private void senderLoop() {
            Random random = new Random();
            while (sock.isConnected()) {
                try {
                    if (!busy && random.nextFloat() < 0.18) {
                        // take a packet from the sending queue
                        Packet msg = sendingQueue.take();
                        if (msg.getType() == PacketType.DATA
                                || msg.getType() == PacketType.DATA_SHORT) {
                            ByteBuffer data = msg.getData();
                            // reset position just to be sure
                            data.position(0);
                            // assume capacity is also what we want to send here!
                            int length = data.capacity();
                            ByteBuffer toSend = ByteBuffer.allocate(length + 2);
                            if (msg.getType() == PacketType.SETUP) {
                                toSend.put((byte) 10);
                            } else if (msg.getType() == PacketType.DATA) {
                                toSend.put((byte) 3);
                            } else {
                                // must be DATA_SHORT due to check above
                                toSend.put((byte) 6);
                            }
                            toSend.put((byte) length);
                            toSend.put(data);
                            toSend.position(0);
                            if (DEBUGGING_MODE) {
                                printPacket(msg, "Sending (TYPE, data): ");
                            }
                            sock.write(toSend);
                        }
                    }
                    Thread.sleep(100);
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
                            // return DATA here
                            // printByteBuffer(messageBuffer, messageLength);
                            // TODO: System.out.println("pos:" + messageBuffer.position());
                            messageBuffer.position(0);
                            ByteBuffer temp = ByteBuffer.allocate(messageLength);
                            temp.put(messageBuffer);
                            temp.rewind();
                            // TODO: put SETUP message
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
                                busy = false;
                                break;
                            case 0x02:
                                // System.out.println("BUSY");
                                receivedQueue.put(new Packet(PacketType.BUSY));
                                busy = true;
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
                System.err.println("Failed to put data in receivedQueue: " + e);
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