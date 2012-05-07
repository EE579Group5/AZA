package aza.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Message;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bump.api.IBumpAPI;
import com.bump.api.BumpAPIIntents;

//****************************************//
//Modify the codes relating to socket and processing message.
//

public class BumpActivity extends Activity
{
    private IBumpAPI api;
    Socket clientSocket = null;//socket connected to server
   	Socket fileSocket = null;
  	DataOutputStream out=null;
  	DataInputStream in=null;
  	String meetId = "-1";//record the meeting id given by server
  	String myId = "2";
  	String yourId = "0";//the id of people i meet
	
  	byte[] clientInputBuffer = new byte[11000];
  	byte[] clientOutputBuffer = new byte[11000];
    Timer timer;
    int flag=0;
	Message msg = new Message();
	String  slatitude,slongitude, syourlan,syourlon;
	int i = 10;
    int connectflag=0;
    int counter = 0;
	double  latitude = 0;
    double longitude= 0;
    double yourlan=0;
    double yourlon=0;
    double distance=0;
    String recMsg;
    long channelID;
    int unmatchcount=0;
    
    ImageButton b1;
    ImageButton b2;
    ImageButton b3;
    ImageButton b4;
    //SimpleDateFormat formatter = new SimpleDateFormat("MMddhhmmss");     
		//Date curDate = new Date(System.currentTimeMillis());    
    SharedPreferences.Editor editor;
    SharedPreferences settings;
    
