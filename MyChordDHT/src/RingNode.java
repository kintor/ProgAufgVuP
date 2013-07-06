import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RingNode extends Node {
	// Attribute
	private static final long serialVersionUID = 1L;
	private Node nextNode;
	private Node prevNode;

	private ServerSocket sock;
	private Socket conn;
	private boolean serverStopped;
	private BufferedReader inServer;
	private ObjectOutputStream outServer;
	private ObjectInputStream inConnection;
	private PrintStream outConnection;
	private String inMsg;
	private Protocol protocol;
	private Communicator communicator;

	private Thread server;
	private ExecutorService serverThreadPool; // nimmt Verbindungen an

	public RingNode(String ip, int port, String nextIp, int nextPort) {
		super(ip, port);
		nextNode = new Node(nextIp, nextPort);
		prevNode = new Node(null, 0);
		protocol = new Protocol();
		communicator = new Communicator();
	}

	public void setNextNode(String ip, int port) {
		nextNode.setNode(ip, port);
		System.out.println("Mein Nachfolger ist jetzt " + ip + ":" + port);
	}

	private void setPrevNode(String ip, int port) {
		prevNode.setNode(ip, port);
	}

	/*
	 * Server-Thread: eršffnet einen ServerSocket, nimmt Verbindungen an und
	 * verteilt sie dann an einen ThreadPool
	 */
	public void initServerThread() {
		server = new Thread(new Runnable() {
			public void run() {
				try {
					serverThreadPool = Executors.newCachedThreadPool();
					sock = new ServerSocket(getPort());
					System.out.println("DEBUG: Horche auf Port: " + getPort());
					while (!serverStopped) {
						conn = sock.accept();
						serverThreadPool.execute(new ServerThreadWorker(conn,
								null)); // TODO: Node irgendwie Ÿbergeben? :-/
					}
				} catch (IOException e) {
					if (serverStopped) {
						System.out.println("Server ist gestoppt auf Port: "
								+ getPort());
					}
					e.printStackTrace();
				}
			}
		});
	}

	/*
	 * startet den Server-Thread
	 */
	public void startListening() {
		serverStopped = false;
		server.start();
	}

	/*
	 * beende den ThreadPool und schlie§e den ServerSocket
	 */
	private boolean stopListening() {
		try {
			serverStopped = true;
			serverThreadPool.shutdown();
			sock.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void searchNodePosition(String msg) {
		try {
			/*
			 * liegt der gesuchte Hash-Wert zwischen dem eigenen und dem
			 * Nachfolger, so sende nextNode zurŸck, sonst Anfrage weiterreichen
			 * an nextNode
			 */
			outServer = new ObjectOutputStream(conn.getOutputStream());
			long hash = protocol.getHash(msg);

			System.out.println("DEBUG: " + getHash()); // 7
			System.out.println("DEBUG: " + hash); // 9
			System.out.println("DEBUG: " + nextNode.getHash()); // 3

			if (getHash() == nextNode.getHash()) {
				// besteht der Ring derzeit aus nur einem Node,
				// so wird dieser zurŸck geliefert
				// self: 7 - search: 9 - next: 7
				outServer.writeObject(nextNode);
			} else if (getHash() > nextNode.getHash()) {
				// Grenze im Wertbereich des Rings
				// -> nextNode ist kleiner als ich selbst
				// self: 7 - next: 3
				if ((getHash() < hash) || (hash < nextNode.getHash())) {
					// gesuchter Key liegt im Grenzbereich
					// self:7 - search:9
					// oder:Êsearch:2 - next:3
					outServer.writeObject(nextNode);
				} else {
					// gesuchter Key liegt hinter dem nextNode,
					// also weiterleiten
					// self: 7 - search: 5 - next: 3
					outServer.writeObject(startConnection(msg));
				}
			} else if ((getHash() < hash) && (hash < nextNode.getHash())) {
				// gesuchter Key liegt zwischen mir und
				// nextNode,
				// also liefer nextNode zurŸck
				// self: 7 - search: 9 - next: 13
				outServer.writeObject(nextNode);
			} else {
				// gesuchter Key liegt hinter dem nextNode, also
				// weiterleiten
				outServer.writeObject(startConnection(msg));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Wird fŸr jeden Node gestartet. Im eigenen Thread lŠuft dauerhaft das
	 * Horchen auf den Port
	 */
	public void startEndPoint() {
		Thread endPoint = new Thread(new Runnable() {
			public void run() {
				try {
					String leave = "no";
					do {
						outServer = new ObjectOutputStream(conn
								.getOutputStream());
						inMsg = inServer.readLine();

						int task = protocol.evalTask(inMsg);
						switch (task) {
						case 0:
							System.out.println("Ein neuer Knoten");
							setNextNode(protocol.getIP(inMsg),
									protocol.getPort(inMsg));
							break;
						case 1:
							System.out.println("Suche nach Daten");
							/*
							 * liegt der gesuchte Hash-Wert zwischen dem eigenen
							 * und dem Nachfolger, so sende nextNode zurŸck,
							 * sonst Anfrage weiterreichen an nextNode
							 */
							long hash = protocol.getHash(inMsg);
							System.out.println(getHash()); // 7
							System.out.println(hash); // 9
							System.out.println(nextNode.getHash()); // 3

							if (getHash() == nextNode.getHash()) {
								// besteht der Ring derzeit aus nur einem Node,
								// so wird dieser zurŸck geliefert
								// self: 7 - search: 9 - next: 7
								outServer.writeObject(nextNode);
							} else if (getHash() > nextNode.getHash()) {
								// Grenze im Wertbereich des Rings
								// -> nextNode ist kleiner als ich selbst
								// self: 7 - next: 3
								if ((getHash() < hash)
										|| (hash < nextNode.getHash())) {
									// gesuchter Key liegt im Grenzbereich
									// self:7 - search:9
									// oder:Êsearch:2 - next:3
									outServer.writeObject(nextNode);
								} else {
									// gesuchter Key liegt hinter dem nextNode,
									// also weiterleiten
									// self: 7 - search: 5 - next: 3
									outServer
											.writeObject(startConnection(inMsg));
								}
							} else if ((getHash() < hash)
									&& (hash < nextNode.getHash())) {
								// gesuchter Key liegt zwischen mir und
								// nextNode,
								// also liefer nextNode zurŸck
								// self: 7 - search: 9 - next: 13
								outServer.writeObject(nextNode);
							} else {
								// gesuchter Key liegt hinter dem nextNode, also
								// weiterleiten
								outServer.writeObject(startConnection(inMsg));
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
		Node tmpNode = startConnection("position," + this.getIp() + ","
				+ this.getPort() + "," + this.getHash());
		setNextNode(tmpNode.getIp(), tmpNode.getPort());
		// tmpNode = startConnection("new," + this.getIp() + "," +
		// this.getPort()
		// + "," + this.getHash());

		// spŠtere Mšglichkeit die Finger-Table zu erstellen
		// refreshTable();
	}
}
