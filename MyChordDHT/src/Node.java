import java.io.Serializable;

public class Node implements Serializable, Hashable {
	// Attribute
	private static final long serialVersionUID = 1L;
	private final String ip;
	private final int port;
	private int hash;
	// private Hasher hasher;

	// Konstruktor
	public Node(String ip, int port) {
		this.ip = ip;
		this.port = port;
		this.hash = hashThis();
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

	public int getHash() {
		return hash;
	}

	// hashCode() gibt einen int zurück
	public int hashThis() {
		String nodeID = ip + port;
		return nodeID.hashCode();
	}
}
