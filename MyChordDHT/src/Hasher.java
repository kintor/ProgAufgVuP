import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Hasher {
	// Attribute
	private MessageDigest hasher;
	
	//Konstruktor
	Hasher (String algorithm) {
		try {
			this.hasher = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Hash-Funktion ist nicht verf�gbar");
			e.printStackTrace();
		}
	}
	
	

}
