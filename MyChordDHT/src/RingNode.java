import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RingNode extends Node implements Runnable {
	// Attribute
	private static final long serialVersionUID = 1L;
	public volatile Node nextNode;
	public volatile Node prevNode;

	private ServerSocket sock;
	private Socket conn;
	private boolean serverStopped;

	private Thread server;
	private ExecutorService msgHandlerPool; // nimmt Verbindungen an
	private Timer timer;
	private Protocol protocol;
	private Communicator communicator;

	// Konstruktor
	public RingNode(String ip, int port, String nextIp, int nextPort) {
		super(ip, port);
		nextNode = new Node(nextIp, nextPort);
		timer = new Timer();
		communicator = new Communicator(this);
		protocol = new Protocol(this);
	}

	// Getter und Setter
	public Protocol getProtocol() {
		return protocol;
	}

	public Communicator getCommunicator() {
		return communicator;
	}

	public Socket getConnection() {
		return conn;
	}

	// setzt die Werte f�r nextNode
	public synchronized void setNextNode(String ip, int port) {
		nextNode = new Node(ip, port);
		System.out.println("Mein Nachfolger ist jetzt " + ip + ":" + port);
	}

	// setzt die Werte f�r prevNode
	public synchronized void setPrevNode(String ip, int port) {
		prevNode = new Node(ip, port);
		System.out.println("Mein Vorgaenger ist jetzt " + ip + ":" + port);
	}

	public void showStatus() {
		if (prevNode != null) {
			System.out.println("Mein Vorgaenger: ");
			System.out.println("    Ip: " + prevNode.getIp());
			System.out.println("    Port: " + prevNode.getPort());
			System.out.println("    Hash: " + prevNode.getHash());
		}
		System.out.println("Ich: ");
		System.out.println("    Ip: " + getIp());
		System.out.println("    Port: " + getPort());
		System.out.println("    Hash: " + getHash());

		System.out.println("Mein Nachfolger: ");
		System.out.println("    Ip: " + nextNode.getIp());
		System.out.println("    Port: " + nextNode.getPort());
		System.out.println("    Hash: " + nextNode.getHash());
	}

	/*
	 * run-Methode f�r Server-Thread: er�ffnet einen ServerSocket, nimmt
	 * Verbindungen an und verteilt sie dann an einen ThreadPool
	 */
	public void run() {
		try {
			msgHandlerPool = Executors.newCachedThreadPool();
			sock = new ServerSocket(getPort());
			System.out.println("DEBUG: Horche auf Port: " + getPort());
			while (!serverStopped) {
				conn = sock.accept();
				msgHandlerPool.execute(new MessageHandler(conn, this));
			}
		} catch (IOException e) {
			if (serverStopped) {
				System.out.println("DEBUG: Server ist gestoppt auf Port: "
						+ getPort());
			}
			e.printStackTrace();
		}
	}

	// erzeugt den Server-Thread
	public void initServerThread() {
		server = new Thread(this);
	}

	// startet den Server-Thread
	public void startListening() {
		serverStopped = false;
		server.start();
	}

	// beende den ThreadPool und schlie�e den ServerSocket
	public boolean stopListening() {
		try {
			serverStopped = true;
			msgHandlerPool.shutdown();
			sock.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	// ist das Ziel noch nicht erreicht, wird die Anfrage weitergeleitet,
	// ansonsten der n�chste Knoten zur�ck geschickt
	public synchronized Node searchNodePosition(String req, String ip, int port, long hash) {
		/*
		 * System.out.println("DEBUG: " + getHash()); // 7
		 * System.out.println("DEBUG: " + hash); // 9
		 * System.out.println("DEBUG: " + nextNode.getHash()); // 3
		 */
		if (getHash() == nextNode.getHash()) {
			// besteht der Ring derzeit aus nur einem Node, so wird dieser
			// zur�ck geliefert und der anfragende Knoten wird zum nextNode
			// self: 7 - search: 9 - next: 7
			Node tmpNode = nextNode;
			setNextNode(ip, port);
			System.out.println("antworte mit Knoten");
			return tmpNode;
		} else if (getHash() > nextNode.getHash()) {
			// Grenze im Wertbereich des Rings
			// -> nextNode ist kleiner als ich selbst
			// self: 7 - next: 3
			if ((getHash() < hash) || (hash < nextNode.getHash())) {
				// gesuchter Key liegt im Grenzbereich
				// self: 7 - search: 9
				// oder:�search: 2 - next: 3
				System.out.println("antworte mit Knoten");
				return nextNode;
			} else {
				// gesuchter Key liegt hinter dem nextNode, also weiterleiten
				// self: 7 - search: 5 - next: 3
				System.out.println("weiterleiten");
				return communicator.connect2FindNodePosition(req);
			}
		} else if ((getHash() < hash) && (hash < nextNode.getHash())) {
			// gesuchter Key liegt zwischen mir und nextNode, also liefere
			// nextNode zur�ck
			// self: 7 - search: 9 - next: 13
			System.out.println("antworte mit Knoten");
			return nextNode;
		} else {
			// gesuchter Key liegt hinter dem nextNode, also weiterleiten
			System.out.println("weiterleiten");
			return communicator.connect2FindNodePosition(req);
		}
	}

	/*
	 * wird beim Start des Nodes aufgerufen und die Position im Ring wird
	 * ermittelt
	 */
	public void searchPosition() {
		Node tmpNode = communicator.connect2FindNodePosition("position,"
				+ getIp() + "," + getPort() + "," + getHash());
		setNextNode(tmpNode.getIp(), tmpNode.getPort());

		// sage dem n�chsten Knoten seinen neuer Vorg�nger
		communicator.connect2SetPrev();

		// startStabilization();

		// sp�tere M�glichkeit die Finger-Table zu erstellen:
		// refreshTable();
	}
	
	public void startStabilization() {
		timer.schedule(protocol, 2000, 5000);
	}
}
