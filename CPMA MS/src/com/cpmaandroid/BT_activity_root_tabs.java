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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.Toast;

public class BT_activity_root_tabs extends TabActivity implements OnTabChangeListener{

	//member variables
	private String activityName = "BT_activity_root_tabs";
	private Handler buildInterfaceHandler = null;
	private TabActivity thisActivityObject = this;
	private AlertDialog myAlert = null;
	public ProgressDialog progressBox = null;
	public BT_progressSpinner progressSpinner = null;
	public TabHost tabHost = null;
	public TabWidget tabWidget = null;
	public Integer selectedTab = 0;
	public boolean didCreate = false;

	//////////////////////////////////////////////////////////////////////////
	//activity life-cycle events.
	
    //onCreate
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		BT_debugger.showIt(activityName + ":onCreate");	
 		
		//hide title bar then set to full-screen before setting content..The AndroidManifest.xml file
        //also uses "full" screen in the application them node. 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//uncomment the next line to hide the status bar maybe hide status bar next time
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
		//set the content view 
        setContentView(R.layout.act_root_tabs);
		
		//show progress
 		showProgress(null, null);
		
	    //update device connection type...
		cpmaandroid_appDelegate.rootApp.getRootDevice().updateDeviceConnectionType();
		
		//update device size...
		cpmaandroid_appDelegate.rootApp.getRootDevice().updateDeviceSize();
		
		//get selected tab...
		selectedTab = BT_strings.getPrefInteger("selectedTab");

