import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class RingNode extends Node {
	// Attribute
	private static final long serialVersionUID = 1L;
	private Node nextNode;
	private Node prevNode;
	private ServerSocket sock;
	private Socket conn;
	private BufferedReader inEndPoint;
	private ObjectOutputStream outEndPoint;
	private ObjectInputStream inConnection;
	private PrintStream outConnection;
	private String endPointMsg;
	private Protocol protocol;

	public RingNode(String ip, int port, String nextIp, int nextPort) {
		super(ip, port);
		nextNode = new Node(nextIp, nextPort);
		prevNode = new Node(null, 0);
		protocol = new Protocol();
	}

	private void setNextNode(String ip, int port) {
		nextNode.setNode(ip, port);
		System.out.println("Mein Nachfolger ist jetzt " + ip + ":" + port);
	}

	private void setPrevNode(String ip, int port) {
		prevNode.setNode(ip, port);
	}

	/*
	 * Wird fŸr jeden Node gestartet. Im eigenen Thread lŠuft dauerhaft das
	 * Horchen auf den Port
	 */
	public void startEndPoint() {
		Thread endPoint = new Thread(new Runnable() {
			public void run() {
				try {
					sock = new ServerSocket(getPort());
					System.out.println("Horche auf Port " + getPort());
					String leave = "no";
					do {
						System.out.println("Warte auf Verbindung...");
						conn = sock.accept();
						inEndPoint = new BufferedReader(new InputStreamReader(
								conn.getInputStream()));
						outEndPoint = new ObjectOutputStream(conn
								.getOutputStream());
						endPointMsg = inEndPoint.readLine();

						int task = protocol.evalTask(endPointMsg);
						switch (task) {
						case 0:
							System.out.println("Ein neuer Knoten");
							setNextNode(protocol.getIP(endPointMsg),
									protocol.getPort(endPointMsg));
							break;
						case 1:
							System.out.println("Suche nach Daten");
							/*
							 * liegt der gesuchte Hash-Wert zwischen dem eigenen
							 * und dem Nachfolger, so sende nextNode zurŸck,
							 * sonst Anfrage weiterreichen an nextNode
							 */
							long hash = protocol.getHash(endPointMsg);
							System.out.println(getHash()); // 7
							System.out.println(hash); // 9
							System.out.println(nextNode.getHash()); // 3

							if (getHash() == nextNode.getHash()) {
								// besteht der Ring derzeit aus nur einem Node,
								// so wird dieser zurŸck geliefert
								// self: 7 - search: 9 - next: 7
								outEndPoint.writeObject(nextNode);
							} else if (getHash() > nextNode.getHash()) {
								// Grenze im Wertbereich des Rings
								// -> nextNode ist kleiner als ich selbst
								// self: 7 - next: 3
								if ((getHash() < hash)
										|| (hash < nextNode.getHash())) {
									// gesuchter Key liegt im Grenzbereich
									// self:7 - search:9
									// oder:Êsearch:2 - next:3
									outEndPoint.writeObject(nextNode);
								} else {
									// gesuchter Key liegt hinter dem nextNode,
									// also weiterleiten
									// self: 7 - search: 5 - next: 3
									outEndPoint
											.writeObject(startConnection(endPointMsg));
								}
							} else if ((getHash() < hash)
									&& (hash < nextNode.getHash())) {
								// gesuchter Key liegt zwischen mir und
								// nextNode,
								// also liefer nextNode zurŸck
								// self: 7 - search: 9 - next: 13
								outEndPoint.writeObject(nextNode);
							} else {
								// gesuchter Key liegt hinter dem nextNode, also
								// weiterleiten
								outEndPoint
										.writeObject(startConnection(endPointMsg));
							}

						case 2:
							System.out.println("Ping");
							break;
						case 3:
							System.out.println("Pong");
							break;
						case 4:
							System.out.println("Node leaves");
							break;
						}
						conn.close();
						System.out.println("Verbindung wieder geschlossen...");
					} while (leave.equals("no"));
					sock.close();
					System.out
							.println("Socket geschlossen, horche nicht mehr auf Port");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		endPoint.start();
	}

	/*
	 * šffnet eine neue Verbindung zum nŠchsten Node, wird im benštigten Fall
	 * gešffnet Antwort vom Endpunkt ist immer ein Node-Objekt
	 */
	private Node startConnection(String connMsg) {
		System.out.println("Starte Verbindung zu IP " + nextNode.getIp()
				+ " auf Port " + nextNode.getPort());
		Node responseNode = null;
		try {
			conn = new Socket(nextNode.getIp(), nextNode.getPort());
			outConnection = new PrintStream(conn.getOutputStream());
			outConnection.println(connMsg);
			inConnection = new ObjectInputStream(conn.getInputStream());
			responseNode = (Node) inConnection.readObject();
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

	/*
	 * wird beim Start des Nodes aufgerufen und die Position im Ring wird
	 * ermittelt
	 */
	public void searchPosition() {
		Node tmpNode = startConnection("search," + this.getIp() + ","
				+ this.getPort() + "," + this.getHash());
		setNextNode(tmpNode.getIp(), tmpNode.getPort());
		tmpNode = startConnection("new," + this.getIp() + "," + this.getPort()
				+ "," + this.getHash());

		// spŠtere Mšglichkeit die Finger-Table zu erstellen
		// refreshTable();
	}
}
