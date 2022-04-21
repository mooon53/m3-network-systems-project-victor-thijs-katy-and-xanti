package model;

import model.objects.Client;

/**
 * Gets created when a ping is received, waits 1 minute and then sets the node to unreachable.
 * If another ping from the node is received withing that minute it will be disabled and not set it to unreachable.
 */
public class PingListener implements Runnable {
	int nodeID;
	Client client;
	boolean disable = false;

	/**
	 * Constructor of PingListener.
	 * @param nodeID the ID of the node it's waiting for
	 * @param client the client, needed to be able to set the node to unreachable
	 */
	public PingListener(int nodeID, Client client) {
		this.nodeID = nodeID;
		this.client = client;
	}

	public void run() {
		try {
			Thread.sleep(90000); // Wait 90 seconds
		} catch (InterruptedException e) { }
		if (!disable) client.setInRange(nodeID, false); // If it hasn't been disabled yet, set the node to unreachable
	}

	/**
	 * Disables this thread so nothing happens after the minute timeout passes.
	 */
	public void disable() {
		disable = true;
	}
}