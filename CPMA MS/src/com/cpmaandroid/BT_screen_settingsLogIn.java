/*
 *	Copyright 2011, David Book, buzztouch.com
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

import java.util.ArrayList;
import org.json.JSONObject;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;



public class BT_screen_settingsLogIn extends BT_activity_base{
	
	public boolean didLoadData = false;
	public boolean didCreate = false;
	private String JSONData = "";
	private DownloadScreenDataWorker downloadScreenDataWorker;
	
	private String dataURL = "";
	
	private TextView logInIdLabel = null;
	private EditText logInIdText = null;
	private TextView logInPasswordLabel = null;
	private EditText logInPasswordText = null;
	private Button submitButton = null;
	private ImageView iconStatusView = null;
	
	private String textOnBackgroundColor = "";
	private String labelLogInId = "";
	private String labelPassword = "";
	
	//results from server...
	String userGuid = "";
	String userDisplayName = "";
	String userEmail = "";
	
	//////////////////////////////////////////////////////////////////////////
	//activity life-cycle events.
	
	//onCreate
	@Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        this.activityName = "BT_screen_settingsLogIn";
		BT_debugger.showIt(activityName + ":onCreate");	
		
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
		
		///////////////////////////////////////////////////////////////////
		//values from JSON in screen data...
		dataURL = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "dataURL", "");
		textOnBackgroundColor = BT_strings.getStyleValueForScreen(this.screenData, "textOnBackgroundColor", "#FFFFFF");
		labelLogInId = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "labelLogInId", getString(R.string.logInIdLabel));
		labelPassword = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "labelPassword", getString(R.string.logInPasswordLabel));

		//inflate this screens layout file..
		LayoutInflater vi = (LayoutInflater)thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View thisScreensView = vi.inflate(R.layout.screen_settingslogin, null);
	
		//reference to parts...
		
		//logInIdLabel
		logInIdLabel = (TextView) thisScreensView.findViewById(R.id.logInIdLabel);
			logInIdLabel.setTextColor(BT_color.getColorFromHexString(textOnBackgroundColor));
			logInIdLabel.setText(labelLogInId);
		
		//logInIdText
		logInIdText = (EditText) thisScreensView.findViewById(R.id.logInIdText);
		logInIdText.setText(BT_strings.getPrefString("userLogInId"));
		
		//logInPasswordLabel
		logInPasswordLabel = (TextView) thisScreensView.findViewById(R.id.logInPasswordLabel);
			logInPasswordLabel.setTextColor(BT_color.getColorFromHexString(textOnBackgroundColor));
			logInPasswordLabel.setText(labelPassword);
			
		//logInPasswordText
		logInPasswordText = (EditText) thisScreensView.findViewById(R.id.logInPasswordText);
		
		//statusImage
		iconStatusView = (ImageView) thisScreensView.findViewById(R.id.iconStatusView);

		//submitButton
		submitButton = (Button) thisScreensView.findViewById(R.id.submitButton);
	       	if(cpmaandroid_appDelegate.rootApp.getRootUser().getIsLoggedIn() == true){
	    		submitButton.setText(getString(R.string.logOut));
				iconStatusView.setImageDrawable(BT_fileManager.getDrawableByName("dot_green.png"));
	    	}else{
	    		submitButton.setText(getString(R.string.logInSubmit));
				iconStatusView.setImageDrawable(BT_fileManager.getDrawableByName("bt_blank.png"));
	    	}
		
	    //invalidate icon...
	    iconStatusView.invalidate();
	       	
		//submit click listener..
		submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
            	if(cpmaandroid_appDelegate.rootApp.getRootUser().getIsLoggedIn()){
            		logOut();
            	}else{
            		logIn();
            	}
            }
        });	        
		
		//add the view to the base view...
		baseView.addView(thisScreensView);
		
       	if(cpmaandroid_appDelegate.rootApp.getRootUser().getIsLoggedIn() == true){
       		showToast(getString(R.string.loggedInYes), "long");
       	}else{
       		showToast(getString(R.string.loggedInNo), "long");
       	}
		
		//flag as created..
        didCreate = true;
        
 		
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
		if(downloadScreenDataWorker != null){
			boolean retry = true;
			downloadScreenDataWorker.setThreadRunning(false);
			while(retry){
				try{
					downloadScreenDataWorker.join();
					retry = false;
				}catch (Exception je){
				}
			}
		}
	}
	
	
	//onDestroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        //BT_debugger.showIt(activityName + ":onDestroy");	
    }
    
	//end activity life-cycle events
	//////////////////////////////////////////////////////////////////////////
    
    //login
    public void logIn(){
    	BT_debugger.showIt(activityName + ":logIn");
    	
    	if(logInIdText.getText().length() < 1 || logInPasswordText.getText().length() < 1){
    		showAlert(getString(R.string.errorTitle), getString(R.string.logInIdAndPasswordRequired));
    	}else{
    		
    		//remember some values...
	    	BT_strings.setPrefString("userLogInId", logInIdText.getText().toString());
	    	BT_strings.setPrefString("userLogInPassword", logInPasswordText.getText().toString());
    		
	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserLogInId(logInIdText.getText().toString());  
	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserLogInPassword(logInPasswordText.getText().toString());  
   		
	        if(dataURL.length() > 1){
	        	showProgress(null, null);
		      	downloadScreenDataWorker = new DownloadScreenDataWorker();
	        	downloadScreenDataWorker.setDownloadURL(dataURL);
	        	downloadScreenDataWorker.setSaveAsFileName("");
	        	downloadScreenDataWorker.setThreadRunning(true);
	        	downloadScreenDataWorker.start();
	        }else{
	            BT_debugger.showIt(activityName + ":refreshScreenData NO DATA URL for this screen? Not downloading.");	
	        }  
	        
    	}
    	
    }
    
    //logout
    public void logOut(){
    	BT_debugger.showIt(activityName + ":logOut");
    	
		//forget prefs
    	BT_strings.setPrefString("userId", "");
    	BT_strings.setPrefString("userGuid", "");
    	BT_strings.setPrefString("userDisplayName", "");
    	BT_strings.setPrefString("userEmail", "");
    	BT_strings.setPrefString("userLogInId", "");
    	BT_strings.setPrefString("userLogInPassword", "");
    	
		//forget values in rootUser (logout)
    	cpmaandroid_appDelegate.rootApp.getRootUser().setIsLoggedIn(false);
    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserId("");
    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserDisplayName("");
    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserEmail("");
    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserLogInId("");
    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserLogInPassword("");
    	
    	//clear text boxes...
    	logInPasswordText.setText("");
    	
    	//show alert
    	showAlert(getString(R.string.logOutSuccessTitle), getString(R.string.logOutSuccessDescription));

    	//update label, icon..
		submitButton.setText(getString(R.string.logInSubmit));
		iconStatusView.setImageDrawable(BT_fileManager.getDrawableByName("dot_red.png"));

		
    	
    }
    
    //parse results...
    public void parseScreenData(String JSONString){
    	BT_debugger.showIt(activityName + ":parseScreenData " + JSONString);
    	hideProgress();
    	
    	//values from server...
    	boolean validLogIn = false;

       	try{

            //if theJSONString is empty, look for child items in this screen's config data..
    		if(JSONString.length() < 10){
    			validLogIn = false;
    		}else{
        		JSONObject raw = new JSONObject(JSONString);
        		if(raw.has("result")){
        			JSONObject result = raw.getJSONObject("result");
        			if(result.has("status")){
        				if(result.getString("status").equalsIgnoreCase("valid")){
        					
        					//flag
        					validLogIn = true;
        					if(result.has("userGuid")) userGuid = result.getString("userGuid");
        					if(result.has("userDisplayName")) userGuid = result.getString("userDisplayName");
        					if(result.has("userEmail")) userGuid = result.getString("userEmail");
      					
        				}
        			}
        		}
    		
    		}
    		
    		//valid or invalid?
    		if(validLogIn){
    			
    			//success
    			
    			//remember prefs
    	    	BT_strings.setPrefString("userId", userGuid);
    	    	BT_strings.setPrefString("userGuid", userGuid);
    	    	BT_strings.setPrefString("userDisplayName", userDisplayName);
    	    	BT_strings.setPrefString("userEmail", userEmail);
    	    	BT_strings.setPrefString("userLogInId", logInIdText.getText().toString());
    	    	BT_strings.setPrefString("userLogInPassword", logInPasswordText.getText().toString());
    	    	
    			//remember values in rootUser (logout)
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setIsLoggedIn(true);
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserId(userGuid);
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserDisplayName(userDisplayName);
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserEmail(userEmail);
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserLogInId(logInIdText.getText().toString());
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserLogInPassword(logInPasswordText.getText().toString());
    			
    	    	//toast...
    	    	if(userDisplayName.length() > 1){
    	    		showToast(getString(R.string.welcome), "long");
    	    	}
    	    	
    	    	//clear password box
    	    	logInPasswordText.setText("");

    	    	//show alert
    	    	showAlert(getString(R.string.logInSuccessTitle), getString(R.string.logInSuccessDescription));

    	    	//set button, image...
	    		submitButton.setText(getString(R.string.logOut));
				iconStatusView.setImageDrawable(BT_fileManager.getDrawableByName("dot_green.png"));

    		}else{
    			
    			//failed
    			
      			//forget prefs
    	    	BT_strings.setPrefString("userId", "");
    	    	BT_strings.setPrefString("userGuid", "");
    	       	BT_strings.setPrefString("userDisplayName", "");
    	    	BT_strings.setPrefString("userEmail", "");
    	    	BT_strings.setPrefString("userLogInId", "");
    	    	BT_strings.setPrefString("userLogInPassword", "");
    	    	
    			//forget values in rootUser (logout)
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setIsLoggedIn(false);
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserId("");
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserDisplayName("");
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserEmail("");
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserLogInId("");
    	    	cpmaandroid_appDelegate.rootApp.getRootUser().setUserLogInPassword("");
    			
    	    	//show alert...
    	    	showAlert(getString(R.string.logInFailedTitle), getString(R.string.logInFailedDescription));
    	    	
       	    	//set button, image...
	    		submitButton.setText(getString(R.string.logOut));
				iconStatusView.setImageDrawable(BT_fileManager.getDrawableByName("dot_red.png"));
    	    	
    		}
    		
    		
       	}catch(Exception e){
       		validLogIn = false;
       	}
    	
    	
    }
    
    ///////////////////////////////////////////////////////////////////
	//DownloadScreenDataThread and Handler
	Handler downloadScreenDataHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			if(JSONData.length() < 1){
				hideProgress();
				showAlert(getString(R.string.errorTitle), getString(R.string.errorDownloadingData));
			}else{
				parseScreenData(JSONData);
			}
		}
	};	   
    
	public class DownloadScreenDataWorker extends Thread{
		 boolean threadRunning = false;
		 String downloadURL = "";
		 String saveAsFileName = "";
		 void setThreadRunning(boolean bolRunning){
			 threadRunning = bolRunning;
		 }	
		 void setDownloadURL(String theURL){
			 downloadURL = theURL;
		 }
		 void setSaveAsFileName(String theFileName){
			 saveAsFileName = theFileName;
		 }
		 @Override 
    	 public void run(){
			
			 //downloader fetches data from login dataURL..
			 cpmaandroid_appDelegate.rootApp.setCurrentScreenData(screenData);
			 String useURL = BT_strings.mergeBTVariablesInString(dataURL);
			 BT_debugger.showIt(activityName + ":downloading login result from " + useURL);
			 BT_downloader objDownloader = new BT_downloader(useURL);
			 objDownloader.setSaveAsFileName("");
			 JSONData = objDownloader.downloadTextData();
			
			 //send message to handler..
			 this.setThreadRunning(false);
			 downloadScreenDataHandler.sendMessage(downloadScreenDataHandler.obtainMessage());
   	 	
		 }
	}	
	//END DownloadScreenDataThread and Handler
	///////////////////////////////////////////////////////////////////
   
    
    
    
    
	/////////////////////////////////////////////////////
	//options menu (hardware menu-button)
	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) { 
		super.onPrepareOptionsMenu(menu); 
		
		 //set up dialog
        final Dialog dialog = new Dialog(this);
        
		//linear layout holds all the options...
		LinearLayout optionsView = new LinearLayout(this);
		optionsView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		optionsView.setOrientation(LinearLayout.VERTICAL);
		optionsView.setGravity(Gravity.CENTER_VERTICAL);
		optionsView.setPadding(0, 0, 0, 20);
		
		//options have individual layout params
		LinearLayout.LayoutParams btnLayoutParams = new LinearLayout.LayoutParams(400, LayoutParams.WRAP_CONTENT);
		btnLayoutParams.setMargins(10, 10, 10, 10);
		btnLayoutParams.leftMargin = 10;
		btnLayoutParams.rightMargin = 10;
		btnLayoutParams.topMargin = 0;
		btnLayoutParams.bottomMargin = 10;
		
		//holds all the options 
		ArrayList<Button> options = new ArrayList<Button>();

		//cancel...
		final Button btnCancel = new Button(this);
		btnCancel.setText(getString(R.string.okClose));
		btnCancel.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                dialog.cancel();
            }
        });
		options.add(btnCancel);
		
		//refreshAppData (if we are on home screen)
		if(this.screenData.isHomeScreen() && cpmaandroid_appDelegate.rootApp.getDataURL().length() > 1){
			
			final Button btnRefreshAppData = new Button(this);
			btnRefreshAppData.setText(getString(R.string.refreshAppData));
			btnRefreshAppData.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	                dialog.cancel();
					refreshAppData();
	            }
	        });
			options.add(btnRefreshAppData);			
		}		
		
		//add each option to layout, set layoutParams as we go...
		for(int x = 0; x < options.size(); x++){
            Button btn = (Button)options.get(x);
            btn.setTextSize(18);
            btn.setLayoutParams(btnLayoutParams);
            btn.setPadding(5, 5, 5, 5);
			optionsView.addView(btn);
		}
		
	
		//set content view..        
        dialog.setContentView(optionsView);
        if(options.size() > 1){
        	dialog.setTitle(getString(R.string.menuOptions));
        }else{
        	dialog.setTitle(getString(R.string.menuNoOptions));
        }
        
        //show
        dialog.show();
		return true;
		
	} 
	//end options menu
	/////////////////////////////////////////////////////
	    
    
    
}







