package DatabaseController;

import java.io.IOException;
import java.sql.SQLException;
import DatabaseController.Server;


public class Run extends Thread {
	
	static Server server = new Server();
	

	public static void main(String args[]) throws IOException, SQLException{
				
		server.InitialzeServer(4321);
		
		while(true){
		
			server.AcceptClient();
			MultiUserServer mus = new MultiUserServer(server);
			mus.start();
			//server.ProcessMessage();
		}
		
	}
}
