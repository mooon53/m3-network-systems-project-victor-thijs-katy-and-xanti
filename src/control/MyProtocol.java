package control;

import model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static control.Client.*;
import static utils.HelpFunc.*;

/**
 * This is just some example code to show you how to interact
 * with the server using the provided 'Client' class and two queues.
 * Feel free to modify this code in any way you like!
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

    public MyProtocol(String server_ip, int server_port, int frequency) {
        client = new Client(SERVER_IP, SERVER_PORT, frequency, receivedQueue, sendingQueue, nodeID); // Give the client the Queues to use

        new receiveThread(receivedQueue).start(); // Start thread to handle received messages!

        // handle sending from stdin from this thread.
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while (!br.ready()) {
                input = br.readLine(); // read input
                System.out.println(input);
                byte[] inputBytes = input.getBytes(); // get bytes from input
                for (byte i : inputBytes) {
                    System.out.println(i);
                }

                byte[] packet = new byte[32];
                System.out.println(input.length());
                byte[] header = Client.createHeader(0, false, false, false, false, 0, input.length(), 0, 0);
                System.arraycopy(header, 0, packet, 0, 3);
                System.out.println(bytesToString(header));

                // data
                System.arraycopy(inputBytes, 0, packet, 3, inputBytes.length);

                ByteBuffer toSend = ByteBuffer.allocate(packet.length); // make a new byte buffer with the length of the input string
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

    public static void main(String[] args) {
        if (args.length > 0) {
            frequency = Integer.parseInt(args[0]);
        }
        new MyProtocol(SERVER_IP, SERVER_PORT, frequency);
    }

    public static void sendMessage(Packet message) {
        try {
            sendingQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class receiveThread extends Thread {
        private final BlockingQueue<Packet> receivedQueue;

        public receiveThread(BlockingQueue<Packet> receivedQueue) {
            super();
            this.receivedQueue = receivedQueue;
        }

        public void printByteBuffer(ByteBuffer bytes, int bytesLength) {
            for (int i = 0; i < bytesLength; i++) {
                System.out.print(bytes.get(i) + " ");
            }
            System.out.println();
        }

        // Handle messages from the server / audio framework
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
                            Thread messageHandler = new Thread(new PacketDecoder(m.getData().array()));
                            messageHandler.start();
                            break;
                        case DATA_SHORT:
                            System.out.print("[DATA_SHORT]: ");
                            printByteBuffer(m.getData(), m.getData().capacity()); //Just print the data
                            try {

                            } catch (IllegalArgumentException e) {
                                System.out.println(";");
                            }
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
                            System.out.println("[SETUP] your node is: "+ nodeID);
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