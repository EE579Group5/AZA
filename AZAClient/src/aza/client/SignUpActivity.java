package aza.client;

import aza.client.ClientSocket;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SignUpActivity extends Activity {

	ImageButton imageButton1;
	ImageButton imageButton2;
	ImageButton imageButton3;
	private EditText editText1;	//input box for user name
	private EditText editText2;	//input box for password
	private EditText editText3;	//input box for password confirmation
	private EditText editText4;	
	private LinearLayout layout1;
	
	private boolean validFlag = false;	//flag for input validation
	private String errorMsg;
	private String userName;
    private String password;
    private String phoneNumber;
    
    private String msg;
    private String uid;

    SharedPreferences.Editor editor;
    SharedPreferences settings;
    //create socket for communication with the server        
    private ClientSocket cskt = new ClientSocket();    
    //private int portNum = 4321;
    //private String hostName= "10.0.0.23";
	
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        
        phoneNumber=getPhoneNumber();
        
        settings = this.getSharedPreferences("AZAClient", Context.MODE_PRIVATE);
    	editor = settings.edit();
        
        editText1 = (EditText)findViewById(R.id.editText1);
        editText2 = (EditText)findViewById(R.id.editText2);
        editText3 = (EditText)findViewById(R.id.editText3);
        editText4 = (EditText)findViewById(R.id.editText4);
        layout1 = (LinearLayout)findViewById(R.id.phone);
        uid = settings.getString("UID", "Default");
        
        imageButton1= (ImageButton) findViewById(R.id.imageButton1);
        imageButton1.setOnClickListener(new View.OnClickListener() {    
         	
	        	public void onClick(View v) {    
	        		//input validation	
	        		if(editText4.getVisibility()== View.VISIBLE){
	        			phoneNumber = editText4.getText().toString();
	        		}
	        		
	        		if(editText1.getText().toString().length()==0){
	        				errorMsg = "Username can not be empty";
	        				validFlag = false;
	        			}
	        			else if(editText2.getText().toString().length()==0){
	        				errorMsg = "Password can not be empty";
	        				validFlag = false;
	        			}
	        			else if(editText3.getText().toString().length()==0){
	        				errorMsg = "Please re-enter the password ";
	        				validFlag = false;
	        			}
	        			else if (!editText2.getText().toString().equals(editText3.getText().toString())){
	        				errorMsg = "Re-enter the password correctly";
	        				validFlag = false;
	        			}
	        			else if(phoneNumber==null || phoneNumber.length()==0){
	        				errorMsg = "Can not get phone number, you can enter manually";
	        				layout1.setVisibility(View.VISIBLE);
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
		        				
		        				
		        				msg = "SignUp#"+ userName +"#"+ password + "#" + phoneNumber + "#";
		        				
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
	        		Intent intentSignIn = new Intent();
					intentSignIn.setClass(SignUpActivity.this,SignInActivity.class);
					startActivity(intentSignIn);
	        	}
	        	
        }); 
        
        imageButton3= (ImageButton) findViewById(R.id.imageButton3);
        imageButton3.setOnClickListener(new View.OnClickListener() {    
         	
	        	public void onClick(View v) {
	        		System.exit(1);
	        		
	        	}
	        	
        }); 
    }
    
    public void ProcessMessage(String[] parts){
    	setTitle(parts[0]);
    	if (parts[0].equals("SignUp")) {
    		setTitle(parts[1]);
    		if(parts[1].equals("Fail")){
    			 Toast.makeText(this, "User name exists, try again!", Toast.LENGTH_SHORT).show();
    		}
    		else{
    			editor.putString("UID", parts[1]);
    			editor.commit();
    			uid = settings.getString("UID", "Default");
    			Toast.makeText(this, "Sign up suceed, your id is:" + uid, Toast.LENGTH_SHORT).show();
    			Intent intentSignUp = new Intent();
				intentSignUp.setClass(SignUpActivity.this,BumpActivity.class);
				startActivity(intentSignUp);
    		}
   		}
   		if (parts[0].equals("SignIn")) {
   		}
   		if (parts[0].equals("Meet")) {
   				//meetId = parts[1];
   		}
   		if (parts[0].equals("Apart")) {
   				//if (parts[1].equals("ack")){}
   		}
   		if (parts[0].equals("Call")) {
   				//meetId = parts[1];
   		}
   		if (parts[0].equals("Msg")) {
   				//meetId = parts[1];
   		}
    }
    
    public String getPhoneNumber(){
    	String phoneNumber = null;
        TelephonyManager mTelephonyMgr;  
        mTelephonyMgr = (TelephonyManager)  getSystemService(Context.TELEPHONY_SERVICE);   
        phoneNumber = mTelephonyMgr.getLine1Number();
        return   phoneNumber;
    }   
    
}