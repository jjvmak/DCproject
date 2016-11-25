
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Connections {

	static int targetPort;
	static DatagramPacket packet;
	static DatagramSocket datagramSocket;
	static InetAddress targetAdd;
	static byte[] data;
	static Socket cs;
	static ServerSocket ss;
	static ObjectOutput output;
	static ObjectInputStream input;
	static String portNumber;
	static int streamInput = 0;
	
	public void init() throws IOException {
		try {
			targetAdd = InetAddress.getLocalHost();
			targetPort = 3126;
			portNumber = "7777";
			data = portNumber.getBytes();
			packet = new DatagramPacket(data, data.length, targetAdd, 3126);
			datagramSocket = new DatagramSocket();
			ss = new ServerSocket(7777);
	
			do {
				System.out.println("noni");
				sendDatagram();
				acceptTCP();
				setupStreams();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (!isConnected());
			
			readInput();
			sendPortNumbers();
		

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
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
	
	public void setupStreams() {
		try {
			output = new ObjectOutputStream(cs.getOutputStream());
			output.flush();
			input = new ObjectInputStream(cs.getInputStream());
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
		try {
			streamInput = input.readInt();
			System.out.println(streamInput);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (streamInput == 0) {
			
			readInput();
		}
	}
	
	public void sendPortNumbers() {
		int portNumber = 7778;
		for (int i = 0; i < streamInput; i++) {
			try {
				openSocket(portNumber);
				output.flush();
				System.out.println("heppis");
				output.writeObject(portNumber);
				output.flush();
				portNumber++;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	public void openSocket(int port){
		//String hostName = "localhost";
		int p = port; 
		Socket ss;
		int str = 0;
		try {
		    ss = new Socket(InetAddress.getByName("localhost"), p);
		    ObjectInputStream ois = new ObjectInputStream(ss.getInputStream());
		    ObjectOutputStream oos = new ObjectOutputStream(ss.getOutputStream());
		    str = 1;
		    oos.writeObject(str);

		    while ((str = (int) ois.readObject()) != 0) {
		      System.out.println(str);
		      oos.writeObject(5);

		      if (str == 0)
		        break;
		    }

		    ois.close();
		    oos.close();
		    ss.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
