
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connections {

	static int targetPort;
	static DatagramPacket packet;
	static DatagramSocket datagramSocket;
	static InetAddress targetAdd;
	static byte[] data;
	static Socket cs;
	static ServerSocket ss;
	static ObjectOutputStream output;
	static ObjectInputStream input;
	static String portNumber;
	static int streamInput = 0;
	static Summarizer[] summarize;
	static Thread[] threads;
	static int connectionAttemp = 1;

	public void init() throws IOException {
		try {
			targetAdd = InetAddress.getLocalHost();
			targetPort = 3126;
			portNumber = "3127";
			data = portNumber.getBytes();
			packet = new DatagramPacket(data, data.length, targetAdd, 3126);
			datagramSocket = new DatagramSocket();
			ss = new ServerSocket(3127);

			do {
				
				if (connectionAttemp > 5) {
					System.exit(0);
				}
				
				System.out.println("Sending datagram packet to server. Attemp: "+connectionAttemp);
				sendDatagram();
				ss.setSoTimeout(5000);
				acceptTCP();
				input = new ObjectInputStream(cs.getInputStream());
				output = new ObjectOutputStream(cs.getOutputStream());
				output.flush();
				connectionAttemp++;

			} while (!isConnected());

			readInput();
			summarizerService();
			System.out.println("Oletko t‰‰ll‰");
			close();


		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/* **********************************************************************
	 * Joonan ‰l‰ k‰yt‰ 
	 */
	private static void close()throws Exception{
		System.out.println("Closing application");
		if (threads != null){
			for (int i=0; i<threads.length; i++){
				if (threads[i].isAlive()) threads[i].interrupt();
			}
		}
		ss.close();
		datagramSocket.close();
		if (cs != null) cs.close();
		if (input != null) input.close();
		if (output != null) output.close();
		System.exit(0);
	}
	/*
	 * **********************************************************************
	 */
	public void sendDatagram() {
		try {
			System.out.println(packet);
			datagramSocket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void acceptTCP() {
		try {
			cs = ss.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isConnected() {
		if (cs.isConnected()) {
			return true;
		}
		else {
			return false;
		}
	}

	public void readInput() {
		//T‰lle vois tehd‰ ehk‰ jonkun loopin, ett‰ t‰st‰ sais j‰rkev‰mm‰n.
		int tryTimes = 0;
		try {
			streamInput = input.readInt();
			System.out.println("Reading input. Received: "+streamInput);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if ((streamInput < 2 || streamInput > 10 ) && tryTimes <= 5){
			tryTimes++;
			readInput();
		}
		
		if (tryTimes >= 5) {
			try {
				output.writeInt(-1);
				close();
				System.exit(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void summarizerService(){
		summarize = new Summarizer[streamInput];
		threads = new Thread[streamInput];
		try {

			for(int i=0; i<streamInput; i++){
				summarize[i] = new Summarizer(3128+i);
				threads[i] = summarize[i];
				threads[i].start();
				output.writeInt(summarize[i].getPort());
				output.flush();
			}
			System.out.println("K‰nniss‰: " + streamInput + " summauspalvelinta." );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
