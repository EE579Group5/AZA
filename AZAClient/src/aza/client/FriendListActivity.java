
package aza.client;

import aza.client.ClientSocket;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class FriendListActivity extends Activity {
	ImageButton button1;
	private ClientSocket cskt = new ClientSocket();
    SharedPreferences.Editor editor;
    SharedPreferences settings;
    String uid;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendlist);
       
        settings = this.getSharedPreferences("AZAClient", Context.MODE_PRIVATE);
	    editor = settings.edit();
	    uid = settings.getString("UID", "Default");

        button1= (ImageButton) findViewById(R.id.imageButton1);
        button1.setOnClickListener(new View.OnClickListener() {    
	        	public void onClick(View v) {    
		        		Intent intentBump = new Intent();
		    				intentBump.setClass(FriendListActivity.this,BumpActivity.class);
		    				startActivity(intentBump);
	        	}
	      });
        
        if(!cskt.InitializeClient()){
			setTitle("Network Error");
		}
		try {
				cskt.SendMessage("FriendList#" + uid);
				if(cskt.ReceiveMessage()){
						ProcessMessage(cskt.parts);
				}
		} catch (NumberFormatException e) {
				e.printStackTrace();
		} catch (IOException e) {
				e.printStackTrace();
		}        
    }
    
    //Process the message for social start.
    public void ProcessMessage(String[] parts){
    	
    	TextView tv0=(TextView) this.findViewById(R.id.textView0);
    	TextView tv1=(TextView) this.findViewById(R.id.textView1);
    	TextView tv2=(TextView) this.findViewById(R.id.textView2);
    	TextView tv3=(TextView) this.findViewById(R.id.textView3);
		TextView tv4=(TextView) this.findViewById(R.id.textView4);
		TextView Tv0=(TextView) this.findViewById(R.id.TextView0);
		TextView Tv1=(TextView) this.findViewById(R.id.TextView1);
		TextView Tv2=(TextView) this.findViewById(R.id.TextView2);
		TextView Tv3=(TextView) this.findViewById(R.id.TextView3);
		TextView Tv4=(TextView) this.findViewById(R.id.TextView4);

	    String bestfriends[] = parts[1].split("@");
	    String popularstars[] = parts[2].split("@");
		
		
		
		for(int i=0; i<bestfriends.length;i++){
			if(i==0)
				tv0.setText(bestfriends[i]);
			if(i==1)
				tv1.setText(bestfriends[i]);
			if(i==2)
				tv2.setText(bestfriends[i]);
			if(i==3)
				tv3.setText(bestfriends[i]);
			if(i==4)
				tv4.setText(bestfriends[i]);    			
		}
		for(int j=0; j<popularstars.length; j++){
			if(j==0)
				Tv0.setText(popularstars[j]);
			if(j==1)
				Tv1.setText(popularstars[j]);
			if(j==2)
				Tv2.setText(popularstars[j]);
			if(j==3)
				Tv3.setText(popularstars[j]);
			if(j==4)
				Tv4.setText(popularstars[j]);
		}
    }
}
