
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

	public void init() throws IOException {
		try {
			targetAdd = InetAddress.getLocalHost();
			targetPort = 3126;
			portNumber = "6000";
			data = portNumber.getBytes();
			packet = new DatagramPacket(data, data.length, targetAdd, 3126);
			datagramSocket = new DatagramSocket();
			ss = new ServerSocket(6000);

			while(connectionAttemp <= 5){
				System.out.println("Sending datagram packet to server. Attemp: "+connectionAttemp);
				sendDatagram();
				try {
					ss.setSoTimeout(5000);
					acceptTCP();
					connection = true;
					if(cs.isConnected()) break;
				} catch (SocketTimeoutException e) {
					connectionAttemp++;
					System.out.println("No connection to server");
					continue;
				}
				
			}
			if(!connection) System.exit(0);;
			setupStreams();
			readInput();
			summarizerService();
			listener();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendDatagram() {
		try {
			System.out.println(packet);
			datagramSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setupStreams(){
		try {
			iS = cs.getInputStream();
			oS = cs.getOutputStream();
			output = new ObjectOutputStream(oS);
			input = new ObjectInputStream(iS);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void acceptTCP() throws IOException{
		cs = ss.accept();
	}

	public void readInput() throws Exception{
			try {
				streamInput = input.readInt();
				System.out.println("Reading input. Received: "+streamInput);

			}catch (SocketTimeoutException e){
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

	public static void listener() throws SocketException{
		ss.setSoTimeout(60000);
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

	public static void summarizerService(){
		summarizers = new Summarizer[streamInput];
		threads = new Thread[streamInput];
		try {

			for(int i=0; i<streamInput; i++){
				summarizers[i] = new Summarizer(6001+i);
				threads[i] = summarizers[i];
				threads[i].start();
				output.writeInt(summarizers[i].getPort());
				output.flush();
			}
			System.out.println("Started: " + streamInput + " summarizers." );
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void closeConnections() {
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

	public static int readTotalSum() {
			int total = 0;
		try {
			Thread.sleep(100);
			for (int i = 0; i < summarizers.length; i++) {
				total += summarizers[i].getSum();
			} 
		}catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return total;
	}

	public static int readMaxIndex() {
		int largest = 0;
		int maxIndex = 0;
		try {
			Thread.sleep(100);
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

	public static int readIntegerAmount() {
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
