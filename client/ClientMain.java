package client;


import java.util.Properties;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class ClientMain {
	
	


	// percorso file di configurazione Client
	private static final String configFile = "./client/resources/client.properties";
	

	// port e hostName per connessione TCP (readConfig())
	public static int port;
	public static String hostname;
	
	// oggeot che si preoccupa della condivisione/stampa delle parite 
	public static ClientMulticast gruppomulticast;
	
	// porta e indirizzo per connessione UDP del gruppo multicast (readConfig())
	private static String indirizzoMulticast;
	private static int portaMulticastSocket;
	
	
 
	
	public static void main(String[] args) throws IOException {
		
		// Leggo il file di configurazione.
		readConfig();
		
		try (Socket socket = new Socket(hostname, port);
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				Scanner scanner = new Scanner(System.in)) {
			
		
			// ------- adesso posso avere comunicazione TCP tramite socket --------

			boolean end = false;
			
			while (!end) {

				System.out.println("\nDigitare il numero della scelta:\n [1] Registrazione\n [2] Login\n [0] Exit");
				String line = scanner.nextLine();

				// divido in 3 casi

				// caso 1 registrazione
				if (line.equals("1")) {
					// invio 1 per dire che mi devo registrare
					out.writeUTF("1");
					registrazione(in, out, scanner);
					continue; // mi serve per slatare le cose fuori dagli if
				}

				// caso 2 login
				// dentro login chiamerò la funzione gioca
				if (line.equals("2")) {
					// invio 2 perchè devo fare il login
					out.writeUTF("2");
					login(in, out, scanner);
					continue;
				}

				// caso 3 exit
				if (line.equals("0")) {
					// invio 3 perchè voglio uscire
					out.writeUTF("0");
					System.out.println("a presto!");
					chiudi(in, out, scanner, gruppomulticast);
					return; 
				}

				// caso inserimento sbagliato
				System.out.println("non abbiamo capito la tua scelta, riprova");

			} // fine while

		} catch (ConnectException e) {
			System.out.println("\nErrore connessione server: " + e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			System.err.println("connessione persa");
		} 

	} // fine main
	
	
	
	
	
	
	// metodo per la letture del file properties
	public static void readConfig() throws FileNotFoundException, IOException {
		InputStream input = new FileInputStream(configFile);
		Properties prop = new Properties();
		prop.load(input);
		hostname = prop.getProperty("hostname");
		port = Integer.parseInt(prop.getProperty("port"));
		indirizzoMulticast = prop.getProperty("indirizzoMulticast");
		portaMulticastSocket = Integer.parseInt(prop.getProperty("portaMulticast"));
		
		input.close();
	}
	
	
	
	
	
	
	// metodo per la chiusura 
	public static void chiudi(DataInputStream in, DataOutputStream out, Scanner scanner, ClientMulticast gruppomulticast) throws IOException {
		
		// controllo se client ha già avuto una connessione con server 
		if (in != null)
			in.close(); 
		if (out != null)
			out.close();
		if (scanner != null)
			scanner.close();
		
		// solo se chiamato dopo il login
		if (gruppomulticast != null) 
			// stop thread gestione condivisioni
			gruppomulticast.interrupt();
		
		return;
	}
	
	
	
	
	
	
	
	// metodo che gestisce la REGISTRAZIONE 
	public static void registrazione(DataInputStream in, DataOutputStream out, Scanner scanner) throws IOException {

		boolean unicName = false;
		String line = null; // per legegre da console
		String risposta; // per leggere risposte dal server

		// finchè non trovo un username già esistente continuo a provare
		while (!unicName) {
			boolean nonCorretta = true;
			
			// ciclo finchè non mi viene dato un formato di nome utente valido o esco (0)
			while (nonCorretta) {
				System.out.println("\nInseirisci un nome utente senza spazi e che contenga almeno 5 caratteri\nDigita 0 per tornare al menù iniziale");
				line = scanner.nextLine();
				if (line.equals("0")) {
					out.writeUTF("back");
					return; //torno al menù iniziale
				}
				// nonCorretta diventerà falsa quando sarà senza spazi, e lunga almeno 5 => esco dal while 
				nonCorretta = line.contains(" ") || line.length() < 5 ;
				if (nonCorretta) {
					System.out.println("\nNome utente non valido");
				}
			}
			
			// invio username con giusto formato al server
			out.writeUTF(line);
			risposta = in.readUTF();
			if (risposta.equals("ok")) {
				unicName = true;
				break;
			}

			// se non ricevo ok significa che quel nome è già esistente e quindi riparto con il ciclio
			System.out.println("ci spiace questo nome utente è già utilizzato");
		}

		// fuori dal while ho trovato un giusto user name, posso creare password
		boolean nonCorretta = true;
		while (nonCorretta) {
			System.out.println("\nInseirisci una password senza spazi e che contenga almeno 5 caratteri\nDigita 0 per tornare al menù iniziale");
			line = scanner.nextLine();
			if (line.equals("0")) {
				out.writeUTF("back");
				return; //torno al menù iniziale
			}
			// nonCorretta diventerà falsa quando password sarà senza spazi, e lunga almeno 5
			nonCorretta = line.contains(" ") || line.length() < 5 ;
			if (nonCorretta) {
				System.out.println("\nPassword non valida");
			}
		}
		
		// adesso che ho individuato una password valida la invio al server
		out.writeUTF(line);
		// stampo la conferma che è stato registrato con successo
		risposta = in.readUTF();
		System.out.println("\n" + risposta);
		
		// adesso entro direttamente nella fase di login 
		login(in, out, scanner);
		
		return;
	}
	
	
	
	
	
	
	
	// metodo che gestisce il LOGIN
	public static void login(DataInputStream in, DataOutputStream out, Scanner scanner) throws IOException {

		String line = null;
		String risposta;

		// finchè non mando un username esistente continuo ciclo
		while (true) {
			System.out.println("\nIserisci il tuo nome utente per accedere \nDigita 0 per tornare al menù iniziale");
			line = scanner.nextLine(); // per legegre da console
			// caso in cui voglia tornare indietro
			if (line.equals("0")) {
				out.writeUTF("back");
				return; //torno al menù iniziale
			}
			
			// invio nome utente
			out.writeUTF(line);
			
			// ricevo riscontro ok se usernname esistente, no altirmenti
			risposta = in.readUTF();
			if (risposta.equals("ok"))
				break;
			// se non entro dentro if allora nome utente inesistente
			System.out.println("\nNome utente inesistente o utente già online");
		}

		// ho un nome utente esistente adesso controllo password
		// ciclio finchè non ho password corretta o non voglia uscire
		while (true) {
			System.out.println("\nIserisci la tua password per accedere \nDigita 0 per tornare al menù iniziale");
			line = scanner.nextLine();
			
			// caso in cui voglia tornare indietro
			if (line.equals("0")) {
				out.writeUTF("back");
				return; //torno al menù iniziale
			}
			// invio password
			out.writeUTF(line);
			// ricevo riscontro ok se password valida, no altrimeti
			risposta = in.readUTF();
			if (risposta.equals("ok"))
				break;
			System.out.println("\nPassword non valida");
		}

		// stampo la conferma che è stato loggato con successo
		risposta = in.readUTF();
		System.out.println("\n" + risposta);
		
		// creo oggetto gruppomulticast, del quale chiamerò metodo per mostrare partite
		gruppomulticast = new ClientMulticast(portaMulticastSocket, indirizzoMulticast);
		
		// creo thread che prende oggetto appena creato 
		try {
			Thread  gruppomulticastthread = new Thread(gruppomulticast); 
			// faccio runnare il thread
			gruppomulticastthread.start();
		
		}catch (Exception e) {
			System.err.println("ci spiace");
		} 
		
		
		// ------- utente dentro al gruppo ---------
		
		// adesso entro dentro il menù di wordle 
		// passo in out e gruppomulticast 
		wordle(in, out, gruppomulticast, scanner);
		
		// ho fatto return in wordle significa che utente vuole fare logout
		// allora faccio return in modo da tornare all'interno del main
		// dove mi verrà chiesto se voglio accedere, registrarmi o uscire. 
		
		return;
	}
	
	
	
	
	
	
	
	// metodo che gestisce la fase all'interno di WORDLE
	public static void wordle(DataInputStream in, DataOutputStream out, ClientMulticast gruppomulticast, Scanner scanner) throws IOException {

		while (true) {
			
		
			System.out.println("\nDigitare il numero della scleta: "
					+ "\n[1] Inizia a giocare "
					+ "\n[2] Ottieni statistiche "
					+ "\n[3] Mostra bacheca risultati condivisi "
					+ "\n[0] Esci e torna al menù iniziale");
			String line = scanner.nextLine();

			// inizia a giocare
			if (line.equals("1")) {
				out.writeUTF(line); // avverto server che voglio giocare
				gioca(in, out, scanner);
			}

			// ottieni statistiche
			if (line.equals("2")) {
				out.writeUTF(line);
				statistiche(in, out);
			}
          
			// mostra partite degli utenti
			if (line.equals("3")) {
				out.writeUTF(line);
				gruppomulticast.showMeSharing();
			}

			// logout
			if (line.equals("0")) {
				out.writeUTF(line);
				// esco da wordle e torno nella funzione chiamante -> login 
				return; 
			}
			
		}

	} 
	
	
	
	
	
	
	
	// metodo che gestisce la fase di GIOCO 
	public static void gioca(DataInputStream in, DataOutputStream out, Scanner scanner) throws IOException {
		
		String line;
		String risposta;
		String suggerimenti; 
	
		
		// mi viene detto se posso giocare o no (quindi se ho gia giocato con la parola attuale)
		String gioco = in.readUTF();
		if (gioco.equals("ok")) {
            
			// inizia la partita
			System.out.println( "\nInserire parole lunghe esattamente 10 caratteri");
			
			int numTentativi = 1;
			while (numTentativi <= 12) {
				System.out.println( "tentativo numero " + numTentativi +":\n" ); 
				line = scanner.nextLine(); // per legegre da console
				// invio la parola 
				out.writeUTF(line);
				
				// server mi risponderà dicendomi se è valida
				risposta = in.readUTF(); 
				
				// se è valido aspetto una seconda risosta dal server, suggerimenti
				if (risposta.equals("ok")) {
					suggerimenti = in.readUTF(); 
					System.out.println( suggerimenti + "\n"); 
					
					// caso vittoria
					if (suggerimenti.equals("\n!!!  HAI VINTO  !!! \n    COMPLIMENTI "))
						break; //esco dal while  
					
				}
				// se non è valido allora faccio un continue 
				else {
					System.out.println("Tentiativo non valido\n"); 
					continue; 
				}
				
				numTentativi = numTentativi + 1; // aumneto numeri dei tantivi solo se valido
			}
			
			// se esco dal while ho perso 
			if (numTentativi == 13)
				System.out.println("\nChe peccato, hai perso");
			

			// (partita finita) chiedo a utente se vuole condividere risulatati
			System.out.println("\nDigitare 1 per condividere la partita, qualsiasi altro tasto altrimenti");
			line = scanner.nextLine();
			if (line.equals("1")) 
				out.writeUTF("cond");
			
			else 
				out.writeUTF("NoNcond");
			return;
			
			

		} 
		// se invece server mi dice "no"
		else {
			System.out.println("\nHai già giocato, attendi che esca una nuova parola");
			return;
		}
	}
	
	
	
	
	
	
	
	// metodo per la stampa delle STATISTICHE
	public static void statistiche(DataInputStream in, DataOutputStream out) throws IOException {
		
		// stampo semplicemnte la "stringa statistiche"
		System.out.println(in.readUTF());
		return;
	}
	

}










