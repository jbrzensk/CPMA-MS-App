/*  File Version: 3.0
 *	Copyright, David Book, buzztouch.com
 *
 *	All rights reserved.
 *
 *	Redistribution and use in source and binary forms, with or without modification, are 
 *	permitted provided that the following conditions are met:
 *
 *	Redistributions of source code must retain the above copyright notice which includes the
 *	name(s) of the copyright holders. It must also retain this list of conditions and the 
 *	following disclaimer. 
 *
 *	Redistributions in binary form must reproduce the above copyright notice, this list 
 *	of conditions and the following disclaimer in the documentation and/or other materials 
 *	provided with the distribution. 
 *
 *	Neither the name of David Book, or buzztouch.com nor the names of its contributors 
 *	may be used to endorse or promote products derived from this software without specific 
 *	prior written permission.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 *	IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 *	INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 *	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 *	PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 *	WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 *	ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 *	OF SUCH DAMAGE. 
 */
package com.cpmaandroid;
import static com.cpmaandroid.BT_gcmConfig.EXTRA_MESSAGE;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
// Parse Notification Imports -->
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.PushService;


public class BT_activity_base extends Activity implements LocationListener{

	protected String activityName = "BT_activity_base";
	public static Activity thisActivity = null;
	public BackgroundImageWorkerThread backgroundImageWorkerThread = null;
	public Handler backgroundImageWorkerHandler = null;
	public boolean downloadInProgress = false;
	public Drawable backgroundImage = null;
	public AlertDialog myAlert = null;
	public ProgressDialog progressBox = null;
	public BT_progressSpinner progressSpinner = null;
	protected BT_item screenData = null;
	public BT_item tapScreenLoadObject = null;
	public BT_item tapMenuItemObject = null;
	public String appLastModifiedOnServer = "";
	public RelativeLayout backgroundSolidColorView = null;
	public RelativeLayout backgroundGradientColorView = null;
	public ImageView backgroundImageView = null;
	public LinearLayout baseContentView = null;
	public LinearLayout titleContainerView = null;

	public LocationManager locationManager;
	public int locationUpdateCount = 0;
	public String locationListenerType = "";
	
	//task to register with GCM...
	AsyncTask<Void, Void, Void> gcmRegisterTask;
	public String gcmRegId = "";
	boolean promptForPush = false;
	
	//visible / showing
	public static final int INVISIBLE = 4;
	public static final int VISIBLE = 0;
	
	
	//////////////////////////////////////////////////////////////////////////
	//activity life-cycle events.
	
    //onCreate
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		BT_debugger.showIt(activityName + ":onCreate (BASE CLASS)");	
		
		Parse.initialize(this, "", "");
		BT_debugger.showIt(activityName + ":Parse.initialized");
		
		PushService.setDefaultPushCallback(this, BT_activity_base.class);
		BT_debugger.showIt(activityName + "PushService.setDefault");
		
		ParseInstallation.getCurrentInstallation().saveInBackground();
		BT_debugger.showIt(activityName + "ParseSave IN Background");
		//remember this activity...
        thisActivity = this;
        
		//create handler to configure backgrounds...
		backgroundImageWorkerHandler = new Handler();
		
		///////////////////////////////////////////////////////////////////////
		//register for push logic (gcm registration id may have expired)...
		
		if(cpmaandroid_appDelegate.rootApp.getPromptForPushNotifications().equals("1")  && cpmaandroid_appDelegate.rootApp.getRegisterForPushURL().length() > 3){
			promptForPush = true;
		}

		//if setup for push...
		if(promptForPush){
			try{
		        	
				GCMRegistrar.checkDevice(this);
		        GCMRegistrar.checkManifest(this);
		        	
		        //setup intent to "listen" for messages from GCM...
		        registerReceiver(baseHandlePushReceiver, new IntentFilter("com.cpmaandroid.DISPLAY_MESSAGE"));        	
		        	
		        //get a possible existing gcmRegId...
		        gcmRegId = GCMRegistrar.getRegistrationId(this);

			}catch(java.lang.NoClassDefFoundError e){
				BT_debugger.showIt(activityName + ":Error configuring Push Notification setup. EXCEPTION " + e.toString());
			}
		}
		
		
		//see if this device is already registered...
		if(gcmRegId.length() < 1 && promptForPush){
			
			//prompt user for "allow" push notifications if they have NOT previously said "no thanks"...
            if(BT_fileManager.doesCachedFileExist("rejectedpush.txt")){
				BT_debugger.showIt(activityName + ":Device owner has rejected Push Notifications");
            }else{
            	confirmRegisterForPush();
            }
			
		}
		
		//log message if device is already registered...
		if(gcmRegId.length() > 1){
			BT_debugger.showIt(activityName + ":Device is registered for Google Cloud Messaging (Push) with token: " + gcmRegId);
		}
		
