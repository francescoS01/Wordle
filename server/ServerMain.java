package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Properties;
import java.util.concurrent.*;


public class ServerMain {
	
	
	// numero porta di ascolto del Server (da file configurazione)
	private static int porta; 
	
	// indirizzo e porta gruppo multicast (da file configurazione)
	private static String indirizzoMulticast;
	private static int portaMulticast;
	
	private static int maxDelay;
	
	// Percorso del file di configurazione del server
	public static final String configFile = "./server/resources/server.properties";

	
	
	public static void main(String[] args) throws IOException {
		
		// Leggo il file di configurazione.
		readConfig();
		
		try (ServerSocket listener = new ServerSocket(porta)) {
			
			System.out.println("The server is running...");
			
			// creo l'oggetto CentralServer che verrà passata tutti i thread per riferimento 
			CentralServer centralserver = new CentralServer();
			
			// pool di 20 thread,20 possibili sessioni in contemporanea
			ExecutorService pool = Executors.newFixedThreadPool(20);
			
			// Avvio l'handler di terminazione.
			Runtime.getRuntime().addShutdownHook(new TerminationHandler(maxDelay, pool, listener, centralserver));
						
			// lancio il pool di thread 
			while (true) {
				pool.execute(new Worker(listener.accept(), centralserver, indirizzoMulticast, portaMulticast ));
			}
		} catch (SocketException e) {
		    // Gestione dell'eccezione SocketException
		}

	}
	
	
	
	
	
	
	// metodo per la letture del file properties
	public static void readConfig() throws FileNotFoundException, IOException, NumberFormatException {
		InputStream input = new FileInputStream(configFile);
		Properties prop = new Properties();
		prop.load(input);
		porta = Integer.parseInt(prop.getProperty("port"));
		indirizzoMulticast = prop.getProperty("indirizzoMulticast");
		portaMulticast = Integer.parseInt(prop.getProperty("portaMulticast"));
		maxDelay = Integer.parseInt(prop.getProperty("maxDelay"));
		
		input.close();
	}
}



