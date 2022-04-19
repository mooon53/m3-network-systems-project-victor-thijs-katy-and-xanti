package model;

import control.Ping;

import static control.MyProtocol.*;

public class PingSender implements Runnable{
	Ping ping;

	public PingSender(int nodeID) {
		this.ping = new Ping(nodeID);
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {

			}
			sendPacket(ping.toBytes());
		}
	}
}
