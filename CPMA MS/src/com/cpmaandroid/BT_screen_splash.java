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

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;


public class BT_screen_splash extends BT_activity_base{
	
	//properties...
	String transitionType = "";
	int startTransitionAfterSeconds = 0;
	int transitionDurationSeconds = 0;
	
	
	//onCreate
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activityName = "BT_screen_splash";
		BT_debugger.showIt(activityName + ":onCreate");	
        
        //set the content view...
		LinearLayout baseView = (LinearLayout)findViewById(R.id.baseView);

		//setup background colors...
		BT_viewUtilities.updateBackgroundColorsForScreen(this, this.screenData);
		
		//setup background images..
		if(backgroundImageWorkerThread == null){
			backgroundImageWorkerThread = new BackgroundImageWorkerThread();
			backgroundImageWorkerThread.start();
		}			
		
		//inflate this views layout file...
		//inflate this screens layout file..
		LayoutInflater vi = (LayoutInflater)thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View thisScreensView = vi.inflate(R.layout.screen_splash, null);
		
		
		//add the view to the base view...
		baseView.addView(thisScreensView);
	
		/*
		 * *******************************************************************
		 * Notes:
		 * screen_splash.xml is an empty layout file. You can add whatever you
		 * want to it. The image for the splash screen is setup with the
		 * BT_viewUtilities.updateBackgroundColorsForScreen() method.
		 * That method sets the background image (the splash image) in this 
		 * screens parent class. BT_activity_base. See above, almost all 
		 * BT screens extend BT_activity_base
		 ********************************************************************
		*/

        
	}
	
	//onResume...
	public void onResume(){
		super.onResume();
		
		//get values..
		this.transitionType = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "transitionType", "");
		this.startTransitionAfterSeconds = Integer.parseInt(BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "startTransitionAfterSeconds", "0"));
		
		//setup transition if we don't have -1 
		if(startTransitionAfterSeconds > -1){
			delayHandler.removeCallbacks(mDelayTask);
			delayHandler.postDelayed(mDelayTask, ((startTransitionAfterSeconds + 1) * 1000));
		}
		
		
	}
	
	//handle touch event..
	@Override  
	public boolean onTouchEvent(MotionEvent event){  
	 	//BT_debugger.showIt("BT_screen_splash: touch event..");
		//ignore touch events if we have a startTransitionAfterSeconds value..
		if(startTransitionAfterSeconds < 1){
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				animateSplashScreen();
			}
		}
		return false;
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
		BT_debugger.showIt("BT_screen_splash:animateSplashScreen");
			
		
		//finish this screen so it cannot be returned to...
		BT_screen_splash.this.finish();
		
		//the next screen...
		BT_item tmpLoadScreenObject = null;
		BT_item tmpMenuItemObject = null;
		tmpMenuItemObject = new BT_item();
		tmpMenuItemObject.setItemId("tempMenuItem");
		tmpMenuItemObject.setItemNickname("tempMenuItem");
		try{
			tmpMenuItemObject.setItemType("BT_menuItem");
			tmpMenuItemObject.setJsonObject(new JSONObject("{\"transitionType\":\"fade\"}"));
		}catch(Exception e){
			
		}		
		
		
		//next screen to load...either tabbed home or the first screen in the list...
		if(cpmaandroid_appDelegate.rootApp.getTabs().size() > 0){
			BT_debugger.showIt("Building tabbed interface...");
			
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
		
			//remember current screen...
			cpmaandroid_appDelegate.rootApp.setCurrentScreenData(tmpLoadScreenObject);

			//load screen object...
			BT_act_controller.loadScreenObject(this, this.screenData, tmpMenuItemObject, tmpLoadScreenObject);
		
		}

		
	}
	
}











