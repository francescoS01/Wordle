package server;



public class Utente {

	public String nome;
	public String password;
	public Boolean online = false;
	// parola casuale per il caso in cui utente non abbia mai giocato
	public String lastWord = "zzz"; 

	// statistiche
	Integer partiteVinte = 0;
	Integer partitePerse = 0;
	Integer numTentTot = 0; 
	Integer numPartTot = 0;
	// esito ultima partita
	Boolean esitoUltPart = true; 
	Integer lastStreak = 0;
	Integer bestStreak = 0;
	Integer currentStreak = 0;
	// array per distribuzione
	int[] array = new int[13];

	
	
	// COSTRUTTORE
	Utente(String nome, String password) {

		this.nome = nome;
		this.password = password;

		// Inizializzazione delle distribuzioni con il valore 0
		for (int i = 0; i < array.length; i++) {
			array[i] = 0;
		}

	}
	
	
	
	
	
	// metodo per restituire nome 
	public String getName() {
		return nome;
	}
	
	
	
	
	
	
	// ultima parola con cui ha giocato l'utente
	public String getLastWordUtente() {
		return lastWord;
	}
	
	
	
	
	
	// metodo per restituire password 
	public String getPassword(String nome) {
		return password;
	}
	
	
	
	
	
	// metodo per aggiornare l'ultima parola con cui ha giocato utente 
	public void aggiornaLastWord(String word) {
		lastWord = word;
	}
	
	
	
	
	
	
	// metodo per inserire utente come online
	public void putOnline() {
		online = true;
	}
	
	
	
	
	
	
	// metodo per togliere utente da online
	public void removeOnline() {
		online = false;
	}
	
	
	
	
	
	
	// metodo per aggiornare tutti le varibili di utente ( per statistiche )
	public void aggiornaValori(String word, Integer tentativi, String esito) {

		lastWord = word;
		numTentTot = numTentTot + tentativi;
		numPartTot = numPartTot + 1;

		// caso vittoria
		if (esito.equals("vinto")) {
			partiteVinte = partiteVinte + 1;
			// partite vinte con n tenativi + 1
			array[tentativi] = array[tentativi] + 1;

			// partita prec vinta
			if (esitoUltPart == true) {
				// ho vinto e anche l'ultima l'avevo vinta
				// allora incremento la sequenza di vittore attuale
				currentStreak = currentStreak + 1;
				// se ho superato miglior
				if (currentStreak > bestStreak)
					bestStreak = currentStreak;

			}
			// partita prec persa
			else {
				esitoUltPart = true;
				currentStreak = 1;
				// per caso inziale
				if (currentStreak > bestStreak)
					bestStreak = currentStreak;
			}

		}

		// caso perdita
		else {
			partitePerse = partitePerse + 1;
			// partita prec vinta
			if (esitoUltPart) {
				// se partita precedente vinta e questa persa
				// allora ho "rotto" la sequenza di vittore e aggiorno lastStreak
				lastStreak = currentStreak;

				// adesso aggiorno valori per prossimo streak
				esitoUltPart = false;
				currentStreak = 0;

			}
			// partita prec persa
			// non devo aggiornare niente
		}
	}
	
	
	
	
	
	
	
	// metodo che partendo dalle variabili crea una stringa rappresentante le statistiche 
	public String getStatistics() {

		// genero stringa che mi dice quante partie ho vinto per ogni numero di
		// tentativi
		String s = "\n";

		for (int i = 1; i < 13; i++) {

			if (i == 1)
				s = s + "- partite vinte con " + i + " tentativo: " + array[i] + "\n";

			else
				s = s + "- partite vinte con " + i + " tentativi: " + array[i] + "\n";
		}
		
		// divido in due casi (mai giocato, già giocato) e ottengo due stringhe leggermente diverse 

		if (numPartTot != 0) {

			// genero la percentuale di vittore
			double percVittorie = ((double) partiteVinte / numPartTot) * 100;
			// arrotondo
			long percVittorieArrotondato = Math.round(percVittorie);

			return "\n----- LE TUE STATISTICHE -----" + "\n- Numero partite giocate: " + numPartTot
					+ "\n- Percentaule di vittoria: " + percVittorieArrotondato + "%"
					+ "\n- Lunghezza dell’ultima sequenza continua (streak) di vincite: " + lastStreak
					+ "\n- Lunghezza della massima sequenza continua (streak) di vincite: " + bestStreak + s;
		}
		else 
			return "\n----- LE TUE STATISTICHE -----" + "\n- Numero partite giocate: " + numPartTot
					+ "\n- Percentaule di vittoria: " + "ancora nessuna partita giocata"
					+ "\n- Lunghezza dell’ultima sequenza continua (streak) di vincite: " + lastStreak
					+ "\n- Lunghezza della massima sequenza continua (streak) di vincite: " + bestStreak + s;
	
	}
	
	
}

