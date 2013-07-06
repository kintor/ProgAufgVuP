import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class NodeAbstract {
	// Abstrakte Funktionen
	public abstract int calculateHash(String str);

	// Attribute
	private String selfIP;
	private int selfPort;
	private int selfHash;
	private String nextIP;
	private int nextPort;
	/*private String prevIP;
	private int prevPort;
	private int prevHash;*/

	private Socket conn;
	private ServerSocket sock;
	private PrintStream out;
	private BufferedReader in;

	// Konstruktor
	public NodeAbstract(String selfIP, int selfPort) {
		this.selfIP = selfIP;
		this.selfPort = selfPort;
		this.selfHash = calculateHash(selfIP + selfPort);
	}

	// Getter und Setter
	public void setNextNode(String nextIP, int nextPort) {
		this.nextIP = nextIP;
		this.nextPort = nextPort;
	}

	public void findNextNode() {
		String nodeID = selfIP + selfPort;
		int hashNode = calculateHash(nodeID);
		if (searchPosition(hashNode)) {

		}
	}

	private boolean searchPosition(int hash) {
		System.out.println("Der Hash ist: " + hash);
		String msg = "new," + String.valueOf(hash);
		
		startSocket(msg);
		return false;
	}

	public void startServer() {
		System.out.println("Starte Server mit Port " + selfPort + " auf IP " + selfIP);
		try {
			sock = new ServerSocket(selfPort);
			conn = sock.accept();

			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			out = new PrintStream(conn.getOutputStream());
			
			String msg = in.readLine();
			System.out.println("Die erhaltene Nachricht ist:" + msg);
			System.out.println("Selfhash ist: " + selfHash);
			String parts[] = msg.split(",");
			if (Integer.parseInt(parts[1]) > selfHash) {
				searchPosition(Integer.parseInt(msg));
			} else {
				String rmsg = "Dein Nachfolger ist jetzt der Knoten mit IP " + selfIP + " und dem Port " + selfPort;
				out.println(rmsg);
			}

			System.out.println(in.readLine());

			conn.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String startSocket(String msg) {
		System.out.println("Starte Verbindung zu IP " + nextIP + " auf Port " + nextPort);
		System.out.println("Die Message ist: " + msg);
		try {
			conn = new Socket(nextIP, nextPort);
			out = new PrintStream(conn.getOutputStream());
			out.println(msg);
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			conn.close();
			return in.readLine();
		} catch (UnknownHostException uhe) {
			uhe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}
}