		//spawn thread to build the inteface...
		buildInterfaceHandler = new Handler();
		buildInterfaceThread.start();
 		
 		
    }//end onCreate
	
	//onStart
	@Override 
	protected void onStart() {
		//BT_debugger.showIt(activityName + ":onStart");	
		super.onStart();
	    
		//get selected tab...
		selectedTab = BT_strings.getPrefInteger("selectedTab");

	}
	
    //onResume
    @Override
    public void onResume() {
        //BT_debugger.showIt(activityName + ":onResume");	
       super.onResume();
       
       //get selected tab...
       selectedTab = BT_strings.getPrefInteger("selectedTab");
       
    }
    
    //onPause
    @Override
    public void onPause() {
        super.onPause();
        //BT_debugger.showIt(activityName + ":onPause");	
        
        //save previously selected tab...
        BT_strings.setPrefInteger("selectedTab", selectedTab);
         
    }
    
    //onStop
	@Override 
	protected void onStop(){
		super.onStop();
        //BT_debugger.showIt(activityName + ":onStop");	
		
	    //save previously selected tab...
        BT_strings.setPrefInteger("selectedTab", selectedTab);
 		
	}	
	
	//onDestroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        //BT_debugger.showIt(activityName + ":onDestroy");	
    }
    
	//end activity life-cycle events
	//////////////////////////////////////////////////////////////////////////

    
    
    //sets each tabs background color...
	public static void setTabColor(TabHost tabhost) {
		/* uncomment this to change tab colors when a tab is selected...
		BT_debugger.showIt(activityName + ":setTabColor");	
		for(int i = 0; i < tabhost.getTabWidget().getChildCount(); i++){
	       tabhost.getTabWidget().getChildAt(i).setBackgroundColor(BT_color.getColorFromHexString("#000000")); //unselected
	    }
	    tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundColor(BT_color.getColorFromHexString("#999999")); // selected
		*/
	}	
	
	//on tab change..
	public void onTabChanged(String tabId) {
		BT_debugger.showIt(activityName + ":onTabChanged Tab: " + tabId);	
		
		//set selected tab...
		selectedTab = Integer.parseInt(tabId);
		
		
		//json data for selected tab...
		BT_item selTabData = cpmaandroid_appDelegate.rootApp.getTabs().get(Integer.parseInt(tabId));
		String tabSoundEffectName = BT_strings.getJsonPropertyValue(selTabData.getJsonObject(), "soundEffectFileName", "");
		
    	//json data for selected tabs home screen...
    	BT_item screenData = null;
    	if(tabId.toString().equals("0")) screenData = cpmaandroid_appDelegate.rootApp.getTab0ScreenData();
    	if(tabId.toString().equals("1")) screenData = cpmaandroid_appDelegate.rootApp.getTab1ScreenData();
    	if(tabId.toString().equals("2")) screenData = cpmaandroid_appDelegate.rootApp.getTab2ScreenData();
    	if(tabId.toString().equals("3")) screenData = cpmaandroid_appDelegate.rootApp.getTab3ScreenData();
    	if(tabId.toString().equals("4")) screenData = cpmaandroid_appDelegate.rootApp.getTab4ScreenData();
   	
     	//flag this as the current working screen...
    	cpmaandroid_appDelegate.rootApp.setCurrentScreenData(screenData);
    	
    	//remember selected tab in delegate
    	cpmaandroid_appDelegate.rootApp.setSelectedTab(Integer.parseInt(tabId));
		
    	//play possible sound effect for the tab...
    	if(tabSoundEffectName.length() > 1){
    		cpmaandroid_appDelegate.playSoundEffect(tabSoundEffectName);
    	}		

		//change tab color...(setTabColor is commented out intentionally, un-comment as needed)
	    setTabColor(tabHost);
	    
		
 	}
	
	
	//show alert message
	public void showAlert(String theTitle, String theMessage) {
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
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	buildInterfaceThread and Handler
	
	//build interface...
	private Runnable buildInterfaceUsingAppData = new Runnable(){
        public void run(){
	 		
        	//debugger...
 			BT_debugger.showIt(activityName + ":buildInterfaceUsingAppData..");

			//tabbed interface or not?
			if(cpmaandroid_appDelegate.rootApp.getTabs().size() > 0){
				BT_debugger.showIt(activityName + ": setting up a tabbed interface.");

 				//setup tab host and widget..        
		        tabHost = getTabHost();
		        tabWidget = getTabWidget();

		        //process each tab...
		        TabHost.TabSpec spec;
		        Intent intent;
	    		for(int t = 0; t < cpmaandroid_appDelegate.rootApp.getTabs().size(); t++){
	    			BT_item thisTab = cpmaandroid_appDelegate.rootApp.getTabs().get(t);
		            		
	            	//look at this tabs jsonVars to determine tab options...
	            	String tabTextabel = BT_strings.getJsonPropertyValue(thisTab.getJsonObject(), "textLabel", "NA");
	            	String tabIconName = BT_strings.getJsonPropertyValue(thisTab.getJsonObject(), "iconName", "bt_blank.png");
	            	String tabHomeScreenItemId = BT_strings.getJsonPropertyValue(thisTab.getJsonObject(), "homeScreenItemId", "");

	            	//icon for this tab...
	    			Drawable tabIcon = BT_fileManager.getDrawableByName(tabIconName);
		        	
	            	//screenData for this tabs home screen...      
	            	BT_item screenData = cpmaandroid_appDelegate.rootApp.getScreenDataByItemId(tabHomeScreenItemId);
	            		
	            	//set this screenData as "isHomeScreen"
	            	if(t == 0){
		            	BT_debugger.showIt(activityName + ":tab:0 HOMESCREEN is screen with itemId: " + screenData.getItemId());
	            		screenData.setIsHomeScreen(true);
	            	}else{
	            		screenData.setIsHomeScreen(false);
	            	}
	            	
	            	//remember this as this tabs home screen
	            	if(t == 0) cpmaandroid_appDelegate.rootApp.setTab0ScreenData(screenData);
	            	if(t == 1) cpmaandroid_appDelegate.rootApp.setTab1ScreenData(screenData);
	            	if(t == 2) cpmaandroid_appDelegate.rootApp.setTab2ScreenData(screenData);
	            	if(t == 3) cpmaandroid_appDelegate.rootApp.setTab3ScreenData(screenData);
	            	if(t == 4) cpmaandroid_appDelegate.rootApp.setTab4ScreenData(screenData);
	            	
	            	//remember as this tabs home screen for tab 0...
	            	if(t == 0){
	            		cpmaandroid_appDelegate.rootApp.setCurrentScreenData(screenData);
	            	}
	            	
	            	//debugger
	            	BT_debugger.showIt(activityName + ":creating tab " + t + " with item type: " + screenData.getItemType() + " and nickname: " + screenData.getItemNickname());
					String theActivityClass = "com.cpmaandroid." + screenData.getItemType();

					try{
						
				    	intent = new Intent().setClass(cpmaandroid_appDelegate.getContext(), Class.forName(theActivityClass));
			    		intent.putExtra("tabIndex", t);
			    		spec = tabHost.newTabSpec(Integer.toString(t));
			    		spec.setIndicator(tabTextabel, tabIcon);
			    		spec.setContent(intent);
			    		tabHost.addTab(spec);
				    	
				    }catch(ClassNotFoundException e) {
						BT_debugger.showIt(activityName + ": ERROR creating Class from string: " + theActivityClass);
				    }
	            	
	            }//end for each tab
	        
	    		//color the tabs..
	    		for(int i = 0;i < tabHost.getTabWidget().getChildCount(); i++){
	    			//tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#52A2D6"));
	    		}
		    
	    		try{
	    		
	    			//refresh entire screen...
	    			tabHost.getCurrentView().invalidate();
	    			didCreate = true;

	    			//set current tab..        
	    			tabHost.setCurrentTab(selectedTab);  
	    			tabHost.setOnTabChangedListener((OnTabChangeListener) thisActivityObject);
    
	    		}catch(Exception e){
					BT_debugger.showIt(activityName + ": ERROR setting starting tab. This happens when the .java class file for this tab could not load.");
	    		}
			}	
						
			//hide progress
			hideProgress();

        }
    };	
	
    //Thread allows us to launch a handler to build the interface..
    public Thread buildInterfaceThread = new Thread(){
    	 public void run(){
			try{
				
				//configure UI in main thread...
				buildInterfaceHandler.post(buildInterfaceUsingAppData);
			
			}catch(Exception e){
    			BT_debugger.showIt(activityName + ":backgroundWorkerThread Exception: " + e.toString());
			}
			
    	 }//run		 
    };
	//END buildInterfaceThread and Handler		
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
}









































