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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebView.PictureListener;
import android.widget.Button;
import android.widget.LinearLayout;


public class BT_screen_customURL extends BT_activity_base{
	
	private boolean didCreate = false;
	private WebView webView = null;
	private String dataURL = "";
	private String currentURL = "";
	private String originalURL = "";
	private AlertDialog confirmLaunchInNativeAppDialogue = null;
	private AlertDialog confirmEmailDocumentDialogue = null;

	private String showBrowserBarBack = "";
	private String showBrowserBarLaunchInNativeApp = "";
	private String showBrowserBarEmailDocument = "";
	private String showBrowserBarRefresh = "";
	
	//////////////////////////////////////////////////////////////////////////
	//activity life-cycle events.
	
	//onCreate
	@Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        this.activityName = "BT_screen_customURL";
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
		View thisScreensView = vi.inflate(R.layout.screen_customurl, null);
		
		
		//add the view to the base view...
		baseView.addView(thisScreensView);
	     
		//reference to the webview in the layout file.
		webView = (WebView) thisScreensView.findViewById(R.id.webView);
		webView.setBackgroundColor(0);
		webView.setInitialScale(0);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setPluginsEnabled(true);
    	webView.setPictureListener(new MyPictureListener());
		webView.setWebViewClient(new WebViewClient(){
	    	
	    	@Override
	    	public boolean shouldOverrideUrlLoading(WebView view, String url){
				
	    		//remember the URL...
	    		currentURL = url;
	    		
	    		//load the URL in the app's built-in browser if it's in our list of types to load...
	    		if(BT_act_controller.canLoadDocumentInWebView(url)){
	    			
	    			//load url in built-in browser...
	    			showProgress(null, null);
	    			return false;	    			

	    		
	    		}else{
	    			
	    			//ask user what app to open this in if the method returned NO...
	    			try{
	    				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	    				startActivity(Intent.createChooser(i, getString(R.string.openWithWhatApp)));  
	    			}catch(Exception e){
	    				BT_debugger.showIt(activityName + ": Error launching native app for url: " + url);
	    				showAlert(getString(R.string.noNativeAppTitle), getString(R.string.noNativeAppDescription));
	    			}
	    			
	    			//do not try to load the URL..
	    			return true;	    			
	    			

	    		}
	    		
	        }
	        
	    	@Override
	        public void onPageFinished(WebView view, String url){
	    		hideProgress();
			    BT_debugger.showIt(activityName + ":onPageFinished finished Loading: " + url);
	        }
	    	
	    	@Override
	    	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
	    		hideProgress();
	    		showAlert(getString(R.string.errorTitle), getString(R.string.errorLoadingScreen));
	    		BT_debugger.showIt(activityName = ":onReceivedError ERROR loading url: " + failingUrl + " Description: " + description);
	    	}	    	
        
        });	
		
    	
		//fill JSON properties...
		dataURL = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "dataURL", "");
		currentURL = dataURL;
		originalURL = dataURL;
		
		//button options for hardware menu key...
		showBrowserBarBack = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarBack", "0");
		showBrowserBarLaunchInNativeApp = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarLaunchInNativeApp", "0");
		showBrowserBarEmailDocument = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarEmailDocument", "0");
		showBrowserBarRefresh = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarRefresh", "0");
		
		
		//prevent user interaction?
		String preventUserInteraction = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "preventUserInteraction", "0");
		if(preventUserInteraction.equalsIgnoreCase("1")){
			//can't seem to get Android to "prevent user interaction"...??? 
		}
		
		//hide scroll bars..
		String hideVerticalScrollBar = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "hideVerticalScrollBar", "0");
		if(hideVerticalScrollBar.equalsIgnoreCase("1")){
			webView.setVerticalScrollBarEnabled(false);
		}
		String hideHorizontalScrollBar = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "hideHorizontalScrollBar", "0");
		if(hideHorizontalScrollBar.equalsIgnoreCase("1")){
			webView.setHorizontalScrollBarEnabled(false);
		}
		String preventAllScrolling = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "preventAllScrolling", "0");
		if(preventAllScrolling.equalsIgnoreCase("1")){
			webView.setVerticalScrollBarEnabled(false);
			webView.setHorizontalScrollBarEnabled(false);
		}		

		
		//figure out what to load...
		if(dataURL.length() > 1){

			String useUrl = BT_strings.mergeBTVariablesInString(dataURL);
			BT_debugger.showIt(activityName + ": loading URL from: " + useUrl);
			this.loadUrl(useUrl);
			
		}else{
			BT_debugger.showIt(activityName + ": No URL found? Not loading web-view!");
			showAlert(getString(R.string.errorTitle), getString(R.string.errorLoadingScreen));
		}
		
		
        //flag as created..
        didCreate = true;
        
    	/*
		 * *******************************************************************
		 
		 Notes: This screen has fully loaded it's layout file at this point. 
		 		 
		 BT_activity_base loaded act_base.xml 
		 --THEN--
		 this screen loaded it's own layout file. 
		 
		 JSON properties for this screen from the app's configuration data are 
		 available for use. You can read these properties easily using the BT_strings class.
		 
		 Example: 
		 String thisScreensNavBarTitleText = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "thisScreensNavBarTitleText", "default value here");
		 
		 The screens background and title bar were setup using the BT_viewUtilities class:
		 BT_viewUtilities.updateBackgroundColorsForScreen() 
		 
		 You are free to modify the layout file and extend this screen however you want. 
		 
		 ********************************************************************
		*/		
        
        
 		
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
		
       //verify onCreate already ran...
       if(didCreate){
    	   
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
    
    
    //load URL in webView
	public void loadUrl(String theUrl){
		BT_debugger.showIt(activityName + ": loadUrl");
		try{
			webView.loadUrl(theUrl);
		}catch(Exception e){
        	BT_debugger.showIt(activityName + ":loadUrl Exception: " + e.toString());
		}
	}
	

    
    /* 	Hardware Back-Key, uncomment as needed.
     	Uncomment this to make the hardware back-key act like a browser back button
     	Warning: If you do this the user will need to go "back" the number of times they
     	when "forward" to return to the previous screen.
    	@Override
    	public boolean onKeyDown(int keyCode, KeyEvent event){
    		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
            	webView.goBack();
            	return true;
        	}
        	//not the back-key..
        	return super.onKeyDown(keyCode, event);
    	}
    */
    
    //back button...
    public void handleBackButton(){
    	BT_debugger.showIt(activityName + ":handleBackButton");
    	if(webView.canGoBack()){
            webView.goBack();
        }else{
        	BT_debugger.showIt(activityName + ":handleBackButton cannot go back?");
        }
    }
    
    //refresh button...
    public void handleRefreshButton(){
    	BT_debugger.showIt(activityName + ":handleRefreshButton");
    	if(currentURL.length() > 1){
    		showProgress(null, null);
    		webView.loadUrl(currentURL);
    	}else{
        	BT_debugger.showIt(activityName + ":handleRefreshButton cannot refresh?");
    	}
    }
    
    //launch in native app button...
    public void handleLaunchInNativeAppButton(){
    	BT_debugger.showIt(activityName + ":handleLaunchInNativeAppButton");
    	if(currentURL.length() > 1 && originalURL.length() > 1){
    		confirmLaunchInNativeApp();
    	}else{
    		showAlert(getString(R.string.errorTitle), getString(R.string.cannotOpenDocumentInNativeApp));
        	BT_debugger.showIt(activityName + ":handleLaunchInNativeAppButton NO url?");
    	}
    }
    
    //handle email button
    public void handleEmailDocumentButton(){
    	BT_debugger.showIt(activityName + ":handleLaunchInNativeAppButton");
    	showAlert(getString(R.string.errorTitle), getString(R.string.cannotEmailDocument));
        BT_debugger.showIt(activityName + ":handleEmailDocumentButton NO  document to email on URL screens");
    }
    
    //confirm launch in native app
	public void confirmLaunchInNativeApp(){
		confirmLaunchInNativeAppDialogue = new AlertDialog.Builder(this).create();
		confirmLaunchInNativeAppDialogue.setTitle(getString(R.string.confirm));
		confirmLaunchInNativeAppDialogue.setMessage(getString(R.string.confirmLaunchInNativeBrowser));
		confirmLaunchInNativeAppDialogue.setIcon(R.drawable.icon);
		
		//YES
		confirmLaunchInNativeAppDialogue.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	    	  confirmLaunchInNativeAppDialogue.dismiss();
	    	  
	    	  //tell Android to load the URL in the best available Native App...
  				try{
  					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(currentURL));
  					startActivity(i);
  				}catch(Exception e){
  					BT_debugger.showIt(activityName + ": Error launching native app for url: " + currentURL);
  					showAlert(getString(R.string.noNativeAppTitle), getString(R.string.noNativeAppDescription));
  				}	    	  
	    } }); 
		
		//NO
		confirmLaunchInNativeAppDialogue.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				confirmLaunchInNativeAppDialogue.dismiss();
		    } }); 
		
		//show the confirmation box...
		confirmLaunchInNativeAppDialogue.show();
	}		

	//confirm email document
	public void confirmEmailDocumentDialogue(){
		confirmEmailDocumentDialogue = new AlertDialog.Builder(this).create();
		confirmEmailDocumentDialogue.setTitle(getString(R.string.confirm));
		confirmEmailDocumentDialogue.setMessage(getString(R.string.confirmEmailDocument));
		confirmEmailDocumentDialogue.setIcon(R.drawable.icon);
		
		//YES
		confirmEmailDocumentDialogue.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	    	  confirmEmailDocumentDialogue.dismiss();
	    	  
	    	  //tell Android launch the native email application...
	    	  Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
	    	  emailIntent.setType("plain/text");  
	    	  emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.sharingWithYou));  
	    	  emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, currentURL);  	    	  
	    	  
	    	  //chooser will propmpt user if they have more than one email client..
	    	  startActivity(Intent.createChooser(emailIntent, getString(R.string.openWithWhatApp)));  
	    	  
	    } }); 
		
		//NO
		confirmEmailDocumentDialogue.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				confirmEmailDocumentDialogue.dismiss();
		    } }); 
		
		//show the confirmation box...
		confirmEmailDocumentDialogue.show();
	}		


	//see webView (above) where onPageFinished is loading too soon...
	class MyPictureListener implements PictureListener {
	    public void onNewPicture(WebView view, Picture arg1) {
	    	//BT_debugger.showIt("here" + ":onNewPicture done");
	    	hideProgress();      
	    }    
	}
	
	
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

		//back...
		if(showBrowserBarBack.equalsIgnoreCase("1")){
			final Button btn = new Button(this);
			btn.setText(getString(R.string.browserBack));
			btn.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	                dialog.cancel();
	                handleBackButton();
	            }
	        });
			options.add(btn);
		}

		//launch in native app...
		if(showBrowserBarLaunchInNativeApp.equalsIgnoreCase("1")){
			final Button btn = new Button(this);
			btn.setText(getString(R.string.browserOpenInNativeApp));
			btn.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	                dialog.cancel();
	                handleLaunchInNativeAppButton();
	            }
	        });
			options.add(btn);
		}	
		
		//email document...
		if(showBrowserBarEmailDocument.equalsIgnoreCase("1")){
			final Button btn = new Button(this);
			btn.setText(getString(R.string.browserEmailDocument));
			btn.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	                dialog.cancel();
	                handleEmailDocumentButton();
	            }
	        });
			options.add(btn);
		}

		//refresh page...
		if(showBrowserBarRefresh.equalsIgnoreCase("1")){
			final Button btn = new Button(this);
			btn.setText(getString(R.string.browserRefresh));
			btn.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	                dialog.cancel();
					handleRefreshButton();
	            }
	        });
			options.add(btn);
		}		
		
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
			options.get(x).setLayoutParams(btnLayoutParams);
			options.get(x).setPadding(5, 5, 5, 5);
			optionsView.addView(options.get(x));
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


 


