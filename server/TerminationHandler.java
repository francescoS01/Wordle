package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class TerminationHandler extends Thread {
	
	private int maxDelay;
	private ExecutorService pool;
	private ServerSocket serverSocket;
	private CentralServer centralserver;
	
	public TerminationHandler(int maxDelay, ExecutorService pool, ServerSocket serverSocket, CentralServer centralserver) {
		this.maxDelay = maxDelay;
		this.pool = pool;
		this.serverSocket = serverSocket;
		this.centralserver = centralserver;
	}
	
	public void run() {
		// Avvio la procedura di terminazione del server.
        System.out.println("[SERVER] Avvio terminazione...");
        // Chiudo la ServerSocket in modo tale da non accettare piu' nuove richieste.
        try {serverSocket.close();}
        catch (IOException e) {
        	System.err.printf("[SERVER] Errore: %s\n", e.getMessage());
        }
        
        // METODO DEL CENTRAL SERVER per salvataggio
        // qui verranno salvati utenti e word (la parola da indovinare)
        centralserver.saveServer();
        
        // Faccio terminare il pool di thread.
        pool.shutdown();
	    try {
	        if (!pool.awaitTermination(maxDelay, TimeUnit.MILLISECONDS)) 
	        	pool.shutdownNow();
	    } 
	    catch (InterruptedException e) {pool.shutdownNow();}
        System.out.println("[SERVER] Terminato.");
	}

}
