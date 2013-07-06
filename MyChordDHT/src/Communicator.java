import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class Communicator {
	// Attribute
	private ServerSocket sock;
	private Socket conn;

	private ExecutorService connectionThreadPool;	// baut Verbindungen auf

	// Konstruktor
	public Communicator() {
	}

}
