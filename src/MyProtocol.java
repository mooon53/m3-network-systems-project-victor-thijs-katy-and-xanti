import client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    private static int frequency = 500 + 9 * 100;//TODO: Set this to your group frequency!

    private final Client client;
    private int nodeID;
    private int sequenceNumber;

    public MyProtocol(String server_ip, int server_port, int frequency) {
        BlockingQueue<Message> receivedQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Message> sendingQueue = new LinkedBlockingQueue<>();

        client = new Client(SERVER_IP, SERVER_PORT, frequency, receivedQueue, sendingQueue); // Give the client the Queues to use

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

                /* // source node, destination node & syn-flag
                packet[0] = bitsetToByte(concatBitSet(
                        new BitSet[]{
                                bytesToBitSet(new byte[]{(byte) nodeID}),
                                new BitSet(2), //TODO: destination node
                                new BitSet(1), //TODO: syn-flag
                                new BitSet(1), //TODO: ack-flag
                                new BitSet(1), //TODO: frag-flag
                                new BitSet(1)  //TODO: DM-flag
                        }));

                // sequence number & data length pt.1
                packet[1] = bitsetToByte(concatBitSet(
                        new BitSet[]{
                                bytesToBitSet(new byte[]{(byte) sequenceNumber}),
                                new BitSet(2), //TODO: destination node
                                new BitSet(1), //TODO: syn-flag
                                new BitSet(1), //TODO: ack-flag
                                new BitSet(1), //TODO: frag-flag
                                new BitSet(1)  //TODO: DM-flag
                        }));

                // syn, next-hop, fragmentation-flag & fragmentation-number
                packet[2] = bitsetToByte(concatBitSet(new BitSet[]{new BitSet(1), //TODO: syn
                        bytesToBitSet(new byte[]{(byte) nodeID}), //TODO: next-hop
                        new BitSet(1), //TODO: fragmentation-flag
                        new BitSet(4)}) //TODO: fragmentation-number
                );

                // error detection (ignoring the second and third byte)
                packet[3] = xorCheck(new Message(
                        MessageType.DATA,
                        ByteBuffer.wrap(concatByteArrays(new byte[]{packet[0]},
                                concatByteArrays(new byte[]{packet[1]}, inputBytes)))));

                */

                // data
                System.arraycopy(inputBytes, 0, packet, 4, inputBytes.length);

                ByteBuffer toSend = ByteBuffer.allocate(inputBytes.length); // make a new byte buffer with the length of the input string
                toSend.put(inputBytes, 0, inputBytes.length); // copy the input string into the byte buffer.
                Message msg;
                if ((input.length()) > 2) {
                    msg = new Message(MessageType.DATA, toSend);
                } else {
                    msg = new Message(MessageType.DATA_SHORT, toSend);
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

    public byte bitsetToByte(BitSet bitset) {
        if (bitset.length() > 8) return 0;
        return bitset.toByteArray()[0];
    }

    public BitSet bytesToBitSet(byte[] input) {
        return BitSet.valueOf(input);
    }

    public byte xorCheck(Message message) {
        byte[] bytes = message.getData().array();
        if (bytes.length >= 2) {
            byte xoredByte = bytes[0];
            for (int i = 1; i < bytes.length; i++) {
                xoredByte = (byte) (xoredByte ^ bytes[i]);
            }
            return xoredByte;
        } else if (bytes.length == 1) {
            return bytes[0];
        } else {
            return 0;
        }
    }

    public byte[] concatByteArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);

        return result;
    }

    public BitSet concatBitSet(BitSet[] bitsets) {
        BitSet output = new BitSet();
        int index = 0;
        for (int i = 0; i < bitsets.length; i++) {
            if (i > 0) index += bitsets[i - 1].length();
            for (int n = 0; n < bitsets[i].length(); i++) {
                if (bitsets[i].get(n)) output.set(index + n);
            }
        }
        return output;
    }

    private byte getBit(byte b, int index) {
        return (byte) ((b >> 7 - index) & 1);
    }

    private class receiveThread extends Thread {
        private final BlockingQueue<Message> receivedQueue;

        public receiveThread(BlockingQueue<Message> receivedQueue) {
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
                    Message m = receivedQueue.take();
                    switch (m.getType()) {
                        case BUSY:
                            System.out.println("[BUSY]");
                            break;
                        case FREE:
                            System.out.println("[FREE]");
                            break;
                        case DATA:
                            // TODO: test this with data-length in the header
                            byte firstByte = m.getData().get(0);
                            byte secondByte = m.getData().get(1);
                            byte thirdByte = m.getData().get(2);

                            System.out.print("[DATA from ");
                            System.out.print((getBit(firstByte, 0) * 2 + getBit(firstByte, 0) * 1) + "]: ");
                            try {

                                int dataLength =
                                        16 * getBit(secondByte, 5)
                                                + 8 * getBit(secondByte, 6)
                                                + 4 * getBit(secondByte, 7)
                                                + 2 * getBit(thirdByte, 0)
                                                + getBit(thirdByte, 1);
                                for (int i = 0; i < dataLength; i++) {
                                    System.out.print(((char) m.getData().get(i)));
                                }
                                System.out.println();
                            } catch (IllegalArgumentException e) {
                                System.out.println(";");
                            }
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