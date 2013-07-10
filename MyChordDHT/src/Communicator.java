import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Communicator {
	// Attribute
	private RingNode node;

	private Socket conn;
	private PrintStream ps;
	private ObjectInputStream ois;

	// Konstruktor
	public Communicator(RingNode node) {
		this.node = node;
	}

	/*
	 * öffnet eine neue Verbindung zum nächsten Node, Antwort vom Endpunkt ist
	 * immer ein Node-Objekt
	 */
	// Wieso wird auf node.nextNode.xy zugegriffen? nextNode kann sich ändern
	// und wird dann nur in node gespeichert, also muss auf die jeweils aktuelle
	// Belegung zugegriffen werden
	public Node connect2FindNodePosition(String msg) {
		System.out.println("Starte Position zu IP " + node.nextNode.getIp()
				+ " auf Port " + node.nextNode.getPort());
		Node responseNode = null;
		try {
			conn = new Socket(node.nextNode.getIp(), node.nextNode.getPort());
			ps = new PrintStream(conn.getOutputStream());
			ps.println(msg);
			ois = new ObjectInputStream(conn.getInputStream());
			responseNode = (Node) ois.readObject();
			// conn.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return responseNode;
	}

	public boolean connect2SetPrev() {
		System.out.println("Starte new zu IP " + node.nextNode.getIp()
				+ " auf Port " + node.nextNode.getPort());
		String msg = "new," + node.getIp() + "," + node.getPort() + ","
				+ node.getHash();
		try {
			conn = new Socket(node.nextNode.getIp(), node.nextNode.getPort());
			ps = new PrintStream(conn.getOutputStream());
			ps.println(msg);

			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Node connect2SendPing() {
		System.out.println("Starte Ping zu IP " + node.nextNode.getIp()
				+ " auf Port " + node.nextNode.getPort());
		String msg = "ping," + node.getIp() + "," + node.getPort() + "," + node.getHash();
		try {
			conn = new Socket(node.nextNode.getIp(), node.nextNode.getPort());
			ps = new PrintStream(conn.getOutputStream());
			ps.println(msg);
			ois = new ObjectInputStream(conn.getInputStream());
			return (Node) ois.readObject();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
