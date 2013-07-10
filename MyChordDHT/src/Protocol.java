import java.util.TimerTask;

public class Protocol extends TimerTask {
	// Attribute
	private Node respNode;
	private RingNode node;
	private Communicator communicator;

	// Konstruktor
	public Protocol(RingNode node) {
		this.node = node;
		this.communicator = node.getCommunicator();
	}

	public void run() {
		respNode = communicator.connect2SendPing();
		// wenn der zurück erhaltene Knoten ich selbst bin, dann ist alles ok,
		// wenn nicht, wird mein nextNode neu gesetzt
		if (!(respNode.getHash() == node.getHash())) {
			node.setNextNode(respNode.getIp(), respNode.getPort());
		}
	}
}
