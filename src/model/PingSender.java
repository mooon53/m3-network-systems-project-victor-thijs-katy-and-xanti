package model;

import model.objects.Client;
import model.objects.Ping;

import static control.MyProtocol.*;

/**
 * Sends pings.
 */
public class PingSender implements Runnable {
    Ping ping;
    Client client;
    long timeout;

    /**
     * Constructor for a ping sender.
     *
     * @param nodeID source from which the ping is sent
     * @param client client from which the ping is sent
     * @param timeout the amount of milliseconds between pings
     */
    public PingSender(int nodeID, Client client, long timeout) {
        this.ping = new Ping(nodeID);
        this.client = client;
        this.timeout = timeout;
    }

    /**
     * Sends a ping every 15 seconds.
     */
    public void run() {
        while (true) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) { }
            if (DEBUGGING_MODE) System.out.println("ping");
            sendPacket(ping.toBytes(), true);
        }
    }
}