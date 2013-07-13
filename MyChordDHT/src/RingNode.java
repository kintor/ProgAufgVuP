import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RingNode extends Node implements Runnable {
	// Attribute
	private static final long serialVersionUID = 1L;
	public volatile Node nextNode;
	public volatile Node prevNode;

	private volatile Hashtable<Integer, String> database;

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
		database = new Hashtable<Integer, String>();
		System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
				+ "    " + Thread.currentThread() + "    "
				+ "Der Knoten wurde initialisiert.");
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
		System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
				+ "    " + Thread.currentThread() + "    "
				+ "Mein Nachfolger ist jetzt " + ip + ":" + port);
	}

	// setzt die Werte f�r prevNode
	public synchronized void setPrevNode(String ip, int port) {
		prevNode = new Node(ip, port);
		System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
				+ "    " + Thread.currentThread() + "    "
				+ "Mein Vorgaenger ist jetzt " + ip + ":" + port);
	}

	// Funktionen f�r die User-Interaktion
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

		System.out.println("Meine Daten sind: ");
		if (database.isEmpty()) {
			System.out.println("    Es sind keine Daten gespeichert.");
		} else {
			for (int key : database.keySet()) {
				System.out.println("    Key: " + key + " -> "
						+ database.get(key));
			}
		}
		System.out.println("");
	}

	public String loadData(int searchHash) {
		String response = "";
		if ((getHash() < prevNode.getHash())
				&& ((searchHash > prevNode.getHash()) || (searchHash < getHash()))) {
			response = database.get(searchHash);
		} else if ((searchHash < getHash() && searchHash > prevNode.getHash())) {
			response = database.get(searchHash);
		} else {
			response = communicator.connect2FindData(searchHash);
		}

		if (response == null) {
			response = "Daten nicht gefunden!";
		}
		return response;
	}

	public void saveData(int strHash, String str) {
		while (prevNode == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if ((getHash() < prevNode.getHash())
				&& ((strHash > prevNode.getHash()) || (strHash < getHash()))) {
			database.put(strHash, str);
		} else if ((strHash < getHash() && strHash > prevNode.getHash())) {
			database.put(strHash, str);
		} else {
			communicator.connect2SaveData(strHash, str);
		}
	}

	public ArrayList<String> listData(String absenderIp, int absenderPort,
			int absenderHash) {
		ArrayList<String> list = new ArrayList<String>();
		if (absenderHash != nextNode.getHash()) {
			list = communicator.connect2GetAll(absenderIp, absenderPort,
					absenderHash);
		}
		for (int key : database.keySet()) {
			list.add(database.get(key));
		}
		return list;
	}

	// startet den Timer f�r das Stabilisierungsprotokol
	public void startStabilization() {
		timer.schedule(protocol, 2000, 5000);
	}

	/*
	 * run-Methode f�r Server-Thread: er�ffnet einen ServerSocket, nimmt
	 * Verbindungen an und verteilt sie dann an einen ThreadPool
	 */
	public void run() {
		try {
			msgHandlerPool = Executors.newCachedThreadPool();
			sock = new ServerSocket(getPort());
			System.err.println(new SimpleDateFormat("hh:mm:ss")
					.format(new Date())
					+ "    "
					+ Thread.currentThread()
					+ "    "
					+ "Der Server wurder auf Port "
					+ getPort()
					+ " gestartet.");
			while (!serverStopped) {
				conn = sock.accept();
				msgHandlerPool.execute(new MessageHandler(conn, this));
			}
		} catch (IOException e) {
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
	public synchronized Node searchNodePosition(String req, String ip,
			int port, int hash) {

		if (getHash() == nextNode.getHash()) {
			// besteht der Ring derzeit aus nur einem Node, so wird dieser
			// zur�ck geliefert und der anfragende Knoten wird zum nextNode
			// self: 7 - search: 9 - next: 7
			Node tmpNode = nextNode;
			setNextNode(ip, port);
			System.err
					.println(new SimpleDateFormat("hh:mm:ss")
							.format(new Date())
							+ "    "
							+ Thread.currentThread()
							+ "    "
							+ "Position wurde gefunden, nextNode wird zurueck geschickt.");
			return tmpNode;
		} else if (getHash() > nextNode.getHash()) {
			// Grenze im Wertbereich des Rings
			// -> nextNode ist kleiner als ich selbst
			// self: 7 - next: 3
			if ((getHash() < hash) || (hash < nextNode.getHash())) {
				// gesuchter Key liegt im Grenzbereich
				// self: 7 - search: 9
				// oder:�search: 2 - next: 3
				System.err
						.println(new SimpleDateFormat("hh:mm:ss")
								.format(new Date())
								+ "    "
								+ Thread.currentThread()
								+ "    "
								+ "Position wurde gefunden, nextNode wird zurueck geschickt.");
				return nextNode;
			} else {
				// gesuchter Key liegt hinter dem nextNode, also weiterleiten
				// self: 7 - search: 5 - next: 3
				System.err
						.println(new SimpleDateFormat("hh:mm:ss")
								.format(new Date())
								+ "    "
								+ Thread.currentThread()
								+ "    "
								+ "Position wurde noch nicht gefunden, Anfrage wird weitergeleitet.");
				return communicator.connect2FindNodePosition(req);
			}
		} else if ((getHash() < hash) && (hash < nextNode.getHash())) {
			// gesuchter Key liegt zwischen mir und nextNode, also liefere
			// nextNode zur�ck
			// self: 7 - search: 9 - next: 13
			System.err
					.println(new SimpleDateFormat("hh:mm:ss")
							.format(new Date())
							+ "    "
							+ Thread.currentThread()
							+ "    "
							+ "Position wurde gefunden, nextNode wird zurueck geschickt.");
			return nextNode;
		} else {
			// gesuchter Key liegt hinter dem nextNode, also weiterleiten
			System.err
					.println(new SimpleDateFormat("hh:mm:ss")
							.format(new Date())
							+ "    "
							+ Thread.currentThread()
							+ "    "
							+ "Position wurde noch nicht gefunden, Anfrage wird weitergeleitet.");
			return communicator.connect2FindNodePosition(req);
		}
	}

	// wird beim Start des Nodes aufgerufen und die Position im Ring wird
	// ermittelt
	public boolean addNode2Ring() {
		Node tmpNode = communicator.connect2FindNodePosition("position,"
				+ getIp() + "," + getPort() + "," + getHash());
		if (tmpNode == null) {
			return false;
		}

		setNextNode(tmpNode.getIp(), tmpNode.getPort());

		// sage dem n�chsten Knoten seinen neuer Vorg�nger und bekomme
		// gegebenenfalls zu verwaltene Daten (als CSV) und speicher diese
		String data = communicator.connect2SetPrev();
		if (data != null) {
			data = data.substring(1);
			String[] dataArray = data.split(",");
			for (int i = 0; i < dataArray.length; i = i + 2) {
				database.put(Integer.valueOf(dataArray[i]), dataArray[i + 1]);
			}
		}

		// sp�tere M�glichkeit die Finger-Table zu erstellen:
		// refreshTable();

		return true;
	}

	// Daten, f�r die der neue Vorg�nger zust�ndig ist, werden zu diesem
	// geleitet und hier gel�scht
	public synchronized String passData2Prev() {
		String msg = "";
		int[] keys = new int[database.size()];
		int i = 0;
		for (int key : database.keySet()) {
			// das erste Komma wird nach dem Empfangen entfernt
			if (key < prevNode.getHash()) {
				msg = msg + "," + key + "," + database.get(key);
				keys[i] = key;
				i++;
			}
		}
		for (int j = 0; j < i; j++) {
			database.remove(keys[j]);
		}
		return msg;
	}
}
