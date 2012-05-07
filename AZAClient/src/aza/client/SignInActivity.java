package aza.client;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class SignInActivity extends Activity {
	ImageButton imageButton1;
	ImageButton imageButton2;
	ImageButton imageButton3;
	private EditText editText1;	//input box for user name
	private EditText editText2;	//input box for password

	private boolean validFlag = false;	//flag for input validation
	private String errorMsg;
	private String userName;
    private String password;
    private String msg;
    private String uid;
    private String lastUpdate;
    
    SharedPreferences.Editor editor;
    SharedPreferences settings;

    //create socket for communication with the server        
    private ClientSocket cskt = new ClientSocket();    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        
        settings = this.getSharedPreferences("AZAClient", Context.MODE_PRIVATE);
    	editor = settings.edit();
       
        editText1 = (EditText)findViewById(R.id.editText1);
        editText2 = (EditText)findViewById(R.id.editText2);
        
        imageButton1= (ImageButton) findViewById(R.id.imageButton1);
        imageButton1.setOnClickListener(new View.OnClickListener() {    
        
	    	public void onClick(View v) {    
	    		//input validation	
	    		if(editText1.getText().toString().length()==0){
	    				errorMsg = "Username can not be empty";
	    				validFlag = false;
	    			}
	    			else if(editText2.getText().toString().length() == 0){
	    				errorMsg = "Password can not be empty";
	    				validFlag = false;
	    			}
	    			else {
	    				validFlag = true;
	    			}
	    		
	    			//If pass validation, send sign up information to server
	    			if(validFlag){
	    					//get the input string
	        				userName = editText1.getText().toString();
	        				password = editText2.getText().toString();
	        				msg = "SignIn#"+ userName +"#"+ password + "#";
	        				
	        				//send sign up string to server for further process
	        				if(!cskt.InitializeClient()){
	        					setTitle("Network Error");
	        				}
	        				
	        				try {
	        						cskt.SendMessage(msg);
	        						if(cskt.ReceiveMessage()){
	        							ProcessMessage(cskt.parts);
	        						}
	        				} catch (NumberFormatException e) {
	        						e.printStackTrace();
	        				} catch (IOException e) {
	        						e.printStackTrace();
	        				}
	    			}
	    			
	    			//If not pass validation, show error message
	    			else{
	    					setTitle(errorMsg);
	    			}
	        	}
	        	
        }); 
       
        imageButton2= (ImageButton) findViewById(R.id.imageButton2);
        imageButton2.setOnClickListener(new View.OnClickListener() {    
        
	        	public void onClick(View v) {    
	        		Intent intentSignUp = new Intent();
    				intentSignUp.setClass(SignInActivity.this,SignUpActivity.class);
    				startActivity(intentSignUp);
	        	}
	        	
        }); 
        
        imageButton3= (ImageButton) findViewById(R.id.imageButton3);
        imageButton3.setOnClickListener(new View.OnClickListener() {    
        
	        	public void onClick(View v) { 
	        		System.exit(1);
	        
	        	}
	        	
        }); 
        
    }
    
    
    
    public void ProcessMessage(String[] parts) throws IOException{
    	
    	setTitle(parts[0]);
    	if (parts[0].equals("SignUp")) {
    		
   		}
   		if (parts[0].equals("SignIn")) {
   			setTitle(parts[1]);
    		if(parts[1].equals("Fail")){
    			 Toast.makeText(this, "Your user name or password is incorrect, try again!", Toast.LENGTH_SHORT).show();
    		}
    		else{
    			editor.putString("UID", parts[1]);
    			editor.commit();
    			uid = settings.getString("UID", "Default");
    			Toast.makeText(this, "Sign in suceed, your id is:" + uid, Toast.LENGTH_SHORT).show();

    			lastUpdate = settings.getString("recUpdateTime", "0");
    			String str1 = ReadCallRecord();
    			String str2 = ReadMsgRecord();
    			String historystr = "CallMsg#"+uid+"#Call%"+str1+"#Msg%" +
    					""+str2;
    			
    			SimpleDateFormat formatter = new SimpleDateFormat("MMddhhmmss");     
      		    Date curDate = new Date(System.currentTimeMillis());     
      		    String curtime = formatter.format(curDate);
      		    
				editor.putString("recUpdateTime", curtime);
				editor.commit();
    			
    			cskt.SendMessage(historystr);
    			
    			Intent intentSignIn = new Intent();
				intentSignIn.setClass(SignInActivity.this,BumpActivity.class);
				startActivity(intentSignIn);
				cskt.closeSocket();
    		}
   		}
    }
    
    public String ReadCallRecord() {
	      String test="";
	      String callDuration;	//duration of the call
	      String callNumber;	//phone number the call was made with
	      Date callDate;	//system format date and time stamp when the call was made    
	      String callTime;	//formated date and time stamp when the call was made
	
	      //get voice call record from CONTENT_URI
	      final Cursor cursor1 = getContentResolver().query(
	      		CallLog.Calls.CONTENT_URI, 
	      		new String[]{CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.NUMBER}, 
	      		null, null,CallLog.Calls.DEFAULT_SORT_ORDER
	      );
	      
	      
	      //read the record one by one and append in the result string
	      for (int i = 0; i < cursor1.getCount(); i++) {        	
	      		cursor1.moveToPosition(i);
						SimpleDateFormat sfd = new SimpleDateFormat("MMddhhmmss");
						callDate = new Date(Long.parseLong(cursor1.getString(1)));
						callTime = sfd.format(callDate);
						if(callTime.compareTo(lastUpdate)<0){
			        		continue;
						}
	          callDuration = cursor1.getString(2);
	          callNumber = cursor1.getString(3);
	 
	          test += callNumber + "@" + callDuration +"%";
	      }      
	      return test;
		}
		
  //To get text message record
		private String ReadMsgRecord() {
		    String test="";
		    String msgNumber;	//phone number the text was made with
		    Date msgDate;	//system format date and time stamp when the message was made
		    String msgTime;	//formated date and time stamp when the message was made
	      
				//get text message record form SMS_URI
				final String SMS_URI = "content://sms/";
				Uri uri = Uri.parse(SMS_URI);
				final Cursor cursor2 = getContentResolver().query(uri, new String[]{"address", "date", "type"}, null, null, null);
		      
				//read the record one by one and append in the result string
				for (int i = 0; i < cursor2.getCount(); i++) {  
			        cursor2.moveToPosition(i);
			                    
			        int phoneNumberColumn = cursor2.getColumnIndex("address");    
			        msgNumber = cursor2.getString(phoneNumberColumn);    
			        int dateColumn = cursor2.getColumnIndex("date");    
			            
			        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddhhmmss");    
			        msgDate = new Date(Long.parseLong(cursor2.getString(dateColumn)));    
			        msgTime = dateFormat.format(msgDate);
					if(msgTime.compareTo(lastUpdate)<0){
			    		continue;
					}	        
			        test += msgNumber+"%";
		      }   
					return test;
			}
}