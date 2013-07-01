import java.io.Serializable;

public class Hasher implements Serializable {
	// Attribute
	private static final long serialVersionUID = 2L;
	private String nodeID;
	private long hash;

	// Konstruktor
	public Hasher() {
	}

	public long NodeToHash(String nodeIP, int nodePort) {
		nodeID = nodeIP + nodePort;
		hash = (long) nodeID.hashCode();
		hash = transferHash(hash);
		return hash;
	}

	public long StringToHash(String str) {
		hash = (long) str.hashCode();
		return hash;
	}

	/*
	 * verschiebt den Wertebereich von int (-2147483648 ... 2147483647) auf den
	 * Bereich 0 - 4.294.967.295
	 */
	private long transferHash(long hash) {
		hash = hash + 2147483648L;
		return hash;
	}
}
