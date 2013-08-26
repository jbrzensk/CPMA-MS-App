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

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;


public class BT_screen_htmlDoc extends BT_activity_base{
	
	private DownloadScreenDataWorker downloadScreenDataWorker;
	private boolean didCreate = false;
	private WebView webView = null;
	private String localFileName = "";
	private String saveAsFileName = "";
	private String dataURL = "";
	private String currentURL = "";
	private String originalURL = "";

	private String showBrowserBarBack = "";
	private String showBrowserBarLaunchInNativeApp = "";
	private String showBrowserBarEmailDocument = "";
	private String showBrowserBarRefresh = "";
	private String forceRefresh = "";
	
	//////////////////////////////////////////////////////////////////////////
	//activity life-cycle events.
	
	//onCreate
	@Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        this.activityName = "BT_screen_htmlDoc";
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
		View thisScreensView = vi.inflate(R.layout.screen_htmldoc, null);
		
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
		localFileName = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "localFileName", "");
		forceRefresh = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "forceRefresh", "0");
		currentURL = dataURL;
		originalURL = dataURL;
		
		//setup the saveAsFileName
		if(localFileName.length() > 1){
			
			//use the file name in the JSON data...
			saveAsFileName = localFileName;			
			
		}else{
		
			//create a file name...
			saveAsFileName = this.screenData.getItemId() + "_screenData.html";
		
		}
		
		//remove file if we are force-refreshing...
		if(forceRefresh.equalsIgnoreCase("1")){
			BT_fileManager.deleteFile(saveAsFileName);
		}		

		
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

			//if we have a cached version, load that...
			if(BT_fileManager.doesCachedFileExist(saveAsFileName) && !forceRefresh.equalsIgnoreCase("1")){
			
				//load from cache
				BT_debugger.showIt(activityName + ": loading from cache: " +  saveAsFileName);
				String theData = BT_fileManager.readTextFileFromCache(saveAsFileName);
				this.showProgress(null, null);
				this.loadDataString(theData);
				
				
			}else{
				
				//load from URL
				BT_debugger.showIt(activityName + ": loading from URL: " +  dataURL);
				String useUrl = BT_strings.mergeBTVariablesInString(dataURL);
				this.downloadAndSaveFile(useUrl, saveAsFileName);
			
			}
			
		}else{
			
			//HTML doc must be in /assets/BT_Docs folder...
			if(!BT_fileManager.doesProjectAssetExist("BT_Docs", saveAsFileName)){
			
				BT_debugger.showIt(activityName + ": ERROR. HTML file \"" + saveAsFileName + "\" does not exist in BT_Docs folder and not URL found? Not loading.");
				showAlert(getString(R.string.errorTitle), getString(R.string.errorLoadingScreen));
			
			}else{
				
				
				//load from BT_Docs folder...
				BT_debugger.showIt(activityName + ": loading from BT_Docs: " +  saveAsFileName);
				this.showProgress(null, null);
				webView.loadUrl("file:///android_asset/BT_Docs/" + saveAsFileName);
				
			}
			
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
    
    
    //load URL in webView...
	public void loadUrl(String theUrl){
		BT_debugger.showIt(activityName + ": loadUrl");
		try{
			webView.loadUrl(theUrl);
		}catch(Exception e){
        	BT_debugger.showIt(activityName + ":loadUrl Exception: " + e.toString());
		}
	}
	
	//load html string...
	public void loadDataString(String theString){
		BT_debugger.showIt(activityName + ": loadDataString");
		webView.loadDataWithBaseURL(null, theString, "text/html", "utf-8", "about:blank");
		hideProgress();
	}	
	

    
    //	Hardware Back-Key behaves like the browser back button. Closes the activity if webview has not been navigated.
    //  Uncomment this to make the hardware back-key move screen "back" regardless of webview's nav-depth    	
	//  Default behavior means the user will need to go "back" the number of times they clicked a link.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
        	showProgress(null, null);
			webView.goBack();
        	return true;
    	}
    	//not the back-key..
    	return super.onKeyDown(keyCode, event);
	}
    
    
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
    		
    		//remove cached version...
    		BT_fileManager.deleteFile(saveAsFileName);
    		
    		//re-download...
    		downloadAndSaveFile(dataURL, saveAsFileName);
    		
    		
    	}else{
        	BT_debugger.showIt(activityName + ":handleRefreshButton cannot refresh?");
    	}
    }
    
    //launch in native app button...
    public void handleLaunchInNativeAppButton(){
    	BT_debugger.showIt(activityName + ":handleLaunchInNativeAppButton");
    	if(currentURL.length() > 1 && originalURL.length() > 1){
    		launchInNativeApp();
    	}else{
    		showAlert(getString(R.string.errorTitle), getString(R.string.cannotOpenDocumentInNativeApp));
        	BT_debugger.showIt(activityName + ":launchInNativeApp NO url?");
    	}
    }
    
    
    //launch in native app
	public void launchInNativeApp(){
	  //tell Android to load the URL in the best available Native App...
		try{
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(currentURL));
			startActivity(i);
		}catch(Exception e){
			BT_debugger.showIt(activityName + ": Error launching native app for url: " + currentURL);
			showAlert(getString(R.string.noNativeAppTitle), getString(R.string.noNativeAppDescription));
		}	    	  
	}		

	//emailDocument
	public void emailDocument(){
		
		//must have already downloaded the document
		if(cpmaandroid_appDelegate.rootApp.getRootDevice().canSendEmail() && BT_fileManager.doesCachedFileExist(saveAsFileName)){
		
			try{
				
				//tell Android launch the native email application...
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
				emailIntent.setType("plain/text");  
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.sharingWithYou));  
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, currentURL);  	    	  
    	  
				//chooser will prompt user if they have more than one email client..
				startActivity(Intent.createChooser(emailIntent, getString(R.string.openWithWhatApp)));  
			
	    	}catch(Exception e){
	    		
	    		BT_activity_base.showAlertFromClass(getString(R.string.errorTitle), getString(R.string.cannotEmailDocument));
	           	BT_debugger.showIt(activityName + ":emailDocument EXCEPTION " + e.toString());
	    	  
	    	}
	    	
		}else{
	   		
			BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.errorTitle), getString(R.string.cannotEmailDocument));
           	BT_debugger.showIt(activityName + ":emailDocument Cannot email document, no URL provided");
			
		}				
	}		


	//see webView (above) where onPageFinished is loading too soon...
	class MyPictureListener implements PictureListener {
	    public void onNewPicture(WebView view, Picture arg1) {
	    	BT_debugger.showIt(activityName + ":MyPictureListener: onNewPicture");
	    	hideProgress();      
	    }    
	}
	
	   //download and save file....
    public void downloadAndSaveFile(String dataURL, String saveAsFileName){
    	
       	//show progress
       	showProgress(null, null);

       	//trigger the download...
      	downloadScreenDataWorker = new DownloadScreenDataWorker();
    	downloadScreenDataWorker.setDownloadURL(dataURL);
    	downloadScreenDataWorker.setSaveAsFileName(saveAsFileName);
    	downloadScreenDataWorker.setThreadRunning(true);
    	downloadScreenDataWorker.start();
       	
       	
    }
   	
	///////////////////////////////////////////////////////////////////
	//DownloadScreenDataThread and Handler
	Handler downloadScreenDataHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			hideProgress();
			
			//read text file, load in webView...
			//if we have a cached version, load that...
			if(BT_fileManager.doesCachedFileExist(saveAsFileName)){
			
				//load from cache
				BT_debugger.showIt(activityName + ": loading from cache: " +  saveAsFileName);
				String theData = BT_fileManager.readTextFileFromCache(saveAsFileName);
				loadDataString(theData);
				
			}else{
				
				//error. We should have downloaded then cached...
	    		BT_activity_base.showAlertFromClass(getString(R.string.errorTitle), getString(R.string.fileNotDownloadedYet));
				BT_debugger.showIt(activityName + ": ERROR loading from cache after download: " +  dataURL);
			
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
			
			 //downloader will fetch and save data..Set this screen data as "current" to be sure the screenId
			 //in the URL gets merged properly. Several screens could be loading at the same time...
			 String useURL = BT_strings.mergeBTVariablesInString(dataURL);
			 BT_debugger.showIt(activityName + ":downloading HTML (plain / text)  data from " + useURL + " Saving As: " + saveAsFileName);
			 BT_downloader objDownloader = new BT_downloader(useURL);
			 objDownloader.setSaveAsFileName(saveAsFileName);
			 @SuppressWarnings("unused")
			String result = objDownloader.downloadTextData();
			
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
		optionsView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		optionsView.setPadding(20, 0, 20, 20); //left, top, right, bottom
		
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
	                emailDocument();
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


 