		//register for push logic...
		///////////////////////////////////////////////////////////////////////


        /*
         * set the screenData for this screen BEFORE setting the content view...
         * If this Activity was started with passed-in payload then we started it from 
         * BT_activity_root when a tab was created. In this we use that tabs
         * home-screen data and not the rootApp.currentScreenData. This is because the
         * rootApp.currentScreenData property has not been set yet.
         */
		Intent startedFromIntent = getIntent();
		int tabIndex = startedFromIntent.getIntExtra("tabIndex", -1);
		if(tabIndex > -1){
			
           	//get this tabs home-screen data
        	if(tabIndex == 0) this.screenData = cpmaandroid_appDelegate.rootApp.getTab0ScreenData();
        	if(tabIndex == 1) this.screenData = cpmaandroid_appDelegate.rootApp.getTab1ScreenData();
        	if(tabIndex == 2) this.screenData = cpmaandroid_appDelegate.rootApp.getTab2ScreenData();
        	if(tabIndex == 3) this.screenData = cpmaandroid_appDelegate.rootApp.getTab3ScreenData();
        	if(tabIndex == 4) this.screenData = cpmaandroid_appDelegate.rootApp.getTab4ScreenData();
		
		}else{
			
			//set the screen data..
			this.screenData = cpmaandroid_appDelegate.rootApp.getCurrentScreenData();
			
			//#######################################################################################################
			//if this intent was started with a JSON payload holding the screen data, use it...this means it was
			//passed along in the BT_act_controller.java > loadScreenObject method (see line 335 of that file)
			Bundle extras = getIntent().getExtras();
			if(getIntent().getStringExtra("screenData") != null){
				String tmpJSON = extras.getString("screenData");
		    	try{
			    	JSONObject payloadScreenData = new JSONObject(tmpJSON);
			    	BT_item payloadScreen = new BT_item();
		    		if(payloadScreenData.has("itemId")) payloadScreen.setItemId(payloadScreenData.getString("itemId"));
		    		if(payloadScreenData.has("itemType")) payloadScreen.setItemType(payloadScreenData.getString("itemType"));
		    		if(payloadScreenData.has("itemNickname")) payloadScreen.setItemNickname(payloadScreenData.getString("itemNickname"));
		    		payloadScreen.setJsonObject(payloadScreenData);
		    		
		    		//is this a home screen?
        			if(getIntent().getStringExtra("screenDataIsHomeScreen") != null){
        				String tmpHomeScreen = extras.getString("screenDataIsHomeScreen");
        				if(tmpHomeScreen.equalsIgnoreCase("1")){
        					payloadScreen.setIsHomeScreen(true);
        				}else{
        					payloadScreen.setIsHomeScreen(false);
        				}
        			}	
        			
		    		this.screenData = payloadScreen;
		    	}catch(Exception e){
		    		BT_debugger.showIt("BT_activity_base:onCreate EXCEPTION parsing payloadScreenData's JSON: " + tmpJSON);	
		    	}
		    }
			//#########################################################################################################
			
			//because we are not in the root of a tab we can adjust the status bar dynamically...
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			
			String statusBarStyle = BT_strings.getStyleValueForScreen(this.screenData, "statusBarStyle", "");
			if(statusBarStyle.equalsIgnoreCase("hidden")){
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}else{
			   getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			 
			}
		
		}
                
		//content view for "base" activity is empty (it must be set before sub-classes can set another content view)
        setContentView(R.layout.act_base);
        
