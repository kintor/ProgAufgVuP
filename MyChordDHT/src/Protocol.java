/*
 * Das Protokol ist nach folgendem Schema aufgebaut:
 * Task,Absender-IP,Absender-Port,Daten
 * 
 * Task beschreibt die Aufgabe:
 * 		neuer Knoten: 	new -> 0
 * 		Suche:		 	search -> 1
 * 		Stabilization: 	ping -> 2
 * 						pong -> 3
 * 		Austritt:		leave -> 4
 */

public class Protocol {
	// Attribute
	private int task;

	// Konstruktor
	public Protocol() {
	}

	public int evalTask(String msg) {
		String tmp = msg.split(",")[0];
		if (tmp.equals("new")) {
			task = 0;
		} else if (tmp.equals("search")) {
			task = 1;
		} else if (tmp.equals("ping")) {
			task = 2;
		} else if (tmp.equals("pong")) {
			task = 3;
		} else if (tmp.equals("leave")) {
			task = 4;
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
