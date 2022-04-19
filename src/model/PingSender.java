package model;

import control.Client;
import control.MyProtocol;
import control.Ping;

import java.util.Arrays;

import static control.MyProtocol.*;

public class PingSender implements Runnable{
	Ping ping;
	Client client;
	int pingCounter = 0;

	public PingSender(int nodeID, Client client) {
		this.ping = new Ping(nodeID);
		this.client = client;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {

			}
			System.out.println("ping");
			System.out.println(Arrays.toString(client.getInRange()));
			sendPacket(ping.toBytes(), true);
		}
	}
}
