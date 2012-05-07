package DatabaseController;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
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
	  
	  double meetStartTime=0;
	  double meetEndTime=0;
	  double meetTime=0;
	  
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
	    		e.printStackTrace();
	    	}
	  }
	  
	  //Read message from client and reply
	  public void ReplyMessage() throws IOException, SQLException{
				System.out.println("processmessage!!!!!!!!!");
				String fromClient;
				//String buffer="";
				if ((fromClient=in.readUTF()) != null) {
//						if(fromClient.equals("Record#End#")){
//								break;
//						}
				//		buffer += fromClient;
						System.out.println(fromClient);
						String toClient = FormReply(fromClient);
						out.writeUTF(toClient);   
				}				
				//System.out.println("Record End!");
		}
		
	  //To build up the reply message.
		public String FormReply(String receivemsg) throws IOException, SQLException{
				System.out.println("formreply!!!!!!!!!");
				System.out.println(receivemsg);
				String reply = "";
				String[] arr = receivemsg.split("#");
				String type = arr[0];
				String UID;
				
				//message from client: Sighup#username#password
				if (type.equals("SignUp")) {
						if(!db.CheckUserName(arr[1])){
								db.InsertNewUser(arr[1], arr[2],arr[3]);
								reply = "SignUp#"+db.GetUserID(arr[1], arr[2])+"#";
						}
						else{
								reply = "SignUp#Fail#";
						}
				}
			
				//message from client: Signin#username#password
				if (type.equals("SignIn")) {
						if((UID=db.GetUserID(arr[1], arr[2]))!=null){
								reply = "SignIn#"+UID+"#";
						}
						else{
								reply = "SignIn#Fail#";
						}
				}
				
				if (type.equals("Meet")) {
						if(!db.CheckRelation(arr[1], arr[2])){
						db.InsertRelation(arr[1], arr[2]);
						}
						reply = "Meet#"+db.GetRelationID(arr[1], arr[2])+"#";
						meetStartTime = Double.parseDouble(arr[3]);
				}
				
				if (type.equals("Apart")) {
						reply = "Apart#ack#";
						meetEndTime = Double.parseDouble(arr[2]);
						meetTime = (meetEndTime - meetStartTime)/1000;
						db.UpdateMeetTime(arr[1],meetTime);
				}
				
				//message from client: CallMsg#myID#yourPhoneNum@duration*yourPhoneNum@duration#yourPhoneNum*yourPhoneNum
				if (type.equals("CallMsg")) {
						String myid = arr[1];
						String callstr = arr[2];
						System.out.println("callstr:"+callstr);
						String msgstr = arr[3];
						System.out.println("msgstr:"+msgstr);
						String calls[] = callstr.split("%");
						String msgs[] = msgstr.split("%");
						
						System.out.println("!!!!!!!!!!!!!!!!!"+calls.length);
						for(int i=1; i<calls.length; i++){
								String[] record = calls[i].split("@");
								if(record[0].contains("+")){
									record[0] = record[0].substring(2);
								}
								String yourID = db.GetUidFromPhoneNum(record[0]);
								String rID = db.GetRelationID(arr[1], yourID);
								if(rID!=null){
									int duration = Integer.parseInt(record[1]);
									db.InsertCallRecord(rID, duration);
								}
						}
					
						for(int j=1; j<msgs.length; j++){
							if(msgs[j].contains("+")){
								msgs[j] = msgs[j].substring(2);
							}
							String yourID = null;
							
							String rID = null;
							yourID = db.GetUidFromPhoneNum(msgs[j]);
							if(yourID !=null){
								rID = db.GetRelationID(arr[1], yourID);
							}
							
							if(rID!=null){
								db.InsertMsgRecord(rID);
							}
							
						}
						reply = "CallMsg#Success";
				}
				
										
				if (type.equals("FriendList")) {
					String msg="FriendList#";
					String userName =null;
					ArrayList<String> topUser;
					ArrayList<String> activeUser;
					topUser=db.GetTopUsers(arr[1], 5);
					activeUser = db.GetTopActiveUsers(5);
					
					//msg+="TopUser";
					for(String str: topUser){
						userName = db.GetUserNameById(str);
						msg+=userName+"@";
					}
					
					msg+="#";
					//msg+="ActiveUser";
					
					for(String str: activeUser){
						userName = db.GetUserNameById(str);
						msg+=userName+"@";
					}
					
					msg+="#";
					reply = msg;
				}
			
				System.out.println(reply);
				return reply;
		}
	 
		
		 public ArrayList<String> testActive(int top) throws SQLException{
			 return db.GetTopActiveUsers(top);
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
