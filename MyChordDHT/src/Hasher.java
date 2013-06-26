
public class Hasher {
	// Attribute
	private String nodeID;
	private int hash;
	
	//Konstruktor
	Hasher () {
	}
	
	public int NodeToHash(String nodeIP, int nodePort) {
		nodeID = nodeIP + nodePort;
		hash = nodeID.hashCode();
		return hash;
	}
	
	public int StringToHash(String str) {
		hash = str.hashCode();
		return hash;
	}
}
