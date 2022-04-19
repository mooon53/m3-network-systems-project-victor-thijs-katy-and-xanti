package model;

import control.Client;
import control.MyProtocol;
import control.Ping;

import java.util.Arrays;

import static control.MyProtocol.*;

public class PingSender implements Runnable {
    Ping ping;
    Client client;
    int pingCounter = 0;

    /**
     * Constructor for a ping sender.
     *
     * @param nodeID source from which the ping is sent
     * @param client client from which the ping is sent
     */
    public PingSender(int nodeID, Client client) {
        this.ping = new Ping(nodeID);
        this.client = client;
    }

    /**
     * Sends a ping every 15 seconds.
     */
    public void run() {
        while (true) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {

            }
            System.out.println("ping");
            System.out.println(Arrays.toString(client.getInRange()));
            sendPacket(ping.toBytes(), true);
        }
    }
}
