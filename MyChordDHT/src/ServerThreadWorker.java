import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerThreadWorker implements Runnable {
	// Attribute
	private Socket conn;
	private RingNode node;
	private BufferedReader inServer;
	private String inMsg;
	private Protocol protocol;

	public ServerThreadWorker(Socket conn, RingNode node) {
		this.conn = conn;
		this.node = node;
	}

	public void run() {
		try {
			System.out
					.println("DEBUG: Verbindung angenommen und uebergeben an Thread: "
							+ Thread.currentThread());
			inServer = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			inMsg = inServer.readLine();
			// TODO: debug entfernen
			System.out.println("DEBUG: Nachricht wurde empfangen");

			// TODO: verarbeite gelesene Nachricht
			int task = protocol.evalTask(inMsg);
			switch (task) {
			case 0:
				System.out.println("Suche fuer neuen Knoten");
				node.searchNodePosition(inMsg);
				break;
			case 1:
				System.out.println("Ein neuer Knoten");
				node.setNextNode(protocol.getIP(inMsg), protocol.getPort(inMsg));
				break;
			case 2:
				System.out.println("Suche nach Daten");
				break;
			case 3:
				System.out.println("Ping");
				break;
			case 4:
				System.out.println("Pong");
				break;
			case 5:
				System.out.println("Node leaves");
				break;
			}
			System.out.println("Verbindung wird geschlossen.");
			conn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
