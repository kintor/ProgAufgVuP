import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageHandler implements Runnable {
	// Attribute
	private Socket conn;
	private RingNode node;
	private Protocol protocol;
	private BufferedReader inServer;
	private ObjectOutputStream outServer;
	private String inMsg;

	private int task;
	private String absenderIP;
	private int absenderPort;
	private long absenderHash;

	public MessageHandler(Socket conn, RingNode node) {
		this.conn = conn;
		this.protocol = node.getProtocol();
		this.node = node;
	}

	public void run() {
		try {
			System.out
					.println("DEBUG: Verbindung angenommen und uebergeben an Thread: "
							+ Thread.currentThread());
			outServer = new ObjectOutputStream(conn.getOutputStream());
			inServer = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			inMsg = inServer.readLine();
			System.out.println("DEBUG: Nachricht wurde empfangen: " + inMsg);

			evalMsg(inMsg);

			switch (task) {
			case 0:
				System.out.println("Suche fuer neuen Knoten");
				outServer.writeObject(node.searchNodePosition(inMsg,
						absenderIP, absenderPort, absenderHash));
				break;
			case 1:
				System.out.println("Ein neuer Knoten");
				node.setPrevNode(getAbsenderIP(), getAbsenderPort());
				break;
			case 2:
				System.out.println("Suche nach Daten");
				break;
			case 3:
				System.out.println("Ping");
				if (node.prevNode == null) {
					node.setPrevNode(absenderIP, absenderPort);
				}
				outServer.writeObject(node.prevNode);
				break;
			case 4:
				System.out.println("Pong");
				break;
			case 5:
				System.out.println("Node leaves");
				break;
			}

			System.out.println("DEBUG: Verbindung wird geschlossen.");
			conn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void evalMsg(String msg) {
		String[] parts;
		parts = msg.split(",");
		task = evalTask(parts[0]);
		absenderIP = parts[1];
		absenderPort = Integer.valueOf(parts[2]);
		absenderHash = Long.valueOf(parts[3]);

	}

	public int evalTask(String taskString) {
		int task = 0;
		if (taskString.equals("position")) {
			task = 0;
		} else if (taskString.equals("new")) {
			task = 1;
		} else if (taskString.equals("search")) {
			task = 2;
		} else if (taskString.equals("ping")) {
			task = 3;
		} else if (taskString.equals("pong")) {
			task = 4;
		} else if (taskString.equals("leave")) {
			task = 5;
		}
		return task;
	}

	public String getAbsenderIP() {
		return absenderIP;
	}

	public int getAbsenderPort() {
		return absenderPort;
	}

	public long getAbsenderHash() {
		return absenderHash;
	}
}
