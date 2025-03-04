package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.*;
import com.google.gson.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class CentralServer {

    // hashMap di chiave nome utente e valore utente 
	HashMap<String, Utente> HMUtenti = new HashMap<>();

	// parola con cui si gioca che cambierà periodicamente
	String word;

	// vocabolario
	public static final String words = "./server/resources/words.txt";

	// variabile che mi dice quando è stata generata l'ultima parola
	// questa viene cambiata ogni volta che viene generata una nuova parola
	long startTimeWord;

	// ogni 24h dovrò cambiare la variabile
	long fixedTime = 86400000;

	
	
	
	// COSTRUTTORE
	CentralServer() throws FileNotFoundException, IOException {

		// controllo se devo recuperare dati dal file utenti
		File jsonFileUtenti = new File("./server/resources/utenti.json");
		if (jsonFileUtenti.exists()) {
			recuperaUtenti(jsonFileUtenti);
		}

		// in questo file recupero l'ultima parola in utilizzo e il
		// momento in cui è stata generata (prima che server si interremposse)
		File jsonFileWord = new File("./server/resources/word.json");
		if (jsonFileWord.exists()) {
			recuperaWord(jsonFileWord);
		}

		// chiamo metodo che provvede a genereare nuova parola ogni 24h
		generateNewWord();

	}
	
	
	
	
	
	
    // metodo che controlla se uno username è già esistente o no
	public synchronized boolean ExistsUserName(String nomeUt) {
		Utente utente = HMUtenti.get(nomeUt);
		if (utente == null)
			return false;
		return true;
	}
	
	
	
	
	
	
	// metodo per aggiungere un nuovo utente tra i registrati 
	public synchronized void aggiungiUtente(Utente utente) {
		HMUtenti.put(utente.getName(), utente);
	}
	
	
	
	
	
	
	// metodo che verifica se la password associata ad un utente è corretta (per login)
	public synchronized boolean verificaPassword(String nomeUt, String passwordUt) {
		Utente utente = HMUtenti.get(nomeUt); // utente di nome "nomeUT"
		// controllo se la password dell'utente di nome "nomeUT" è uguale a quella
		// ricevuta
		if ((utente.getPassword(nomeUt)).equals(passwordUt)) {
			return true;
		}
		return false;
	}
	
	
	
	
	
	
	// metodo per capire se utente è gia online (per login)
	public synchronized boolean utenteOnline(String nomeUt) {
		Utente utente = HMUtenti.get(nomeUt); // utente di nome "nomeUT"
		if (utente.online == false)
			return false;
		return true;
	}
	
	
	
	
	
	
	// metodo per ottenere utente
	public synchronized Utente getUtente(String nomeUt) {
		Utente utente = HMUtenti.get(nomeUt);
		return utente;
	}
	
	
	
	
	
	
	// metodo per restituire la parola attuale con cui giocare
	public synchronized String getWord() {
		return word;
	}
	
	
	
	
	
	
	// metodo che lancia periodicamente un thread che andrà a generare nuova parola
	public void generateNewWord() {

		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		// unico metodo del thread che sarà = chooseWord()
		final Runnable chooseWordTask = new Runnable() {
			public void run() {
				try {
					chooseWord();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}

		};

		// tempo rimasto
		long tempoRimasto = 0;
		long now = System.currentTimeMillis();

		if (startTimeWord + fixedTime > now) {
			// non è ancarrivato il momento di cambiare parola
			// clacolo il tempo rimanente per arrivare a 24h
			tempoRimasto = startTimeWord + fixedTime - now;

		}
		// se da quando ho generato parola + 24h < now allora significa che
		// devo cambiare parola adesso e quindi lascio tempoRimasto = 0

		scheduler.scheduleAtFixedRate(chooseWordTask, tempoRimasto, fixedTime, TimeUnit.MILLISECONDS);

	}
	
	
	
	
	
	
	// metodo che genera una nuova parola 
	public void chooseWord() throws FileNotFoundException, IOException {

		// Genera un numero intero casuale compreso tra 0 e 30823
		Random random = new Random();
		int randomNumber = random.nextInt(30823) + 1; // +1 perchè righe non partono da 0

		// per controllare quando la riga corrisponde al numero random
		int cont = 1;
		String line;

		try (BufferedReader reader = new BufferedReader(new FileReader(words))) {

			while ((line = reader.readLine()) != null) {
				if (cont == randomNumber)
					break;
				cont = cont + 1;
			}
		}

		// adesso la nuova parola da indovinare è quella stata scelta casualemnte
		word = line;
		// avvendo generato nuova parola aggiorno startTimeWord
		startTimeWord = System.currentTimeMillis();
	}
	
	
	
	
	
	
	// metodo per il recupero delle inrfomazioni utenti da file json 
	public void recuperaUtenti(File jsonFileUtenti) {

		try (FileReader fileReader = new FileReader(jsonFileUtenti);
				JsonReader jsonReader = new JsonReader(fileReader)) {

			Gson gson = new Gson();
			HMUtenti = gson.fromJson(jsonReader, new TypeToken<HashMap<String, Utente>>() {
			}.getType());

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	
	
	
	
	// metodo per il recupero delle inrfomazioni (startWord e word) da file json
	public void recuperaWord(File jsonFile) throws FileNotFoundException, IOException {

		try (JsonReader jsonReader = new JsonReader(new BufferedReader(new FileReader(jsonFile)))) {

			// oggetto json che utilizzo per leggere da file
			jsonReader.beginObject();

			while (jsonReader.hasNext()) {

				String name = jsonReader.nextName();

				if ("word".equalsIgnoreCase(name)) {
					word = jsonReader.nextString();
				} else if ("startTimeWord".equalsIgnoreCase(name)) {
					startTimeWord = jsonReader.nextLong();
				}
			}

		}

	}
	
	
	
	
	
	
	// metodo per salvataggio delle informazioni 
	public void saveServer() {

		// metto tutti utenti offline
		// Scorrere la mappa utilizzando il ciclo for-each
		for (Entry<String, Utente> entry : HMUtenti.entrySet()) {
			// String key = entry.getKey();
			Utente utente = entry.getValue();
			utente.removeOnline();
		}

		// Creazione dell'oggetto Gson
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// Serializzazione della mappa in formato JSON come stringa
		String jsonString = gson.toJson(HMUtenti);

		// Creazione del file e scrittura del contenuto JSON
		try (FileWriter writer = new FileWriter("./server/resources/utenti.json")) {
			writer.write(jsonString);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// adesso salvo su un altro file json word e startTimeWord
		try (JsonWriter jsonWriter = new JsonWriter(new FileWriter("./server/resources/word.json"))) {

			jsonWriter.beginObject();

			// Aggiungi le proprietà e i valori all'oggetto JSON
			jsonWriter.name("word").value(word);
			jsonWriter.name("startTimeWord").value(startTimeWord);

			jsonWriter.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}


