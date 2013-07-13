import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class DHT {
	// Attribute
	private static RingNode node;
	private static String selfIP;
	private static int selfPort;
	private static String entryIP;
	private static int entryPort;

	// Main
	public static void main(String[] args) {
		System.err.println(new SimpleDateFormat("hh:mm:ss").format(new Date())
				+ "    " + Thread.currentThread() + "    "
				+ "Der Prozess wurde gestartet.");
		selfIP = args[0];
		selfPort = Integer.parseInt(args[1]);
		if (args.length > 2) {
			entryIP = args[2];
			entryPort = Integer.parseInt(args[3]);
		} else {
			entryIP = args[0];
			entryPort = Integer.parseInt(args[1]);
		}

		node = new RingNode(selfIP, selfPort, entryIP, entryPort);
		node.initServerThread();
		node.startListening();
		if (args.length > 2) {
			while (!node.addNode2Ring()) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		node.startStabilization();

		// Eingabe für User-Befehle
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line = "";
		int index = 0;
		String exec = "";
		String data = "";

		System.out.println("Der Knoten wurde gestartet.");
		System.out.println("Sobald er vollständig im Ring integriert ist,"
				+ "koennen Sie Daten speichern.");
		System.out.println("Folgende Befehle koennen Sie absetzen:");
		System.out.println("put <Stringkette> ->"
				+ "Speichert den String auf dem entsprechenden Knoten.");
		System.out.println("get <Hashwert> ->"
				+ "Laedt die zu dem Hashwert gespeicherten Daten.");
		System.out.println("list ->"
				+ "Zeigt alle im Ring gespeicherten Daten an.");
		System.out
				.println("status ->"
						+ "Zeigt Vorgaenger, Nachfolger sowie die eigenen Werte des Knoten an.");
		System.out.println("");

		while (true) {
			try {
				System.out.print("-->  ");
				line = in.readLine();
				index = line.indexOf(" ");

				if (index >= 0) {
					exec = line.substring(0, index);
					data = line.substring(index).trim();
				} else {
					exec = line;
				}

				if (exec.equals("status")) {
					node.showStatus();
				} else if (exec.equals("put")) {
					Node respNode = node.saveData(data.hashCode(), data);
					System.out.println("Die Daten wurden gespeichert.");
					System.out.println("Der Hashwert der Daten lautet: "
							+ data.hashCode());
					System.out
							.println("Die Daten liegen jetzt auf dem Knoten mit ID, IP + Port: "
									+ respNode.getHash()
									+ ", "
									+ respNode.getIp()
									+ ":"
									+ respNode.getPort());
				} else if (exec.equals("get")) {
					String[] resp = node.loadData(Integer.valueOf(data)).split(",");
					System.out.println("Die angefragten Daten sind: "
							+ resp[0]);
					System.out.println("Geantwortet hat der Knoten mit ID, IP + Port: "
							+ resp[3] + ", " + resp[1] + ":" + resp[2]);
				} else if (exec.equals("list")) {
					ArrayList<String> list = node.listData(node.getIp(),
							node.getPort(), node.getHash());
					System.out.println("Die im Ring gespeicherten Werte sind:");
					System.out.println("    Hash --> Data: ");
					for (Iterator<String> i = list.iterator(); i.hasNext();) {
						System.out.println("    " + i.next() + " --> " + i.next());
					}
				} else if (exec.equals("exit")) {
					node.stopListening();
					break;
				}
				System.out.println("");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
