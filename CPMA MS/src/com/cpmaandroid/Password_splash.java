/*
 *	Copyright 2012, David Book
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

import org.json.JSONObject;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class Password_splash extends BT_activity_base{

	int formX = 0;
	int formY = 0;
	String secretPassword = "";
	String rememberSecretOnDevice = "";
	String secretPasswordWarning = "";
	String passwordLabel = "";
	String passwordLabelColor = "";
	String buttonText = "";
	String buttonTextColor = "";
	String buttonImageFileName = "";
	
	//layout parts...
	LinearLayout frmView = null;
	TextView lblPassword = null;
	EditText txtPassword = null;
	Button btnSubmit = null;
	
	
 	//////////////////////////////////////////////////////////////////////////
	//activity life-cycle events.

	//onCreate
	@Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        this.activityName = "Password_splash";
		BT_debugger.showIt(activityName + ":onCreate");	
		
		//reference to base layout..
		LinearLayout baseView = (LinearLayout)findViewById(R.id.baseView);
		
		//setup background colors...
		BT_viewUtilities.updateBackgroundColorsForScreen(this, this.screenData);
		
		//setup background images..
		if(backgroundImageWorkerThread == null){
			backgroundImageWorkerThread = new BackgroundImageWorkerThread();
			backgroundImageWorkerThread.start();
		}			
		
		//setup navigation bar...
		LinearLayout navBar = BT_viewUtilities.getNavBarForScreen(this, this.screenData);
		if(navBar != null){
			baseView.addView(navBar);
		}
		
		//inflate this screens layout file...
		LayoutInflater vi = (LayoutInflater)thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View thisScreensView = vi.inflate(R.layout.screen_password_splash, null);
		
		//add the view to the base view...
		baseView.addView(thisScreensView);
	     
		//figure out default x value (center of screen)...(frm view is 175 pixels wide)...
		int defaultX = ( (cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceWidth() / 2) - (175 / 2));
		String tmpDefaultX = "" + defaultX;
		
		//fill properties from JSON data...
		formX = Integer.parseInt(BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "formXSmallDevice", tmpDefaultX));
		formY = Integer.parseInt(BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "formYSmallDevice", "0"));
		secretPassword = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "secretPassword", "0");
		rememberSecretOnDevice = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "rememberSecretOnDevice", "0");
		secretPasswordWarning = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "secretPasswordWarning", "The password you entered is incorrect. Please try again.");
		passwordLabel = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "passwordLabel", "Password");
		passwordLabelColor = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "passwordLabelColor", "#000000");
		buttonText = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "buttonText", "Submit");
		buttonTextColor = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "buttonTextColor", "#000000");
		buttonImageFileName = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "buttonImageFileName", "ps_submit.png");

		//update x,y if this is a large device...
		if(cpmaandroid_appDelegate.rootApp.getRootDevice().getIsLargeDevice()){
			formX = Integer.parseInt(BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "formXLargeDevice", tmpDefaultX));
			formY = Integer.parseInt(BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "formYLargeDevice", "0"));
		}
		
		//get reference to layout objects...
		frmView = (LinearLayout) thisScreensView.findViewById(R.id.frmView);
		lblPassword = (TextView) thisScreensView.findViewById(R.id.lblPassword);
		txtPassword = (EditText) thisScreensView.findViewById(R.id.txtPassword);
		btnSubmit = (Button) thisScreensView.findViewById(R.id.btnSubmit);

		//colors for text, labels...
		lblPassword.setText(passwordLabel);
		lblPassword.setTextColor(BT_color.getColorFromHexString(passwordLabelColor));
		btnSubmit.setText(buttonText);
		btnSubmit.setTextColor(BT_color.getColorFromHexString(buttonTextColor));
		
		//update background image for button...
		btnSubmit.setBackgroundDrawable(BT_fileManager.getDrawableByName(buttonImageFileName));
		
		//click listener for button...
		btnSubmit.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	submitCick();
            }
        }); 		
		
		//if the secret password already saved on the device...
	    String saveSecretName = "secret_" + secretPassword;
	    if(secretPassword.length() > 1 && BT_strings.getPrefString(saveSecretName).equalsIgnoreCase(secretPassword)){
		
			BT_debugger.showIt(activityName + ": secret password exists on device");	
	    	animateSplashScreen();
	    
		}else{
	    	
			//layout screen to position the form....
			layoutScreen(getResources().getConfiguration().orientation);
	 	    	
	    }
		
		
		
		
		
	}//onCreate

	//onStart
	@Override 
	protected void onStart() {
		//BT_debugger.showIt(activityName + ":onStart");	
		super.onStart();
	}
	
    //onResume
    @Override
    public void onResume() {
       super.onResume();
       	//BT_debugger.showIt(activityName + ":onResume");
		
		//get values..
		String transitionType = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "transitionType", "");
		
		//setup transition... 
		if(transitionType.length() > 2){
			delayHandler.removeCallbacks(mDelayTask);
			delayHandler.postDelayed(mDelayTask, (2 * 1000));
		}
       
    }
    
    //onPause
    @Override
    public void onPause() {
        super.onPause();
        //BT_debugger.showIt(activityName + ":onPause");	
    }
    
    //onStop
	@Override 
	protected void onStop(){
		super.onStop();
        //BT_debugger.showIt(activityName + ":onStop");	
	}	
	
	//onDestroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        //BT_debugger.showIt(activityName + ":onDestroy");	
    }
    
	//end activity life-cycle events
	//////////////////////////////////////////////////////////////////////////
 
    //submit click...
    public void submitCick(){
        BT_debugger.showIt(activityName + ":submitCick");	
        
	    String saveSecretName = "secret_" + secretPassword;
	    
	    if(txtPassword.getText().toString().length() > 1 && secretPassword.equalsIgnoreCase(txtPassword.getText().toString())){
	    
	    	//save to prefs if we are "remembering" the value...
	    	if(rememberSecretOnDevice.equalsIgnoreCase("1")){
	    		BT_strings.setPrefString(saveSecretName, txtPassword.getText().toString());
	    	}
	    	
	        //hide keyboard...
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);
	        
	        //animate screen away...
	        animateSplashScreen();
	 
	    	
	    }else{
	    	
	    	//show alert...
	    	showAlert("Please try again", secretPasswordWarning);
	    	
	    }

        
    }
    
    
    //layoutScreen...
    public void layoutScreen(int configuration){
        BT_debugger.showIt(activityName + ":layoutScreen");	
      
		//figure out default x value (center of screen)...
		int defaultX = (cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceWidth() / 2) - 150;
         
        switch(configuration){
  			case  Configuration.ORIENTATION_LANDSCAPE:
  				BT_debugger.showIt(activityName + ":layoutScreen to landscape");
  				defaultX = (cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceHeight() / 2) - 150;
  				break;
   			default:
        }	          
        
        //trick to string...
		String tmpDefaultX = "" + defaultX;

		formX = Integer.parseInt(BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "formXSmallDevice", tmpDefaultX));
		formY = Integer.parseInt(BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "formYSmallDevice", "20"));
        
		//update x,y if this is a large device...
		if(cpmaandroid_appDelegate.rootApp.getRootDevice().getIsLargeDevice()){
			formX = Integer.parseInt(BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "formXLargeDevice", tmpDefaultX));
			formY = Integer.parseInt(BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "formYLargeDevice", "20"));
		}
		
        //setup layout params for frmView....
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300, 175);
		params.setMargins(formX, formY, 10, 10);
						 //left, top, right, bottom);
		frmView.setLayoutParams(params);
		

    }
    
	//handles keyboard rotation events
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		layoutScreen(newConfig.orientation);
	} 
  
    
	/////////////////////////////////////////////////////////////////////
	//handles question delay updates after each answer
	Handler delayHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			delayHandler.removeCallbacks(mDelayTask);
		}
	};		
	
	private Runnable mDelayTask = new Runnable() {
		public void run() {
		    animateSplashScreen();
		}
	};	
	//end timer stuff
	/////////////////////////////////////////////////////////////////////

	
	//handles animation...
	public void animateSplashScreen(){
		BT_debugger.showIt(activityName + ": animateSplash");
			
		
		//finish this screen so it cannot be returned to...
		Password_splash.this.finish();
		
		//init the next screen object...
		BT_item tmpLoadScreenObject = null;
		
		//create a generic, blank menu item to pass to the loadScreenObject method...
		BT_item tmpMenuItemObject = new BT_item();
		tmpMenuItemObject.setItemId("tempMenuItem");
		tmpMenuItemObject.setItemNickname("tempMenuItem");
		try{
			tmpMenuItemObject.setItemType("BT_menuItem");
			tmpMenuItemObject.setJsonObject(new JSONObject("{\"transitionType\":\"fade\"}"));
		}catch(Exception e){
			
		}		
		
		
		//next screen to load...either tabbed home or the first screen in the list...
		if(cpmaandroid_appDelegate.rootApp.getTabs().size() > 0){
			BT_debugger.showIt("Loading tabbed interface...");
			
			//load BT_activity_root_tabs...
			tmpLoadScreenObject = new BT_item();
			tmpLoadScreenObject.setItemId("tmpRootTabs");
			tmpLoadScreenObject.setItemNickname("tmpRootTabs");
			try{
				tmpLoadScreenObject.setItemType("BT_activity_root_tabs");
				tmpLoadScreenObject.setJsonObject(new JSONObject("{}"));
			}catch(Exception e){
				
			}
			
		}else{
			
			//find the app's home screen...
			tmpLoadScreenObject = cpmaandroid_appDelegate.rootApp.getHomeScreen();
			
			//flag it as the home screen...
			tmpLoadScreenObject.setIsHomeScreen(true);
			
		}
		
		//load...
		if(tmpLoadScreenObject != null){
		
			//load screen object...
			BT_act_controller.loadScreenObject(this, this.screenData, tmpMenuItemObject, tmpLoadScreenObject);
		
		}

		
	}
	
	
	
	
	
}


 


