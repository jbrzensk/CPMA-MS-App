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

import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;



public class BT_screen_menuButtons extends BT_activity_base implements OnClickListener{
	
	public boolean didCreate = false;
	private boolean didLoadData = false;
	private DownloadScreenDataWorker downloadScreenDataWorker = null;
	private GetButtonImageWorkerThread getButtonImageWorkerThread = null;
	public LinearLayout containerView = null;
	private RelativeLayout containerViewHorizontal = null;
	private LinearLayout containerViewHorizontalBottom = null;
	private LinearLayout containerHorizontalButtons = null;
	private ScrollView verticalScrollView = null;
	private HorizontalScrollView horizontalScrollView = null;
	private TableLayout tableLayout = null;
	public int selectedIndex = -1;

	private String JSONData = "";
	private ArrayList<BT_item> childItems = null;
	private ArrayList<RelativeLayout> buttonBoxes = null;
	private ArrayList<RelativeLayout> buttonSquares = null;
	private ArrayList<Drawable> buttonImages = null;
	
	//properties from JSON
	public String dataURL = "";
	public String saveAsFileName = "";
	
	public String preventAllScrolling = "";
	public String buttonLayoutStyle = "";
	public String buttonLabelLayoutStyle = "";
	public String buttonLabelFontColor = "";
	public String buttonBackgroundColor = "";
	public int buttonOpacity = 100;
	
	public int buttonLabelFontSize = 0;
	public int buttonSize = 0;
	public int buttonPadding = 0;
	public int buttonCornerRadius = 0;

	
	//////////////////////////////////////////////////////////////////////////
	//activity life-cycle events.
	
