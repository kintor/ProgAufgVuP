import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageHandler implements Runnable {
	// Attribute
	private Socket conn;
	private RingNode node;
	private BufferedReader inServer;
	private ObjectOutputStream outServer;
	private String inMsg;

	private int task;
	private String absenderIP;
	private int absenderPort;
	private int absenderHash;
	private int dataHash;
	private String data;

	public MessageHandler(Socket conn, RingNode node) {
		this.conn = conn;
		this.node = node;
	}

	public void run() {
		try {
			System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
					+ "    " + Thread.currentThread() + "    "
					+ "Eine Verbindung angenommen und an aktuellen Thread uebergeben");
			outServer = new ObjectOutputStream(conn.getOutputStream());
			inServer = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "UTF-8"));
			inMsg = inServer.readLine();
			System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
					+ "    " + Thread.currentThread() + "    "
					+ "Folgende Nachricht wurde vom Server empfangen: " + inMsg);

			evalTask(inMsg);

			switch (task) {
			case 0:
				evalNode(inMsg);
				System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
						+ "    " + Thread.currentThread() + "    "
						+ "Die Aufgabe ist: Suche neue Knotenposition.");
				outServer.writeObject(node.searchNodePosition(inMsg,
						absenderIP, absenderPort, absenderHash));
				break;
			case 1:
				evalNode(inMsg);
				System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
						+ "    " + Thread.currentThread() + "    "
						+ "Die Aufgabe ist: Fuege den neuen Knoten (bei die als Vorgaenger) im Ring ein.");
				synchronized (node) {
					if (node.prevNode == null
							|| (absenderHash > node.prevNode.getHash() && absenderHash < node
									.getHash())
							|| (node.prevNode.getHash() > node.getHash() && absenderHash > node.prevNode.getHash())) {
						node.setPrevNode(absenderIP, absenderPort);

						String msg = node.passData2Prev();
						if (msg.equals("")) {
							outServer.writeObject("noData");
						} else {
							outServer.writeObject(msg);
						}
					}
				}
				break;
			case 2:
				evalSaveData(inMsg);
				System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
						+ "    " + Thread.currentThread() + "    "
						+ "Die Aufgabe ist: Speicher die Daten auf dem richtigen Knoten ab.");
				outServer.writeObject(node.saveData(dataHash, data));
				break;
			case 3:
				evalLoadData(inMsg);
				System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
						+ "    " + Thread.currentThread() + "    "
						+ "Die Aufgabe ist: Suche und lade die angeforderten Daten.");
				outServer.writeObject(node.loadData(dataHash));
				break;
			case 4:
				evalNode(inMsg);
				System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
						+ "    " + Thread.currentThread() + "    "
						+ "Die Aufgabe ist: Sammel alle gespeicherten Daten ein.");
				outServer.writeObject(node.listData(absenderIP, absenderPort,
						absenderHash));
				break;
			case 5:
				evalNode(inMsg);
				System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
						+ "    " + Thread.currentThread() + "    "
						+ "Die Aufgabe ist: Verarbeite Ping des Stabilisierungsprotokoll");
				synchronized (node) {
					if ((node.prevNode == null)
							|| (absenderHash > node.prevNode.getHash() && absenderHash < node
									.getHash())
							// Grenze des Rings
							|| (node.prevNode.getHash() > node.getHash() && absenderHash > node.prevNode
									.getHash())) {
						node.setPrevNode(absenderIP, absenderPort);
					}
				}
				outServer.writeObject(node.prevNode);
				break;
			}

			System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
					+ "    " + Thread.currentThread() + "    "
					+ "Die Verbindung im aktuellen Thread wird wieder geschlossen.");
			conn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void evalNode(String msg) {
		String[] parts;
		parts = msg.split(",");
		absenderIP = parts[1];
		absenderPort = Integer.valueOf(parts[2]);
		absenderHash = Integer.valueOf(parts[3]);
	}

	private void evalSaveData(String msg) {
		String[] parts;
		parts = msg.split(",");
		absenderIP = parts[1];
		absenderPort = Integer.valueOf(parts[2]);
		dataHash = Integer.valueOf(parts[3]);
		data = parts[4];
	}

	private void evalLoadData(String msg) {
		String[] parts;
		parts = msg.split(",");
		absenderIP = parts[1];
		absenderPort = Integer.valueOf(parts[2]);
		dataHash = Integer.valueOf(parts[3]);
	}

	private void evalTask(String msg) {
		String[] parts;
		parts = msg.split(",");
		String taskString = parts[0];
		if (taskString.equals("position")) {
			task = 0;
		} else if (taskString.equals("new")) {
			task = 1;
		} else if (taskString.equals("save")) {
			task = 2;
		} else if (taskString.equals("load")) {
			task = 3;
		} else if (taskString.equals("list")) {
			task = 4;
		} else if (taskString.equals("ping")) {
			task = 5;
		}
	}
}
