import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
			// System.out
			// .println("DEBUG: Verbindung angenommen und uebergeben an Thread: "
			// + Thread.currentThread());
			outServer = new ObjectOutputStream(conn.getOutputStream());
			inServer = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "UTF-8"));
			inMsg = inServer.readLine();
			// System.out.println("DEBUG: Nachricht wurde empfangen: " + inMsg);

			evalTask(inMsg);

			switch (task) {
			case 0:
				evalNode(inMsg);
				System.out.println("Suche fuer neuen Knoten");
				outServer.writeObject(node.searchNodePosition(inMsg,
						absenderIP, absenderPort, absenderHash));
				break;
			case 1:
				evalNode(inMsg);
				System.out.println("Ein neuer Knoten");
				synchronized (node) {
					if (node.prevNode == null
							|| (absenderHash > node.prevNode.getHash() && absenderHash < node
									.getHash())
							|| (node.prevNode.getHash() > node.getHash() && absenderHash > node.prevNode.getHash())) {
						node.setPrevNode(getAbsenderIP(), getAbsenderPort());

						String msg = node.passData2Prev();
						if (msg.equals("")) {
							outServer.writeObject("noData");
						} else {
							outServer.writeObject(msg);
						}
					}
				}
				System.out.println("DEBUG: Verbindung wird geschlossen.");
				break;
			case 2:
				System.out.println("Suche nach Daten und laden");
				evalLoadData(inMsg);
				/*
				 * PrintStream ps = new PrintStream(conn.getOutputStream(),
				 * false, "UTF-8"); String respMsg = node.loadData(dataHash);
				 * for (byte b : respMsg.getBytes()) { System.out.println(b); }
				 * System.out.println("IM HANDLER: " + respMsg);
				 * ps.println(respMsg);
				 */
				outServer.writeObject(node.loadData(dataHash));
				break;
			case 3:
				evalNode(inMsg);
				System.out.println("Ping");
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
			case 4:
				evalSaveData(inMsg);
				System.out.println("Daten abspeichern");
				node.saveData(dataHash, data);
				break;
			case 5:
				System.out.println("Liste aller Daten");
				evalNode(inMsg);
				outServer.writeObject(node.listData(absenderIP, absenderPort,
						absenderHash));
				break;
			}

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
		} else if (taskString.equals("load")) {
			task = 2;
		} else if (taskString.equals("ping")) {
			task = 3;
		} else if (taskString.equals("save")) {
			task = 4;
		} else if (taskString.equals("list")) {
			task = 5;
		}
	}

	public String getAbsenderIP() {
		return absenderIP;
	}

	public int getAbsenderPort() {
		return absenderPort;
	}

	public int getAbsenderHash() {
		return absenderHash;
	}
}
