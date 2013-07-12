import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Communicator {
	// Attribute
	private RingNode node;

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
		// System.out.println("Starte Position zu IP " + node.nextNode.getIp()
		// + " auf Port " + node.nextNode.getPort());
		Node responseNode = null;
		try {
			Socket conn = new Socket(node.nextNode.getIp(),
					node.nextNode.getPort());
			PrintStream ps = new PrintStream(conn.getOutputStream());
			ps.println(msg);
			ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
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

	// neuer Knoten meldet sich bei Nachfolger setzt dessen prevNode und
	// bekommt von diesem zu verwaltende Daten als Liste geschickt
	public String connect2SetPrev() {
		// System.out.println("Starte new zu IP " + node.nextNode.getIp()
		// + " auf Port " + node.nextNode.getPort());
		String msg = "new," + node.getIp() + "," + node.getPort() + ","
				+ node.getHash();
		try {
			Socket conn = new Socket(node.nextNode.getIp(),
					node.nextNode.getPort());
			PrintStream ps = new PrintStream(conn.getOutputStream());
			ps.println(msg);
			
			// Daten kommen als CSV-String
			ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
			
			String data = (String) ois.readObject();
			if (data.equals("noData")) {
				data = null;
			}
			return data;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Node connect2SendPing() {
		// System.out.println("Starte Ping zu IP " + node.nextNode.getIp()
		// + " auf Port " + node.nextNode.getPort());
		String msg = "ping," + node.getIp() + "," + node.getPort() + ","
				+ node.getHash();
		try {
			Socket conn = new Socket(node.nextNode.getIp(),
					node.nextNode.getPort());
			PrintStream ps = new PrintStream(conn.getOutputStream());
			ps.println(msg);
			ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
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

	public void connect2SaveData(long hash, String str) {
		// System.out.println("Starte Save zu IP " + node.nextNode.getIp()
		// + " auf Port " + node.nextNode.getPort());
		String msg = "save," + node.getIp() + "," + node.getPort() + "," + hash
				+ "," + str;
		try {
			Socket conn = new Socket(node.nextNode.getIp(),
					node.nextNode.getPort());
			PrintStream ps = new PrintStream(conn.getOutputStream());
			ps.println(msg);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String connect2FindData(int searchHash) {
		// System.out.println("Starte Load zu IP " + node.nextNode.getIp()
		// + " auf Port " + node.nextNode.getPort());
		String msg = "load," + node.getIp() + "," + node.getPort() + ","
				+ searchHash;
		try {
			Socket conn = new Socket(node.nextNode.getIp(),
					node.nextNode.getPort());
			PrintStream ps = new PrintStream(conn.getOutputStream());
			ps.println(msg);

			/*
			 * BufferedReader br = new BufferedReader(new
			 * InputStreamReader(conn.getInputStream(), "UTF-8")); String
			 * respMsg = br.readLine(); for (byte b : respMsg.getBytes()) {
			 * System.out.println(b); } System.out.println("IM COMM: " +
			 * respMsg);
			 */
			ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
			String respMsg = (String) ois.readObject();
			return respMsg;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<String> connect2GetAll(String absenderIp,
			int absenderPort, int absenderHash) {
		String msg = "list," + absenderIp + "," + absenderPort + ","
				+ absenderHash;
		try {
			Socket conn = new Socket(node.nextNode.getIp(),
					node.nextNode.getPort());
			PrintStream ps = new PrintStream(conn.getOutputStream());
			ps.println(msg);

			ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
			ArrayList<String> list = new ArrayList<String>();
			list = (ArrayList<String>) ois.readObject();
			return list;
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
