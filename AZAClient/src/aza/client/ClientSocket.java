
package aza.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;


public class ClientSocket extends Activity{
  	Socket clientSocket = null;
  	Socket fileSocket = null;
  	DataOutputStream out = null;
  	DataInputStream in = null;
  	String meetId = null;
  	// data was sent in format of byte array
  	byte[] clientInputBuffer = new byte[11000];
  	byte[] clientOutputBuffer = new byte[11000];
  	String[] parts = null;
  	
  	String hostName = "207.151.253.82";
	int portNum = 4321;
  	
    //initialize client socket
  	public boolean InitializeClient(){
  		try {
  			clientSocket = new Socket(hostName, portNum);
  		    out = new DataOutputStream(clientSocket.getOutputStream());
  		    in = new DataInputStream(clientSocket.getInputStream());
  		    return true;
  		} catch (UnknownHostException e) {
  		    System.err.println(e.toString());
  		    return false;
  		} catch (IOException e) {
  		    System.err.println(e.toString());
  		    return false;
  		}
  	}
   	
  	//send message to server
    public void SendMessage(String fromClient) throws NumberFormatException, IOException{
    	out.writeUTF(fromClient);			
    }
  	
    //receive message from server and process message
   	public boolean ReceiveMessage() throws NumberFormatException, IOException{
   		String fromServer;
   		if ( (fromServer = in.readUTF())!= null) {
 
       			//message format: message type#message string
       			parts = fromServer.split("#");
    	   		return true;
    	   		
   	    }
   		return false;
   	}
   	
   	public void closeSocket() throws IOException{
    	out.close();
  		in.close();
    	clientSocket.close();
    	
    }		
   	
}
