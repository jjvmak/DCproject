
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/*
 * L‰hett‰‰ datagrampaketin WorkDistributor serville, jonka j‰lkeen avaa TCP-yhteyden.
 * Alustaa ja k‰ynnist‰‰ vaaditun m‰‰r‰n summauspalveluita.
 * Kuuntelee serverilt‰ saapuvia kyseilyit‰ vastaten niihin.
 */
public class Connections {

	static int targetPort;
	static DatagramPacket packet;
	static DatagramSocket datagramSocket;
	static InetAddress targetAdd;
	static byte[] data;
	static Socket cs;
	static ServerSocket ss;
	static OutputStream oS;
	static InputStream iS;	
	static ObjectOutputStream output;
	static ObjectInputStream input;
	static String portNumber;
	static int streamInput = 0;
	static Summarizer[] summarizers;
	static Thread[] threads;
	static int connectionAttemp = 01;
	static boolean connection = false;

	/**
	 * Initializes necessary variables for UDP and TCP connection. 
	 * Tries to connect WorkDistributor server and after five failed attempts program will shut down. 
	 * @throws IOException
	 */
	public void init() throws IOException {
		try { 
			targetAdd = InetAddress.getLocalHost(); //Kohdeosoitteeseen alustetaan localhost osoite.
			targetPort = 3126; 
			portNumber = "6000";
			data = portNumber.getBytes(); //Enkoodataan merkkijono biteiksi datagrampakettia varten.
			packet = new DatagramPacket(data, data.length, targetAdd, 3126); //Muodostetaan datagrampaketti.
			datagramSocket = new DatagramSocket(); //Muodostetaan datagramsoketti.
			ss = new ServerSocket(6000); //Muodostetaan serversoketti ja annetaan parametriksi porttinumero 6000.

			while(connectionAttemp <= 5){ 
				System.out.println("Sending datagram packet to server. Attemp: "+connectionAttemp);
				sendDatagram(); //L‰hetet‰‰n datagrampaketti serverille.
				try {
					ss.setSoTimeout(5000); //Asetetaan serverisoketin timeout.
					acceptTCP(); //Hyv‰ksyt‰‰n TCP-yhteys k‰ytt‰en kyseist‰ metodia.
					connection = true;
					if(cs.isConnected()) break;
				} catch (SocketTimeoutException e) {
					connectionAttemp++;
					System.out.println("No connection to server");
					continue;
				}
			}
			if(!connection) System.exit(0);

			setupStreams(); //Alustetaan input- ja outputstreamit.
			readInput(); //Luetaan tiedostovirtaa inputstreamista.
			summarizerService(); //Luodaan ja k‰ynnistet‰‰n summaajapalvelut.
			listener(); //K‰ynnistet‰‰n kuuntelu serverin testej‰ varten.

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends datagram packet to WorkDistributor server which contains port number which will be used to TCP connection.
	 */
	public void sendDatagram() {
		try {
			System.out.println(packet);
			datagramSocket.send(packet); //L‰hett‰‰ datagrampaketin serverille.
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes ObjectInputStream and ObjectOutputStream.
	 */
	public void setupStreams(){
		try {
			iS = cs.getInputStream();
			oS = cs.getOutputStream();
			output = new ObjectOutputStream(oS); //Muodostetaan ja alustetaan outputstream. Annetaan parametriksi socketilta saatu output.
			input = new ObjectInputStream(iS); //Muodostetaan ja alustetaan inputstream. Annetaan parametriksi socketilta saatu input.
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Accepts incoming connection to designated socket. 
	 * @throws IOException
	 */
	public void acceptTCP() throws IOException{
		cs = ss.accept(); //Hyv‰ksyt‰‰n TCP-yhteys.
	}

	/**
	 * Reads the output from WorkDistributor. If timeout occurs, client will answer with -1 and after that program will shut down.
	 * If the input is not between 2 and 10, program will shut down.
	 * @throws Exception
	 */
	public void readInput() throws Exception{
		try {
			streamInput = input.readInt(); //Luetaan serverilt‰ saapuvaa tiedostovirtaa.
			System.out.println("Reading input. Received: "+streamInput);

		}catch (SocketTimeoutException e){
			//Timeoutin sattuessa kirjoitetaan tiedostovirtaan -1 ja suljetaan ohjelma.
			output.writeInt(-1); 
			output.flush();
			System.exit(0);		
		}catch (IOException e) {
			e.printStackTrace();
		}
		if(streamInput < 2 || streamInput > 10){
			System.exit(0);
		}
	}

	/**
	 * Listens incoming inquiries from WorkDistributor. If input receives 0, the program will shut down.
	 * Inquiry 1. writes total sum of the received integers to the output.
	 * Inquiry 2. writes the index number which contains largest integer to the output.
	 * Inquiry 3. writes total number of integers received to the output.
	 * @throws SocketException
	 */
	public static void listener() throws SocketException{
		//Metodi kuuntelee serverilt‰ tiedostovirtaan saapuvia kyselyit‰.
		ss.setSoTimeout(60000); //Asetetaan serversoketin timeout.
		while (true) {
			try {
				int num = input.readInt();

				if(num == 0){
					closeConnections();
				}
				else if(num == 1){
					//System.out.println("Reading total sum from summarizers");
					//System.out.println("koko summa: " + readTotalSum());
					//System.out.println();
					output.writeInt(readTotalSum());
					output.flush();
				}
				else if(num == 2){
					//System.out.println("Reading largest sums from summarizers");
					//System.out.println("Koko indeksi m‰‰r‰: " + readMaxIndex());
					//System.out.println();
					output.writeInt(readMaxIndex());
					output.flush();
				}
				else if(num == 3){
					//System.out.println("Reading amount of received integers");
					//System.out.println("Suurin summa summarizer: " + readIntegerAmount());
					//System.out.println();
					output.writeInt(readIntegerAmount());
					output.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initializes required amount of Summarizer threads with port numbers starting at 6001.
	 * Writes port numbers to the output stream for WorkDistributor.
	 */
	public static void summarizerService(){
		summarizers = new Summarizer[streamInput]; 
		threads = new Thread[streamInput];
		try {
			
			//Luodaan ja alustetaan serverin vaatima m‰‰r‰ summauspalveluita.
			for(int i=0; i<streamInput; i++){
				summarizers[i] = new Summarizer(6001+i);
				threads[i] = summarizers[i];
				threads[i].start(); //K‰ynnistet‰‰n summauspalvelut.
				output.writeInt(summarizers[i].getPort());
				output.flush();
			}
			System.out.println("Started: " + streamInput + " summarizers." );
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	/**
	 * Closes all connection made and interrupts all summarizers.
	 * After all connections are closed program will shut down.
	 */
	public static void closeConnections() {
		//Metodi sulkee kaikki k‰ynniss‰ olevat summauspalvelut sek‰ UDP- ja TCP-yhteydet.
		try {
			for (int i = 0; i < threads.length; i++) {
				threads[i].interrupt();
			}
			datagramSocket.close();
			ss.close();
			cs.close();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads total sum from the summarizers.
	 * @return
	 */
	public static int readTotalSum() {
		//Lukee ja palauttaa yhteenlasketun summan kaikilta summauspalveluilta.
		int total = 0;
		try {
			Thread.sleep(100);
			//K‰yd‰‰n l‰pi summauspalveluita sis‰lt‰v‰ taulukko ja lasketaan total muuttujaan summa.
			for (int i = 0; i < summarizers.length; i++) {
				total += summarizers[i].getSum();
			} 
		}catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return total;
	}

	/**
	 * Reads the index which summarizer has largest sum.
	 * @return
	 */
	public static int readMaxIndex() {
		//Etsit‰‰n ja palautetaan taulukon indeksi, jossa summauspalvelulla on suurin summa.
		int largest = 0;
		int maxIndex = 0;
		try {
			Thread.sleep(100);
			/*K‰yd‰‰n summauspalveluja sis‰lt‰v‰ taulukko l‰pi ja tallennetaan maxIndex muuttujan suurimman indeksi numero.
			 *WorkDistributo serverin indeksi alkavat 1:st‰, joten palautetaan maxIndex + 1.
			 */
			for (int i = 0; i < summarizers.length; i++) {
				if (summarizers[i].getSum() > largest) {
					largest = summarizers[i].getSum();
					maxIndex = i+1;
				}
			}
		}catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}	
		return maxIndex;
	}

	/**
	 * Reads the total sum of integers received.
	 * @return
	 */
	public static int readIntegerAmount() {
		//Luetaan yhteenlaskettu lukum‰‰r‰, paljonko summauspalvelut ovat saaneet kokonaislukuja summattavaksi.
		int amount = 0;
		try {
			Thread.sleep(100);
			for (int i = 0; i < summarizers.length; i++) {
				amount += summarizers[i].getAmount();
			}
		}catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return amount;
	}
}