    private ClientSocket cskt = new ClientSocket();   
    
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.i("BumpTest", "onServiceConnected");
            api = IBumpAPI.Stub.asInterface(binder);
            try {
                api.configure("dc3a9ec6cf3447b59ac2ad6085cdafe0",myId);//configure bump api
            } catch (RemoteException e) {
                Log.w("BumpTest", e);
            }
            Log.d("Bump Test", "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d("Bump Test", "Service disconnected");
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            try {
                if (action.equals(BumpAPIIntents.DATA_RECEIVED)) {
                    Log.i("Bump Test", "Received data from: " + api.userIDForChannelID(intent.getLongExtra("channelID", 0))); 
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    recMsg = new String(intent.getByteArrayExtra("data"));
                  	setTitle(recMsg);

                    if(recMsg.equals("Hello, world!")){
                    	syourlan = "0";
                      syourlon = "0";
                    	//setTitle("hello world");
                      counter=0;
           	  			  i=10;
           	  		 	  connectflag=0;
           	  		 	  setTitle("Match with each other");
        							//setTitle("i:"+i+"   counter:"+counter);
	            		    //long time=System.currentTimeMillis();
	            		    //setTitle(Long.toString(time));
                    }
                    else if(recMsg.equals("over")){
                    	i=0;
                    	setTitle("meeting is over");
                    	
                    }
                    else{
                    	String[] stype = recMsg.split("#") ;
                    	syourlan = stype[0];
                      syourlon = stype[1];
                      
                    	yourlan = Double.parseDouble(syourlan);
                    	yourlon = Double.parseDouble(syourlon);
                    	distance = getDistance(yourlon, yourlan, longitude,latitude);
         	  		 	  //setTitle("distance is "+distance);
                    	setTitle("connected");
                    	//public static double getDistance(double longt1, double lat1, double longt2,double lat2)
                    	if(distance > 50){
                      	connectflag++;
                    	}
                      //three condition will end the meeting
                      //1. counter >1000; 2. distance >50; 3.click button "disconnect" 
                    	if(connectflag>3||counter>60){
                    		i=0;
                    		setTitle("counter = 0");
                    	}

                    }
                    
                    //##########################################################################################################
                    Log.i("Bump Test", "Data: " + recMsg);
                } else if (action.equals(BumpAPIIntents.MATCHED)) {//the two client match with each other
                    long channelID = intent.getLongExtra("proposedChannelID", 0); 
                    Log.i("Bump Test", "Matched with: " + api.userIDForChannelID(channelID));
                    api.confirm(channelID, true);
                    Log.i("Bump Test", "Confirm sent");
                } else if (action.equals(BumpAPIIntents.CHANNEL_CONFIRMED)) {//confirm the connection
                	  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                	  	//physical bump
                      channelID = intent.getLongExtra("channelID", 0);//get channel id given by bump server
                      yourId = api.userIDForChannelID(channelID);
                      Log.i("Bump Test", "Channel confirmed with " + yourId);
                      setTitle("socket connection haha, my id is "+ myId);
                      api.send(channelID, "Hello, world!".getBytes());//send confirm msg to the other client
                      b1.setVisibility(View.VISIBLE);
                      b2.setVisibility(View.VISIBLE);
                      
                      //socket open
                      if(myId.compareTo(yourId) > 0){//the client whose id is lager send the meeting info to server
                      	setTitle("socket send");

                      	cskt.InitializeClient();//open socket to server
			            			
		            				Log.i("Bump Test", "Channel confirmed with " );
                    		
                    	  //send msg to server to confirm the beginning of the meeting
                    		//communicate with server to tell the beginning of this meeting
		            				 
		            				//String curtime = formatter.format(curDate);
		            				long time=System.currentTimeMillis();

                    		String meetInfo = "Meet#"+myId+"#"+yourId+"#"+time+"#";
                    		try {
                    			cskt.SendMessage(meetInfo);
                    			if(cskt.ReceiveMessage()){
                    				ProcessMessage(cskt.parts);
                    			}
                    			//ProcessMessage(meetInfo);//send msg and receive the meeting id
                        	setTitle("meeting info haha:"+meetId);
                    		} catch (IOException e) {
                    			// TODO Auto-generated catch block
                    			e.printStackTrace();
                    		}
                    		setTitle(meetInfo);
                  	  	
                      }
                    //######################################################################################
                } else if (action.equals(BumpAPIIntents.NOT_MATCHED)) {
                    Log.i("Bump Test", "Not matched.");
                    unmatchcount++;
                    setTitle(unmatchcount+" not match, my id is "+myId);
                    ///////////////////////////////////////////////////////////////////////////////////////

                    
                    //########################################################################################
                } else if (action.equals(BumpAPIIntents.CONNECTED)) {
                    Log.i("Bump Test", "Connected to Bump...");
                    api.enableBumping();
                }
            } catch (RemoteException e) {}
        } 
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bump);
        settings = this.getSharedPreferences("AZAClient", Context.MODE_PRIVATE);
    	  editor = settings.edit();
        myId = settings.getString("UID", "Default");
        
        bindService(new Intent(IBumpAPI.class.getName()),
                    connection, Context.BIND_AUTO_CREATE);
        Log.i("BumpTest", "boot");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BumpAPIIntents.CHANNEL_CONFIRMED);
        filter.addAction(BumpAPIIntents.DATA_RECEIVED);
        filter.addAction(BumpAPIIntents.NOT_MATCHED);
        filter.addAction(BumpAPIIntents.MATCHED);
        filter.addAction(BumpAPIIntents.CONNECTED);
        registerReceiver(receiver, filter);
        //////////////////////////////////////////////////////////////////////////////////////////////////
        //button 1
        //keeps simulating bump when the meet is going
    	  //keeps tracking the status of this meeting
        //open the GPS and ACCELEROMETER	
    		openGPSSettings();
        
		    //final Texiew tv1 = (TextView) findViewById(R.id.tv1);

    	  b1 = (ImageButton) findViewById(R.id.imageButton1);//connect
    	  b2 = (ImageButton) findViewById(R.id.imageButton2);//disconnect
    	  b3 = (ImageButton) findViewById(R.id.imageButton3);//close
    	  b4 = (ImageButton) findViewById(R.id.imageButton4);//friends list
    	  //Button b5 = (Button) findViewById(R.id.button1);

    	 // tv1.setText("myid is "+myId+" yourid is "+yourId);

    	  Log.i("group5", Thread.currentThread().getName());

    	  final Handler handler = new Handler() {
    	  	@Override
    	  	public void handleMessage(Message msg) {
    	  		super.handleMessage(msg);
    	  		if(msg.what!=0){//if simulate bump match, keeps calling timer task
          		getLocation();
          		
    	  			try {
								api.send(channelID, (slatitude+"#"+slongitude+"#").getBytes());
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//double distance = getDistance(yourlon, yourlan, longitude,latitude);
    	  			//tv1.setText(counter+"~~distance is "+distance+"\n");

    	  		}else{
    	  			
    	  			//tv1.append("button 1 Meet over!!!!");//if simulate bump not match, stop timer
    	  			timer.cancel();
    	  			//send to inform server that we are apart
      				long time=System.currentTimeMillis();

    	  			String meetInfo = "Apart#"+meetId+"#"+time+"#";

    	  			try {
								api.send(channelID, ("over").getBytes());
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(myId.compareTo(yourId) > 0){
								
								try {
	          			cskt.SendMessage(meetInfo);
	        			  if(cskt.ReceiveMessage()){
	        				  ProcessMessage(cskt.parts);
	        			  }//send msg and receive the meeting id

	          		} catch (IOException e) {
	          			// TODO Auto-generated catch block
	          			e.printStackTrace();
	          		}
								
								//tv1.append("handler: I've send the server apart");
								//close socket with server
								try {
									cskt.closeSocket();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							counter = 0;
       	                     i=10;
       	                    connectflag=0;
							setTitle("i:"+i+"   counter:"+counter);
							setTitle("Meeting is over!");
							b1.setVisibility(View.INVISIBLE);
			                b2.setVisibility(View.INVISIBLE);	
    	  		}
    	  	}
    	  };
    	  
    	  b1.setOnClickListener(new View.OnClickListener() 
    	  {
    	  	 @Override
    	  	 public void onClick(View arg0) {
    	  		 timer = new Timer();
    	  		 timer.schedule(new TimerTask() {
    	  			 @Override
    	  			 public void run() {
    	  				 Log.i("yao", Thread.currentThread().getName());
    	  				 Message msg = new Message();
    	  				 msg.what = i;
    	  				 counter++;
    	  				 handler.sendMessage(msg);
    	  			}
    	  		}, 1000, 3000);//(how long to start, intertime)
    	  	 }
    	  });
    		//disconnect button
    	  b2.setOnClickListener(new View.OnClickListener() 
    	  {
    	  	 @Override
    	  	 public void onClick(View arg0) {
    	  		 
    	  		 //tv1.append("button 2 Meet over!!!!");//if simulate bump not match, stop timer
   	  			 setTitle("Meet is over!");
   	  	      	 b1.setVisibility(View.INVISIBLE);
                 b2.setVisibility(View.INVISIBLE);	
    	  		 timer.cancel();
   	  			 //send to inform server that we are apart
     				long time=System.currentTimeMillis();

   	  			 String meetInfo = "Apart#"+meetId+"#"+time+"#";
   	  			 if(counter>0){
   	  				try {
  							api.send(channelID, ("over").getBytes());
  						} catch (RemoteException e) {
  							// TODO Auto-generated catch block
  							e.printStackTrace();
  						}
  						if(myId.compareTo(yourId) > 0){
  							
  							try {
            			cskt.SendMessage(meetInfo);
          			  if(cskt.ReceiveMessage()){
          				  ProcessMessage(cskt.parts);
          			  }//send msg and receive the meeting id
            		} catch (IOException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
            		
            		try {
  								cskt.closeSocket();
  							} catch (IOException e) {
  								// TODO Auto-generated catch block
  								e.printStackTrace();
  							}
   	  			 }
  					
  				  
  				  counter=0;
   	  			  i=10;
   	  		 	  connectflag=0;
							//tv1.append("button 2:I've send the server apart");
							//setTitle("i:"+i+"   counter:"+counter);
							setTitle("button 2:I've send the server apart");

						}
    	  		 
    	  	 }
    	  });
    	  
        b3.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View v){
        		setTitle("oh, you are gonna exit this application");
        		System.exit(1);

        	}
        });
        
        b4.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View v){
        		Intent intentFriendList = new Intent();
    				intentFriendList.setClass(BumpActivity.this,FriendListActivity.class);
    				startActivity(intentFriendList);

        	}
        });

        /*
        b5.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View v){
            long time=System.currentTimeMillis();
   		      setTitle(Long.toString(time));

        	}
        });*/
    		        
    }

     public void onStart() {
        Log.i("BumpTest", "onStart");
        super.onStart();
     }
     
     public void onRestart() {
        Log.i("BumpTest", "onRestart");
        super.onRestart();
     }

     public void onResume() {
        Log.i("BumpTest", "onResume");
        super.onResume();
     }

     public void onPause() {
        Log.i("BumpTest", "onPause");
        super.onPause();
     }

     public void onStop() {
        Log.i("BumpTest", "onStop");
        super.onStop();
     }

     public void onDestroy() {
        Log.i("BumpTest", "onDestroy");
        unbindService(connection);
        unregisterReceiver(receiver);
        super.onDestroy();
     }
     
     ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     //below is what i wrote
     //open socket and transfer string to PC server
     public void openSocket(String ipAddr, int portNum) throws IOException{
          	try {
       			clientSocket = new Socket(ipAddr, portNum);
       			out = new DataOutputStream(clientSocket.getOutputStream());
       			in = new DataInputStream(clientSocket.getInputStream());
       		} catch (UnknownHostException e) {
       			System.err.println("Don't know about host.");
       			setTitle(e.getMessage());
       			//System.exit(1);
       		} catch (IOException e) {
       			//System.err.println("Couldn't get I/O for the connection to: aludra.");
       			//System.exit(1);
       			setTitle(e.getMessage());
       		}
     }
   	//process the msg send back by the server, and react based on the msg type
   	public void ProcessMessage(String[] parts) throws NumberFormatException, IOException{
   		//String[] type = msg.split("#");
   		if (parts[0].equals("Reg")) {
   			setTitle("Registration finish!");
   		}
   		if (parts[0].equals("Meet")) {
   			meetId = parts[1];
   			setTitle("meeting id is "+ meetId);
   		}
   		if (parts[0].equals("Apart")) {
   			if (parts[1].equals("ack"))
   				setTitle("meeting over confirm");

   		}
   		if (parts[0].equals("Call")) {
   			meetId = parts[1];
   			setTitle("call timestamp is "+meetId);
   		}
   		if (parts[0].equals("Msg")) {
   			meetId = parts[1];
   			setTitle("msg time stamp is "+meetId);
   		}
   	}
   	
    //close socket
    public void closeSocket() throws IOException{
    	out.close();
  		in.close();
    	clientSocket.close();
    	
    }		
    //###############################################################################################################
    //the following code about GPS is incited from http://www.cnblogs.com/fly_binbin/archive/2010/12/16/1908518.html
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void openGPSSettings(){
    	LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	
//    	if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
    	if (alm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
      Toast.makeText(this, "GPS works fine", Toast.LENGTH_SHORT).show();
      return;
      }
    	Toast.makeText(this, "please open GPS!", Toast.LENGTH_SHORT).show();
      Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
      startActivityForResult(intent,0);//this returns to the screen after setup
    }
    
    private void getLocation(){
      // get location management service
      LocationManager locationManager;
      String serviceName = Context.LOCATION_SERVICE;
      locationManager = (LocationManager) this.getSystemService(serviceName);
      // search for service information
      Criteria criteria = new Criteria();
      criteria.setAccuracy(Criteria.ACCURACY_FINE); //high accuracy
      criteria.setAltitudeRequired(false);
      criteria.setBearingRequired(false);
      criteria.setCostAllowed(true);
      criteria.setPowerRequirement(Criteria.POWER_LOW); //low power

      String provider = locationManager.getBestProvider(criteria, true); //get GPS information
      Location location = locationManager.getLastKnownLocation(provider); //get location through GPS
      updateToNewLocation(location);
      //set listener, min update time is(1*1000 in one second)or min location change is n miles
      locationManager.requestLocationUpdates(provider, 100 * 1000, 500,locationListener);
    }
    
    private void updateToNewLocation(Location location) {
			//final TextView tv2 = (TextView) findViewById(R.id.tv2);
      if (location != null) {
      	latitude = location.getLatitude();
        longitude= location.getLongitude();
        slatitude=Double.toString(latitude);
        slongitude=Double.toString(longitude);
        //tv2.setText(slatitude+"; "+slongitude+"\n");
      }else{
      	slatitude="0";
      	slongitude="0";
      }

    }
    
    private LocationListener locationListener = new LocationListener(){
      public void onLocationChanged(Location location) {
          Log.d("Location", "onLocationChanged");
          Log.d("Location", "onLocationChanged Latitude" + location.getLatitude());
          Log.d("Location", "onLocationChanged location" + location.getLongitude());
          updateToNewLocation(location);
      }

      public void onProviderDisabled(String provider) {
          Log.d("Location", "onProviderDisabled");
      }
      public void onProviderEnabled(String provider) {
          Log.d("Location", "onProviderEnabled");
      }
      public void onStatusChanged(String provider, int status, Bundle extras) {
          Log.d("Location", "onStatusChanged");
      }
    };

    //get the distance of two client
    private final static double PI = 3.14159265358979323;//pi
    private final static double Ra = 6371229;//radius of the earth

    public static double getDistance(double longt1, double lat1, double longt2,double lat2) {
        double x, y, distance;
        x = (longt2-longt1)*PI*Ra*Math.cos(((lat1 + lat2)/2)*PI/180)/180;
        y = (lat2 - lat1)*PI*Ra/180;
        distance = Math.hypot(x, y);
        return distance;
    }

    //######################################################################################################
}
