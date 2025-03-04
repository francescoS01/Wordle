package client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientMulticast extends Thread {
	
	// Indirizzo e porta 
	private final String indirizzo; // formato String
	InetAddress indirizzoMulticastServer;
	private final int portaMulticastServer;
	
	// idnirizzo del gruppo
	SocketAddress indGruppo;
	
	// interfaccia netowrok
	NetworkInterface Nint;
	
	// lista dove andranno le partite (String)
	private final List<String> listaPartite;
	
	// socket multicast 
	public static MulticastSocket mcClientsocket;

	
	
	// COSTRUTTORE 
	ClientMulticast(int porta, String indirizzo) {
		
		this.portaMulticastServer = porta;
		this.indirizzo = indirizzo;
		listaPartite = Collections.synchronizedList(new ArrayList<>());
    }
	
	
	
	
	
	
	@Override
	public void run() {
		
		try {
			
			// create a multicast client socket
			mcClientsocket = new MulticastSocket(portaMulticastServer); 
			
			// ottengo indirizzo da stringa "indirizzo"
			indirizzoMulticastServer = InetAddress.getByName(indirizzo);
			
			// indirizzo del gruppo (porta + indirizzo IP)
			indGruppo = new InetSocketAddress(indirizzo, portaMulticastServer);
			
			// interfaccia netowork 
			Nint = NetworkInterface.getByInetAddress(indirizzoMulticastServer);
			
			// mi unisco al gruppo
			mcClientsocket.joinGroup(indGruppo, Nint);
			
			// -------------- utente adesso è dentro al gruppo ---------------
			
			while (true) {
				
				// pacchetto dove inserire partite ricevute 
				DatagramPacket pacchetto = new DatagramPacket(new byte[1024], 1024);
				
				// ricevo pacchetto 
				mcClientsocket.receive(pacchetto);
				
				// converto pacchetto in stringa 
				String partita = new String(pacchetto.getData());
				
				// aggiungo partita alla lista 
				listaPartite.add(partita);
			}
			
			
		} catch (ConnectException  e) {
			// TODO Auto-generated catch block
		} 
		catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
	}
	
	
	
	
	
	
	public void showMeSharing() {
		
		// se lista vuota avverto 
		if (listaPartite.isEmpty()) {
			System.out.println("Nessuno ha ancora condiviso niente!\n");
			return;
		}
		
		// stampo tutte la partite scorrendo la lista 
		for (String partita : listaPartite) 
			System.out.println("\n" + partita.trim() );
	}

}


