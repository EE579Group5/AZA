package DatabaseController;

import java.io.IOException;
import java.sql.SQLException;

public class MultiUserServer extends Thread{
	private Server server;
	
	
	public MultiUserServer(Server s){
		this.server=s;
	} 
	
	public void run(){
		try {
			server.ReplyMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
