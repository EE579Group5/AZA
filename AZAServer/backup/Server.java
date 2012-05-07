package DatabaseController;
import java.net.*;
import java.sql.SQLException;
import java.io.*;

import DatabaseController.Database;
public class Server {

	  int packetLen = 0;
	  int i = 0;
	
	  String msgType = null;
	  String msgTemp = null;
	  String msgList = null;
	  String msgGet = null;
	  
	  byte[] data = new byte[10050];
	  byte[] sendData = null;
	  
	
	  String part[] = null;
	  
	  ServerSocket serverSocket = null;
	  Socket clientSocket = null;
	  DataOutputStream out = null;
	  DataInputStream in = null;
	  byte[] serverInputBuffer = new byte[11000];
	  byte[] serverOutputBuffer = null;
	  
	  Database db = new Database();
	  
	  //Initialize the socket connection
	  public void InitialzeServer(int protNum){
	    try {
	    	serverSocket = new ServerSocket(protNum);
	    } catch (IOException e) {
        System.err.println("Could not listen on port:"+protNum);
        System.exit(1);
	    }
	  }
	 
	  //AcceptClient: Accept the client socket.
	  public void AcceptClient() throws IOException{
	    try {
        clientSocket = serverSocket.accept();
	    } catch (IOException e) {
        System.err.println("Accept failed.");
        System.exit(1);
	    }
	    out = new DataOutputStream(clientSocket.getOutputStream());
	    in = new DataInputStream(clientSocket.getInputStream());
	    try {
			ReplyMessage();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

	  
	  public void ReplyMessage() throws IOException, SQLException{
			System.out.println("processmessage!!!!!!!!!");
			String fromClient;
			String buffer="";
			while ((fromClient=in.readUTF()) != null) {
					if(fromClient.equals("Record#End#")){
							break;
					}
					buffer += fromClient;
					System.out.println(fromClient);
					String toClient = FormReply(fromClient);
					out.writeUTF(toClient);   
			}				
			System.out.println("Record End!");
	  }

		
	  
	    //To build up the reply message.
		public String FormReply(String replymsg) throws IOException, SQLException{
			System.out.println("formreply!!!!!!!!!");
			//System.out.println(replymsg);
			String reply = "";
			String[] arr = replymsg.split("#");
			String type = arr[0];
//			String msg = arr[1];
			String UID;
//			System.out.println("@@"+replymsg);
			if (type.equals("SignUp")) {
				if(!db.CheckUserName(arr[1])){
					db.InsertNewUser(arr[1], arr[2],arr[3]);
					reply = "SignUp#"+db.GetUserID(arr[1], arr[2])+"#";
					System.out.println(reply);
				}
				else{
					reply = "SignUp#Fail#";
				}
			}
			
			if (type.equals("SignIn")) {
				if((UID=db.GetUserID(arr[1], arr[2]))!=null){
					reply = "SignIn#"+UID+"#";
					System.out.println(reply);
				}
				else{
					reply = "SignIn#Fail#";
				}
			}
			if (type.equals("Meet")) {
				if(!db.CheckRelation(arr[1], arr[2])){
					db.InsertRelation(arr[1], arr[2]);
				}
				
				//System.out.println("test");
				reply = "Meet#"+db.GetRelationID(arr[1], arr[2])+"#";///////;
				System.out.println(reply);
			}
			if (type.equals("Apart")) {
				reply = "Apart#ack#";////////;
				System.out.println(reply);
			}
			//message from client: Call#myID#timeStamp#yourPhoneNum@duration#...
			if (type.equals("Call")) {
					int numOfItems = arr.length-3;
					for(int i=0; i<numOfItems; i++){
							String[] record = arr[3+i].split("@");
							String yourID = db.GetUidFromPhoneNum(record[0]);
							String rID = db.GetRelationID(arr[1], yourID);
							int duration = Integer.parseInt(record[1]);
							db.InsertCallRecord(rID, duration);
					}
					reply = "Call#Success";
			}
			
			//message from client: Msg#myID#timeStamp#yourPhoneNum#...
			if (type.equals("Msg")) {
					int numOfItems = arr.length-3;
					for(int i=0; i<numOfItems; i++){
							String record = arr[3+i];
							String yourID = db.GetUidFromPhoneNum(record);
							String rID = db.GetRelationID(arr[1], yourID);
							db.InsertMsgRecord(rID);
					}
					reply = "Call#Success";
			}
		
			System.out.println(reply);

			return reply;
		}
	 
	 //Terminate the connection
	 public void TerminateConnection() throws IOException{
		 System.out.println("GOOD BYE!");
		 out.close();
		 in.close();
		 clientSocket.close();
		 serverSocket.close();
	 }
}
