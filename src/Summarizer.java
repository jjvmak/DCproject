import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Summarizer extends Thread{

	private int port;
	private static ServerSocket ss;
	private static Socket cs;
	private ObjectInputStream input;
	private int calculateSum = 0;
	private int numberSum = 0;
	private int temp;
	private static InputStream iS;

	public Summarizer(int port){
		this.port = port;
	}
	public boolean isConnected() {
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
	@Override
	public void run(){
		try{
			ss = new ServerSocket(port);
			cs = ss.accept();
			iS = cs.getInputStream();
			input = new ObjectInputStream(iS);
		}
		catch (IOException e){
			System.err.println(e.getMessage());
			System.exit(0);
		}
		do {
			try {
				System.out.println("numero: " + input.readInt());
				temp = input.readInt();
				if (temp == 0){
					break;
				}
				calculateSum += temp;
				numberSum++;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (!isConnected());
		try {
			cs.close();
			ss.close();
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.interrupt();
	}
}

