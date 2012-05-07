package DatabaseController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Database {
	
		private static String url = "jdbc:mysql://127.0.0.1/AZAdatabase";
		private static String dbUserName = "root";
		private static String dbPassword = "";
		private static Connection conn = null;
		private static Statement stmt = null;
		private static ResultSet rs = null;
		static Server server = new Server();
		
		//To check whether a user name exists.
		public boolean CheckUserName(String userName) throws SQLException{
				conn = DriverManager.getConnection(url,dbUserName,dbPassword);
				Statement stmt = conn.createStatement();
		    String sql = "SELECT UID FROM users where username = '" + userName + "'";
		    System.out.println(sql);
		    if (stmt.execute(sql)) {
		        rs = stmt.getResultSet();
		        if (rs.next()){ //To find if there is result.
		        		return true;
		        }    
		    }
		    return false;
		}
		
		//To insert a new user into database
		public boolean InsertNewUser(String userName, String password, String phoneNumber){
				try {
					conn = DriverManager.getConnection(url,dbUserName,dbPassword);
					stmt = conn.createStatement();
				} catch (SQLException e1) {
						e1.printStackTrace();
				}			
		    String sql = "INSERT INTO users (username, password,phonenumber) VALUES ('"+userName+"'"+",'"+password+"','"+phoneNumber+"')";
		    System.out.println(sql);
		    try {
		    		if (stmt.execute(sql)) {
		    				if (CheckUserName(userName)){ // Double check if the new user is added.
		    						return true;
		    				}    
		    		}
		    } catch (SQLException e) {
		    	//e.printStackTrace();
		    	return false;
		    }
		    
		    return false;
		}
		
		//To get the id of user.
		public String GetUserID(String userName, String password) throws SQLException{
				conn = DriverManager.getConnection(url,dbUserName,dbPassword);
				Statement stmt = conn.createStatement();
		    String sql = "SELECT UID FROM users where username = '" + userName + "'" + " AND password = '" + password + "'";
		    System.out.println(sql);
		    if (stmt.execute(sql)) {
		        rs = stmt.getResultSet();
		        if(rs.next()){
		        	return rs.getString("UID");
		        }
		    }
		    return null;
		}
		
		//To get user name by the user id
		public String GetUserNameById(String uid) throws SQLException{
			conn = DriverManager.getConnection(url,dbUserName,dbPassword);
			Statement stmt = conn.createStatement();
			String sql = "SELECT username FROM users where uid = '" + uid + "'";
			System.out.println(sql);
		    if (stmt.execute(sql)) {
		        rs = stmt.getResultSet();
		        if(rs.next()){
		        	return rs.getString("username");
		        }
		    }
		    return null;
		}
		
		//To insert new relation between user 1 and user 2 if it does not exist.
		public void InsertRelation(String UID1, String UID2) throws SQLException{
			if(UID1.compareTo(UID2)>0){
					String tmp = UID1;
					UID1 = UID2;
					UID2 = tmp;
			}
			conn = DriverManager.getConnection(url,dbUserName,dbPassword);
			Statement stmt = conn.createStatement();
			String sql = "INSERT INTO relations (uid1, uid2,duration,msgnum,meettime) VALUES ('"+UID1+"'"+",'"+UID2+"',0,0,0)";
		    System.out.println(sql);
		    if (stmt.execute(sql)) {
		    		rs = stmt.getResultSet();
		        rs.next();
		    }
		}
		
		//To get ID if relation between user 1 and user 2.
		public String GetRelationID(String UID1, String UID2) throws SQLException{
			String RID =null;
			if(UID1.compareTo(UID2)>0){
					String tmp = UID1;
					UID1 = UID2;
					UID2 = tmp;
			}
			conn = DriverManager.getConnection(url,dbUserName,dbPassword);
			Statement stmt = conn.createStatement();
		    String sql = "SELECT RID FROM relations where UID1 = '" + UID1 + "'" + " AND UID2 = '" + UID2 + "'";
		    System.out.println(sql);
		    if (stmt.execute(sql)) {
		        rs = stmt.getResultSet();
		        rs.next();
		        RID = rs.getString("RID");
		    }
		    return RID;
		}
		
		//To get relation id by user id
		public String GetRelationID(String UID) throws SQLException{
			conn = DriverManager.getConnection(url,dbUserName,dbPassword);
			Statement stmt = conn.createStatement();
			String sql = "SELECT RID FROM relations where UID1 = '" + UID + "'" + " OR UID2 = '" + UID + "'";
			System.out.println(sql);
			if (stmt.execute(sql)) {
				rs = stmt.getResultSet();
				rs.next();
			}
			return rs.getString("RID");
	}
		
		//To check if there is relation existing between user 1 and user 2.
		public boolean CheckRelation(String UID1, String UID2) throws SQLException{
				if(UID1.compareTo(UID2)>0){
						String tmp = UID1;
						UID1 = UID2;
						UID2 = tmp;
				}
				conn = DriverManager.getConnection(url,dbUserName,dbPassword);
				Statement stmt = conn.createStatement();
				String sql = "SELECT RID FROM relations where UID1 = '" + UID1 + "'" + " AND UID2 = '" + UID2 + "'";
		    System.out.println(sql);
		    if (stmt.execute(sql)) {
		        rs = stmt.getResultSet();
		        if (rs.next()){
		        	return true;
		        }    
		    }			
		    return false;
		}
		
		//To get user id by phone number
		public String GetUidFromPhoneNum(String phoneNum) throws SQLException {
			conn = DriverManager.getConnection(url,dbUserName,dbPassword);
			Statement stmt = conn.createStatement();
			String UID = null;
		    String sql = "SELECT UID FROM users where phoneNumber = '" + phoneNum + "'";
		    System.out.println(sql);
		    if (stmt.execute(sql)) {
		        rs = stmt.getResultSet();
		        if(rs.next()){
		        	UID = rs.getString("UID");
		        }
		        
		    }
		    return UID;
		}
		
		//To insert call record by rid
		public void InsertCallRecord(String rID, int duration) throws SQLException {
				conn = DriverManager.getConnection(url,dbUserName,dbPassword);
				Statement stmt = conn.createStatement();
				String sql1 = "SELECT duration FROM relations WHERE RID='"+ rID +"'";
		    System.out.println(sql1);
		    if (stmt.execute(sql1)) {
		        rs = stmt.getResultSet();
		        rs.next();
		    }
	
		    int oldDuration =  rs.getInt("duration");
				int newDuration = oldDuration + duration;
				String sql2 = "UPDATE relations SET duration='"+ newDuration +"' WHERE RID='"+ rID +"'";
		    System.out.println(sql2);
		    if (stmt.execute(sql2)) {
	        rs = stmt.getResultSet();
	        rs.next();
		    }				
		}
		
		//to insert message by rid
		public void InsertMsgRecord(String rID) throws SQLException {
			conn = DriverManager.getConnection(url,dbUserName,dbPassword);
			Statement stmt = conn.createStatement();
			String sql1 = "SELECT msgnum FROM relations WHERE RID='"+ rID +"'";
			System.out.println(sql1);
			if (stmt.execute(sql1)) {
				rs = stmt.getResultSet();
				rs.next();
			}

			int oldnum =  rs.getInt("msgnum");
			int newnum = oldnum + 1;
			String sql2 = "UPDATE relations SET msgnum='"+ newnum +"' WHERE RID='"+ rID +"'";
			System.out.println(sql2);
			if (stmt.execute(sql2)) {
				rs = stmt.getResultSet();
				rs.next();
			}				
		}
		
		public void UpdateMeetTime(String rID,double meetTime) throws SQLException {
			conn = DriverManager.getConnection(url,dbUserName,dbPassword);
			Statement stmt = conn.createStatement();
			String sql1 = "SELECT meettime FROM relations WHERE RID='"+ rID +"'";
			System.out.println(sql1);
			if (stmt.execute(sql1)) {
				rs = stmt.getResultSet();
				rs.next();
			}

			double oldTime =  rs.getDouble("meettime");
			double newTime = oldTime + meetTime;
			String sql2 = "UPDATE relations SET meettime='"+ newTime +"' WHERE RID='"+ rID +"'";
			System.out.println(sql2);
			if (stmt.execute(sql2)) {
				rs = stmt.getResultSet();
				rs.next();
			}				
		}
		
		//Get score by rid
		public double GetScore(String RID) throws SQLException{
			double callTime=0;
			double msgNum=0;
			double score = 0;
			conn = DriverManager.getConnection(url,dbUserName,dbPassword);
			Statement stmt = conn.createStatement();
		    String sql = "SELECT * FROM relations where RID = '" + RID + "'";
		    System.out.println(sql);
		    if (stmt.execute(sql)) {
		        rs = stmt.getResultSet();
		        rs.next();
		        callTime = Double.parseDouble(rs.getString("Duration"));
		        msgNum = Double.parseDouble(rs.getString("MsgNum"));
		        score = callTime * 0.1 + score +msgNum;
		    }
		   
		    return score;
		}
		
		//to get best friends user array by user id and the number of best friends
		public ArrayList<String> GetTopUsers(String UID,int top) throws SQLException{
			ArrayList<String> userArray = new ArrayList<String>();
			conn = DriverManager.getConnection(url,dbUserName,dbPassword);
			Statement stmt = conn.createStatement();
		    String sql = "SELECT * FROM `Relations` WHERE (UID1='"+UID+"' OR UID2 ='"+UID+"') Order by (Duration / 60 +MsgNum + MeetTime /30) ASC Limit " + top;
		    System.out.println(sql);
		    if (stmt.execute(sql)) {
		        rs = stmt.getResultSet();
		        while(rs.next()){
		        	if(UID.equals(rs.getString("UID1"))){
		        		userArray.add(rs.getString("UID2"));
		        	}
		        	else{
		        		userArray.add(rs.getString("UID1"));
		        	}
		        }
		    }
		   
		    return userArray;
		}
		
		
		//To get the most active users
		public ArrayList<String> GetTopActiveUsers(int top) throws SQLException{
			ArrayList<String> topArray = new ArrayList<String>();
			Hashtable<String,Double> userTable = new Hashtable<String,Double>();
			double tmp,tmp2;
			//double v;
			String k;
			
			conn = DriverManager.getConnection(url,dbUserName,dbPassword);
			Statement stmt = conn.createStatement();
		    String sql = "SELECT uid1,COUNT(uid1) FROM `Relations` GROUP BY uid1 ASC ";
		    
		    if (stmt.execute(sql)) {
		        rs = stmt.getResultSet();
		        while(rs.next()){
		        	tmp = Double.parseDouble(rs.getString(2));
		        	userTable.put(rs.getString(1),tmp);
		        }
		    }
		    
		    sql = "SELECT uid2,COUNT(uid2) FROM `Relations` GROUP BY uid2 ASC ";
		    
		    if (stmt.execute(sql)) {
		        rs = stmt.getResultSet();
		        while(rs.next()){
		        	tmp = Double.parseDouble(rs.getString(2));
		        	k = rs.getString(1);
		        	if(userTable.contains(k)){
		        		tmp2 = userTable.get(k);
		        		tmp = tmp + tmp2;
		        		userTable.put(k,tmp);
		        	}
		        	else{
		        		userTable.put(k,tmp);
		        	}
		        	
		        }
		     }
		    
		    
		    Map.Entry entry ;
		    String key;
		   
			double minval=10000;
			int minIndex=0;
		    for (Iterator<Entry<String, Double>> it = userTable.entrySet().iterator(); it.hasNext(); )
		    {
		    	entry = (Map.Entry) it.next();
		    	tmp = (Double) entry.getValue();
		    	key = (String)entry.getKey();
		    	
		    	if(topArray.size()<top){
		    		topArray.add((String) entry.getKey());	    		
		    		if (tmp < minval) {
	    				minval = tmp;
	    				minIndex = topArray.size()-1;
	    				//System.out.println(key+":"+tmp+"topArraySize:"+topArray.size()+"minIndex:"+minIndex);
	    			}
		    	}
		    	else{
		    		
		    		if (tmp > minval) {
		    			//System.out.println("minval:"+tmp+"||minIndex:"+minIndex);
		    			topArray.remove(minIndex);
		    			topArray.add((String) entry.getKey());
		    			minval = tmp;
		    			for(int i = 0;i < topArray.size(); i ++){
			    			tmp = userTable.get(topArray.get(i));
			    			if (tmp<minval) {
			    				minval = tmp;
			    				minIndex = i;
			    			}
			    		}
	    			}	    		
		    	}
		    }
		    
		    
		    for (int i = topArray.size()-1; i>0;i--){
		    	for(int j = i; j>0; j--){
		    		tmp = userTable.get(topArray.get(j));
		    		tmp2 = userTable.get(topArray.get(j-1));
		    		if(tmp>tmp2){
		    			String tmpStr=topArray.get(j);
		    			topArray.set(j, topArray.get(j-1));
		    			topArray.set(j-1, tmpStr);
		    		}
		    	}
		    }
		    
		    return topArray;

		}
		
		
			
}

