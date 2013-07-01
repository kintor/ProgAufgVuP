import java.io.Serializable;


public class Node implements Serializable{
	// Attribute
	private static final long serialVersionUID = 1L;
	private String ip;
	private int port;
	private long hash;
	private Hasher hasher;
	
	// Konstruktor
	public Node (String ip, int port) {
		this.ip = ip;
		this.port = port;
		hasher = new Hasher();
		this.hash = hasher.NodeToHash(ip, port);
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
		hash = hasher.NodeToHash(ip, port);
	}
}
