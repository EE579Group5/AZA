package aza.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class AZAClientActivity extends Activity {
		ImageButton button1;
		
		SharedPreferences.Editor editor;
	    SharedPreferences settings;
	    
	    String myId;
	    @Override
        public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        settings = this.getSharedPreferences("AZAClient", Context.MODE_PRIVATE);
    	editor = settings.edit();
        myId = settings.getString("UID", "Default");
        
        //Jump to SignIn activity when click button1
        button1= (ImageButton) findViewById(R.id.imageButton1);
        button1.setOnClickListener(new View.OnClickListener() {    
    		public void onClick(View v) {
    			
				if(!myId.equals("Default") || myId.equals("Default")){
					Intent intentSignIn = new Intent();
    				intentSignIn.setClass(AZAClientActivity.this,SignInActivity.class);
    				startActivity(intentSignIn);
				}
				
				else{
					Intent intentBump = new Intent();
    				intentBump.setClass(AZAClientActivity.this,BumpActivity.class);
    				startActivity(intentBump);
				}
    				
            }    
        });

    }
}
