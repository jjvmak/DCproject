import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Summarizer extends Thread{

	private int port;
	private ServerSocket ss;
	private Socket cs;
	private ObjectInputStream input;
	private int calculateSum = 0;
	private int numberSum = 0;
	private int temp;
	private InputStream is;

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
			is = cs.getInputStream();
			input = new ObjectInputStream(is);
		}
		catch (IOException e){
			System.exit(0);
		}
		do {
			try {
				temp = input.readInt();
				if (temp == 0){
					cs.close();
					ss.close();
					input.close();
					break;
					
				}
				numberSum++;
				calculateSum += temp;
				
			} catch (Exception e) {
				System.exit(0);
			}
		} while (true);
	}
}

