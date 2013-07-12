import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

		// Eingabe fŸr User-Befehle
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line = "";
		int index = 0;
		String exec = "";
		String data = "";

		while (true) {
			try {
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
					node.saveData(data.hashCode(), data);
				} else if (exec.equals("get")) {
					System.out.println("DAS IST DIE ANTWORT: "
							+ node.loadData(Integer.valueOf(data)));
				} else if (exec.equals("list")) {
					ArrayList<String> list = node.listData(node.getIp(),
							node.getPort(), node.getHash());
					System.out.println("Die im Ring gespeicherten Werte sind:");
					for (Iterator<String> i = list.iterator(); i.hasNext();) {
						System.out.println(i.next());
					}
				}
				if (exec.contains("exit")) {
					node.stopListening();
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