        //create a pretend lastModifiedDate file if one does not exist yet...
        if(!BT_fileManager.doesCachedFileExist(cpmaandroid_appDelegate.cachedConfigModifiedFileName)){
        	BT_fileManager.saveTextFileToCache("{this is a pretend date}", cpmaandroid_appDelegate.cachedConfigModifiedFileName);
        }
        
        
	}
	
	//onStart
	@Override 
	protected void onStart() {
		BT_debugger.showIt(activityName + ":onStart (BASE CLASS)");	

		super.onStart();
		//BT_debugger.showIt(activityName + ":onStart");	
		
		//start location manager?
		
		/*	Location Manager Logic (turn on GPS?)
		 	-------------------------------------
			Should this device report it's location? The device will turn on it's GPS and begin
		 	tracking it's location if three things are true:
		 	1) The application's configuration data is set to "startLocationUpdates" 
		 	2) The user has not turned on "allow location tracking" in a BT_screen_settingsLocation
		 	3) The device is capable of tracking it's location (has GPS)
		 */
		//Save battery! We remember the last reported location in the app's delegate so we don't have to turn on the GSP for every screen...
		if(!cpmaandroid_appDelegate.foundUpdatedLocation && cpmaandroid_appDelegate.rootApp.getRootDevice().canUseGPS()){
			if(cpmaandroid_appDelegate.rootApp.getStartLocationUpdates().equalsIgnoreCase("1")){
				BT_debugger.showIt(activityName + ": start GPS is set to YES in the applications configuration data, trying to start GPS");
				if(!BT_strings.getPrefString("userAllowLocation").equalsIgnoreCase("prevent")){
					BT_debugger.showIt(activityName + ": user has not prevented the GPS from starting using a BT_screen_settingsLocation screen");
					
					//trigger method...
					getLastLocation();
					
				}else{
					BT_debugger.showIt(activityName + ": user has prevented the GPS from starting using a BT_screen_settingsLocation screen");
				}
			}else{
				BT_debugger.showIt(activityName + ": start GPS is set to NO in the applications configuration data, not starting GPS");
			}
		}//already found location and saved it in the app's delegate.

		
		
	}
	
    //onResume
    @Override
    public void onResume() {
		BT_debugger.showIt(activityName + ":onResume (BASE CLASS)");	

       super.onResume();
       //BT_debugger.showIt(activityName + ":onResume");
       
		//if this is a home-screen, reportToCloud...
		if(this.screenData.isHomeScreen()){
	
			//must have dataURL's
			if(cpmaandroid_appDelegate.rootApp.getDataURL().length() > 1 && cpmaandroid_appDelegate.rootApp.getReportToCloudURL().length() > 1){
				
				this.reportToCloud();
				
			}else{
		        BT_debugger.showIt(activityName + ":reportToCloud no dataURL and / or reportToCloudURL, both required for remote updates, not reporting.");	
			}
			
		} 			   
	   
	   
   }
    
    //onPause
    @Override
    public void onPause() {
		BT_debugger.showIt(activityName + ":onPause (BASE CLASS)");	

        super.onPause();
        //BT_debugger.showIt(activityName + ":onPause");	
    }
    
    //onStop
	@Override 
	protected void onStop(){
		BT_debugger.showIt(activityName + ":onStop (BASE CLASS)");	

		super.onStop();
        //BT_debugger.showIt(activityName + ":onStop");	
	}	
	
	//onDestroy
    @Override
    public void onDestroy() {
		BT_debugger.showIt(activityName + ":onDestroy (BASE CLASS)");	

		//kill possible GCM registration task...
		if (gcmRegisterTask != null) {
			gcmRegisterTask.cancel(true);
        }
        try{
            unregisterReceiver(baseHandlePushReceiver);
            GCMRegistrar.onDestroy(this);
        }catch(Exception e){
        	//ignore...
        }		
        super.onDestroy();
    }
     
	//handles keyboard hiding and rotation events
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		  switch(newConfig.orientation){
	      		case  Configuration.ORIENTATION_LANDSCAPE:
	      			BT_debugger.showIt(activityName + ":onConfigurationChanged to landscape");
	      			cpmaandroid_appDelegate.rootApp.getRootDevice().updateDeviceOrientation("landscape");
				break;
	      		case Configuration.ORIENTATION_PORTRAIT:
	    			BT_debugger.showIt(activityName + ":onConfigurationChanged to portrait");
	      			cpmaandroid_appDelegate.rootApp.getRootDevice().updateDeviceOrientation("portrait");
	    			break;
	      		case Configuration.ORIENTATION_SQUARE:
	      			BT_debugger.showIt(activityName + ":onConfigurationChanged is square");	
	      			cpmaandroid_appDelegate.rootApp.getRootDevice().updateDeviceOrientation("portrait");
	      			break;
	      		case Configuration.ORIENTATION_UNDEFINED:
	      			BT_debugger.showIt(activityName + ":onConfigurationChanged is unidentified");
	      			cpmaandroid_appDelegate.rootApp.getRootDevice().updateDeviceOrientation("portrait");
	      			break;
	      		default:
	      }	  
	  //update device size so we can keep track...
	  cpmaandroid_appDelegate.rootApp.getRootDevice().updateDeviceSize();
	} 
	//end activity life-cycle events
	//////////////////////////////////////////////////////////////////////////
		
	//show alert message
	public void showAlert(String theTitle, String theMessage) {
		if(theTitle.equals("")) theTitle = "No Alert Title?";
		if(theMessage.equals("")) theMessage = "No alert message?";
		myAlert = new AlertDialog.Builder(this).create();
		myAlert.setTitle(theTitle);
		myAlert.setMessage(theMessage);
		myAlert.setIcon(R.drawable.icon);
		myAlert.setCancelable(false);
		myAlert.setButton("OK", new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	        myAlert.dismiss();
	    } }); 
		myAlert.show();
	}	
	
	public static void showAlertFromClass(String theTitle, String theMessage){
		if(theTitle.equals("")) theTitle = "No Alert Title?";
		if(theMessage.equals("")) theMessage = "No alert message?";
		final AlertDialog theAlert = new AlertDialog.Builder(thisActivity).create();
		theAlert.setTitle(theTitle);
		theAlert.setMessage(theMessage);
		theAlert.setIcon(R.drawable.icon);
		theAlert.setCancelable(false);
		theAlert.setButton("OK", new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	    	  theAlert.dismiss();
	    } }); 
		theAlert.show();	
	}
	
	//show toast
	public void showToast(String theMessage, String shortOrLong){
		Toast toast = null;
		if(shortOrLong.equalsIgnoreCase("short")){
			toast = Toast.makeText(getBaseContext(), theMessage, Toast.LENGTH_SHORT);
		}else{
			toast = Toast.makeText(cpmaandroid_appDelegate.getContext(), theMessage, Toast.LENGTH_LONG);
		}
		toast.show();
	}
	
	
	//show / hide progress (two different types, depending on the message)...
	public void showProgress(String theTitle, String theMessage){
		if(theTitle == null && theMessage == null){
	        progressSpinner = BT_progressSpinner.show(this, null, null, true);
		}else{
			progressBox = ProgressDialog.show(this, theTitle, theMessage, true);
		}
	}
	public void hideProgress(){
		if(progressBox != null){
			progressBox.dismiss();
		}
		if(progressSpinner != null){
			progressSpinner.dismiss();
		}
	}
	
	//handles back button...
	public void onBackPressed(){
		BT_debugger.showIt(activityName + ":onBackPressed");
		BT_debugger.showIt(activityName + ":REVERSING TRANSITIONS ARE DISABLED");
	    super.onBackPressed();
	    
	    //figure out how to reverse a possible custom transition loaded in BT_act_controller.java
	    //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

	}
	
	//confirm refresh...(asked after reportingToCloud)
	public void confirmRefresh(){

		final AlertDialog confirmRefreshAlert = new AlertDialog.Builder(this).create();
		confirmRefreshAlert.setTitle(getString(R.string.confirmRefreshTitle));
		confirmRefreshAlert.setMessage(getString(R.string.confirmRefreshDescription));
		confirmRefreshAlert.setIcon(R.drawable.icon);
		confirmRefreshAlert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	    	  
	    	  //refresh data...
	    	  confirmRefreshAlert.dismiss();
	    	  refreshAppData();
	    	  
	    } }); 
		confirmRefreshAlert.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  confirmRefreshAlert.dismiss();
		    } }); 
		//confirmRefreshAlert.show(); old code
		refreshAppData();
	}		
	
	//refreshes app data...
	public void refreshAppData(){
		BT_debugger.showIt(activityName + ":refreshAppData");
		
		//showToast...
		showToast(getString(R.string.loadingTitle), "long");
		
		//finish this activity...
		this.finish();
		
		//start BT_activity_root with "1" as payload to signal we are refreshing...
	   Intent i = new Intent(this, BT_activity_root.class);
	   i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	   i.putExtra("isRefreshing", 1);
	   startActivity(i);
	   overridePendingTransition(R.anim.fadein, R.anim.fadeout);

		
	}
	
	//confirm register for Push Notifications...
	public void confirmRegisterForPush(){

		final AlertDialog confirmPushAlert = new AlertDialog.Builder(this).create();
		confirmPushAlert.setTitle("Accept Push Notifications?");
		confirmPushAlert.setMessage("This app would like to send you notifications and simple messages. Is this OK?");
		confirmPushAlert.setIcon(R.drawable.icon);
		confirmPushAlert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	    	  
	    	  //dismiss dialgue...
	    	  confirmPushAlert.dismiss();
	    	  
	    	  //register for push...
	    	  registerForPush();
	    	  
	    } }); 
		confirmPushAlert.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  
		    	  //save a text file to remember that user "rejected push"...
		    	  BT_fileManager.saveTextFileToCache("rejected", "rejectedpush.txt");

		    	  //dismiss dialgue..		    	  
		    	  confirmPushAlert.dismiss();
		    	  
		    } }); 
		confirmPushAlert.show();
	}
	
	//register for Push Notifications...
	public void registerForPush(){
		BT_debugger.showIt(activityName + ":registerForPush");
		
  	  	//remove "rejected push" file...
  	  	BT_fileManager.deleteFile("rejectedpush.txt");
		
		boolean doSetupForPush = false;
		if(cpmaandroid_appDelegate.rootApp.getPromptForPushNotifications().equals("1")  && cpmaandroid_appDelegate.rootApp.getRegisterForPushURL().length() > 3){
			doSetupForPush = true;
		}
		
		//see if this device is already registered...
		if(gcmRegId.equals("") && doSetupForPush){
        	BT_debugger.showIt(activityName + ":device is NOT registered with GCM (Google Cloud Messaging)");

        	//GCMRegistration id not available, register now...
        	cpmaandroid_appDelegate.rootApp.setPushRegistrationId("");
			GCMRegistrar.register(this, BT_gcmConfig.SENDER_ID);
			
		}
		
		//if we already have a registration id, make sure it's registered on server...
		if(!gcmRegId.equals("") && doSetupForPush) {
			
			//device is registered on GCM already...
            if(GCMRegistrar.isRegisteredOnServer(this)) {
    		
            	BT_debugger.showIt(activityName + ":device is registered with GCM (Google Cloud Messaging)");
            	cpmaandroid_appDelegate.rootApp.setPushRegistrationId(gcmRegId);

            }else{
                
            	//try to register again, off the UI thread...
                final Context context = this;
                gcmRegisterTask = new AsyncTask<Void, Void, Void>(){
 
                    @Override
                    protected Void doInBackground(Void... params) {

                    	//register on backend server...
                        BT_gcmServerUtils.gcmRegisterOnServer(context, gcmRegId);
                        return null;
                    }
 
                    @Override
                    protected void onPostExecute(Void result) {
                    	gcmRegisterTask = null;
                    }
 
                };
                gcmRegisterTask.execute(null, null, null);
            }			
		} //gcmRegId == ""
		
	}
	
	//unregister for push...
	public void unregisterForPush(){
		BT_debugger.showIt(activityName + ":unregisterForPush");
		
  	  	//save a text file to remember that user "rejected push"...
  	  	BT_fileManager.saveTextFileToCache("rejected", "rejectedpush.txt");
		
       	//try to register again, off the UI thread...
        final Context context = this;
        gcmRegisterTask = new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {

            	//register on backend server...
                BT_gcmServerUtils.gcmUnregisterOnServer(context, gcmRegId);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
            	gcmRegisterTask = null;
            }

        };
        gcmRegisterTask.execute(null, null, null);
  	  	
	}	
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//BackgroundWorkerThread and Handler. Loads a possible image (from bundle, cache, or a URL) then calls handler..

	//handles image download when complete...
	private Runnable setBackgroundImage = new Runnable(){
        public void run(){
			BT_debugger.showIt(activityName + ":setBackgroundImage: returned to UI Thread...");

			//find background image view...
			backgroundImageView = (ImageView) thisActivity.findViewById(R.id.backgroundImageView);
			
			//global theme used if this screen doesn't override a setting...
			BT_item theThemeData = cpmaandroid_appDelegate.rootApp.getRootTheme();
			
			//does the file exist in the project bundle...
			if(backgroundImage != null && backgroundImageView != null){
				BT_debugger.showIt(activityName + ":setBackgroundImage: setting background image");

				//make sure imageView is visible..
	    		backgroundImageView.setVisibility(1);
				
	    		//background scale...
				String backgroundImageScale = "";
				if(BT_strings.getJsonPropertyValue(screenData.getJsonObject(), "backgroundImageScale", "").length() > 1){
					backgroundImageScale = BT_strings.getJsonPropertyValue(screenData.getJsonObject(), "backgroundImageScale", "");
				}else{
					backgroundImageScale = BT_strings.getJsonPropertyValue(theThemeData.getJsonObject(), "backgroundImageScale", "");
				}
					
				if(backgroundImageScale.equalsIgnoreCase("center")) backgroundImageView.setScaleType(ScaleType.CENTER);
				if(backgroundImageScale.equalsIgnoreCase("fullScreen")) backgroundImageView.setScaleType(ScaleType.FIT_XY);
				if(backgroundImageScale.equalsIgnoreCase("fullScreenPreserve")) backgroundImageView.setScaleType(ScaleType.FIT_CENTER);
				if(backgroundImageScale.equalsIgnoreCase("top")) backgroundImageView.setScaleType(ScaleType.FIT_START);
				if(backgroundImageScale.equalsIgnoreCase("bottom")) backgroundImageView.setScaleType(ScaleType.FIT_END);
				if(backgroundImageScale.equalsIgnoreCase("topLeft")) backgroundImageView.setScaleType(ScaleType.FIT_START);
				if(backgroundImageScale.equalsIgnoreCase("topRight")) backgroundImageView.setScaleType(ScaleType.FIT_START);
				if(backgroundImageScale.equalsIgnoreCase("bottomLeft")) backgroundImageView.setScaleType(ScaleType.FIT_END);
				if(backgroundImageScale.equalsIgnoreCase("bottomRight")) backgroundImageView.setScaleType(ScaleType.FIT_END);
				
				//set the image...
				backgroundImageView.setImageDrawable(backgroundImage);
				backgroundImageView.invalidate();
				
				//fade in background image...
				Animation animation = new AlphaAnimation(0.0f, 1.0f);
				animation.setDuration(500);
				backgroundImageView.startAnimation(animation); 
				
				
			}else{
				//BT_debugger.showIt(activityName + ":setBackgroundImage: This screen does not use a background image");
			}
			
			//hideProgress;
			hideProgress();
			
        }//run
    };	
	
    public class BackgroundImageWorkerThread extends Thread{
    	 public void run(){
			try{
				
				//backround image name or url, small or large device...
				String backgroundImageName = "";
				String backgroundImageURL = "";
				if(cpmaandroid_appDelegate.rootApp.getRootDevice().getIsLargeDevice()){
					
					//large device background...
					backgroundImageName = BT_strings.getStyleValueForScreen(screenData, "backgroundImageNameLargeDevice", "");
					backgroundImageURL = BT_strings.getStyleValueForScreen(screenData, "backgroundImageURLLargeDevice", "");
							
				}else{
				
					//large device background...
					backgroundImageName = BT_strings.getStyleValueForScreen(screenData, "backgroundImageNameSmallDevice", "");
					backgroundImageURL = BT_strings.getStyleValueForScreen(screenData, "backgroundImageURLSmallDevice", "");
					
				}//small or large device...
				
				//use a local or cached image if we have one, else, download...
				String useImageName = "";
				if(backgroundImageName.length() > 1){
					useImageName = backgroundImageName;
				}else{
					if(backgroundImageURL.length() > 1){
						useImageName = BT_strings.getSaveAsFileNameFromURL(backgroundImageURL);
					}
				}
				
		    	//does the file exist in the project bundle...
				if(useImageName.length() > 1){
					
					//does image exist in /res/drawable folder...
					if(BT_fileManager.getResourceIdFromBundle("drawable", useImageName) > 0){
						
						BT_debugger.showIt(activityName + ":backgroundWorkerThread using image from project bundle: \"" + useImageName + "\"");
						backgroundImage = BT_fileManager.getDrawableByName(useImageName);
						
					}else{
						
		        		//does file exist in cache...
		        		if(BT_fileManager.doesCachedFileExist(useImageName)){
			        		
		        			BT_debugger.showIt(activityName + ":backgroundWorkerThread using image from cache: \"" + useImageName + "\"");
			        		backgroundImage = BT_fileManager.getDrawableFromCache(useImageName);
		        		
		        		}else{
		        			
		        			//download from URL if we have one...
		        			if(backgroundImageURL.length() > 1){
		        	    		
		        				//if we have a url..
		        	    		if(useImageName.length() > 1){
		        	    			//don't bother pulling name from URL, already have it..
		        				}else{
		        					if(backgroundImageURL.length() > 1){
		        						useImageName = BT_strings.getSaveAsFileNameFromURL(backgroundImageURL);
		        					}
		        				}
		        	    		
		        			 	BT_downloader objDownloader = new BT_downloader(backgroundImageURL);
		        			 	objDownloader.setSaveAsFileName(useImageName);
		        			 	backgroundImage = objDownloader.downloadDrawable();
		        			 	
		        			 	//print to log of failed...
		        			 	if(backgroundImage == null){
		        	    			BT_debugger.showIt(activityName + ":backgroundWorkerThread NOT SAVING iamge to cache (null)");
		        			 	}
		        			}
		        			
		        		}//cached file exists
					}//bundle file exists
				}else{//usesImageName
					BT_debugger.showIt(activityName + ":backgroundWorkerThread this screen does not use a background image");
				}
				
				//fire handler in main UI thread if we have an image...
				if(backgroundImage != null){
					backgroundImageWorkerHandler.post(setBackgroundImage);
				}
				
			}catch(Exception e){
    			BT_debugger.showIt(activityName + ":backgroundWorkerThread Exception: " + e.toString());
			}
			
    	 }//run		 
    };
	//end BackgroundWorkerThread and Handler	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //handles messages after report to cloud thread completes...
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
 			
			//hide progress...
 			hideProgress();
 			
 			//compare, save, continue..
			String cachedConfigModifiedFileName = cpmaandroid_appDelegate.cachedConfigModifiedFileName;
			String previousModified = "";
			
			//parse returned data...
			try{
			
	            JSONObject obj = new JSONObject(appLastModifiedOnServer);
	            if(obj.has("lastModifiedUTC")){
	            	appLastModifiedOnServer = obj.getString("lastModifiedUTC");
	    			BT_debugger.showIt(activityName + ":handleReportToCloudResults appLastModifiedOnServer (value on server): " + appLastModifiedOnServer);
	            }
				
	            //ignore if we don't have a "server date"
	            if(appLastModifiedOnServer.length() > 1){

		            //see if we have a cached lastModified file...
		            if(BT_fileManager.doesCachedFileExist(cachedConfigModifiedFileName)){
		            	previousModified = BT_fileManager.readTextFileFromCache(cachedConfigModifiedFileName);
		    			BT_debugger.showIt(activityName + ":handleReportToCloudResults previousModified (value on device): " + previousModified);
		            }
				
		            //save downloaded lastModifiedDate
	            	BT_fileManager.saveTextFileToCache(appLastModifiedOnServer, cachedConfigModifiedFileName);
	            
		            //do we prompt for refresh?
		    		if(appLastModifiedOnServer.length() > 3 && previousModified.length() > 3){
		    			if(!appLastModifiedOnServer.equalsIgnoreCase(previousModified)){
		    				BT_debugger.showIt(activityName + ":handleReportToCloudResults server data changed, app needs refreshed");
		    				confirmRefresh();
		    			}else{
		    				BT_debugger.showIt(activityName + ":handleReportToCloudResults server data not changed, no refresh needed");
		    			}
		
		    		}
			    		
	            }//appLastModifiedOnServer
	            
			}catch(Exception e){
				BT_debugger.showIt(activityName + ":handleReportToCloudResults EXCEPTION processing results: " + e.toString());
			} 			
 			
 		}
	};
	
	//reportToCloud
	public void reportToCloud(){
		BT_debugger.showIt(activityName + ":reportToCloud");			

		new Thread(){
			
			@Override
			public void run(){
				
		   		//prepare looper...
	    		Looper.prepare();

				//dataURL and reportToCloudURL may be empty or not used at all...
				String dataURL = BT_strings.mergeBTVariablesInString(cpmaandroid_appDelegate.rootApp.getDataURL());
				String reportToCloudURL = BT_strings.mergeBTVariablesInString(cpmaandroid_appDelegate.rootApp.getReportToCloudURL());
				
	            //do we have a data URL for remote updates?
        		if(dataURL.length() < 1){
        			BT_debugger.showIt(activityName + ":reportToCloudWorkerThread does not use a dataURL, automatic updates disabled.");			
        		}
        		if(reportToCloudURL.length() < 1){
        			BT_debugger.showIt(activityName + ":reportToCloudWorkerThread does not use a reportToCloudURL, automatic updates disabled.");			
        		}						
        		
				//if we have a dataURL AND a reportToCloudURL...report to cloud...
				if(dataURL.length() > 5 && reportToCloudURL.length() > 5){
	    			
					//if we have a currentMode, append it to the end of the URL...
					if(cpmaandroid_appDelegate.rootApp.getCurrentMode().length() > 1){
						reportToCloudURL += "&currentMode=" + cpmaandroid_appDelegate.rootApp.getCurrentMode();
					}					
					
					BT_debugger.showIt(activityName + ":reportToCloudWorkerThread getting lastModified from reportToCloudURL " + reportToCloudURL);
				
	   	    		BT_downloader objDownloader = new BT_downloader(reportToCloudURL);
	   	    		objDownloader.setSaveAsFileName("");
	   	    		appLastModifiedOnServer = objDownloader.downloadTextData();

				}//dataURL.length() 
	 				
				//send message...
				sendMessageToMainThread(0);
				
			}
			
			//send message....
			private void sendMessageToMainThread(int what){
				Message msg = Message.obtain();
				msg.what = what;
				mHandler.sendMessage(msg);
			}
			
		}.start();
		
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
    

     
     
 	////////////////////////////////////////////////////////////////////
     //Location Manager Methods
     public void getLastLocation(){
         try{
         	//only ask for location info "once" when app launches (saves battery)
 			if(!cpmaandroid_appDelegate.foundUpdatedLocation){
 				locationUpdateCount = 0;
 	        	if(this.locationManager == null){
 	        		this.locationUpdateCount = 0;
 	        		this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);   
 	        	}
 	        	if(this.locationManager != null){
 	            	Location lastLocation = this.locationManager.getLastKnownLocation("gps");
 	            	if(lastLocation != null){
 	            	    
 	            		
 	            		//remember in delegate
 	            		cpmaandroid_appDelegate.rootApp.getRootDevice().setDeviceLatitude(String.valueOf(lastLocation.getLatitude()));
 	            		cpmaandroid_appDelegate.rootApp.getRootDevice().setDeviceLongitude(String.valueOf(lastLocation.getLongitude()));

 	            		String s = "";
 	            		//s += " Updated: " + lastLocation.getTime();
 	            		s += " Lat: " + lastLocation.getLatitude();
 	            		s += " Lon: " + lastLocation.getLongitude();
 	            		s += " Accuracy: " + lastLocation.getAccuracy();
 	            		BT_debugger.showIt(activityName + ":getLastLocation " + s);
 	            		
 	            	}
 	            	//start listening for location updates if we have GPS enabled...
 	            	if(this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
 	            		locationListenerType = "GPS";
 	            		startListening();
 	            	}else{
 	            		if(this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
 	            			locationListenerType = "NETWORK/CELL";
 	            			startListening();
 	            		}else{
 	 	            		BT_debugger.showIt(activityName + ":getLastLocation Can't start GPS or Network Services to get location info.");
 	            		}
 	            	}
 	            	
 	        	}else{
 	        		BT_debugger.showIt(activityName + ":getLastLocation locationManager == null?");
 	        	}
 			}
         }catch (Exception je){
      		BT_debugger.showIt(activityName + ":getLastLocation EXCEPTION " + je.toString());
 	    }
     }
 	
 	//LocationListener must implement these methods
 	public void onProviderDisabled(String theProvider){
  		BT_debugger.showIt(activityName + ":onProviderDisabled The GPS is disabled on this device.");
 	};    
 	public void onProviderEnabled(String theProvider){
  		BT_debugger.showIt(activityName + ":onProviderDisabled The GPS is enabled on this device.");
 	};
 	public void onLocationChanged(Location location){
 		this.locationUpdateCount++;
  		BT_debugger.showIt(activityName + ":onLocationChanged The device's location changed.");
   		
  		try{

  			/*
  			 	Example of how to get the device's current location in any code you write anywhere in your app....

  				String myLatitude = cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceLatitude();
  				String myLongitude = cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceLongitude();
  				
  				Then convert myLatitude and myLongitude strings to doubles, integers, floats as needed.
  			 
  			*/
  			
	 	    cpmaandroid_appDelegate.rootApp.getRootDevice().setDeviceLatitude(String.valueOf(location.getLatitude()));
	 	    cpmaandroid_appDelegate.rootApp.getRootDevice().setDeviceLongitude(String.valueOf(location.getLongitude()));
	 	    
	 		String s = "";
     		s += "From: " + locationListenerType;
	 		//s += " Updated: " + location.getTime();
	 		s += " Lat:: " + location.getLatitude();
	 		s += " Lon:: " + location.getLongitude();
	 		s += " Accuracy:: " + location.getAccuracy();
	 		BT_debugger.showIt(activityName + ":onLocationChanged " + s);
	 		
	 		//stop listening after 10 reports (about 10 seconds) or if we have good accuracy faster....
	 		if(locationUpdateCount > 10 || location.getAccuracy() < 25){
	 	  		BT_debugger.showIt(activityName + ":onLocationChanged turning off GPS to save battery, saved last location.");
	 			
	 	  		//flag foundUpdatedLocation in the delgate so other screens don't turn on the GPS	 	  		
	 	  		cpmaandroid_appDelegate.foundUpdatedLocation = true;
	 	  		
	 	  		//stop listening (kill the locationManager)...
	 			stopListening();
	 			
	 		}
  		}catch(Exception e){
  			
  		}
  	};
 	public void onStatusChanged(String theProvider, int status, Bundle extras){
 		try{
 			BT_debugger.showIt(activityName + ":onStatusChanged (for the location manager)");
 		}catch(Exception e){
 			
 		}
 	};
 	//start listening..
 	public void startListening(){
 		BT_debugger.showIt(activityName + ":startListening (started listening for location changes)");
 		try{
 			if(this.locationManager != null){
 				
 				//we we started this in getLastLocation() we set a flag to tell us what type of service to setup..
 				if(locationListenerType == "GPS"){
 					//request updates from GPS...
     				this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
     				BT_debugger.showIt(activityName + ":startListening asking for GPS locations updates...");
 				}
 				if(locationListenerType == "NETWORK/CELL"){
 					//request updates from Network (Cell Towwers, Wi-Fi)...
     				this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
     				BT_debugger.showIt(activityName + ":startListening asking for Network (Cell Tower or Wi-Fi) location updates...");
 				}
 				
  			}
 		}catch(Exception e){
 		}
     }
 	//stop listening..
 	public void stopListening(){
 		BT_debugger.showIt(activityName + ":startListening (stopped listening to location changes)");
        try{ 
        	if(this.locationManager != null){
        		this.locationManager.removeUpdates(this);
        		this.locationManager = null;
        	}
        }catch(Exception e){
        	
        }
     }
 	//END location methods
 	////////////////////////////////////////////////////////////////////

 	
 	////////////////////////////////////////////////////////////////////
 	//Broadcast Receiver to handle Push Notifications...
    private final BroadcastReceiver baseHandlePushReceiver = new BroadcastReceiver() {
    	@Override
        public void onReceive(Context context, Intent intent) {
 	 		BT_debugger.showIt(activityName + ":BroadcastReceiver baseHandlePushReceiver");
 	 		String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
 	 		
            //wake device if sleeping...
			BT_gcmWakeLocker.acquire(getApplicationContext());
	 
			//show quick toast so user knows a new message arrived...
			showToast(newMessage, "short");
	            
	        //releasing wake lock
			BT_gcmWakeLocker.release();
				
        }//getIntent
    }; 	
 	////////////////////////////////////////////////////////////////////
 	
    
    
 	//get / set screenData... 	
 	public BT_item getScreenData() {
		return this.screenData;
	}

	public void setScreenData(BT_item theScreenData) {
		this.screenData = theScreenData;
	}     
     

}
































