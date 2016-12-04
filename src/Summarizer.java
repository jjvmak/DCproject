import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
/*
 * Perii Thread-luokan ja toteuttaa summauspalvelun WorkDistributor serverille.
 * Lukee tiedostovirtaa ja laskee summaa calculateSum muuttujaan sek‰ pit‰‰ kirjaa
 * saatujen kokonaislukujen m‰‰r‰st‰ numberSum muuttujaan.
 */
public class Summarizer extends Thread{

	private int port;
	private ServerSocket ss;
	private Socket cs;
	private ObjectInputStream input;
	private int calculateSum = 0;
	private int numberSum = 0;
	private int temp;
	private InputStream is;

	/**
	 * Constructor
	 * @param port
	 */
	public Summarizer(int port){
		this.port = port;
	}

	/**
	 * Checks if the cs socket is connected.
	 * @return
	 */
	public boolean isConnected() {
		//Tarkastaa on cs soketti yhdistetty.
		if (cs.isConnected()) {
			return true;
		}
		else {
			return false;
		}
	}
	public int getPort(){
		return port;
	}
	public int getSum(){
		return calculateSum;
	}
	public int getAmount(){
		return numberSum;
	}

	/**
	 * Initializes TCP connection. After successful connection starts to sum up received integers. 
	 * If zero is received, thread will shut down socket connections and input stream will be closed.
	 */
	@Override
	public void run(){
		//Alustetaan soketit ja muodostetaan TCP-yhteys.
		try{
			ss = new ServerSocket(port);
			cs = ss.accept();
			is = cs.getInputStream();
			input = new ObjectInputStream(is);
		}
		catch (IOException e){
			System.exit(0);
		}
		do {
			try {
				temp = input.readInt(); //Luetaan serverilt‰ saapuvaa tiedostovirtaa.
				if (temp == 0){
					cs.close();
					ss.close();
					input.close();
					break;

				}
				numberSum++; //Pidet‰‰n kirjaa saapuvien kokonaislukujen m‰‰r‰st‰.
				calculateSum += temp; //Summataan saapuvia kokonaislukuja edelliseen summaan.

			} catch (Exception e) {
				System.exit(0);
			}
		} while (true);
	}
}

