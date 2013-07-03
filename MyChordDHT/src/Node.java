import java.io.Serializable;

public class Node implements Serializable, Hashable {
	// Attribute
	private static final long serialVersionUID = 1L;
	private String ip;
	private int port;
	private long hash;
	// private Hasher hasher;

	// Konstruktor
	public Node(String ip, int port) {
		this.ip = ip;
		this.port = port;
		// hasher = new Hasher();
		// this.hash = hasher.NodeToHash(ip, port);
	}

	// Getter / Setter
	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public long getHash() {
		return hash;
	}

	public void setNode(String ip, int port) {
		this.ip = ip;
		this.port = port;
		// hash = hasher.NodeToHash(ip, port);
		hash = hashThis();
	}

	/*
	 * hashCode() gibt einen int zurück, zusätzlich wird der Wertebereich von
	 * int (-2147483648 ... 2147483647) auf den Bereich 0 - 4.294.967.295 (long)
	 * verschoben um positive Zahlen zu erhalten
	 */
	public long hashThis() {
		String nodeID = ip + port;
		hash = (long) nodeID.hashCode() + 2147483648L;
		return hash;
	}
}
