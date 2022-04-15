package control;

import model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static utils.HelpFunc.*;

/**
 * The implementation of our protocol.
 */
public class MyProtocol {

    // The host to connect to. Set this to localhost when using the audio interface tool.
    private static final String SERVER_IP = "netsys.ewi.utwente.nl"; //"127.0.0.1";
    // The port to connect to. 8954 for the simulation server.
    private static final int SERVER_PORT = 8954;
    // The frequency to use.
    private static int frequency = 500 + 9 * 100;

    BlockingQueue<Packet> receivedQueue = new LinkedBlockingQueue<>();
    static BlockingQueue<Packet> sendingQueue = new LinkedBlockingQueue<>();

    private final Client client;
    private int nodeID;
    private int sequenceNumber;

    /**
     * Constructor of the protocol, starting a client,
     * receiving thread, while waiting for text input.
     *
     * @param serverIp   IP of the server to connect to
     * @param serverPort port of the server to connect to
     * @param frequency  frequency on which the communication should take place
     */
    public MyProtocol(String serverIp, int serverPort, int frequency) {
        // Give the client the Queues to use
        client = new Client(SERVER_IP, SERVER_PORT, frequency, receivedQueue, sendingQueue, nodeID);

        new ReceiveThread(receivedQueue).start(); // Start thread to handle received messages!

        // handle sending from stdin from this thread.
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while (!br.ready()) {
                input = br.readLine(); // read input
                //System.out.println(input);
                byte[] inputBytes = input.getBytes(); // get bytes from input
//                for (byte i : inputBytes) {
//                    System.out.println(i);
//                }

                byte[] packet = new byte[32];
                byte[] header = Client.createHeader(0, false, false, false, false, 0,
                        input.length(), 0, 0);
                System.arraycopy(header, 0, packet, 0, 3);

                // data
                System.arraycopy(inputBytes, 0, packet, 3, inputBytes.length);

                // make a new byte buffer with the length of the input string
                ByteBuffer toSend = ByteBuffer.allocate(packet.length);
                toSend.put(packet, 0, packet.length); // copy the input string into the byte buffer.
                Packet msg;
                //TODO: send the setup and find index using SYN

                if ((input.length()) > 2) {
                    msg = new Packet(PacketType.DATA, toSend);
                } else {
                    msg = new Packet(PacketType.DATA_SHORT, toSend);
                }
                sendingQueue.put(msg);
            }
        } catch (InterruptedException | IOException e) {
            System.exit(2);
        }
    }

    /**
     * Main method starting a new MyProtocol.
     *
     * @param args wanted frequency as program argument
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            frequency = Integer.parseInt(args[0]);
        }
        new MyProtocol(SERVER_IP, SERVER_PORT, frequency);
    }

    /**
     * Puts a message in the sendingQueue.
     *
     * @param packet packet which needed to be added to the sendingQueue
     */
    public static void sendMessage(Packet packet) {
        try {
            sendingQueue.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ReceiveThread extends Thread {
        private final BlockingQueue<Packet> receivedQueue;

        /**
         * Constructor for the receiveThread, setting the receivedQueue.
         *
         * @param receivedQueue receivedQueue to which this Thread's queue should be set to
         */
        public ReceiveThread(BlockingQueue<Packet> receivedQueue) {
            super();
            this.receivedQueue = receivedQueue;
        }

        // Handle messages from the server / audio framework

        /**
         * Runs the Thread for receiving messages.
         */
        public void run() {
            while (client.isConnected()) {
                try {
                    Packet m = receivedQueue.take();
                    switch (m.getType()) {
                        case BUSY:
                            System.out.println("[BUSY]");
                            break;
                        case FREE:
                            System.out.println("[FREE]");
                            break;
                        case DATA:
                            Thread messageHandler = new Thread(
                                    new PacketDecoder(m.getData().array()));
                            messageHandler.start();
                            break;
                        case DATA_SHORT:
                            System.out.print("[DATA_SHORT]: ");
                            //Just print the data
                            printByteBuffer(m.getData(), m.getData().capacity());
                            break;
                        case DONE_SENDING:
                            System.out.println("[DONE_SENDING]");
                            break;
                        case HELLO:
                            System.out.println("[HELLO]");
                            break;
                        case SENDING:
                            System.out.println("[SENDING]");
                            break;
                        case END:
                            System.out.println("[END]");
                            System.exit(0);
                            break;
                        case SETUP:
                            System.out.println("[SETUP] your node is: " + nodeID);
                            break;
                        default:
                            System.out.println();
                            break;
                    }
                } catch (InterruptedException e) {
                    System.err.println("Failed to take from queue: " + e);
                }
            }
        }
    }
}