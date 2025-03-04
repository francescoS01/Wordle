package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;



public class Partita {

	Utente utente;
	String word;
	public static final String words = "./server/resources/words.txt";
	// stringa che verrà creata come partita durante la fase di gico 
	public String partita;
	// mi tiene conto dei numeri dei tantativi 
	public int tent; 

	Partita(Utente utente, String word) {
		
		// ogni volta che viene creato un oggrtto partita gli viene passata la parola attuale
		
		this.utente = utente;
		this.word = word;
		this.partita = "\n\nPartita di " + utente.getName() + ":\n";
		this.tent = 1; 
		
	}

	
	
	
	
	
	// metodo per capire se utente ha già giocato con la parola attuale
	public boolean giaGiocato(Utente utente) {
		// ultima parola con cui ha giocato utente
		String s = utente.getLastWordUtente();
		if (s.equals(word)) {
			return true; 
		}
		return false;
	}

	
	
	
	
	
	
	// metodo per controllo se il tentativo è valido
	public boolean tentativoValido(String wordToCheck) throws FileNotFoundException, IOException {
		// controllo che sia lunga 10 e che non abbiamo spazi vuoti
		if (wordToCheck.length() != 10 || wordToCheck.contains(" "))
			return false;

		// adeeso controllo che appartenga al vocabolario
		// da implemntare
		try (BufferedReader reader = new BufferedReader(new FileReader(words))) {
			
			String line;
			boolean wordFound = false;
            // scorro file e se trovo la parola metto wordfound = true e esco
			while ((line = reader.readLine()) != null) {
				if (line.equalsIgnoreCase(wordToCheck)) {
					wordFound = true;
					break;
				}
			}

			if (wordFound) {
				return true;
			} else {
				return false;
			}
		}

	}

	
	
	
	
	public String suggerimenti(String tentativo) {

		String suggerimenti = "";

		// caso vittoria
		if (tentativo.equals(word)) {
			
			// aggiorno stringa partita
			partita = partita + "tentativo " + tent + ": vittoria\n";
			tent = tent + 1;
			
			return "vinto"; // torno al menù di wordle
		}
		
		
		// array dei caratteri del suggerimento 
		String[] arrCarSugg = new String[10];
		
		// parola che verrà utilizzata per controllo di ? e x
		String wordcheck = word;
		
		// prima individuo tutti i '+' e li tolgo dalla stringa
        for (int i = 0; i < 10; i++) {
        	
        	Character cTent = tentativo.charAt(i);
			Character cWord = word.charAt(i);
			
			if (Character.compare(cTent, cWord) == 0) {
				// se carattere i-esimo di word = carattere i.esimo di tentativo allora => +
				arrCarSugg[i] = "+";
				// adesso tolgo dalla stringa il carattere che ha il ?
				wordcheck = word.substring(0, i) + word.substring(i + 1);
			}
        }
		
        // adesso rimane da vedere per tutti gli altri caratteri se appartengono alla stringa rimasta (wordcheck)

        for (int i = 0; i < 10; i++) {
        	
        	Character cTent = tentativo.charAt(i);
        	String charAsString = String.valueOf(cTent);
        	
        	// se in questa posizione ho già "+" niente altrimenti aggiorno
        	if ( !(arrCarSugg[i] == "+") ) {
        		
        		//controllo se esiste nella parola rimasta 
        		if (wordcheck.contains(charAsString)) 
        			arrCarSugg[i] = "?"; 
        		else 
        			arrCarSugg[i] = "x"; 
        	}
        	
        }
        
        // creo suggerimenti 
        for (int i = 0; i < 10; i++) {
        	suggerimenti = suggerimenti + arrCarSugg[i];
        }
        
      
		// aggiorno stringa partita
		partita = partita + "tentativo " + tent + ": " + suggerimenti + "\n";
		tent = tent + 1;
		
		// invio suggeriumento (caso tentativo valido)
		return suggerimenti;
	}
	
	
	
	
	
	
	// metodo per inviare la partita appena giocata 
	public String getPartita() {
		return partita;
	}
	
}
