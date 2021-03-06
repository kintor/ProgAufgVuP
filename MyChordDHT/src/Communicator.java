import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Communicator {
	// Attribute
	private RingNode node;

	// Konstruktor
	public Communicator(RingNode node) {
		this.node = node;
	}

	/*
	 * �ffnet eine neue Verbindung zum n�chsten Node, Antwort vom Endpunkt ist
	 * immer ein Node-Objekt
	 */
	// Wieso wird auf node.nextNode.xy zugegriffen? nextNode kann sich �ndern
	// und wird dann nur in node gespeichert, also muss auf die jeweils aktuelle
	// Belegung zugegriffen werden
	public Node connect2FindNodePosition(String msg) {
		System.err
				.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
						+ "    "
						+ Thread.currentThread()
						+ "    "
						+ "Fuer die Positionssuche wird eine neue Verbindung aufgebaut nach: "
						+ node.nextNode.getIp() + ":" + node.nextNode.getPort());
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
		System.err
				.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
						+ "    "
						+ Thread.currentThread()
						+ "    "
						+ "Fuer das Einfuegen des neuen Knotens wird eine neue Verbindung aufgebaut nach: "
						+ node.nextNode.getIp() + ":" + node.nextNode.getPort());
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
		System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
				+ "    " + Thread.currentThread() + "    "
				+ "Fuer den Ping wird eine neue Verbindung aufgebaut nach: "
				+ node.nextNode.getIp() + ":" + node.nextNode.getPort());
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

	public Node connect2SaveData(long hash, String str) {
		System.err
				.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
						+ "    "
						+ Thread.currentThread()
						+ "    "
						+ "Fuer das Speichern von Daten wird eine neue Verbindung aufgebaut nach: "
						+ node.nextNode.getIp() + ":" + node.nextNode.getPort());
		String msg = "save," + node.getIp() + "," + node.getPort() + "," + hash
				+ "," + str;
		try {
			Socket conn = new Socket(node.nextNode.getIp(),
					node.nextNode.getPort());
			PrintStream ps = new PrintStream(conn.getOutputStream());
			ps.println(msg);
			
			ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
			Node respNode = (Node) ois.readObject();
			return respNode;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String connect2FindData(int searchHash) {
		System.err
				.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
						+ "    "
						+ Thread.currentThread()
						+ "    "
						+ "Fuer das Laden von Daten wird eine neue Verbindung aufgebaut nach: "
						+ node.nextNode.getIp() + ":" + node.nextNode.getPort());
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
		System.err
				.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
						+ "    "
						+ Thread.currentThread()
						+ "    "
						+ "Fuer das Einsammeln aller Daten wird eine neue Verbindung aufgebaut nach: "
						+ node.nextNode.getIp() + ":" + node.nextNode.getPort());
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