	//onCreate
	@Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        this.activityName = "BT_screen_menuList";
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
		
		
		//init properties for JSON data...
		childItems = new ArrayList<BT_item>();
		dataURL = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "dataURL", "");
		saveAsFileName = this.screenData.getItemId() + "_screenData.txt";
		if(dataURL.length() < 1) BT_fileManager.deleteFile(saveAsFileName);
		
		//array holds list of "button boxes"...
		buttonBoxes = new ArrayList<RelativeLayout>();
		buttonSquares = new ArrayList<RelativeLayout>();
		buttonImages = new ArrayList<Drawable>();
		
		///////////////////////////////////////////////////////////////////
		//properties for both large and small devices...
	
		preventAllScrolling = BT_strings.getStyleValueForScreen(this.screenData, "preventAllScrolling", "0");
		buttonLayoutStyle = BT_strings.getStyleValueForScreen(this.screenData, "buttonLayoutStyle", "grid");
		buttonLabelLayoutStyle = BT_strings.getStyleValueForScreen(this.screenData, "buttonLabelLayoutStyle", "below");
		buttonLabelFontColor = BT_strings.getStyleValueForScreen(this.screenData, "buttonLabelFontColor", "#CCCCCC");
		buttonBackgroundColor = BT_strings.getStyleValueForScreen(this.screenData, "buttonBackgroundColor", "#707070");
		buttonOpacity = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "buttonOpacity", "100"));

		//settings that depend on large or small devices...
		if(cpmaandroid_appDelegate.rootApp.getRootDevice().getIsLargeDevice()){
			
			//large devices...
			buttonCornerRadius = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "buttonCornerRadiusLargeDevice", "0"));
			buttonLabelFontSize = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "buttonLabelFontSizeLargeDevice", "13"));
			buttonPadding = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "buttonPaddingLargeDevice", "15"));
			buttonSize = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "buttonSizeLargeDevice", "100"));

		}else{
			
			//small devices...
			buttonCornerRadius = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "buttonCornerRadiusSmallDevice", "0"));
			buttonLabelFontSize = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "buttonLabelFontSizeSmallDevice", "13"));
			buttonPadding = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "buttonPaddingSmallDevice", "15"));
			buttonSize = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "buttonSizeSmallDevice", "100"));

		}
		
		
		//inflate this screens layout file...
		LayoutInflater vi = (LayoutInflater)thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View thisScreensView = vi.inflate(R.layout.screen_menubuttons, null);
		
		//reference the container views in the layout file...
		containerView = (LinearLayout) thisScreensView.findViewById(R.id.containerView);
		containerViewHorizontal = (RelativeLayout) thisScreensView.findViewById(R.id.containerViewHorizontal);
		containerViewHorizontalBottom = (LinearLayout) thisScreensView.findViewById(R.id.containerViewHorizontalBottom);
		containerHorizontalButtons = (LinearLayout) thisScreensView.findViewById(R.id.containerHorizontalButtons);

		//reference to vertical ScrollView in the layout file (TableView is in scroll view)
		verticalScrollView = (ScrollView) thisScreensView.findViewById(R.id.verticalScrollView);
		verticalScrollView.setHorizontalScrollBarEnabled(false);
		verticalScrollView.setVerticalScrollBarEnabled(false);
		
		//reference to horizontal ScrollView in the layout file
		horizontalScrollView = (HorizontalScrollView) thisScreensView.findViewById(R.id.horizontalScrollView);
		horizontalScrollView.setHorizontalScrollBarEnabled(false);
		horizontalScrollView.setVerticalScrollBarEnabled(false);
		
		//reference to TableLayout in the layout file...
		tableLayout = (TableLayout) thisScreensView.findViewById(R.id.tableLayout);

		//add the view to the base view...
		baseView.addView(thisScreensView);
		
		//invalidate view to it repaints...
		baseView.invalidate();
		
		//flag as created..
        didCreate = true;
        
 		
	}//onCreate

	//onStart
	@Override 
	protected void onStart() {
		//BT_debugger.showIt(activityName + ":onStart");	
		super.onStart();
		
		//ignore this if we already created the screen...
		if(!didLoadData){
			
			if(saveAsFileName.length() > 1){
				
				//check cache...
				String parseData = "";
				if(BT_fileManager.doesCachedFileExist(saveAsFileName)){
					BT_debugger.showIt(activityName + ":onStart using cached screen data");	
					parseData = BT_fileManager.readTextFileFromCache(saveAsFileName);
					parseScreenData(parseData);
				}else{
					//get data from URL if we have one...
					if(this.dataURL.length() > 1){
						BT_debugger.showIt(activityName + ":onStart downloading screen data from URL");	
						refreshScreenData();
					}else{
						//parse with "empty" data...
						BT_debugger.showIt(activityName + ":onStart using data from app's configuration file");	
						parseScreenData("");
					}
				}
					
			}//saveAsFileName
		}//did load data
		
		
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
  
    //need to re-build the button layout on screen orientation changes..
	@Override
	public void onConfigurationChanged(Configuration newConfig){
	  super.onConfigurationChanged(newConfig);
	  layoutButtons();
	} 
   
    
    //handles button clicks
    public void onClick(View view){
        BT_debugger.showIt(activityName + ":button clicked");	
    	int buttonIndex = view.getId();
    	
     	//the BT_item
    	BT_item tappedItem = (BT_item) childItems.get(buttonIndex);
		String loadScreenWithItemId = BT_strings.getJsonPropertyValue(tappedItem.getJsonObject(), "loadScreenWithItemId", "");
		String loadScreenWithNickname = BT_strings.getJsonPropertyValue(tappedItem.getJsonObject(), "loadScreenWithNickname", "");
		
 		//bail if none...
		if(loadScreenWithItemId.equalsIgnoreCase("none")){
			return;
		}				
		
		//add animation to the tapped buttonBox...
    	RelativeLayout box = (RelativeLayout) buttonBoxes.get(view.getId());
		AlphaAnimation alphaFade = new AlphaAnimation(0.3f, 1.0f);
		alphaFade.setDuration(500);
		alphaFade.setFillAfter(true);
		box.startAnimation(alphaFade);
  		
       	//itemId, nickname or object...
        BT_item tapScreenLoadObject = null;
    	if(loadScreenWithItemId.length() > 1 && !loadScreenWithItemId.equalsIgnoreCase("none")){
			BT_debugger.showIt(activityName + ":handleItemTap loads screen with itemId: \"" + loadScreenWithItemId + "\"");
    		tapScreenLoadObject = cpmaandroid_appDelegate.rootApp.getScreenDataByItemId(loadScreenWithItemId);
    	}else{
    		if(loadScreenWithNickname.length() > 1){
				BT_debugger.showIt(activityName + ":handleItemTap loads screen with nickname: \"" + loadScreenWithNickname + "\"");
    			tapScreenLoadObject = cpmaandroid_appDelegate.rootApp.getScreenDataByItemNickname(loadScreenWithNickname);
    		}else{
    			try{
	    			JSONObject obj = tappedItem.getJsonObject();
		            if(obj.has("loadScreenObject")){
						BT_debugger.showIt(activityName + ":handleItemTap button loads screen object configured with JSON object.");
		            	JSONObject tmpLoadScreen = obj.getJSONObject("loadScreenObject");
		            	tapScreenLoadObject = new BT_item();
    		            if(tmpLoadScreen.has("itemId")) tapScreenLoadObject.setItemId(tmpLoadScreen.getString("itemId"));
    		            if(tmpLoadScreen.has("itemNickname")) tapScreenLoadObject.setItemNickname(tmpLoadScreen.getString("itemNickname"));
    		            if(tmpLoadScreen.has("itemType")) tapScreenLoadObject.setItemType(tmpLoadScreen.getString("itemType"));
    		            if(obj.has("loadScreenObject")) tapScreenLoadObject.setJsonObject(tmpLoadScreen);
		            }
    			}catch(Exception e){
					BT_debugger.showIt(activityName + ":handleItemTap EXCEPTION reading screen-object for item: " + e.toString());
    			}
    		}
    	}

    	//if we have a screen object to load from the right-button tap, build a BT_itme object...
    	if(tapScreenLoadObject != null){
    		
        	//call loadScreenObject (static method in BT_act_controller class)...
   			BT_act_controller.loadScreenObject(thisActivity, screenData, tappedItem, tapScreenLoadObject);
   		
    	}else{
			BT_debugger.showIt(activityName + ":handleItemTap ERROR. No screen is connected to this item?");	
    		BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.errorTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.errorNoScreenConnected));
    	}
    	
    }   
    
    //layout buttons
    public void layoutButtons(){
        BT_debugger.showIt(activityName + ":layoutButtons");	
        
        //device size...
        int deviceWidth = cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceWidth();
        @SuppressWarnings("unused")
		int deviceHeight = cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceHeight();
                
        //if we are in landscape orientation the sizes are backwards (intentionally, see BT_device class)...
		if(cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceOrientation().equalsIgnoreCase("landscape")){
			deviceWidth = cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceHeight();
	        deviceHeight = cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceWidth();
		}
        
		//empty list of buttons...
		buttonBoxes.clear();
		buttonSquares.clear();
		buttonImages.clear();
		
		//flag for if we are using a tableLayout or not...
		boolean usingTable = false;
		
		//font-size should not be more than 20...
		if(buttonLabelFontSize > 20){
			buttonLabelFontSize = 18;
		}
		
		//we may need to add some height if labels are "above" or "below" to button's color or image...
    	int addHeight = 0;
		if(buttonLabelLayoutStyle.equalsIgnoreCase("above") || buttonLabelLayoutStyle.equalsIgnoreCase("below")){
    		addHeight += (buttonLabelFontSize * 1.3);
    	}	
    		
		//determine how many rows / columns...
		int numButtons = childItems.size();
		int cols = 0;
		int rows = 0;
		int cnt = 0;
		
		//layout params for each table row...
		LayoutParams trLp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				
		////////////////////////////////////////////////////////////////////////////
		//grid layout
		if(buttonLayoutStyle.equalsIgnoreCase("grid") || buttonLayoutStyle.length() < 1){
			
			//hide horizontal scroller, not used...
			containerViewHorizontal.setVisibility(View.GONE);
			
			//tableLayout gets top padding same as button padding...
			tableLayout.setPadding(0, buttonPadding, 0, buttonPadding);
			tableLayout.removeAllViews();
			usingTable = true;
			
			//figure out rows / cols...
			int buttonsPerRow = (int) Math.floor(deviceWidth / (buttonSize + buttonPadding));
			rows = numButtons;
			cols = buttonsPerRow;
			
			//outer loop rows..
			int tmpRowCounter = 0;
	        while (cnt < numButtons) {
	        	
	        	//table row holds parts...
	        	TableRow tr = null;
	        	if(tmpRowCounter < buttonsPerRow){
	        		tr = new TableRow(this);
	        		tr.setLayoutParams(trLp);
	        		tr.setPadding(0, 0, 0, 0);
	        	}
	        	
	        	//re-set for each row...
	        	tmpRowCounter++;
	        	if(tmpRowCounter >= buttonsPerRow) tmpRowCounter = 0;
	        	
	            //inner loop cols..
	            for(int c = 0; c < cols; c++){
	            	
	                //increment counter here if NOT a vertical layout...
	        		if(cnt < numButtons){
		            	
		            	//BT_item...
		            	BT_item tmpItem = childItems.get(cnt);
		
		            	//relative layout holds button parts...this is the button "size" and the background color...
		            	RelativeLayout buttonBox = getButton(tmpItem, cnt, buttonSize, buttonSize + addHeight, buttonPadding, addHeight);
		            	
		                //add to list of "button boxes" to keep track of it...
		                buttonBoxes.add(buttonBox);
		        		
		                //add buttonBox to the table row...
		                tr.addView(buttonBox, buttonSize + buttonPadding, buttonSize + buttonPadding);
		                // was tr.addView(buttonBox, buttonSize + buttonPadding, buttonSize + buttonPadding + addHeight); *****************************
		                //increment counter...
		                cnt++;
	                
	        		}//cnt < numButtons...   
		           
        		
	            } //for num cols...
	            
 	    		//add the row...
	            tableLayout.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	            
	       		//if addHeight was used we need a spacer between each row..
        		if(addHeight > 0){
        			TableRow spacerRow = new TableRow(this);
        			spacerRow.setPadding(0, 0, 0, 0);
        			RelativeLayout spacerBox = new RelativeLayout(this);
        			spacerBox.setMinimumHeight(15);
        			spacerRow.addView(spacerBox, 15, 45);  //45 was 30
        			tableLayout.addView(spacerRow, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        		}
	        		            
	        } //for num rows...
	        
			
		}		
		
		//end grid layout..
		////////////////////////////////////////////////////////////////////////////
		
		////////////////////////////////////////////////////////////////////////////
		//vertical buttons, right or left side of screen...
		if(buttonLayoutStyle.equalsIgnoreCase("verticalLeft") || buttonLayoutStyle.equalsIgnoreCase("verticalRight")){
			
			//hide horizontal container, not used...
			containerViewHorizontal.setVisibility(View.GONE);
			
			//tableLayout gets top padding same as button padding...
			tableLayout.setPadding(0, buttonPadding, 0, buttonPadding);
			tableLayout.removeAllViews();
			usingTable = true;
			
			//counts...
			rows = numButtons;
			cols = 1;
			
			//left or right...
			int boxGravity = Gravity.LEFT;
			if(buttonLayoutStyle.equalsIgnoreCase("verticalRight")){
				boxGravity = Gravity.RIGHT;
			}		
		
			//outer loop rows..
	        for (int f = 0; f <= rows; f++) {
	        	
	        	//table row holds parts...
	            TableRow tr = new TableRow(this);
        		tr = new TableRow(this);
        		tr.setLayoutParams(trLp);
        		tr.setPadding(0, 0, 0, 0);
	            tr.setGravity(boxGravity);
	            
	            //inner loop cols..
	            for(int c = 0; c < cols; c++){
	            	
	                //increment counter here if NOT a vertical layout...
	        		if(cnt < numButtons){
		            	
		            	//BT_item...
		            	BT_item tmpItem = childItems.get(cnt);
		
		            	//relative layout holds button parts...this is the button "size" and the background color...
		            	RelativeLayout buttonBox = getButton(tmpItem, cnt, buttonSize, buttonSize + addHeight, buttonPadding, addHeight);
		            	
		                //add to list of "button boxes" to keep track of it...
		                buttonBoxes.add(buttonBox);
	
		            	//add to layout...
		            	RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
		        		if(buttonLayoutStyle.equalsIgnoreCase("verticalRight")){
			            	lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		        		}
		        		buttonBox.setLayoutParams(lp);
			                
		                //add buttonBox to the table row...
		                tr.addView(buttonBox, buttonSize + (buttonPadding * 2), buttonSize + buttonPadding + addHeight);
		                
		                //increment counter...
		                cnt++;
	                
	        		}//cnt > numButtons  
	        		
		           
	            } //for cols...
	            
	    		//add the row...
	            tableLayout.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	            
	       		//if addHeight was used we need a spacer between each row..
        		if(addHeight > 0){
        			TableRow spacerRow = new TableRow(this);
        			spacerRow.setPadding(0, 0, 0, 0);
        			RelativeLayout spacerBox = new RelativeLayout(this);
        			spacerBox.setMinimumHeight(30);
        			spacerRow.addView(spacerBox, 30, 30);
        			tableLayout.addView(spacerRow, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        		}	            
	            
	        } //for rows...
	        
		}		
		//end vertical buttons, right or left side of screen...
		////////////////////////////////////////////////////////////////////////////
		
		//if we used a table layout...
		//add a few empty row at end so scrolling shows enough of bottom margin...
		if(usingTable){
			for(int t = 0; t < 3; t++){
				TableRow tr = new TableRow(this);
				tr.setPadding(0, 0, 0, 0);
				RelativeLayout buttonBox = new RelativeLayout(this);
				buttonBox.setMinimumWidth(buttonSize);
				buttonBox.setMinimumHeight(buttonSize);
				tr.addView(buttonBox, buttonSize, buttonSize);
				tableLayout.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}	
			
			//invalidate tableLayout...
	        tableLayout.invalidate();

		}
		
		
		////////////////////////////////////////////////////////////////////////////
		//horizontal buttons, top or bottom of screen...
		if(buttonLayoutStyle.equalsIgnoreCase("horizontalTop") || buttonLayoutStyle.equalsIgnoreCase("horizontalBottom")){
			
			//hide vertical scroller, not used...
			verticalScrollView.setVisibility(View.GONE);
			
			//remove horizontal buttons...
			containerHorizontalButtons.removeAllViews();
			
			//assume horizontalTop
        	containerViewHorizontalBottom.setPadding(0, 0, 0, 0);
			
			//move the container view to the bottom of the screen if needed...
			if(buttonLayoutStyle.equalsIgnoreCase("horizontalBottom")){
        		containerViewHorizontalBottom.setPadding(0, 0, 0, buttonPadding);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
            	lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            	containerViewHorizontalBottom.setLayoutParams(lp);
			}
			
			//button box gravity...
			@SuppressWarnings("unused")
			int boxGravity = Gravity.LEFT;
					 
            //loop numButtons...
            for(int c = 0; c < numButtons; c++){
            	
                //increment counter here if NOT a vertical layout...
        		if(cnt < numButtons){
	            	
	            	//BT_item...
	            	BT_item tmpItem = childItems.get(cnt);
	
	            	//relative layout holds button parts...this is the button "size" and the background color...
	            	RelativeLayout buttonBox = getButton(tmpItem, cnt, buttonSize, buttonSize + buttonPadding + addHeight, buttonPadding, addHeight);
	            	
	                //add to list of "button boxes" to keep track of it...
	                buttonBoxes.add(buttonBox);

	            	//add to layout...
	            	RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(buttonSize + buttonPadding, buttonSize + buttonPadding + addHeight);
	        		buttonBox.setLayoutParams(lp);
		                
	                //add buttonBox to the table row...
	        		containerHorizontalButtons.addView(buttonBox);
	                
	                //increment counter...
	                cnt++;
                
        		}//cnt > numButtons     
	           
            } // for
			
            //add a few more to right side so scroller is long enough...
            for(int x = 0; x < 2; x++){
				RelativeLayout buttonBox = new RelativeLayout(this);
				buttonBox.setMinimumWidth(buttonSize);
				buttonBox.setMinimumHeight(buttonSize);
				containerHorizontalButtons.addView(buttonBox);
            }
	            
            //invalidate scroller...
            containerHorizontalButtons.invalidate();
	        
		}		
		//end horizontal buttons, top or bottom of screen...
		////////////////////////////////////////////////////////////////////////////

       //hide progress...
        hideProgress();
        
    }
    
    //creates and returns a RelativeLayout with button parts...
    public RelativeLayout getButton(BT_item buttonJSON, int buttonIndex, int buttonWidth, int buttonHeight, int buttonPadding, int addHeight){
    	
       	//BT_item...
		String titleText = BT_strings.getJsonPropertyValue(buttonJSON.getJsonObject(), "titleText", "");
    	String iconName = BT_strings.getJsonPropertyValue(buttonJSON.getJsonObject(), "iconName", "");
    	String imageName = BT_strings.getJsonPropertyValue(buttonJSON.getJsonObject(), "imageNameSmallDevice", "");
    	String imageURL = BT_strings.getJsonPropertyValue(buttonJSON.getJsonObject(), "imageURLSmallDevice", "");
    	if(cpmaandroid_appDelegate.rootApp.getRootDevice().getIsLargeDevice()){
    	   	imageName = BT_strings.getJsonPropertyValue(buttonJSON.getJsonObject(), "imageNameLargeDevice", "");
        	imageURL = BT_strings.getJsonPropertyValue(buttonJSON.getJsonObject(), "imageURLLargeDevice", "");
    	}
    	
    	//buttonBox is a square holding all the view parts...
    	RelativeLayout buttonBox = new RelativeLayout(this);
	   	
        //buttonBox and textLabel is taller for "above" and "below"...
        int tmpWidth = buttonSize + buttonPadding;
        int tmpHeight = buttonSize + buttonPadding + addHeight;
        if(buttonLayoutStyle.contains("vertical")){
        	tmpWidth += buttonPadding;
        	tmpHeight += buttonPadding;
        }
    	
		//we may need to add to the height of the box if we are using "above" or "below" textViews...
    	RelativeLayout.LayoutParams boxLp = new RelativeLayout.LayoutParams(tmpWidth, tmpHeight);
	   	buttonBox.setLayoutParams(boxLp);
    	buttonBox.setMinimumWidth(tmpWidth);
    	buttonBox.setMinimumHeight(tmpHeight);
    	
 	   	//create a square for background image / color...
	   	RelativeLayout bgColor = new RelativeLayout(this);
	   	RelativeLayout.LayoutParams colorLp = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
	   	colorLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
	   	
        if(buttonLabelLayoutStyle.equalsIgnoreCase("above")){
        	colorLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }else{            
        	if(buttonLabelLayoutStyle.equalsIgnoreCase("below")){
            	colorLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        	}else{
        	   	colorLp.addRule(RelativeLayout.CENTER_VERTICAL);
        	}
        }
	   	bgColor.setLayoutParams(colorLp);
	   	bgColor.setBackgroundColor(BT_color.getColorFromHexString(buttonBackgroundColor));
	   	buttonBox.addView(bgColor);
		
	   	//create an invisible button...
        Button b = new Button(this);
 		b.setBackgroundColor(Color.TRANSPARENT);
        b.setOnClickListener(this);
        b.setId(buttonIndex);
		b.setMinimumWidth(buttonSize);
   		b.setMinimumHeight(buttonSize);
         
        //add the bgColor "square" to the buttonSquares array so we can updates it's background later if we need..
        buttonSquares.add(buttonIndex, bgColor);
        
        //if we have text...
        if(titleText.length() > 1){
        	TextView t = new TextView(this);
            t.setText(titleText);
            t.setTextSize(buttonLabelFontSize);
            t.setTextColor(BT_color.getColorFromHexString(buttonLabelFontColor));
            t.setLines(1);
            t.setSingleLine(true);
            t.setLayoutParams(new LayoutParams(tmpWidth, tmpHeight));
   			t.setWidth(tmpWidth);
   			t.setMinimumWidth(tmpWidth);
   			t.setMaxWidth(tmpWidth);
   			t.setHeight(tmpHeight);
   			t.setMinimumHeight(tmpHeight);
   			t.setMaxHeight(tmpHeight);
                        
            //gravity depends on button position..."above" and "below" need a bigger buttonBox...
            if(buttonLabelLayoutStyle.equalsIgnoreCase("middle")){
            	t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);                    
            }
            if(buttonLabelLayoutStyle.equalsIgnoreCase("bottom") || buttonLabelLayoutStyle.equalsIgnoreCase("below")){
            	t.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);                    
            }            
            if(buttonLabelLayoutStyle.equalsIgnoreCase("top") || buttonLabelLayoutStyle.equalsIgnoreCase("above")){
            	t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);                    
            }              
            
            //add text label to background box...	
            buttonBox.addView(t);
        }
        
         //fill our buttonImages array with an empty image...We do this so the buttonImages array has something 
        //in this position (null to begin with). The buttonImageHandler will replace this null value with the
        //actual image after it loads it.
        buttonImages.add(null);
        
        //if we have a background image (name or url)...
        if(imageName.length() > 1 || imageURL.length() > 1){
        		
        	//if we have an image name and it's in the assets...
        	if(imageName.length() > 1){
        			
        		if(BT_fileManager.getResourceIdFromBundle("drawable", imageName) > 0){
					BT_debugger.showIt(activityName + ": using button image from project bundle: \"" + imageName + "\"");

        			
        			//do we need to round the image?
					Drawable d;
					if(buttonCornerRadius > 0){
        				d = BT_fileManager.getDrawableByName(imageName);
        				
        	       		//we have a drawable, our rounding method needs a bitmap...
                		Bitmap bg = ((BitmapDrawable)d).getBitmap();
                		bg = BT_viewUtilities.getRoundedImage(bg, buttonCornerRadius);
                		
                		//convert it back to a drawable...
                		d = new BitmapDrawable(bg);
        				
        				
        			}else{
        				d = BT_fileManager.getDrawableByName(imageName);
        			}
    				bgColor.setBackgroundDrawable(d);
        			
        		}
        		
        	}else{
            	
        		if(imageURL.length() > 1){
            		
        			
            		//get image from cache or donwload it...
            		getButtonImageWorkerThread = new GetButtonImageWorkerThread();
            		getButtonImageWorkerThread.setButtonIndex(buttonIndex);
            		getButtonImageWorkerThread.setDrawableArray(this.buttonImages);
            		getButtonImageWorkerThread.setImageName(imageName);
            		getButtonImageWorkerThread.setImageURL(imageURL);
            		getButtonImageWorkerThread.start();
            	
            	}
        		
            }
        	
        }
        
        //if we have an icon name (must be in assets folder)...put on top of background image...
        if(iconName.length() > 1){
    		if(BT_fileManager.getResourceIdFromBundle("drawable", iconName) > 0){
 				ImageView i = new ImageView(this);
				i.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				i.setScaleType(ScaleType.CENTER_INSIDE);
				i.setMaxWidth(buttonSize);
				i.setMaxHeight(buttonSize);
	           	i.setImageDrawable(BT_fileManager.getDrawableByName(iconName));
			    bgColor.addView(i);
			}
        }
        
        //handle opacity...
        if(buttonOpacity < 0){
        	double useAlpha = Double.parseDouble("." + buttonOpacity);
            Animation opacityAnimation = new AlphaAnimation(1.0f, (float) useAlpha);
            opacityAnimation.setFillAfter(true);
            bgColor.startAnimation(opacityAnimation);
        }        
        
        //add the button to the buttonBox...
        bgColor.addView(b);

    	//return...
    	return buttonBox;
    }
    
 
    //parse screenData...
    public void parseScreenData(String theJSONString){
        BT_debugger.showIt(activityName + ":parseScreenData");	
        //BT_debugger.showIt(activityName + ":parseScreenData " + theJSONString);
		//parse JSON string
    	try{

    		//empty data if previously filled...
    		childItems.clear();

            //if theJSONString is empty, look for child items in this screen's config data..
    		JSONArray items = null;
    		if(theJSONString.length() < 1){
    			if(this.screenData.getJsonObject().has("childItems")){
        			items =  this.screenData.getJsonObject().getJSONArray("childItems");
    			}
    		}else{
        		JSONObject raw = new JSONObject(theJSONString);
        		if(raw.has("childItems")){
        			items =  raw.getJSONArray("childItems");
        		}
    		}
  
    		//loop items..
    		if(items != null){
                for (int i = 0; i < items.length(); i++){
                	
                	JSONObject tmpJson = items.getJSONObject(i);
                	BT_item tmpItem = new BT_item();
                	if(tmpJson.has("itemId")) tmpItem.setItemId(tmpJson.getString("itemId"));
                	if(tmpJson.has("itemType")) tmpItem.setItemType(tmpJson.getString("itemType"));
                	tmpItem.setJsonObject(tmpJson);
                	childItems.add(tmpItem);
                	
                }//for
                
                
                //flag data loaded...
                didLoadData = true;
    			
    		}else{
    			BT_debugger.showIt(activityName + ":parseScreenData NO CHILD ITEMS?");
    			
    		}
    	}catch(Exception e){
			BT_debugger.showIt(activityName + ":parseScreenData EXCEPTION " + e.toString());
    	}
        
    	
 	    //setup list click listener
    	@SuppressWarnings("unused")
		final OnItemClickListener listItemClickHandler = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
            	
              	//the BT_item
	        	BT_item tappedItem = (BT_item) childItems.get(position);
				String titleText = BT_strings.getJsonPropertyValue(tappedItem.getJsonObject(), "titleText", "");
				String loadScreenWithItemId = BT_strings.getJsonPropertyValue(tappedItem.getJsonObject(), "loadScreenWithItemId", "");
				String loadScreenWithNickname = BT_strings.getJsonPropertyValue(tappedItem.getJsonObject(), "loadScreenWithNickname", "");
				
				//bail if none...
				if(loadScreenWithItemId.equalsIgnoreCase("none")){
					return;
				}				
				
				
	           	//itemId, nickname or object...
	            BT_item tapScreenLoadObject = null;
	        	if(loadScreenWithItemId.length() > 1 && !loadScreenWithItemId.equalsIgnoreCase("none")){
	    			BT_debugger.showIt(activityName + ":handleItemTap loads screen with itemId: \"" + loadScreenWithItemId + "\"");
	        		tapScreenLoadObject = cpmaandroid_appDelegate.rootApp.getScreenDataByItemId(loadScreenWithItemId);
	        	}else{
	        		if(loadScreenWithNickname.length() > 1){
	    				BT_debugger.showIt(activityName + ":handleItemTap loads screen with nickname: \"" + loadScreenWithNickname + "\"");
	        			tapScreenLoadObject = cpmaandroid_appDelegate.rootApp.getScreenDataByItemNickname(loadScreenWithNickname);
	        		}else{
	        			try{
	    	    			JSONObject obj = tappedItem.getJsonObject();
	    		            if(obj.has("loadScreenObject")){
	    						BT_debugger.showIt(activityName + ":handleItemTap button loads screen object configured with JSON object.");
	    		            	JSONObject tmpLoadScreen = obj.getJSONObject("loadScreenObject");
	    		            	tapScreenLoadObject = new BT_item();
	        		            if(tmpLoadScreen.has("itemId")) tapScreenLoadObject.setItemId(tmpLoadScreen.getString("itemId"));
	        		            if(tmpLoadScreen.has("itemNickname")) tapScreenLoadObject.setItemNickname(tmpLoadScreen.getString("itemNickname"));
	        		            if(tmpLoadScreen.has("itemType")) tapScreenLoadObject.setItemType(tmpLoadScreen.getString("itemType"));
	        		            if(obj.has("loadScreenObject")) tapScreenLoadObject.setJsonObject(tmpLoadScreen);
	    		            }
	        			}catch(Exception e){
	    					BT_debugger.showIt(activityName + ":handleItemTap EXCEPTION reading screen-object for item: " + e.toString());
	        			}
	        		}
	        	}

	        	//if we have a screen object to load from the right-button tap, build a BT_itme object...
	        	if(tapScreenLoadObject != null){
	        		
	            	//call loadScreenObject (static method in this class)...
	       			BT_act_controller.loadScreenObject(thisActivity, screenData, tappedItem, tapScreenLoadObject);
	       		
	        	}else{
	    			BT_debugger.showIt(activityName + ":handleItemTap ERROR. No screen is connected to this item?");	
	        		BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.errorTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.errorNoScreenConnected));
	        	}
	        	            	
           	}
        };    
        
        //layout buttons...
        layoutButtons();
        
    }
    
  
    //refresh screenData
    public void refreshScreenData(){
        BT_debugger.showIt(activityName + ":refreshScreenData");	
        showProgress(null, null);
        
        if(dataURL.length() > 1){
	      	downloadScreenDataWorker = new DownloadScreenDataWorker();
        	downloadScreenDataWorker.setDownloadURL(dataURL);
        	downloadScreenDataWorker.setSaveAsFileName(saveAsFileName);
        	downloadScreenDataWorker.setThreadRunning(true);
        	downloadScreenDataWorker.start();
        }else{
            BT_debugger.showIt(activityName + ":refreshScreenData NO DATA URL for this screen? Not downloading.");	
        }
        
    }    
       

	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//GetButtonImageWorkerThread and Handler. Loads a possible image (from bundle, cache, or a URL) then calls handler..
    private Handler buttonImageHandler = new Handler() {
        public void handleMessage(Message msg){
            //BT_debugger.showIt(activityName + ":buttonImageHandler setting background image for button.");	
        	//msg.what will equal the index of the button images array...
        	
        	//set the drawable...
        	Drawable d;
        	
        	//we may need to round the image...
        	if(buttonCornerRadius > 0){
        		d = buttonImages.get(msg.what);
        		
        		//we have a drawable, our rounding method needs a bitmap...
        		Bitmap b = ((BitmapDrawable)d).getBitmap();
        		b = BT_viewUtilities.getRoundedImage(b, buttonCornerRadius);
        		
        		//convert it back to a drawable...
        		d = new BitmapDrawable(b);
        		
        	}else{
        		d = buttonImages.get(msg.what);
        	}
        	buttonSquares.get(msg.what).setBackgroundDrawable(d);
        	buttonSquares.get(msg.what).invalidate();

        }        
	};	   
    
	public class GetButtonImageWorkerThread extends Thread{
		private int buttonIndex = -1;
		private ArrayList<Drawable> buttonImages = null;
		private String imageName = "";
		private String imageURL = "";
   	 	public void setButtonIndex(int buttonIndex){
   	 		this.buttonIndex = buttonIndex;
   	 	}
   	 	public void setDrawableArray(ArrayList<Drawable> buttonImages){
   	 		this.buttonImages = buttonImages;
   	 	}
   	 	public void setImageName(String imageName){
   	 		this.imageName = imageName;
   	 	}
   	 	public void setImageURL(String imageURL){
   	 		this.imageURL = imageURL;
   	 	}
   	 	public void run(){
   	 		try{
				 
				//use a local or cached image if we have one, else, download...
				String useImageName = "";
				if(imageName.length() > 1){
					useImageName = imageName;
				}else{
					if(imageURL.length() > 1){
						useImageName = BT_strings.getSaveAsFileNameFromURL(imageURL);
					}
				}
				
				//empty drawable...
				Drawable buttonBackgroundImage = null;
				if(useImageName.length() > 1){
						
		        		//does file exist in cache...
		        		if(BT_fileManager.doesCachedFileExist(useImageName)){
			        		
		        			BT_debugger.showIt(activityName + ":GetButtonImageWorkerThread using image from cache: \"" + useImageName + "\"");
		        			buttonBackgroundImage = BT_fileManager.getDrawableFromCache(useImageName);
	        			 	buttonImages.set(buttonIndex, buttonBackgroundImage);
		        		
		        		}else{
		        			
		        			//download from URL if we have one...
		        			if(imageURL.length() > 1){
		        	    		
		        				//if we have a url..
		        	    		if(useImageName.length() > 1){
		        	    			//don't bother pulling name from URL, already have it..
		        				}else{
		        					if(imageURL.length() > 1){
		        						useImageName = BT_strings.getSaveAsFileNameFromURL(imageURL);
		        					}
		        				}
		        	    		
		        			 	BT_downloader objDownloader = new BT_downloader(imageURL);
		        			 	objDownloader.setSaveAsFileName(useImageName);
		        			 	
		        			 	//save the background image in the array of background images...
		        			 	buttonBackgroundImage = objDownloader.downloadDrawable();
		        			 	buttonImages.set(buttonIndex, buttonBackgroundImage);

		        			 	//print to log of failed...
		        			 	if(buttonBackgroundImage == null){
		        	    			BT_debugger.showIt(activityName + ":GetButtonImageWorkerThread NOT SAVING iamge to cache (null)");
		        			 	}
		        			}
		        		}
		        			
				}else{//usesImageName
					BT_debugger.showIt(activityName + ":GetButtonImageWorkerThread this screen does not use a background image");
				}
				
				//fire handler in main UI thread if we have an image...
				if(buttonBackgroundImage != null){
					buttonImageHandler.sendEmptyMessage(buttonIndex);
				}
				
			
   	 		}catch(Exception e){
   	 			BT_debugger.showIt(activityName + ":GetButtonImageWorkerThread Exception: " + e.toString());
   	 		}
			
   	 	}//run		 
  	};
	//END DownloadImageThread and Handler
	///////////////////////////////////////////////////////////////////

   
    
    
    
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
			
			 //downloader will fetch and save data..Set this screen data as "current" to be sure the screenId
			 //in the URL gets merged properly. Several screens could be loading at the same time...
			 cpmaandroid_appDelegate.rootApp.setCurrentScreenData(screenData);
			 String useURL = BT_strings.mergeBTVariablesInString(dataURL);
			 BT_debugger.showIt(activityName + ": downloading screen data from " + useURL);
			 BT_downloader objDownloader = new BT_downloader(useURL);
			 objDownloader.setSaveAsFileName(saveAsFileName);
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
		
		//refresh screen data...
		if(dataURL.length() > 1){
			final Button btnRefreshScreenData = new Button(this);
			btnRefreshScreenData.setText(getString(R.string.refreshScreenData));
			btnRefreshScreenData.setOnClickListener(new OnClickListener(){
            	public void onClick(View v){
                	dialog.cancel();
            		refreshScreenData();
            	}
			});
			options.add(btnRefreshScreenData);
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













