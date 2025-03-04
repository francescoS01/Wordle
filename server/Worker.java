package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
//import java.net.SocketException;

public class Worker implements Runnable {

	// Socket e stream per la comunicazione con il client.
	private Socket socket;
	DataInputStream in; 
	DataOutputStream out; 

	// indirizzo multicast e numero porta multicast
	private final InetAddress indirizzoMulticast;
	private int portaMulticastSocket;

	// oggetto condiviso da tutti i thread WordleServer
	CentralServer centralserver;

	// la parola verrà ottenuta nel caso utente vorrà giocare richiedendola a centralserver
	public String word;

	// utente che verrà creato dopo nella registrazione
	public Utente utente = null;

	
	
	// COSTRUTTORE
	Worker(Socket Socket, CentralServer centralserver, String indirizzoMulticast, int portaMulticast)
			throws IOException {

		this.socket = Socket;
		this.centralserver = centralserver;
		this.indirizzoMulticast = InetAddress.getByName(indirizzoMulticast);
		this.portaMulticastSocket = portaMulticast;
	}

	
	
	
	
	
	public void run() {
		try {
			// dove leggerò dati
			in = new DataInputStream(socket.getInputStream()); 
			// dove scriverò dati
			out = new DataOutputStream(socket.getOutputStream()); 
			
			String scelta; 
			boolean end = false; // controllo while

			while (!end) {

				scelta = in.readUTF();

				// caso 1 utente deve registrarsi
				if (scelta.equals("1")) {
					registrazione(in, out);
					continue;
				}

				// caso 2 utente deve fare login
				if (scelta.equals("2")) {
					login(in, out);
					continue;

				}

				// caso 0 utente vuole uscire 
				if (scelta.equals("0")) {
					break;
				}

			}

		} catch (IOException e) {
			System.err.println("connessione interrotta con un utente");
		} finally {

			// quando client esce (in qulunque modo esca) deve essere messo offline 
			if (utente != null) {
				utente.removeOnline();
			}

			// chiudo socket
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	} // fine metodo run

	
	
	
	
	
	
	// metodo per la gestione della REGISTRAZIONE
	public void registrazione(DataInputStream in, DataOutputStream out) throws IOException {

		// utente mi invia il suo username come prima cosa

		boolean existName = true;
		String username = null;
		String password = null;

		while (existName) {

			// questo è il caso in cui in realtà non invia un username ma vuole tornare
			// indietro
			username = in.readUTF();

			if (username.equals("back"))
				return;

			// se non entro dentro if utente mi sta proponendo un possibile nome utente
			// controllo in centralserver se esiste gia questo nome, se si continuo a
			// chiedere all'utente
			existName = centralserver.ExistsUserName(username);
			if (existName == false) {
				out.writeUTF("ok"); // se non esiste questo nome invio ok a client
			} else
				out.writeUTF("no"); // se esiste questo nome invio no a client

			// se unicName = false esco dal while, non esco altrimenti
		}

		// adesso aspetto che mi invii la password
		password = in.readUTF();
		// questo è il caso in cui in realtà non invia una password ma vuole tornare
		// indietro
		if (password.equals("back"))
			return;

		// ho ricevuto password adesso creo un nuovo utente
		utente = new Utente(username, password);
		// adesso che ho un nuovo utente posso aggiungerlo in centralServer
		centralserver.aggiungiUtente(utente);
		out.writeUTF("registrazione avvenuta con successo!");

		// adesso entro dentro la fase di login
		login(in, out);
		return;
	}

	
	
	
	
	
	
	// metodo per la gestione del LOGIN e fase dentro WORDLE
	public void login(DataInputStream in, DataOutputStream out) throws IOException {

		// login, utente mi invia il suo nome utente come prima cosa

		String username = null;
		String password = null;
		boolean exist;
		boolean correctPassword;

		// ciclo finchè non mi viene inviato un user name esistente o utente decida di
		// uscire
		while (true) {

			username = in.readUTF(); // ricevo username
			// questo è il caso in cui in realtà non invia un username ma vuole tornare
			// indietro
			if (username.equals("back"))
				return;

			// ho ricevuto username, controllo in centralserver se esiste gia questo nome,
			// se si allora procedo con password
			exist = centralserver.ExistsUserName(username); // esiste nome utente?
			if (exist == true) {
				if (!(centralserver.utenteOnline(username))) { // utente già online?
					out.writeUTF("ok"); // se utente non è gia online e nome utente esiste invio ok
					break;
				} else
					out.writeUTF("no"); // caso in cui nome utente esiste ma utente è online
			} else
				out.writeUTF("no"); // caso in cui non esiste quel nome utente
		}

		// ciclo ficnhè non mi viene inviata password corretta o utente decida di uscire
		while (true) {

			password = in.readUTF();
			// questo è il caso in cui in realtà non invia una password ma vuole tornare
			// indietro
			if (password.equals("back"))
				return;

			// ho ricevuto una password
			correctPassword = centralserver.verificaPassword(username, password);
			if (correctPassword == true) {
				out.writeUTF("ok"); // se password è corretta invio ok a client
				break;
			} else
				out.writeUTF("no"); // password errata invio no e aspetto nuovo tentativo
		}

		// ho ottenuto username e passsword corretta, prendo utente
		utente = centralserver.getUtente(username);

		out.writeUTF("======== Benvenuto in WORDLE! ========");

		// --------- adesso sono online dentro wordle --------------

		// aggiorno l'utente come online
		utente.putOnline();

		// adesso che ha fatto il login utente è dentro woerdle e può decidere cosa
		// fare, 5
		// possibilità

		while (true) {

			String scelta = in.readUTF(); // ricevo scelta

			// caso scelta = 1 utente vuole giocare
			if (scelta.equals("1")) {
				gioca(in, out);
				continue;
			}

			// caso scleta = 2 utente vuole vedere le statistiche
			if (scelta.equals("2")) {
				statistiche(in, out);

			}

			// caso scleta = 0 utente vuole uscire
			if (scelta.equals("0")) {
				break;
				// in questo caso devo chiudere la socket
				// esco dal while, esco da LOGIN e in run chiudo socket
			}
		}

		// utente è uscito quindi lo metto offline
		utente.removeOnline();
		return;
	} // fine metodo login

	
	
	
	
	
	
	// meotodo per la gestione della fase di GIOCO
	public void gioca(DataInputStream in, DataOutputStream out) throws IOException {

		// ottengo la parola da CentralServer => quindi parola attuale!
		word = centralserver.getWord();

		// creo oggetto parita passandogli l'utente che gioca e la parola attuale
		Partita partita = new Partita(utente, word);

		// client aspetta che gli dia la conferma per giocare
		// in partita controllo se ho già giocato con questa parola
		if (partita.giaGiocato(utente) == true) {
			out.writeUTF("no"); // se utente ha gia giocato dico a client di no
			return;
		}

		// se utente non ha già giocato allora può iniziare la partita
		out.writeUTF("ok"); // client può giocare

		// client inizia ad inviare i suoi tentativi
		Integer end = 1;
		String tentativo;

		// ciclio finchè non finiscono i tentativi o utente indovini la parola
		while (end <= 12) {
			tentativo = in.readUTF();
			String suggerimenti;
			// controllo che il tetativo sia valido
			if (partita.tentativoValido(tentativo)) {
				out.writeUTF("ok"); // tentativo valido dico ok a utente

				suggerimenti = partita.suggerimenti(tentativo);

				// se partita mi dice che ho vinto allora lo comunico a client
				if (suggerimenti.equals("vinto")) {
					out.writeUTF("\n!!!  HAI VINTO  !!! \n    COMPLIMENTI ");
					break;
				}

				// altrimenti partita mi restituisce dei suggerimenti da inviare a client
				out.writeUTF(suggerimenti);
			}
			// se non è valido il tetativo allota invio no
			else {
				out.writeUTF("no");
				continue; // salto end+1 (non considero il tentativo)
			}

			end = end + 1; // end verrà aggiornato solo quando tenativo valido
		}

		// caso perdita
		if (end == 13) {
			utente.aggiornaValori(word, end, "perso");
		}
		// caso vittoria
		else
			utente.aggiornaValori(word, end, "vinto");

		// adesso mi verrà detto se voglio condividere o no la partita
		if (in.readUTF().equals("cond")) {
			inviopartita(partita);
		}
		// parto con condivisione
		// altrimenti non faccio niente

		return;

	} // fine metodo gioca
	
	
	
	
	
	
	
	// metodo per l'invio delle statistiche
	public void statistiche(DataInputStream in, DataOutputStream out) throws IOException {
		String s = utente.getStatistics();
		out.writeUTF(s);
	}

	
	
	
	
	
	// metodo per l'invio della partita sotto forma di stringa
	public void inviopartita(Partita partita) throws IOException {

		// utente vuole condividere
		try (DatagramSocket socket = new DatagramSocket()) {

			// chiedo a all'oggetto partita la parita sotto forma di stringa
			String partitaString = partita.getPartita();

			// creazione pacchetto da inviare in multicast
			byte[] partitabyte = partitaString.getBytes();

			DatagramPacket pacchetto = new DatagramPacket(partitabyte, partitabyte.length, indirizzoMulticast,
					portaMulticastSocket);

			// invio risultato in multicast
			socket.send(pacchetto);
		}
	}

}

