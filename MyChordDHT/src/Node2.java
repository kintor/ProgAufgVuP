
public class Node2 extends NodeAbstract {
	// Attribute
	private int hash;

	public Node2(String selfIP, int selfPort) {
		super(selfIP, selfPort);
	}

	public int calculateHash(String str) {
		hash = str.hashCode();
		return hash;
	}
}
