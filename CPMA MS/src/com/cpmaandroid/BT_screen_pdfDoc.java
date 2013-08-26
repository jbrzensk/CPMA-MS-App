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

import java.io.File;
import java.util.ArrayList;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class BT_screen_pdfDoc extends BT_activity_base{
	
	private boolean didCreate = false;
	private String dataURL = "";
	private String localFileName = "";
	private String saveAsFileName = "";
	Button openWithButton;
	Button downloadButton;
	ImageView documentIconView;
	private DownloadScreenDataWorker downloadScreenDataWorker;

	private String showBrowserBarEmailDocument = "";
	private String showBrowserBarLaunchInNativeApp = "";
	private String forceRefresh = "";
	
	
	//////////////////////////////////////////////////////////////////////////
	//activity life-cycle events.
	
	//onCreate
	@Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        this.activityName = "BT_screen_pdfDoc";
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
		View thisScreensView = vi.inflate(R.layout.screen_pdfdoc, null);
		
		//add the view to the base view...
		baseView.addView(thisScreensView);
	     
		//reference to the document type icon...
		documentIconView = (ImageView) thisScreensView.findViewById(R.id.documentTypeIcon);
		documentIconView.setImageDrawable(BT_fileManager.getDrawableByName("screen_pdfdoc.png"));
		
		//reference to "open" and "donwload" buttons...
		openWithButton = (Button) thisScreensView.findViewById(R.id.openWithButton);
		downloadButton = (Button) thisScreensView.findViewById(R.id.downloadButton);
		
		//fill JSON properties...
		localFileName = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "localFileName", "");
		dataURL = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "dataURL", "");
		showBrowserBarEmailDocument = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarEmailDocument", "");
		showBrowserBarLaunchInNativeApp = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarLaunchInNativeApp", "");
		forceRefresh = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "forceRefresh", "");
		
		//setup the saveAsFileName
		if(localFileName.length() > 1){
			
			//use the file name in the JSON data...
			saveAsFileName = localFileName;			
			
			//copy the file from the /assets/BT_Docs folder to the cache if it doesn't already exist...
			if(!BT_fileManager.doesCachedFileExist(saveAsFileName)){
				BT_fileManager.copyAssetToCache("BT_Docs", saveAsFileName);
			}
			
		}else{
			saveAsFileName = this.screenData.getItemId() + "_screenData.pdf";
		}
		
		//remove file if we are force-refreshing...
		if(forceRefresh.equalsIgnoreCase("1") && dataURL.length() > 1){
			BT_fileManager.deleteFile(saveAsFileName);
		}

		
		
		//change click event for "open with" button...
		openWithButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	openDocInCache(saveAsFileName);
            }
        });       
       
       
		//change click event for "document image"...
		documentIconView.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	openDocInCache(saveAsFileName);
            }
        });       
	
		//change click event for "document image"...
		documentIconView.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	openDocInCache(saveAsFileName);
            }
        }); 
		
		//add face effect to image (it's not a button, it's an image)...
		AlphaAnimation alphaFade = new AlphaAnimation(0.3f, 1.0f);
		alphaFade.setDuration(500);
		alphaFade.setFillAfter(true);
		documentIconView.startAnimation(alphaFade);
		
		//click event of the download button...
		downloadButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				downloadAndSaveFile(dataURL, saveAsFileName);
            }
            
		});
		
		//if file is not in the cache, disable the openWith button...
		if(!BT_fileManager.doesCachedFileExist(saveAsFileName)){
			openWithButton.setEnabled(false);
		}
			
		//hide download button if we don't have a URL...
		if(dataURL.length() < 5){
			downloadButton.setVisibility(View.GONE);
		}else{
			//if we have a dataURL, AND we have a cached "saveAsFileName", change label to "refresh"...
			if(BT_fileManager.doesCachedFileExist(saveAsFileName)){
				downloadButton.setText(getString(R.string.refreshFromURL));
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
    
	//email document
	public void emailDocument(){
		
		//must have already downloaded the document
		if(cpmaandroid_appDelegate.rootApp.getRootDevice().canSendEmail() && BT_fileManager.doesCachedFileExist(saveAsFileName)){
		
			try{
	    		  
	    		//copy file from cache to SDCard so emailer can access it (can't email from internal files directory)...
	    		BT_fileManager.copyFileFromCacheToSDCard(saveAsFileName);
	    		  
	  		   	//copy from assets to internal cache...
	            File file = new File(cpmaandroid_appDelegate.getApplication().getExternalCacheDir(), saveAsFileName);
	    		String savedToPath = file.getAbsolutePath();
	            
	    		//make sure file exists...
	    		if(file.exists()){
	    		
	    			//send from path...THIS IS REQUIRED OR GMAIL CLIENT WILL NOT INCLUDE ATTACHMENT
	    			String sendFromPath = "file:///sdcard/Android/data/com.daviddebug/cache/" + saveAsFileName;

	    			//tell Android launch the native email application...
	    			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
	    			emailIntent.setType("text/plain");
	    			emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.sharingWithYou));  
	    			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "\n\n" + getString(R.string.attachedFile));  	    	  
	    			emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(sendFromPath));
                  
		    	  //open users email app...
		    	  startActivity(emailIntent);
	    		
	    		}else{
		           	BT_debugger.showIt(activityName + ":emailDocument Cannot email document, file does not exist: " + savedToPath);
	    			
	    		}
		    	  
	    	}catch(Exception e){
	    		BT_activity_base.showAlertFromClass(getString(R.string.errorTitle), getString(R.string.cannotEmailDocument));
	           	BT_debugger.showIt(activityName + ":emailDocument EXCEPTION " + e.toString());
	    	  
	    	}
		    	  
		}else{
	   		
			BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.errorTitle), getString(R.string.fileNotDownloadedYet));
           	BT_debugger.showIt(activityName + ":emailDocument Cannot email document, cached file does not exist");
			
		}
	}		
    
	
    
    //open cached file...
    public void openDocInCache(String fileName){
        BT_debugger.showIt(activityName + ":openDocInCache: " + fileName);	
        if(BT_fileManager.doesCachedFileExist(fileName)){
        	
			try{
				
				//make sure it copied...
				if(BT_fileManager.doesCachedFileExist(fileName)){
					
		    		PackageManager pm = getPackageManager();
		    		ApplicationInfo appInfo = pm.getApplicationInfo(getPackageName(), 0);
					File theFile = new File(appInfo.dataDir + "/files/" + fileName);
					
					Intent intent = new Intent();
				    intent.setAction(Intent.ACTION_VIEW);
				    Uri uri = Uri.fromFile(theFile);
				    intent.setDataAndType(uri, "application/pdf");
	                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				    startActivity(intent);				
				
				}else{
					BT_debugger.showIt(activityName + ":openDocInCache: could not copy file to cache");
				}
				
				
			}catch(Exception e){
	    		BT_activity_base.showAlertFromClass(getString(R.string.errorTitle), getString(R.string.noNativeAppDescription));
	           	BT_debugger.showIt(activityName + ":openDocInCache EXCEPTION " + e.toString());
			}	
        
        }else{
    		BT_activity_base.showAlertFromClass(getString(R.string.errorTitle), getString(R.string.fileNotDownloadedYet));
           	BT_debugger.showIt(activityName + ":openDocInCache Cannot open document, not in cache yet: " + fileName);
        }
      
    }
    
    //launch in native browser..
    public void launchInNativeBrowser(){
       	if(this.dataURL.length() > 1){
           	BT_debugger.showIt(activityName + ":launchInNativeBrowser URL: " + this.dataURL);
			
           	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.dataURL));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(intent);				

       	}else{
    		BT_activity_base.showAlertFromClass(getString(R.string.errorTitle), getString(R.string.cannotOpenDocumentInNativeApp));
           	BT_debugger.showIt(activityName + ":launchInNativeBrowser ERROR, no URL. Cannot load local files in native browser.");
       	}
    }    
    
    //download and save file....
    public void downloadAndSaveFile(String dataURL, String saveAsFileName){
    	
       	//hide graphic...
       	documentIconView.setVisibility(View.GONE);
       	
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
			documentIconView.setVisibility(View.VISIBLE);
			openWithButton.setEnabled(true);
			downloadButton.setText(getString(R.string.refreshFromURL));
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
			 BT_debugger.showIt(activityName + ":downloading binary data from " + useURL + " Saving As: " + saveAsFileName);
			 BT_downloader objDownloader = new BT_downloader(useURL);
			 objDownloader.setSaveAsFileName(saveAsFileName);
			 @SuppressWarnings("unused")
			 String result = objDownloader.downloadAndSaveBinaryData();
			
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

		//email document...
		if(showBrowserBarEmailDocument.equalsIgnoreCase("1")){
			final Button btn = new Button(this);
			btn.setText(getString(R.string.emailAsAttachment));
			btn.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	                dialog.cancel();
	                emailDocument();
	            }
	        });
			options.add(btn);
		}
		
		//launch in native browser...
		if(showBrowserBarLaunchInNativeApp.equalsIgnoreCase("1")){
			final Button btn = new Button(this);
			btn.setText(getString(R.string.launchInNativeBrowser));
			btn.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	                dialog.cancel();
	                launchInNativeBrowser();
	            }
	        });
			options.add(btn);
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


 


