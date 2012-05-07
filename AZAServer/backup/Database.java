package DatabaseController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import DatabaseController.Server;

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
	    //System.out.println(sql);
	    if (stmt.execute(sql)) {
	        rs = stmt.getResultSet();
	        //System.out.println("test");
	        System.out.println(rs.getRow());
	        if (rs.next()){ //To find if there is result.
	        	System.out.println(rs.getString("UID"));
	        	return true;
	        }
	        
	    }
		return false;
	}
	
	//To insert a new user into database
	public boolean InsertNewUser(String userName, String password,String phoneNumber){
		try {
			conn = DriverManager.getConnection(url,dbUserName,dbPassword);
			stmt = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    String sql = "INSERT INTO users (username, password,phonenumber) VALUES ('"+userName+"'"+",'"+password+"','"+phoneNumber+"')";
	    System.out.println(sql);
	    try {
			if (stmt.execute(sql)) {
				System.out.println("test");
				//rs = stmt.getResultSet();
			    //System.out.println(rs.getRow());
			    if (CheckUserName(userName)){ // Double check if the new user is added.
			    	//System.out.println(rs.getString("UID"));
			    	return true;
			    }
			    
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
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
	
	//To insert new relation between user 1 and user 2 if it does not exist.
	public void InsertRelation(String UID1, String UID2) throws SQLException{
		conn = DriverManager.getConnection(url,dbUserName,dbPassword);
		Statement stmt = conn.createStatement();
		String sql = "INSERT INTO relations (uid1, uid2,duration,msgnum) VALUES ('"+UID1+"'"+",'"+UID2+"',0,0)";
	    System.out.println(sql);
	    if (stmt.execute(sql)) {
	        rs = stmt.getResultSet();
	        rs.next();
	    }
	}
	
	//To get ID if relation between user 1 and user 2.
	public String GetRelationID(String UID1, String UID2) throws SQLException{
		conn = DriverManager.getConnection(url,dbUserName,dbPassword);
		Statement stmt = conn.createStatement();
	    String sql = "SELECT RID FROM relations where UID1 = '" + UID1 + "'" + " AND UID2 = '" + UID2 + "'";
	    System.out.println(sql);
	    if (stmt.execute(sql)) {
	        rs = stmt.getResultSet();
	        rs.next();
	    }
	    return rs.getString("RID");
	}
	
	//To check if there is relation existing between user 1 and user 2.
	public boolean CheckRelation(String UID1, String UID2) throws SQLException{
		conn = DriverManager.getConnection(url,dbUserName,dbPassword);
		Statement stmt = conn.createStatement();
		String sql = "SELECT RID FROM relations where UID1 = '" + UID1 + "'" + " AND UID2 = '" + UID2 + "'";
	    //System.out.println(sql);
	    if (stmt.execute(sql)) {
	        rs = stmt.getResultSet();
	        //System.out.println(rs.getRow());
	        if (rs.next()){
	        	//System.out.println(rs.getString("UID"));
	        	return true;
	        }
	        
	    }
		return false;
	}
	
	public String GetUidFromPhoneNum(String phoneNum) throws SQLException {
		conn = DriverManager.getConnection(url,dbUserName,dbPassword);
		Statement stmt = conn.createStatement();
		String sql = "SELECT UID FROM users where phoneNumber = '" + phoneNum + "'";
		System.out.println(sql);
		if (stmt.execute(sql)) {
			rs = stmt.getResultSet();
			rs.next();
		}
		return rs.getString("UID");
	}

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


}
