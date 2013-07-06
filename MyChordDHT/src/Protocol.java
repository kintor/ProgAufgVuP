/*
 * Das Protokol ist nach folgendem Schema aufgebaut:
 * Task,Absender-IP,Absender-Port,Daten
 * 
 * Task beschreibt die Aufgabe:
 * 		Position neuer Knoten:	position -> 0
 * 		Anmelden neuer Knoten: 	new -> 1
 * 		Suche nach Daten:	 	search -> 2
 * 		Stabilization: 			ping -> 3
 * 								pong -> 4
 * 		Austritt eines Knoten:	leave -> 5
 */

public class Protocol {
	// Attribute
	private int task;

	// Konstruktor
	public Protocol() {
	}

	public int evalTask(String msg) {
		String tmp = msg.split(",")[0];
		if (tmp.equals("position")) {
			task = 0;
		} else if (tmp.equals("new")) {
			task = 1;
		} else if (tmp.equals("search")) {
			task = 2;
		} else if (tmp.equals("ping")) {
			task = 3;
		} else if (tmp.equals("pong")) {
			task = 4;
		} else if (tmp.equals("leave")) {
			task = 5;
		}
		return task;
	}

	public String getIP(String msg) {
		return msg.split(",")[1];
	}

	public int getPort(String msg) {
		return Integer.parseInt(msg.split(",")[2]);
	}
	
	public int getHash(String msg) {
		return Integer.parseInt(msg.split(",")[3]);
	}
}
