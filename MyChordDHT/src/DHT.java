import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DHT {
	// Attribute
	private static RingNode node;
	private static String selfIP;
	private static int selfPort;
	private static String entryIP;
	private static int entryPort;

	private static BufferedReader in;
	private static String exec;

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
			while(!node.addNode2Ring()) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		node.startStabilization();
		
		// Eingabe für User-Befehle
		in = new BufferedReader(new InputStreamReader(System.in));
		exec = "";

		while (true) {
			try {
				exec = in.readLine();
				if (exec.equals("status")) {
					node.showStatus();
				}
				if (exec.contains("exit")) {
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
